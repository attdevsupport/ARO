#include "crypto_common.h"
#include "crypto_alg.h"
#include "crypto_key.h"
#include "crypto_bignum.h"

BEGIN_NAMESPACE(CRYPTO)

void cryptoapi_report_error(const char * msg) {
	DWORD err = GetLastError();
	//assert(0);
}

int cryptoapi_hash_vector(ALG_ID alg, size_t hash_len, size_t num_elem,
						  const u8 *addr[], const size_t *len, u8 *mac)
{
	HCRYPTPROV prov;
	HCRYPTHASH hash;
	size_t i;
	DWORD hlen;
	int ret = 0;

	/*
	if (!CryptAcquireContext(&prov, NULL, NULL, PROV_RSA_FULL, 0)) {
		cryptoapi_report_error("CryptAcquireContext");
		return -1;
	}
	*/

	BOOL r = CryptAcquireContext(&prov, NULL, NULL, PROV_RSA_FULL, 0);
	if (!r) {
		if (GetLastError() == NTE_BAD_KEYSET) {
			r = CryptAcquireContext(&prov, NULL, NULL, PROV_RSA_FULL, CRYPT_NEWKEYSET);
		}
	}

	if (!r) {
		cryptoapi_report_error("CryptAcquireContext");
		return -1;
	}

	if (!CryptCreateHash(prov, alg, 0, 0, &hash)) {
		cryptoapi_report_error("CryptCreateHash");
		CryptReleaseContext(prov, 0);
		return -1;
	}

	for (i = 0; i < num_elem; i++) {
		if (!CryptHashData(hash, (BYTE *) addr[i], len[i], 0)) {
			cryptoapi_report_error("CryptHashData");
			CryptDestroyHash(hash);
			CryptReleaseContext(prov, 0);
		}
	}

	hlen = hash_len;
	if (!CryptGetHashParam(hash, HP_HASHVAL, mac, &hlen, 0)) {
		cryptoapi_report_error("CryptGetHashParam");
		ret = -1;
	}

	CryptDestroyHash(hash);
	CryptReleaseContext(prov, 0);

	return ret;
}


/**
 * hmac_md5_vector - HMAC-MD5 over data vector (RFC 2104)
 * @key: Key for HMAC operations
 * @key_len: Length of the key in bytes
 * @num_elem: Number of elements in the data vector
 * @addr: Pointers to the data areas
 * @len: Lengths of the data blocks
 * @mac: Buffer for the hash (16 bytes)
 * Returns: 0 on success, -1 on failure
 */
int hmac_md5_vector(const u8 *key, size_t key_len, size_t num_elem,
		    const u8 *addr[], const size_t *len, u8 *mac)
{
	u8 k_pad[64]; /* padding - key XORd with ipad/opad */
	u8 tk[16];
	const u8 *_addr[6];
	size_t i, _len[6];

	if (num_elem > 5) {
		/*
		 * Fixed limit on the number of fragments to avoid having to
		 * allocate memory (which could fail).
		 */
		return -1;
	}

        /* if key is longer than 64 bytes reset it to key = MD5(key) */
        if (key_len > 64) {
		if (md5_vector(1, &key, &key_len, tk))
			return -1;
		key = tk;
		key_len = 16;
        }

	/* the HMAC_MD5 transform looks like:
	 *
	 * MD5(K XOR opad, MD5(K XOR ipad, text))
	 *
	 * where K is an n byte key
	 * ipad is the byte 0x36 repeated 64 times
	 * opad is the byte 0x5c repeated 64 times
	 * and text is the data being protected */

	/* start out by storing key in ipad */
	os_memset(k_pad, 0, sizeof(k_pad));
	os_memcpy(k_pad, key, key_len);

	/* XOR key with ipad values */
	for (i = 0; i < 64; i++)
		k_pad[i] ^= 0x36;

	/* perform inner MD5 */
	_addr[0] = k_pad;
	_len[0] = 64;
	for (i = 0; i < num_elem; i++) {
		_addr[i + 1] = addr[i];
		_len[i + 1] = len[i];
	}
	if (md5_vector(1 + num_elem, _addr, _len, mac))
		return -1;

	os_memset(k_pad, 0, sizeof(k_pad));
	os_memcpy(k_pad, key, key_len);
	/* XOR key with opad values */
	for (i = 0; i < 64; i++)
		k_pad[i] ^= 0x5c;

	/* perform outer MD5 */
	_addr[0] = k_pad;
	_len[0] = 64;
	_addr[1] = mac;
	_len[1] = MD5_MAC_LEN;
	return md5_vector(2, _addr, _len, mac);
}


