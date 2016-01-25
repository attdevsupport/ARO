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

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.model.ContentException;
import com.att.aro.model.ExtensionFileFilter;
import com.att.aro.model.HttpRequestResponseInfo;
import com.att.aro.util.Util;

/**
 * Contains methods for managing the viewing and saving of downloaded content.
 */
public class ContentViewer {

	private static final ContentViewer instance = new ContentViewer();
	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();
	private static Map<HttpRequestResponseInfo, ContentViewFrame> viewerMap = new HashMap<HttpRequestResponseInfo, ContentViewFrame>();
	private static String contentViewerDirectory;

	/**
	 * Returns an instance of the ContentViewer.
	 * 
	 * @return ContentViewer The ContentViewer instance.
	 */
	public static ContentViewer getInstance() {
		return instance;
	}

	/**
	 * Private constructor. Use getInstance()
	 */
	private ContentViewer() {

	}

	/**
	 * Displays the content when the View button is clicked.
	 * 
	 * @param httpReqResInfo
	 *            - The HttpRequestResponseInfo object containing the content to
	 *            be viewed.
	 * @see HttpRequestResponseInfo
	 */
	public synchronized void viewContent(HttpRequestResponseInfo httpReqResInfo)
			throws IOException {
		if (httpReqResInfo != null) {
			try {
				ContentViewFrame frame = viewerMap.get(httpReqResInfo);
				if (frame == null) {
					frame = new ContentViewFrame(httpReqResInfo);
					frame.addWindowListener(new WindowAdapter() {

						@Override
						public void windowClosing(WindowEvent e) {
							viewerMap.remove(((ContentViewFrame) e.getWindow())
									.getRrInfo());
						}

					});
					viewerMap.put(httpReqResInfo, frame);
				}
				frame.setState(Frame.NORMAL);
				frame.setVisible(true);
			} catch (ContentException e) {
				MessageDialogFactory.showErrorDialog(null,
						rb.getString("viewer.contentUnavailable"));
			}
		}
	}

