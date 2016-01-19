package com.att.aro.ui.view.diagnostictab;

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
