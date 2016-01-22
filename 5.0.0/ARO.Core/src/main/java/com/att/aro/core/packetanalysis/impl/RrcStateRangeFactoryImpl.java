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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.configuration.pojo.Profile3G;
import com.att.aro.core.configuration.pojo.ProfileLTE;
import com.att.aro.core.configuration.pojo.ProfileType;
import com.att.aro.core.configuration.pojo.ProfileWiFi;
import com.att.aro.core.packetanalysis.IRrcStateRangeFactory;
import com.att.aro.core.packetanalysis.pojo.DchDemotionQueue;
import com.att.aro.core.packetanalysis.pojo.FachQueue;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.RRCState;
import com.att.aro.core.packetanalysis.pojo.RrcStateRange;
import com.att.aro.core.packetreader.pojo.PacketDirection;

public class RrcStateRangeFactoryImpl implements IRrcStateRangeFactory {

	@Override
	public List<RrcStateRange> create(List<PacketInfo> packetlist,
			Profile profile, double traceDuration) {
		if(profile.getProfileType() == ProfileType.T3G){
			Profile3G prof = (Profile3G)profile;
			return this.create3G(packetlist, prof, traceDuration);
		}else if(profile.getProfileType() == ProfileType.WIFI){
			ProfileWiFi prof = (ProfileWiFi)profile;
			return this.createWiFi(packetlist, prof, traceDuration);
		}else if(profile.getProfileType() == ProfileType.LTE){
			ProfileLTE prof = (ProfileLTE)profile;
			return this.createLTE(packetlist, prof, traceDuration);
		}else{
			throw new IllegalArgumentException("Invalid profile type for state machine: "
					+ profile.getClass());
		}
	}
	/**
	 * This method contains the main algorithm for creating the List of
	 * RrcStateRange for a LTE profile
	 * 
	 * @param analysisData
	 *            Analysis data
	 * @param profile
	 *            LTE profile
	 * @return list of RRC State range values.
	 */
	private List<RrcStateRange> createLTE(List<PacketInfo> packetlist,
			ProfileLTE profile, double traceDuration) {

		// Create results list
		ArrayList<RrcStateRange> result = new ArrayList<RrcStateRange>();

		// Iterate through packets in trace
		Iterator<PacketInfo> iter = packetlist.iterator();
		PacketInfo packet;
		if (iter.hasNext()) {

			// Track time of state changes
			double timer = 0.0;

			// Keep timestamp of previous packet in iteration
			packet = iter.next();
			packet.setStateMachine(RRCState.LTE_CONTINUOUS);
			double last = packet.getTimeStamp();

			// First packet starts continuous reception
			timer = promoteLTE(result, timer, last, profile);
			while (iter.hasNext()) {
				packet = iter.next();
				packet.setStateMachine(RRCState.LTE_CONTINUOUS);
				double curr = packet.getTimeStamp();

				// Check to see if we dropped to CR tail
				if (curr - last > profile.getInactivityTimer()) {
					timer = tailLTE(result, timer, last, curr, profile);

					// If end of tail was reached, we need to promote for new
					// packet
					if (timer < curr) {
						timer = promoteLTE(result, timer, curr, profile);
					}
				}

				// Save current packet time as last packet for next iteration
				last = curr;
			}

			// Do final LTE tail
			timer = tailLTE(result, timer, last, traceDuration, profile);

			// Check for final idle time
			if (timer < traceDuration) {
				result.add(new RrcStateRange(timer, traceDuration, RRCState.LTE_IDLE));
			}
		} else {

			// State is idle for the entire trace
			result.add(new RrcStateRange(0.0, traceDuration,
					RRCState.LTE_IDLE));
		}

		return result;
	}
	/**
	 * Utility method that creates RRC state ranges for an LTE tail sequence.
	 * 
	 * @param result
	 *            List where state ranges will be added
	 * @param timer
	 *            Time at which first packet was received for LTE continuous
	 *            reception
	 * @param start
	 *            Time at which last packet was received for LTE continuous
	 *            reception and the tail sequence begins
	 * @param end
	 *            Time at which tail sequence is stopped (either by new
	 *            continuous reception state or end of trace).
	 * @param profile
	 *            LTE profile being used to model state ranges
	 * @return The time at which the tail sequence was completed or stopped
	 */
	private static double tailLTE(List<RrcStateRange> result, double timer, double start,
			double end, ProfileLTE profile) {

		double tailTime = timer;
		double startTime = start;
		// Add the continuous reception time
		result.add(new RrcStateRange(tailTime, startTime, RRCState.LTE_CONTINUOUS));

		// Check for CR tail time
		tailTime = Math.min(startTime + profile.getInactivityTimer(), end);
		if (tailTime > startTime) {
			result.add(new RrcStateRange(startTime, tailTime, RRCState.LTE_CR_TAIL));
			startTime = tailTime;
		}

		// Check for DRX short tail time
		tailTime = Math.min(startTime + profile.getDrxShortTime(), end);
		if (tailTime > startTime) {
			result.add(new RrcStateRange(startTime, tailTime, RRCState.LTE_DRX_SHORT));
			startTime = tailTime;
		}

		// Check for DRX long tail time
		tailTime = Math.min(startTime + profile.getDrxLongTime(), end);
		if (tailTime > startTime) {
			result.add(new RrcStateRange(startTime, tailTime, RRCState.LTE_DRX_LONG));
			startTime = tailTime;
		}
		return tailTime;
	}
	/**
	 * Private utility method that creates RRC state range entries for promoting
	 * between LTE idle and continuous reception. This method will create the
	 * IDLE and PROMOTION state ranges. The
	 * 
	 * @param result
	 *            List where state ranges will be added
	 * @param start
	 *            Indicates time of end of last LTE long tail state or beginning
	 *            of trace
	 * @param end
	 *            Indicates time of packet that is causing the promotion.
	 * @param profile
	 *            LTE profile being used to model state ranges
	 * @return The time at which the promotion is complete
	 */
	private double promoteLTE(List<RrcStateRange> result, double start, double end,
			ProfileLTE profile) {

		// Find the time that the promotion started before the packet was
		// received
		double promoStart = Math.max(start, end - profile.getPromotionTime());

		// Check to see if there was some IDLE time
		if (promoStart > start) {
			result.add(new RrcStateRange(start, promoStart, RRCState.LTE_IDLE));
		}

		// Add the promotion state range
		result.add(new RrcStateRange(promoStart, end, RRCState.LTE_PROMOTION));
		return end;
	}
	
