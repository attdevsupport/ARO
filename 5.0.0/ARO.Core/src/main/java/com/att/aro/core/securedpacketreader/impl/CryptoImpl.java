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

import java.util.ArrayList;
import java.util.List;

import com.att.aro.core.securedpacketreader.ICrypto;
import com.att.aro.core.securedpacketreader.IReceiveSSLKey;
import com.att.aro.core.securedpacketreader.pojo.SSLKey;

/**
 * call native lib to read SSL key, and provide functions to decrypt data
 */
public class CryptoImpl implements ICrypto, IReceiveSSLKey {

	List<SSLKey> sslkeys;
	CryptoAdapter adapter;
	
	public CryptoImpl(){
		sslkeys = new ArrayList<SSLKey>();
	}
	public void setAdapter(CryptoAdapter adapter){
		this.adapter = adapter;
	}
	void ensureAdapter(){
		if(adapter == null){
			adapter = new CryptoAdapter();
			adapter.setSubscriber(this);
			adapter.loadAroCryptoLib();
		}
	}
	@Override
	public int readSSLKeys(String filename) {
		ensureAdapter();
		return adapter.ReadSSLKeys(filename);
	}

	@Override
	public int cryptoCipherInit(int alg, byte[] temp1, byte[] temp2,
			int keymaterial, int bClient) {
		ensureAdapter();
		return adapter.cryptocipherinit(alg, temp1, temp2, keymaterial, bClient);
	}

	@Override
	public void cryptoCipherDeinit(int objectType) {
		ensureAdapter();
		adapter.cryptocipherdeinit(objectType);
	}

	@Override
	public void setCryptoCipherNull(int objectType, int bClient) {
		ensureAdapter();
		adapter.setcryptociphernull(objectType, bClient);
	}

	@Override
	public int cryptoCipherDecrypt(int pCipher, byte[] enc, byte[] plain,
			int enclength, int bClient) {
		ensureAdapter();
		return adapter.cryptocipherdecrypt(pCipher, enc, plain, enclength, bClient);
	}

	@Override
	public void copyCryptoCipher(int fromObjectType, int toObjectType) {
		ensureAdapter();
		adapter.copycryptocipher(fromObjectType, toObjectType);
	}

	@Override
	public int cryptoHashInitUpdateFinish(int dir, int hashAlg,
			byte[] keyBlock, int hashSize, int recType, int payloadLen,
			byte[] plain, byte[] seqNum) {
		ensureAdapter();
		return adapter.cryptohashInitUpdateFinish(dir, hashAlg, keyBlock, hashSize, recType, payloadLen, plain, seqNum);
	}

	@Override
	public void handleSSLKey(double tsvalue, int preMasterLen, byte[] preMaster, byte[] master) {
		SSLKey key = new SSLKey();
		key.setbUsed(0);
		key.setTs(tsvalue);
		key.setPreMasterLen(preMasterLen);
		key.setPreMaster(preMaster);
		key.setMasterLen(master.length);
		key.setMaster(master);
		sslkeys.add(key);
	}
	@Override
	public List<SSLKey> getSSLKeyList() {
		return sslkeys;
	}
	@Override
	public void resetSSLKeyUsage() {
		for(SSLKey key : sslkeys){
			key.setbUsed(0);
		}
	}

}
