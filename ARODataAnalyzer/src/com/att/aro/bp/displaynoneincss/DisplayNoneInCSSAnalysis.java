package com.att.aro.bp.displaynoneincss;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.att.aro.model.ContentException;
import com.att.aro.model.HttpRequestResponseInfo;
import com.att.aro.model.HttpRequestResponseInfo.Direction;
import com.att.aro.model.TCPSession;

/**
 * 
 * Display:none in CSS Analysis.
 *
 */

public class DisplayNoneInCSSAnalysis {
	
	private static final Logger logger = Logger.getLogger(DisplayNoneInCSSAnalysis.class.getName());

	private List<DisplayNoneInCSSEntry> results = new ArrayList<DisplayNoneInCSSEntry>();
		
	public DisplayNoneInCSSAnalysis(List<TCPSession> tcpSessions) {
		for (TCPSession tcpSession : tcpSessions) {
			for (HttpRequestResponseInfo rr : tcpSession
					.getRequestResponseInfo()) {
				Document doc = null;
				String css;
				if ((rr.getDirection() == Direction.RESPONSE) && (rr.getContentType() != null)
						&& (rr.getContentType().equalsIgnoreCase("text/html"))) {
				
					doc = getParsedHtml(rr);
					if (doc != null) {
						if (checkIfDisplayNoneIsPresentInCSSembeddedInHTML(doc)) {
							results.add(new DisplayNoneInCSSEntry(rr));
						}
					}
				}
				else if(rr.getDirection() == Direction.RESPONSE && rr.getContentType() != null
						&& rr.getContentType().equalsIgnoreCase("text/css")){
					css = getCSS(rr);
					if (css != null) {
						if (checkIfDisplayNoneIsPresentInCSS(css)) {
							results.add(new DisplayNoneInCSSEntry(rr));
						}
					}
				}
			}
		}
	}
	
	/**
	 * Parse the HTML file using JSOUP
	 * @return Document, the parsed HTML file
	 */
	private Document getParsedHtml(HttpRequestResponseInfo rr){
		Document doc = null;
		String htmlContent = null;
		try {
			htmlContent = rr.getContentString();
		}catch (ContentException e) {
			logger.log(Level.FINE, "HTML content is not available.");
		} catch (IOException e) {
			logger.log(Level.SEVERE, "HTML content is not available.");
		}
		
		if (htmlContent != null){
			doc = Jsoup.parse(htmlContent);
		}
		if (doc != null)
			return doc;
		else 
			return null;
	}
	/**
	 * This method return the CSS file from the RR objects
	 * @return CSS file 
	 */
	private String getCSS(HttpRequestResponseInfo rr){
		
		String cssContent = null;
		try {
			cssContent = rr.getContentString();
		}catch (ContentException e) {
			logger.log(Level.SEVERE, "CSS content is not available.");
		} catch (IOException e) {
			logger.log(Level.SEVERE, "CSS content is not available.");
		}
		return cssContent;
	}

	/**
	 * This function checks for display:none in CSS embedded in HTML
	 * 
	 * @return true if found
	 */
	private boolean checkIfDisplayNoneIsPresentInCSSembeddedInHTML(Document doc) {
		/* Check HEAD for CSS rules*/
		Elements headLink = doc.select("head");
		/*Iterate the HEAD elements for CSS rules.*/
		Elements elements = headLink.select("*");
		for (Element element : elements) {
			/*Check if the CSS is embedded in the html */
			if ((element.tagName().equalsIgnoreCase("style"))
					&& (element.toString().contains("text/css"))) {
				if(element.text().contains("display:none")){
					//docElements.add(element);
					return true;
				}
			}
		}
		/* get all the html elements which contains inline CSS rules.*/
		Elements cssElements  = doc.select("[style*=display:none]");
		if(!cssElements.isEmpty())
			return true;
		
		return false;
	}
	
	/**
	 * This function checks for display:none in css files.
	 * 
	 * @return true if found
	 */
	private boolean checkIfDisplayNoneIsPresentInCSS(String css){
		
		if(css.contains("display:none")){
			return true;
		}
		return false;
		
	}
	
	/**
	 * Indicates whether the test passed or failed.
	 * 
	 * @return true if the test passed
	 */
	public boolean isTestPassed() {
		return (this.results.size() == 0);
	}
	
	/**
	 * Returns number of CSS files containing the display:none.
	 * 
	 * @return number of CSS files.
	 */
	public int getNumberOfCSSFilesWithDisplayNone() {
		return this.results.size();
	}

	/**
	 * Returns a list of files of type DisplayNoneInCSSEntry  
	 * 
	 * @return the results
	 */
	public List<DisplayNoneInCSSEntry> getResults() {
		return results;
	}
}
