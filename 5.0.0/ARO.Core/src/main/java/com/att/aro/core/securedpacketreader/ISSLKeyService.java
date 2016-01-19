package com.att.aro.core.securedpacketreader;

public interface ISSLKeyService {
	int getMasterFromSSLLog(double serverHelloTS, byte[] master, byte[] clientRandom, byte[] serverRandom);
	int tlsprf(byte[] secret, int secretlen, String label, byte[] seed, int seedlen, byte[] out, int outlen);
}
