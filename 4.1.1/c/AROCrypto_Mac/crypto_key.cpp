#include "crypto_key.h"
#include "crypto_common.h"
#include "crypto_bignum.h"
#include "crypto_alg.h"

BEGIN_NAMESPACE(CRYPTO)

static const char *pem_key_begin = "-----BEGIN RSA PRIVATE KEY-----";
static const char *pem_key_end = "-----END RSA PRIVATE KEY-----";
static const char *pem_key2_begin = "-----BEGIN PRIVATE KEY-----";
static const char *pem_key2_end = "-----END PRIVATE KEY-----";

static const u8 * search_tag(const char *tag, const u8 *buf, size_t len)
{
	size_t i, plen;

	plen = os_strlen(tag);
	if (len < plen)
		return NULL;

	for (i = 0; i < len - plen; i++) {
		if (os_memcmp(buf + i, tag, plen) == 0)
			return buf + i;
	}

	return NULL;
}

struct crypto_private_key * crypto_private_key_import(const u8 *key,
	size_t len,
	const char *passwd)
{
	struct crypto_private_key *res;

	/* First, check for possible PKCS #8 encoding */
	res = pkcs8_key_import(key, len);
	if (res)
		return res;

	if (passwd) {
		/* Try to parse as encrypted PKCS #8 */
		//res = pkcs8_enc_key_import(key, len, passwd);
		res = NULL;	//TODO
		if (res)
			return res;
	}

	/* Not PKCS#8, so try to import PKCS #1 encoded RSA private key */
	wpa_printf(MSG_DEBUG, "Trying to parse PKCS #1 encoded RSA private "
		"key");
	return (struct crypto_private_key *)
		crypto_rsa_import_private_key(key, len);
}

struct crypto_private_key * tlsv1_set_key_pem(const u8 *key, size_t len)
{
	const u8 *pos, *end;
	unsigned char *der;
	size_t der_len;
	struct crypto_private_key *pkey;

	pos = search_tag(pem_key_begin, key, len);
	if (!pos) {
		pos = search_tag(pem_key2_begin, key, len);
		if (!pos)
			return NULL;
		pos += os_strlen(pem_key2_begin);
		end = search_tag(pem_key2_end, pos, key + len - pos);
		if (!end)
			return NULL;
	} else {
		pos += os_strlen(pem_key_begin);
		end = search_tag(pem_key_end, pos, key + len - pos);
		if (!end)
			return NULL;
	}

	der = base64_decode(pos, end - pos, &der_len);
	if (!der)
		return NULL;
	pkey = crypto_private_key_import(der, der_len, NULL);
	os_free(der);
	return pkey;
}

int asn1_get_next(const u8 *buf, size_t len, struct asn1_hdr *hdr)
{
	const u8 *pos, *end;
	u8 tmp;

	os_memset(hdr, 0, sizeof(*hdr));
	pos = buf;
	end = buf + len;

	hdr->identifier = *pos++;
	hdr->_class = hdr->identifier >> 6;
	hdr->constructed = !!(hdr->identifier & (1 << 5));

	if ((hdr->identifier & 0x1f) == 0x1f) {
		hdr->tag = 0;
		do {
			if (pos >= end) {
				wpa_printf(MSG_DEBUG, "ASN.1: Identifier "
					"underflow");
				return -1;
			}
			tmp = *pos++;
			wpa_printf(MSG_MSGDUMP, "ASN.1: Extended tag data: "
				"0x%02x", tmp);
			hdr->tag = (hdr->tag << 7) | (tmp & 0x7f);
		} while (tmp & 0x80);
	} else
		hdr->tag = hdr->identifier & 0x1f;

	tmp = *pos++;
	if (tmp & 0x80) {
		if (tmp == 0xff) {
			wpa_printf(MSG_DEBUG, "ASN.1: Reserved length "
				"value 0xff used");
			return -1;
		}
		tmp &= 0x7f; /* number of subsequent octets */
		hdr->length = 0;
		if (tmp > 4) {
			wpa_printf(MSG_DEBUG, "ASN.1: Too long length field");
			return -1;
		}
		while (tmp--) {
			if (pos >= end) {
				wpa_printf(MSG_DEBUG, "ASN.1: Length "
					"underflow");
				return -1;
			}
			hdr->length = (hdr->length << 8) | *pos++;
		}
	} else {
		/* Short form - length 0..127 in one octet */
		hdr->length = tmp;
	}

	if (end < pos || hdr->length > (unsigned int) (end - pos)) {
		wpa_printf(MSG_DEBUG, "ASN.1: Contents underflow");
		return -1;
	}

	hdr->payload = pos;
	return 0;
}


