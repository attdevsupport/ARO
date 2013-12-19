package com.att.aro.libimobiledevice;

public interface Screencapture {

	public String initService();

	public byte[] getScreenImage();

	public void stopCapture();

}