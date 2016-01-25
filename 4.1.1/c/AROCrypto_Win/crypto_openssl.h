#pragma once

#include "crypto_common.h"

BEGIN_NAMESPACE(CRYPTO)


enum crypto_cipher_alg {
	CRYPTO_CIPHER_NULL = 0, CRYPTO_CIPHER_ALG_AES, CRYPTO_CIPHER_ALG_3DES,
	CRYPTO_CIPHER_ALG_DES, CRYPTO_CIPHER_ALG_RC2, CRYPTO_CIPHER_ALG_RC4
};

/* CipherSuite */
#define TLS_NULL_WITH_NULL_NULL			0x0000 /* RFC 2246 */
#define TLS_RSA_WITH_NULL_MD5			0x0001 /* RFC 2246 */
#define TLS_RSA_WITH_NULL_SHA			0x0002 /* RFC 2246 */
#define TLS_RSA_EXPORT_WITH_RC4_40_MD5		0x0003 /* RFC 2246 */
#define TLS_RSA_WITH_RC4_128_MD5		0x0004 /* RFC 2246 */
#define TLS_RSA_WITH_RC4_128_SHA		0x0005 /* RFC 2246 */
#define TLS_RSA_EXPORT_WITH_RC2_CBC_40_MD5	0x0006 /* RFC 2246 */
#define TLS_RSA_WITH_IDEA_CBC_SHA		0x0007 /* RFC 2246 */
#define TLS_RSA_EXPORT_WITH_DES40_CBC_SHA	0x0008 /* RFC 2246 */
#define TLS_RSA_WITH_DES_CBC_SHA		0x0009 /* RFC 2246 */
#define TLS_RSA_WITH_3DES_EDE_CBC_SHA		0x000A /* RFC 2246 */
#define TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA	0x000B /* RFC 2246 */
#define TLS_DH_DSS_WITH_DES_CBC_SHA		0x000C /* RFC 2246 */
#define TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA	0x000D /* RFC 2246 */
#define TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA	0x000E /* RFC 2246 */
#define TLS_DH_RSA_WITH_DES_CBC_SHA		0x000F /* RFC 2246 */
#define TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA	0x0010 /* RFC 2246 */
#define TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA	0x0011 /* RFC 2246 */
#define TLS_DHE_DSS_WITH_DES_CBC_SHA		0x0012 /* RFC 2246 */
#define TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA	0x0013 /* RFC 2246 */
#define TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA	0x0014 /* RFC 2246 */
#define TLS_DHE_RSA_WITH_DES_CBC_SHA		0x0015 /* RFC 2246 */
#define TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA	0x0016 /* RFC 2246 */
#define TLS_DH_anon_EXPORT_WITH_RC4_40_MD5	0x0017 /* RFC 2246 */
#define TLS_DH_anon_WITH_RC4_128_MD5		0x0018 /* RFC 2246 */
#define TLS_DH_anon_EXPORT_WITH_DES40_CBC_SHA	0x0019 /* RFC 2246 */
#define TLS_DH_anon_WITH_DES_CBC_SHA		0x001A /* RFC 2246 */
#define TLS_DH_anon_WITH_3DES_EDE_CBC_SHA	0x001B /* RFC 2246 */
#define TLS_RSA_WITH_AES_128_CBC_SHA		0x002F /* RFC 3268 */
#define TLS_DH_DSS_WITH_AES_128_CBC_SHA		0x0030 /* RFC 3268 */
#define TLS_DH_RSA_WITH_AES_128_CBC_SHA		0x0031 /* RFC 3268 */
#define TLS_DHE_DSS_WITH_AES_128_CBC_SHA	0x0032 /* RFC 3268 */
#define TLS_DHE_RSA_WITH_AES_128_CBC_SHA	0x0033 /* RFC 3268 */
#define TLS_DH_anon_WITH_AES_128_CBC_SHA	0x0034 /* RFC 3268 */
#define TLS_RSA_WITH_AES_256_CBC_SHA		0x0035 /* RFC 3268 */
#define TLS_DH_DSS_WITH_AES_256_CBC_SHA		0x0036 /* RFC 3268 */
#define TLS_DH_RSA_WITH_AES_256_CBC_SHA		0x0037 /* RFC 3268 */
#define TLS_DHE_DSS_WITH_AES_256_CBC_SHA	0x0038 /* RFC 3268 */
#define TLS_DHE_RSA_WITH_AES_256_CBC_SHA	0x0039 /* RFC 3268 */
#define TLS_DH_anon_WITH_AES_256_CBC_SHA	0x003A /* RFC 3268 */

#define TLS_SEQ_NUM_LEN 8


typedef enum {
	TLS_KEY_X_NULL,
	TLS_KEY_X_RSA,
	TLS_KEY_X_RSA_EXPORT,
	TLS_KEY_X_DH_DSS_EXPORT,
	TLS_KEY_X_DH_DSS,
	TLS_KEY_X_DH_RSA_EXPORT,
	TLS_KEY_X_DH_RSA,
	TLS_KEY_X_DHE_DSS_EXPORT,
	TLS_KEY_X_DHE_DSS,
	TLS_KEY_X_DHE_RSA_EXPORT,
	TLS_KEY_X_DHE_RSA,
	TLS_KEY_X_DH_anon_EXPORT,
	TLS_KEY_X_DH_anon
} tls_key_exchange;

typedef enum {
	TLS_CIPHER_NULL,
	TLS_CIPHER_RC4_40,
	TLS_CIPHER_RC4_128,
	TLS_CIPHER_RC2_CBC_40,
	TLS_CIPHER_IDEA_CBC,
	TLS_CIPHER_DES40_CBC,
	TLS_CIPHER_DES_CBC,
	TLS_CIPHER_3DES_EDE_CBC,
	TLS_CIPHER_AES_128_CBC,
	TLS_CIPHER_AES_256_CBC
} tls_cipher;

typedef enum {
	TLS_HASH_NULL,
	TLS_HASH_MD5,
	TLS_HASH_SHA
} tls_hash;

enum crypto_hash_alg {
	CRYPTO_HASH_ALG_MD5, CRYPTO_HASH_ALG_SHA1,
	CRYPTO_HASH_ALG_HMAC_MD5, CRYPTO_HASH_ALG_HMAC_SHA1
};

struct tls_cipher_suite {
	u16 suite;
	tls_key_exchange key_exchange;
	tls_cipher cipher;
	tls_hash hash;
};

typedef enum {
	TLS_CIPHER_STREAM,
	TLS_CIPHER_BLOCK
} tls_cipher_type;

struct tls_cipher_data {
	tls_cipher cipher;
	tls_cipher_type type;
	size_t key_material;
	size_t expanded_key_material;
	size_t block_size; /* also iv_size */
	enum crypto_cipher_alg alg;
};


struct crypto_cipher * crypto_cipher_init(enum crypto_cipher_alg alg,
	const u8 *iv, const u8 *key,
	size_t key_len);

int crypto_cipher_decrypt(struct crypto_cipher *ctx, const u8 *crypt,
						  u8 *plain, size_t len);

void crypto_cipher_deinit(struct crypto_cipher *ctx);


const struct tls_cipher_data * tls_get_cipher_data(tls_cipher cipher);
const struct tls_cipher_suite * tls_get_cipher_suite(u16 suite);

struct crypto_hash * crypto_hash_init(enum crypto_hash_alg alg, const u8 *key,
	size_t key_len);
void crypto_hash_update(struct crypto_hash *ctx, const u8 *data, size_t len);
int crypto_hash_finish(struct crypto_hash *ctx, u8 *mac, size_t *len);

END_NAMESPACE