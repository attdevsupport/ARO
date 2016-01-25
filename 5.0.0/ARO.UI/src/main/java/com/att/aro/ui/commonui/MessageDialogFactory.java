/**
 * Copyright 2016 AT&T
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
package com.att.aro.ui.commonui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.jfree.ui.tabbedui.VerticalLayout;

import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.view.images.Images;

/**
  * A factory class for displaying common message dialogs used by the ARO Data Analyzer.
  */

public class MessageDialogFactory extends JOptionPane{
	public MessageDialogFactory() {
	}
	private static final long serialVersionUID = 1L;

	private static MessageDialogFactory msgDialogInstance;
	private static byte[] msgDialogInstanceCriticalRegion = new byte[0];
	
	public static MessageDialogFactory getInstance(){
		synchronized(msgDialogInstanceCriticalRegion) {
			if (msgDialogInstance == null) {
				msgDialogInstance = new MessageDialogFactory();
			}
		}
		
		return msgDialogInstance;
	}
	
	
	/**
	 * Displays a dialog that is used for reporting unexpected exceptions to the
	 * user. The error dialog is associated with the specified parent window,
	 * and contains the specified exception. Unexpected exceptions can be I/O
	 * exceptions or other checked exceptions that can be handled locally.
	 * 
	 * @param parentComponent
	 *            The parent window to associate with this dialog.
	 * @param throwable
	 *            The exception that should be thrown for this error.
	 */
	public void showUnexpectedExceptionDialog(Component parentComponent, Throwable throwable) {
		String msg = throwable.getLocalizedMessage();
		if (msg != null && msg.length() > 200) {
			msg = ResourceBundleHelper.getMessageString("Error.defaultMsg");
		}
		showMessageDialog(
				parentComponent,
				MessageFormat.format(ResourceBundleHelper.getMessageString("Error.unexpected"), throwable.getClass().getName(),
						msg), ResourceBundleHelper.getMessageString("error.title"), ERROR_MESSAGE);
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
	 * @param throwable
	 *            The exception that should be thrown for this error.
	 */
	public void showInvalidTraceDialog(String strTraceDir, Component parentComponent,
			Throwable throwable) {
		showMessageDialog(
				parentComponent,
				MessageFormat.format(ResourceBundleHelper.getMessageString("Error.invalidTrace"), strTraceDir,
						throwable.getLocalizedMessage()), ResourceBundleHelper.getMessageString("Error.title"), ERROR_MESSAGE);
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
	 * @param throwable
	 *            The exception that should be thrown for this error.
	 */
	public void showInvalidDirectoryDialog(String strTraceDir, Component parentComponent,
			Throwable throwable) {
		showMessageDialog(
				parentComponent,
				MessageFormat.format(ResourceBundleHelper.getMessageString("Error.invalidDirecotry"), strTraceDir,
						throwable.getLocalizedMessage()), ResourceBundleHelper.getMessageString("Error.title"), ERROR_MESSAGE);
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
	public void showErrorDialog(Window window, String message, String title) {
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
	public void showErrorDialog(Window window, String message) {
		showMessageDialog(window, message, ResourceBundleHelper.getMessageString("error.title"), ERROR_MESSAGE);
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
	public int showConfirmDialog(Component parentComponent, String message, int optionType) {
		Object[] options = { ResourceBundleHelper.getMessageString("jdialog.option.yes"), ResourceBundleHelper.getMessageString("jdialog.option.no") };
		return JOptionPane.showOptionDialog(parentComponent, message,
				ResourceBundleHelper.getMessageString("confirm.title"), optionType, JOptionPane.QUESTION_MESSAGE, null,
				options, options[0]);
	}

	public int showConfirmDialog2(Component parentComponent, String message, String title, int optionType) {
		Object[] options = { ResourceBundleHelper.getMessageString("jdialog.option.yes"), ResourceBundleHelper.getMessageString("jdialog.option.no") };
		return JOptionPane.showOptionDialog(parentComponent
					, message
					, title
					, optionType
					, JOptionPane.QUESTION_MESSAGE
					, null
					, options
					, options[0]);
	}

	/**
	 * Displays a confirmation dialog for exporting data from a table. The dialog 
	 * uses the default title, and is associated with the specified parent window.
	 * 
	 * @param parentComponent
	 *            The parent window to associate with this dialog.
	 */
	public int showExportConfirmDialog(Component parentComponent) {
		Object[] options = { ResourceBundleHelper.getMessageString("Button.open"), ResourceBundleHelper.getMessageString("Button.ok") };
		return JOptionPane.showOptionDialog(parentComponent, ResourceBundleHelper.getMessageString("table.export.success"),
				ResourceBundleHelper.getMessageString("confirm.title"), JOptionPane.YES_OPTION,
				JOptionPane.OK_CANCEL_OPTION, null, options, options[1]);
	}
	
	/**
	 * Display input dialog for user to enter tracefolder name and a checkbox for video capture
	 * 
	 * @param parent
	 *            parent component
	 * @param title
	 *            what to show on the dialog box title
	 * @param message
	 *            what to show to user.
	 * @param fontsize
	 *            how big should the font be
	 * @return String[] result[0] is foldername, result[1] is chckbxCaptureVideo true/false
	 */
	public String[] showInputText(Component parent
								, String title
								, String message
								, int fontsize) {
		
		String[] result = new String[2];
		
		JCheckBox chckbxCaptureVideo;
		JPanel panel = new JPanel();
		JLabel label = new JLabel(message + "\r\n");
		label.setFont(new Font(label.getFont().getName(), Font.PLAIN, fontsize));
		
		// TextField
		JTextField textField = new JTextField(30);
		panel.add(label);
		panel.add(textField);
		textField.selectAll();
		textField.requestFocusInWindow();

		
		// Checkbox
		chckbxCaptureVideo = new JCheckBox("Capture Video");
		chckbxCaptureVideo.setToolTipText("Capture video of mobile screen");
		chckbxCaptureVideo.setSelected(true);
		chckbxCaptureVideo.setBounds(6, 32, 150, 23);
		panel.add(chckbxCaptureVideo);
		
		// Buttons
		String[] options = new String[] { "OK", "Cancel" };

		textField.selectAll();
		int opt = MessageDialogFactory.showOptionDialog(parent, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]); 
		
		String text = null; // if cancel return null else return value
		if (opt == MessageDialogFactory.OK_OPTION) {
			text = textField.getText();
		}
		
		result[0] = text;
		result[1] = chckbxCaptureVideo.isSelected() ? "true" : "false";

		return result;
	}
	
	/**
	 * Display input dialog for user to enter tracefolder name and a checkbox for video capture
	 * 
	 * @param parent
	 *            parent component
	 * @param title
	 *            what to show on the dialog box title
	 * @param message
	 *            what to show to user.
	 * @param fontsize
	 *            how big should the font be
	 * @return String[] result[0] is foldername, result[1] is chckbxCaptureVideo true/false
	 */
	public String[] showInputTraceFolder(
								Component parent
								, String title
								, String message
								, int fontsize) {
		
		String[] result = new String[2];
		
		JCheckBox chckbxCaptureVideo;
		JPanel panel = new JPanel();
		JLabel label = new JLabel(message + "\r\n");
		label.setFont(new Font(label.getFont().getName(), Font.PLAIN, fontsize));
		
		// TextField
		JTextField textField = new JTextField(30);
		panel.add(label);
		panel.add(textField);
		textField.selectAll();
		textField.requestFocusInWindow();
		
		// Checkbox
		chckbxCaptureVideo = new JCheckBox("Capture Video");
		chckbxCaptureVideo.setToolTipText("Capture video of mobile screen");
		chckbxCaptureVideo.setSelected(true);
		chckbxCaptureVideo.setBounds(6, 32, 150, 23);
		panel.add(chckbxCaptureVideo);
		
		// Buttons
		String[] options = new String[] { "OK", "Cancel" };

		int opt = MessageDialogFactory.showOptionDialog(parent
							, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]); 
		
		String text = null; // if cancel return null else return value
		if (opt == MessageDialogFactory.OK_OPTION) {
			text = textField.getText();
		}
		
		result[0] = text;
		result[1] = chckbxCaptureVideo.isSelected() ? "true" : "false";

		return result;
	}
	
	public String[] showInputTraceFolder2( Component parent
											, String title
											, String message
											, int fontsize) {
		
		String[] result = new String[2];
		
		JCheckBox chckbxCaptureVideo;
		JPanel panel = new JPanel();
		JLabel label = new JLabel(message + "\r\n");
		label.setFont(new Font(label.getFont().getName(), Font.PLAIN, fontsize));
		
		// TextField
		JTextField textField = new JTextField(30);
		panel.add(label);
		panel.add(textField);
		textField.selectAll();
		textField.requestFocusInWindow();
		
		// Checkbox
		chckbxCaptureVideo = new JCheckBox("Capture Video");
		chckbxCaptureVideo.setToolTipText("Capture video of mobile screen");
		chckbxCaptureVideo.setSelected(true);
		chckbxCaptureVideo.setBounds(6, 32, 150, 23);
		panel.add(chckbxCaptureVideo);
		
		// Buttons
		String[] options = new String[] { "OK", "Cancel" };
		
		JPanel optionBtns = new JPanel();
		optionBtns.setLayout(new GridBagLayout());
		JButton btnOK = new JButton( "OK" );
		JButton btnCancel = new JButton( "Cancel" );

		optionBtns.add(btnOK, new GridBagConstraints(0, 0, 1, 4, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, null, 0, 0));
		optionBtns.add(btnCancel, new GridBagConstraints(1, 0, 1, 4, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, null, 0, 0));
		
		JDialog dialog = new JDialog();
		dialog.setLayout(new BorderLayout());
		dialog.add(panel,BorderLayout.NORTH);
		dialog.add(optionBtns, BorderLayout.SOUTH);
		dialog.setSize(400, 200);
		textField.requestFocusInWindow();
		dialog.setVisible( true );
		
//		String text = null; // if cancel return null else return value
//		if (opt == MessageDialogFactory.OK_OPTION) {
//		text = textField.getText();
//		}
		
//		result[0] = text;
//		result[1] = chckbxCaptureVideo.isSelected() ? "true" : "false";
		
		return result;
}

	/**
	 * 
	 * @param parent
	 * @param path
	 * @param videoStatus
	 * @param traceDuration
	 * @return
	 */
	public String showTimeOutOptions( 
			Component parent
			, String path
			, String title
			, String videoStatus
			, String traceDuration){
		
		boolean approveOpenTrace = false;
		
		Font TEXT_FONT = new Font("TEXT_FONT", Font.PLAIN, 12);
		int HEADER_DATA_SPACING = 10;
		
		JLabel titleLabel;
		
//		JLabel pathLabel;
//		JLabel pathValueLabel;
		
		JLabel dataLabel;
		JLabel dataValueLabel;
		
//		JLabel videoLabel;
		JLabel videoValueLabel;
		
//		JLabel durationLabel;
//		JLabel durationValueLabel;
		
		JPanel summaryAlligmentPanel = new JPanel(new BorderLayout());
		
		JPanel emulatorSummaryDataPanel = new JPanel();
		emulatorSummaryDataPanel.setLayout(new VerticalLayout());
		
//		pathLabel = new JLabel(ResourceBundleHelper.getMessageString("collector.path"));
//		pathLabel.setFont(TEXT_FONT);
//		pathValueLabel = new JLabel(path);
//		pathValueLabel.setFont(TEXT_FONT);
		
		dataLabel = new JLabel(ResourceBundleHelper.getMessageString("collector.timeout.option"));
		dataLabel.setFont(TEXT_FONT);
		dataValueLabel = new JLabel(ResourceBundleHelper.getMessageString("collector.timeout.options1"));
		dataValueLabel.setFont(TEXT_FONT);
		
//		videoLabel = new JLabel(ResourceBundleHelper.getMessageString("collector.timeout.options2"));
//		videoLabel.setFont(TEXT_FONT);
		videoValueLabel = new JLabel(ResourceBundleHelper.getMessageString("collector.timeout.options2"));
		videoValueLabel.setFont(TEXT_FONT);
		
//		durationLabel = new JLabel(ResourceBundleHelper.getMessageString("collector.duration"));
//		durationLabel.setFont(TEXT_FONT);
//		durationValueLabel = new JLabel(traceDuration);
//		durationValueLabel.setFont(TEXT_FONT);
		
		JPanel spacePanel = new JPanel();
		spacePanel.setPreferredSize(new Dimension(this.getWidth(), HEADER_DATA_SPACING));
		
		JPanel summaryDataPanel = new JPanel(new GridLayout(4, 2, 0, 5));
//		summaryDataPanel.add(pathLabel);
//		summaryDataPanel.add(pathValueLabel);
//		summaryDataPanel.add(dataLabel);
		summaryDataPanel.add(dataValueLabel);
//		summaryDataPanel.add(videoLabel);
		summaryDataPanel.add(videoValueLabel);
//		summaryDataPanel.add(durationLabel);
//		summaryDataPanel.add(durationValueLabel);
		
		JPanel summaryTitlePanel = new JPanel(new BorderLayout());
		titleLabel = new JLabel(title);
		titleLabel.setFont(TEXT_FONT);
		summaryTitlePanel.add(titleLabel, BorderLayout.CENTER);
		
		emulatorSummaryDataPanel.add(summaryTitlePanel);
		emulatorSummaryDataPanel.add(spacePanel);
		emulatorSummaryDataPanel.add(summaryDataPanel);
		
		summaryAlligmentPanel.add(emulatorSummaryDataPanel, BorderLayout.SOUTH);
		
		
		// Buttons
		String[] options = new String[] { "Quit"};//, "Stop" };//, "Restart"};
		
		int opt = MessageDialogFactory.showOptionDialog(parent, summaryAlligmentPanel, "Confirm", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]); 
		
		String text = null; // if cancel return null else return value
		
		return options[opt];
		
	}

	public boolean showTraceSummary( Component parent
									, String path
									, boolean videoStatus
									, String traceDuration){
		
		boolean approveOpenTrace = false;

		Font TEXT_FONT = new Font("TEXT_FONT", Font.PLAIN, 12);
		int HEADER_DATA_SPACING = 10;
		
		JLabel summaryLabel;
		JLabel pathLabel;
		JLabel pathValueLabel;
		JLabel dataLabel;
		JLabel dataValueLabel;
		JLabel videoLabel;
		JLabel videoValueLabel;
		JLabel durationLabel;
		JLabel durationValueLabel;
		
		JPanel summaryAlligmentPanel = new JPanel(new BorderLayout());

		JPanel emulatorSummaryDataPanel = new JPanel();
		emulatorSummaryDataPanel.setLayout(new VerticalLayout());

		pathLabel = new JLabel(ResourceBundleHelper.getMessageString("collector.path"));
		pathLabel.setFont(TEXT_FONT);
		pathValueLabel = new JLabel(path);
		pathValueLabel.setFont(TEXT_FONT);
		dataLabel = new JLabel(ResourceBundleHelper.getMessageString("collector.data"));
		dataLabel.setFont(TEXT_FONT);
		dataValueLabel = new JLabel(ResourceBundleHelper.getMessageString("collector.dataValue"));
		dataValueLabel.setFont(TEXT_FONT);
		videoLabel = new JLabel(ResourceBundleHelper.getMessageString("collector.video"));
		videoLabel.setFont(TEXT_FONT);
		videoValueLabel = new JLabel(videoStatus?"Yes":"No");
		videoValueLabel.setFont(TEXT_FONT);
		durationLabel = new JLabel(ResourceBundleHelper.getMessageString("collector.duration"));
		durationLabel.setFont(TEXT_FONT);
		durationValueLabel = new JLabel(traceDuration);
		durationValueLabel.setFont(TEXT_FONT);

		JPanel spacePanel = new JPanel();
		spacePanel.setPreferredSize(new Dimension(this.getWidth(), HEADER_DATA_SPACING));

		JPanel summaryDataPanel = new JPanel(new GridLayout(4, 2, 0, 5));
		summaryDataPanel.add(pathLabel);
		summaryDataPanel.add(pathValueLabel);
		summaryDataPanel.add(dataLabel);
		summaryDataPanel.add(dataValueLabel);
		summaryDataPanel.add(videoLabel);
		summaryDataPanel.add(videoValueLabel);
		summaryDataPanel.add(durationLabel);
		summaryDataPanel.add(durationValueLabel);

		JPanel summaryTitlePanel = new JPanel(new BorderLayout());
		summaryLabel = new JLabel(ResourceBundleHelper.getMessageString("collector.summary"));
		summaryLabel.setFont(TEXT_FONT);
		summaryTitlePanel.add(summaryLabel, BorderLayout.CENTER);

		emulatorSummaryDataPanel.add(summaryTitlePanel);
		emulatorSummaryDataPanel.add(spacePanel);
		emulatorSummaryDataPanel.add(summaryDataPanel);

		summaryAlligmentPanel.add(emulatorSummaryDataPanel, BorderLayout.SOUTH);
		
		
		// Buttons
		String[] options = new String[] { "OK", "Open" };

		int opt = MessageDialogFactory.showOptionDialog(parent, summaryAlligmentPanel, "Confirm", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]); 
		
		String text = null; // if cancel return null else return value

		approveOpenTrace = (opt == 1);
	
		return approveOpenTrace;
		
	}
	
	/**
	 * Display input dialog for user to enter password
	 * @param parent parent component
	 * @param title what to show on the dialog box title
	 * @param message what to show to user.
	 * @return
	 */
	public String showInputPassword(Component parent, String title, String message){
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
	public String showInputPassword(Component parent, String title, String message, int fontsize){
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

	/**
	 * Inform user that phone is rooted and does not have the rooted collector
	 * supports a hyperlink to the developer portal
	 * 
	 * @param options contains navigation buttons such as OK & Cancel
	 * @return user response
	 */
	public int confirmCollectionMethod(Object[] options, String message, String linkMessageParm) {
		JLabel label = new JLabel();
		Font font = label.getFont();
		String linkMessage = linkMessageParm == null ? "" : linkMessageParm;

		// create some css from the label's font
		StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;");

		String line2 = "";
		if (message != null) {
			line2 = "<p>" + message + "</p>";
		}
		
		// html content
		JEditorPane editPane = new JEditorPane("text/html", "<html><body style=\"" + style + "\">" //
				+ "<p>The Phone appears to have root capability, but does not have the rooted collector installed.</p>"
				+ line2
				+ linkMessage
				+ "</body></html>");

		// handle link events
		editPane.addHyperlinkListener(new HyperlinkListener() {

			@Override
			public void hyperlinkUpdate(HyperlinkEvent hlEvent) {
				if (hlEvent.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
					// ProcessHandler.launchUrl(e.getURL().toString()); // roll your own link launcher or use Desktop if J6+

					try {
						Desktop.getDesktop().browse(new URI("https://developer.att.com/application-resource-optimizer/get-aro/download"));
					} catch (IOException ioExp) {
						// TODO Auto-generated catch block - send stack trace to log file
						ioExp.printStackTrace();
					} catch (URISyntaxException uriExp) {
						// TODO Auto-generated catch block - send stack trace to log file
						uriExp.printStackTrace();
					}

				}
			}
		});
		editPane.setEditable(false);
		editPane.setBackground(label.getBackground());

		JOptionPane pane = new JOptionPane(editPane
				, JOptionPane.CANCEL_OPTION
				, JOptionPane.OK_CANCEL_OPTION
				, Images.ICON.getIcon()
				, options
				, options[0]);
		JDialog dialog = pane.createDialog("Message");
		dialog.setModal(true);
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		dialog.setVisible(true);
		
		int response = 0;

		Object selectedValue = pane.getValue();

		if (selectedValue == null) {
			response = 1;
		}	
		
		for (int counter = 0, maxCounter = options.length; counter < maxCounter; counter++) {
			if (options[counter].equals(selectedValue)){

				response = counter;
				break;
			}
		}


		return Math.abs(response);
		
	}

}
