#include "crypto_openssl.h"
#include "crypto_common.h"
#include "crypto_alg.h"

#include <openssl/opensslv.h>
#include <openssl/err.h>
#include <openssl/des.h>
#include <openssl/aes.h>
#include <openssl/bn.h>
#include <openssl/evp.h>
#include <openssl/dh.h>


BEGIN_NAMESPACE(CRYPTO)

/*
How to compile openssl on Windows
(1) Open Visual Studio Developer Command Prompt
(2) Go to c:\openssl-1.0.1c
(2) perl Configure VC-WIN32 -prefix=c:/openssl-1.0.1c
(3) ms\do_ms
(4) For dynamic library:
    nmake -f ms\ntdll.mak
	For static library
	namke -f ms\nt.mak
(5) Test:
    nmake -f ms\ntdll.mak test
(6) Install
    nmake -f ms\ntdll.mak install
(7) Results are in C:\openssl-1.0.1c\out32dll
*/

//openssl implementation
struct crypto_cipher {
	EVP_CIPHER_CTX enc;
	EVP_CIPHER_CTX dec;
};

//windows library implementation
struct crypto_hash {
	enum crypto_hash_alg alg;
	int error;
	HCRYPTPROV prov;
	HCRYPTHASH hash;
	HCRYPTKEY key;
};

struct crypto_cipher * crypto_cipher_init(enum crypto_cipher_alg alg,
	const u8 *iv, const u8 *key,
	size_t key_len)
{
	struct crypto_cipher *ctx;
	const EVP_CIPHER *cipher;

	ctx = (struct crypto_cipher *) os_zalloc(sizeof(*ctx));
	if (ctx == NULL)
		return NULL;

	switch (alg) {
#ifndef OPENSSL_NO_RC4
	case CRYPTO_CIPHER_ALG_RC4:
		cipher = EVP_rc4();
		break;
#endif /* OPENSSL_NO_RC4 */
#ifndef OPENSSL_NO_AES
	case CRYPTO_CIPHER_ALG_AES:
		switch (key_len) {
		case 16:
			cipher = EVP_aes_128_cbc();
			break;
		case 24:
			cipher = EVP_aes_192_cbc();
			break;
		case 32:
			cipher = EVP_aes_256_cbc();
			break;
		default:
			os_free(ctx);
			return NULL;
		}
		break;
#endif /* OPENSSL_NO_AES */
#ifndef OPENSSL_NO_DES
	case CRYPTO_CIPHER_ALG_3DES:
		cipher = EVP_des_ede3_cbc();
		break;
	case CRYPTO_CIPHER_ALG_DES:
		cipher = EVP_des_cbc();
		break;
#endif /* OPENSSL_NO_DES */
#ifndef OPENSSL_NO_RC2
	case CRYPTO_CIPHER_ALG_RC2:
		cipher = EVP_rc2_ecb();
		break;
#endif /* OPENSSL_NO_RC2 */
	default:
		os_free(ctx);
		return NULL;
	}

	EVP_CIPHER_CTX_init(&ctx->enc);
	EVP_CIPHER_CTX_set_padding(&ctx->enc, 0);
	if (!EVP_EncryptInit_ex(&ctx->enc, cipher, NULL, NULL, NULL) ||
		!EVP_CIPHER_CTX_set_key_length(&ctx->enc, key_len) ||
		!EVP_EncryptInit_ex(&ctx->enc, NULL, NULL, key, iv)) {
			EVP_CIPHER_CTX_cleanup(&ctx->enc);
			os_free(ctx);
			return NULL;
	}

	EVP_CIPHER_CTX_init(&ctx->dec);
	EVP_CIPHER_CTX_set_padding(&ctx->dec, 0);
	if (!EVP_DecryptInit_ex(&ctx->dec, cipher, NULL, NULL, NULL) ||
		!EVP_CIPHER_CTX_set_key_length(&ctx->dec, key_len) ||
		!EVP_DecryptInit_ex(&ctx->dec, NULL, NULL, key, iv)) {
			EVP_CIPHER_CTX_cleanup(&ctx->enc);
			EVP_CIPHER_CTX_cleanup(&ctx->dec);
			os_free(ctx);
			return NULL;
	}

	//Need to set it here, o.w. the padding flag previously set will disappear...
	EVP_CIPHER_CTX_set_padding(&ctx->dec, 0);

	return ctx;
}

int crypto_cipher_decrypt(struct crypto_cipher *ctx, const u8 *crypt,
						  u8 *plain, size_t len)
{
	int outl;
	outl = len;
	if (!EVP_DecryptUpdate(&ctx->dec, plain, &outl, crypt, len))
		return -1;

	return 0;
}


void crypto_cipher_deinit(struct crypto_cipher *ctx)
{
	EVP_CIPHER_CTX_cleanup(&ctx->enc);
	EVP_CIPHER_CTX_cleanup(&ctx->dec);
	os_free(ctx);
}



/*
 * TODO:
 * RFC 2246 Section 9: Mandatory to implement TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA
 * Add support for commonly used cipher suites; don't bother with exportable
 * suites.
 */ 

