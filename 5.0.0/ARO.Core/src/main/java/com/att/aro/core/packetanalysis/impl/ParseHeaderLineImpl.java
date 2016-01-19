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
package com.att.aro.core.packetanalysis.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.IParseHeaderLine;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpPattern;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.util.Util;

public class ParseHeaderLineImpl implements IParseHeaderLine{
	
	@InjectLogger
	private static ILogger logger;
	
	private static final String CHARSET = "charset";
	private static final String CHUNKED = "chunked";
	private static final String NOCACHE = "no-cache";
	private static final String NOSTORE = "no-store";
	private static final String PUBLIC = "public";
	private static final String PRIVATE = "private";
	private static final String MUSTREVALIDATE = "must-revalidate";
	private static final String PROXYREVALIDATE = "proxy-revalidate";
	private static final String ONLYIFCACHED = "only-if-cached";

	private static final Object HEADERS_SEPARATOR = " ";
	
	
	/**
	 * Parse data from the line of text
	 * 
	 * @param headerLine
	 * @param rrInfo
	 */
	public void parseHeaderLine(String headerLine, HttpRequestResponseInfo rrInfo) {

		appendHeaderToHttpRequestResponseInfo(headerLine, rrInfo);

		Matcher matcher;
		String[] str;

		// Get request host "[H|h]ost:"
		matcher = HttpPattern.strReRequestHost.matcher(headerLine);
		if (matcher.lookingAt()) {
			String hostName = headerLine.substring(matcher.end()).trim();

			// Strip port info if included
			int index = hostName.indexOf(':');
			if (index >= 0) {
				hostName = hostName.substring(0, index);
			}
			rrInfo.setHostName(hostName);
			return;
		}

		// Get request content length "[C|c]ontent-[L|l]ength:"
		matcher = HttpPattern.strReResponseContentLength.matcher(headerLine);
		if (matcher.lookingAt() && rrInfo.getContentLength() == 0) {
			try {
				rrInfo.setContentLength(Integer.parseInt(headerLine.substring(matcher.end()).trim()));
			} catch (NumberFormatException e) {
				/*
				 * The value exceeds the Interger.MAX_VALUE i.e
				 * 2^31-1=2147483647
				 */
				logger.info("Cannot parse the string to int for contentLength,because" + " The value to parse is :" + (headerLine.substring(matcher.end()).trim())
						+ " which is greater than the Integer.MAX_VALUE (2^31-1=2147483647).");
			}

			return;
		}

		// Get request transfer encoding "[T|t]ransfer-[E|e]ncoding:"
		matcher = HttpPattern.strReTransferEncoding.matcher(headerLine);
		if (matcher.lookingAt()) {
			rrInfo.setChunked(CHUNKED.equals(headerLine.substring(matcher.end()).trim()));
			return;
		}

		// Get request transfer encoding
		matcher = HttpPattern.strReResponseContentEncoding.matcher(headerLine);
		if (matcher.lookingAt()) {
			rrInfo.setContentEncoding(headerLine.substring(matcher.end()).trim().toLowerCase());
			return;
		}

		// Get content type
		matcher = HttpPattern.strReResponseContentType.matcher(headerLine);
		if (matcher.lookingAt()) {
			str = headerLine.substring(matcher.end()).trim().split(";");
			rrInfo.setContentType(str[0].trim().toLowerCase());
			for (int i = 1; i < str.length; ++i) {
				int index = str[i].indexOf('=');
				if (index >= 0) {
					String attr = str[i].substring(0, index).trim();
					if (CHARSET.equals(attr)) {
						rrInfo.setCharset(str[i].substring(index+1).trim());
					}
				}
			}
			return;
		}

		// Date
		matcher = HttpPattern.strReResponseDate.matcher(headerLine);
		if (matcher.lookingAt()) {
			rrInfo.setDate(Util.readHttpDate(matcher.group(1), false));
			return;
		}

		// Pragma: no-cache
		matcher = HttpPattern.strReResponsePragmaNoCache.matcher(headerLine);
		if (matcher.lookingAt()) {
			rrInfo.setHasCacheHeaders(true);
			rrInfo.setPragmaNoCache(true);
			return;
		}

		// Cache-Control
		matcher = HttpPattern.strReResponseCacheControl.matcher(headerLine);
		if (matcher.lookingAt()) {
			str = matcher.group(1).split(",");
			if (str.length > 0) {
				rrInfo.setHasCacheHeaders(true);
			}
			for (int i = 0; i < str.length; ++i) {
				String directive = str[i].trim();
				if (NOCACHE.equals(directive)) {
					rrInfo.setNoCache(true);
					continue;
				} else if (NOSTORE.equals(directive)) {
					rrInfo.setNoStore(true);
					continue;
				}

				// max-age
				matcher = HttpPattern.strReCacheMaxAge.matcher(directive);
				if (matcher.lookingAt()) {
					rrInfo.setMaxAge(Long.valueOf(matcher.group(1)));
					continue;
				}

				if (rrInfo.getDirection() == HttpDirection.REQUEST) {
					if (ONLYIFCACHED.equals(directive)) { // only-if-cached
						rrInfo.setOnlyIfCached(true);
						continue;
					}

					// min-fresh
					matcher = HttpPattern.strReCacheMinFresh.matcher(directive);
					if (matcher.lookingAt()) {
						rrInfo.setMinFresh(Long.valueOf(matcher.group(1)));
						continue;
					}

					// max-stale
					matcher = HttpPattern.strReCacheMaxStale.matcher(directive);
					if (matcher.lookingAt()) {
						rrInfo.setMaxStale(matcher.group(1) != null ? Long.valueOf(matcher.group(1)) : Long.MAX_VALUE);
						continue;
					}

				} else if (rrInfo.getDirection() == HttpDirection.RESPONSE) {
					if (PUBLIC.equals(directive)) {
						rrInfo.setPublicCache(true);
						continue;
					} else if (PRIVATE.equals(directive)) {
						rrInfo.setPrivateCache(true);
						continue;
					} else if (MUSTREVALIDATE.equals(directive)) {
						rrInfo.setMustRevalidate(true);
						continue;
					} else if (PROXYREVALIDATE.equals(directive)) {
						rrInfo.setProxyRevalidate(true);
						continue;
					}

					// s-maxage
					matcher = HttpPattern.strReCacheSMaxAge.matcher(directive);
					if (matcher.lookingAt()) {
						rrInfo.setsMaxAge(Long.valueOf(matcher.group(1)));
						continue;
					}

				}
			}
			return;
		}

		if (rrInfo.getDirection() == HttpDirection.RESPONSE) {

			// ETag
			matcher = HttpPattern.strReResponseEtag.matcher(headerLine);
			if (matcher.lookingAt()) {
				rrInfo.setEtag(matcher.group(2));
				return;
			}

			// Age
			matcher = HttpPattern.strReResponseAge.matcher(headerLine);
			if (matcher.lookingAt()) {
				rrInfo.setAge(Long.valueOf(matcher.group(1)));
				return;
			}

			// Expires
			matcher = HttpPattern.strReResponseExpires.matcher(headerLine);
			if (matcher.lookingAt()) {
				rrInfo.setExpires(Util.readHttpDate(matcher.group(1), true));
				return;
			}

			// Last modified
			matcher = HttpPattern.strReResponseLastMod.matcher(headerLine);
			if (matcher.lookingAt()) {
				rrInfo.setLastModified(Util.readHttpDate(matcher.group(1), false));
				return;
			}

			// Content-Range
			matcher = HttpPattern.strReContentRange.matcher(headerLine);
			if (matcher.lookingAt()) {
				rrInfo.setRangeResponse(true);
				rrInfo.setRangeFirst(Integer.parseInt(matcher.group(1)));
				try {
					rrInfo.setRangeLast(Integer.parseInt(matcher.group(2)));
				} catch (NumberFormatException e) {
					/*
					 * The value exceeds the Interger.MAX_VALUE i.e
					 * 2^31-1=2147483647. Continue.
					 */
					logger.info("Cannot parse the string to int for rangeLast,because" + " The value to parse is :" + matcher.group(2)
							+ " which is greater than the Integer.MAX_VALUE (2^31-1=2147483647).");

				}
				rrInfo.setRangeFull(Long.parseLong(matcher.group(3)));

				if (rrInfo.getContentLength() == 0) {
					rrInfo.setContentLength(rrInfo.getRangeLast() - rrInfo.getRangeFirst() + 1);
				}
				return;
			}

		} else if (rrInfo.getDirection() == HttpDirection.REQUEST) {

			// Referer
			matcher = HttpPattern.strReResponseReferer.matcher(headerLine);
			if (matcher.lookingAt()) {
				try {
					rrInfo.setReferrer(new URI(matcher.group(1).trim()));
				} catch (URISyntaxException e) {
					logger.warn("Invalid referrer URI: " + matcher.group(1));
				}
				return;
			}

			// If-Modified-Since
			matcher = HttpPattern.strReIfModifiedSince.matcher(headerLine);
			if (matcher.lookingAt()) {
				rrInfo.setIfModifiedSince(true);
				return;
			}

			// If-None-Match
			matcher = HttpPattern.strReIfNoneMatch.matcher(headerLine);
			if (matcher.lookingAt()) {
				rrInfo.setIfNoneMatch(true);
				return;
			}
		}
	}

	private void appendHeaderToHttpRequestResponseInfo(String line, HttpRequestResponseInfo rrInfo) {
		if (rrInfo != null) {
			StringBuilder headersBuilder;
			if (rrInfo.getAllHeaders() == null) {
				headersBuilder = new StringBuilder();
			} else {
				headersBuilder = new StringBuilder(rrInfo.getAllHeaders());
			}
			headersBuilder.append(HEADERS_SEPARATOR).append(line);
			rrInfo.setAllHeaders(headersBuilder.toString());
		}
	}

}