/**
 * hmac_md5 - HMAC-MD5 over data buffer (RFC 2104)
 * @key: Key for HMAC operations
 * @key_len: Length of the key in bytes
 * @data: Pointers to the data area
 * @data_len: Length of the data area
 * @mac: Buffer for the hash (16 bytes)
 * Returns: 0 on success, -1 on failure
 */
int hmac_md5(const u8 *key, size_t key_len, const u8 *data, size_t data_len,
	      u8 *mac)
{
	return hmac_md5_vector(key, key_len, 1, &data, &data_len, mac);
}


int md5_vector(size_t num_elem, const u8 *addr[], const size_t *len, u8 *mac)
{
	return cryptoapi_hash_vector(CALG_MD5, 16, num_elem, addr, len, mac);
}

/**
 * hmac_md5_vector_non_fips_allow - HMAC-MD5 over data vector (RFC 2104)
 * @key: Key for HMAC operations
 * @key_len: Length of the key in bytes
 * @num_elem: Number of elements in the data vector
 * @addr: Pointers to the data areas
 * @len: Lengths of the data blocks
 * @mac: Buffer for the hash (16 bytes)
 * Returns: 0 on success, -1 on failure
 */
int hmac_md5_vector_non_fips_allow(const u8 *key, size_t key_len,
				   size_t num_elem, const u8 *addr[],
				   const size_t *len, u8 *mac)
{
	u8 k_pad[64]; /* padding - key XORd with ipad/opad */
	u8 tk[16];
	const u8 *_addr[6];
	size_t i, _len[6];

	if (num_elem > 5) {
		/*
		 * Fixed limit on the number of fragments to avoid having to
		 * allocate memory (which could fail).
		 */
		return -1;
	}

        /* if key is longer than 64 bytes reset it to key = MD5(key) */
        if (key_len > 64) {
		if (md5_vector_non_fips_allow(1, &key, &key_len, tk))
			return -1;
		key = tk;
		key_len = 16;
        }

	/* the HMAC_MD5 transform looks like:
	 *
	 * MD5(K XOR opad, MD5(K XOR ipad, text))
	 *
	 * where K is an n byte key
	 * ipad is the byte 0x36 repeated 64 times
	 * opad is the byte 0x5c repeated 64 times
	 * and text is the data being protected */

	/* start out by storing key in ipad */
	os_memset(k_pad, 0, sizeof(k_pad));
	os_memcpy(k_pad, key, key_len);

	/* XOR key with ipad values */
	for (i = 0; i < 64; i++)
		k_pad[i] ^= 0x36;

	/* perform inner MD5 */
	_addr[0] = k_pad;
	_len[0] = 64;
	for (i = 0; i < num_elem; i++) {
		_addr[i + 1] = addr[i];
		_len[i + 1] = len[i];
	}
	if (md5_vector_non_fips_allow(1 + num_elem, _addr, _len, mac))
		return -1;

	os_memset(k_pad, 0, sizeof(k_pad));
	os_memcpy(k_pad, key, key_len);
	/* XOR key with opad values */
	for (i = 0; i < 64; i++)
		k_pad[i] ^= 0x5c;

	/* perform outer MD5 */
	_addr[0] = k_pad;
	_len[0] = 64;
	_addr[1] = mac;
	_len[1] = MD5_MAC_LEN;
	return md5_vector_non_fips_allow(2, _addr, _len, mac);
}

