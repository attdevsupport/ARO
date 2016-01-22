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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;
import org.springframework.beans.factory.annotation.Autowired;
import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetreader.pojo.PacketDirection;
import com.att.aro.core.securedpacketreader.ICrypto;
import com.att.aro.core.securedpacketreader.ISSLKeyService;
import com.att.aro.core.securedpacketreader.ITLSSessionInfo;
import com.att.aro.core.securedpacketreader.pojo.CryptoEnum.CryptoHashAlg;
import com.att.aro.core.securedpacketreader.pojo.SavedTLSSession;
import com.att.aro.core.securedpacketreader.pojo.TLSCipherData;
import com.att.aro.core.securedpacketreader.pojo.TLSCipherSuite;


public class TLSSessionInfoImpl implements ITLSSessionInfo {


	@InjectLogger
	private static ILogger logger;
	
	@Autowired
	ICrypto crypto;
	
	@Autowired
	ISSLKeyService sslkeyservice;
	
	static final int SHA1_MAC_LEN = 20;
	static final int MD5_MAC_LEN = 16;
	
	private static final int MAX_KEY_BLOCK_SIZE = 4096;
	//private static final int INFLATION_BUF_SIZE = 65536;
	private static final int TLS_SEQ_NUM_LEN = 8;
	private static final int COMPRESS_DEFLATE = 1;
	private static final int COMPRESS_NONE = 0;
	
	public static final int TLS_MASTER_SECRET_LEN = 48;
	public static final int TLS_RANDOM_LEN = 32;
	
	private TLSCipherSuite pSuite = null; 
	private TLSCipherData pCipherData = null;
	int compressionMethod;
	int pCipherClient = -1;
	int pCipherServer = -1;
	int objectType = -1;
	byte[] seqNum = new byte[TLS_SEQ_NUM_LEN];
	byte[] keyBlock = new byte[MAX_KEY_BLOCK_SIZE];
	int keyBlockLen;

	//for inflation
	byte[] inflationBuf = null;
	Inflater decompresser = null;
	
	private List<SavedTLSSession> savedTLSSessionsByID = new ArrayList<SavedTLSSession>();
	private List<SavedTLSSession> savedTLSSessionsByTicket = new ArrayList<SavedTLSSession>();
	
	public void init(int objectType){
		this.objectType = objectType;
		this.pSuite = null;
		this.pCipherData = null;
		this.pCipherClient = -1;
		crypto.setCryptoCipherNull(objectType, 1);
		this.pCipherServer = -1;
		crypto.setCryptoCipherNull(objectType, 0);
		this.compressionMethod = COMPRESS_NONE;
		this.keyBlockLen = 0;
		for(int i=0; i<this.seqNum.length; i++) {
			this.seqNum[i] = 0;
		}
		for(int i=0; i<this.keyBlock.length; i++) {
			this.keyBlock[i] = 0;
		}
		this.inflationBuf = null;
		this.decompresser = null;
	}
	@Override
	public TLSCipherSuite getTLSCipherSuite(){
		return this.pSuite;
	}
	@Override
	public TLSCipherData getCipherData(){
		return this.pCipherData;
	}
	@Override
	public int getCipherClient(){
		return this.pCipherClient;
	}
	@Override
	public void setpCipherClient(int pCipherClient) {
		this.pCipherClient = pCipherClient;
	}

	@Override
	public int getCipherServer(){
		return this.pCipherServer;
	}
	@Override
	public void setpCipherServer(int pCipherServer) {
		this.pCipherServer = pCipherServer;
	}

	@Override
	public int getCompressionMethod() {
		return compressionMethod;
	}

	@Override
	public void setCompressionMethod(int compressionMethod) {
		this.compressionMethod = compressionMethod;
	}

	@Override
	public Inflater getDecompresser() {
		return decompresser;
	}

	@Override
	public void copyFrom(ITLSSessionInfo tsi) {
		clean();

		//this.objectType = objectType; //NEVER copy objectType
		this.pSuite = tsi.getTLSCipherSuite();
		this.pCipherData = tsi.getCipherData();
		this.compressionMethod = tsi.getCompressionMethod();
		this.pCipherClient = tsi.getCipherClient();
		this.pCipherServer = tsi.getCipherServer();
		crypto.copyCryptoCipher(tsi.getObjectType(), this.objectType);
		this.keyBlockLen = tsi.getKeyBlockLen();
		byte[] ckeyBlock = tsi.getKeyBlock();
		System.arraycopy(ckeyBlock, 0, this.keyBlock, 0, MAX_KEY_BLOCK_SIZE);
	}