	/**
	 * Saves the specified content in a file at the location selected in the
	 * FileChooser dialog.
	 * 
	 * @param parent
	 *            - The Panel that invokes the FileChooser dialog.
	 * @param httpReqResInfo
	 *            - The HttpRequestResponseInfo object containing the content to
	 *            be saved.
	 */
	public void saveContent(Component parent,
			HttpRequestResponseInfo httpReqResInfo) {
		String contentType = httpReqResInfo.getContentType();
		if (contentType == null) {
			contentType = "";
		}
		
		JFileChooser fc = new JFileChooser();
		if (contentViewerDirectory != null) {
			fc.setCurrentDirectory(new File(contentViewerDirectory));
		}
		ArrayList<String> potentialFileTypes = new ArrayList<String>();
		if (contentType.contains(rb.getString("fileChooser.contentType.image"))) {
			potentialFileTypes
					.add(rb.getString("fileChooser.contentType.jpeg"));
			potentialFileTypes.add(rb.getString("fileChooser.contentType.jpg"));
			potentialFileTypes.add(rb.getString("fileChooser.contentType.png"));
			potentialFileTypes.add(rb.getString("fileChooser.contentType.gif"));
		} else if (contentType.contains(rb
				.getString("fileChooser.contentType.text"))
				|| contentType.contains(rb
						.getString("fileChooser.contentType.html"))
				|| contentType.contains(rb
						.getString("fileChooser.contentType.application"))) {
			potentialFileTypes.add(rb.getString("fileChooser.contentType.css"));
			potentialFileTypes
					.add(rb.getString("fileChooser.contentType.html"));
			potentialFileTypes.add(rb.getString("fileChooser.contentType.xml"));
			potentialFileTypes.add(rb.getString("fileChooser.contentType.js"));
			potentialFileTypes
					.add(rb.getString("fileChooser.contentType.json"));
			potentialFileTypes.add(rb.getString("fileChooser.contentType.txt"));
		}
		fc.setDialogTitle(rb.getString("fileChooser.Title"));
		String fileName = "";
		String fileType = "";
		String strUri = "";
		if (httpReqResInfo.getAssocReqResp() != null) {
			URI uriInfo = httpReqResInfo.getAssocReqResp().getObjUri();
			strUri = uriInfo.getPath();
			fileName = getFileName(strUri);
			if (fileName != null && fileName.length() > 0) {
				int iLastDotIndex = fileName.lastIndexOf(rb
						.getString("fileType.filters.dot"));
				if (iLastDotIndex >= 0) {
					fileType = fileName.substring(iLastDotIndex + 1);
					fileName = fileName
							.substring(
									0,
									(fileName.indexOf(rb
											.getString("fileType.filters.dot")) == -1) ? fileName
											.length() : fileName.indexOf(rb
											.getString("fileType.filters.dot")));
				}
			}
		}

		// Set up file types
		String fileDisplayType = null;
		String[] tempfileTypes = new String[2];
		int iMatchedFileTypes = 0;
		for (int i = 0; i < potentialFileTypes.size(); i++) {
			String strPotentialFileType = potentialFileTypes.get(i);
			if (fileType.equalsIgnoreCase(strPotentialFileType)) {
				iMatchedFileTypes++;
				tempfileTypes[0] = fileType;
				String fileDisplayTypeKey = "fileChooser.contentDisplayType"
						+ rb.getString("fileType.filters.dot")
						+ fileType.toLowerCase();
				fileDisplayType = rb.getString(fileDisplayTypeKey);
				if (fileType.equalsIgnoreCase(rb
						.getString("fileChooser.contentType.jpeg"))) {
					tempfileTypes[1] = rb
							.getString("fileChooser.contentType.jpg");
					iMatchedFileTypes++;
				} else if (fileType.equalsIgnoreCase(rb
						.getString("fileChooser.contentType.jpg"))) {
					tempfileTypes[1] = rb
							.getString("fileChooser.contentType.jpeg");
					iMatchedFileTypes++;
				}
				break;
			}
		}

		if (iMatchedFileTypes == 0) {
			String strUriLowerCase = strUri.toLowerCase();
			String strObjNameLowerCase = httpReqResInfo.getAssocReqResp()
					.getObjName().toLowerCase();
			if (contentType.contains("text/html")
					|| strUriLowerCase.contains("html")) {
				tempfileTypes[iMatchedFileTypes] = rb
						.getString("fileChooser.contentType.html");
				fileType = rb.getString("fileChooser.contentType.html");
				iMatchedFileTypes++;
			} else if (contentType.contains("application")) {
				if (contentType.contains("application/xml")
						|| (strUriLowerCase.contains(rb
								.getString("fileChooser.contentType.xml")) || strObjNameLowerCase
								.contains("xml"))) {
					tempfileTypes[iMatchedFileTypes] = rb
							.getString("fileChooser.contentType.xml");
					fileType = rb.getString("fileChooser.contentType.xml");
					iMatchedFileTypes++;
				} else if (contentType.contains("application/json")
						|| strUriLowerCase.contains(rb
								.getString("fileChooser.contentType.json"))) {
					tempfileTypes[iMatchedFileTypes] = rb
							.getString("fileChooser.contentType.json");
					fileType = rb.getString("fileChooser.contentType.json");
					iMatchedFileTypes++;
				} else if (contentType.contains("application/css")
						|| strUriLowerCase.contains(rb
								.getString("fileChooser.contentType.css"))) {
					tempfileTypes[iMatchedFileTypes] = rb
							.getString("fileChooser.contentType.css");
					fileType = rb.getString("fileChooser.contentType.css");
					iMatchedFileTypes++;
				} else if (contentType.contains("text/javascript")
						|| contentType.contains("application/x-javascript")
						|| contentType.contains("application/x-javascript")
						|| strUriLowerCase.contains("json")) {
					tempfileTypes[iMatchedFileTypes] = rb
							.getString("fileChooser.contentType.js");
					fileType = rb.getString("fileChooser.contentType.js");
					iMatchedFileTypes++;
				} else {
					fileType = "txt";
					tempfileTypes[iMatchedFileTypes] = rb
							.getString("fileChooser.contentType.txt");
					iMatchedFileTypes++;
				}
			} else if (contentType.contains("image")) {
				if (contentType.contains("image/jpeg")
						|| (strUriLowerCase.contains(rb
								.getString("fileChooser.contentType.jpg")) || strObjNameLowerCase
								.contains("xml"))) {
					tempfileTypes[iMatchedFileTypes] = rb
							.getString("fileChooser.contentType.jpg");
					fileType = rb.getString("fileChooser.contentType.jpg");
					iMatchedFileTypes++;
				} else if (contentType.contains("image/png")
						|| (strUriLowerCase.contains(rb
								.getString("fileChooser.contentType.png")) || strObjNameLowerCase
								.contains("xml"))) {
					tempfileTypes[iMatchedFileTypes] = rb
							.getString("fileChooser.contentType.png");
					fileType = rb.getString("fileChooser.contentType.png");
					iMatchedFileTypes++;
				} else if (contentType.contains("image/gif")
						|| (strUriLowerCase.contains(rb
								.getString("fileChooser.contentType.gif")) || strObjNameLowerCase
								.contains("xml"))) {
					tempfileTypes[iMatchedFileTypes] = rb
							.getString("fileChooser.contentType.gif");
					fileType = rb.getString("fileChooser.contentType.gif");
					iMatchedFileTypes++;
				}

			}
		}

		// Set up filters
		fc.addChoosableFileFilter(fc.getAcceptAllFileFilter());
		if (iMatchedFileTypes > 0) {
			String[] fileTypes = new String[iMatchedFileTypes];
			fileTypes[0] = tempfileTypes[0];
			if (iMatchedFileTypes > 1) {
				fileTypes[1] = tempfileTypes[1];
			}
			FileFilter fileFilter = new ExtensionFileFilter(fileDisplayType,
					fileTypes);
			fc.setFileFilter(fileFilter);
		}
		tempfileTypes[0] = null;
		tempfileTypes[1] = null;
		tempfileTypes = null;

		// Set up file chooser
		fc.setApproveButtonText(rb.getString("fileChooser.Save"));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		File contentFile = new File(fileName);
		fc.setSelectedFile(contentFile);

		boolean bSavedOrCancelled = false;
		while (!bSavedOrCancelled) {
			if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
				String strFile = fc.getSelectedFile().toString();
				if (strFile.length() > 0) {
					// Save current directory
					contentViewerDirectory = fc.getCurrentDirectory().getPath();
					String strFileLowerCase = strFile.toLowerCase();
					if ((fileType != null) && (fileType.length() > 0)) {
						String fileTypeLowerCaseWithDot = rb
								.getString("fileType.filters.dot")
								+ fileType.toLowerCase();
						if (!strFileLowerCase
								.endsWith(fileTypeLowerCaseWithDot)) {
							strFile += rb.getString("fileType.filters.dot")
									+ fileType;
						}
					}
					contentFile = new File(strFile);
					boolean bAttemptToWriteToFile = true;
					if (contentFile.exists()) {
						if (MessageDialogFactory.showConfirmDialog(parent,
								MessageFormat.format(
										rb.getString("fileChooser.fileExists"),
										contentFile.getAbsolutePath()), rb
										.getString("fileChooser.confirm"),
								JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
							bAttemptToWriteToFile = false;
						}
					}
					if (bAttemptToWriteToFile) {
						try {
							httpReqResInfo.saveContentToFile(contentFile);
							bSavedOrCancelled = true;
						} catch (IOException e) {
							
							MessageDialogFactory.showMessageDialog(parent, MessageFormat.format(Util.RB.getString("fileChooser.errorWritingToFile"),contentFile.toString()));
		
						}
					}
				}
			} else {
				bSavedOrCancelled = true;
			}
		}
		// }
	}

