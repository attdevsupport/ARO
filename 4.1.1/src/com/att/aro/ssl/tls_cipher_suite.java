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

import com.att.aro.ssl.crypto_openssl.tls_cipher;
import com.att.aro.ssl.crypto_openssl.tls_hash;
import com.att.aro.ssl.crypto_openssl.tls_key_exchange;

public class tls_cipher_suite {
	int suite;
	tls_key_exchange key_exchange;
	tls_cipher cipher;
	tls_hash hash;
	
	tls_cipher_suite() {				
	}
	
	tls_cipher_suite(int suite, tls_key_exchange key_exchange, tls_cipher cipher, tls_hash hash){
		this.suite = suite;
		this.key_exchange = key_exchange;
		this.cipher = cipher;
		this.hash = hash;
	}
	
	public int getSuite() {
		return suite;
	}

	public void setSuite(int suite) {
		this.suite = suite;
	}

	public tls_key_exchange getKey_exchange() {
		return key_exchange;
	}

	public void setKey_exchange(tls_key_exchange key_exchange) {
		this.key_exchange = key_exchange;
	}

	public tls_cipher getCipher() {
		return cipher;
	}

	public void setCipher(tls_cipher cipher) {
		this.cipher = cipher;
	}

	public tls_hash getHash() {
		return hash;
	}

	public void setHash(tls_hash hash) {
		this.hash = hash;
	}	
}
