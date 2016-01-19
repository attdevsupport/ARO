package com.att.aro.core.securedpacketreader;

public interface IReceiveSSLKey {
	void handleSSLKey(double tsvalue, int preMasterLen, byte[] preMaster, byte[] master);
}
