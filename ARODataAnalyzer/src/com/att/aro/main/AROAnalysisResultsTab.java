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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.att.aro.commonui.AROUIManager;
import com.att.aro.commonui.DataTable;
import com.att.aro.commonui.ImagePanel;
import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.images.Images;
import com.att.aro.model.BurstAnalysisInfo;
import com.att.aro.model.Profile;
import com.att.aro.model.Profile3G;
import com.att.aro.model.TraceData;
import com.att.aro.model.UserPreferences;

/**
 * Represents the Statistics tab screen.
 */
public class AROAnalysisResultsTab extends JScrollPane implements Printable {
	private static final long serialVersionUID = 1L;

	private JPanel mainPanel;
	private BasicStatisticsPanel basicStatisticsPanel;
	private BurstAnalysisPanel burstAnalysisPanel;
	private RRCStatisticsPanel rrcStatisticsPanel;
	private EnergyModelStatisticsPanel energyModelStatisticsPanel;
	private HttpCacheStatisticsPanel cacheStatisticsPanel;
	private ImagePanel headerPanel;
	private DateTraceAppDetailPanel dateTraceAppDetailPanel;
	private JLabel exportBtn;
	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

	String lineSep = System.getProperty(rb
			.getString("statics.csvLine.seperator"));

	/**
	 * Initializes a new instance of the AROAnalysisResultsTab class, using the
	 * specified instance of the ApplicationResourceOptimizer as the parent
	 * window for the tab.
	 * 
	 * @param parent
	 *            - The ApplicationResourceOptimizer instance.
	 */
	public AROAnalysisResultsTab(ApplicationResourceOptimizer parent) {
		setViewportView(getMainPanel());
		getVerticalScrollBar().setUnitIncrement(10);
	}

	/**
	 * Refreshes the content of the Panels in the Analysis Results tab with the
	 * specified trace data.
	 * 
	 * @param analysisData
	 *            - The Analysis object containing the trace data.
	 */
	public void refresh(TraceData.Analysis analysisData) {
		dateTraceAppDetailPanel.refresh(analysisData);
		getBasicStatisticsPanel().refresh(analysisData);
		getBurstAnalysisPanel().refresh(analysisData);
		getRRCStatisticsPanel().refresh(analysisData);
		getCacheStatisticsPanel().refresh(analysisData);
		if (analysisData != null) {
			final Profile profile = analysisData.getProfile();
			if (profile instanceof Profile3G) {
				getEnergyModelStatistics3GPanel().refresh(analysisData);
			} else {
				getEnergyModelStatisticsLTEPanel().refresh(analysisData);
			}
		}
		exportBtn.setEnabled(true);
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
		return new AROPrintablePanel(getMainPanel()).print(graphics,
				pageFormat, pageIndex);
	}

	/**
	 * Scrolls the display to the Cache Statistics panel of the Analysis Results
	 * tab.
	 */

	public void scrollToCacheStatistics() {
		getMainPanel().scrollRectToVisible(
				getCacheStatisticsPanel().getBounds());
	}

