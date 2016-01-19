/*
 *  Copyright 2015 AT&T
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
package com.att.aro.ui.model;

import java.awt.Desktop;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.att.aro.ui.commonui.MessageDialogFactory;
import com.att.aro.ui.commonui.UserPreferences;
import com.att.aro.ui.utils.ResourceBundleHelper;


/**
 * @author Harikrishna Yaramachu
 *
 * Represents the default popup menu for a data table in the ARO Data Analyzer.
 */
public class DataTablePopupMenu extends JPopupMenu {
	private static final long serialVersionUID = 1L;

	private DataTable<?> table;
	private JMenuItem exportMenuItem;

	/**
	 * Initializes a new instance of the DataTablePopupMenu class using the
	 * specified DataTable object.
	 * 
	 * @param table
	 *            The DataTable to associate with the DataTablePopupMenu.
	 */
	public DataTablePopupMenu(DataTable<?> table) {
		this.table = table;
		initialize();
	}

	/**
	 * Method to put the Export menu item in table.
	 */
	private void initialize() {
		this.add(getExportMenuItem());
	}

	/**
	 * Method to initialize the Export menu item for the table.
	 * 
	 * @return the exportMenuItem
	 */
	private JMenuItem getExportMenuItem() {
		if (exportMenuItem == null) {
			exportMenuItem = new JMenuItem(ResourceBundleHelper.getMessageString("table.export"));
			exportMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent aEvent) {
					JFileChooser chooser = new JFileChooser(UserPreferences.getInstance()
							.getLastExportDirectory());
					FileNameExtensionFilter filter = new FileNameExtensionFilter(
							ResourceBundleHelper.getMessageString("fileChooser.desc.csv"), 
							ResourceBundleHelper.getMessageString("fileChooser.contentType.csv"));
					chooser.setFileFilter(filter);
					chooser.addChoosableFileFilter(null);
					chooser.setDialogTitle(ResourceBundleHelper.getMessageString("fileChooser.Title"));
					chooser.setApproveButtonText(ResourceBundleHelper.getMessageString("fileChooser.Save"));
					chooser.setMultiSelectionEnabled(false);
					try {
						saveFile(chooser);
					} catch (Exception exp) {
						MessageDialogFactory.getInstance().showErrorDialog(new Window(new Frame()),
								ResourceBundleHelper.getMessageString("exportall.errorFileOpen") + exp.getMessage());
					}
				}

			});
		}
		return exportMenuItem;
	}

	/**
	 * Method to export the table content in to the CSV file format.
	 * 
	 * @param chooser
	 *            {@link JFileChooser} object to validate the save option.
	 */
	private void saveFile(JFileChooser chooser) throws Exception {
		Frame frame = Frame.getFrames()[0];// get parent frame
		if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (!chooser.getFileFilter().accept(file)) {
				file = new File(file.getAbsolutePath() + "."
						+ ResourceBundleHelper.getMessageString("fileChooser.contentType.csv"));
			}
			if (file.exists()) {
				if (MessageDialogFactory.getInstance().showConfirmDialog(
						frame,
						MessageFormat.format(ResourceBundleHelper.getMessageString("fileChooser.fileExists"),
								file.getAbsolutePath()), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					saveFile(chooser);
					return;
				}
			}

			FileWriter writer = new FileWriter(file);
			try {
				if (table.getSelectedRowCount() > 1) {
					writeFile(writer, false);
				} else {
					writeFile(writer, true);
				}
				//TODO take care about the commented code.
				//UserPreferences.getInstance().setLastExportDirectory(file);
			} finally {
				writer.close();
			}
			if (file.getName().contains(".csv")) {
				if (MessageDialogFactory.getInstance().showExportConfirmDialog(frame) == JOptionPane.YES_OPTION) {
					try {
						Desktop desktop = Desktop.getDesktop();
						desktop.open(file);
					} catch (UnsupportedOperationException unsupportedException) {
						MessageDialogFactory.showMessageDialog(frame,
								ResourceBundleHelper.getMessageString("Error.unableToOpen"));
					}
				}
			} else {
				MessageDialogFactory.showMessageDialog(frame, ResourceBundleHelper.getMessageString("table.export.success"));
			}
		}
	}

	/**
	 * Method to convert the {@link Object} values in to {@link String} values.
	 * 
	 * @param val
	 *            {@link Object} value retrieved from the table cell.
	 * @return Cell data in string format.
	 */
	private String createCSVEntry(Object val) {
		StringBuffer writer = new StringBuffer();
		String str = val != null ? val.toString() : "";
		writer.append('"');
		for (char strChar : str.toCharArray()) {
			switch (strChar) {
			case '"':
				// Add an extra
				writer.append("\"\"");
				break;
			default:
				writer.append(strChar);
			}
		}
		writer.append('"');
		return writer.toString();
	}

	private FileWriter writeFile(FileWriter writer, boolean isAllRow) throws IOException {
		final String lineSep = System.getProperty("line.separator");
		// Write headers
		for (int columnIndex = 0; columnIndex < table.getColumnCount(); ++columnIndex) {
			if (columnIndex > 0) {
				writer.append(',');
			}
			writer.append(createCSVEntry(table.getColumnModel().getColumn(columnIndex).getHeaderValue()));
		}
		writer.append(lineSep);

		if (isAllRow) {
			// Write data
			for (int rowIndex = 0; rowIndex < table.getRowCount(); ++rowIndex) {
				for (int columnIndex = 0; columnIndex < table.getColumnCount(); ++columnIndex) {
					if (columnIndex > 0) {
						writer.append(',');
					}
					writer.append(createCSVEntry(table.getValueAt(rowIndex, columnIndex)));
				}
				writer.append(lineSep);
			}
		} else {
			int[] rowIndex = table.getSelectedRows();
			for (int sRow = 0; sRow < table.getSelectedRowCount(); ++sRow) {
				for (int sColumn = 0; sColumn < table.getColumnCount(); ++sColumn) {
					if (sColumn > 0) {
						writer.append(',');
					}
					writer.append(createCSVEntry(table.getValueAt(rowIndex[sRow], sColumn)));
				}
				writer.append(lineSep);
			}
		}

		return writer;
	}
}
