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

import java.util.zip.Inflater;
import com.att.aro.core.packetreader.pojo.PacketDirection;
import com.att.aro.core.securedpacketreader.pojo.CryptoEnum.CryptoHashAlg;
import com.att.aro.core.securedpacketreader.pojo.SavedTLSSession;
import com.att.aro.core.securedpacketreader.pojo.TLSCipherData;
import com.att.aro.core.securedpacketreader.pojo.TLSCipherSuite;

public interface ITLSSessionInfo {
	int getCipherClient();
	
	int getCipherServer();
	
	TLSCipherSuite getTLSCipherSuite();
	
	TLSCipherData getCipherData();
	
	void setpCipherClient(int pCipherClient);

	void setpCipherServer(int pCipherServer);

	int getCompressionMethod();

	void setCompressionMethod(int compressionMethod);

	Inflater getDecompresser();

	void copyFrom(ITLSSessionInfo tslinfo);

	void clean();

	void initDecompression();

	int decrypt(byte[] enc, byte[] dec, Integer[] recPayloadLen, PacketDirection dir, int recType);
	
	int getObjectType();
	int getKeyBlockLen();
	void setKeyBlockLen(int len);
	byte[] getKeyBlock();
	void getHashSizeAlg(TLSCipherSuite pSuite, Integer[] hashSize, CryptoHashAlg[] hashAlg);
	
	void clearSavedTLSSessions();
	int getSavedTLSSessionByTicket(byte[] sessionID, SavedTLSSession[] pSaved);
	int getSavedTLSSessionByID(byte[] sessionID, SavedTLSSession[] pSaved);
	int saveTLSSessionByTicket(byte[] sessionID, byte[] master);
	int saveTLSSessionByID(byte[] sessionID, byte[] master);
	int setupCiphers(byte[] master, byte[] clientRandom, byte[] serverRandom, ITLSSessionInfo tsiPending);
}
