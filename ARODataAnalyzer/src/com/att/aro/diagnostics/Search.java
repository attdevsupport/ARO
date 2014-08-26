/*
 * Copyright 2013 AT&T
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
package com.att.aro.diagnostics;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import com.att.aro.main.ApplicationResourceOptimizer;
import com.att.aro.model.ContentException;
import com.att.aro.model.HttpRequestResponseInfo;
import com.att.aro.model.TCPSession;
import com.att.aro.model.HttpRequestResponseInfo.Direction;
import com.att.aro.util.Util;

class Search extends JTextField {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(Search.class.getName());
	private static final int MIN_SIZE_TXT_STRING = 2;
	private static final boolean FIND_NEXT = true;
	private static final String TXT_FIELD_INIT_STRING = Util.RB.getString("element.search");
	private static final int TXT_FIELD_SIZE = 10;
	private static final DefaultHighlighter.DefaultHighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(
			new Color(0, 162, 232));
	private boolean firstCaretEvent = true;
	private ApplicationResourceOptimizer aro;
	private String searchString;
	private Iterator<TCPSession> tcpSessionIterator;
	private Iterator<HttpRequestResponseInfo> rrIterator;
	private TCPSession tcpSession;
	private HttpRequestResponseInfo rr;
	private SearchablePanel searchPanel;
	private JTextArea simpleTextArea;

	Search(ApplicationResourceOptimizer aro, SearchablePanel searchPanel) {
		this(aro, searchPanel, null);
	}
	
	Search(ApplicationResourceOptimizer aro, SearchablePanel searchPanel, JTextArea textArea) {
		super(TXT_FIELD_INIT_STRING, TXT_FIELD_SIZE);
		this.aro = aro;
		this.searchPanel = searchPanel;
		this.addCaretListener(getCaretListener());
		this.addMouseListener(getMouseListener());
		//if textArea is provided, it will be searched. null/default will search all HttpRequestResponseInfo
		this.simpleTextArea = textArea;
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
				// blank out the txt field the 1st time the event is received (mouse is clicked inside the field)
				// just in case he Caret Event would not catch the click
				if(firstCaretEvent) {
					LOGGER.log(Level.FINEST, "mouseClicked");
					JTextField textField = (JTextField) e.getComponent();
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
				
				// blank out the txt field the 1st time the event is received (mouse is clicked inside the field)
				if(firstCaretEvent) {
					blankOutTextField(arg);
					return;
				}
 				
				// get string typed into the search field
				String textToSearch = ((JTextField)arg.getSource()).getText(); 
				
				// do not search if only MIN_SIZE_TXT_STRING characters where typed into the search field
				if(textToSearch.length() >= MIN_SIZE_TXT_STRING) {
					doTextSearch(textToSearch, !FIND_NEXT);
				} else {
					if (simpleTextArea == null) {
						//deselect row on TCP Session search
						deselectRow();
					} else {
						//remove highlight on simple text area search
						simpleTextArea.getHighlighter().removeAllHighlights();
					}
					setFindNextButtonEnable(false);
				}
			}

			private void blankOutTextField(CaretEvent arg) {
				JTextField textField = ((JTextField)arg.getSource());
				if (textField.isEnabled()) {
					textField.setText(null);
					firstCaretEvent = false;
				}
			}
			
			private void deselectRow() {
				// deselect any row if it was previously selected
				aro.displayDiagnosticTab();
				aro.getAroAdvancedTab().resetHighlightedRequestResponse();
				//reset previously stored search string
				searchString = "";
			}
		};
	}
	
	void doNextSearch() {
		// continue with the previous search
		doTextSearch(searchString, FIND_NEXT);
	}
	
	private void doTextSearch(String text, boolean next) {
		LOGGER.log(Level.FINEST, "searching for: {0}", text);

		boolean foundIt = false;
		boolean findNext = next;
		
		// only search when the search string has changed or when search next was clicked
		if (text.equals(searchString) && !findNext) {
			return;
		}
		
		searchString = text;

		if (simpleTextArea == null) {
			foundIt = searchTcpSessions(text, findNext);
			if (!foundIt) {
				setFindNextButtonEnable(false);
				aro.getAroAdvancedTab().resetHighlightedRequestResponse();
				// the next search must start from the beginning 
			}
		} else {
			foundIt = searchTextArea(text, findNext);
			
			if (!foundIt && findNext) {
				//search from beginning???
				int selection = JOptionPane.showConfirmDialog(
						aro,
						Util.RB.getString("content.search.begin"), 
						Util.RB.getString("content.search.title"), 
						JOptionPane.YES_NO_OPTION, 
						JOptionPane.QUESTION_MESSAGE);
				
				if (selection == JOptionPane.YES_OPTION) {
					//search from beginning
					simpleTextArea.setCaretPosition(0);
					foundIt = searchTextArea(text, false);
				}
			}
		}

	}

	private boolean searchTextArea(String text, boolean findNext) {
		boolean foundIt = false;
		
		String areaContent = simpleTextArea.getText().toLowerCase();
		text = text.toLowerCase();
		
		int index = -1;
		if (findNext) {
			//from caret position
			index = areaContent.indexOf(text, simpleTextArea.getCaretPosition());
		} else {
			//from beginning
			index = areaContent.indexOf(text);
		}
		
		try {
			if (index > -1) {
				foundIt = true;
				simpleTextArea.getHighlighter().removeAllHighlights();
				int indexEnd = index + text.length();
				simpleTextArea.getHighlighter().addHighlight(index,
						indexEnd, highlightPainter);
				simpleTextArea.setCaretPosition(indexEnd);
				setFindNextButtonEnable(true);
				
				if (areaContent.indexOf(text, simpleTextArea.getCaretPosition()) == -1) {
					//no more matches, disable find next
					//setFindNextButtonEnable(false);
				}
				
			} else {
				if (!findNext) {
					simpleTextArea.getHighlighter().removeAllHighlights();
					simpleTextArea.setCaretPosition(0);
					setFindNextButtonEnable(false);
				}
			}
		} catch (BadLocationException e) {
			LOGGER.warning("Unable to highlight [" + text + "]");
		}
		    
		return foundIt;
	}
	
	private boolean searchTcpSessions(String text, boolean findNext) {
		boolean lookForNextOccurrence = true;
		boolean foundIt = false;
		
		// if this is a new search
		if (!findNext) {
			tcpSessionIterator =  aro.getAnalysisData().getTcpSessions().iterator();
			LOGGER.log(Level.FINEST, "New search");
		} else {
			LOGGER.log(Level.FINEST, "Find Next search");
		}
		
		
		// TCP session iteration
		while (hasNextTcpSession(findNext)) {
			
			if (!findNext) {
				tcpSession = (TCPSession) tcpSessionIterator.next();
				rrIterator = tcpSession.getRequestResponseInfo().iterator();
			}
			
			// Request/Response iteration
			while (hasNextRr(findNext)) {
				
				if (!findNext) {
					rr = rrIterator.next();
				}
				// to continue/iterate as usual the next time around
				findNext = false;
				
				try {
					// if found a match in the payload
					if (matchFound(rr, text)) {
						// look for a next occurrence of the string?
						if(lookForNextOccurrence) {
							LOGGER.log(Level.FINE, "found it, now looking for the next!");
							highlightRequestRespose(rr);
							setFindNextButtonEnable(false);
							//search for the next only once
							lookForNextOccurrence = false;
							foundIt = true;
						} else {
							LOGGER.log(Level.FINE, "found next, done searching");
							setFindNextButtonEnable(true);
							// return to allow Find Next function
							return foundIt;
						}
					}
				} catch (ContentException e) {
					// nothing can be done here if the content is not available 
					LOGGER.log(Level.FINE, "Search - Unexpected Exception {0}", e.getMessage());
				} catch (IOException e) {
					// nothing can be done here in case of IO Exception 
					LOGGER.log(Level.FINE, "Search - Unexpected Exception {0}", e.getMessage());
				}
			} // END:  Request/Response iteration
		} // END: TCP session iteration
		
		return foundIt;
	}
	
	private boolean matchFound(HttpRequestResponseInfo rrInfo, String text) throws ContentException, IOException {
		
		// if found a match in the payload
		if (rrInfo.getContentLength() != 0 && rrInfo.getContentString().indexOf(text) != -1) {
			LOGGER.log(Level.FINE, "found a match in the payload");
			return true;
		// else if found in the headers 	
		} else if (rrInfo.getAllHeaders() != null && rrInfo.getAllHeaders().indexOf(text) != -1) {
			LOGGER.log(Level.FINE, "found in the headers");
			return true;
		// else if found in request/response request/status line	
		} else if (rrInfo.getDirection() == Direction.REQUEST &&
				   rrInfo.getStatusLine() != null &&
				   rrInfo.getStatusLine().indexOf(text) != -1) {
			LOGGER.log(Level.FINE, "found in request/response request/status line");
			return true;
		}
		return false;
	}

	private boolean hasNextRr(boolean findNext) {
		if(findNext) {
			return true;
		} else {		
			return rrIterator.hasNext();
		}
	}

	private boolean hasNextTcpSession(boolean findNext) {
		if(findNext) {
			return true;
		} else {		
			return tcpSessionIterator.hasNext();
		}
	}

	private void highlightRequestRespose(HttpRequestResponseInfo foundRr) {
		aro.displayDiagnosticTab();
		// highlight the line
		aro.getAroAdvancedTab().setHighlightedRequestResponse(foundRr);
	}
	
	private void setFindNextButtonEnable(boolean enable) {
		this.searchPanel.getSearchNextButton().setEnabled(enable);
	}


}
