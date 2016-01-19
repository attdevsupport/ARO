package com.att.aro.core.packetanalysis.pojo;

/**
 * The HttpRequestResponseInfo.Direction Enumeration specifies constant
 * values that describe the direction of an HTTP request/response. The
 * direction indicates whether an HttpRequestResponseInfo object contains a
 * request (up link) or a response (downlink). This enumeration is part of
 * the HttpRequestResponseInfo class.
 */
public enum HttpDirection {
	/**
	 * A Request traveling in the up link direction.
	 */
	REQUEST,
	/**
	 * A Response traveling in the down link direction.
	 */
	RESPONSE;
}