int sha1_vector(size_t num_elem, const u8 *addr[], const size_t *len, u8 *mac)
{
	return cryptoapi_hash_vector(CALG_SHA, 20, num_elem, addr, len, mac);
}

/**
 * hmac_sha1_vector - HMAC-SHA1 over data vector (RFC 2104)
 * @key: Key for HMAC operations
 * @key_len: Length of the key in bytes
 * @num_elem: Number of elements in the data vector
 * @addr: Pointers to the data areas
 * @len: Lengths of the data blocks
 * @mac: Buffer for the hash (20 bytes)
 * Returns: 0 on success, -1 on failure
 */
int hmac_sha1_vector(const u8 *key, size_t key_len, size_t num_elem,
		     const u8 *addr[], const size_t *len, u8 *mac)
{
	unsigned char k_pad[64]; /* padding - key XORd with ipad/opad */
	unsigned char tk[20];
	const u8 *_addr[6];
	size_t _len[6], i;

	if (num_elem > 5) {
		/*
		 * Fixed limit on the number of fragments to avoid having to
		 * allocate memory (which could fail).
		 */
		return -1;
	}

        /* if key is longer than 64 bytes reset it to key = SHA1(key) */
        if (key_len > 64) {
		if (sha1_vector(1, &key, &key_len, tk))
			return -1;
		key = tk;
		key_len = 20;
        }

	/* the HMAC_SHA1 transform looks like:
	 *
	 * SHA1(K XOR opad, SHA1(K XOR ipad, text))
	 *
	 * where K is an n byte key
	 * ipad is the byte 0x36 repeated 64 times
	 * opad is the byte 0x5c repeated 64 times
	 * and text is the data being protected */

	/* start out by storing key in ipad */
	os_memset(k_pad, 0, sizeof(k_pad));
	os_memcpy(k_pad, key, key_len);
	/* XOR key with ipad values */
	for (i = 0; i < 64; i++)
		k_pad[i] ^= 0x36;

	/* perform inner SHA1 */
	_addr[0] = k_pad;
	_len[0] = 64;
	for (i = 0; i < num_elem; i++) {
		_addr[i + 1] = addr[i];
		_len[i + 1] = len[i];
	}
	if (sha1_vector(1 + num_elem, _addr, _len, mac))
		return -1;

	os_memset(k_pad, 0, sizeof(k_pad));
	os_memcpy(k_pad, key, key_len);
	/* XOR key with opad values */
	for (i = 0; i < 64; i++)
		k_pad[i] ^= 0x5c;

	/* perform outer SHA1 */
	_addr[0] = k_pad;
	_len[0] = 64;
	_addr[1] = mac;
	_len[1] = SHA1_MAC_LEN;
	return sha1_vector(2, _addr, _len, mac);
}


/**
 * tls_prf - Pseudo-Random Function for TLS (TLS-PRF, RFC 2246)
 * @secret: Key for PRF
 * @secret_len: Length of the key in bytes
 * @label: A unique label for each purpose of the PRF
 * @seed: Seed value to bind into the key
 * @seed_len: Length of the seed
 * @out: Buffer for the generated pseudo-random key
 * @outlen: Number of bytes of key to generate
 * Returns: 0 on success, -1 on failure.
 *
 * This function is used to derive new, cryptographically separate keys from a
 * given key in TLS. This PRF is defined in RFC 2246, Chapter 5.
 */
