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

import com.att.aro.core.securedpacketreader.pojo.CryptoEnum.TLSCipher;
import com.att.aro.core.securedpacketreader.pojo.CryptoEnum.TLSHash;
import com.att.aro.core.securedpacketreader.pojo.CryptoEnum.TLSKeyExchange;

public class TLSCipherSuite {
	int suite;
	TLSKeyExchange keyexchange;
	TLSCipher cipher;
	TLSHash hash;
	public TLSCipherSuite(int suite, TLSKeyExchange keyexchange, TLSCipher cipher, TLSHash hash){
		this.suite = suite;
		this.keyexchange = keyexchange;
		this.cipher = cipher;
		this.hash = hash;
	}
	public int getSuite() {
		return suite;
	}
	public void setSuite(int suite) {
		this.suite = suite;
	}
	public TLSKeyExchange getKeyexchange() {
		return keyexchange;
	}
	public void setKeyexchange(TLSKeyExchange keyexchange) {
		this.keyexchange = keyexchange;
	}
	public TLSCipher getCipher() {
		return cipher;
	}
	public void setCipher(TLSCipher cipher) {
		this.cipher = cipher;
	}
	public TLSHash getHash() {
		return hash;
	}
	public void setHash(TLSHash hash) {
		this.hash = hash;
	}
	
}