	private List<RrcStateRange> createWiFi(List<PacketInfo> packetlist , ProfileWiFi profile, double traceDuration) {
		

		// Create results list
		ArrayList<RrcStateRange> result = new ArrayList<RrcStateRange>();

		// Iterate through packets in trace
		Iterator<PacketInfo> iter = packetlist.iterator();
		PacketInfo packet;
		if (iter.hasNext()) {

			// Track time of state changes
			double timer = 0.0;

			// Keep timestamp of previous packet in iteration
			packet = iter.next();
			packet.setStateMachine(RRCState.WIFI_ACTIVE);
			double last = packet.getTimeStamp();
 
			// Idle state till first packet is received
			result.add(new RrcStateRange(timer, last, RRCState.WIFI_IDLE));
			timer = last;
			 
			while (iter.hasNext()) {
				packet = iter.next();
				packet.setStateMachine(RRCState.WIFI_ACTIVE);
				double curr = packet.getTimeStamp();

				// Check to see if we dropped to WiFi Active
				if (curr - last > profile.getWifiTailTime()) {
					timer = tailWiFi(result, timer, last, curr, profile);

					// If end of tail was reached, we need to the idle time before the next packet arrives
					if (timer < curr) {
						result.add(new RrcStateRange(timer , curr , RRCState.WIFI_IDLE));
						timer = curr;
					}
				}

				// Save current packet time as last packet for next iteration
				last = curr;
			}

			// Do final WiFi tail
			timer = tailWiFi(result, timer, last, traceDuration, profile);

			// Check for final idle time
			if (timer < traceDuration) {
				result.add(new RrcStateRange(timer, traceDuration, RRCState.WIFI_IDLE));
			}
		} else {

			// State is idle for the entire trace
			result.add(new RrcStateRange(0.0, traceDuration,
					RRCState.WIFI_IDLE));
		}

		return result;
		
	}
	
