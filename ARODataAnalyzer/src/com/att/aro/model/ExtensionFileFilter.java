/*
 *  Copyright 2012 AT&T
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
package com.att.aro.model;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * An extended abstract class that is used to filter files based on their types in in the 
 * JFileChooser. 
 * 
 * @see FileFilter
 */
public class ExtensionFileFilter extends FileFilter {

	/**
	 * description of the file filter.
	 */
	private String description;

	/**
	 * File types in String array.
	 */
	private String extensions[];

	/**
	 * Initializes an instance of the ExtensionFileFilter class using the specified extension and description.
	 * 
	 * @param description – The file filter description. 
	 * 
	 * @param extension – The file extension.
	 */
	public ExtensionFileFilter(String description, String extension) {
		this(description, new String[] { extension });
	}

	/**
	 * Initializes the ExtensionFileFilter class using the specified description, and an 
	 * array of file extensions.. 
	 * 
	 * @param description – The file filter description. 
	 * 
	 * @param extensions – An array of file extensions.
	 */
	public ExtensionFileFilter(String description, String extensions[]) {
		if (description == null) {
			this.description = extensions[0];
		} else {
			this.description = description;
		}
		this.extensions = (String[]) extensions.clone();
		toLower(this.extensions);
	}

	/**
	 * Changes the given string array items in to lower case letters.
	 * 
	 * @param array
	 *            values to be converted to lower case.
	 */
	private void toLower(String array[]) {
		for (int i = 0, n = array.length; i < n; i++) {
			array[i] = array[i].toLowerCase();
		}
	}

	/**
	 * Returns the filter type description. 
	 * 
	 * @return The description of the file filter.
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Returns a value indicating whether the specified file in valid. 
	 * 
	 * @return A Boolean value that is true if the file is valid, and is false if it isinvalid.
	 */
	@Override
	public boolean accept(File file) {
		if (file.isDirectory()) {
			return true;
		} else {
			String path = file.getAbsolutePath().toLowerCase();
			for (int i = 0, n = extensions.length; i < n; i++) {
				String extension = extensions[i];
				if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length()
						- 1)) == '.')) {
					return true;
				}
			}
		}
		return false;
	}
}
