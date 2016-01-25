#include "objbase.h"
#include "crypto_common.h"
#pragma once

BEGIN_NAMESPACE(CRYPTO)

#define SHA1_MAC_LEN 20
#define MD5_MAC_LEN 16



#define md5_vector_non_fips_allow md5_vector
#define hmac_md5_non_fips_allow hmac_md5

void cryptoapi_report_error(const char * msg);
int cryptoapi_hash_vector(ALG_ID alg, size_t hash_len, size_t num_elem,
						  const u8 *addr[], const size_t *len, u8 *mac);
int hmac_md5_vector(const u8 *key, size_t key_len, size_t num_elem,
					const u8 *addr[], const size_t *len, u8 *mac);
int hmac_md5(const u8 *key, size_t key_len, const u8 *data, size_t data_len,
			 u8 *mac);
int md5_vector(size_t num_elem, const u8 *addr[], const size_t *len, u8 *mac);
int hmac_md5_vector_non_fips_allow(const u8 *key, size_t key_len,
								   size_t num_elem, const u8 *addr[],
								   const size_t *len, u8 *mac);
int hmac_sha1_vector(const u8 *key, size_t key_len, size_t num_elem,
					 const u8 *addr[], const size_t *len, u8 *mac);
int tls_prf(const u8 *secret, size_t secret_len, const char *label,
			const u8 *seed, size_t seed_len, u8 *out, size_t outlen);
int sha1_vector(size_t num_elem, const u8 *addr[], const size_t *len, u8 *mac);
int hmac_sha1(const u8 *key, size_t key_len, const u8 *data, size_t data_len,
			  u8 *mac);

void crypto_rsa_free(struct crypto_rsa_key *key);




int crypto_rsa_exptmod(const u8 *in, size_t inlen, u8 *out, size_t *outlen,
struct crypto_rsa_key *key, int use_private);

size_t crypto_rsa_get_modulus_len(struct crypto_rsa_key *key);

END_NAMESPACE
