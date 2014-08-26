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
package com.att.aro.ssl;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Inflater;

import com.att.aro.model.PacketInfo;
import com.att.aro.model.TraceData;
import com.att.aro.ssl.crypto_openssl.crypto_hash_alg;
import com.att.aro.util.Util;

public class TLS_SESSION_INFO {

	private static final Logger logger = Logger.getLogger(TLS_SESSION_INFO.class.getName());
	private static final int MAX_KEY_BLOCK_SIZE = 4096;
	private static final int INFLATION_BUF_SIZE = 65536;
	private static final int TLS_SEQ_NUM_LEN = 8;
	private static final int COMPRESS_DEFLATE = 1;
	private static final int COMPRESS_NONE = 0;
	
	public tls_cipher_suite pSuite = null; 
	public tls_cipher_data pCipherData = null;
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
	
	public void setpCipherClient(int pCipherClient) {
		this.pCipherClient = pCipherClient;
	}

	public void setpCipherServer(int pCipherServer) {
		this.pCipherServer = pCipherServer;
	}
	
	public int getCompressionMethod() {
		return compressionMethod;
	}
	
	public void setCompressionMethod(int compressionMethod) {
		this.compressionMethod = compressionMethod;
	}
	
	public Inflater getDecompresser() {
		return decompresser;
	}

	public TLS_SESSION_INFO(int objectType) {
		this.objectType = objectType;
		this.pSuite = null;
		this.pCipherData = null;
		this.pCipherClient = -1;
		TraceData.getCryptAdapter().setcryptociphernull(this.objectType, 1);
		this.pCipherServer = -1;
		TraceData.getCryptAdapter().setcryptociphernull(this.objectType, 0);
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
	
	public void CopyFrom(TLS_SESSION_INFO tsi) {
		Clean();

		//this.objectType = objectType; //NEVER copy objectType
		this.pSuite = tsi.pSuite;
		this.pCipherData = tsi.pCipherData;
		this.compressionMethod = tsi.compressionMethod;
		this.pCipherClient = tsi.pCipherClient;
		this.pCipherServer = tsi.pCipherServer;
		TraceData.getCryptAdapter().copycryptocipher(tsi.objectType, this.objectType);
		this.keyBlockLen = tsi.keyBlockLen;
		for(int i=0; i<MAX_KEY_BLOCK_SIZE; i++) {
			this.keyBlock[i] = tsi.keyBlock[i];
		}
	}
	
	public void Clean() {
		this.pCipherClient = -1;
		this.pCipherServer = -1;
		TraceData.getCryptAdapter().cryptocipherdeinit(this.objectType);
		
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
	
	public void InitDecompression() {
		switch (compressionMethod) {
			case COMPRESS_DEFLATE:
				decompresser = new Inflater();
				break;
				
			case COMPRESS_NONE:
				break;

			default:
				logger.log(Level.FINE, "30013 - Invalid compression type.");
		}
	}
	
	int Decompress(byte[] inBuf, int inSize, byte[] out, Integer[] decLen) {
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
					logger.log(Level.FINE, "30015 - Error in decompression.");
				}								
				break;
				
			default:
				logger.log(Level.FINE, "30014 - Invalid compression type.");
		}
		return ret;
	}

	public int Decrypt(byte[] enc, byte[] dec, Integer[] recPayloadLen, PacketInfo.Direction dir, int recType) {
		int pCipher = -1;
		switch (dir) {
			case UPLINK:
				//uplink traffic decrypted using the server cipher
				pCipher = 0; //this.pCipherServer; Workaround as crypto_cipher* pCipher can not be passed through JNI.
				break;

			case DOWNLINK:
				//downlink traffic decrypted using the client cipher
				pCipher = 1; //this.pCipherClient; Workaround as crypto_cipher* pCipher can not be passed through JNI.
				break;
		}
		
		if (this.pSuite == null || pCipher == -1) {
			return Decompress(enc, enc.length, dec, recPayloadLen);
		} 
		
		//Workaround as crypto_cipher* pCipher can not be passed through JNI.
		if(dir == PacketInfo.Direction.UPLINK) {
			pCipher = 0;
		} else {
			pCipher = 1;
		}
		
		Integer[] hash_size = new Integer[1];
		crypto_openssl.crypto_hash_alg[] hash_alg = new crypto_hash_alg[1];
		SslKey.getHashSizeAlg(this.pSuite, hash_size, hash_alg);
		
		int PLAIN_TEXT_BUF_SIZE = 65536;
		byte[] _plain = new byte[PLAIN_TEXT_BUF_SIZE];
		int r = TraceData.getCryptAdapter().cryptocipherdecrypt(pCipher, enc, _plain, enc.length, this.objectType);
		if(r != 0) {
			logger.fine(Util.RB.getString("tls.error.decrypt"));
			return -1;
		}
		
		byte[] plain = _plain;
		int payloadLen = enc.length;

		if (pCipherData.block_size > 0) { //block cipher
			//TODO: for TLS version 1.1, see tlsv1_record.c line 389

			//Remove padding
			int padLen = plain[payloadLen - 1];
			payloadLen -= padLen + 1;
		}
		payloadLen -= hash_size[0];
		
		//Check MAC
		int ret = 0;
		String osname = System.getProperty("os.name");
		if (osname != null && osname.contains("Windows")) {
			ret = TraceData.getCryptAdapter().cryptohashInitUpdateFinish(
					dir.ordinal(), hash_alg[0].ordinal(), this.keyBlock, hash_size[0], recType, payloadLen, plain, this.seqNum);			
		}
		if(ret != 0) {
			logger.fine(Util.RB.getString("tls.error.hash"));
			return -1;
		}
		
		if (payloadLen > 0) {
			if(Decompress(plain, payloadLen, dec, recPayloadLen) != 1) {
				logger.fine(Util.RB.getString("tls.error.decompress"));
				return -1;
			}
		}

		//Increase seqnum by 1 per record
		inc_byte_array(this.seqNum, TLS_SEQ_NUM_LEN);
		return 1;
	}
	
	void inc_byte_array(byte[] counter, int len)
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
}