	private static double tailWiFi(List<RrcStateRange> result, double timer, double start,
			double end, ProfileWiFi profile) {
		
		double tailTime = timer;
		// Add the continuous reception time
		result.add(new RrcStateRange(tailTime, start, RRCState.WIFI_ACTIVE));

		// Check for CR tail time
		tailTime = Math.min(start + profile.getWifiTailTime(), end);
		if (tailTime > start) {
			result.add(new RrcStateRange(start, tailTime, RRCState.WIFI_TAIL));

		}
		return tailTime;
	}
	
	/**
	 * This method contains the main algorithm for creating the List of
	 * RrcStateRange for a 3G profile
	 * 
	 * @param analysisData
	 *            Analysis data
	 * @param profile
	 *            3G profile
	 * @return list of RRC State range values.
	 */
	private List<RrcStateRange> create3G(List<PacketInfo> packetlist, Profile3G profile, double traceDuration) {

		List<PacketInfo> packetInfos = packetlist;

		List<RrcStateRange> result = new ArrayList<RrcStateRange>();
		if (packetInfos != null && !packetInfos.isEmpty()) {

			// Get important profile info
			double idleDchPromoAvg = profile.getIdleDchPromoAvg();
			double idleDchPromoMin = profile.getIdleDchPromoMin();
			double idleDchPromoMax = profile.getIdleDchPromoMax();
			double fachDchPromoAvg = profile.getFachDchPromoAvg();
			double fachDchPromoMin = profile.getFachDchPromoMin();
			double fachDchPromoMax = profile.getFachDchPromoMax();
			double dchFachTimer = profile.getDchFachTimer();
			double fachIdleTimer = profile.getFachIdleTimer();

			double timer = 0;

			DchDemotionQueue dchDemotionQueue = new DchDemotionQueue(profile);
			FachQueue fachQueue = new FachQueue(profile);

			// Set up initial packet
			PacketInfo prevPacket = packetInfos.get(0);
			double currTimeStamp = prevPacket.getTimeStamp();
			prevPacket.setStateMachine(RRCState.PROMO_IDLE_DCH);

			// Add initial idle state
			addStateRangeEx(result, 0, Double.MAX_VALUE, RRCState.STATE_IDLE, currTimeStamp);

			for (int i = 1; i <= packetInfos.size(); ++i) {
				PacketInfo packet;
				PacketDirection dir;
				int currLen;
				if (i >= packetInfos.size()) {

					// The last iteration of this loop
					packet = null;
					dir = PacketDirection.UPLINK;
					currTimeStamp = Double.MAX_VALUE;
					currLen = 0;
				} else {

					// Iteration on a packet
					packet = packetInfos.get(i);
					dir = packet.getDir();
					currTimeStamp = packet.getTimeStamp();
					currLen = packet.getLen();
				}
				double prevTimeStamp = prevPacket.getTimeStamp();
				double deltaTime = currTimeStamp - prevTimeStamp;

				RRCState state = null; // the next state to be determined
				RRCState promoState = prevPacket.getStateMachine();
				
				if(promoState == RRCState.PROMO_IDLE_DCH || promoState == RRCState.PROMO_FACH_DCH){
					double promoAvg, promoMin, promoMax;
					if (promoState == RRCState.PROMO_IDLE_DCH) {
						promoAvg = idleDchPromoAvg;
						promoMin = idleDchPromoMin;
						promoMax = idleDchPromoMax;
					} else {
						promoAvg = fachDchPromoAvg;
						promoMin = fachDchPromoMin;
						promoMax = fachDchPromoMax;
					}

					if (dir == PacketDirection.UPLINK && timer + deltaTime <= promoMin) { // Case
																					// 1
						prevTimeStamp = addStateRangeEx(result, prevTimeStamp, Double.MAX_VALUE,
								promoState, currTimeStamp);
						state = promoState;
						timer += deltaTime;
					} else if (dir == PacketDirection.DOWNLINK && timer + deltaTime <= promoMin) {
						// TODO: handle an error situation here: a DOWNLINK DCH
						// packet follows "immediately" after a packet on
						// FACH/IDLE
						// promotion

						prevTimeStamp = addStateRangeEx(result, prevTimeStamp, Double.MAX_VALUE,
								promoState, currTimeStamp);
						state = promoState;
						timer += deltaTime;

					} else if (timer + deltaTime <= promoMax) { // Case 2
						prevTimeStamp = addStateRangeEx(result, prevTimeStamp, Double.MAX_VALUE,
								promoState, currTimeStamp);
						state = RRCState.STATE_DCH;

						dchDemotionQueue.init(currTimeStamp, currLen, dir);

					} else if (timer + deltaTime <= promoAvg + dchFachTimer) { // Case
																				// 3
						prevTimeStamp = addStateRangeEx(result, prevTimeStamp, promoAvg - timer,
								promoState, currTimeStamp);
						prevTimeStamp = addStateRangeEx(result, prevTimeStamp, Double.MAX_VALUE,
								RRCState.STATE_DCH, currTimeStamp);
						state = RRCState.STATE_DCH;

						dchDemotionQueue.init(currTimeStamp, currLen, dir);

					} else if (timer + deltaTime <= promoAvg + dchFachTimer + fachIdleTimer) { // Case
																								// 4
						if (dir == PacketDirection.DOWNLINK) {
							fachQueue.init();
							if (fachQueue.simFACH(currTimeStamp, dir, currLen)) { // FACH->DCH
								double tMax0 = currTimeStamp - fachDchPromoAvg;
								prevTimeStamp = addStateRangeEx(result, prevTimeStamp, promoAvg
										- timer, promoState, tMax0);
								prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
										dchFachTimer, RRCState.TAIL_DCH, tMax0);
								prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
										Double.MAX_VALUE, RRCState.STATE_FACH, tMax0);
								// promoTime = tMax - tt;
								prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
										Double.MAX_VALUE, RRCState.PROMO_FACH_DCH, currTimeStamp);
								state = RRCState.STATE_DCH;

								dchDemotionQueue.init(currTimeStamp, currLen, dir);

							} else {
								prevTimeStamp = addStateRangeEx(result, prevTimeStamp, promoAvg
										- timer, promoState, currTimeStamp);
								prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
										dchFachTimer, RRCState.TAIL_DCH, currTimeStamp);
								prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
										Double.MAX_VALUE, RRCState.STATE_FACH, currTimeStamp);
								state = RRCState.STATE_FACH;
							}
						} else { // downlink
							fachQueue.init();
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
									promoAvg - timer, promoState, currTimeStamp);
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp, dchFachTimer,
									RRCState.TAIL_DCH, currTimeStamp);
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
									Double.MAX_VALUE, RRCState.STATE_FACH, currTimeStamp);
							if (fachQueue.simFACH(currTimeStamp, dir, currLen)) {
								state = RRCState.PROMO_FACH_DCH;
								timer = 0;
							} else {
								state = RRCState.STATE_FACH;
							}
						}
					} else { // case 5
						if (dir == PacketDirection.UPLINK) {
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
									promoAvg - timer, promoState, currTimeStamp);
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp, dchFachTimer,
									RRCState.TAIL_DCH, currTimeStamp);
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp, fachIdleTimer,
									RRCState.TAIL_FACH, currTimeStamp);
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
									Double.MAX_VALUE, RRCState.STATE_IDLE, currTimeStamp);
							state = RRCState.PROMO_IDLE_DCH;
							timer = 0;
						} else { // downlink
							double tMax0 = currTimeStamp - idleDchPromoAvg;
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
									promoAvg - timer, promoState, tMax0);
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp, dchFachTimer,
									RRCState.TAIL_DCH, tMax0);
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp, fachIdleTimer,
									RRCState.TAIL_FACH, tMax0);
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
									Double.MAX_VALUE, RRCState.STATE_IDLE, tMax0);
							// promoTime = tMax - tt;
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
									Double.MAX_VALUE, RRCState.PROMO_IDLE_DCH, currTimeStamp);
							state = RRCState.STATE_DCH;

							dchDemotionQueue.init(currTimeStamp, currLen, dir);

						}
					}
