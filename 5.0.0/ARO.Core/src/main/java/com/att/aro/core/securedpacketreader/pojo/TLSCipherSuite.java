package com.att.aro.core.securedpacketreader.pojo;

import com.att.aro.core.securedpacketreader.pojo.CryptoEnum.TLSCipher;
import com.att.aro.core.securedpacketreader.pojo.CryptoEnum.TLSHash;
import com.att.aro.core.securedpacketreader.pojo.CryptoEnum.TLSKeyExchange;

public class TLSCipherSuite {
	int suite;
	TLSKeyExchange keyexchange;
	TLSCipher cipher;
	TLSHash hash;
	public TLSCipherSuite(int suite, TLSKeyExchange keyexchange, TLSCipher cipher, TLSHash hash){
		this.suite = suite;
		this.keyexchange = keyexchange;
		this.cipher = cipher;
		this.hash = hash;
	}
	public int getSuite() {
		return suite;
	}
	public void setSuite(int suite) {
		this.suite = suite;
	}
	public TLSKeyExchange getKeyexchange() {
		return keyexchange;
	}
	public void setKeyexchange(TLSKeyExchange keyexchange) {
		this.keyexchange = keyexchange;
	}
	public TLSCipher getCipher() {
		return cipher;
	}
	public void setCipher(TLSCipher cipher) {
		this.cipher = cipher;
	}
	public TLSHash getHash() {
		return hash;
	}
	public void setHash(TLSHash hash) {
		this.hash = hash;
	}
	
}
