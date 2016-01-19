package com.att.aro.core.packetanalysis.pojo;

public enum TextFileCompression {

	GZIP(HttpRequestResponseInfo.CONTENT_ENCODING_GZIP),
	COMPRESS(HttpRequestResponseInfo.CONTENT_ENCODING_COMPRESS),
	DEFLATE(HttpRequestResponseInfo.CONTENT_ENCODING_DEFLATE),
	NONE(HttpRequestResponseInfo.CONTENT_ENCODING_NONE),
	NOT_APPLICABLE(HttpRequestResponseInfo.CONTENT_ENCODING_NA);

	private String type;

	TextFileCompression(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}
}