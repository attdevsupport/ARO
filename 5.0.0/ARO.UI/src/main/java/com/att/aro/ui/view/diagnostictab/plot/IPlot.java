package com.att.aro.ui.view.diagnostictab.plot;

import org.jfree.chart.plot.XYPlot;

import com.att.aro.core.pojo.AROTraceData;

public interface IPlot {
	void populate(XYPlot plot, AROTraceData analysis); 
}
