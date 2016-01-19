package com.att.aro.core.securedpacketreader;

import com.att.aro.core.securedpacketreader.pojo.CryptoEnum.TLSCipher;
import com.att.aro.core.securedpacketreader.pojo.TLSCipherData;
import com.att.aro.core.securedpacketreader.pojo.TLSCipherSuite;

public interface ICipherDataService {
	TLSCipherData getTLSCipherData(TLSCipher cipher);
	TLSCipherSuite getTLSCipherSuite(int suite);
}
