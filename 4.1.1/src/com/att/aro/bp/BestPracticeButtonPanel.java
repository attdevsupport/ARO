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
package com.att.aro.bp;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Defines the button features or functionality for a best practice. Developers
 * may use in new ARO best practices by creating classes that implement this
 * interface.
 */
public class BestPracticeButtonPanel extends JPanel {

	private static final long serialVersionUID = 8670817677769035815L;
	private JScrollPane scrollPane;
	private JButton viewBtn = null;
	private boolean isExpanded = false;

	public boolean isExpanded() {
		return isExpanded;
	}

	public JButton getViewBtn() {
		return viewBtn;
	}

	public void setExpanded(boolean isExpanded) {
		this.isExpanded = isExpanded;
	}

	int noOfRecords = 0;

	private static final int ROW_HEIGHT = 20;
	private static final int NO_OF_ROWS = 6;
	private static final int SCROLL_PANE_HEIGHT = BestPracticeButtonPanel.NO_OF_ROWS
			* BestPracticeButtonPanel.ROW_HEIGHT;
	private static final int SCROLL_PANE_LENGHT = 300;

	public BestPracticeButtonPanel() {

		super(new GridBagLayout());
		this.viewBtn = new JButton("+");
		this.viewBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (isExpanded) {
					isExpanded = false;
					viewBtn.setText("+");
					scrollPane.setPreferredSize(new Dimension(
							BestPracticeButtonPanel.SCROLL_PANE_LENGHT,
							BestPracticeButtonPanel.SCROLL_PANE_HEIGHT));
				} else {
					isExpanded = true;
					viewBtn.setText("-");
					scrollPane.setPreferredSize(new Dimension(
							BestPracticeButtonPanel.SCROLL_PANE_LENGHT,
							BestPracticeButtonPanel.ROW_HEIGHT
									* (noOfRecords + 1)));
				}
				scrollPane.revalidate();
			}
		});

		JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
		panel.add(viewBtn);

		this.add(panel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(
						5, 5, 5, 5), 0, 0));
	}

	public int getNoOfRecords() {
		return noOfRecords;
	}

	public void setNoOfRecords(int noOfRecords) {
		this.noOfRecords = noOfRecords;

	}

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	public void setScrollPane(JScrollPane scrollPane) {
		this.scrollPane = scrollPane;
	}

}
