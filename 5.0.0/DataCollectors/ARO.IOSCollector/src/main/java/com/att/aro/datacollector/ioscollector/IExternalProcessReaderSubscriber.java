package com.att.aro.datacollector.ioscollector;

public interface IExternalProcessReaderSubscriber {
	public void newMessage(String message);
	public void willExit();
}
