/*
 *  Copyright 2015 AT&T
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

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JPanel;

/**
 * @author Nathan F Syfrig/Harikrishna Yaramachu
 * 
 * Opens an instance of the web browser from inside the ARO application.
 */
public class BrowserLauncher {

	/**
	 * Opens the browser and displays the page specified by the URL.
	 * 
	 * @param url
	 *            - The URL for the page to be displayed in the browser.
	 * @param panel - The panel used for exception handling display
	 * 
	 * @throws IOException
	 */
	public void launchURI(String uriString, JPanel panel) {
		try {
			launchURI(new URI(uriString), panel);
		} catch (URISyntaxException e) {
			new MessageDialogFactory().showUnexpectedExceptionDialog(
					panel, e);
		}
	}
	/**
	 * Convenience method for launchURI(uriString, null)
	 * 
	 * @param uriString
	 * @see #launchURI(String, JPanel)
	 * @see #launchURI(URI, JPanel)
	 * @see #launchURI(URI)
	 */
	public void launchURI(String uriString) {
		launchURI(uriString, null);
	}

	/**
	 * Opens the browser and displays the page specified by the URL.
	 * 
	 * @param uri
	 *            - The URI for the page to be displayed in the browser.
	 * @param panel - The panel used for exception handling display
	 * 
	 * @throws IOException
	 * @see #launchURI(URI)
	 * @see #launchURI(String, JPanel)
	 * @see #launchURI(String)
	 */
	public void launchURI(URI uri, JPanel panel) {
		try {
			Desktop.getDesktop().browse(uri);
		} catch (IOException e) {
			new MessageDialogFactory().showUnexpectedExceptionDialog(
					panel, e);
		}
	}
	/**
	 * Convenience method for launchURI(uri, null)
	 * 
	 * @param uriString
	 * @see #launchURI(URI, JPanel)
	 * @see #launchURI(String, JPanel)
	 * @see #launchURI(String)
	 */
	public void launchURI(URI uri) {
		launchURI(uri, null);
	}
}