struct crypto_private_key * pkcs8_key_import(const u8 *buf, size_t len)
{
	struct asn1_hdr hdr;
	const u8 *pos, *end;
	struct bignum *zero;
	struct asn1_oid oid;
	char obuf[80];

	/* PKCS #8, Chapter 6 */

	/* PrivateKeyInfo ::= SEQUENCE */
	if (asn1_get_next(buf, len, &hdr) < 0 ||
	    hdr._class != ASN1_CLASS_UNIVERSAL ||
	    hdr.tag != ASN1_TAG_SEQUENCE) {
		wpa_printf(MSG_DEBUG, "PKCS #8: Does not start with PKCS #8 "
			   "header (SEQUENCE); assume PKCS #8 not used");
		return NULL;
	}
	pos = hdr.payload;
	end = pos + hdr.length;

	/* version Version (Version ::= INTEGER) */
	if (asn1_get_next(pos, end - pos, &hdr) < 0 ||
	    hdr._class != ASN1_CLASS_UNIVERSAL || hdr.tag != ASN1_TAG_INTEGER) {
		wpa_printf(MSG_DEBUG, "PKCS #8: Expected INTEGER - found "
			   "class %d tag 0x%x; assume PKCS #8 not used",
			   hdr._class, hdr.tag);
		return NULL;
	}

	zero = bignum_init();
	if (zero == NULL)
		return NULL;

	if (bignum_set_unsigned_bin(zero, hdr.payload, hdr.length) < 0) {
		wpa_printf(MSG_DEBUG, "PKCS #8: Failed to parse INTEGER");
		bignum_deinit(zero);
		return NULL;
	}
	pos = hdr.payload + hdr.length;

	if (bignum_cmp_d(zero, 0) != 0) {
		wpa_printf(MSG_DEBUG, "PKCS #8: Expected zero INTEGER in the "
			   "beginning of private key; not found; assume "
			   "PKCS #8 not used");
		bignum_deinit(zero);
		return NULL;
	}
	bignum_deinit(zero);

	/* privateKeyAlgorithm PrivateKeyAlgorithmIdentifier
	 * (PrivateKeyAlgorithmIdentifier ::= AlgorithmIdentifier) */
	if (asn1_get_next(pos, len, &hdr) < 0 ||
	    hdr._class != ASN1_CLASS_UNIVERSAL ||
	    hdr.tag != ASN1_TAG_SEQUENCE) {
		wpa_printf(MSG_DEBUG, "PKCS #8: Expected SEQUENCE "
			   "(AlgorithmIdentifier) - found class %d tag 0x%x; "
			   "assume PKCS #8 not used",
			   hdr._class, hdr.tag);
		return NULL;
	}

	if (asn1_get_oid(hdr.payload, hdr.length, &oid, &pos)) {
		wpa_printf(MSG_DEBUG, "PKCS #8: Failed to parse OID "
			   "(algorithm); assume PKCS #8 not used");
		return NULL;
	}

	asn1_oid_to_str(&oid, obuf, sizeof(obuf));
	wpa_printf(MSG_DEBUG, "PKCS #8: algorithm=%s", obuf);

	if (oid.len != 7 ||
	    oid.oid[0] != 1 /* iso */ ||
	    oid.oid[1] != 2 /* member-body */ ||
	    oid.oid[2] != 840 /* us */ ||
	    oid.oid[3] != 113549 /* rsadsi */ ||
	    oid.oid[4] != 1 /* pkcs */ ||
	    oid.oid[5] != 1 /* pkcs-1 */ ||
	    oid.oid[6] != 1 /* rsaEncryption */) {
		wpa_printf(MSG_DEBUG, "PKCS #8: Unsupported private key "
			   "algorithm %s", obuf);
		return NULL;
	}

	pos = hdr.payload + hdr.length;

	/* privateKey PrivateKey (PrivateKey ::= OCTET STRING) */
	if (asn1_get_next(pos, end - pos, &hdr) < 0 ||
	    hdr._class != ASN1_CLASS_UNIVERSAL ||
	    hdr.tag != ASN1_TAG_OCTETSTRING) {
		wpa_printf(MSG_DEBUG, "PKCS #8: Expected OCTETSTRING "
			   "(privateKey) - found class %d tag 0x%x",
			   hdr._class, hdr.tag);
		return NULL;
	}
	wpa_printf(MSG_DEBUG, "PKCS #8: Try to parse RSAPrivateKey");

	return (struct crypto_private_key *)
		crypto_rsa_import_private_key(hdr.payload, hdr.length);
}

static const unsigned char base64_table[65] =
	"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
/**
 * base64_decode - Base64 decode
 * @src: Data to be decoded
 * @len: Length of the data to be decoded
 * @out_len: Pointer to output length variable
 * Returns: Allocated buffer of out_len bytes of decoded data,
 * or %NULL on failure
 *
 * Caller is responsible for freeing the returned buffer.
 */
