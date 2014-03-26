#include <stdio.h>
#include "crypto_openssl.h"
#include "crypto_common.h"
#include "crypto_alg.h"
#include <string.h>

#include "opensslwin/opensslv.h"
#include "opensslwin/err.h"
#include "opensslwin/des.h"
#include "opensslwin/aes.h"
#include "opensslwin/bn.h"
#include "opensslwin/evp.h"
#include "opensslwin/dh.h"

#define os_malloc malloc
#define os_memset memset
#define ALG_CLASS_HASH (4 << 13)
#define ALG_TYPE_ANY (0)
#define ALG_SID_MD5 3
#define CALG_MD5 (ALG_CLASS_HASH | ALG_TYPE_ANY | ALG_SID_MD5)
#define ALG_SID_SHA  4
#define CALG_SHA                (ALG_CLASS_HASH | ALG_TYPE_ANY | ALG_SID_SHA)
#define ALG_SID_MAC  5
#define CALG_MAC                (ALG_CLASS_HASH | ALG_TYPE_ANY | ALG_SID_MAC)
#define ALG_SID_HMAC   9
#define CALG_HMAC               (ALG_CLASS_HASH | ALG_TYPE_ANY | ALG_SID_HMAC)
#define CALG_HMAC               (ALG_CLASS_HASH | ALG_TYPE_ANY | ALG_SID_HMAC)
#define CUR_BLOB_VERSION        2
#define PLAINTEXTKEYBLOB        0x8
#define ALG_SID_RC2                     2
#define CALG_RC2                (ALG_CLASS_DATA_ENCRYPT|ALG_TYPE_BLOCK|ALG_SID_RC2)
#define ALG_CLASS_DATA_ENCRYPT          (3 << 13)
#define ALG_TYPE_BLOCK                  (3 << 9)
#define PROV_RSA_FULL           1
#define HP_HMAC_INFO            0x0005
#define HP_HASHVAL              0x0002

typedef unsigned char       BYTE;
typedef unsigned short      WORD;
typedef unsigned int ALG_ID;
typedef unsigned long DWORD;

typedef struct _HMAC_Info {
    ALG_ID  HashAlgid;
    BYTE    *pbInnerString;
    DWORD   cbInnerString;
    BYTE    *pbOuterString;
    DWORD   cbOuterString;
} HMAC_INFO;

typedef struct _PUBLICKEYSTRUC {
        BYTE    bType;
        BYTE    bVersion;
        WORD    reserved;
        ALG_ID  aiKeyAlg;
} BLOBHEADER;

struct crypto_cipher {
	EVP_CIPHER_CTX enc;
	EVP_CIPHER_CTX dec;
};

void * os_zalloc(size_t size)
{
	void *ptr = os_malloc(size);
	if (ptr)
		os_memset(ptr, 0, size);
	return ptr;
}

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

int crypto_hash_finish1(int abc)
{
	return abc*2;
}