/**
 * Copyright 2016 AT&T
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
package com.att.aro.core.securedpacketreader.pojo;

public class CryptoEnum {
	public enum CryptoHashAlg {
		CRYPTO_HASH_ALG_MD5, 
		CRYPTO_HASH_ALG_SHA1,
		CRYPTO_HASH_ALG_HMAC_MD5, 
		CRYPTO_HASH_ALG_HMAC_SHA1
	};
	public enum CryptoCipherAlg {
		CRYPTO_CIPHER_NULL, 
		CRYPTO_CIPHER_ALG_AES, 
		CRYPTO_CIPHER_ALG_3DES,
		CRYPTO_CIPHER_ALG_DES, 
		CRYPTO_CIPHER_ALG_RC2, 
		CRYPTO_CIPHER_ALG_RC4
	};
	
	public enum TLSKeyExchange {
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
	
	public enum TLSCipher {
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
	
	public enum TLSHash {
		TLS_HASH_NULL,
		TLS_HASH_MD5,
		TLS_HASH_SHA
	};
	
	public enum TLSCipherType {
		TLS_CIPHER_STREAM,
		TLS_CIPHER_BLOCK
	};
}
