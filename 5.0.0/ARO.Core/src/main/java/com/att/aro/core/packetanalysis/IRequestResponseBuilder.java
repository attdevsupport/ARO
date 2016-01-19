package com.att.aro.core.packetanalysis;

import java.io.IOException;
import java.util.List;

import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.Session;

public interface IRequestResponseBuilder {
	List<HttpRequestResponseInfo> createRequestResponseInfo(Session session) throws IOException;
}
