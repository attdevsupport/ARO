package com.att.aro.core.mobiledevice.pojo;

import java.nio.charset.Charset;

import com.android.ddmlib.IShellOutputReceiver;

public 	class RootCheckOutputReceiver implements IShellOutputReceiver {

	String data = "";
	boolean isCancelledFlag = false;

	@Override
	public void addOutput(byte[] buff, int offset, int length) {
		Charset charset = Charset.forName("ISO-8859-1");
		data = new String(buff, offset, length, charset);
		isCancelledFlag = true;
	}

	public boolean isRootId() {
		return data.contains("uid=0(root) gid=0(root)");
	}

	@Override
	public void flush() {
		isCancelledFlag = true;
	}

	@Override
	public boolean isCancelled() {
		return isCancelledFlag;
	}
}

