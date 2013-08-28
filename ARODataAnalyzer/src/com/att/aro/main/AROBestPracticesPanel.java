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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.att.aro.bp.BestPracticeDisplay;
import com.att.aro.bp.BestPracticeDisplayFactory;
import com.att.aro.bp.BestPracticeDisplayGroup;
import com.att.aro.bp.BestPracticeExport;
import com.att.aro.commonui.AROUIManager;
import com.att.aro.commonui.ImagePanel;
import com.att.aro.images.Images;
import com.att.aro.model.TraceData;

/**
 * Represents the panel for the Best Practices tab.
 */
public class AROBestPracticesPanel extends JScrollPane implements Printable {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();

	private ImagePanel headerPanel;
	private JPanel aroBestPracticesMainPanel;
	private AROBpOverallResulsPanel bpOverallResultsPanel;
	private List<AROBpDetailedResultPanel> detailedResultPanels;

	/**
	 * Initializes a new instance of the AROBestPracticesPanel class, using the
	 * specified instance of the ApplicationResourceOptimizer as the parent
	 * window for the panel.
	 * 
	 * @param appParent
	 *            - The ApplicationResourceOptimizer instance.
	 */
	public AROBestPracticesPanel(ApplicationResourceOptimizer appParent) {

		aroBestPracticesMainPanel = new JPanel(new BorderLayout());
		aroBestPracticesMainPanel.setBackground(Color.white);
		this.setViewportView(aroBestPracticesMainPanel);
		this.getVerticalScrollBar().setUnitIncrement(10);

		aroBestPracticesMainPanel.add(getHeaderPanel(), BorderLayout.NORTH);

		// Build the main panel
		ImagePanel panel = new ImagePanel(null);
		panel.setLayout(new GridBagLayout());
		Insets insets = new Insets(10, 10, 10, 10);

		// Get the best practice display
		Collection<BestPracticeDisplayGroup> bpGroups = BestPracticeDisplayFactory
				.getInstance().getBestPracticeDisplay();
		
		// adding page 1 - overview page
		int page = 0;
		bpOverallResultsPanel = new AROBpOverallResulsPanel(appParent, bpGroups);
		panel.add(bpOverallResultsPanel, new GridBagConstraints(0, page++, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));

		// Add best practice detail pages for each group
		this.detailedResultPanels = new ArrayList<AROBpDetailedResultPanel>(bpGroups.size());
		for (BestPracticeDisplayGroup bpGroup : bpGroups) {
			
			// Create the detailed panel for each best practice in the group
			Collection<BestPracticeDisplay> bps = bpGroup.getBestPractices();
			List<DetailedResultRowPanel> list = new ArrayList<DetailedResultRowPanel>(bps.size());
			for (BestPracticeDisplay bp : bps) {
				list.add(new DetailedResultRowPanel(appParent, bp));
			}

			// Create the group page and add it to the panel
			final AROBpDetailedResultPanel detailedResultPanel = new AROBpDetailedResultPanel(
					bpGroup.getHeaderName(), bpGroup.getDescription(), list);
			panel.add(detailedResultPanel, new GridBagConstraints(0, page++, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
			this.detailedResultPanels.add(detailedResultPanel);
			
			// Create listener for refer section link
			ActionListener listener = new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					aroBestPracticesMainPanel.scrollRectToVisible(detailedResultPanel
							.getBounds());
				}

			};
			for (AROBpOverallResulsPanel.BPResultRowPanel resultPanel : bpOverallResultsPanel.getResultRowPanels(bpGroup)) {
				resultPanel.getReferSectionLabel().addActionListener(listener);
			}
		}
		
		// Add main pages to main panel
		aroBestPracticesMainPanel.add(panel, BorderLayout.CENTER);
	}

	/**
	 * Refreshes the content of the Best Practices panel with the specified
	 * trace data.
	 * 
	 * @param analysisData
	 *            - The Analysis object containing the trace data.
	 */
	public void refresh(TraceData.Analysis analysisData) {
		bpOverallResultsPanel.refresh(analysisData);
		for (AROBpDetailedResultPanel panel : detailedResultPanels) {
			panel.refresh(analysisData);
		}
	}

