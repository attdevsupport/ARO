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
