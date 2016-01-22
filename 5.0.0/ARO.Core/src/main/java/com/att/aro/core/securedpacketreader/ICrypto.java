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
package com.att.aro.core.securedpacketreader;

import java.util.List;

import com.att.aro.core.securedpacketreader.pojo.SSLKey;

public interface ICrypto {
	int readSSLKeys(String filename);
	int cryptoCipherInit(int alg, byte[] temp1, byte[] temp2, int keymaterial, int bClient);
	void cryptoCipherDeinit(int objectType);
	void setCryptoCipherNull(int objectType, int bClient);
	int cryptoCipherDecrypt(int pCipher, byte[] enc, byte[] plain, int enclength, int bClient);
	void copyCryptoCipher(int fromObjectType, int toObjectType);
	int cryptoHashInitUpdateFinish(int dir, int hashalg, byte[] keyBlock, int hashsize, int recType, int payloadLen, byte[] plain, byte[] seqNum);
	List<SSLKey> getSSLKeyList();
	/**
	 * reset all flags of bUsage to 0
	 */
	void resetSSLKeyUsage();
}