	/**
	 * Adds the various Panels for the Statistics tab.
	 * 
	 * @return the mainPanel The JPanel containing the entire screen.
	 */
	private JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel(new GridBagLayout());
			mainPanel.setBackground(UIManager
					.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
			Insets headerInsets = new Insets(0, 0, 0, 0);
			Insets insets = new Insets(5, 5, 5, 5);
			Insets insetsWithOutHeader = new Insets(5, 20, 5, 5);
			mainPanel.add(getHeaderPanel(), new GridBagConstraints(0, 0, 1, 1,
					0.6, 0.0, GridBagConstraints.NORTH,
					GridBagConstraints.HORIZONTAL, headerInsets, 0, 0));
			dateTraceAppDetailPanel = new DateTraceAppDetailPanel();
			mainPanel.add(dateTraceAppDetailPanel, new GridBagConstraints(0, 1,
					1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, insetsWithOutHeader, 0, 0));
			mainPanel.add(new ImagePanel(Images.DIVIDER.getImage(), true,
					Color.WHITE), new GridBagConstraints(0, 2, 3, 1, 1.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
					insets, 0, 0));
			mainPanel.add(getBasicStatisticsPanel(), new GridBagConstraints(0,
					3, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, insetsWithOutHeader, 0, 0));
			mainPanel.add(new ImagePanel(Images.DIVIDER.getImage(), true,
					Color.WHITE), new GridBagConstraints(0, 4, 3, 1, 1.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
					insets, 0, 0));
			mainPanel.add(getRRCStatisticsPanel(), new GridBagConstraints(0, 5,
					1, 1, 0.6, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, insetsWithOutHeader, 0, 0));
			mainPanel.add(new ImagePanel(Images.DIVIDER.getImage(), true,
					Color.WHITE), new GridBagConstraints(0, 6, 3, 1, 1.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
					insets, 0, 0));
			mainPanel.add(getBurstAnalysisPanel(), new GridBagConstraints(0, 7,
					1, 1, 0.6, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, insetsWithOutHeader, 0, 0));
			mainPanel.add(new ImagePanel(Images.DIVIDER.getImage(), true,
					Color.WHITE), new GridBagConstraints(0, 8, 3, 1, 1.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
					insets, 0, 0));
			mainPanel.add(getCacheStatisticsPanel(), new GridBagConstraints(0,
					9, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, insetsWithOutHeader, 0, 0));
			mainPanel.add(new ImagePanel(Images.DIVIDER.getImage(), true,
					Color.WHITE), new GridBagConstraints(0, 10, 3, 1, 1.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
					insets, 0, 0));
			mainPanel.add(getEnergyModelStatistics3GPanel(),
					new GridBagConstraints(0, 11, 1, 3, 0.4, 0.0,
							GridBagConstraints.WEST,
							GridBagConstraints.HORIZONTAL, insetsWithOutHeader,
							0, 0));
		}
		return mainPanel;
	}

	/**
	 * Initializes cache statistics panel
	 */
	private BasicStatisticsPanel getBasicStatisticsPanel() {
		if (basicStatisticsPanel == null) {
			basicStatisticsPanel = new BasicStatisticsPanel();
		}
		return basicStatisticsPanel;
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
			if (energyModelStatisticsPanel instanceof EnergyModelStatisticsLTEPanel) {
				mainPanel.remove(energyModelStatisticsPanel);
				energyModelStatisticsPanel = null;
				energyModelStatisticsPanel = new EnergyModelStatistics3GPanel();
				mainPanel.add(energyModelStatisticsPanel,
						new GridBagConstraints(0, 11, 1, 3, 0.4, 0.0,
								GridBagConstraints.WEST,
								GridBagConstraints.HORIZONTAL, new Insets(5,
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
				&& energyModelStatisticsPanel instanceof EnergyModelStatistics3GPanel) {
			mainPanel.remove(energyModelStatisticsPanel);
			energyModelStatisticsPanel = null;
			energyModelStatisticsPanel = new EnergyModelStatisticsLTEPanel();
			mainPanel.add(energyModelStatisticsPanel, new GridBagConstraints(0,
					11, 1, 3, 0.4, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, new Insets(5, 20, 5, 5), 0,
					0));
		}
		return energyModelStatisticsPanel;
	}

	/**
	 * Creates the blue header with the ATT logo.
	 */
	private ImagePanel getHeaderPanel() {
		if (headerPanel == null) {
			headerPanel = new ImagePanel(Images.BLUE_HEADER.getImage());
			headerPanel.setLayout(new BorderLayout(50, 50));
			headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
					10));
			JLabel l = new JLabel(Images.HEADER_ICON.getIcon(),
					SwingConstants.CENTER);
			l.setPreferredSize(new Dimension(80, 80));
			headerPanel.add(l, BorderLayout.WEST);

			JLabel bpHeaderLabel = new JLabel(rb.getString("statistics.title"));
			bpHeaderLabel.setFont(UIManager
					.getFont(AROUIManager.TITLE_FONT_KEY));
			bpHeaderLabel.setForeground(Color.WHITE);
			headerPanel.add(bpHeaderLabel, BorderLayout.CENTER);

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new BorderLayout());
			buttonPanel.setOpaque(false);
			buttonPanel.add(getExportBtn(), BorderLayout.CENTER);
			headerPanel.add(buttonPanel, BorderLayout.EAST);
		}
		return headerPanel;
	}

	/**
	 * Creates a export button and handles the export functionality.
	 */
	private JLabel getExportBtn() {
		if (exportBtn == null) {
			exportBtn = new JLabel(Images.EXPORT_BTN.getIcon());
			exportBtn.setEnabled(false);
			exportBtn.setToolTipText(rb.getString("chart.tooltip.export"));
			exportBtn.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					JFileChooser chooser = new JFileChooser(UserPreferences
							.getInstance().getLastExportDirectory());
					chooser.addChoosableFileFilter(new FileNameExtensionFilter(
							rb.getString("fileChooser.desc.csv"), rb
									.getString("fileChooser.contentType.csv")));
					chooser.setDialogTitle(rb.getString("fileChooser.Title"));
					chooser.setApproveButtonText(rb
							.getString("fileChooser.Save"));
					chooser.setMultiSelectionEnabled(false);
					try {
						saveCSV(chooser);
					} catch (IOException e1) {
						MessageDialogFactory.showUnexpectedExceptionDialog(
								AROAnalysisResultsTab.this
										.getTopLevelAncestor(), e1);
					}
				}
			});
		}
		return exportBtn;
	}

	/**
	 * Method to export the statics data in the csv format.
	 */
	private void saveCSV(JFileChooser chooser) throws IOException {
		if (chooser.showSaveDialog(AROAnalysisResultsTab.this
				.getTopLevelAncestor()) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = chooser.getSelectedFile();
		if (!chooser.getFileFilter().accept(file)) {
			file = new File(file.getAbsolutePath() + "."
					+ rb.getString("fileChooser.contentType.csv"));
		}
		if (file.exists()) {
			if (MessageDialogFactory.showConfirmDialog(this
					.getTopLevelAncestor(), MessageFormat.format(
					rb.getString("fileChooser.fileExists"),
					file.getAbsolutePath()), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				saveCSV(chooser);
				return;
			}
		}
		FileWriter writer = new FileWriter(file);
		try {
			writer.append(rb.getString("statics.csvHeader.tcp"));
			writer.append(lineSep);

			Map<String, String> basicStatisticsData = basicStatisticsPanel
					.getBasicContent();
			// Adding the Basic content in to the file writer
			writer = addBasicContent(writer, basicStatisticsData);
			writer.append(lineSep);
			writer.append(lineSep);

			Map<String, String> rrcStatisticsData = rrcStatisticsPanel
					.getRrcContent();
			writer.append(rb.getString("statics.csvHeader.rrcState"));
			writer.append(lineSep);
			// Adding the RRC content in to the file writer
			writer = addRRCContent(writer, rrcStatisticsData);
			writer.append(lineSep);
			writer.append(lineSep);

			Map<String, String> energyStatisticsData = energyModelStatisticsPanel
					.getEnergyContent();
			writer.append(rb.getString("statics.csvHeader.energyState"));
			writer.append(lineSep);
			// Adding the energy content in to the file writer
			writer = addEnergyContent(writer, energyStatisticsData);
			writer.append(lineSep);
			writer.append(lineSep);

			writer.append(MessageFormat.format(
					rb.getString("statics.csvHeader.burst"), 0, 840.00));
			writer.append(lineSep);
			// Adding the burst table in to the file writer
			writer = addBurstTable(writer, burstAnalysisPanel.getTable());
			writer.append(lineSep);
			writer.append(lineSep);

			Map<String, String> cacheStatisticsData = cacheStatisticsPanel
					.getCacheContent();
			// Adding the cache analysis content in to the file writer
			writer = addCacheContent(writer, cacheStatisticsData);

		} finally {
			writer.close();
		}
	}

	/**
	 * Method to add the cache content in to the csv file
	 */
	private FileWriter addCacheContent(FileWriter writer,
			Map<String, String> cacheStatisticsData) {
		try {
			for (Map.Entry<String, String> iter : cacheStatisticsData
					.entrySet()) {
				String individualVal = iter.getValue().replace(
						rb.getString("statics.csvCell.seperator"), "");
				String individualKey = iter.getKey().replace(
						rb.getString("statics.csvCell.seperator"), "");
				if (individualKey.startsWith(rb
						.getString("fileType.filters.hash"))) {
					writer.append(lineSep);
					writer.append(lineSep);
					writer.append(individualKey.substring(1));
				} else {
					writer.append(individualKey);
				}

				writer.append(rb.getString("statics.csvCell.seperator"));
				if (individualVal.contains(rb
						.getString("fileType.filters.forwardSlash"))) {
					writer.append(individualVal.substring(
							0,
							individualVal.indexOf(rb
									.getString("fileType.filters.forwardSlash"))));
					writer.append(rb.getString("statics.csvCell.seperator"));
					writer.append(individualVal.substring(individualVal.indexOf(rb
							.getString("fileType.filters.forwardSlash")) + 1));
				} else if (!individualVal.equalsIgnoreCase(rb
						.getString("fileType.filters.backwardSlash"))) {
					writer.append(individualVal);
				}
				writer.append(rb.getString("statics.csvCell.seperator"));
				writer.append(lineSep);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return writer;
	}

	/**
	 * Method to write the RRC statistics content into the csv file
	 */
	private FileWriter addRRCContent(FileWriter writer,
			Map<String, String> rrcStatisticsData) {
		try {
			for (Map.Entry<String, String> iter : rrcStatisticsData.entrySet()) {
				String individualVal = iter.getValue().replace(
						rb.getString("statics.csvCell.seperator"), "");
				writer.append(iter.getKey());
				writer.append(rb.getString("statics.csvCell.seperator"));
				if (individualVal.contains(rb
						.getString("statics.csvCell.openBraket"))) {
					writer.append(individualVal.substring(0,
							individualVal.indexOf(rb
									.getString("statics.csvCell.openBraket"))));
					writer.append(rb.getString("statics.csvCell.seperator"));
					writer.append(rb.getString("statics.csvUnits.s"));
					writer.append(rb.getString("statics.csvCell.seperator"));
					writer.append(individualVal.substring(
							individualVal.indexOf(rb
									.getString("statics.csvCell.openBraket")) + 1,
							individualVal.indexOf(rb
									.getString("statics.csvCell.closeBraket"))));
				} else {
					writer.append(individualVal);
				}
				writer.append(lineSep);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return writer;
	}

	/**
	 * Method to add the energy statistics content in the csv file
	 */
	private FileWriter addEnergyContent(FileWriter writer,
			Map<String, String> energyStatisticsData) {
		try {
			for (Map.Entry<String, String> iter : energyStatisticsData
					.entrySet()) {
				String individualVal = iter.getValue().replace(
						rb.getString("statics.csvCell.seperator"), "");
				writer.append(iter.getKey());
				writer.append(rb.getString("statics.csvCell.seperator"));
				if (individualVal.contains(rb.getString("statics.csvUnits.j"))) {
					writer.append(individualVal.substring(0, individualVal
							.indexOf(rb.getString("statics.csvUnits.j"))));
					writer.append(rb.getString("statics.csvCell.seperator"));
					writer.append(rb.getString("statics.csvUnits.j"));
				} else {
					writer.append(individualVal);
				}
				writer.append(lineSep);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return writer;
	}

	/**
	 * Method to add the TCP Statistics content in the csv file.
	 */
	private FileWriter addBasicContent(FileWriter writer,
			Map<String, String> csvContent) {
		try {
			int count = 0;
			for (Map.Entry<String, String> iter : csvContent.entrySet()) {
				writer.append(iter.getKey());
				writer.append(rb.getString("statics.csvCell.seperator"));
				writer.append(iter.getValue().replace(
						rb.getString("statics.csvCell.seperator"), ""));
				writer.append(rb.getString("statics.csvCell.seperator"));
				if (count == 0) {
					writer.append(rb.getString("statics.csvUnits.bytes"));
				} else if (count == 1) {
					writer.append(rb.getString("statics.csvUnits.s"));
				}
				writer.append(lineSep);
				count++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return writer;
	}

	/**
	 * Method to write the burst information into the csv file.
	 */
	private FileWriter addBurstTable(FileWriter writer,
			DataTable<BurstAnalysisInfo> table) {
		try {
			// Write headers
			for (int i = 0; i < table.getColumnCount(); ++i) {
				if (i > 0) {
					writer.append(rb.getString("statics.csvCell.seperator"));
				}
				writer.append(createCSVEntry(table.getColumnModel()
						.getColumn(i).getHeaderValue()));
			}
			writer.append(lineSep);
			// Write data
			for (int i = 0; i < table.getRowCount(); ++i) {
				for (int j = 0; j < table.getColumnCount(); ++j) {
					if (j > 0) {
						writer.append(rb.getString("statics.csvCell.seperator"));
					}
					writer.append(createCSVEntry(table.getValueAt(i, j)));
				}
				writer.append(lineSep);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return writer;
	}

	/**
	 * Changes the format of the table object.
	 */
	private String createCSVEntry(Object val) {
		StringBuffer writer = new StringBuffer();
		String str = val != null ? val.toString() : "";
		writer.append('"');
		for (char c : str.toCharArray()) {
			switch (c) {
			case '"':
				// Add an extra
				writer.append("\"\"");
				break;
			default:
				writer.append(c);
			}
		}
		writer.append('"');
		return writer.toString();
	}
}
