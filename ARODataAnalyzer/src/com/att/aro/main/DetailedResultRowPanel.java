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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.images.Images;
import com.att.aro.model.BestPractices;
import com.att.aro.model.TraceData;

/**
 * Represents a row in the Best Practices tab. Each row represents one Best
 * Practices test which has a category ( Caching, Connections, or Other), an
 * icon, a test name, an About line, and Results details.
 */
public abstract class DetailedResultRowPanel {

	private static ImageIcon passIcon = Images.BP_PASS_DARK.getIcon();
	private static ImageIcon failIcon = Images.BP_FAIL_DARK.getIcon();
	private static ImageIcon notRunIcon = Images.BP_SELFTEST_TRIGGERED
			.getIcon();
	private static ImageIcon manualIcon = Images.BP_MANUAL.getIcon();
	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();
	private String PASS = rb.getString("bestPractice.tooltip.pass");
	private String FAIL = rb.getString("bestPractice.tooltip.fail");
	private String MANUAL = rb.getString("bestPractice.tooltip.manual");

	private static Font textFont = new Font("TextFont", Font.PLAIN, 12);
	// private Font titleFont = new Font("TITLEFont", Font.BOLD, 18);
	// private Font pageFont = new Font("TITLEFont", Font.BOLD, 14);
	private static Font boldTextFont = new Font("BoldTextFont", Font.BOLD, 12);

	private JLabel iconLabel;
	private JLabel testLabel;
	private JLabel testNameLabel;
	private JLabel aboutLabel;
	private JTextPane aboutText;
	// private HyperlinkLabel learnMoreLabel;
	private JLabel resultLabel;
	private JTextPane resultDetailsLabel;
	private boolean selfTest;
	private TraceData.Analysis analysisData;
	private Window parent;

	/**
	 * Initializes a new instance of the DetailedResultRowPanel class using the
	 * specified self-test flag, test title, test description, and url for more
	 * information.
	 * 
	 * @param isSelf
	 *            – A boolean that indicates whether or not the test is a
	 *            self-test. A self-test is a test that is not automatically run
	 *            by the ARO Data Analyzer.
	 * 
	 * @param bpTitle
	 *            - The title of the Best Practice test.
	 * 
	 * @param bpDesc
	 *            – A description of the Best Practice test.
	 * 
	 * @param bpUrl
	 *            – A url to a site containing more information about the test.
	 */
	public DetailedResultRowPanel(Window parent, boolean isSelfTest,
			String bpTitle, String bpDesc, String bpUrl) {
		this.parent = parent;
		this.selfTest = isSelfTest;
		createDetailedResultRow(isSelfTest, bpTitle, bpDesc, bpUrl);
	}

	protected abstract boolean isPass(BestPractices bp);

	protected abstract String resultText(TraceData.Analysis analysisData);

	protected abstract void performAction();

	/**
	 * Refreshes the icons and sets the Results text using the specified trace
	 * data.
	 * 
	 * @param analysisData
	 *            – An Analysis object containing the trace data.
	 * 
	 */
	public void refresh(TraceData.Analysis analysisData) {
		this.analysisData = analysisData;

		if (analysisData == null) {
			iconLabel.setIcon(notRunIcon);
			iconLabel.setToolTipText(null);
			resultDetailsLabel.setText(selfTest ? resultText(null) : null);
		} else if (selfTest) {
			iconLabel.setIcon(manualIcon);
			iconLabel.setToolTipText(MANUAL);
		} else {
			if (isPass(analysisData.getBestPractice())) {
				iconLabel.setIcon(passIcon);
				iconLabel.setToolTipText(PASS);
				resultDetailsLabel.setText(resultText(analysisData));
			} else {
				iconLabel.setIcon(failIcon);
				iconLabel.setToolTipText(FAIL);
				resultDetailsLabel.setText(resultText(analysisData));
			}
		}
	}

	/**
	 * Returns the pass/fail/self-test icon for the best practice test.
	 * 
	 * @return A JLabel object containing the icon.
	 */
	public JLabel getIconLabel() {
		return iconLabel;
	}

	/**
	 * Returns the Results text for the best practice test.
	 * 
	 * @return A JLabel object containing the Results text.
	 */
	public JTextPane getResultDetailsLabel() {
		return resultDetailsLabel;
	}