	/**
	 * Prints the information in the AROBestPracticesPanel using the specified
	 * graphics object, page format, and page index. This method implements the
	 * print command of the java.awt.print.Printable interface.
	 * 
	 * @see java.awt.print.Printable#print(java.awt.Graphics,
	 *      java.awt.print.PageFormat, int)
	 */
	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {

		int pages;
		int pageCount = 0;

		// Print the overall results and then detail pages
		JComponent c = bpOverallResultsPanel;
		Iterator<AROBpDetailedResultPanel> iter = detailedResultPanels.iterator();
		do {
			AROPrintablePanel p = new AROPrintablePanel(c);
			pages = p.getPageCount(pageFormat);

			if (pageIndex < pageCount + pages) {
				return p.print(graphics, pageFormat, pageIndex - pageCount);
			}
			pageCount += pages;
			
			c = iter.hasNext() ? iter.next() : null;
		} while (c != null);

		return NO_SUCH_PAGE;
	}

	/**
	 * Returns the blue header panel with the ATT logo.
	 */
	private ImagePanel getHeaderPanel() {
		if (headerPanel == null) {
			headerPanel = new ImagePanel(Images.BLUE_HEADER.getImage());
			headerPanel.setLayout(new BorderLayout(50, 50));
			headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			JLabel l = new JLabel(Images.HEADER_ICON.getIcon(), SwingConstants.CENTER);
			l.setPreferredSize(new Dimension(80, 80));
			headerPanel.add(l, BorderLayout.WEST);

			JLabel bpHeaderLabel = new JLabel(rb.getString("bestPractices.header.result"));
			bpHeaderLabel.setFont(UIManager.getFont(AROUIManager.TITLE_FONT_KEY));
			bpHeaderLabel.setForeground(Color.WHITE);
			headerPanel.add(bpHeaderLabel, BorderLayout.CENTER);
		}
		return headerPanel;
	}

	/**
	 * Returns the AROBpOverallResulsPanel object
	 * 
	 * @return thoe overall results panel
	 */
	public AROBpOverallResulsPanel getAROBpOverallResulsPanel() {
		return this.bpOverallResultsPanel;
	}
	
	/**
	 * Writes best best practice result data in CSV file.
	 */
	public void addBestPracticeContent(FileWriter writer, TraceData.Analysis analysisData) throws IOException {
		String lineSep = BestPracticeExport.lINE;
		String cellSep = BestPracticeExport.COMMA;
		String pass = rb.getString("bestPractice.tooltip.pass");
		String fail = rb.getString("bestPractice.tooltip.fail");
		String warning = rb.getString("bestPractice.tooltip.warning");
		String manual = rb.getString("bestPractice.tooltip.manual");

		// Write the section header
		writer.append(rb.getString("exportall.csvHeader.bestpractice"));
		writer.append(lineSep);

		// Iterate through best practices
		for (AROBpOverallResulsPanel.BPResultRowPanel resultPanel : bpOverallResultsPanel.getResultRowPanels(null)) {
			
			BestPracticeDisplay bp = resultPanel.getBp();
			
			// Write title and status
			BestPracticeExport.writeValue(writer, bp.getDetailTitle());
			writer.append(cellSep);
			if (bp.isSelfTest()) {
				BestPracticeExport.writeValue(writer, manual);
			} else {
				if(((bp.getOverviewTitle()).equals(rb.getString("caching.usingCache.title")))
						|| ((bp.getOverviewTitle()).equals(rb.getString("caching.cacheControl.title")))
						|| ((bp.getOverviewTitle()).equals(rb.getString("connections.offloadingToWifi.title")))
						|| ((bp.getOverviewTitle()).equals(rb.getString("html.httpUsage.title")))
						|| ((bp.getOverviewTitle()).equals(rb.getString("other.accessingPeripherals.title")))
						) {
					BestPracticeExport.writeValue(writer, bp.isPass(analysisData) ? pass : warning);
				} else {
					BestPracticeExport.writeValue(writer, bp.isPass(analysisData) ? pass : fail);
				}
			}
			writer.write(lineSep);
			
			// Write custom info for best practice
			List<BestPracticeExport> list = bp.getExportData(analysisData);
			if (list != null && list.size() > 0) {
				for (BestPracticeExport bpe : list) {
					bpe.write(writer);
				}
			} else {
				writer.write(lineSep);
			}
		}
	}
	
	/**
	 * Returns the aroBestPracticesMainPanel object
	 */
	JPanel getAroBestPracticesMainPanel() {
		return aroBestPracticesMainPanel;
	}

	/**
	 * Returns the detailed results panels
	 * @return the detailedResultPanels
	 */
	List<AROBpDetailedResultPanel> getDetailedResultPanels() {
		return detailedResultPanels;
	}
	
}
