package com.att.aro.bp.fileorder;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.att.aro.model.HttpRequestResponseInfo;
import com.att.aro.model.HttpRequestResponseInfo.Direction;
import com.att.aro.model.TCPSession;

/**
 * Represents File Order Analysis
 * 
 */
public class FileOrderAnalysis {

	private static final Logger LOGGER = Logger
			.getLogger(FileOrderAnalysis.class.getName());

	private List<FileOrderEntry> results = new ArrayList<FileOrderEntry>();
	private int fileOrderCount = 0;
	private Elements css; // To store all the CSS elements present in HEAD
	private Elements js; // To store all the js elements present in HEAD

	public FileOrderAnalysis(List<TCPSession> tcpSessions) {
		for (TCPSession tcpSession : tcpSessions) {
			
			/*Taking this variable inside TCPSession FOR loop because after analyzing the content, 
			we should always reset lastRequestObj to null for a different TCP session.*/
			HttpRequestResponseInfo lastRequestObj = null;
			
			for (HttpRequestResponseInfo rr : tcpSession
					.getRequestResponseInfo()) {
				org.jsoup.nodes.Document doc = null;
				if (rr.getDirection() == Direction.RESPONSE) {
					doc = rr.parseHtml(this);
					if (doc != null) {
						if (checkFileOrderAnalysisResults(doc)) {
							results.add(new FileOrderEntry(rr, lastRequestObj));
						}
					}
				} else if (rr.getDirection() == Direction.REQUEST) {
					lastRequestObj = rr;
				}
			}
		}
	}

	/**
	 * Check element whether they are part of CSS or JS list created from the
	 * document.
	 * 
	 * */
	public boolean checkElementPresentInCSSJSList(Element elem) {
		for (Element elm : css) {
			if (elm.equals(elem)) {
				return true;
			}
		}
		for (Element elm : js) {
			if (elm.equals(elem)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Gets the parsed packet and checks any CSS files are getting downloaded
	 * after JS.
	 */
	public boolean checkFileOrderAnalysisResults(org.jsoup.nodes.Document doc) {

		String lastContentType = null;
		ArrayList<Element> docElements = new ArrayList<Element>(doc
				.getAllElements().size());

		// Extracting the elements in HEAD
		Elements headLink = doc.select("head");

		// Taking out the CSS and JS elements in HEAD
		css = headLink.select("link[href]");
		js = headLink.select("[src]");

		// Take out all the elements in HEAD to iterate in the above list
		Elements de = headLink.select("*");
		for (Element element : de) {
			// Checking whether the element has link and text/css to make sure
			// it is a CSS element.
			if ((checkElementPresentInCSSJSList(element))
					&& (element.tagName().equals("link"))
					&& (element.toString().contains("text/css"))) {
				LOGGER.log(Level.FINE, "CSS element" + element.toString());
				docElements.add(element);
				continue;
			}
			// Checking whether the element has link and text/css to make sure
			// it is a JS element.
			if ((checkElementPresentInCSSJSList(element))
					&& (element.hasAttr("src"))
					&& (element.tagName().equals("script"))) {
				LOGGER.log(Level.FINE,
						"Java Script element" + element.toString());
				docElements.add(element);
				continue;
			}
		}

		/*
		 * Updating the last content type after every iteration and checking it
		 * to make sure that no CSS files are loaded after a JS
		 */
		for (Element elem : docElements) {
			LOGGER.log(Level.FINE, "CSS/JavaScript element" + elem.toString());
			if ((elem.tagName().equals("link"))
					&& (elem.toString().contains("text/css"))) {
				if (lastContentType == null) {
					lastContentType = "css";
					continue;
				} else if (lastContentType.equals("javascript")) {
					incrementFileOrderCount();
					return true;
				} else {
					lastContentType = "css";
					continue;
				}
			}
			if ((elem.tagName().equals("script")) && (elem.hasAttr("src"))) {
				lastContentType = "javascript";
				continue;
			}
		}
		LOGGER.log(Level.FINE,
				"Packet parsing to find css and js elements completed");
		return false;
	}

	/**
	 * Increments the file order count
	 * 
	 */
	public void incrementFileOrderCount() {
		this.fileOrderCount++;
	}

	/**
	 * Returns the file order count
	 * 
	 */
	public int getFileOrderCount() {
		return fileOrderCount;
	}

	/**
	 * Returns an indicator whether the file order test has failed or not.
	 * 
	 * @return failed/success test indicator
	 */
	public boolean isTestFailed() {
		return (getFileOrderCount() > 0);
	}

	/**
	 * Returns a list of file order BP files
	 * 
	 * @return the results
	 */
	public List<FileOrderEntry> getResults() {
		return results;
	}
}