int tls_prf(const u8 *secret, size_t secret_len, const char *label,
	    const u8 *seed, size_t seed_len, u8 *out, size_t outlen)
{
	size_t L_S1, L_S2, i;
	const u8 *S1, *S2;
	u8 A_MD5[MD5_MAC_LEN], A_SHA1[SHA1_MAC_LEN];
	u8 P_MD5[MD5_MAC_LEN], P_SHA1[SHA1_MAC_LEN];
	int MD5_pos, SHA1_pos;
	const u8 *MD5_addr[3];
	size_t MD5_len[3];
	const unsigned char *SHA1_addr[3];
	size_t SHA1_len[3];

	if (secret_len & 1)
		return -1;

	MD5_addr[0] = A_MD5;
	MD5_len[0] = MD5_MAC_LEN;
	MD5_addr[1] = (unsigned char *) label;
	MD5_len[1] = os_strlen(label);
	MD5_addr[2] = seed;
	MD5_len[2] = seed_len;

	SHA1_addr[0] = A_SHA1;
	SHA1_len[0] = SHA1_MAC_LEN;
	SHA1_addr[1] = (unsigned char *) label;
	SHA1_len[1] = os_strlen(label);
	SHA1_addr[2] = seed;
	SHA1_len[2] = seed_len;

	/* RFC 2246, Chapter 5
	 * A(0) = seed, A(i) = HMAC(secret, A(i-1))
	 * P_hash = HMAC(secret, A(1) + seed) + HMAC(secret, A(2) + seed) + ..
	 * PRF = P_MD5(S1, label + seed) XOR P_SHA-1(S2, label + seed)
	 */

	L_S1 = L_S2 = (secret_len + 1) / 2;
	S1 = secret;
	S2 = secret + L_S1;
	if (secret_len & 1) {
		/* The last byte of S1 will be shared with S2 */
		S2--;
	}

	hmac_md5_vector_non_fips_allow(S1, L_S1, 2, &MD5_addr[1], &MD5_len[1],
				       A_MD5);
	hmac_sha1_vector(S2, L_S2, 2, &SHA1_addr[1], &SHA1_len[1], A_SHA1);

	MD5_pos = MD5_MAC_LEN;
	SHA1_pos = SHA1_MAC_LEN;
	for (i = 0; i < outlen; i++) {
		if (MD5_pos == MD5_MAC_LEN) {
			hmac_md5_vector_non_fips_allow(S1, L_S1, 3, MD5_addr,
						       MD5_len, P_MD5);
			MD5_pos = 0;
			hmac_md5_non_fips_allow(S1, L_S1, A_MD5, MD5_MAC_LEN,
						A_MD5);
		}
		if (SHA1_pos == SHA1_MAC_LEN) {
			hmac_sha1_vector(S2, L_S2, 3, SHA1_addr, SHA1_len,
					 P_SHA1);
			SHA1_pos = 0;
			hmac_sha1(S2, L_S2, A_SHA1, SHA1_MAC_LEN, A_SHA1);
		}

		out[i] = P_MD5[MD5_pos] ^ P_SHA1[SHA1_pos];

		MD5_pos++;
		SHA1_pos++;
	}

	return 0;
}


/**
 * hmac_sha1 - HMAC-SHA1 over data buffer (RFC 2104)
 * @key: Key for HMAC operations
 * @key_len: Length of the key in bytes
 * @data: Pointers to the data area
 * @data_len: Length of the data area
 * @mac: Buffer for the hash (20 bytes)
 * Returns: 0 on success, -1 of failure
 */
int hmac_sha1(const u8 *key, size_t key_len, const u8 *data, size_t data_len,
	       u8 *mac)
{
	return hmac_sha1_vector(key, key_len, 1, &data, &data_len, mac);
}


/**
 * crypto_rsa_exptmod - RSA modular exponentiation
 * @in: Input data
 * @inlen: Input data length
 * @out: Buffer for output data
 * @outlen: Maximum size of the output buffer and used size on success
 * @key: RSA key
 * @use_private: 1 = Use RSA private key, 0 = Use RSA public key
 * Returns: 0 on success, -1 on failure
 */
