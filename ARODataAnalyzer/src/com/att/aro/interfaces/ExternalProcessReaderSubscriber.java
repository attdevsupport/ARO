package com.att.aro.interfaces;

public interface ExternalProcessReaderSubscriber {
	public void newMessage(String message);
	public void willExit();
}
