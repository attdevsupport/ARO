/*
 *  Copyright 2012 AT&T
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
package com.att.aro.model;

/**
 * Identifies valid RRC states
 */
public enum RRCState {
	/**
	 * Idle state for RRC.
	 */
	STATE_IDLE,
	/**
	 *  Direct Channel active state for RRC.
	 */
	STATE_DCH,
	/**
	 *  Forward Access Channel active state for RRC.
	 */
	STATE_FACH,
	/**
	 *  Direct Channel tail state for RRC.
	 */
	TAIL_DCH, 
	/**
	 *  Forward Access Channel tail state for RRC.
	 */
	TAIL_FACH,
	/**
	 *  Promotion from Idle to Direct Channel state.
	 */
	PROMO_IDLE_DCH, 
	/**
	 *  Promotion from Forward Access to Direct Channel state.
	 */
	PROMO_FACH_DCH,
	
	/**
	 * Idle state for RRC.
	 */
	LTE_IDLE,

	/**
	 * Promotion state between IDLE and LTE_CONTINUOUS
	 */
	LTE_PROMOTION,
	
	/**
	 * LTE data is flowing
	 */
	LTE_CONTINUOUS,
	
	/**
	 * After data flow ends
	 */
	LTE_CR_TAIL,
	
	/**
	 * Next step of LTE tail
	 */
	LTE_DRX_SHORT,
	
	/**
	 * Long part of LTE tail
	 */
	LTE_DRX_LONG;
	
}
