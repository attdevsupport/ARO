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
package com.att.aro.ui.view.diagnostictab;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import com.att.aro.core.ILogger;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class SearchTextField extends JTextField {

	private static final long serialVersionUID = 1L;
	private ILogger logger = ContextAware.getAROConfigContext().getBean(
			ILogger.class);
 

	private static final int MIN_SIZE_TXT_STRING = 2;
	private static final boolean FIND_NEXT = true;
	private static final int TXT_FIELD_SIZE = 10;
	private static final DefaultHighlighter.DefaultHighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(
			new Color(0, 162, 232));
	private boolean firstCaretEvent = true;
	private String searchString;
 
	private JTextArea simpleTextArea;

	private ContentViewJPanel jpanel;
	
	SearchTextField(ContentViewJPanel searchPanel, JTextArea textArea) {
		super(ResourceBundleHelper.getMessageString("element.search"), TXT_FIELD_SIZE);
		jpanel = searchPanel;
		simpleTextArea = textArea;
		addCaretListener(getCaretListener());
		addMouseListener(getMouseListener());
	}

	private MouseListener getMouseListener() {
		return new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				// blank out the txt field the 1st time the event is received
				// (mouse is clicked inside the field)
				// just in case he Caret Event would not catch the click
				if (firstCaretEvent) {
					logger.info("mouseClicked");
					SearchTextField textField = (SearchTextField) e.getComponent();
					if (textField.isEnabled()) {
						textField.setText(null);
						firstCaretEvent = false;
					}
				}
			}
		};
	}

	private CaretListener getCaretListener() {
		return new CaretListener() {

			@Override
			public void caretUpdate(CaretEvent arg) {

				// blank out the txt field the 1st time the event is received
				// (mouse is clicked inside the field)
				if (firstCaretEvent) {
					blankOutTextField(arg);
					return;
				}

				// get string typed into the search field
				String textToSearch = ((JTextField) arg.getSource()).getText();

				// do not search if only MIN_SIZE_TXT_STRING characters where
				// typed into the search field
				if (textToSearch.length() >= MIN_SIZE_TXT_STRING) {
					doTextSearch(textToSearch, !FIND_NEXT);
				} else {
					if (simpleTextArea == null) {
						// deselect row on TCP Session search
						deselectRow();
					} else {
						// remove highlight on simple text area search
						simpleTextArea.getHighlighter().removeAllHighlights();
					}
					setFindNextButtonEnable(false);
				}
			}

			private void blankOutTextField(CaretEvent arg) {
				JTextField textField = ((JTextField) arg.getSource());
				if (textField.isEnabled()) {
					textField.setText(null);
					firstCaretEvent = false;
				}
			}

			private void deselectRow() {
				// reset previously stored search string
				searchString = "";
			}
		};
	}

	void doNextSearch() {
		// continue with the previous search
		doTextSearch(searchString, FIND_NEXT);
	}

	private void doTextSearch(String text, boolean next) {
		logger.info("searching for: " + text);

		boolean foundIt = false;
		boolean findNext = next;

		// only search when the search string has changed or when search next
		// was clicked
		if (text.equals(searchString) && !findNext) {
			return;
		}

		searchString = text;

		foundIt = searchTextArea(text, findNext);

		if (!foundIt && findNext) {
			// search from beginning???
			int selection = JOptionPane.showConfirmDialog(this,
					ResourceBundleHelper
							.getMessageString("content.search.begin"),
					ResourceBundleHelper
							.getMessageString("content.search.title"),
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (selection == JOptionPane.YES_OPTION) {
				// search from beginning
				simpleTextArea.setCaretPosition(0);
				foundIt = searchTextArea(text, false);
			}
		}

	}

	private boolean searchTextArea(String text, boolean findNext) {
		boolean foundIt = false;

		String areaContent = simpleTextArea.getText().toLowerCase();
		text = text.toLowerCase();

		int index = -1;
		if (findNext) {
			// from caret position
			index = areaContent
					.indexOf(text, simpleTextArea.getCaretPosition());
		} else {
			// from beginning
			index = areaContent.indexOf(text);
		}

		try {
			if (index > -1) {
				foundIt = true;
				simpleTextArea.getHighlighter().removeAllHighlights();
				int indexEnd = index + text.length();
				simpleTextArea.getHighlighter().addHighlight(index, indexEnd,
						highlightPainter);
				simpleTextArea.setCaretPosition(indexEnd);
				setFindNextButtonEnable(true);

				if (areaContent
						.indexOf(text, simpleTextArea.getCaretPosition()) == -1) {
					// no more matches, disable find next
					// setFindNextButtonEnable(false);
				}

			} else {
				if (!findNext) {
					simpleTextArea.getHighlighter().removeAllHighlights();
					simpleTextArea.setCaretPosition(0);
					setFindNextButtonEnable(false);
				}
			}
		} catch (BadLocationException e) {
			logger.error("Unable to highlight [" + text + "]");
		}

		return foundIt;
	}

	private void setFindNextButtonEnable(boolean enable) {
		jpanel.getSearchNextButton().setEnabled(enable);
	}
}