unsigned char * base64_decode(const unsigned char *src, size_t len,
			      size_t *out_len)
{
	unsigned char dtable[256], *out, *pos, block[4], tmp;
	size_t i, count, olen;
	int pad = 0;

	os_memset(dtable, 0x80, 256);
	for (i = 0; i < sizeof(base64_table) - 1; i++)
		dtable[base64_table[i]] = (unsigned char) i;
	dtable['='] = 0;

	count = 0;
	for (i = 0; i < len; i++) {
		if (dtable[src[i]] != 0x80)
			count++;
	}

	if (count == 0 || count % 4)
		return NULL;

	olen = count / 4 * 3;
	pos = out = (unsigned char *)os_malloc(olen);
	if (out == NULL)
		return NULL;

	count = 0;
	for (i = 0; i < len; i++) {
		tmp = dtable[src[i]];
		if (tmp == 0x80)
			continue;

		if (src[i] == '=')
			pad++;
		block[count] = tmp;
		count++;
		if (count == 4) {
			*pos++ = (block[0] << 2) | (block[1] >> 4);
			*pos++ = (block[1] << 4) | (block[2] >> 2);
			*pos++ = (block[2] << 6) | block[3];
			count = 0;
			if (pad) {
				if (pad == 1)
					pos--;
				else if (pad == 2)
					pos -= 2;
				else {
					/* Invalid padding */
					os_free(out);
					return NULL;
				}
				break;
			}
		}
	}

	*out_len = pos - out;
	return out;
}


/**
 * crypto_rsa_import_private_key - Import an RSA private key
 * @buf: Key buffer (DER encoded RSA private key)
 * @len: Key buffer length in bytes
 * Returns: Pointer to the private key or %NULL on failure
 */
struct crypto_rsa_key *
crypto_rsa_import_private_key(const u8 *buf, size_t len)
{
	struct crypto_rsa_key *key;
	struct bignum *zero;
	struct asn1_hdr hdr;
	const u8 *pos, *end;

	key = (struct crypto_rsa_key *) os_zalloc(sizeof(*key));
	if (key == NULL)
		return NULL;

	key->private_key = 1;

	key->n = bignum_init();
	key->e = bignum_init();
	key->d = bignum_init();
	key->p = bignum_init();
	key->q = bignum_init();
	key->dmp1 = bignum_init();
	key->dmq1 = bignum_init();
	key->iqmp = bignum_init();

	if (key->n == NULL || key->e == NULL || key->d == NULL ||
	    key->p == NULL || key->q == NULL || key->dmp1 == NULL ||
	    key->dmq1 == NULL || key->iqmp == NULL) {
		crypto_rsa_free(key);
		return NULL;
	}

	/*
	 * PKCS #1, 7.2:
	 * RSAPrivateKey ::= SEQUENCE {
	 *    version Version,
	 *    modulus INTEGER, -- n
	 *    publicExponent INTEGER, -- e
	 *    privateExponent INTEGER, -- d
	 *    prime1 INTEGER, -- p
	 *    prime2 INTEGER, -- q
	 *    exponent1 INTEGER, -- d mod (p-1)
	 *    exponent2 INTEGER, -- d mod (q-1)
	 *    coefficient INTEGER -- (inverse of q) mod p
	 * }
	 *
	 * Version ::= INTEGER -- shall be 0 for this version of the standard
	 */
	if (asn1_get_next(buf, len, &hdr) < 0 ||
	    hdr._class != ASN1_CLASS_UNIVERSAL ||
	    hdr.tag != ASN1_TAG_SEQUENCE) {
		wpa_printf(MSG_DEBUG, "RSA: Expected SEQUENCE "
			   "(public key) - found class %d tag 0x%x",
			   hdr._class, hdr.tag);
		goto error;
	}
	pos = hdr.payload;
	end = pos + hdr.length;

	zero = bignum_init();
	if (zero == NULL)
		goto error;
	pos = crypto_rsa_parse_integer(pos, end, zero);
	if (pos == NULL || bignum_cmp_d(zero, 0) != 0) {
		wpa_printf(MSG_DEBUG, "RSA: Expected zero INTEGER in the "
			   "beginning of private key; not found");
		bignum_deinit(zero);
		goto error;
	}
	bignum_deinit(zero);

	pos = crypto_rsa_parse_integer(pos, end, key->n);
	pos = crypto_rsa_parse_integer(pos, end, key->e);
	pos = crypto_rsa_parse_integer(pos, end, key->d);
	pos = crypto_rsa_parse_integer(pos, end, key->p);
	pos = crypto_rsa_parse_integer(pos, end, key->q);
	pos = crypto_rsa_parse_integer(pos, end, key->dmp1);
	pos = crypto_rsa_parse_integer(pos, end, key->dmq1);
	pos = crypto_rsa_parse_integer(pos, end, key->iqmp);

	if (pos == NULL)
		goto error;

	if (pos != end) {
		/*
		wpa_hexdump(MSG_DEBUG,
			    "RSA: Extra data in public key SEQUENCE",
			    pos, end - pos);
		*/
		goto error;
	}

	return key;

error:
	crypto_rsa_free(key);
	return NULL;
}


