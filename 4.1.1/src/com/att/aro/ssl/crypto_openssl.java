/*
 *  Copyright 2012 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.att.aro.ssl;

public class crypto_openssl {
	static final int SHA1_MAC_LEN = 20;
	static final int MD5_MAC_LEN = 16;
	private static final int TLS_NULL_WITH_NULL_NULL		=	0x0000; /* RFC 2246 */
	private static final int  TLS_RSA_WITH_RC4_128_MD5		=0x0004; /* RFC 2246 */
	private static final int  TLS_RSA_WITH_RC4_128_SHA		=0x0005; /* RFC 2246 */
	private static final int  TLS_RSA_WITH_DES_CBC_SHA		=0x0009; /* RFC 2246 */
	private static final int  TLS_RSA_WITH_3DES_EDE_CBC_SHA		=0x000A; /* RFC 2246 */
	private static final int  TLS_DH_ANON_WITH_RC4_128_MD5		=0x0018; /* RFC 2246 */
	private static final int  TLS_DH_ANON_WITH_DES_CBC_SHA		=0x001A; /* RFC 2246 */
	private static final int  TLS_DH_ANON_WITH_3DES_EDE_CBC_SHA	=0x001B; /* RFC 2246 */
	private static final int  TLS_RSA_WITH_AES_128_CBC_SHA		=0x002F; /* RFC 3268 */
	private static final int  TLS_DH_ANON_WITH_AES_128_CBC_SHA	=0x0034; /* RFC 3268 */
	private static final int  TLS_RSA_WITH_AES_256_CBC_SHA		=0x0035; /* RFC 3268 */
	private static final int  TLS_DH_ANON_WITH_AES_256_CBC_SHA	=0x003A; /* RFC 3268 */
	
	enum crypto_hash_alg {
		CRYPTO_HASH_ALG_MD5, 
		CRYPTO_HASH_ALG_SHA1,
		CRYPTO_HASH_ALG_HMAC_MD5, 
		CRYPTO_HASH_ALG_HMAC_SHA1
	};
	public enum crypto_cipher_alg {
		CRYPTO_CIPHER_NULL, 
		CRYPTO_CIPHER_ALG_AES, 
		CRYPTO_CIPHER_ALG_3DES,
		CRYPTO_CIPHER_ALG_DES, 
		CRYPTO_CIPHER_ALG_RC2, 
		CRYPTO_CIPHER_ALG_RC4
	};
	
	public enum tls_key_exchange {
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
	};
	
	enum tls_cipher {
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
	};
	
	enum tls_hash {
		TLS_HASH_NULL,
		TLS_HASH_MD5,
		TLS_HASH_SHA
	};
	
	enum tls_cipher_type {
		TLS_CIPHER_STREAM,
		TLS_CIPHER_BLOCK
	};
	
	public static tls_cipher_suite tlsCipherSuites[] = {
		new tls_cipher_suite(TLS_NULL_WITH_NULL_NULL, tls_key_exchange.TLS_KEY_X_NULL, tls_cipher.TLS_CIPHER_NULL, tls_hash.TLS_HASH_NULL),
		new tls_cipher_suite(TLS_RSA_WITH_RC4_128_MD5, tls_key_exchange.TLS_KEY_X_RSA, tls_cipher.TLS_CIPHER_RC4_128, tls_hash.TLS_HASH_MD5),
		new tls_cipher_suite(TLS_RSA_WITH_RC4_128_SHA, tls_key_exchange.TLS_KEY_X_RSA, tls_cipher.TLS_CIPHER_RC4_128, tls_hash.TLS_HASH_SHA),
		new tls_cipher_suite(TLS_RSA_WITH_DES_CBC_SHA, tls_key_exchange.TLS_KEY_X_RSA, tls_cipher.TLS_CIPHER_DES_CBC, tls_hash.TLS_HASH_SHA),
		new tls_cipher_suite(TLS_RSA_WITH_3DES_EDE_CBC_SHA, tls_key_exchange.TLS_KEY_X_RSA, tls_cipher.TLS_CIPHER_3DES_EDE_CBC, tls_hash.TLS_HASH_SHA),
		new tls_cipher_suite(TLS_DH_ANON_WITH_RC4_128_MD5, tls_key_exchange.TLS_KEY_X_DH_anon, tls_cipher.TLS_CIPHER_RC4_128, tls_hash.TLS_HASH_MD5),
		new tls_cipher_suite(TLS_DH_ANON_WITH_DES_CBC_SHA, tls_key_exchange.TLS_KEY_X_DH_anon, tls_cipher.TLS_CIPHER_DES_CBC, tls_hash.TLS_HASH_SHA),
		new tls_cipher_suite(TLS_DH_ANON_WITH_3DES_EDE_CBC_SHA, tls_key_exchange.TLS_KEY_X_DH_anon, tls_cipher.TLS_CIPHER_3DES_EDE_CBC, tls_hash.TLS_HASH_SHA),
		new tls_cipher_suite(TLS_RSA_WITH_AES_128_CBC_SHA, tls_key_exchange.TLS_KEY_X_RSA, tls_cipher.TLS_CIPHER_AES_128_CBC, tls_hash.TLS_HASH_SHA),
		new tls_cipher_suite(TLS_DH_ANON_WITH_AES_128_CBC_SHA, tls_key_exchange.TLS_KEY_X_DH_anon, tls_cipher.TLS_CIPHER_AES_128_CBC, tls_hash.TLS_HASH_SHA),
		new tls_cipher_suite(TLS_RSA_WITH_AES_256_CBC_SHA, tls_key_exchange.TLS_KEY_X_RSA, tls_cipher.TLS_CIPHER_AES_256_CBC, tls_hash.TLS_HASH_SHA),
		new tls_cipher_suite(TLS_DH_ANON_WITH_AES_256_CBC_SHA, tls_key_exchange.TLS_KEY_X_DH_anon, tls_cipher.TLS_CIPHER_AES_256_CBC, tls_hash.TLS_HASH_SHA)
	};
	
	static tls_cipher_data tlsCiphers[] = {
			new tls_cipher_data(tls_cipher.TLS_CIPHER_NULL, tls_cipher_type.TLS_CIPHER_STREAM, 0, 0, 0, crypto_cipher_alg.CRYPTO_CIPHER_NULL),
			new tls_cipher_data(tls_cipher.TLS_CIPHER_IDEA_CBC, tls_cipher_type.TLS_CIPHER_BLOCK, 16, 16, 8, crypto_cipher_alg.CRYPTO_CIPHER_NULL),
			new tls_cipher_data(tls_cipher.TLS_CIPHER_RC2_CBC_40, tls_cipher_type.TLS_CIPHER_BLOCK, 5, 16, 0, crypto_cipher_alg.CRYPTO_CIPHER_ALG_RC2),
			new tls_cipher_data(tls_cipher.TLS_CIPHER_RC4_40, tls_cipher_type.TLS_CIPHER_STREAM, 5, 16, 0, crypto_cipher_alg.CRYPTO_CIPHER_ALG_RC4),
			new tls_cipher_data(tls_cipher.TLS_CIPHER_RC4_128, tls_cipher_type.TLS_CIPHER_STREAM, 16, 16, 0, crypto_cipher_alg.CRYPTO_CIPHER_ALG_RC4),
			new tls_cipher_data(tls_cipher.TLS_CIPHER_DES40_CBC, tls_cipher_type.TLS_CIPHER_BLOCK, 5, 8, 8, crypto_cipher_alg.CRYPTO_CIPHER_ALG_DES),
			new tls_cipher_data(tls_cipher.TLS_CIPHER_DES_CBC, tls_cipher_type.TLS_CIPHER_BLOCK, 8, 8, 8, crypto_cipher_alg.CRYPTO_CIPHER_ALG_DES),
			new tls_cipher_data(tls_cipher.TLS_CIPHER_3DES_EDE_CBC, tls_cipher_type.TLS_CIPHER_BLOCK, 24, 24, 8, crypto_cipher_alg.CRYPTO_CIPHER_ALG_3DES),
			new tls_cipher_data(tls_cipher.TLS_CIPHER_AES_128_CBC, tls_cipher_type.TLS_CIPHER_BLOCK, 16, 16, 16, crypto_cipher_alg.CRYPTO_CIPHER_ALG_AES),
			new tls_cipher_data(tls_cipher.TLS_CIPHER_AES_256_CBC, tls_cipher_type.TLS_CIPHER_BLOCK, 32, 32, 16, crypto_cipher_alg.CRYPTO_CIPHER_ALG_AES)
		};
	
	private static int numElemsSuites() {
		int val = 12; //(sizeof(tls_cipher_suites) / sizeof((tls_cipher_suites)[0]);
		return val;
	}
	
	public static tls_cipher_suite tlsGetCipherSuite(int suite)
	{
		int i = 0;
		int numTlsCipherSuites = numElemsSuites();
		for (i = 0; i < numTlsCipherSuites; i++) {
			if (tlsCipherSuites[i].suite == suite) {
				return tlsCipherSuites[i];
			}
		}
		return null;
	}
	
	private static int numElemsData() {
		int val = 10; //(sizeof(tls_ciphers) / sizeof((tls_ciphers)[0]);
		return val;
	}
	
	public static tls_cipher_data tlsGetCipherData(tls_cipher cipher)
	{
		int numTlsCipherData = numElemsData();
		int i;
		for (i = 0; i < numTlsCipherData; i++) {
			if (tlsCiphers[i].cipher == cipher) {
				return tlsCiphers[i];
			}
		}
		return null;
	}
}
