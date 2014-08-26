#pragma once

#include "crypto_common.h"
#include "crypto_bignum.h"

BEGIN_NAMESPACE(CRYPTO)

#define ASN1_CLASS_UNIVERSAL		0
#define ASN1_CLASS_APPLICATION		1
#define ASN1_CLASS_CONTEXT_SPECIFIC	2
#define ASN1_CLASS_PRIVATE		3

#define ASN1_TAG_SEQUENCE	0x10 /* shall be constructed */
#define ASN1_TAG_INTEGER	0x02
#define ASN1_TAG_OCTETSTRING	0x04
#define ASN1_TAG_OID		0x06

struct asn1_hdr {
	const u8 *payload;
	u8 identifier, _class, constructed;
	unsigned int tag, length;
};

#define ASN1_MAX_OID_LEN 20

struct asn1_oid {
	unsigned long oid[ASN1_MAX_OID_LEN];
	size_t len;
};

const u8 * search_tag(const char *tag, const u8 *buf, size_t len);
struct crypto_private_key * tlsv1_set_key_pem(const u8 *key, size_t len);
struct crypto_private_key * crypto_private_key_import(const u8 *key,
	size_t len,
	const char *passwd);
struct crypto_private_key * pkcs8_key_import(const u8 *buf, size_t len);
struct crypto_rsa_key *
	crypto_rsa_import_private_key(const u8 *buf, size_t len);
unsigned char * base64_decode(const unsigned char *src, size_t len,
							  size_t *out_len);
int asn1_parse_oid(const u8 *buf, size_t len, struct asn1_oid *oid);


int crypto_private_key_decrypt_pkcs1_v15(struct crypto_private_key *key,
										 const u8 *in, size_t inlen,
										 u8 *out, size_t *outlen);

int pkcs1_v15_private_key_decrypt(struct crypto_rsa_key *key,
								  const u8 *in, size_t inlen,
								  u8 *out, size_t *outlen);

void asn1_oid_to_str(struct asn1_oid *oid, char *buf, size_t len);
int asn1_get_oid(const u8 *buf, size_t len, struct asn1_oid *oid,
				 const u8 **next);
const u8 * crypto_rsa_parse_integer(const u8 *pos, const u8 *end,
struct bignum *num);

END_NAMESPACE