//					break;
				}

				else if(promoState ==  RRCState.STATE_DCH) {
					double dchTail = dchDemotionQueue.getDCHTail(currTimeStamp); // ***
																					// Dynamic
																					// DCH
					// Tail ***

					if (deltaTime <= dchTail + 1e-5) { // DCH Case 1
						prevTimeStamp = addStateRangeEx(result, prevTimeStamp, Double.MAX_VALUE,
								RRCState.STATE_DCH, currTimeStamp);
						state = RRCState.STATE_DCH;

						dchDemotionQueue.update(currTimeStamp, currLen, dir);

					} else if (deltaTime <= dchTail + fachIdleTimer) { // DCH
																		// Case
																		// 2
						if (dir == PacketDirection.DOWNLINK) { // downlink
							fachQueue.init();
							if (fachQueue.simFACH(currTimeStamp, dir, currLen)) {
								double tMax0 = currTimeStamp - fachDchPromoAvg;
								changeStateRangeBack(result, dchFachTimer - dchTail,
										RRCState.TAIL_DCH);
								prevTimeStamp = addStateRangeEx(result, prevTimeStamp, dchTail,
										RRCState.TAIL_DCH, tMax0);
								prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
										Double.MAX_VALUE, RRCState.STATE_FACH, tMax0);
								// promoTime = tMax - tt;
								prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
										Double.MAX_VALUE, RRCState.PROMO_FACH_DCH, currTimeStamp);
								state = RRCState.STATE_DCH;

								dchDemotionQueue.init(currTimeStamp, currLen, dir);

							} else {
								changeStateRangeBack(result, dchFachTimer - dchTail,
										RRCState.TAIL_DCH);
								prevTimeStamp = addStateRangeEx(result, prevTimeStamp, dchTail,
										RRCState.TAIL_DCH, currTimeStamp);
								prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
										Double.MAX_VALUE, RRCState.STATE_FACH, currTimeStamp);
								state = RRCState.STATE_FACH;
							}
						} else { // uplink
							fachQueue.init();
							changeStateRangeBack(result, dchFachTimer - dchTail,
									RRCState.TAIL_DCH);
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp, dchTail,
									RRCState.TAIL_DCH, currTimeStamp);
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
									Double.MAX_VALUE, RRCState.STATE_FACH, currTimeStamp);
							if (fachQueue.simFACH(currTimeStamp, dir, currLen)) {
								state = RRCState.PROMO_FACH_DCH;
								timer = 0;
							} else {
								state = RRCState.STATE_FACH;
							}
						}
					} else { // DCH Case 3
						if (dir == PacketDirection.UPLINK) { // uplink
							changeStateRangeBack(result, dchFachTimer - dchTail,
									RRCState.TAIL_DCH);
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp, dchTail,
									RRCState.TAIL_DCH, currTimeStamp);
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp, fachIdleTimer,
									RRCState.TAIL_FACH, currTimeStamp);
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
									Double.MAX_VALUE, RRCState.STATE_IDLE, currTimeStamp);
							state = RRCState.PROMO_IDLE_DCH;
							timer = 0;
						} else { // downlink
							double tMax0 = currTimeStamp - idleDchPromoAvg;
							changeStateRangeBack(result, dchFachTimer - dchTail,
									RRCState.TAIL_DCH);
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp, dchTail,
									RRCState.TAIL_DCH, tMax0);
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp, fachIdleTimer,
									RRCState.TAIL_FACH, tMax0);
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
									Double.MAX_VALUE, RRCState.STATE_IDLE, tMax0);
							// promoTime = tMax - tt;
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
									Double.MAX_VALUE, RRCState.PROMO_IDLE_DCH, currTimeStamp);
							state = RRCState.STATE_DCH;

							dchDemotionQueue.init(currTimeStamp, currLen, dir);
						}
					}
