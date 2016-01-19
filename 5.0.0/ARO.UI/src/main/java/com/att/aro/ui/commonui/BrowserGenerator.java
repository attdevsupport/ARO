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


package com.att.aro.ui.commonui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

/**
 * Opens an instance of the web browser from inside the ARO application.
 */
public class BrowserGenerator {

	/**
	 * Opens the browser and displays the page specified by the URL.
	 * 
	 * @param url
	 *            - The URL for the page to be displayed in the browser.
	 * 
	 * @throws IOException
	 */
	public static void openBrowser(String url) throws IOException {
		Desktop.getDesktop().browse(URI.create(url));
	}

	/**
	 * Opens the browser and displays the page specified by the URL.
	 * 
	 * @param url
	 *            - The URI for the page to be displayed in the browser.
	 * 
	 * @throws IOException
	 */
	public static void openBrowser(URI url) throws IOException {
		Desktop.getDesktop().browse(url);
	}
}
