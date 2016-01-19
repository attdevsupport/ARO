package com.att.aro.core.bestpractice.pojo;

import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;

public class FileOrderEntry extends HttpEntry {
	private int contentLength;
	public FileOrderEntry(HttpRequestResponseInfo hrri,
			HttpRequestResponseInfo lastRequestObj, String domainName) {
		super(hrri, lastRequestObj, domainName);
		this.contentLength = hrri.getContentLength();
	}
	/**
	 * Returns size of the file.
	 * 
	 * @return file size
	 */
	public Object getSize() {
		return contentLength;
	}
}
