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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.securedpacketreader.ICrypto;
import com.att.aro.core.securedpacketreader.ISSLKeyService;
import com.att.aro.core.securedpacketreader.pojo.SSLKey;


public class SSLKeyServiceImpl implements ISSLKeyService {

	@Autowired
	ICrypto crypto;
	

	@InjectLogger
	private static ILogger logger;
	
	public static final int TLS_MASTER_SECRET_LEN = 48;
	public static final int TLS_RANDOM_LEN = 32;
	public static final int MD5_MAC_LEN = 16;
	public static final int SHA1_MAC_LEN = 20;
	private static final String CALG_MD5 = "MD5";
	private static final String CALG_SHA1 = "SHA-1";
	
	@Override
	public int getMasterFromSSLLog(double serverHelloTS, byte[] master, byte[] clientRandom, byte[] serverRandom) {
		List<SSLKey> keylist = crypto.getSSLKeyList();
		int listsize = keylist.size();
		byte[] random = new byte[64];
		
		System.arraycopy(clientRandom, 0, random, 0, 32);
		
		System.arraycopy(serverRandom, 32, random, 32, 32);
		
		//Step 1: find the key whose ts is closest to serverHelloTS
		int ivalue = search(0, listsize-1, serverHelloTS);
		int jvalue = ivalue + 1;
		while (true) {
			while (ivalue >= 0) {
				if ((jvalue < listsize)
						&& (Math.abs(keylist.get(jvalue).getTsvalue()
								- serverHelloTS) < Math.abs(keylist.get(ivalue).getTsvalue() - serverHelloTS))) {
					break;
				}
				if ((keylist.get(ivalue).getbUsed() == 0) && (match(keylist.get(ivalue),random, master) == 1)) {
					return 1;
				}
				ivalue--;
			}

			while (jvalue < listsize) {
				if ((ivalue >= 0)
						&& (Math.abs(keylist.get(ivalue).getTsvalue()
								- serverHelloTS) < Math.abs(keylist.get(jvalue).getTsvalue() - serverHelloTS))) {
					break;
				}
				if ((keylist.get(jvalue).getbUsed() == 0) && (match(keylist.get(jvalue),random, master) == 1)) {
					return 1;
				}
				jvalue++;
			}

			if ((ivalue < 0) && (jvalue >= listsize)) {
				break;
			}
		}
		return 0;
	}
	private int match(SSLKey key, byte[] random, byte[] master) {
		int ret = tlsprf(key.getPreMaster(), key.getPreMasterLen(), "master secret", random, 
					TLS_RANDOM_LEN + TLS_RANDOM_LEN, master, TLS_MASTER_SECRET_LEN);
		if(ret == -1) {
			logger.warn("Error in deriving new, cryptographically separate keys from a given key in TLS.");
		}

		boolean match = true;
		for(int j=0; j<TLS_MASTER_SECRET_LEN; j++) {
			if(master[j] != key.getMaster()[j]) {
				match = false;
				break;
			}
		}
		if (ret == 0 && match) {
			key.setbUsed(1);
			return 1;
		} else {
			return 0;
		}
	}
	@Override
	public int tlsprf(byte[] secret, int secretLen, String label, byte[] seed, int seedLen, byte[] out, int outlen)
	{
		int ls1, ls2;
		byte[] s1arr = null; 
		byte[] s2arr = null;
		byte[] aMD5 = new byte[MD5_MAC_LEN];
		byte[] aSHA1 = new byte[SHA1_MAC_LEN];
		byte[] pMD5 = new byte[MD5_MAC_LEN];
		byte[] pSHA1 = new byte[SHA1_MAC_LEN];

		int md5pos, sha1pos;
		byte[][] md5addr = new byte[3][];
		int[] md5len = new int[3];
		byte[][] sha1addr = new byte[3][];
		int[] sha1len = new int[3];

		int result = secretLen & 1;
		if (result != 0) {
			return -1;
		}

		md5addr[0] = aMD5;
		md5len[0] = MD5_MAC_LEN;
		md5addr[1] = label.getBytes();
		md5len[1] = label.length();
		md5addr[2] = seed;
		md5len[2] = seedLen;

		sha1addr[0] = aSHA1;
		sha1len[0] = SHA1_MAC_LEN;
		sha1addr[1] = label.getBytes();
		sha1len[1] = label.length();
		sha1addr[2] = seed;
		sha1len[2] = seedLen;

		ls1 = ls2 = (secretLen + 1) / 2;
		s1arr = secret;
		
		ByteBuffer pDatasecret = ByteBuffer.wrap(secret);
		int offset = ls1; 
		for(int j=0; j<offset; j++) {
			pDatasecret.get();
		}
		s2arr = new byte[secret.length - ls1];
		pDatasecret.get(s2arr, 0, secret.length - ls1);
		pDatasecret.position(0);
		
		result = secretLen & 1; 
		if (result != 0) {
			offset = ls1 - 1; 
			for(int j=0; j<offset; j++) {
				pDatasecret.get();
			}
			s2arr = new byte[secret.length - (ls1-1)];
			pDatasecret.get(s2arr, 0, secret.length - (ls1-1));
			pDatasecret.position(0);
		}

		hmacMd5VectorNonFipsAllow(s1arr, ls1, 2, md5addr, md5len, aMD5, 1);
		hmacSha1Vector(s2arr, ls2, 2, sha1addr, sha1len, aSHA1, 1);

		md5pos = MD5_MAC_LEN;
		sha1pos = SHA1_MAC_LEN;
		for (int i = 0; i < outlen; i++) {
			if (md5pos == MD5_MAC_LEN) {
				hmacMd5VectorNonFipsAllow(s1arr, ls1, 3, md5addr, md5len, pMD5, 0);
				md5pos = 0;
				hmacMd5NonFipsAllow(s1arr, ls1, aMD5, MD5_MAC_LEN, aMD5);
			}
			if (sha1pos == SHA1_MAC_LEN) {
				hmacSha1Vector(s2arr, ls2, 3, sha1addr, sha1len, pSHA1, 0);
				sha1pos = 0;
				hmacSha1(s2arr, ls2, aSHA1, SHA1_MAC_LEN, aSHA1);
			}

			out[i] = (byte) (pMD5[md5pos] ^ pSHA1[sha1pos]);
			md5pos++;
			sha1pos++;
		}

		return 0;
	}
	private boolean cryptoapiHashVector(String alg, int hashlen, int numelem, byte[] addr, int len, byte[] mac) {
		//TODO: why is this variable unused?
		logger.debug("unused parameters => hashlen: "+hashlen+", len: "+len+", numelem: "+numelem);
		
		MessageDigest msgd = null;
		try {
			msgd = MessageDigest.getInstance(alg);
			msgd.update(addr);
			byte[] hashdata = msgd.digest(); 
			System.arraycopy(hashdata, 0, mac, 0, hashdata.length);
		} catch (NoSuchAlgorithmException e) {
			return false;
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	private boolean cryptoapiHashVector(String alg, int hashlen, int numelem, byte[][] addr, int[] len, byte[] mac) {
		//TODO: why is this variable unused?
		logger.debug("unused parameters => hashlen: "+hashlen);
		MessageDigest msgd = null;
		try {
			msgd = MessageDigest.getInstance(alg);
			int combinedlen = 0;
			for(int i=0; i<numelem; i++) {
				 combinedlen += len[i];
			}
			
			byte[] combinedaddr = new byte[combinedlen];
			ByteBuffer combinedaddrpData = ByteBuffer.wrap(combinedaddr);
			for(int i=0; i<numelem; i++) {
				combinedaddrpData.put(addr[i]);
			}
			
			msgd.update(combinedaddr);
			byte[] hashdata = msgd.digest();
			System.arraycopy(hashdata, 0, mac, 0, hashdata.length);
		} catch (NoSuchAlgorithmException e) {
			return false;
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	private boolean md5VectorNonFipsAllow(int numelem, byte[] addr, int len, byte[] mac)
	{
		return cryptoapiHashVector(CALG_MD5, 16, numelem, addr, len, mac);
	}
	
	private boolean md5VectorNonFipsAllow(int numelem, byte[][] addr, int[] len, byte[] mac)
	{
		return cryptoapiHashVector(CALG_MD5, 16, numelem, addr, len, mac);
	}
	
	private boolean sha1Vector(int numelem, byte[] addr, int len, byte[] mac)
	{
		return cryptoapiHashVector(CALG_SHA1, 20, numelem, addr, len, mac);
	}
	
	private boolean sha1Vector(int numelem, byte[][] addr, int[] len, byte[] mac)
	{
		return cryptoapiHashVector(CALG_SHA1, 20, numelem, addr, len, mac);
	}
	
	private boolean hmacMd5VectorNonFipsAllow(byte[] keyArr, int keylenValue, int numelem, byte[][] addr, int[] len, byte[] mac, int addrIndex)
	{
		byte[] kpad = new byte[64]; 
		byte[] tkarr = new byte[16];
		byte[][] addrArr = new byte[6][];
		int[] lenArr = new int [6];
		
		byte[] key = keyArr;
		int keylen = keylenValue;
		
		if (numelem > 5) {
			return false;
		}
		
		if (keylen > 64) {
			if (!md5VectorNonFipsAllow(1, key, keylen, tkarr)) {
				return false;
			}
			key = tkarr;
			keylen = 16;
		}
		
		for(int i=0; i<64; i++) {
			kpad[i] = 0;
		}		
		System.arraycopy(key, 0, kpad, 0, keylen);
		for (int i = 0; i<64; i++) {
			kpad[i] ^= 0x36;
		}
		
		addrArr[0] = kpad;
		lenArr[0] = 64;
		
		System.arraycopy(addr, addrIndex, addrArr, 1, numelem-1);
		System.arraycopy(len, addrIndex, lenArr, 1, numelem-1);
		if (!md5VectorNonFipsAllow(1 + numelem, addrArr, lenArr, mac)) {
			return false;
		}
		
		for(int i=0; i<64; i++) {
			kpad[i] = 0;
		}		
		
		System.arraycopy(key, 0, kpad, 0, keylen);
		for (int i = 0; i < 64; i++) {
			kpad[i] ^= 0x5c;
		}
		
		addrArr[0] = kpad;
		lenArr[0] = 64;
		addrArr[1] = mac;
		lenArr[1] = MD5_MAC_LEN;
		return md5VectorNonFipsAllow(2, addrArr, lenArr, mac);
	}
	
	private boolean hmacSha1Vector(byte[] keyArr, int keylenValue, int numelem, byte[][] addr, int[] len, byte[] mac, int addrIndex)
	{
		//TODO: why is this variable unused?
		logger.debug("unused parameters => addrIndex: "+addrIndex);
		byte[] kpad = new byte[64]; 
		byte[] tkarr = new byte[16];
		byte[][] addrArr = new byte[6][];
		int[] lenArr = new int [6];
	
		byte[] key = keyArr;
		int keylen = keylenValue;
		
		if (numelem > 5) {
			return false;
		}
	
	    if (keylen > 64) {
			if (!sha1Vector(1, key, keylen, tkarr)) {
				return false;
			}
			key = tkarr;
			keylen = 20;
	       }
	
	    for(int i=0; i<64; i++) {
			kpad[i] = 0;
		}
		System.arraycopy(key, 0, kpad, 0, keylen);
		for (int i = 0; i < 64; i++) {
			kpad[i] ^= 0x36;
		}
		
		addrArr[0] = kpad;
		lenArr[0] = 64;
		System.arraycopy(addr, addrIndex, addrArr, 1, numelem-1);
		System.arraycopy(len, addrIndex, lenArr, 1, numelem-1);
		if (!sha1Vector(1 + numelem, addrArr, lenArr, mac)) {
			return false;
		}
	
		for(int i=0; i<64; i++) {
			kpad[i] = 0;
		}
		System.arraycopy(key, 0, kpad, 0, keylen);
		for (int i = 0; i < 64; i++){
			kpad[i] ^= 0x5c;
		}
		addrArr[0] = kpad;
		lenArr[0] = 64;
		addrArr[1] = mac;
		lenArr[1] = SHA1_MAC_LEN;
		return sha1Vector(2, addrArr, lenArr, mac);
	}
	
	private boolean hmacMd5Vector(byte[] keyArr, int keylenValue, int numelem, byte[] addr, int len, byte[] mac)
	{
		byte[] kpad = new byte[64]; 
		byte[] tkarr = new byte[16];
		byte[][] addrArr = new byte[6][];
		int[] lenArr = new int [6];
		byte[] key = keyArr;
		int keylen = keylenValue;
		
		if (numelem > 5) {
			return false;
		}
	
        if (keylen > 64) {
			if (!md5VectorNonFipsAllow(1, key, keylen, tkarr)) {
				return false;
			}
			key = tkarr;
			keylen = 16;
        }
	
		for(int i=0; i<64; i++) {
			kpad[i] = 0;
		}
		System.arraycopy(key, 0, kpad, 0, keylen);
		for (int i = 0; i < 64; i++){
			kpad[i] ^= 0x36;
		}
		addrArr[0] = kpad;
		lenArr[0] = 64;
		
		for (int i = 0; i < numelem; i++) {
			addrArr[i + 1] = addr;
			lenArr[i + 1] = len;
		}
		
		if (!md5VectorNonFipsAllow(1 + numelem, addrArr, lenArr, mac)) {
			return false;
		}
		
		for(int i=0; i<64; i++) {
			kpad[i] = 0;
		}
		System.arraycopy(key, 0, kpad, 0, keylen);
		for (int i = 0; i < 64; i++) {
			kpad[i] ^= 0x5c;
		}
	
		addrArr[0] = kpad;
		lenArr[0] = 64;
		addrArr[1] = mac;
		lenArr[1] = MD5_MAC_LEN;
		return md5VectorNonFipsAllow(2, addrArr, lenArr, mac);
	}
	
	private boolean hmacSha1Vector(byte[] keyArr, int keylenValue, int numelem, byte[] addr, int len, byte[] mac)
	{
		byte[] kpad = new byte[64]; 
		byte[] tkarr = new byte[16];
		byte[][] addrArr = new byte[6][];
		int[] lenArr = new int [6];
	
		byte[] key = keyArr;
		int keylen = keylenValue;
		
		if (numelem > 5) {
			return false;
		}
	
	    if (keylen > 64) {
			if (!sha1Vector(1, key, keylen, tkarr)) {
				return false;
			}
			key = tkarr;
			keylen = 20;
	    }
	
		for(int i=0; i<64; i++) {
			kpad[i] = 0;
		}	
		System.arraycopy(key, 0, kpad, 0, keylen);
		for (int i = 0; i < 64; i++) {
			kpad[i] ^= 0x36;
		}
	
		addrArr[0] = kpad;
		lenArr[0] = 64;
		for (int i = 0; i < numelem; i++) {
			addrArr[i + 1] = addr;
			lenArr[i + 1] = len;
		}
		if (!sha1Vector(1 + numelem, addrArr, lenArr, mac)) {
			return false;
		}
	
		for(int i=0; i<64; i++) {
			kpad[i] = 0;
		}
		System.arraycopy(key, 0, kpad, 0, keylen);
		for (int i = 0; i < 64; i++) {
			kpad[i] ^= 0x5c;
		}
	
		addrArr[0] = kpad;
		lenArr[0] = 64;
		addrArr[1] = mac;
		lenArr[1] = SHA1_MAC_LEN;
		return sha1Vector(2, addrArr, lenArr, mac);
	}
	
	private boolean hmacMd5NonFipsAllow(byte[] key, int keylen, byte[] data, int datalen, byte[] mac)
	{
		return hmacMd5Vector(key, keylen, 1, data, datalen, mac);
	}
	
	private boolean hmacSha1(byte[] key, int keylen, byte[] data, int datalen, byte[] mac)
	{
		return hmacSha1Vector(key, keylen, 1, data, datalen, mac);
	}
	private int search(int nBegin, int nEnd, double shTS) {
		List<SSLKey> keylist = crypto.getSSLKeyList();
		if (nEnd - nBegin <= 5) {
			double best = Math.abs(keylist.get(nBegin).getTsvalue() - shTS);
			int bestI = nBegin;
			for (int i=nBegin+1; i<=nEnd; i++) {
				double tvalue = Math.abs(keylist.get(i).getTsvalue() - shTS);
				if (tvalue < best) {
					bestI = i;
					best = tvalue;
				}
			}
			return bestI;
		}

		double deltaBegin = keylist.get(nBegin).getTsvalue() - shTS;
		double deltaEnd   = keylist.get(nEnd).getTsvalue() - shTS;

		if (sgn(deltaBegin) == sgn(deltaEnd)) {
			if (Math.abs(deltaBegin) < Math.abs(deltaEnd)) {
				return nBegin; 
			} else {
				return nEnd;
			}
		}

		int nMid = (nBegin + nEnd) / 2;
		double deltaMid = keylist.get(nMid).getTsvalue() - shTS;
		if (sgn(deltaBegin) == sgn(deltaMid)) {
			return search(nMid, nEnd, shTS);
		} else {
			return search(nBegin, nMid, shTS);
		}
	}
	int sgn(double xvalue) {
		if (xvalue >= 0) {
			return 1; 
		} else {
			return -1;
		}
	}

}
