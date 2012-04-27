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
