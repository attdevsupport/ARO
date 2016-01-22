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
package com.att.aro.core.securedpacketreader.impl;

import com.att.aro.core.securedpacketreader.ICipherDataService;
import com.att.aro.core.securedpacketreader.pojo.CryptoEnum.TLSCipher;
import com.att.aro.core.securedpacketreader.pojo.CryptoEnum.TLSCipherType;
import com.att.aro.core.securedpacketreader.pojo.CryptoEnum.TLSHash;
import com.att.aro.core.securedpacketreader.pojo.CryptoEnum.TLSKeyExchange;
import com.att.aro.core.securedpacketreader.pojo.CryptoEnum.CryptoCipherAlg;
import com.att.aro.core.securedpacketreader.pojo.TLSCipherData;
import com.att.aro.core.securedpacketreader.pojo.TLSCipherSuite;


public class CipherDataServiceImpl implements ICipherDataService {

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
	
	private TLSCipherSuite tlsCipherSuites[] = {
		new TLSCipherSuite(TLS_NULL_WITH_NULL_NULL, TLSKeyExchange.TLS_KEY_X_NULL, TLSCipher.TLS_CIPHER_NULL, TLSHash.TLS_HASH_NULL),
		new TLSCipherSuite(TLS_RSA_WITH_RC4_128_MD5, TLSKeyExchange.TLS_KEY_X_RSA, TLSCipher.TLS_CIPHER_RC4_128, TLSHash.TLS_HASH_MD5),
		new TLSCipherSuite(TLS_RSA_WITH_RC4_128_SHA, TLSKeyExchange.TLS_KEY_X_RSA, TLSCipher.TLS_CIPHER_RC4_128, TLSHash.TLS_HASH_SHA),
		new TLSCipherSuite(TLS_RSA_WITH_DES_CBC_SHA, TLSKeyExchange.TLS_KEY_X_RSA, TLSCipher.TLS_CIPHER_DES_CBC, TLSHash.TLS_HASH_SHA),
		new TLSCipherSuite(TLS_RSA_WITH_3DES_EDE_CBC_SHA, TLSKeyExchange.TLS_KEY_X_RSA, TLSCipher.TLS_CIPHER_3DES_EDE_CBC, TLSHash.TLS_HASH_SHA),
		new TLSCipherSuite(TLS_DH_ANON_WITH_RC4_128_MD5, TLSKeyExchange.TLS_KEY_X_DH_anon, TLSCipher.TLS_CIPHER_RC4_128, TLSHash.TLS_HASH_MD5),
		new TLSCipherSuite(TLS_DH_ANON_WITH_DES_CBC_SHA, TLSKeyExchange.TLS_KEY_X_DH_anon, TLSCipher.TLS_CIPHER_DES_CBC, TLSHash.TLS_HASH_SHA),
		new TLSCipherSuite(TLS_DH_ANON_WITH_3DES_EDE_CBC_SHA, TLSKeyExchange.TLS_KEY_X_DH_anon, TLSCipher.TLS_CIPHER_3DES_EDE_CBC, TLSHash.TLS_HASH_SHA),
		new TLSCipherSuite(TLS_RSA_WITH_AES_128_CBC_SHA, TLSKeyExchange.TLS_KEY_X_RSA, TLSCipher.TLS_CIPHER_AES_128_CBC, TLSHash.TLS_HASH_SHA),
		new TLSCipherSuite(TLS_DH_ANON_WITH_AES_128_CBC_SHA, TLSKeyExchange.TLS_KEY_X_DH_anon, TLSCipher.TLS_CIPHER_AES_128_CBC, TLSHash.TLS_HASH_SHA),
		new TLSCipherSuite(TLS_RSA_WITH_AES_256_CBC_SHA, TLSKeyExchange.TLS_KEY_X_RSA, TLSCipher.TLS_CIPHER_AES_256_CBC, TLSHash.TLS_HASH_SHA),
		new TLSCipherSuite(TLS_DH_ANON_WITH_AES_256_CBC_SHA, TLSKeyExchange.TLS_KEY_X_DH_anon, TLSCipher.TLS_CIPHER_AES_256_CBC, TLSHash.TLS_HASH_SHA)
	};
	
	private TLSCipherData tlsCiphers[] = {
			new TLSCipherData(TLSCipher.TLS_CIPHER_NULL, TLSCipherType.TLS_CIPHER_STREAM, 0, 0, 0, CryptoCipherAlg.CRYPTO_CIPHER_NULL),
			new TLSCipherData(TLSCipher.TLS_CIPHER_IDEA_CBC, TLSCipherType.TLS_CIPHER_BLOCK, 16, 16, 8, CryptoCipherAlg.CRYPTO_CIPHER_NULL),
			new TLSCipherData(TLSCipher.TLS_CIPHER_RC2_CBC_40, TLSCipherType.TLS_CIPHER_BLOCK, 5, 16, 0, CryptoCipherAlg.CRYPTO_CIPHER_ALG_RC2),
			new TLSCipherData(TLSCipher.TLS_CIPHER_RC4_40, TLSCipherType.TLS_CIPHER_STREAM, 5, 16, 0, CryptoCipherAlg.CRYPTO_CIPHER_ALG_RC4),
			new TLSCipherData(TLSCipher.TLS_CIPHER_RC4_128, TLSCipherType.TLS_CIPHER_STREAM, 16, 16, 0, CryptoCipherAlg.CRYPTO_CIPHER_ALG_RC4),
			new TLSCipherData(TLSCipher.TLS_CIPHER_DES40_CBC, TLSCipherType.TLS_CIPHER_BLOCK, 5, 8, 8, CryptoCipherAlg.CRYPTO_CIPHER_ALG_DES),
			new TLSCipherData(TLSCipher.TLS_CIPHER_DES_CBC, TLSCipherType.TLS_CIPHER_BLOCK, 8, 8, 8, CryptoCipherAlg.CRYPTO_CIPHER_ALG_DES),
			new TLSCipherData(TLSCipher.TLS_CIPHER_3DES_EDE_CBC, TLSCipherType.TLS_CIPHER_BLOCK, 24, 24, 8, CryptoCipherAlg.CRYPTO_CIPHER_ALG_3DES),
			new TLSCipherData(TLSCipher.TLS_CIPHER_AES_128_CBC, TLSCipherType.TLS_CIPHER_BLOCK, 16, 16, 16, CryptoCipherAlg.CRYPTO_CIPHER_ALG_AES),
			new TLSCipherData(TLSCipher.TLS_CIPHER_AES_256_CBC, TLSCipherType.TLS_CIPHER_BLOCK, 32, 32, 16, CryptoCipherAlg.CRYPTO_CIPHER_ALG_AES)
		};
	public CipherDataServiceImpl(){
		
	}
	@Override
	public TLSCipherData getTLSCipherData(TLSCipher cipher) {
		TLSCipherData data = null;
		for(int i = 0;i<tlsCiphers.length;i++){
			if(tlsCiphers[i].getCipher() == cipher){
				data = tlsCiphers[i];
				break;
			}
		}
		return data;
	}

	@Override
	public TLSCipherSuite getTLSCipherSuite(int suite) {
		TLSCipherSuite ciphersuite = null;
		for(int i = 0;i<this.tlsCipherSuites.length;i++){
			if(tlsCipherSuites[i].getSuite() == suite){
				ciphersuite = tlsCipherSuites[i];
				break;
			}
		}
		return ciphersuite;
	}

}
