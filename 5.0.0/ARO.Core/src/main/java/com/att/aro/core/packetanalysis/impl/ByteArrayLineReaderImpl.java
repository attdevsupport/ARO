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
package com.att.aro.core.packetanalysis.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.att.aro.core.packetanalysis.IByteArrayLineReader;


/**
 * Class to encapsulate a byte[] for the retrieval of Strings
 */
public class ByteArrayLineReaderImpl implements IByteArrayLineReader {
	
	private int length;
	private byte[] byteArray;
	private int index;
	
	/**
	 * Instantiate a byte[] for retrieval of strings
	 * @param data
	 */
	public void init(byte[] data) {
		this.byteArray = data;
		index = 0;
		length = this.byteArray.length;
	}

	/**
	 * 
	 * @param skipAmount
	 */
	public void skipContent(int skipAmount) {
		index = Math.min(length, index + skipAmount);
		if (index <0){
			index = length;
		}
		
	}

	/**
	 * 
	 * @param count
	 */
	public void skipForward(int count) {
		index += count;
		if (index >= length){
			index = length - 1;
		}
	}

	/**
	 * Reads from input stream keeping counter of how much has been read
	 * 
	 * @return
	 * @throws IOException
	 */
	private int readInput() {
		int result;
		if (index < length) {
			result = byteArray[index];
			++index;
		} else {
			result = -1;
		}
		return result;
	}

	/**
	 * Read a line of text from the HTTP request/response stream. 
	 * Line terminations recognized by crlf (\r\n)
	 * 
	 * @param byteArray
	 *            the request/response stream
	 * @return Next line of text in stream or null if end of stream reached
	 * @throws IOException
	 */
	public String readLine() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		int num;
		try {

			// Look for CRLF
			while ((num = readInput()) != -1) {
				if (num == '\r') {
					num = readInput();
					if (num == '\n') {

						// Return found line of text
						return new String(output.toByteArray(), "UTF-8");
					} else {
						output.write('\r');
						output.write(num);
					}
				} else {
					output.write(num);
				}
			}

			// End of stream
			return output.size() > 0 ? new String(output.toByteArray(), "UTF-8") : null;
		} finally {
			output.close();
		}
	}

	public int getIndex() {
		return index;
	}

	public void setArrayIndex(int arrayIndex) {
		this.index = arrayIndex;
	}

	@Override
	public String toString() {
		try {
			String response = "index :" + index + "\n";
			return response + (byteArray != null ? new String(byteArray, "UTF-8") : "null");
		} catch (UnsupportedEncodingException e) {
			return "UnsupportedEncodingException :"+e.getMessage();
		}
	}

}
