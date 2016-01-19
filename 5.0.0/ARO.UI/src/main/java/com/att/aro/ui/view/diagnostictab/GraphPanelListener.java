package com.att.aro.ui.view.diagnostictab;

/**
 * Exposes a method that listens for click events on a GraphPanel object.
 */
public interface GraphPanelListener {

	/**
	 * This method is invoked when a GraphPanel object is clicked. The
	 * coordinates of the click on the graph plot are used to set a new selected
	 * time for the graph.
	 * 
	 * @param timeStamp
	 *            - The coordinatespoint on the graph plot where the mouse was
	 *            clicked.
	 */
	public void graphPanelClicked(double timeStamp);

}
