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

import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import com.att.aro.main.ResourceBundleManager;

/**
 * A factory class for displaying common message dialogs used by the ARO Data
 * Analyzer.
 */
public class MessageDialogFactory extends JOptionPane {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();

	/**
	 * Displays a dialog that is used for reporting unexpected exceptions to the
	 * user. The error dialog is associated with the specified parent window,
	 * and contains the specified exception. Unexpected exceptions can be I/O
	 * exceptions or other checked exceptions that can be handled locally.
	 * 
	 * @param parentComponent
	 *            The parent window to associate with this dialog.
	 * @param t
	 *            The exception that should be thrown for this error.
	 */
	public static void showUnexpectedExceptionDialog(Component parentComponent, Throwable t) {
		t.printStackTrace();
		String msg = t.getLocalizedMessage();
		if (msg != null && msg.length() > 200) {
			msg = rb.getString("Error.defaultMsg");
		}
		showMessageDialog(
				parentComponent,
				MessageFormat.format(rb.getString("Error.unexpected"), t.getClass().getName(),
						msg), rb.getString("Error.title"), ERROR_MESSAGE);
	}

	/**
	 * Displays an error dialog for the specified invalid trace file name. The
	 * error dialog is associated with the specified parent window, and contains
	 * the specified exception.
	 * 
	 * @param strTraceDir
	 *            The trace directory of the invalid trace file.
	 * @param parentComponent
	 *            The parent window to associate with this dialog.
	 * @param t
	 *            The exception that should be thrown for this error.
	 */
	public static void showInvalidTraceDialog(String strTraceDir, Component parentComponent,
			Throwable t) {
		showMessageDialog(
				parentComponent,
				MessageFormat.format(rb.getString("Error.invalidTrace"), strTraceDir,
						t.getLocalizedMessage()), rb.getString("Error.title"), ERROR_MESSAGE);
	}
	
	/**
	 * Displays an error dialog for the specified invalid directory. The
	 * error dialog is associated with the specified parent window, and contains
	 * the specified exception.
	 * 
	 * @param strTraceDir
	 *            The invalid directory.
	 * @param parentComponent
	 *            The parent window to associate with this dialog.
	 * @param t
	 *            The exception that should be thrown for this error.
	 */
	public static void showInvalidDirectoryDialog(String strTraceDir, Component parentComponent,
			Throwable t) {
		showMessageDialog(
				parentComponent,
				MessageFormat.format(rb.getString("Error.invalidDirecotry"), strTraceDir,
						t.getLocalizedMessage()), rb.getString("Error.title"), ERROR_MESSAGE);
	}

	/**
	 * Displays an error dialog with the specified title. The error dialog is
	 * associated with the specified parent window, and contains the specified
	 * message.
	 * 
	 * @param window
	 *            The parent window to associate with this dialog.
	 * @param message
	 *            The message to be displayed in the dialog.
	 * @param title
	 *            The dialog title.
	 */
	public static void showErrorDialog(Window window, String message, String title) {
		showMessageDialog(window, message, title, ERROR_MESSAGE);
	}

	/**
	 * Displays an error dialog using the default title. The error dialog is
	 * associated with the specified parent window, and contains the specified
	 * message.
	 * 
	 * @param window
	 *            The parent window to associate with this dialog.
	 * @param message
	 *            The message to be displayed in the dialog.
	 */
	public static void showErrorDialog(Window window, String message) {
		showMessageDialog(window, message, rb.getString("Error.title"), ERROR_MESSAGE);
	}

	/**
	 * Displays a confirmation dialog using the default title. The confirmation
	 * dialog is associated with the specified parent window, contains the
	 * specified message, and uses the specified optionType.
	 * 
	 * @param parentComponent
	 *            The parent window to associate with this dialog.
	 * @param message
	 *            The message to be displayed in the dialog.
	 * @param optionType
	 *            An int that identifies the dialog option type.
	 */
	public static int showConfirmDialog(Component parentComponent, String message, int optionType) {
		Object[] options = { rb.getString("jdialog.option.yes"), rb.getString("jdialog.option.no") };
		return JOptionPane.showOptionDialog(parentComponent, message,
				rb.getString("confirm.title"), optionType, JOptionPane.QUESTION_MESSAGE, null,
				options, options[0]);
	}

	/**
	 * Displays a confirmation dialog for exporting data from a table. The dialog 
	 * uses the default title, and is associated with the specified parent window.
	 * 
	 * @param parentComponent
	 *            The parent window to associate with this dialog.
	 */
	public static int showExportConfirmDialog(Component parentComponent) {
		Object[] options = { rb.getString("Button.open"), rb.getString("Button.ok") };
		return JOptionPane.showOptionDialog(parentComponent, rb.getString("table.export.success"),
				rb.getString("confirm.title"), JOptionPane.YES_OPTION,
				JOptionPane.OK_CANCEL_OPTION, null, options, options[1]);
	}
	/**
	 * Display input dialog for user to enter password
	 * @param parent parent component
	 * @param title what to show on the dialog box title
	 * @param message what to show to user.
	 * @return
	 */
	public static String showInputPassword(Component parent, String title, String message){
		return showInputPassword(parent,title,message,14);
	}
	/**
	 * Display input dialog for user to enter password
	 * @param parent parent component
	 * @param title what to show on the dialog box title
	 * @param message what to show to user.
	 * @param fontsize how big should the font be
	 * @return
	 */
	public static String showInputPassword(Component parent, String title, String message, int fontsize){
		JPanel panel = new JPanel();
		JLabel label = new JLabel(message+"\r\n");
		label.setFont(new Font(label.getFont().getName(), Font.PLAIN, fontsize));
		JPasswordField pass = new JPasswordField(10);
		panel.add(label);
		panel.add(pass);
		pass.selectAll();
		pass.requestFocusInWindow();
		
		String[] options = new String[]{"OK", "Cancel"};
		
		int opt = MessageDialogFactory.showOptionDialog(parent, panel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);
		char[] passarr = pass.getPassword();
		String passwd = null; //if cancel return null else return value
		if(opt == MessageDialogFactory.OK_OPTION){
			passwd = new String(passarr);
		}

		return passwd;
	}

}
