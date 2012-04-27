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

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.att.aro.main.ResourceBundleManager;
import com.att.aro.model.UserPreferences;

/**
 * Represents the default popup menu for a data table in the ARO Data Analyzer.
 */
public class DataTablePopupMenu extends JPopupMenu {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(DataTablePopupMenu.class.getName());
	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();

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
			exportMenuItem = new JMenuItem(rb.getString("table.export"));
			exportMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser(UserPreferences.getInstance()
							.getLastExportDirectory());
					chooser.addChoosableFileFilter(new FileNameExtensionFilter(rb
							.getString("fileChooser.desc.csv"), rb
							.getString("fileChooser.contentType.csv")));
					chooser.setDialogTitle(rb.getString("fileChooser.Title"));
					chooser.setApproveButtonText(rb.getString("fileChooser.Save"));
					chooser.setMultiSelectionEnabled(false);
					try {
						saveFile(chooser);
					} catch (Exception e1) {
						MessageDialogFactory.showUnexpectedExceptionDialog(table, e1);
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
		if (chooser.showSaveDialog(table) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (!chooser.getFileFilter().accept(file)) {
				file = new File(file.getAbsolutePath() + "."
						+ rb.getString("fileChooser.contentType.csv"));
			}
			if (file.exists()) {
				if (MessageDialogFactory.showConfirmDialog(
						table,
						MessageFormat.format(rb.getString("fileChooser.fileExists"),
								file.getAbsolutePath()), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					saveFile(chooser);
					return;
				}
			}

			FileWriter writer = new FileWriter(file);
			try {
				String lineSep = System.getProperty("line.separator");

				// Write headers
				for (int i = 0; i < table.getColumnCount(); ++i) {
					if (i > 0) {
						writer.append(',');
					}
					writer.append(createCSVEntry(table.getColumnModel().getColumn(i)
							.getHeaderValue()));
				}
				writer.append(lineSep);

				// Write data
				for (int i = 0; i < table.getRowCount(); ++i) {
					for (int j = 0; j < table.getColumnCount(); ++j) {
						if (j > 0) {
							writer.append(',');
						}
						writer.append(createCSVEntry(table.getValueAt(i, j)));
					}
					writer.append(lineSep);
				}
				UserPreferences.getInstance().setLastExportDirectory(file);
			} finally {
				writer.close();
			}
			Object[] options = { rb.getString("Button.ok"), rb.getString("Button.open") };
			if (JOptionPane.showOptionDialog(table, rb.getString("table.export.success"),
					rb.getString("confirm.title"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[0]) != JOptionPane.YES_OPTION) {
				try {
					Desktop desktop = Desktop.getDesktop();
					desktop.open(file);
				} catch (UnsupportedOperationException unsupportedException) {
					MessageDialogFactory.showMessageDialog(table,
							rb.getString("Error.unableToOpen"));
				}
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