	/**
	 * Returns the file name from the uri found in the req/res object.
	 */
	private String getFileName(String URIPath) {
		// this gets the full url
		String url = URIPath;
		// this removes everything before the last slash in the path
		String strColon = rb.getString("fileType.filters.colon");
		String strSemiColon = rb.getString("fileType.filters.semiC");
		String strHash = rb.getString("fileType.filters.hash");
		String strQuestion = rb.getString("fileType.filters.question");
		String strForwardSlash = rb.getString("fileType.filters.forwardSlash");
		String urlStripped = url
				.substring(url.lastIndexOf(rb
						.getString("fileType.filters.forwardSlash")) + 1, url
						.length());
		if (urlStripped.contains(strColon)
				|| urlStripped.contains(strSemiColon)
				|| urlStripped.contains(strHash)
				|| urlStripped.contains(strQuestion)
				|| urlStripped.contains(strForwardSlash)) {
			// this removes the : at the end, if there is one
			url = url.substring(0, (url.indexOf(strColon) == -1) ? url.length()
					: url.indexOf(rb.getString("fileType.filters.colon")));
			// this removes the ; at the end, if there is one
			url = url.substring(
					0,
					(url.indexOf(strSemiColon) == -1) ? url.length() : url
							.indexOf(rb.getString("fileType.filters.semiC")));
			// this removes the anchor at the end, if there is one
			url = url.substring(0, (url.indexOf(strHash) == -1) ? url.length()
					: url.indexOf(rb.getString("fileType.filters.hash")));
			// this removes the query after the file name, if there is one
			url = url
					.substring(
							0,
							(url.indexOf(strQuestion) == -1) ? url.length()
									: url.indexOf(rb
											.getString("fileType.filters.question")));
			// this removes everything before the last slash in the path
			url = url.substring(url.lastIndexOf(strForwardSlash) + 1,
					url.length());
		} else {
			url = urlStripped;
		}
		return url;
	}
}
