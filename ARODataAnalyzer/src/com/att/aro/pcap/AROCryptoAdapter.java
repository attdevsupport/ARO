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
package com.att.aro.pcap;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.ssl.SslKey;
import com.att.aro.util.Util;


public class AROCryptoAdapter {
	
	private static final Logger logger = Logger.getLogger(AROCryptoAdapter.class.getName());
	private static List<SslKey> ssl_keys = new ArrayList<SslKey>();
	
	public AROCryptoAdapter() {
		loadAroCryptoLib();
	}
	
	public native int ReadSSLKeys(String filename);
	public native int cryptocipherinit(int alg, byte[] temp1, byte[] temp2, int key_material, int bClient);
	public native void cryptocipherdeinit(int objectType);
	public native void setcryptociphernull(int objectType, int bClient);
	public native int cryptocipherdecrypt(int pCipher, byte[] enc, byte[] _plain, int enclength, int bClient);
	public native void copycryptocipher(int from_objectType, int to_objectType);
	public native int cryptohashInitUpdateFinish(
			int dir, int hash_alg, byte[] keyBlock, int hash_size, int recType, int payloadLen, byte[] plain, byte[] seqNum);
	
	private void sslKeyHandler(double ts, int preMasterLen, byte[] preMaster, byte[] master) {
		SslKey key = new SslKey();
		key.setbUsed(0);
		key.setTs(ts);
		key.setPreMasterLen(preMasterLen);
		key.setPreMaster(preMaster);
		key.setMasterLen(48);
		key.setMaster(master);
		ssl_keys.add(key);
	}

	public void loadAroCryptoLib() {
		String osname = System.getProperty("os.name");
		if (osname != null && osname.contains("Windows")) {
			String os = System.getProperty("os.arch");
			if (os != null && os.contains("64")) {
				System.loadLibrary("AROCrypt64");
			} else {
				System.loadLibrary("AROCrypt");
			}
		} else if (osname != null && osname.contains("Mac")) {
			System.loadLibrary("AROCrypt");
		}
	}
	
	static public List<SslKey> getSSL_keys() {
		return ssl_keys;
	}
	
	static public void resetSSL_keys() {
		int size = ssl_keys.size();
		for(int index=0; index<size; index++) {
			ssl_keys.get(index).setbUsed(0);
		}
	}
}