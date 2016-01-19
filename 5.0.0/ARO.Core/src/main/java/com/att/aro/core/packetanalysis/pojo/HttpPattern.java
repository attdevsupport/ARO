/*
 *  Copyright 2014 AT&T
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
package com.att.aro.core.packetanalysis.pojo;

import java.util.regex.Pattern;

public class HttpPattern{
	public static Pattern strReRequestType = Pattern.compile("(\\S*)\\s* \\s*(\\S*)\\s* \\s*(HTTP/1\\.[0|1]|RTSP/1\\.[0|1])");
	public static Pattern strReRequestHost = Pattern.compile("[H|h]ost:");
	public static Pattern strReResponseContentLength = Pattern.compile("[C|c]ontent-[L|l]ength:");
	public static Pattern strReTransferEncoding = Pattern.compile("[T|t]ransfer-[E|e]ncoding:");
	public static Pattern strReResponseContentType = Pattern.compile("[C|c]ontent-[T|t]ype:");
	public static Pattern strReResponseContentEncoding = Pattern.compile("[C|c]ontent-[E|e]ncoding:");
	public static Pattern strReResponseResults = Pattern.compile("(HTTP/1\\.[0|1]|RTSP/1\\.[0|1])\\s* \\s*(\\d++)\\s* \\s*(.*)");
	public static Pattern strReResponseEtag = Pattern.compile("E[T|t]ag\\s*:\\s*(W/)?\"(.*)\"");
	public static Pattern strReResponseAge = Pattern.compile("Age\\s*:\\s*(\\d*)");
	public static Pattern strReResponseExpires = Pattern.compile("Expires\\s*:(.*)");
	public static Pattern strReResponseReferer = Pattern.compile("Referer\\s*:(.*)");
	public static Pattern strReResponseLastMod = Pattern.compile("Last-Modified\\s*:(.*)");
	public static Pattern strReResponseDate = Pattern.compile("Date\\s*:(.*)");
	public static Pattern strReResponsePragmaNoCache = Pattern.compile("Pragma\\s*:\\s*no-cache");
	public static Pattern strReResponseCacheControl = Pattern.compile("Cache-Control\\s*:(.*)");
	public static Pattern strReCacheMaxAge = Pattern.compile("max-age\\s*=\\s*(\\d++)");
	public static Pattern strReCacheSMaxAge = Pattern.compile("s-maxage\\s*=\\s*(\\d++)");
	public static Pattern strReCacheMinFresh = Pattern.compile("min-fresh\\s*=\\s*(\\d++)");
	public static Pattern strReCacheMaxStale = Pattern.compile("max-stale\\s*(?:=\\s*(\\d*))?");
	public static Pattern strReContentRange = Pattern.compile("Content-Range\\s*:\\s*bytes (\\d*)\\s*-\\s*(\\d*)\\s*/\\s*(\\d*)");
	public static Pattern strReIfModifiedSince = Pattern.compile("If-Modified-Since\\s*:");
	public static Pattern strReIfNoneMatch = Pattern.compile("If-None-Match\\s*:");

}