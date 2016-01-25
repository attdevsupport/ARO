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
package com.att.aro.ui.model.diagnostic;

import java.awt.Color;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.ui.commonui.ContextAware;

//this is search function class
public class SearchHelper {
	
	private static final Logger LOGGER = Logger.getLogger(SearchHelper.class.getName());
	private static final DefaultHighlighter.DefaultHighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(
			new Color(0, 162, 232));
 	private String searchString;
	private Iterator<HttpRequestResponseInfo> rrIterator;
	private HttpRequestResponseInfo rr;
	private JTextArea simpleTextArea;

	private IHttpRequestResponseHelper rrHelper = ContextAware.getAROConfigContext().getBean(IHttpRequestResponseHelper.class);

	/**
	 * focus on the current session search
	 * @param session
	 * @throws Exception
	 */
	public void doNextSearch(Session session,JTextArea simpleTextArea, boolean find_next ) throws Exception {
		// continue with the previous search
		this.simpleTextArea = simpleTextArea;
		doTextSearch(searchString, find_next, session);
	}
	
	private void doTextSearch(String text, boolean next,Session session) throws Exception {
		LOGGER.log(Level.FINEST, "searching for: {0}", text);

		boolean foundIt = false;
		boolean findNext = next;
		
		// only search when the search string has changed or when search next was clicked
		if (text.equals(searchString) && !findNext) {
			return;
		}
		
		searchString = text;

		if (simpleTextArea == null) {
			foundIt = searchTcpSessions(text, findNext, session);
			if (!foundIt) {
//				setFindNextButtonEnable(false);
//				aro.getAroAdvancedTab().resetHighlightedRequestResponse();
				// the next search must start from the beginning 
			}
		} else {
			foundIt = searchTextArea(text, findNext);
			
			if (!foundIt && findNext) {

					simpleTextArea.setCaretPosition(0);
					foundIt = searchTextArea(text, false);
				
			}
		}

	}
	private boolean searchTcpSessions(String text, boolean findNext,Session session) throws Exception {
		boolean lookForNextOccurrence = true;
		boolean foundIt = false;
 
 
			while (hasNextRr(findNext)) {
				
				if (!findNext) {
					rr = rrIterator.next();
				}
				// to continue/iterate as usual the next time around
				findNext = false;
				
				try {
					// if found a match in the payload
					if (matchFound(session,rr, text)) {
						// look for a next occurrence of the string?
						if(lookForNextOccurrence) {
							LOGGER.log(Level.FINE, "found it, now looking for the next!");
//							highlightRequestRespose(rr);
//							setFindNextButtonEnable(false);
							//search for the next only once
							lookForNextOccurrence = false;
							foundIt = true;
						} else {
							LOGGER.log(Level.FINE, "found next, done searching");
//							setFindNextButtonEnable(true);
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
 		
		return foundIt;
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
//				setFindNextButtonEnable(true);
				

				
			} else {
				if (!findNext) {
					simpleTextArea.getHighlighter().removeAllHighlights();
					simpleTextArea.setCaretPosition(0);
//					setFindNextButtonEnable(false);
				}
			}
		} catch (BadLocationException e) {
			LOGGER.warning("Unable to highlight [" + text + "]");
		}
		    
		return foundIt;
	}
	
	
	private boolean matchFound(Session session, HttpRequestResponseInfo rrInfo, String text) throws Exception {
		
		// if found a match in the payload
		if (rrInfo.getContentLength() != 0 && rrHelper.getContentString(rrInfo,session).indexOf(text) != -1) {
			LOGGER.log(Level.FINE, "found a match in the payload");
			return true;
		// else if found in the headers 	
		} else if (rrInfo.getAllHeaders() != null && rrInfo.getAllHeaders().indexOf(text) != -1) {
			LOGGER.log(Level.FINE, "found in the headers");
			return true;
		// else if found in request/response request/status line	
		} else if (rrInfo.getDirection() == HttpDirection.REQUEST &&
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
	
	

}
