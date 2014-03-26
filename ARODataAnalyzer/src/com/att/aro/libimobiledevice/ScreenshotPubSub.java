package com.att.aro.libimobiledevice;

public interface ScreenshotPubSub {
	public void newMessage(String message);
	public void willExit();
}
