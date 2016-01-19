package com.att.aro.datacollector.ioscollector;

public interface IScreenCapture {

	public String initService();

	public byte[] getScreenImage();

	public void stopCapture();

}