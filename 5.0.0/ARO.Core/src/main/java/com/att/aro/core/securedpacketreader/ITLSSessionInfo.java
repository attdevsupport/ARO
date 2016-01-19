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
