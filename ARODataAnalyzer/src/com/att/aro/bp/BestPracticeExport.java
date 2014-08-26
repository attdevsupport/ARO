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
package com.att.aro.bp;

import java.io.IOException;
import java.io.Writer;

/**
 * Represents additional data that can be exported with a best practice
 */
public class BestPracticeExport {

	public static final String COMMA = ",";
	public static final String lINE = System.getProperty("line.separator");

	/**
	 * Utility method that write a string in quoted CSV format to the specified
	 * output.  The string is enclosed in double quotes and all double quotes
	 * within the string are escaped.
	 * @param writer The output where string is written
	 * @param s The string to write
	 * @throws IOException when a write error occurs
	 */
	public static void writeValue(Writer writer, String s) throws IOException {
		writer.write('"');
		if (s != null) {
			for (char c : s.toCharArray()) {
				if (c == '"') {
					writer.write("\"\"");
				} else {
					writer.write(c);
				}
			}
		}
		writer.write('"');
	}

	private String value;
	private String unitsDescription;

	/**
	 * Constructor that initializes export data
	 * @param value a value that is included in the best practice export
	 * @param unitsDescription a string that contains units or a description
	 * of the value
	 */
	public BestPracticeExport(String value, String unitsDescription) {
		this.value = value;
		this.unitsDescription = unitsDescription;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return the unitsDescription
	 */
	public String getUnitsDescription() {
		return unitsDescription;
	}

	/**
	 * Exports this value to the specified writer as CSV output
	 * @param writer The output writer
	 * @throws IOException when a write exception occurs
	 */
	public void write(Writer writer) throws IOException {
		writer.write(COMMA);
		writeValue(writer, value);
		writer.write(COMMA);
		writeValue(writer, unitsDescription);
		writer.write(lINE);
	}
	
}
