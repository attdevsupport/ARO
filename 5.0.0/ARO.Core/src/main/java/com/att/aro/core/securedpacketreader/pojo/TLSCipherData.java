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

import com.att.aro.core.securedpacketreader.pojo.CryptoEnum.CryptoCipherAlg;
import com.att.aro.core.securedpacketreader.pojo.CryptoEnum.TLSCipher;
import com.att.aro.core.securedpacketreader.pojo.CryptoEnum.TLSCipherType;


public class TLSCipherData {
	TLSCipher cipher;
	TLSCipherType type;
	CryptoCipherAlg alg;
	int keymaterial;
	int expandedKeyMaterial;
	int blocksize;
	public TLSCipherData(TLSCipher cipher, TLSCipherType type, int keymaterial, int expandedkeymaterial, int blocksize, CryptoCipherAlg alg){
		this.cipher = cipher;
		this.type = type;
		this.keymaterial = keymaterial;
		this.expandedKeyMaterial = expandedkeymaterial;
		this.blocksize = blocksize;
		this.alg = alg;
	}
	public TLSCipher getCipher() {
		return cipher;
	}
	public void setCipher(TLSCipher cipher) {
		this.cipher = cipher;
	}
	public TLSCipherType getType() {
		return type;
	}
	public void setType(TLSCipherType type) {
		this.type = type;
	}
	public CryptoCipherAlg getAlg() {
		return alg;
	}
	public void setAlg(CryptoCipherAlg alg) {
		this.alg = alg;
	}
	public int getKeyMaterial() {
		return keymaterial;
	}
	public void setKeyMaterial(int keymaterial) {
		this.keymaterial = keymaterial;
	}
	public int getExpandedKeyMaterial() {
		return expandedKeyMaterial;
	}
	public void setExpandedKeyMaterial(int expandedkeymaterial) {
		this.expandedKeyMaterial = expandedkeymaterial;
	}
	public int getBlockSize() {
		return blocksize;
	}
	public void setBlockSize(int blocksize) {
		this.blocksize = blocksize;
	}
	
}