	/**
	 * Returns the Test label.
	 * 
	 * @return A JLabel object containing the test label.
	 */
	public JLabel getTestLabel() {
		return testLabel;
	}

	/**
	 * Returns the name of the best practice test.
	 * 
	 * @return A JLabel object containing the best practice test name.
	 */
	public JLabel getTestNameLabel() {
		return testNameLabel;
	}

	/**
	 * Returns the About label.
	 * 
	 * @return A JLabel object containing the about label .
	 */
	public JLabel getAboutLabel() {
		return aboutLabel;
	}

	/**
	 * Returns the Result label.
	 * 
	 * @return A JLabel object containing the result label.
	 */
	public JTextPane getAboutText() {
		return aboutText;
	}

	/**
	 * Returns the results label.
	 * 
	 * @return A JLabel object containing the Results label.
	 */
	public JLabel getResultLabel() {
		return resultLabel;
	}

	/**
	 * Creates a Best Practices row in a particular category i.e. Caching ,
	 * Connections or others Each row contains 3 icons , Test , About and
	 * Results details for the Best Practice.
	 */
	private void createDetailedResultRow(boolean isSelfTest, String bpTitle,
			String bpDesc, final String bpUrl) {

		this.iconLabel = new JLabel(notRunIcon);

		this.testLabel = new JLabel(rb.getString("bestPractices.test"));
		testLabel.setFont(boldTextFont);

		this.testNameLabel = new JLabel(bpTitle);
		testNameLabel.setFont(boldTextFont);

		this.aboutLabel = new JLabel(rb.getString("bestPractices.About"));
		aboutLabel.setFont(boldTextFont);

		this.aboutText = createJTextArea(bpDesc, bpUrl);

		resultLabel = new JLabel(
				isSelfTest ? rb.getString("bestPractices.selfEvaluation")
						: rb.getString("bestPractices.results"));
		resultLabel.setFont(boldTextFont);

		this.resultDetailsLabel = createJTextPane();
		resultDetailsLabel.setText(isSelfTest ? resultText(null) : null);
		resultDetailsLabel.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (analysisData != null
						&& !isPass(analysisData.getBestPractice())) {
					performAction();
				}
			}

		});
	}

	/**
	 * This method creates the about text area.
	 */
	private JTextPane createJTextArea(String textToDisplay, final String url) {
		HTMLDocument doc = new HTMLDocument();
		StyleSheet style = doc.getStyleSheet();
		style.addRule("body { font-family: " + textFont.getFamily() + "; "
				+ "font-size: " + textFont.getSize() + "pt; }");
		style.addRule("a { text-decoration: underline; font-weight:bold; }");
		JTextPane jTextArea = new JTextPane(doc);
		jTextArea.setEditable(false);
		jTextArea.setEditorKit(new HTMLEditorKit());
		jTextArea.setStyledDocument(doc);
		jTextArea.setMargin(new Insets(0, 0, 0, 0));
		jTextArea.setPreferredSize(new Dimension(500, 70));
		jTextArea.setText(textToDisplay + " <a href=\"#\">"
				+ rb.getString("bestPractices.learnMore") + "</a>");
		jTextArea.addHyperlinkListener(new HyperlinkListener() {

			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {

				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						BrowserGenerator.openBrowser(url);
					} catch (IOException e1) {
						MessageDialogFactory.showUnexpectedExceptionDialog(
								parent, e1);
					}
				}
			}

		});
		return jTextArea;
	}

	private JTextPane createJTextPane() {
		HTMLDocument doc = new HTMLDocument();
		StyleSheet style = doc.getStyleSheet();
		style.addRule("body { font-family: " + textFont.getFamily() + "; "
				+ "font-size: " + textFont.getSize() + "pt; }");
		style.addRule("a { text-decoration: underline }");
		JTextPane jTextArea = new JTextPane(doc);
		jTextArea.setEditable(false);
		jTextArea.setEditorKit(new HTMLEditorKit());
		jTextArea.setStyledDocument(doc);
		jTextArea.setMargin(new Insets(0, 0, 0, 0));
		jTextArea.setPreferredSize(new Dimension(500, 50));
		return jTextArea;
	}

}
