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

/**
 * The RRCState Enumeration specifies constant values that describe the valid Radio Resource Control (RRC) states. 
 */
public enum RRCState {
	/**
	 * The Idle state. 
	 */
	STATE_IDLE,
	/**
	 *  The Direct Channel (DCH) active state. 
	 */
	STATE_DCH,
	/**
	 *  The Forward Access Channel (FACH) active state. 
	 */
	STATE_FACH,
	/**
	 *  The Direct Channel (DCH) tail state. 
	 */
	TAIL_DCH, 
	/**
	 *  The Forward Access Channel (FACH) tail state.
	 */
	TAIL_FACH,
	/**
	 * Promotion from the Idle to Direct Channel (DCH) state. 
	 */
	PROMO_IDLE_DCH, 
	/**
	 *  Promotion from the Forward Access (FACH) to Direct Channel (DCH) state. 
	 */
	PROMO_FACH_DCH,
	
	/**
	 * The idle state for LTE.
	 */
	LTE_IDLE,

	/**
	 * Promotion state between IDLE and LTE_CONTINUOUS
	 */
	LTE_PROMOTION,
	
	/**
	 * The LTE state in which data is flowing.
	 */
	LTE_CONTINUOUS,
	
	/**
	 * The LTE tail state that occurs after data flow ends.
	 */
	LTE_CR_TAIL,
	
	/**
	 * The LTE tail state that occurs after the LTE_CR_TAIL state.
	 */
	LTE_DRX_SHORT,
	
	/**
	 * The long part of the LTE tail that occurs after the LTE_DRX_SHORT tail state.
	 */
	LTE_DRX_LONG,

	// TODO Add comments
	/**
	 * The WiFi Active state.
	 */
	WIFI_ACTIVE,
	
	/**
	 * The WiFi Idle state.
	 */
	WIFI_TAIL,
	
	/**
	 * The WiFi Tail state.
	 */
	WIFI_IDLE
}
