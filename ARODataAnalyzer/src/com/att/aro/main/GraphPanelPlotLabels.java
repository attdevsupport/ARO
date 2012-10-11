/*
 * Copyright 2012 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.att.aro.main;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.jfree.chart.plot.XYPlot;

/**
 * Creates the plot labels in the graph.
 */
public class GraphPanelPlotLabels {

	private JLabel label;
	private XYPlot plot;
	private int weight;

	/**
	 * Initializes a new instance of the GraphPanelPlotLabels class using the
	 * specified label, chart plot object, and weight.
	 * 
	 * @param label
	 *            The label for the plot on the graph, such as "GPS" , or
	 *            "Radio".
	 * 
	 * @param plot
	 *            The plot object to be displayed in the Diagnostic chart.
	 * 
	 * @param weight
	 *            An int that is the weight of the plot.
	 */
	public GraphPanelPlotLabels(String label, XYPlot plot, int weight) {
		this.label = new JLabel(label);
		this.label.setHorizontalAlignment(SwingConstants.CENTER);
		this.label.setVerticalAlignment(SwingConstants.CENTER);
		this.plot = plot;
		this.weight = weight;
	}

	/**
	 * Returns the label for the plot associated with this GraphPanelPlotLabels
	 * object.
	 * 
	 * @return A JLabel object that is the label for the plot.
	 */
	public JLabel getLabel() {
		return label;
	}

	/**
	 * Returns the plot associated with this GraphPanelPlotLabels object.
	 * 
	 * @return An XYPlot object that is plot for the label.
	 */
	public XYPlot getPlot() {
		return plot;
	}

	/**
	 * Returns the weight associated with this GraphPanelPlotLabels object.
	 * 
	 * @return An int that is the weight of the plot.
	 */
	public int getWeight() {
		return weight;
	}

}
