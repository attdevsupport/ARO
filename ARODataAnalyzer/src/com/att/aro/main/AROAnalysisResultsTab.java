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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.att.aro.commonui.AROUIManager;
import com.att.aro.commonui.ImagePanel;
import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.commonui.RoundedBorder;
import com.att.aro.images.Images;
import com.att.aro.model.Profile;
import com.att.aro.model.Profile3G;
import com.att.aro.model.ProfileLTE;
import com.att.aro.model.ProfileWiFi;
import com.att.aro.model.TraceData;
import com.att.aro.model.UserPreferences;

/**
 * Represents the Statistics tab screen.
 */
public class AROAnalysisResultsTab extends JScrollPane implements Printable {
	private static final long serialVersionUID = 1L;

	private JPanel mainPanel;
	private JPanel aroBestPracticesMainPanel;
	private BasicStatisticsPanel basicStatisticsPanel;
	private ApplicationScorePanel applicationScorePanel;
	private BurstAnalysisPanel burstAnalysisPanel;
	private EndPointSummaryPanel endPointSummaryPanel;
	private RRCStatisticsPanel rrcStatisticsPanel;
	private EnergyModelStatisticsPanel energyModelStatisticsPanel;
	private HttpCacheStatisticsPanel cacheStatisticsPanel;
	private JPanel headerPanel;
	private DateTraceAppDetailPanel dateTraceAppDetailPanel;
	private JLabel exportBtn;
	private ApplicationResourceOptimizer parent;
	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();

	TraceData.Analysis analysisData = null;

	String lineSep = System.getProperty(rb.getString("statics.csvLine.seperator"));
	/**
	 * Logger
	 */
	private static Logger logger = Logger.getLogger(AROAnalysisResultsTab.class.getName());

	/**
	 * Initializes a new instance of the AROAnalysisResultsTab class, using the
	 * specified instance of the ApplicationResourceOptimizer as the parent
	 * window for the tab.
	 * 
	 * @param appParent
	 *            - The ApplicationResourceOptimizer instance.
	 */
	public AROAnalysisResultsTab(ApplicationResourceOptimizer appParent) {
		this.parent = appParent;
		aroBestPracticesMainPanel = new JPanel(new BorderLayout());
		aroBestPracticesMainPanel.setBackground(Color.white);
		this.setViewportView(aroBestPracticesMainPanel);
		this.getVerticalScrollBar().setUnitIncrement(10);

		aroBestPracticesMainPanel.add(getHeaderPanel(), BorderLayout.NORTH);

		ImagePanel panel = new ImagePanel(Images.BACKGROUND.getImage());
		panel.setLayout(new GridBagLayout());
		Insets insets = new Insets(10, 10, 10, 10);

		panel.add(getMainPanel(), new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));