	@Override
	public void clean() {
		this.pCipherClient = -1;
		this.pCipherServer = -1;
		crypto.cryptoCipherDeinit(this.objectType);
		if (this.inflationBuf != null) {
			this.inflationBuf = null;
		}

		if (this.decompresser != null) {
			this.decompresser = null;
		}

		this.pSuite = null;
		this.pCipherData = null;
		this.compressionMethod = COMPRESS_NONE;
		this.keyBlockLen = 0;
		for(int i=0; i<this.seqNum.length; i++) {
			this.seqNum[i] = 0;
		}
		for(int i=0; i<this.keyBlock.length; i++) {
			this.keyBlock[i] = 0;
		}
	}

	@Override
	public void initDecompression() {
		switch (compressionMethod) {
		case COMPRESS_DEFLATE:
			decompresser = new Inflater();
			break;
			
		case COMPRESS_NONE:
			break;

		default:
			logger.warn("30013 - Invalid compression type.");
		}
	}

	int decompress(byte[] inBuf, int inSize, byte[] out, Integer[] decLen) {
		int ret = -1;
		switch (compressionMethod) {
			case COMPRESS_NONE:
				int index = 0;
				for(index=0; index<inSize; index++) {
					out[index] = inBuf[index];
				}
				decLen[0] = index;
				ret = 1;
				break;

			case COMPRESS_DEFLATE:
				decompresser.setInput(inBuf, 0, inSize);
				try {
					decLen[0] = decompresser.inflate(out);
					ret = 1;
				} catch (Exception e) {
					logger.warn("30015 - Error in decompression.");
				}								
				break;
				
			default:
				logger.warn("30014 - Invalid compression type.");
		}
		return ret;
	}
	@Override
	public int decrypt(byte[] enc, byte[] dec, Integer[] recPayloadLen,
			PacketDirection dir, int recType) {
		int pCipher = -1;
		if(dir == PacketDirection.UPLINK){
			//uplink traffic decrypted using the server cipher
			pCipher = 0; //this.pCipherServer; Workaround as crypto_cipher* pCipher can not be passed through JNI.
		}else if(dir == PacketDirection.DOWNLINK){
			//downlink traffic decrypted using the client cipher
			pCipher = 1; //this.pCipherClient; Workaround as crypto_cipher* pCipher can not be passed through JNI.
		}
		if (this.pSuite == null || pCipher == -1) {
			return decompress(enc, enc.length, dec, recPayloadLen);
		} 
		
		//Workaround as crypto_cipher* pCipher can not be passed through JNI.
		if(dir == PacketDirection.UPLINK) {
			pCipher = 0;
		} else {
			pCipher = 1;
		}
		
		Integer[] hashsize = new Integer[1];
		CryptoHashAlg[] hashalg = new CryptoHashAlg[1];
		
		getHashSizeAlg(this.pSuite, hashsize, hashalg);
		
		byte[] plainArr = new byte[65536];
		int rvalue = crypto.cryptoCipherDecrypt(pCipher, enc, plainArr, enc.length, this.objectType);
		if(rvalue != 0) {
			logger.warn("Error in decrypting data.");
			return -1;
		}
		
		byte[] plain = plainArr;
		int payloadLen = enc.length;

		if (pCipherData.getBlockSize() > 0) { //block cipher
			//TODO: for TLS version 1.1, see tlsv1_record.c line 389

			//Remove padding
			int padLen = plain[payloadLen - 1];
			payloadLen -= padLen + 1;
		}
		payloadLen -= hashsize[0];
		
		//Check MAC
		int ret = 0;
		String osname = System.getProperty("os.name");
		if (osname != null && osname.contains("Windows")) {
			ret = crypto.cryptoHashInitUpdateFinish(dir.ordinal(), hashalg[0].ordinal(), this.keyBlock, hashsize[0], recType, payloadLen, plain, this.seqNum);			
		}
		if(ret != 0) {
			logger.warn("Error in performing hash operation.");
			return -1;
		}
		
		if(payloadLen > 0 && decompress(plain, payloadLen, dec, recPayloadLen) != 1) {
			logger.warn("Error in decompressing data.");
			return -1;
		}

		//Increase seqnum by 1 per record
		incByteArray(this.seqNum, TLS_SEQ_NUM_LEN);
		return 1;
	}
	void incByteArray(byte[] counter, int len)
	{
		int pos = len - 1;
		while (pos >= 0) {
			counter[pos]++;
			if (counter[pos] != 0) {
				break;
			}
			pos--;
		}
	}
	@Override
	public int getObjectType() {
		return this.objectType;
	}
	@Override
	public int getKeyBlockLen() {
		return this.keyBlockLen;
	}
	@Override
	public byte[] getKeyBlock() {
		return this.keyBlock;
	}
	@Override
	public void getHashSizeAlg(TLSCipherSuite pSuite, Integer[] hashsize, CryptoHashAlg[] hashalg) {
		
		switch (pSuite.getHash()) {
			case TLS_HASH_MD5:
				hashsize[0] = MD5_MAC_LEN;
				hashalg[0] = CryptoHashAlg.CRYPTO_HASH_ALG_HMAC_MD5;
				break;

			case TLS_HASH_SHA:
				hashsize[0] = SHA1_MAC_LEN;
				hashalg[0] = CryptoHashAlg.CRYPTO_HASH_ALG_HMAC_SHA1;
				break;

			default:
				logger.warn("30023 - Invalid hash type.");
				break;
		}
	}
	
	
	
