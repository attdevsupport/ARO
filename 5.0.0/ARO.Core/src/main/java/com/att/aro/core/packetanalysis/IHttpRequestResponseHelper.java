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
package com.att.aro.core.packetanalysis;

import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.Session;

public interface IHttpRequestResponseHelper {
	boolean isSameContent(HttpRequestResponseInfo left, HttpRequestResponseInfo right, Session sessionLeft, Session sessionRight);
	long getActualByteCount(HttpRequestResponseInfo item, Session session);
	String getContentString(HttpRequestResponseInfo req, Session session) throws Exception;
	byte[] getContent(HttpRequestResponseInfo req, Session session) throws Exception;
	boolean isJavaScript(String contentType);
	boolean isCss(String contentType);
	boolean isHtml(String contentType);
	boolean isJSON(String contentType);
}
