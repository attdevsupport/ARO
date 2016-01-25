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

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.model.TraceData;
import com.att.aro.pcap.AROCryptoAdapter;
import com.att.aro.ssl.crypto_openssl.crypto_hash_alg;
import com.att.aro.util.Util;


public class SslKey implements Comparable<SslKey> {
	private static final Logger sslLogger = Logger.getLogger(SslKey.class.getName());
	private int bUsed;
	private double ts;
	private int preMasterLen;
	private int masterLen;
	private byte[] preMaster = new byte[256];
	private byte[] master = new byte[48];
	private static List<SAVED_TLS_SESSION>savedTLSSessionsByID = new ArrayList<SAVED_TLS_SESSION>();
	private static List<SAVED_TLS_SESSION>savedTLSSessionsByTicket = new ArrayList<SAVED_TLS_SESSION>();
	
	public static final int TLS_MASTER_SECRET_LEN = 48;
	public static final int TLS_RANDOM_LEN = 32;
	public static final int MD5_MAC_LEN = 16;
	public static final int SHA1_MAC_LEN = 20;
	private static final String CALG_MD5 = "MD5";
	private static final String CALG_SHA1 = "SHA-1";
	
	@Override
	public int compareTo(SslKey arg0) {
		Double.valueOf(getTs()).compareTo(
				Double.valueOf(arg0.getTs()));
		return 0;
	}
	
	public int getbUsed() {
		return bUsed;
	}
	public void setbUsed(int bUsed) {
		this.bUsed = bUsed;
	}
	public double getTs() {
		return ts;
	}
	public void setTs(double ts) {
		this.ts = ts;
	}
	public void setPreMasterLen(int preMasterLen) {
		this.preMasterLen = preMasterLen;
	}
	public void setMasterLen(int masterLen) {
		this.masterLen = masterLen;
	}
	public void setPreMaster(byte[] preMaster) {
		this.preMaster = preMaster;
	}
	public void setMaster(byte[] master) {
		this.master = master;
	}
	
	private static int sgn(double x) {
		if (x >= 0) {
			return 1; 
		} else {
			return -1;
		}
	}
	
	private static int search(int nBegin, int nEnd, double shTS) {
		if (nEnd - nBegin <= 5) {
			double best = Math.abs(AROCryptoAdapter.getSSL_keys().get(nBegin).ts - shTS);
			int bestI = nBegin;
			for (int i=nBegin+1; i<=nEnd; i++) {
				double t = Math.abs(AROCryptoAdapter.getSSL_keys().get(i).ts - shTS);
				if (t < best) {
					bestI = i;
					best = t;
				}
			}
			return bestI;
		}

		double deltaBegin = AROCryptoAdapter.getSSL_keys().get(nBegin).ts - shTS;
		double deltaEnd   = AROCryptoAdapter.getSSL_keys().get(nEnd).ts - shTS;

		if (sgn(deltaBegin) == sgn(deltaEnd)) {
			if (Math.abs(deltaBegin) < Math.abs(deltaEnd)) {
				return nBegin; 
			} else {
				return nEnd;
			}
		}

		int nMid = (nBegin + nEnd) / 2;
		double deltaMid = AROCryptoAdapter.getSSL_keys().get(nMid).ts - shTS;
		if (sgn(deltaBegin) == sgn(deltaMid)) {
			return search(nMid, nEnd, shTS);
		} else {
			return search(nBegin, nMid, shTS);
		}
	}
	