	@Override
	public int setupCiphers(byte[] master, byte[] clientRandom, byte[] serverRandom, ITLSSessionInfo tsiPending) {
			byte[] random = new byte[64];
			
			System.arraycopy(serverRandom, 0, random, 0, 32);
			
			System.arraycopy(clientRandom, 32, random, 32, 32);
	
			Integer[] hashsize = new Integer[1];
			CryptoHashAlg[] hashalg = new CryptoHashAlg[1];
			getHashSizeAlg(tsiPending.getTLSCipherSuite(), hashsize, hashalg);
	
			int blocklen = 2 * (
				hashsize[0] + //MAC secret length
				tsiPending.getCipherData().getKeyMaterial() + //key length
				tsiPending.getCipherData().getBlockSize() //IV length
			);
			tsiPending.setKeyBlockLen(blocklen);
	
			//getting the key block
			int rvalue = sslkeyservice.tlsprf(master, TLS_MASTER_SECRET_LEN, "key expansion",
				random, TLS_RANDOM_LEN + TLS_RANDOM_LEN, tsiPending.getKeyBlock(), tsiPending.getKeyBlockLen()
				);
			if (rvalue != 0) {
				logger.error("Error in deriving new, cryptographically separate keys from a given key in TLS.");
				return -1;
			}
	
			int nclient = initCipher(tsiPending.getCipherData(), tsiPending.getKeyBlock(), hashsize[0], 1);
			tsiPending.setpCipherClient(nclient);
			int nserver = initCipher(tsiPending.getCipherData(), tsiPending.getKeyBlock(), hashsize[0], 0);
			tsiPending.setpCipherServer(nserver);
	
			if ((tsiPending.getCipherClient() == -1) || (tsiPending.getCipherServer() == -1)) {
				return 0;
			} else {
				return 1;
			}
	}
	
	private int initCipher(TLSCipherData pCipherData, byte[] keyBlock, int hashsize, int bClient) {
	
			//key negotiated, now start transferring data
	
			//in keyBlock:
			//client write MAC secret
			//server write MAC secret
			//client write key
			//server write key
			//client write IV (for block ciphers only)
			//server write IV (for block ciphers only)
			byte[] temp1 = null;
			byte[] temp2 = null;
			ByteBuffer keyBlockbuff = ByteBuffer.wrap(keyBlock);
			if (bClient == 1) {
				//for decrypting data from server (DOWNLINK)
				int offset = hashsize * 2 + pCipherData.getKeyMaterial() * 2 + pCipherData.getBlockSize();
				for(int j=0; j<offset; j++) {
					keyBlockbuff.get();
				}
				temp1 = new byte[keyBlock.length - offset];
				keyBlockbuff.get(temp1, 0, keyBlock.length - offset);
				keyBlockbuff.position(0);
				
				offset = hashsize * 2 + pCipherData.getKeyMaterial();
				for(int j=0; j<offset; j++) {
					keyBlockbuff.get();
				}
				temp2 = new byte[keyBlock.length - offset];
				keyBlockbuff.get(temp2, 0, keyBlock.length - offset);
				keyBlockbuff.position(0);				
			} else {
				//for decrypting data to server (UPLINK)
				int offset = hashsize * 2 + pCipherData.getKeyMaterial() * 2;
				for(int j=0; j<offset; j++) {
					keyBlockbuff.get();
				}
				temp1 = new byte[keyBlock.length - offset];
				keyBlockbuff.get(temp1, 0, keyBlock.length - offset);
				keyBlockbuff.position(0);
				
				offset = hashsize * 2;
				for(int j=0; j<offset; j++) {
					keyBlockbuff.get();
				}
				temp2 = new byte[keyBlock.length - offset];
				keyBlockbuff.get(temp2, 0, keyBlock.length - offset);
				keyBlockbuff.position(0);
			}
			
			int ret = crypto.cryptoCipherInit(pCipherData.getAlg().ordinal(), temp1, temp2, pCipherData.getKeyMaterial(), bClient);
			if(ret != 0) {
				logger.error("Error in initializing crypto APIs.");
				return -1;
			}
			return ret;
	}
	