		aroBestPracticesMainPanel.add(panel, BorderLayout.CENTER);
	}

	/**
	 * Refreshes the content of the Panels in the Analysis Results tab with the
	 * specified trace data.
	 * 
	 * @param analysisData
	 *            - The Analysis object containing the trace data.
	 */
	public void refresh(TraceData.Analysis analysisData) {
		this.analysisData = analysisData;
		dateTraceAppDetailPanel.refresh(analysisData);
		getBasicStatisticsPanel().refresh(analysisData);
		getBurstAnalysisPanel().refresh(analysisData);
		getApplicationScorePanel().refresh(analysisData);
		getEndPointSummaryPanel().refresh(analysisData);
		getRRCStatisticsPanel().refresh(analysisData);
		getCacheStatisticsPanel().refresh(analysisData);
		if (analysisData != null) {
			final Profile profile = analysisData.getProfile();
			if (profile instanceof Profile3G) {
				getEnergyModelStatistics3GPanel().refresh(analysisData);
			} else if (profile instanceof ProfileLTE) {
				getEnergyModelStatisticsLTEPanel().refresh(analysisData);
			} else if (profile instanceof ProfileWiFi) {
				getWiFiEnergyModelStatisticsPanel().refresh(analysisData);
			}
		} else if (energyModelStatisticsPanel != null) {

			// Make sure we clear the panel
			energyModelStatisticsPanel.refresh(null);
		}
		exportBtn.setEnabled(analysisData != null);
	}

	/**
	 * Prints the analysis results using the specified graphics object, page
	 * format, and page index. This method implements the print command of the
	 * java.awt.print.Printable interface.
	 * 
	 * @see java.awt.print.Printable#print(java.awt.Graphics,
	 *      java.awt.print.PageFormat, int)
	 */
	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		return new AROPrintablePanel(getMainPanel()).print(graphics, pageFormat, pageIndex);
	}

	/**
	 * Scrolls the display to the Cache Statistics panel of the Analysis Results
	 * tab.
	 */
	public void scrollToCacheStatistics() {
		getMainPanel().scrollRectToVisible(getCacheStatisticsPanel().getBounds());
	}

	/**
	 * Adds the various Panels for the Statistics tab.
	 * 
	 * @return the mainPanel The JPanel containing the entire screen.
	 */
	private JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel(new GridBagLayout());
			mainPanel.setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
			Insets insets = new Insets(10, 10, 10, 10);
			Insets insetsWithOutHeader = new Insets(10, 20, 10, 10);
			mainPanel.setOpaque(false);
			mainPanel.setBorder(new RoundedBorder(new Insets(10, 10, 10, 10), Color.WHITE));
			dateTraceAppDetailPanel = new DateTraceAppDetailPanel();
			mainPanel.add(dateTraceAppDetailPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
			mainPanel.add(new ImagePanel(Images.DIVIDER.getImage(), true, Color.WHITE),
					new GridBagConstraints(0, 1, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.HORIZONTAL, insets, 0, 0));
			mainPanel.add(getBasicStatisticsPanel(), new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insetsWithOutHeader,
					0, 0));
			mainPanel.add(new ImagePanel(Images.DIVIDER.getImage(), true, Color.WHITE),
					new GridBagConstraints(0, 3, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.HORIZONTAL, insets, 0, 0));
			mainPanel.add(getApplicationScorePanel(), new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insetsWithOutHeader,
					0, 0));
			mainPanel.add(new ImagePanel(Images.DIVIDER.getImage(), true, Color.WHITE),
					new GridBagConstraints(0, 5, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.HORIZONTAL, insets, 0, 0));
			mainPanel.add(getEndPointSummaryPanel(), new GridBagConstraints(0, 6, 3, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 5, 10),
					0, 0));
			mainPanel.add(new ImagePanel(Images.DIVIDER.getImage(), true, Color.WHITE),
					new GridBagConstraints(0, 7, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.HORIZONTAL, insets, 0, 0));
			mainPanel.add(getRRCStatisticsPanel(), new GridBagConstraints(0, 8, 1, 1, 0.6, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insetsWithOutHeader, 0,
					0));
			mainPanel.add(new ImagePanel(Images.DIVIDER.getImage(), true, Color.WHITE),
					new GridBagConstraints(0, 9, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.HORIZONTAL, insets, 0, 0));
			mainPanel.add(getBurstAnalysisPanel(), new GridBagConstraints(0, 10, 1, 1, 0.6, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insetsWithOutHeader, 0,
					0));
			mainPanel.add(new ImagePanel(Images.DIVIDER.getImage(), true, Color.WHITE),
					new GridBagConstraints(0, 11, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.HORIZONTAL, insets, 0, 0));
			mainPanel.add(getCacheStatisticsPanel(), new GridBagConstraints(0, 12, 2, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insetsWithOutHeader, 0,
					0));
			mainPanel.add(new ImagePanel(Images.DIVIDER.getImage(), true, Color.WHITE),
					new GridBagConstraints(0, 13, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.HORIZONTAL, insets, 0, 0));
			mainPanel.add(getEnergyModelStatistics3GPanel(), new GridBagConstraints(0, 14, 1, 3,
					0.4, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
					insetsWithOutHeader, 0, 0));
		}
		return mainPanel;
	}

	/**
	 * Initializes basic statistics panel
	 */
	private BasicStatisticsPanel getBasicStatisticsPanel() {
		if (basicStatisticsPanel == null) {
			basicStatisticsPanel = new BasicStatisticsPanel();
		}
		return basicStatisticsPanel;
	}

	/**
	 * Initializes application score statistics panel
	 */
	private ApplicationScorePanel getApplicationScorePanel() {
		if (applicationScorePanel == null) {
			applicationScorePanel = new ApplicationScorePanel();
		}
		return applicationScorePanel;
	}

	/**
	 * Initializes burst statistics panel
	 */
	private BurstAnalysisPanel getBurstAnalysisPanel() {
		if (burstAnalysisPanel == null) {
			burstAnalysisPanel = new BurstAnalysisPanel();
		}
		return burstAnalysisPanel;
	}

	/**
	 * Initializes end point summary statistics panel
	 */
	private EndPointSummaryPanel getEndPointSummaryPanel() {
		if (endPointSummaryPanel == null) {
			endPointSummaryPanel = new EndPointSummaryPanel();
		}
		return endPointSummaryPanel;
	}

	/**
	 * Initializes basic TCP statistics panel
	 */
	private HttpCacheStatisticsPanel getCacheStatisticsPanel() {
		if (cacheStatisticsPanel == null) {
			cacheStatisticsPanel = new HttpCacheStatisticsPanel();
		}
		return cacheStatisticsPanel;
	}

	/**
	 * Initializes RRC simulation statistics panel
	 */
	private RRCStatisticsPanel getRRCStatisticsPanel() {
		if (rrcStatisticsPanel == null) {
			rrcStatisticsPanel = new RRCStatisticsPanel();
		}
		return rrcStatisticsPanel;
	}

	/**
	 * Initializes and returns the 3G Energy model statistics panel
	 */
	private EnergyModelStatisticsPanel getEnergyModelStatistics3GPanel() {
		if (energyModelStatisticsPanel == null) {
			energyModelStatisticsPanel = new EnergyModelStatistics3GPanel();
		} else {
			if (energyModelStatisticsPanel instanceof EnergyModelStatisticsLTEPanel
					|| energyModelStatisticsPanel instanceof EnergyModelStatisticsWiFiPanel) {
				mainPanel.remove(energyModelStatisticsPanel);
				energyModelStatisticsPanel = null;
				energyModelStatisticsPanel = new EnergyModelStatistics3GPanel();
				mainPanel.add(energyModelStatisticsPanel, new GridBagConstraints(0, 17, 1, 3, 0.4,
						0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5,
								20, 5, 5), 0, 0));
			}
		}
		return energyModelStatisticsPanel;
	}

	/**
	 * Initializes and returns the LTE Energy model statistics panel
	 */
	private EnergyModelStatisticsPanel getEnergyModelStatisticsLTEPanel() {
		if (energyModelStatisticsPanel != null
				&& (energyModelStatisticsPanel instanceof EnergyModelStatistics3GPanel || energyModelStatisticsPanel instanceof EnergyModelStatisticsWiFiPanel)) {
			mainPanel.remove(energyModelStatisticsPanel);
			energyModelStatisticsPanel = null;
			energyModelStatisticsPanel = new EnergyModelStatisticsLTEPanel();
			mainPanel.add(energyModelStatisticsPanel, new GridBagConstraints(0, 17, 1, 3, 0.4, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
					new Insets(5, 20, 5, 5), 0, 0));
		}
		return energyModelStatisticsPanel;
	}

	/**
	 * Initializes and returns the LTE Energy model statistics panel
	 */
	private EnergyModelStatisticsPanel getWiFiEnergyModelStatisticsPanel() {
		if (energyModelStatisticsPanel != null
				&& (energyModelStatisticsPanel instanceof EnergyModelStatistics3GPanel || energyModelStatisticsPanel instanceof EnergyModelStatisticsLTEPanel)) {
			mainPanel.remove(energyModelStatisticsPanel);
			energyModelStatisticsPanel = null;
			energyModelStatisticsPanel = new EnergyModelStatisticsWiFiPanel();
			mainPanel.add(energyModelStatisticsPanel, new GridBagConstraints(0, 17, 1, 3, 0.4, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
					new Insets(5, 20, 5, 5), 0, 0));
		}
		return energyModelStatisticsPanel;
	}

	/**
	 * Creates the blue header with logo.
	 */
	private JPanel getHeaderPanel() {
		if (headerPanel == null) {
			JPanel imagePanel = new ImagePanel(Images.BLUE_HEADER.getImage());
			imagePanel.setLayout(new BorderLayout(50, 50));
			imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			imagePanel.setMaximumSize(new Dimension(1375, 114));

			JLabel l = new JLabel(Images.HEADER_ICON.getIcon(), SwingConstants.CENTER);
			l.setPreferredSize(new Dimension(80, 80));
			imagePanel.add(l, BorderLayout.WEST);

			JLabel bpHeaderLabel = new JLabel(rb.getString("statistics.title"));
			bpHeaderLabel.setFont(UIManager.getFont(AROUIManager.TITLE_FONT_KEY));
			bpHeaderLabel.setForeground(Color.WHITE);
			imagePanel.add(bpHeaderLabel, BorderLayout.CENTER);

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new BorderLayout());
			buttonPanel.setOpaque(false);
			buttonPanel.add(getExportBtn(), BorderLayout.CENTER);
			imagePanel.add(buttonPanel, BorderLayout.EAST);
			
			headerPanel = new JPanel();
			headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
			headerPanel.add(imagePanel);
		}
		return headerPanel;
	}

	/**
	 * Creates a export button and handles the export functionality.
	 */
	private JLabel getExportBtn() {
		if (exportBtn == null) {
			// exportBtn = new JLabel(Images.EXPORT_BTN.getIcon());
			exportBtn = new JLabel(rb.getString("table.export"), Images.EXPORT_BTN.getIcon(),
					JLabel.CENTER);
			exportBtn.setVerticalTextPosition(JLabel.BOTTOM);
			exportBtn.setHorizontalTextPosition(JLabel.CENTER);
			exportBtn.setForeground(Color.white);
			exportBtn.setEnabled(false);
			exportBtn.setToolTipText(rb.getString("chart.tooltip.export"));
			exportBtn.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if (!exportBtn.isEnabled())
						return;
					JFileChooser chooser = new JFileChooser(UserPreferences.getInstance()
							.getLastExportDirectory());
					chooser.addChoosableFileFilter(new FileNameExtensionFilter(rb
							.getString("fileChooser.desc.csv"), rb
							.getString("fileChooser.contentType.csv")));
					chooser.setDialogTitle(rb.getString("fileChooser.Title"));
					chooser.setApproveButtonText(rb.getString("fileChooser.Save"));
					chooser.setMultiSelectionEnabled(false);
					try {
						saveCSV(chooser);
					} catch (IOException ex) {
						logger.log(Level.SEVERE, ex.getMessage());
						MessageDialogFactory.showErrorDialog(parent,
								rb.getString("exportall.errorFileOpen") + ex.getMessage());
					}
				}
			});
		}
		return exportBtn;
	}

	/**
	 * Method to export the statistics data in the csv format.
	 */
	private void saveCSV(JFileChooser chooser) throws IOException {
		if (chooser.showSaveDialog(AROAnalysisResultsTab.this.getTopLevelAncestor()) != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File file = chooser.getSelectedFile();
		if (file.getName().length() >= 50) {
			MessageDialogFactory.showErrorDialog(parent,
					rb.getString("exportall.errorLongFileName"));
			return;
		}
		if (!chooser.getFileFilter().accept(file)) {
			file = new File(file.getAbsolutePath() + "."
					+ rb.getString("fileChooser.contentType.csv"));
		}
		if (file.exists()) {
			if (MessageDialogFactory.showConfirmDialog(
					this.getTopLevelAncestor(),
					MessageFormat.format(rb.getString("fileChooser.fileExists"),
							file.getAbsolutePath()), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
				return;
			}
		}

		final Profile profile = this.parent.getProfile();
		final AROBestPracticesTab aroBestPracticesPanel = this.parent.getBestPracticesPanel();
		final AROAnalysisResultsTab analyisResultsPanel = this.parent.getAnalysisResultsPanel();

		FileWriter writer = new FileWriter(file);
		try {
			writer.append(rb.getString("Export.header.tracesummary"));
			writer.append(lineSep);

			// Adding Trace data content to file writer.
			writer = analyisResultsPanel.getdateTraceAppDetailPanel().addTraceDateContent(writer,
					analysisData);
			writer = aroBestPracticesPanel.getBestPracticesPanel().getAROBpOverallResulsPanel()
					.addBpOverallContent(writer, analysisData);
			writer = analyisResultsPanel.getBasicStatisticsPanel().addBasicContent(writer,
					analysisData);
			writer.append(lineSep);

			// Adding Best Practice content to file writer.
			aroBestPracticesPanel.getBestPracticesPanel().addBestPracticeContent(writer, analysisData);
			writer.append(lineSep);

			writer.append(rb.getString("Export.header.filetypes"));
			writer.append(lineSep);
			this.parent.getAroSimpleTab().getFileTypesChartPanel().addFiletypes(writer);
			writer.append(lineSep);
			writer.append(rb.getString("Export.header.traceoverview"));
			writer.append(lineSep);
			this.parent.getAroSimpleTab().getTraceOverviewPanel().addTraceOverview(writer);
			
			// 
			writer.append(lineSep);
			writer.append(rb.getString("Export.header.connectionstatistics"));
			writer.append(lineSep);
			this.parent.getAroSimpleTab().getProperSessionTermChartPanel()
					.addTraceOverview(writer);
			writer.append(lineSep);
			writer.append(rb.getString("statics.csvHeader.appScore"));
			writer.append(lineSep);
			writer = analyisResultsPanel.getApplicationScorePanel().addApplicationScoreInfo(writer, analysisData);
			writer.append(lineSep);

			// Adding the end point per application Summary table in to the file
			// writer
			writer.append(rb.getString("statics.csvHeader.endPointSummaryApp"));
			writer.append(lineSep);
			// Adding the end point summary content in to the file writer
			writer = analyisResultsPanel
					.getEndPointSummaryPanel()
					.getTableModel()
					.addEndPointSummaryPerAppTable(writer,
							analyisResultsPanel.getEndPointSummaryPanel().getTable());
			writer.append(lineSep);
			writer.append(lineSep);

			// Adding the end point per IP addressSummary table in to the file
			// writer
			writer.append(rb.getString("statics.csvHeader.endPointSummaryIP"));
			writer.append(lineSep);
			// Adding the end point summary content in to the file writer
			writer = analyisResultsPanel
					.getEndPointSummaryPanel()
					.getIpTableModel()
					.addEndPointSummaryPerIPTable(writer,
							analyisResultsPanel.getEndPointSummaryPanel().getIPTable());
			writer.append(lineSep);
			writer.append(lineSep);

			writer.append(rb.getString("statics.csvHeader.rrcState"));
			writer.append(lineSep);
			// Adding the RRC content in to the file writer
			writer = analyisResultsPanel.getRRCStatisticsPanel().addRRCContent(writer);
			writer.append(lineSep);
			writer.append(lineSep);

			writer.append(rb.getString("statics.csvHeader.burst"));
			writer.append(lineSep);
			// Adding the burst table in to the file writer
			writer = analyisResultsPanel.getBurstAnalysisPanel().addBurstTable(writer);
			writer.append(lineSep);
			writer.append(rb.getString("statics.csvHeader.individualBurst"));
			writer.append(lineSep);
			writer = analyisResultsPanel.getBurstAnalysisPanel().addBurstCollectionTable(writer);
			writer.append(lineSep);
			writer.append(lineSep);

			// Adding the cache analysis content in to the file writer
			writer = analyisResultsPanel.getCacheStatisticsPanel().addCacheContent(writer);

			writer.append(lineSep);
			writer.append(lineSep);

			writer.append(rb.getString("statics.csvHeader.energyState"));
			writer.append(lineSep);
			// Adding the energy content in to the file writer
			if (profile instanceof Profile3G) {
				writer = analyisResultsPanel.getEnergyModelStatistics3GPanel().addEnergyContent(
						writer);
			} else if (profile instanceof ProfileLTE) {
				writer = analyisResultsPanel.getEnergyModelStatisticsLTEPanel().addEnergyContent(
						writer);
			} else if (profile instanceof ProfileWiFi) {
				writer = analyisResultsPanel.getWiFiEnergyModelStatisticsPanel().addEnergyContent(
						writer);
			}

		} finally {
			writer.close();
		}
		if (file.getName().contains(".csv")) {
			if (MessageDialogFactory.showExportConfirmDialog(chooser) == JOptionPane.YES_OPTION) {
				try {
					Desktop desktop = Desktop.getDesktop();
					desktop.open(file);
				} catch (UnsupportedOperationException unsupportedException) {
					MessageDialogFactory.showMessageDialog(chooser,
							rb.getString("Error.unableToOpen"));
				}
			}
		} else {
			MessageDialogFactory.showMessageDialog(chooser, rb.getString("table.export.success"));
		}
	}

	/**
	 * Returns the object DateTraceAppDetailPanel.
	 * 
	 * @return the trace date and detail panel
	 */
	private DateTraceAppDetailPanel getdateTraceAppDetailPanel() {
		return this.dateTraceAppDetailPanel;
	}
}
