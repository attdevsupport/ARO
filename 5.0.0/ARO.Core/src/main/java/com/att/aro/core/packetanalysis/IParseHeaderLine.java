package com.att.aro.core.packetanalysis;

import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;

public interface IParseHeaderLine {
	void parseHeaderLine(String headerLine, HttpRequestResponseInfo rrInfo);
}