	@Override
	public int saveTLSSessionByID(byte[] sessionID, byte[] master) {
		return saveTLSSessionCore(savedTLSSessionsByID, sessionID, master);
	}
	
	private int saveTLSSessionCore(List<SavedTLSSession> savedTLSSessions, byte[] sessionID, byte[] master) {
		if (sessionID == null) {
			return 1;
		}
	
		SavedTLSSession[] pSaved = new SavedTLSSession[1];
		int rvalue = getSavedTLSSessionCore(savedTLSSessions, sessionID, pSaved);
		
		if (rvalue == 1) {
			boolean match = true; //memcmp(pSaved.master, master, TLS_MASTER_SECRET_LEN)
			for(int j=0; j<TLS_MASTER_SECRET_LEN; j++) {
				if(pSaved[0].getMaster()[j] != master[j]) {
					match = false;
					break;
				}
			}
			if (!match)  {
				return 0;
			} else {
				return 1;
			}
		}
	
		SavedTLSSession saved = new SavedTLSSession();
		
		System.arraycopy(master, 0, saved.getMaster(), 0, TLS_MASTER_SECRET_LEN);
		
		byte[] newsessionid =  new byte[sessionID.length];
		
		System.arraycopy(sessionID, 0, newsessionid, 0, sessionID.length);
		saved.setpSessionIDorTicket(newsessionid);
		savedTLSSessions.add(saved);
		return 1;
	}
	
	private int getSavedTLSSessionCore(List<SavedTLSSession>savedTLSSessions, byte[] sessionID, SavedTLSSession[] pSaved) {
		if (sessionID == null) {
			return 0;
		}
		
		int numb = savedTLSSessions.size();
		for (int i=0; i<numb; i++) {
			pSaved[0] = savedTLSSessions.get(i);
			
			boolean match = true; //!memcmp(pSaved.pSessionIDorTicket->GetDataAt(0), sessionID.GetDataAt(0), sessionID.GetSize()
			for(int j=0; j<sessionID.length; j++) {
				if(pSaved[0].getpSessionIDorTicket()[j] != sessionID[j]) {
					match = false;
					break;
				}
			}
			
			if (pSaved[0].getpSessionIDorTicket().length == sessionID.length && match) {
				return 1;
			}
		}
	
		return 0;
	}
	@Override
	public int saveTLSSessionByTicket(byte[] sessionID, byte[] master) {
		return saveTLSSessionCore(savedTLSSessionsByTicket, sessionID, master);
	}
	@Override
	public int getSavedTLSSessionByID(byte[] sessionID, SavedTLSSession[] pSaved) {
		return getSavedTLSSessionCore(savedTLSSessionsByID, sessionID, pSaved);
	}
	@Override
	public int getSavedTLSSessionByTicket(byte[] sessionID, SavedTLSSession[] pSaved) {
		return getSavedTLSSessionCore(savedTLSSessionsByTicket, sessionID, pSaved);
	}
	@Override
	public void clearSavedTLSSessions() {
		if(savedTLSSessionsByID != null) {
			savedTLSSessionsByID.clear();
		}
		if(savedTLSSessionsByTicket != null) {
			savedTLSSessionsByTicket.clear();
		}
	}
	@Override
	public void setKeyBlockLen(int len) {
		this.keyBlockLen = len;
	}

}//end class
