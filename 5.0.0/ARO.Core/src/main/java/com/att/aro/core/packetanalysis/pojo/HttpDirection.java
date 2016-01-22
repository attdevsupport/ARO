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
package com.att.aro.core.packetanalysis.pojo;

/**
 * The HttpRequestResponseInfo.Direction Enumeration specifies constant
 * values that describe the direction of an HTTP request/response. The
 * direction indicates whether an HttpRequestResponseInfo object contains a
 * request (up link) or a response (downlink). This enumeration is part of
 * the HttpRequestResponseInfo class.
 */
public enum HttpDirection {
	/**
	 * A Request traveling in the up link direction.
	 */
	REQUEST,
	/**
	 * A Response traveling in the down link direction.
	 */
	RESPONSE;
}