static const struct tls_cipher_suite tls_cipher_suites[] = {
	{ TLS_NULL_WITH_NULL_NULL, TLS_KEY_X_NULL, TLS_CIPHER_NULL,
	  TLS_HASH_NULL },
	{ TLS_RSA_WITH_RC4_128_MD5, TLS_KEY_X_RSA, TLS_CIPHER_RC4_128,
	  TLS_HASH_MD5 },
	{ TLS_RSA_WITH_RC4_128_SHA, TLS_KEY_X_RSA, TLS_CIPHER_RC4_128,
	  TLS_HASH_SHA },
	{ TLS_RSA_WITH_DES_CBC_SHA, TLS_KEY_X_RSA, TLS_CIPHER_DES_CBC,
	  TLS_HASH_SHA },
	{ TLS_RSA_WITH_3DES_EDE_CBC_SHA, TLS_KEY_X_RSA,
	  TLS_CIPHER_3DES_EDE_CBC, TLS_HASH_SHA },
 	{ TLS_DH_anon_WITH_RC4_128_MD5, TLS_KEY_X_DH_anon,
	  TLS_CIPHER_RC4_128, TLS_HASH_MD5 },
 	{ TLS_DH_anon_WITH_DES_CBC_SHA, TLS_KEY_X_DH_anon,
	  TLS_CIPHER_DES_CBC, TLS_HASH_SHA },
 	{ TLS_DH_anon_WITH_3DES_EDE_CBC_SHA, TLS_KEY_X_DH_anon,
	  TLS_CIPHER_3DES_EDE_CBC, TLS_HASH_SHA },
	{ TLS_RSA_WITH_AES_128_CBC_SHA, TLS_KEY_X_RSA, TLS_CIPHER_AES_128_CBC,
	  TLS_HASH_SHA },
	{ TLS_DH_anon_WITH_AES_128_CBC_SHA, TLS_KEY_X_DH_anon,
	  TLS_CIPHER_AES_128_CBC, TLS_HASH_SHA },
	{ TLS_RSA_WITH_AES_256_CBC_SHA, TLS_KEY_X_RSA, TLS_CIPHER_AES_256_CBC,
	  TLS_HASH_SHA },
	{ TLS_DH_anon_WITH_AES_256_CBC_SHA, TLS_KEY_X_DH_anon,
	  TLS_CIPHER_AES_256_CBC, TLS_HASH_SHA }
};

#define NUM_ELEMS(a) (sizeof(a) / sizeof((a)[0]))
#define NUM_TLS_CIPHER_SUITES NUM_ELEMS(tls_cipher_suites)


static const struct tls_cipher_data tls_ciphers[] = {
	{ TLS_CIPHER_NULL,         TLS_CIPHER_STREAM,  0,  0,  0,
	  CRYPTO_CIPHER_NULL },
	{ TLS_CIPHER_IDEA_CBC,     TLS_CIPHER_BLOCK,  16, 16,  8,
	  CRYPTO_CIPHER_NULL },
	{ TLS_CIPHER_RC2_CBC_40,   TLS_CIPHER_BLOCK,   5, 16,  0,
	  CRYPTO_CIPHER_ALG_RC2 },
	{ TLS_CIPHER_RC4_40,       TLS_CIPHER_STREAM,  5, 16,  0,
	  CRYPTO_CIPHER_ALG_RC4 },
	{ TLS_CIPHER_RC4_128,      TLS_CIPHER_STREAM, 16, 16,  0,
	  CRYPTO_CIPHER_ALG_RC4 },
	{ TLS_CIPHER_DES40_CBC,    TLS_CIPHER_BLOCK,   5,  8,  8,
	  CRYPTO_CIPHER_ALG_DES },
	{ TLS_CIPHER_DES_CBC,      TLS_CIPHER_BLOCK,   8,  8,  8,
	  CRYPTO_CIPHER_ALG_DES },
	{ TLS_CIPHER_3DES_EDE_CBC, TLS_CIPHER_BLOCK,  24, 24,  8,
	  CRYPTO_CIPHER_ALG_3DES },
	{ TLS_CIPHER_AES_128_CBC,  TLS_CIPHER_BLOCK,  16, 16, 16,
	  CRYPTO_CIPHER_ALG_AES },
	{ TLS_CIPHER_AES_256_CBC,  TLS_CIPHER_BLOCK,  32, 32, 16,
	  CRYPTO_CIPHER_ALG_AES }
};

#define NUM_TLS_CIPHER_DATA NUM_ELEMS(tls_ciphers)


/**
 * tls_get_cipher_suite - Get TLS cipher suite
 * @suite: Cipher suite identifier
 * Returns: Pointer to the cipher data or %NULL if not found
 */
const struct tls_cipher_suite * tls_get_cipher_suite(u16 suite)
{
	size_t i;
	for (i = 0; i < NUM_TLS_CIPHER_SUITES; i++)
		if (tls_cipher_suites[i].suite == suite)
			return &tls_cipher_suites[i];
	return NULL;
}


