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

import java.io.IOException;

import com.att.aro.core.securedpacketreader.IReceiveSSLKey;
import com.att.aro.core.util.Util;

public class CryptoAdapter {
	
	public native int ReadSSLKeys(String filename);
	public native int cryptocipherinit(int alg, byte[] temp1, byte[] temp2, int key_material, int bClient);
	public native void cryptocipherdeinit(int objectType);
	public native void setcryptociphernull(int objectType, int bClient);
	public native int cryptocipherdecrypt(int pCipher, byte[] enc, byte[] _plain, int enclength, int bClient);
	public native void copycryptocipher(int from_objectType, int to_objectType);
	public native int cryptohashInitUpdateFinish(
			int dir, int hash_alg, byte[] keyBlock, int hash_size, int recType, int payloadLen, byte[] plain, byte[] seqNum);
	
	IReceiveSSLKey subscriber;
	public void setSubscriber(IReceiveSSLKey subscriber){
		this.subscriber = subscriber;
	}
	private void sslKeyHandler(double ts, int preMasterLen, byte[] preMaster, byte[] master) {
		if(subscriber != null){
			this.subscriber.handleSSLKey(ts, preMasterLen, preMaster, master);
		}
	}

	public void loadAroCryptoLib() {
		String osname = System.getProperty("os.name");
		if (osname != null && osname.contains("Windows")) {
			String os = System.getProperty("os.arch");
			if (os != null && os.contains("64")) {
				String filename = "AROCrypt64";
				String libFolder = Util.makeLibFilesFromJar(filename);
				Util.loadLibrary(filename, libFolder);
//				System.loadLibrary("AROCrypt64");
			} else {
				String filename = "AROCrypt";
				String libFolder = Util.makeLibFilesFromJar(filename);
				Util.loadLibrary(filename, libFolder);
//				System.loadLibrary("AROCrypt");
			}
		} else if (osname != null && osname.contains("Mac")) {
			String filename = "AROCrypt";
			String libFolder = Util.makeLibFilesFromJar(filename);
			Util.loadLibrary(filename, libFolder);
//			System.loadLibrary("AROCrypt");
		}
	}
}
