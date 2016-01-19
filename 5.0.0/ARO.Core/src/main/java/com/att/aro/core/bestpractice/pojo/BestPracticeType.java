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
package com.att.aro.core.bestpractice.pojo;

/**
 * BestPracticeType is an enumeration of best practice types.
 *
 */
public enum BestPracticeType {
	
	/*
	 * FILE DOWNLOAD SECTION
	 */
	
	/**
	 * FILE DOWNLOAD SECTION, Examine download data for compression efficiency
	 */
	FILE_COMPRESSION,
	/**
	 * FILE DOWNLOAD SECTION, Examine for duplicated downloads
	 */
	DUPLICATE_CONTENT,
	/**
	 * FILE DOWNLOAD SECTION, Examine for usage of cache
	 */
	USING_CACHE,
	/**
	 * FILE DOWNLOAD SECTION, Examine for good usage of caching downloads
	 */
	CACHE_CONTROL,
	/*
	 * FILE DOWNLOAD SECTION, Examine 
	 */
	//PREFETCHING, => removed
	/**
	 * FILE DOWNLOAD SECTION, Examine css and javascript request usage
	 */
	COMBINE_CS_JSS,
	/**
	 * FILE DOWNLOAD SECTION, Examine images for efficient sizing
	 */
	IMAGE_SIZE, 
	/**
	 * FILE DOWNLOAD SECTION, Examine by comparing re-compressed html, javascript, css, and json 
	 */
	MINIFICATION, 
	/**
	 * FILE DOWNLOAD SECTION, Examine sprite image usage
	 */
	SPRITEIMAGE,
	
	/* 
	 * CONNECTION SECTION
	 */
	
	/**
	 * CONNECTION SECTION, Currently only performs a BestPractice self test
	 */
	CONNECTION_OPENING, 
	/**
	 * CONNECTION SECTION, Examine burst activity for unnecessary connections
	 */
	UNNECESSARY_CONNECTIONS,
	/**
	 * CONNECTION SECTION, Examine burst activity for suspicious periodic activity(pinging).
	 */
	PERIODIC_TRANSFER, 
	/**
	 * CONNECTION SECTION, Examine for network activity after a screen rotation.
	 */
	SCREEN_ROTATION,
	/**
	 * CONNECTION SECTION, Examine network activity when connections are closing
	 */
	CONNECTION_CLOSING, 
	/*
	 * CONNECTION SECTION, Examine 
	 */
	//WIFI_OFFLOADING, => removed
	/**
	 * CONNECTION SECTION, Examine for Http 4xx/5xx errors
	 */
	HTTP_4XX_5XX, 
	/**
	 * CONNECTION SECTION, Examine for Http 3xx errors
	 */
	HTTP_3XX_CODE,
	/**
	 * CONNECTION SECTION, Examine 
	 */
	SCRIPTS_URL,
	
	/*
	 * HTML SECTION
	 */
	
	/**
	 * HTML SECTION, Look for Sync packets 
	 */
	ASYNC_CHECK, 
	/**
	 * HTML SECTION, Look for HTTP version 1.0. headers
	 */
	HTTP_1_0_USAGE, 
	/**
	 * HTML SECTION, Examine sequence of downloads ie. javascript, css
	 */
	FILE_ORDER, 
	/**
	 * HTML SECTION, Examine existence of empty URL's
	 */
	EMPTY_URL, 
	/**
	 * HTML SECTION, Examine flash usage
	 */
	FLASH, 
	/**
	 * HTML SECTION, Look for "display:none" in css 
	 */
	DISPLAY_NONE_IN_CSS,
	
	/*
	 * OTHER SECTION
	 */
	
	/**
	 * OTHER SECTION, Examine gps, bluetooth, camera usage above some limit
	 */
	ACCESSING_PERIPHERALS
}
