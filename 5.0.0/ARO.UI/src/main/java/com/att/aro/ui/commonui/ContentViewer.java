package com.att.aro.ui.commonui;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.ui.model.ExtensionFileFilter;
import com.att.aro.ui.model.diagnostic.ContentException;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class ContentViewer {
	private static final ContentViewer instance = new ContentViewer();

	private static Map<HttpRequestResponseInfo, ContentViewFrame> viewerMap = 
			new HashMap<HttpRequestResponseInfo, ContentViewFrame>();
	private static String contentViewerDirectory;
	
	private IHttpRequestResponseHelper httpHelper = ContextAware.getAROConfigContext().getBean(IHttpRequestResponseHelper.class);
	private static final Logger LOGGER = Logger.getLogger(ContentViewer.class.getName());

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
	public void viewContent(Session session, HttpRequestResponseInfo httpReqResInfo)
			throws IOException,Exception {
		if (httpReqResInfo != null) {
			try {
				ContentViewFrame frame = viewerMap.get(httpReqResInfo);
				if (frame == null) {
					frame = new ContentViewFrame(session,httpReqResInfo);
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
				new MessageDialogFactory().showErrorDialog(null,
						ResourceBundleHelper.getMessageString("viewer.contentUnavailable"));
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
	public void saveContent(Component parent,Session session,
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
		if (contentType.contains(ResourceBundleHelper.getMessageString("fileChooser.contentType.image"))) {
			potentialFileTypes
					.add(ResourceBundleHelper.getMessageString("fileChooser.contentType.jpeg"));
			potentialFileTypes.add(ResourceBundleHelper.getMessageString("fileChooser.contentType.jpg"));
			potentialFileTypes.add(ResourceBundleHelper.getMessageString("fileChooser.contentType.png"));
			potentialFileTypes.add(ResourceBundleHelper.getMessageString("fileChooser.contentType.gif"));
		} else if (contentType.contains(ResourceBundleHelper.getMessageString("fileChooser.contentType.text"))
				|| contentType.contains(ResourceBundleHelper.getMessageString("fileChooser.contentType.html"))
				|| contentType.contains(ResourceBundleHelper.getMessageString("fileChooser.contentType.application"))) {
			potentialFileTypes.add(ResourceBundleHelper.getMessageString("fileChooser.contentType.css"));
			potentialFileTypes
					.add(ResourceBundleHelper.getMessageString("fileChooser.contentType.html"));
			potentialFileTypes.add(ResourceBundleHelper.getMessageString("fileChooser.contentType.xml"));
			potentialFileTypes.add(ResourceBundleHelper.getMessageString("fileChooser.contentType.js"));
			potentialFileTypes
					.add(ResourceBundleHelper.getMessageString("fileChooser.contentType.json"));
			potentialFileTypes.add(ResourceBundleHelper.getMessageString("fileChooser.contentType.txt"));
		}
		fc.setDialogTitle(ResourceBundleHelper.getMessageString("fileChooser.Title"));
		String fileName = "";
		String fileType = "";
		String strUri = "";
		if (httpReqResInfo.getAssocReqResp() != null) {
			URI uriInfo = httpReqResInfo.getAssocReqResp().getObjUri();
			strUri = uriInfo.getPath();
			fileName = getFileName(strUri);
			if (fileName != null && fileName.length() > 0) {
				int iLastDotIndex = fileName.lastIndexOf(ResourceBundleHelper.getMessageString("fileType.filters.dot"));
				if (iLastDotIndex >= 0) {
					fileType = fileName.substring(iLastDotIndex + 1);
					fileName = fileName
							.substring(
									0,
									(fileName.indexOf(ResourceBundleHelper.getMessageString("fileType.filters.dot")) == -1) ? fileName
											.length() : fileName.indexOf(ResourceBundleHelper.getMessageString("fileType.filters.dot")));
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
						+ ResourceBundleHelper.getMessageString("fileType.filters.dot")
						+ fileType.toLowerCase();
				fileDisplayType = ResourceBundleHelper.getMessageString(fileDisplayTypeKey);
				if (fileType.equalsIgnoreCase(ResourceBundleHelper.getMessageString("fileChooser.contentType.jpeg"))) {
					tempfileTypes[1] = ResourceBundleHelper.getMessageString("fileChooser.contentType.jpg");
					iMatchedFileTypes++;
				} else if (fileType.equalsIgnoreCase(ResourceBundleHelper.getMessageString("fileChooser.contentType.jpg"))) {
					tempfileTypes[1] = ResourceBundleHelper.getMessageString("fileChooser.contentType.jpeg");
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
				tempfileTypes[iMatchedFileTypes] = ResourceBundleHelper.getMessageString("fileChooser.contentType.html");
				fileType = ResourceBundleHelper.getMessageString("fileChooser.contentType.html");
				iMatchedFileTypes++;
			} else if (contentType.contains("application")) {
				if (contentType.contains("application/xml")
						|| (strUriLowerCase.contains(ResourceBundleHelper.getMessageString("fileChooser.contentType.xml")) || strObjNameLowerCase
								.contains("xml"))) {
					tempfileTypes[iMatchedFileTypes] = ResourceBundleHelper.getMessageString("fileChooser.contentType.xml");
					fileType = ResourceBundleHelper.getMessageString("fileChooser.contentType.xml");
					iMatchedFileTypes++;
				} else if (contentType.contains("application/json")
						|| strUriLowerCase.contains(ResourceBundleHelper.getMessageString("fileChooser.contentType.json"))) {
					tempfileTypes[iMatchedFileTypes] = ResourceBundleHelper.getMessageString("fileChooser.contentType.json");
					fileType = ResourceBundleHelper.getMessageString("fileChooser.contentType.json");
					iMatchedFileTypes++;
				} else if (contentType.contains("application/css")
						|| strUriLowerCase.contains(ResourceBundleHelper.getMessageString("fileChooser.contentType.css"))) {
					tempfileTypes[iMatchedFileTypes] = ResourceBundleHelper.getMessageString("fileChooser.contentType.css");
					fileType = ResourceBundleHelper.getMessageString("fileChooser.contentType.css");
					iMatchedFileTypes++;
				} else if (contentType.contains("text/javascript")
						|| contentType.contains("application/x-javascript")
						|| contentType.contains("application/x-javascript")
						|| strUriLowerCase.contains("json")) {
					tempfileTypes[iMatchedFileTypes] = ResourceBundleHelper.getMessageString("fileChooser.contentType.js");
					fileType = ResourceBundleHelper.getMessageString("fileChooser.contentType.js");
					iMatchedFileTypes++;
				} else {
					fileType = "txt";
					tempfileTypes[iMatchedFileTypes] = ResourceBundleHelper.getMessageString("fileChooser.contentType.txt");
					iMatchedFileTypes++;
				}
			} else if (contentType.contains("image")) {
				if (contentType.contains("image/jpeg")
						|| (strUriLowerCase.contains(ResourceBundleHelper.getMessageString("fileChooser.contentType.jpg")) || strObjNameLowerCase
								.contains("xml"))) {
					tempfileTypes[iMatchedFileTypes] = ResourceBundleHelper.getMessageString("fileChooser.contentType.jpg");
					fileType = ResourceBundleHelper.getMessageString("fileChooser.contentType.jpg");
					iMatchedFileTypes++;
				} else if (contentType.contains("image/png")
						|| (strUriLowerCase.contains(ResourceBundleHelper.getMessageString("fileChooser.contentType.png")) || strObjNameLowerCase
								.contains("xml"))) {
					tempfileTypes[iMatchedFileTypes] = ResourceBundleHelper.getMessageString("fileChooser.contentType.png");
					fileType = ResourceBundleHelper.getMessageString("fileChooser.contentType.png");
					iMatchedFileTypes++;
				} else if (contentType.contains("image/gif")
						|| (strUriLowerCase.contains(ResourceBundleHelper.getMessageString("fileChooser.contentType.gif")) || strObjNameLowerCase
								.contains("xml"))) {
					tempfileTypes[iMatchedFileTypes] = ResourceBundleHelper.getMessageString("fileChooser.contentType.gif");
					fileType = ResourceBundleHelper.getMessageString("fileChooser.contentType.gif");
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
			ExtensionFileFilter fileFilter = new ExtensionFileFilter(fileDisplayType,
					fileTypes);
			fc.setFileFilter(fileFilter);
		}
		tempfileTypes[0] = null;
		tempfileTypes[1] = null;
		tempfileTypes = null;

		// Set up file chooser
		fc.setApproveButtonText(ResourceBundleHelper.getMessageString("fileChooser.Save"));
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
						String fileTypeLowerCaseWithDot = ResourceBundleHelper.getMessageString("fileType.filters.dot")
								+ fileType.toLowerCase();
						if (!strFileLowerCase
								.endsWith(fileTypeLowerCaseWithDot)) {
							strFile += ResourceBundleHelper.getMessageString("fileType.filters.dot")
									+ fileType;
						}
					}
					contentFile = new File(strFile);
					boolean bAttemptToWriteToFile = true;
					if (contentFile.exists()) {
						if (MessageDialogFactory.showConfirmDialog(parent,
								MessageFormat.format(
										ResourceBundleHelper.getMessageString("fileChooser.fileExists"),
										contentFile.getAbsolutePath()), ResourceBundleHelper.getMessageString("fileChooser.confirm"),
								JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
							bAttemptToWriteToFile = false;
						}
					}
					if (bAttemptToWriteToFile) {
						try {
							 saveContentToFile(contentFile,session,httpReqResInfo);
							bSavedOrCancelled = true;
						} catch (Exception e) {
							
							 MessageDialogFactory .showMessageDialog(parent, MessageFormat.format(ResourceBundleHelper.getMessageString("fileChooser.errorWritingToFile"),contentFile.toString()));
		
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
		String strColon = ResourceBundleHelper.getMessageString("fileType.filters.colon");
		String strSemiColon = ResourceBundleHelper.getMessageString("fileType.filters.semiC");
		String strHash = ResourceBundleHelper.getMessageString("fileType.filters.hash");
		String strQuestion = ResourceBundleHelper.getMessageString("fileType.filters.question");
		String strForwardSlash = ResourceBundleHelper.getMessageString("fileType.filters.forwardSlash");
		String urlStripped = url
				.substring(url.lastIndexOf(ResourceBundleHelper.getMessageString("fileType.filters.forwardSlash")) + 1, url
						.length());
		if (urlStripped.contains(strColon)
				|| urlStripped.contains(strSemiColon)
				|| urlStripped.contains(strHash)
				|| urlStripped.contains(strQuestion)
				|| urlStripped.contains(strForwardSlash)) {
			// this removes the : at the end, if there is one
			url = url.substring(0, (url.indexOf(strColon) == -1) ? url.length()
					: url.indexOf(ResourceBundleHelper.getMessageString("fileType.filters.colon")));
			// this removes the ; at the end, if there is one
			url = url.substring(
					0,
					(url.indexOf(strSemiColon) == -1) ? url.length() : url
							.indexOf(ResourceBundleHelper.getMessageString("fileType.filters.semiC")));
			// this removes the anchor at the end, if there is one
			url = url.substring(0, (url.indexOf(strHash) == -1) ? url.length()
					: url.indexOf(ResourceBundleHelper.getMessageString("fileType.filters.hash")));
			// this removes the query after the file name, if there is one
			url = url
					.substring(
							0,
							(url.indexOf(strQuestion) == -1) ? url.length()
									: url.indexOf(ResourceBundleHelper.getMessageString("fileType.filters.question")));
			// this removes everything before the last slash in the path
			url = url.substring(url.lastIndexOf(strForwardSlash) + 1,
					url.length());
		} else {
			url = urlStripped;
		}
		return url;
	}
	/**
	 * Saves the binary content of the request/response body to the specified
	 * file.
	 * 
	 * @throws ContentException
	 *             - When part of content is not available.
	 */
	private void saveContentToFile(File file,Session session,HttpRequestResponseInfo rrInfo) throws Exception {

		if (rrInfo.getContentOffsetLength() != null) {
			FileOutputStream fos = new FileOutputStream(file);
			try {
				fos.write(httpHelper.getContent(rrInfo,session));
			} catch (ContentException exception) {
				LOGGER.log(Level.SEVERE, "Unexpected error creating request/response string", exception);

				// If we get a ContentException, just save the bytes we have
//				byte[] buffer = httpHelper.getStorageBuffer(rrInfo);
//				if (buffer == null) {
//					buffer = new byte[0];
//				}
//				for (Map.Entry<Integer, Integer> entry : rrInfo.getContentOffsetLength()
//						.entrySet()) {
//					int start = entry.getKey();
//					int len = Math.min(entry.getValue(), buffer.length - start);
//					fos.write(buffer, start, len);
//				}
			} finally {
				fos.close();
			}
		}
	}

}
