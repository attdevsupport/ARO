/**
 *  Copyright 2016 AT&T
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.zip.GZIPInputStream;
import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.Session;

/**
 * helper class for dealing HttpRequestResponseInfo object
 */
public class HttpRequestResponseHelperImpl implements IHttpRequestResponseHelper {
	//@InjectLogger
	//private static ILogger log;
	
	/**
	 * Indicates whether the content type is CSS or not.
	 * 
	 * The following content types are considered as CSS:
	 * 
	 * - text/css
	 * 
	 * @return returns true if the content type is CSS otherwise return false
	 * 
	 */
	public boolean isCss(String contentType) {
		return "text/css".equals(contentType);
	}
	
	/**
	 * Indicates whether the content type is HTML or not.
	 * 
	 * The following content types are considered as HTML:
	 * 
	 * - text/html
	 * 
	 * @return returns true if the content type is HTML otherwise return false
	 * 
	 */
	public boolean isHtml(String contentType) {
		return "text/html".equals(contentType);
	}

	public boolean isJSON(String contentType) {
		return "application/json".equals(contentType);
	}
	/**
	 * Indicates whether the content type is JavaScript or not.
	 * 
	 * The following content types are considered as JavaScript:
	 * 
	 * - application/ecmascript
	 * - application/javascript
	 * - text/javascript
	 * 
	 * @return returns true if the content type is JavaScript otherwise return false
	 * 
	 */
	public boolean isJavaScript(String contentType) {

		return ("application/ecmascript".equals(contentType) ||
			"application/javascript".equals(contentType) ||
			"text/javascript".equals(contentType));
	}
	/**
	 * Returns the request/response body as a text string. The returned text may
	 * not be readable.
	 * 
	 * @return The content of the request/response body as a string, or null if
	 *         the method does not execute successfully.
	 * 
	 * @throws ContentException
	 *             - When part of the content is not available.
	 */
	public String getContentString(HttpRequestResponseInfo req, Session session) throws Exception {
		byte[] content = getContent(req, session);
		return content != null ? new String(content, "UTF-8") : null;
	}
	/**
	 * get cotent of the request/response in byte[]
	 * @param req
	 * @return byte array
	 * @throws Exception 
	 */
	public byte[] getContent(HttpRequestResponseInfo req, Session session) throws Exception{
		SortedMap<Integer, Integer> contentOffsetLength = req.getContentOffsetLength();
		String contentEncoding = req.getContentEncoding();
		if (contentOffsetLength != null) {
			byte[] buffer = getStorageBuffer(req, session);
			if (buffer == null) {
				return new byte[0];
			}
			
			ByteArrayOutputStream output = null;
			for (Map.Entry<Integer, Integer> entry : contentOffsetLength
					.entrySet()) {
				int start = entry.getKey();
				int size = entry.getValue();
				if( start + size < 0) {
				       throw new Exception("The content may be too big.");
				} else if (buffer.length < start + size) {
				       throw new Exception("The content may be corrupted.");
				}

				for (int i = start; i < start + size; ++i) {
				    if (output == null) {
				        output = new ByteArrayOutputStream((int) getActualByteCount(req, session));
				    }
					output.write(buffer[i]);
				}
			}
			if ("gzip".equals(contentEncoding) && output != null) {

				// Decompress gzipped content
				GZIPInputStream gzip=null;
				try{
					gzip = new GZIPInputStream(
							new ByteArrayInputStream(output.toByteArray()));
					output.reset();
					buffer = new byte[2048];
					int len;
					while ((len = gzip.read(buffer)) >= 0) {
						output.write(buffer, 0, len);
					}
				}catch(IOException ioe){
					if (gzip != null) {
						try{
							gzip.close();
						}catch (IOException ex){
							throw ex;
						}
					}
					if (output != null) {
						try{
							output.close();
						}catch (IOException ex){
							throw ex;
						}
					}
				}
			}
			if (output != null) {
			    return output.toByteArray();
			} else {
			    return new byte[0];
			}
		}
		return new byte[0];
	}
	/**
	 * Determines whether the same content is contained in this request/response as in
	 * the specified request/response
	 * @param right The request to compare to
	 * @return true if the content is the same
	 */
	public boolean isSameContent(HttpRequestResponseInfo left, HttpRequestResponseInfo right, Session session, Session sessionRight) {
		// Check specified content length
		if (left.getContentLength() > 0 && left.getContentLength() != right.getContentLength()) {
			return false;
		}
		boolean yes = true;
		long leftcount = getActualByteCount(left, session);
		long rightcount = getActualByteCount(right, sessionRight);
		if(leftcount == rightcount){
			
			if(leftcount == 0){
				return true;
			}
			
			// Otherwise do byte by byte compare
			byte[] bufferLeft = getStorageBuffer(left, session);
			byte[] bufferRight = getStorageBuffer(right, sessionRight);
			
			Iterator<Map.Entry<Integer, Integer>> itleft = left.getContentOffsetLength().entrySet().iterator();
			Iterator<Map.Entry<Integer, Integer>> itright = right.getContentOffsetLength().entrySet().iterator();
			int indexLeft = 0;
			int stopLeft = 0;
			int indexRight = 0;
			int stopRight = 0;
			if(itleft.hasNext() && itright.hasNext()){
				Map.Entry<Integer, Integer> entryLeft = itleft.next();
				Map.Entry<Integer, Integer> entryRight = itright.next();
				indexLeft = entryLeft.getKey();
				stopLeft = indexLeft + entryLeft.getValue();
				indexRight = entryRight.getKey();
				stopRight = entryRight.getValue();
				do{
					if(bufferLeft[indexLeft] != bufferRight[indexRight]){
						return false;
					}
					++indexLeft;
					++indexRight;
					if(indexLeft >= bufferLeft.length || indexRight >= bufferRight.length){
						break;
					}
					if(indexLeft >= stopLeft){
						if(itleft.hasNext()){
							entryLeft = itleft.next();
							indexLeft = entryLeft.getKey();
							stopLeft = indexLeft + entryLeft.getValue();
						}else{
							break;
						}
					}
					if(indexRight >= stopRight){
						if(itright.hasNext()){
							entryRight = itright.next();
							indexRight = entryRight.getKey();
							stopRight = entryRight.getValue();
						}else{
							break;
						}
					}
				}while(true);
			}
			yes = true;
		}else{
			yes = false;
		}
		return yes;
	}
	/**
	 * Gets the number of bytes in the request/response body. The actual byte
	 * count.
	 * 
	 * @return The total number of bytes in the request/response body. If
	 *         contentOffsetLength is null, then this method returns 0.
	 */
	public long getActualByteCount(HttpRequestResponseInfo item, Session session) {
		if (item.getContentOffsetLength() != null) {

			byte[] buffer = getStorageBuffer(item, session);
			int bufferSize = buffer != null ? buffer.length : 0;

			long result = 0;
			for (Map.Entry<Integer, Integer> entry : item.getContentOffsetLength().entrySet()) {
				int start = entry.getKey();
				int size = entry.getValue();
				if (bufferSize < start + size) {

					// Only include what was actually downloaded.
					size = bufferSize - start;
				}
				result += size;
			}
			return result;
		} else {
			return 0;
		}
	}
	/**
	 * Convenience method that gets the storage array in the session where this request/
	 * response is located.
	 * @return
	 */
	private byte[] getStorageBuffer(HttpRequestResponseInfo req, Session session) {
		switch (req.getPacketDirection()) {
		case DOWNLINK:
			return session.getStorageDl();
		case UPLINK:
			return session.getStorageUl();
		default:
			return null;
		}

	}
}//end class