//					break;
				}

				else if(promoState == RRCState.STATE_FACH) {
					if (deltaTime <= fachIdleTimer) {
						if (dir == PacketDirection.UPLINK) {
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
									Double.MAX_VALUE, RRCState.STATE_FACH, currTimeStamp);
							if (fachQueue.simFACH(currTimeStamp, dir, currLen)) {
								state = RRCState.PROMO_FACH_DCH;
								timer = 0;
							} else {
								state = RRCState.STATE_FACH;
							}
						} else { // downlink
							if (fachQueue.simFACH(currTimeStamp, dir, currLen)) {
								double tMax0 = currTimeStamp - fachDchPromoAvg;

								/*
								 * TODO: ( diff ) handle the case where promo
								 * delay is 0 ( for what - if )
								 */
								if (tMax0 > prevTimeStamp || fachDchPromoAvg < 1e-6) {
									prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
											Double.MAX_VALUE, RRCState.STATE_FACH, tMax0);
									// promoTime = tMax - tt;
									prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
											Double.MAX_VALUE, RRCState.PROMO_FACH_DCH,
											currTimeStamp);
								} else {
									// *** handle an error situation here: a
									// DOWNLINK DCH packet follows "immediately"
									// after a packet on FACH
									tMax0 = currTimeStamp - fachDchPromoMin; // try
																				// y1
																				// instead
																				// of
																				// y?
									if (tMax0 > prevTimeStamp) {
										prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
												Double.MAX_VALUE, RRCState.STATE_FACH, tMax0);
										// promoTime = tMax - tt;
										prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
												Double.MAX_VALUE, RRCState.PROMO_FACH_DCH,
												currTimeStamp);
									} else { // still not working - try to
												// insert a
												// promotion after some previous
												// packet
										boolean bFixed = false;
										for (int ii = i - 1; ii > 0; ii--) {
											PacketInfo earlierPacket = packetInfos.get(ii);
											if (earlierPacket.getStateMachine() == RRCState.STATE_FACH) {
												// FACH-DCH promo: from
												// packets[ii].ts to
												// packets[ii].ts+y
												// DCH: from packets[ii].ts+y to
												// tMax

												double piTimeStamp = packetInfos.get(ii).getTimeStamp();
												if (earlierPacket.getDir() == PacketDirection.UPLINK
														&& currTimeStamp >= piTimeStamp + fachDchPromoMin) {
													int resultSize = result.size() - 1;
													// boolean bDone = false;
													for (int jj = resultSize; jj > 0; jj--) {
														// double EPS = 1e-4;
														if (result.get(jj).getBeginTime() == piTimeStamp) {

															for (int k = 0; k < resultSize - jj + 1; k++) {
																result.remove(result.size() - 1);
															}

															double avgDchPromo;
															if (currTimeStamp >= piTimeStamp
																	+ fachDchPromoAvg) {
																avgDchPromo = fachDchPromoAvg;
															} else {
																avgDchPromo = fachDchPromoMin;
															}

															result.add(new RrcStateRange(piTimeStamp, piTimeStamp
																	+ avgDchPromo, RRCState.PROMO_FACH_DCH));
															result.add(new RrcStateRange(piTimeStamp + avgDchPromo,
																	prevTimeStamp,
																	RRCState.STATE_DCH));

															prevTimeStamp = addStateRangeEx(result,
																	prevTimeStamp,
																	Double.MAX_VALUE,
																	RRCState.STATE_DCH,
																	currTimeStamp);
															break;
														}
														// #undef EPS

													}

													bFixed = true;
													break;
												}

											} else {
												break;
											}
										}

										if (!bFixed) {
											// still not working - force it on
											// FACH
											prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
													Double.MAX_VALUE, RRCState.STATE_FACH,
													currTimeStamp);
											state = RRCState.STATE_FACH;
											fachQueue.init();
										}
									}
								} // finish handling the error case

								state = RRCState.STATE_DCH;
								dchDemotionQueue.init(currTimeStamp, currLen, dir);

							}

							else {
								prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
										Double.MAX_VALUE, RRCState.STATE_FACH, currTimeStamp);
								state = RRCState.STATE_FACH;
							}

						}
					} else {
						if (dir == PacketDirection.UPLINK) {
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp, fachIdleTimer,
									RRCState.TAIL_FACH, currTimeStamp);
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
									Double.MAX_VALUE, RRCState.STATE_IDLE, currTimeStamp);
							state = RRCState.PROMO_IDLE_DCH;
							timer = 0;
						} else { // downlink
							double tMax0 = currTimeStamp - idleDchPromoAvg;
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp, fachIdleTimer,
									RRCState.TAIL_FACH, tMax0);
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
									Double.MAX_VALUE, RRCState.STATE_IDLE, tMax0);
							// promoTime = tMax - tt;
							prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
									Double.MAX_VALUE, RRCState.PROMO_IDLE_DCH, currTimeStamp);

							state = RRCState.STATE_DCH;

							dchDemotionQueue.init(currTimeStamp, currLen, dir);
						}
					}
				}
				

				if (packet != null) {
					packet.setStateMachine(state);
				}
				prevPacket = packet;
			}
		}

		result = compressStateRanges(result);

		// Truncate state ranges at end of trace
		Iterator<RrcStateRange> iter = result.iterator();
		double prevTimeStamp = 0.0;
		while (iter.hasNext()) {
			RrcStateRange rrc = iter.next();
			if (rrc.getBeginTime() >= traceDuration) {
				iter.remove();
			}
			if (rrc.getEndTime() > traceDuration) {
				rrc.setEndTime(traceDuration);
			}
			prevTimeStamp = rrc.getEndTime();
		}
		if (prevTimeStamp < traceDuration) {

			// Add idle time to end of trace
			result.add(new RrcStateRange(prevTimeStamp, traceDuration, RRCState.STATE_IDLE));
		}

		return result;
	}
	/**
	 * This method adds the an RrcStateRange to the list rrc.
	 * 
	 * @param time
	 * @param duration
	 *            The duration of the state.
	 * @param state
	 *            The RRC state
	 * @param tMax
	 */
	private static double addStateRangeEx(List<RrcStateRange> rrc, double time, double duration1,
			RRCState state, double tMax) {
		double duration = duration1;
		if ((time >= tMax) || (duration <= 0)) {
			return time;
		}
		if ((time + duration) > tMax) {
			duration = tMax - time;
		}

		double end = time + duration;
		rrc.add(new RrcStateRange(time, end, state));
		return end;
	}

	private static void changeStateRangeBack(List<RrcStateRange> rrc, double duration,
			RRCState newState) {

		double tDuration = duration;
		final double EPS = 1e-5;

		if (tDuration < EPS) {
			return;
		}

		int size = rrc.size();
		for (int i = size - 1; i >= 0; i--) {
			rrc.get(i).setState(newState);
			tDuration -= rrc.get(i).getEndTime() - rrc.get(i).getBeginTime();

			if (tDuration <= EPS) {
				break;
			}
		}

	}

	private static List<RrcStateRange> compressStateRanges(List<RrcStateRange> rrc) {
		Collections.sort(rrc);
		int rrcSize = rrc.size();

		List<RrcStateRange> rrc2 = new ArrayList<RrcStateRange>();
		if (rrcSize > 0) {
			int rrcObjPosition = 0;
			rrc2.add(rrc.get(0));

			for (int i = 1; i < rrcSize; i++) {
				if (rrc.get(i).getState() != rrc2.get(rrcObjPosition).getState()) {
					rrc2.get(rrcObjPosition++).setEndTime(rrc.get(i - 1).getEndTime());
					rrc2.add(rrc.get(i));
				}
			}

			rrc2.get(rrcObjPosition).setEndTime( rrc.get(rrcSize - 1).getEndTime());
		}

		return rrc2;
	}

}//end class
