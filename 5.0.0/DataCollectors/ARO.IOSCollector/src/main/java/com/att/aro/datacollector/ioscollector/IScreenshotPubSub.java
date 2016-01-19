package com.att.aro.datacollector.ioscollector;

public interface IScreenshotPubSub {
	public void newMessage(String message);
	public void willExit();
}
