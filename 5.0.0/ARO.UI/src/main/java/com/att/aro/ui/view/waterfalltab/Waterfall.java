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
package com.att.aro.ui.view.waterfalltab;

import com.att.aro.ui.utils.ResourceBundleHelper;

/**
 * @author Harikrishna Yaramachu
 *
 */
public enum Waterfall {

	BEFORE, DNS_LOOKUP, INITIAL_CONNECTION, SSL_NEGOTIATION, REQUEST_TIME, TIME_TO_FIRST_BYTE, CONTENT_DOWNLOAD, 
	AFTER, AFTER_3XX, AFTER_4XX, HTTP_3XX_REDIRECTION, HTTP_4XX_CLIENTERROR;

	@Override
	public String toString() {
		switch (this) {
		case DNS_LOOKUP : 
			return ResourceBundleHelper.getMessageString("waterfall.dnsLookup");
		case INITIAL_CONNECTION : 
			return ResourceBundleHelper.getMessageString("waterfall.initialConnection");
		case SSL_NEGOTIATION : 
			return ResourceBundleHelper.getMessageString("waterfall.sslNeg");
		case REQUEST_TIME : 
			return ResourceBundleHelper.getMessageString("waterfall.reqTime");
		case TIME_TO_FIRST_BYTE : 
			return ResourceBundleHelper.getMessageString("waterfall.firstByteTime");
		case CONTENT_DOWNLOAD : 
			return ResourceBundleHelper.getMessageString("waterfall.contentDownload");
		case HTTP_3XX_REDIRECTION : 
			return ResourceBundleHelper.getMessageString("waterfall.3xxResult");
		case HTTP_4XX_CLIENTERROR : 
			return ResourceBundleHelper.getMessageString("waterfall.4xxResult");
		default :
			return super.toString();
		}
	}	
}