const struct tls_cipher_data * tls_get_cipher_data(tls_cipher cipher)
{
	size_t i;
	for (i = 0; i < NUM_TLS_CIPHER_DATA; i++)
		if (tls_ciphers[i].cipher == cipher)
			return &tls_ciphers[i];
	return NULL;
}


struct crypto_hash * crypto_hash_init(enum crypto_hash_alg alg, const u8 *key,
				      size_t key_len)
{
	struct crypto_hash *ctx;
	ALG_ID calg;
	struct {
		BLOBHEADER hdr;
		DWORD len;
		BYTE key[32];
	} key_blob;

	os_memset(&key_blob, 0, sizeof(key_blob));
	switch (alg) {
	case CRYPTO_HASH_ALG_MD5:
		calg = CALG_MD5;
		break;
	case CRYPTO_HASH_ALG_SHA1:
		calg = CALG_SHA;
		break;
	case CRYPTO_HASH_ALG_HMAC_MD5:
	case CRYPTO_HASH_ALG_HMAC_SHA1:
		calg = CALG_HMAC;
		key_blob.hdr.bType = PLAINTEXTKEYBLOB;
		key_blob.hdr.bVersion = CUR_BLOB_VERSION;
		key_blob.hdr.reserved = 0;
		/*
		 * Note: RC2 is not really used, but that can be used to
		 * import HMAC keys of up to 16 byte long.
		 * CRYPT_IPSEC_HMAC_KEY flag for CryptImportKey() is needed to
		 * be able to import longer keys (HMAC-SHA1 uses 20-byte key).
		 */
		key_blob.hdr.aiKeyAlg = CALG_RC2;
		key_blob.len = key_len;
		if (key_len > sizeof(key_blob.key))
			return NULL;
		os_memcpy(key_blob.key, key, key_len);
		break;
	default:
		return NULL;
	}

	ctx = (crypto_hash *)os_zalloc(sizeof(*ctx));
	if (ctx == NULL)
		return NULL;

	ctx->alg = alg;

	if (!CryptAcquireContext(&ctx->prov, NULL, NULL, PROV_RSA_FULL, 0)) {
		cryptoapi_report_error("CryptAcquireContext");
		os_free(ctx);
		return NULL;
	}

	if (calg == CALG_HMAC) {
#ifndef CRYPT_IPSEC_HMAC_KEY
#define CRYPT_IPSEC_HMAC_KEY 0x00000100
#endif
		if (!CryptImportKey(ctx->prov, (BYTE *) &key_blob,
				    sizeof(key_blob), 0, CRYPT_IPSEC_HMAC_KEY,
				    &ctx->key)) {
			cryptoapi_report_error("CryptImportKey");
			CryptReleaseContext(ctx->prov, 0);
			os_free(ctx);
			return NULL;
		}
	}

	if (!CryptCreateHash(ctx->prov, calg, ctx->key, 0, &ctx->hash)) {
		cryptoapi_report_error("CryptCreateHash");
		CryptReleaseContext(ctx->prov, 0);
		os_free(ctx);
		return NULL;
	}

	if (calg == CALG_HMAC) {
		HMAC_INFO info;
		os_memset(&info, 0, sizeof(info));
		switch (alg) {
		case CRYPTO_HASH_ALG_HMAC_MD5:
			info.HashAlgid = CALG_MD5;
			break;
		case CRYPTO_HASH_ALG_HMAC_SHA1:
			info.HashAlgid = CALG_SHA;
			break;
		default:
			/* unreachable */
			break;
		}

		if (!CryptSetHashParam(ctx->hash, HP_HMAC_INFO, (BYTE *) &info,
				       0)) {
			cryptoapi_report_error("CryptSetHashParam");
			CryptDestroyHash(ctx->hash);
			CryptReleaseContext(ctx->prov, 0);
			os_free(ctx);
			return NULL;
		}
	}

	return ctx;
}

void crypto_hash_update(struct crypto_hash *ctx, const u8 *data, size_t len)
{
	if (ctx == NULL || ctx->error)
		return;

	if (!CryptHashData(ctx->hash, (BYTE *) data, len, 0)) {
		cryptoapi_report_error("CryptHashData");
		ctx->error = 1;
	}
}


int crypto_hash_finish(struct crypto_hash *ctx, u8 *mac, size_t *len)
{
	int ret = 0;
	DWORD hlen;

	if (ctx == NULL)
		return -2;

	if (mac == NULL || len == NULL)
		goto done;

	if (ctx->error) {
		ret = -2;
		goto done;
	}

	hlen = *len;
	if (!CryptGetHashParam(ctx->hash, HP_HASHVAL, mac, &hlen, 0)) {
		cryptoapi_report_error("CryptGetHashParam");
		ret = -2;
	}
	*len = hlen;

done:
	if (ctx->alg == CRYPTO_HASH_ALG_HMAC_SHA1 ||
		ctx->alg == CRYPTO_HASH_ALG_HMAC_MD5)
		CryptDestroyKey(ctx->key);

	os_free(ctx);

	return ret;
}



END_NAMESPACE