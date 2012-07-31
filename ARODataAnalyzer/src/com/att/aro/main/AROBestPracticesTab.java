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

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.JPanel;

import com.att.aro.model.TraceData;

/**
 * Represents the Best Practices tab.
 */
public class AROBestPracticesTab extends JPanel implements Printable {
	private static final long serialVersionUID = 1L;

	private ApplicationResourceOptimizer parent;
	private AROBestPracticesPanel bpScrollPane;

	/**
	 * Initializes a new instance of the AROBestPracticesTab class, using the specified 
	 * instance of the ApplicationResourceOptimizer as the parent window for the tab.
	 * 
	 * @param parent The  ApplicationResourceOptimizer instance.
	 */
	public AROBestPracticesTab(ApplicationResourceOptimizer parent) {
		this.parent = parent;
		this.setLayout(new BorderLayout());
		this.add(getBestPracticesPanel(), BorderLayout.CENTER);
	}

	/**
	 * Prints the information in the AROBestPracticesTab using the specified graphics object, 
	 * page format, and page index. This method implements the print command of the 
	 * java.awt.print.Printable interface.
	 * 
	 * @see java.awt.print.Printable#print(java.awt.Graphics,
	 *      java.awt.print.PageFormat, int)
	 */
	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		return getBestPracticesPanel().print(graphics, pageFormat, pageIndex);
	}

	/**
	 * Refreshes the best practices tab once the trace file is loaded.
	 * 
	 * @param analysisData
	 *            The trace analysis data.
	 */
	protected void refresh(TraceData.Analysis analysisData) {
		getBestPracticesPanel().refresh(analysisData);
	}

	/**
	 * Creates the Best Practices Scenario UI
	 * 
	 * @return JScrollPane containing the Best Practices
	 */
	private AROBestPracticesPanel getBestPracticesPanel() {
		if (bpScrollPane == null) {
			bpScrollPane = new AROBestPracticesPanel(parent);
		}
		return bpScrollPane;
	}

}
