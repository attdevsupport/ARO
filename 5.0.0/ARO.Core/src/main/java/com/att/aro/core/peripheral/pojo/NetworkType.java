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
package com.att.aro.core.peripheral.pojo;

/**
 * The NetworkType Enumeration specifies constant values that identify recognized network types.
 */
public enum NetworkType {
	
	/**
	 * Identifies an unknown network type.
	 */
	none,
	
	/**
	 * Identifies the Wireless Fidelity (Wi-Fi) network.
	 */
	WIFI,

	/**
	 * Identifies the Enhanced Data Rates for GSM Evolution network.
	 */
	EDGE,
	
	/**
	 * Identifies the General Packet Radio Service (GPRS) network.
	 */
	GPRS,
	
	/**
	 * Identifies the Universal Mobile Telecommunications System (UMTS) network.
	 */
	UMTS,
	
	/**
	 * Identifies the Ethernet network.
	 */
	ETHERNET,
	
	/**
	 * Identifies the High-Speed Downlink Packet Access (HSDPA) network.
	 */
	HSDPA,
	
	/**
	 * Identifies the High-Speed Packet Access (HSPA) network.
	 */
	HSPA,
	
	/**
	 * Identifies the High-Speed Packet Access Plus (HSPA+) network.
	 */
	HSPAP,
	
	/**
	 * Identifies the High-Speed Uplink Packet Access (HSUPA) network.
	 */
	HSUPA,
	
	/**
	 * Identifies the Long Term Evolution (LTE) network.
	 */
	LTE
}