	private static boolean cryptoapiHashVector(String alg, int hashlen, int numelem, byte[] addr, int len, byte[] mac) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(alg);
			md.update(addr);
			byte[] hashdata = md.digest(); 
			for(int i=0; i<hashdata.length; i++) {
				mac[i] = hashdata[i];
			}
		} catch (NoSuchAlgorithmException e) {
			return false;
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	private static boolean cryptoapiHashVector(String alg, int hashlen, int numelem, byte[][] addr, int[] len, byte[] mac) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(alg);
			int combined_len = 0;
			for(int i=0; i<numelem; i++) {
				 combined_len += len[i];
			}
			
			byte[] combinedaddr = new byte[combined_len];
			ByteBuffer combined_addr_pData = ByteBuffer.wrap(combinedaddr);
			for(int i=0; i<numelem; i++) {
				combined_addr_pData.put(addr[i]);
			}
			
			md.update(combinedaddr);
			byte[] hashdata = md.digest(); 
			for(int i=0; i<hashdata.length; i++) {
				mac[i] = hashdata[i];
			}
		} catch (NoSuchAlgorithmException e) {
			return false;
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	private static boolean md5VectorNonFipsAllow(int numelem, byte[] addr, int len, byte[] mac)
	{
		return cryptoapiHashVector(CALG_MD5, 16, numelem, addr, len, mac);
	}
	
	private static boolean md5VectorNonFipsAllow(int numelem, byte[][] addr, int[] len, byte[] mac)
	{
		return cryptoapiHashVector(CALG_MD5, 16, numelem, addr, len, mac);
	}
	
	private static boolean sha1Vector(int numelem, byte[] addr, int len, byte[] mac)
	{
		return cryptoapiHashVector(CALG_SHA1, 20, numelem, addr, len, mac);
	}
	
	private static boolean sha1Vector(int numelem, byte[][] addr, int[] len, byte[] mac)
	{
		return cryptoapiHashVector(CALG_SHA1, 20, numelem, addr, len, mac);
	}
	
	private static boolean hmacMd5VectorNonFipsAllow(byte[] key, int keylen, int numelem, byte[][] addr, int[] len, byte[] mac, int addrIndex)
	{
		byte[] kpad = new byte[64]; 
		byte[] tk = new byte[16];
		byte[][]_addr = new byte[6][];
		int i;
		int[] _len = new int [6];
		
		if (numelem > 5) {
			return false;
		}
		
		if (keylen > 64) {
			if (md5VectorNonFipsAllow(1, key, keylen, tk) == false) {
				return false;
			}
			key = tk;
			keylen = 16;
		}
		
		for(i=0; i<64; i++) {
			kpad[i] = 0;
		}	
		for(int index=0; index<keylen; index++) {
			kpad[index] = key[index];
		}		
		for (i = 0; i<64; i++) {
			kpad[i] ^= 0x36;
		}
		
		_addr[0] = kpad;
		_len[0] = 64;
		for (i = 0; i < numelem; i++) {
			_addr[i + 1] = addr[i + addrIndex];
			_len[i + 1] = len[i + addrIndex];
		}
		if (md5VectorNonFipsAllow(1 + numelem, _addr, _len, mac) == false) {
			return false;
		}
		
		for(i=0; i<64; i++) {
			kpad[i] = 0;
		}		
		for(int index=0; index<keylen; index++) {
			kpad[index] = key[index];
		}		
		for (i = 0; i < 64; i++) {
			kpad[i] ^= 0x5c;
		}
		
		_addr[0] = kpad;
		_len[0] = 64;
		_addr[1] = mac;
		_len[1] = MD5_MAC_LEN;
		return md5VectorNonFipsAllow(2, _addr, _len, mac);
	}
	
	private static boolean hmacSha1Vector(byte[] key, int keylen, int numelem, byte[][] addr, int[] len, byte[] mac, int addrIndex)
	{
		byte[] k_pad = new byte[64]; 
		byte[] tk = new byte[16];
		byte[][]_addr = new byte[6][];
		int i;
		int[] _len = new int [6];
	
		if (numelem > 5) {
			return false;
		}
	
	    if (keylen > 64) {
			if (sha1Vector(1, key, keylen, tk) == false) {
				return false;
			}
			key = tk;
			keylen = 20;
	       }
	
	    for(i=0; i<64; i++) {
			k_pad[i] = 0;
		}	
		for(int index=0; index<keylen; index++) {
			k_pad[index] = key[index];
		}	
		for (i = 0; i < 64; i++) {
			k_pad[i] ^= 0x36;
		}
		
		_addr[0] = k_pad;
		_len[0] = 64;
		for (i = 0; i < numelem; i++) {
			_addr[i + 1] = addr[i + addrIndex];
			_len[i + 1] = len[i + addrIndex];
		}
		if (sha1Vector(1 + numelem, _addr, _len, mac) == false) {
			return false;
		}
	
		for(i=0; i<64; i++) {
			k_pad[i] = 0;
		}		
		for(int index=0; index<keylen; index++) {
			k_pad[index] = key[index];
		}	
		
		for (i = 0; i < 64; i++)
			k_pad[i] ^= 0x5c;
	
		_addr[0] = k_pad;
		_len[0] = 64;
		_addr[1] = mac;
		_len[1] = SHA1_MAC_LEN;
		return sha1Vector(2, _addr, _len, mac);
	}
	
	private static boolean hmacMd5Vector(byte[] key, int keylen, int numelem, byte[] addr, int len, byte[] mac)
	{
		byte[] k_pad = new byte[64]; 
		byte[] tk = new byte[16];
		byte[][]_addr = new byte[6][];
		int i;
		int[] _len = new int [6];
		
		if (numelem > 5) {
			return false;
		}
	
        if (keylen > 64) {
			if (md5VectorNonFipsAllow(1, key, keylen, tk) == false) {
				return false;
			}
			key = tk;
			keylen = 16;
        }
	
		for(i=0; i<64; i++) {
			k_pad[i] = 0;
		}	
		for(int index=0; index<keylen; index++) {
			k_pad[index] = key[index];
		}
		for (i = 0; i < 64; i++)
			k_pad[i] ^= 0x36;
	
		_addr[0] = k_pad;
		_len[0] = 64;
		for (i = 0; i < numelem; i++) {
			_addr[i + 1] = addr;
			_len[i + 1] = len;
		}
		if (md5VectorNonFipsAllow(1 + numelem, _addr, _len, mac) == false) {
			return false;
		}
		
		for(i=0; i<64; i++) {
			k_pad[i] = 0;
		}		
		for(int index=0; index<keylen; index++) {
			k_pad[index] = key[index];
		}
		for (i = 0; i < 64; i++) {
			k_pad[i] ^= 0x5c;
		}
	
		_addr[0] = k_pad;
		_len[0] = 64;
		_addr[1] = mac;
		_len[1] = MD5_MAC_LEN;
		return md5VectorNonFipsAllow(2, _addr, _len, mac);
	}
	
	private static boolean hmacSha1Vector(byte[] key, int keylen, int numelem, byte[] addr, int len, byte[] mac)
	{
		byte[] k_pad = new byte[64]; 
		byte[] tk = new byte[16];
		byte[][]_addr = new byte[6][];
		int i;
		int[] _len = new int [6];
	
		if (numelem > 5) {
			return false;
		}
	
	    if (keylen > 64) {
			if (sha1Vector(1, key, keylen, tk) == false) {
				return false;
			}
			key = tk;
			keylen = 20;
	    }
	
		for(i=0; i<64; i++) {
			k_pad[i] = 0;
		}	
		for(int index=0; index<keylen; index++) {
			k_pad[index] = key[index];
		}		
		for (i = 0; i < 64; i++) {
			k_pad[i] ^= 0x36;
		}
	
		_addr[0] = k_pad;
		_len[0] = 64;
		for (i = 0; i < numelem; i++) {
			_addr[i + 1] = addr;
			_len[i + 1] = len;
		}
		if (sha1Vector(1 + numelem, _addr, _len, mac) == false) {
			return false;
		}
	
		for(i=0; i<64; i++) {
			k_pad[i] = 0;
		}		
		for(int index=0; index<keylen; index++) {
			k_pad[index] = key[index];
		}	
		for (i = 0; i < 64; i++) {
			k_pad[i] ^= 0x5c;
		}
	
		_addr[0] = k_pad;
		_len[0] = 64;
		_addr[1] = mac;
		_len[1] = SHA1_MAC_LEN;
		return sha1Vector(2, _addr, _len, mac);
	}
	
	private static boolean hmacMd5NonFipsAllow(byte[] key, int keylen, byte[] data, int data_len, byte[] mac)
	{
		return hmacMd5Vector(key, keylen, 1, data, data_len, mac);
	}
	
	private static boolean hmacSha1(byte[] key, int keylen, byte[] data, int data_len, byte[] mac)
	{
		return hmacSha1Vector(key, keylen, 1, data, data_len, mac);
	}
	
	private static int tlsprf(byte[] secret, int secret_len, String label, byte[] seed, int seed_len, byte[] out, int outlen)
	{
		int L_S1, L_S2, i;
		byte[] S1 = null; 
		byte[] S2 = null;
		byte[] A_MD5 = new byte[MD5_MAC_LEN];
		byte[] A_SHA1 = new byte[SHA1_MAC_LEN];
		byte[] P_MD5 = new byte[MD5_MAC_LEN];
		byte[] P_SHA1 = new byte[SHA1_MAC_LEN];

		int MD5_pos, SHA1_pos;
		byte[][] MD5_addr = new byte[3][];
		int[] MD5_len = new int[3];
		byte[][] SHA1_addr = new byte[3][];
		int[] SHA1_len = new int[3];

		int result = secret_len & 1;
		if (result != 0) {
			return -1;
		}

		MD5_addr[0] = A_MD5;
		MD5_len[0] = MD5_MAC_LEN;
		MD5_addr[1] = label.getBytes();
		MD5_len[1] = label.length();
		MD5_addr[2] = seed;
		MD5_len[2] = seed_len;

		SHA1_addr[0] = A_SHA1;
		SHA1_len[0] = SHA1_MAC_LEN;
		SHA1_addr[1] = label.getBytes();
		SHA1_len[1] = label.length();
		SHA1_addr[2] = seed;
		SHA1_len[2] = seed_len;

		L_S1 = L_S2 = (secret_len + 1) / 2;
		S1 = secret;
		
		ByteBuffer pData_secret = ByteBuffer.wrap(secret);
		int offset = L_S1; 
		for(int j=0; j<offset; j++) {
			pData_secret.get();
		}
		S2 = new byte[secret.length - L_S1];
		pData_secret.get(S2, 0, secret.length - L_S1);
		pData_secret.position(0);
		
		result = secret_len & 1; 
		if (result != 0) {
			offset = L_S1 - 1; 
			for(int j=0; j<offset; j++) {
				pData_secret.get();
			}
			S2 = new byte[secret.length - (L_S1-1)];
			pData_secret.get(S2, 0, secret.length - (L_S1-1));
			pData_secret.position(0);
		}

		hmacMd5VectorNonFipsAllow(S1, L_S1, 2, MD5_addr, MD5_len, A_MD5, 1);
		hmacSha1Vector(S2, L_S2, 2, SHA1_addr, SHA1_len, A_SHA1, 1);

		MD5_pos = MD5_MAC_LEN;
		SHA1_pos = SHA1_MAC_LEN;
		for (i = 0; i < outlen; i++) {
			if (MD5_pos == MD5_MAC_LEN) {
				hmacMd5VectorNonFipsAllow(S1, L_S1, 3, MD5_addr, MD5_len, P_MD5, 0);
				MD5_pos = 0;
				hmacMd5NonFipsAllow(S1, L_S1, A_MD5, MD5_MAC_LEN, A_MD5);
			}
			if (SHA1_pos == SHA1_MAC_LEN) {
				hmacSha1Vector(S2, L_S2, 3, SHA1_addr, SHA1_len, P_SHA1, 0);
				SHA1_pos = 0;
				hmacSha1(S2, L_S2, A_SHA1, SHA1_MAC_LEN, A_SHA1);
			}

			out[i] = (byte) (P_MD5[MD5_pos] ^ P_SHA1[SHA1_pos]);
			MD5_pos++;
			SHA1_pos++;
		}

		return 0;
	}
	
	private static int match(SslKey key, byte[] random, byte[] master) {
		int ret = tlsprf(key.preMaster, key.preMasterLen, "master secret", random, 
					TLS_RANDOM_LEN + TLS_RANDOM_LEN, master, TLS_MASTER_SECRET_LEN);
		if(ret == -1) {
			sslLogger.log(Level.FINE, "^^^^^^TLS WARNING^^^^^^ ### " + Util.RB.getString("tls.error.prf"));
		}

		boolean match = true;
		for(int j=0; j<TLS_MASTER_SECRET_LEN; j++) {
			if(master[j] != key.master[j]) {
				match = false;
				break;
			}
		}
		if (ret == 0 && match == true) {
			key.bUsed = 1;
			return 1;
		} else {
			return 0;
		}
	}
	
	public static int getMasterFromSSLLog(double serverHelloTS, byte[] master, byte[] clientRandom, byte[] serverRandom) {
		int n = AROCryptoAdapter.getSSL_keys().size();
		if (n == 0) {
			return 0;
		}

		byte[] random = new byte[64];
		for(int j=0; j<32; j++) {
			random[j] = clientRandom[j];
		}
		
		int k = 0;
		for(int j=32; j<64; j++) {
			random[j] = serverRandom[k++];
		}
		
		//Step 1: find the key whose ts is closest to serverHelloTS
		int i = search(0, n-1, serverHelloTS);
		int j = i + 1;

		while (true) {
			while (i >= 0) {
				if ((j < n)
						&& (Math.abs(AROCryptoAdapter.getSSL_keys().get(j).ts
								- serverHelloTS) < Math.abs(AROCryptoAdapter
								.getSSL_keys().get(i).ts - serverHelloTS))) {
					break;
				}
				if ((AROCryptoAdapter.getSSL_keys().get(i).bUsed == 0)
						&& (match(AROCryptoAdapter.getSSL_keys().get(i),
								random, master) == 1)) {
					return 1;
				}
				i--;
			}

			while (j < n) {
				if ((i >= 0)
						&& (Math.abs(AROCryptoAdapter.getSSL_keys().get(i).ts
								- serverHelloTS) < Math.abs(AROCryptoAdapter
								.getSSL_keys().get(j).ts - serverHelloTS))) {
					break;
				}
				if ((AROCryptoAdapter.getSSL_keys().get(j).bUsed == 0)
						&& (match(AROCryptoAdapter.getSSL_keys().get(j),
								random, master) == 1)) {
					return 1;
				}
				j++;
			}

			if ((i < 0) && (j >= n)) {
				break;
			}
		}
		return 0;
	}
	
	public static int setupCiphers(byte[] master, byte[] clientRandom, byte[] serverRandom, TLS_SESSION_INFO tsiPending) {
			byte[] random = new byte[64];
			for(int j=0; j<32; j++) {
				random[j] = serverRandom[j];
			}
			
			int k = 0;
			for(int j=32; j<64; j++) {
				random[j] = clientRandom[k++];
			}

			Integer[] hash_size = new Integer[1];
			crypto_openssl.crypto_hash_alg[] hash_alg = new crypto_hash_alg[1];
			getHashSizeAlg(tsiPending.pSuite, hash_size, hash_alg);

			tsiPending.keyBlockLen = 2 * (
				hash_size[0] + //MAC secret length
				tsiPending.pCipherData.key_material + //key length
				tsiPending.pCipherData.block_size //IV length
			);

			//getting the key block
			int r = tlsprf(master, TLS_MASTER_SECRET_LEN, "key expansion",
				random, TLS_RANDOM_LEN + TLS_RANDOM_LEN, tsiPending.keyBlock, tsiPending.keyBlockLen
				);
			if (r != 0) {
				MessageDialogFactory.showMessageDialog(null, Util.RB.getString("tls.error.prf"), Util.RB.getString("Error.title"), JOptionPane.ERROR_MESSAGE);
				return -1;
			}

			tsiPending.pCipherClient = initCipher(tsiPending.pCipherData, tsiPending.keyBlock, hash_size[0], 1);
			tsiPending.pCipherServer = initCipher(tsiPending.pCipherData, tsiPending.keyBlock, hash_size[0], 0);

			if ((tsiPending.pCipherClient == -1) || (tsiPending.pCipherServer == -1)) {
				return 0;
			} else {
				return 1;
			}
	}
	
	private static int initCipher(tls_cipher_data pCipherData, byte[] keyBlock, int hash_size, int bClient) {

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
				int offset = hash_size * 2 + pCipherData.key_material * 2 + pCipherData.block_size;
				for(int j=0; j<offset; j++) {
					keyBlockbuff.get();
				}
				temp1 = new byte[keyBlock.length - offset];
				keyBlockbuff.get(temp1, 0, keyBlock.length - offset);
				keyBlockbuff.position(0);
				
				offset = hash_size * 2 + pCipherData.key_material;
				for(int j=0; j<offset; j++) {
					keyBlockbuff.get();
				}
				temp2 = new byte[keyBlock.length - offset];
				keyBlockbuff.get(temp2, 0, keyBlock.length - offset);
				keyBlockbuff.position(0);				
			} else {
				//for decrypting data to server (UPLINK)
				int offset = hash_size * 2 + pCipherData.key_material * 2;
				for(int j=0; j<offset; j++) {
					keyBlockbuff.get();
				}
				temp1 = new byte[keyBlock.length - offset];
				keyBlockbuff.get(temp1, 0, keyBlock.length - offset);
				keyBlockbuff.position(0);
				
				offset = hash_size * 2;
				for(int j=0; j<offset; j++) {
					keyBlockbuff.get();
				}
				temp2 = new byte[keyBlock.length - offset];
				keyBlockbuff.get(temp2, 0, keyBlock.length - offset);
				keyBlockbuff.position(0);
			}
			
			int ret = TraceData.getCryptAdapter().cryptocipherinit(pCipherData.alg.ordinal(), temp1, temp2, pCipherData.key_material, bClient);
			if(ret != 0) {
				MessageDialogFactory.showMessageDialog(null, Util.RB.getString("tls.error.init"), Util.RB.getString("Error.title"), JOptionPane.ERROR_MESSAGE);
				return -1;
			}
			return ret;
	}
	
	static void getHashSizeAlg(tls_cipher_suite pSuite, Integer[] hash_size, crypto_hash_alg[] hash_alg) {
		if(pSuite == null) {
			System.out.println("asd");
		}
		switch (pSuite.hash) {
			case TLS_HASH_MD5:
				hash_size[0] = crypto_openssl.MD5_MAC_LEN;
				hash_alg[0] = crypto_openssl.crypto_hash_alg.CRYPTO_HASH_ALG_HMAC_MD5;
				break;

			case TLS_HASH_SHA:
				hash_size[0] = crypto_openssl.SHA1_MAC_LEN;
				hash_alg[0] = crypto_openssl.crypto_hash_alg.CRYPTO_HASH_ALG_HMAC_SHA1;
				break;

			default:
				sslLogger.warning("30023 - Invalid hash type.");
				break;
		}
	}
	
	public static int saveTLSSessionByID(byte[] sessionID, byte[] master) {
		return saveTLSSessionCore(savedTLSSessionsByID, sessionID, master);
	}
	
	private static int saveTLSSessionCore(List<SAVED_TLS_SESSION>savedTLSSessions, byte[] sessionID, byte[] master) {
		if (sessionID == null) {
			return 1;
		}

		SAVED_TLS_SESSION[] pSaved = new SAVED_TLS_SESSION[1];
		int r = getSavedTLSSessionCore(savedTLSSessions, sessionID, pSaved);
		
		if (r == 1) {
			boolean match = true; //memcmp(pSaved.master, master, TLS_MASTER_SECRET_LEN)
			for(int j=0; j<TLS_MASTER_SECRET_LEN; j++) {
				if(pSaved[0].master[j] != master[j]) {
					match = false;
					break;
				}
			}
			if (match == false)  {
				return 0;
			} else {
				return 1;
			}
		}

		SAVED_TLS_SESSION saved = new SAVED_TLS_SESSION();
		for(int j=0; j<TLS_MASTER_SECRET_LEN; j++) {   //memcpy(saved.master, master, TLS_MASTER_SECRET_LEN);
			saved.master[j] = master[j];
		}
		saved.pSessionIDorTicket = new byte[sessionID.length];
		for(int j=0; j<sessionID.length; j++) {   //saved.pSessionIDorTicket->SetData(sessionID);
			saved.pSessionIDorTicket[j] = sessionID[j];
		}
		savedTLSSessions.add(saved);
		return 1;
	}
	
	private static int getSavedTLSSessionCore(List<SAVED_TLS_SESSION>savedTLSSessions, byte[] sessionID, SAVED_TLS_SESSION[] pSaved) {
		if (sessionID == null) {
			return 0;
		}
		
		int n = savedTLSSessions.size();
		for (int i=0; i<n; i++) {
			pSaved[0] = savedTLSSessions.get(i);
			
			boolean match = true; //!memcmp(pSaved.pSessionIDorTicket->GetDataAt(0), sessionID.GetDataAt(0), sessionID.GetSize()
			for(int j=0; j<sessionID.length; j++) {
				if(pSaved[0].pSessionIDorTicket[j] != sessionID[j]) {
					match = false;
					break;
				}
			}
			
			if (pSaved[0].pSessionIDorTicket.length == sessionID.length && match == true) {
				return 1;
			}
		}

		return 0;
	}
	
	public static int saveTLSSessionByTicket(byte[] sessionID, byte[] master) {
		return saveTLSSessionCore(savedTLSSessionsByTicket, sessionID, master);
	}
	
	public static int getSavedTLSSessionByID(byte[] sessionID, SAVED_TLS_SESSION[] pSaved) {
		return getSavedTLSSessionCore(savedTLSSessionsByID, sessionID, pSaved);
	}
	
	public static int getSavedTLSSessionByTicket(byte[] sessionID, SAVED_TLS_SESSION[] pSaved) {
		return getSavedTLSSessionCore(savedTLSSessionsByTicket, sessionID, pSaved);
	}
	
	public static void clearSavedTLSSessions() {
		if(savedTLSSessionsByID != null) {
			savedTLSSessionsByID.clear();
		}
		if(savedTLSSessionsByTicket != null) {
			savedTLSSessionsByTicket.clear();
		}
	}
}
