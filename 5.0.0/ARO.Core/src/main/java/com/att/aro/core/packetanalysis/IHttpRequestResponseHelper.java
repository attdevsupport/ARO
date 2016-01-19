package com.att.aro.core.packetanalysis;

import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.Session;

public interface IHttpRequestResponseHelper {
	boolean isSameContent(HttpRequestResponseInfo left, HttpRequestResponseInfo right, Session sessionLeft, Session sessionRight);
	long getActualByteCount(HttpRequestResponseInfo item, Session session);
	String getContentString(HttpRequestResponseInfo req, Session session) throws Exception;
	byte[] getContent(HttpRequestResponseInfo req, Session session) throws Exception;
	boolean isJavaScript(String contentType);
	boolean isCss(String contentType);
	boolean isHtml(String contentType);
	boolean isJSON(String contentType);
}