int crypto_rsa_exptmod(const u8 *in, size_t inlen, u8 *out, size_t *outlen,
		       struct crypto_rsa_key *key, int use_private)
{
	struct bignum *tmp, *a = NULL, *b = NULL;
	int ret = -1;
	size_t modlen;

	if (use_private && !key->private_key)
		return -1;

	tmp = bignum_init();
	if (tmp == NULL)
		return -1;

	if (bignum_set_unsigned_bin(tmp, in, inlen) < 0)
		goto error;
	if (bignum_cmp(key->n, tmp) < 0) {
		/* Too large input value for the RSA key modulus */
		goto error;
	}

	if (use_private) {
		/*
		 * Decrypt (or sign) using Chinese remainer theorem to speed
		 * up calculation. This is equivalent to tmp = tmp^d mod n
		 * (which would require more CPU to calculate directly).
		 *
		 * dmp1 = (1/e) mod (p-1)
		 * dmq1 = (1/e) mod (q-1)
		 * iqmp = (1/q) mod p, where p > q
		 * m1 = c^dmp1 mod p
		 * m2 = c^dmq1 mod q
		 * h = q^-1 (m1 - m2) mod p
		 * m = m2 + hq
		 */
		a = bignum_init();
		b = bignum_init();
		if (a == NULL || b == NULL)
			goto error;

		/* a = tmp^dmp1 mod p */
		if (bignum_exptmod(tmp, key->dmp1, key->p, a) < 0)
			goto error;

		/* b = tmp^dmq1 mod q */
		if (bignum_exptmod(tmp, key->dmq1, key->q, b) < 0)
			goto error;

		/* tmp = (a - b) * (1/q mod p) (mod p) */
		if (bignum_sub(a, b, tmp) < 0 ||
		    bignum_mulmod(tmp, key->iqmp, key->p, tmp) < 0)
			goto error;

		/* tmp = b + q * tmp */
		if (bignum_mul(tmp, key->q, tmp) < 0 ||
		    bignum_add(tmp, b, tmp) < 0)
			goto error;
	} else {
		/* Encrypt (or verify signature) */
		/* tmp = tmp^e mod N */
		if (bignum_exptmod(tmp, key->e, key->n, tmp) < 0)
			goto error;
	}

	modlen = crypto_rsa_get_modulus_len(key);
	if (modlen > *outlen) {
		*outlen = modlen;
		goto error;
	}

	if (bignum_get_unsigned_bin_len(tmp) > modlen)
		goto error; /* should never happen */

	*outlen = modlen;
	os_memset(out, 0, modlen);
	if (bignum_get_unsigned_bin(
		    tmp, out +
		    (modlen - bignum_get_unsigned_bin_len(tmp)), NULL) < 0)
		goto error;

	ret = 0;

error:
	bignum_deinit(tmp);
	bignum_deinit(a);
	bignum_deinit(b);
	return ret;
}


/**
 * crypto_rsa_get_modulus_len - Get the modulus length of the RSA key
 * @key: RSA key
 * Returns: Modulus length of the key
 */
size_t crypto_rsa_get_modulus_len(struct crypto_rsa_key *key)
{
	return bignum_get_unsigned_bin_len(key->n);
}

/**
 * crypto_rsa_free - Free RSA key
 * @key: RSA key to be freed
 *
 * This function frees an RSA key imported with either
 * crypto_rsa_import_public_key() or crypto_rsa_import_private_key().
 */
void crypto_rsa_free(struct crypto_rsa_key *key)
{
	if (key) {
		bignum_deinit(key->n);
		bignum_deinit(key->e);
		bignum_deinit(key->d);
		bignum_deinit(key->p);
		bignum_deinit(key->q);
		bignum_deinit(key->dmp1);
		bignum_deinit(key->dmq1);
		bignum_deinit(key->iqmp);
		os_free(key);
	}
}

END_NAMESPACE