int crypto_private_key_decrypt_pkcs1_v15(struct crypto_private_key *key,
										 const u8 *in, size_t inlen,
										 u8 *out, size_t *outlen)
{
	return pkcs1_v15_private_key_decrypt((struct crypto_rsa_key *) key,
		in, inlen, out, outlen);
}


int pkcs1_v15_private_key_decrypt(struct crypto_rsa_key *key,
								  const u8 *in, size_t inlen,
								  u8 *out, size_t *outlen)
{
	int res;
	u8 *pos, *end;

	res = crypto_rsa_exptmod(in, inlen, out, outlen, key, 1);
	if (res)
		return res;

	if (*outlen < 2 || out[0] != 0 || out[1] != 2)
		return -1;

	/* Skip PS (pseudorandom non-zero octets) */
	pos = out + 2;
	end = out + *outlen;
	while (*pos && pos < end)
		pos++;
	if (pos == end)
		return -1;
	pos++;

	*outlen -= pos - out;

	/* Strip PKCS #1 header */
	os_memmove(out, pos, *outlen);

	return 0;
}


int asn1_get_oid(const u8 *buf, size_t len, struct asn1_oid *oid,
				 const u8 **next)
{
	struct asn1_hdr hdr;

	if (asn1_get_next(buf, len, &hdr) < 0 || hdr.length == 0)
		return -1;

	if (hdr._class != ASN1_CLASS_UNIVERSAL || hdr.tag != ASN1_TAG_OID) {
		wpa_printf(MSG_DEBUG, "ASN.1: Expected OID - found class %d "
			"tag 0x%x", hdr._class, hdr.tag);
		return -1;
	}

	*next = hdr.payload + hdr.length;

	return asn1_parse_oid(hdr.payload, hdr.length, oid);
}


int asn1_parse_oid(const u8 *buf, size_t len, struct asn1_oid *oid)
{
	const u8 *pos, *end;
	unsigned long val;
	u8 tmp;

	os_memset(oid, 0, sizeof(*oid));

	pos = buf;
	end = buf + len;

	while (pos < end) {
		val = 0;

		do {
			if (pos >= end)
				return -1;
			tmp = *pos++;
			val = (val << 7) | (tmp & 0x7f);
		} while (tmp & 0x80);

		if (oid->len >= ASN1_MAX_OID_LEN) {
			wpa_printf(MSG_DEBUG, "ASN.1: Too long OID value");
			return -1;
		}
		if (oid->len == 0) {
			/*
			 * The first octet encodes the first two object
			 * identifier components in (X*40) + Y formula.
			 * X = 0..2.
			 */
			oid->oid[0] = val / 40;
			if (oid->oid[0] > 2)
				oid->oid[0] = 2;
			oid->oid[1] = val - oid->oid[0] * 40;
			oid->len = 2;
		} else
			oid->oid[oid->len++] = val;
	}

	return 0;
}

void asn1_oid_to_str(struct asn1_oid *oid, char *buf, size_t len)
{
	char *pos = buf;
	size_t i;
	int ret;

	if (len == 0)
		return;

	buf[0] = '\0';

	for (i = 0; i < oid->len; i++) {
		ret = os_snprintf(pos, buf + len - pos,
			"%s%lu",
			i == 0 ? "" : ".", oid->oid[i]);
		if (ret < 0 || ret >= buf + len - pos)
			break;
		pos += ret;
	}
	buf[len - 1] = '\0';
}

static const u8 * crypto_rsa_parse_integer(const u8 *pos, const u8 *end,
struct bignum *num)
{
	struct asn1_hdr hdr;

	if (pos == NULL)
		return NULL;

	if (asn1_get_next(pos, end - pos, &hdr) < 0 ||
		hdr._class != ASN1_CLASS_UNIVERSAL || hdr.tag != ASN1_TAG_INTEGER) {
			wpa_printf(MSG_DEBUG, "RSA: Expected INTEGER - found class %d "
				"tag 0x%x", hdr._class, hdr.tag);
			return NULL;
	}

	if (bignum_set_unsigned_bin(num, hdr.payload, hdr.length) < 0) {
		wpa_printf(MSG_DEBUG, "RSA: Failed to parse INTEGER");
		return NULL;
	}

	return hdr.payload + hdr.length;
}

END_NAMESPACE