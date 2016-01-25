/*
 *  Copyright 2012 AT&T
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
package com.att.aro.commonui;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JLabel;

/**
 * Represents a Hyperlink label. This class implements a customized JLabel
 * component to provide a Hyperlink label.
 */
public class HyperlinkLabel extends JLabel {
	private static final long serialVersionUID = 1L;

	/**
	 * Initializes a new instance of the HyperlinkLabel class.
	 */
	public HyperlinkLabel() {
		super();
		init();
	}

	/**
	 * Initializes a new instance of the HyperlinkLabel class using the
	 * specified text.
	 * 
	 * @param text
	 *            A String that is the text to be hyper linked.
	 */
	public HyperlinkLabel(String text) {
		super(text);
		init();
	}

	/**
	 * Initializes a new instance of the HyperlinkLabel class using the 
	 * specified text and horizontal alignment.
	 * @param text - A String that is the text to be hyper linked.
	 * @param horizontalAlignment - Coordinate for horizontal alignment.
	 */
	public HyperlinkLabel(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
		init();
	}

	/**
	 * Initializes a new instance of the HyperlinkLabel class using the 
	 * specified icon.
	 * @param icon - Icon image.
	 */
	public HyperlinkLabel(Icon icon) {
		super(icon);
		init();
	}

	/**
	 * Method to initialize the mouse event for Hyper linked text.
	 */
	private void init() {
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				for (ActionListener al : getActionListeners()) {
					al.actionPerformed(new ActionEvent(HyperlinkLabel.this, me
							.getID(), getName()));
				}
			}

			public void mouseEntered(MouseEvent me) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			public void mouseExited(MouseEvent me) {
				setCursor(Cursor.getDefaultCursor());
			}
		});
	}

	/**
	 * Adds an ActionListener to the hyperlink.
	 * 
	 * @param l
	 *            The ActionListener to be added.
	 */
	public void addActionListener(ActionListener l) {
		listenerList.add(ActionListener.class, l);
	}

	/**
	 * Removes an ActionListener from the hyperlink.
	 * 
	 * @param l
	 *            The ActionListener to be removed.
	 */
	public void removeActionListener(ActionListener l) {
		listenerList.remove(ActionListener.class, l);
	}

	/**
	 * Returns an array of all the ActionListeners added to this HyperLinkLable
	 * using the addActionListener method.
	 * 
	 * @return An array of all the ActionListeners that have been added, or an
	 *         empty array if no listeners have been added.
	 */
	public ActionListener[] getActionListeners() {
		return (ActionListener[]) (listenerList
				.getListeners(ActionListener.class));
	}

}
