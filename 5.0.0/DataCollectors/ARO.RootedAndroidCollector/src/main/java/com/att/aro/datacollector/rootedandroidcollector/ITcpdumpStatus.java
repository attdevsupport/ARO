package com.att.aro.datacollector.rootedandroidcollector;

public interface ITcpdumpStatus {
	void onTcpdumFailToStart();
	void onTcpdumpExit();
}
