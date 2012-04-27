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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.att.aro.model.PacketInfo.Direction;

/**
 * Radio Resource Control analysis carried out from this class. It analysis the
 * time range between each RRC states, also it acts as a Bean class to get the
 * RRC range information.
 */
public class RrcStateRange implements Comparable<RrcStateRange>, Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Handles the calculation related to DCH State and DCH Tail State.
	 */
	private static class DchDemotionQueue {
		private Profile3G profile;
		private int dchDemotionQueueUL = -1;
		private int dchDemotionQueueDL = -1;
		private double dchTimerResetTS;
		private double dchLastPktTSUL;
		private double dchLastPktTSDL;

		/**
		 * Constructor
		 * 
		 * @param profile
		 */
		public DchDemotionQueue(Profile3G profile) {
			this.profile = profile;
		}

		/**
		 * Initialize the DCH state information.
		 * 
		 * @param ts
		 *            time stamp where DCH starts.
		 * @param size
		 *            size of the consumed during that DCH.
		 * @param dir
		 *            Direction of the DCH UPLINK/DOWNLINK.
		 */
		private void init(double ts, int size, Direction dir) {
			if (dir == Direction.UPLINK) {
				dchDemotionQueueUL = size;
				dchDemotionQueueDL = 0;
				dchLastPktTSUL = ts;
				dchLastPktTSDL = -9999.0f;
			} else if (dir == Direction.DOWNLINK) {
				dchDemotionQueueUL = 0;
				dchDemotionQueueDL = size;
				dchLastPktTSDL = ts;
				dchLastPktTSUL = -9999.0f;
			}

			dchTimerResetTS = ts;
		}

		/**
		 * Updates the RRC information in existing RRC state.
		 * 
		 * @param ts
		 * @param size
		 * @param dir
		 */
		private void update(double ts, int size, Direction dir) {
			if (dir == Direction.UPLINK) {
				if (ts > dchLastPktTSUL + profile.getDchTimerResetWin()) {
					dchDemotionQueueUL = size;
				} else {
					dchDemotionQueueUL += size;
				}
				if (ts > dchLastPktTSDL + profile.getDchTimerResetWin())
					dchDemotionQueueDL = 0;
				dchLastPktTSUL = ts;
			}

			else if (dir == Direction.DOWNLINK) {
				if (ts > dchLastPktTSDL + profile.getDchTimerResetWin()) {
					dchDemotionQueueDL = size;
				} else {
					dchDemotionQueueDL += size;
				}
				if (ts > dchLastPktTSUL + profile.getDchTimerResetWin())
					dchDemotionQueueUL = 0;
				dchLastPktTSDL = ts;

			}

			if (dchDemotionQueueUL >= profile.getDchTimerResetSize()
					|| dchDemotionQueueDL >= profile.getDchTimerResetSize()) {
				dchTimerResetTS = ts;
			}
		}

		private double getDCHTail(double ts) {
			double lastTS = Math.max(dchLastPktTSDL, dchLastPktTSUL);
			double dt = profile.getDchFachTimer() - (lastTS - dchTimerResetTS);
			return dt;
		}

	}

	/**
	 * Handles the calculation related to DCH State and DCH Tail State.
	 */
	private static class FachQueue {
		private Profile3G profile;
		private int ulQueue, dlQueue;
		private double ulTS, dlTS;

		/**
		 * Constructor.
		 * 
		 * @param profile
		 */
		public FachQueue(Profile3G profile) {
			this.profile = profile;
		}

		/**
		 * Initialize the FACH data.
		 */
		private void init() {
			ulQueue = dlQueue = 0;
			ulTS = dlTS = -1000.0f;
		}

		/**
		 * return true if it triggers a FACH->DCH promotion
		 * 
		 * @param ts
		 * @param dir
		 * @param size
		 * @return
		 */
		private boolean simFACH(double ts, Direction dir, int size) {
			if (dir == Direction.UPLINK) {
				// MyAssert(ts >= ulTS, 113);
				double queueConsumptionTime = (ulQueue * ulQueue * profile.getRlcUlRateP2()
						+ ulQueue * profile.getRlcUlRateP1() + profile.getRlcUlRateP0()) / 1000.0f;
				if (ts - ulTS > queueConsumptionTime)
					ulQueue = 0;

				// if (ts - ulTS > 0.2f) ulQueue = 0;

				ulQueue += size;
				ulTS = ts;

			}

			else if (dir == Direction.DOWNLINK) {
				// MyAssert(ts >= dlTS, 114);
				double queueConsumptionTime = (dlQueue * dlQueue * profile.getRlcDlRateP2()
						+ dlQueue * profile.getRlcDlRateP1() + profile.getRlcDlRateP0()) / 1000.0f;
				if (ts - dlTS > queueConsumptionTime)
					dlQueue = 0;

				dlQueue += size;
				dlTS = ts;

			}

			return (dlQueue > profile.getRlcDlTh() || ulQueue > profile.getRlcUlTh());
		}

	}

	/**
	 * Runs a state machine analysis for the specified trace analysis
	 * @param analysisData
	 * @return list of RRC state range objects representing the RRC state
	 * machine for the analysis
	 * @throws NullPointerException when analysisData is null
	 */
	public static List<RrcStateRange> runTrace(TraceData.Analysis analysisData) {
		Profile profile = analysisData.getProfile();

		if (profile instanceof Profile3G) {
			return runTrace3G(analysisData, (Profile3G) profile);
		} else if (profile instanceof ProfileLTE) {
			return runTraceLTE(analysisData, (ProfileLTE) profile);
		} else {
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
	private static List<RrcStateRange> runTraceLTE(TraceData.Analysis analysisData,
			ProfileLTE profile) {

		// Create results list
		ArrayList<RrcStateRange> result = new ArrayList<RrcStateRange>();

		// Iterate through packets in trace
		Iterator<PacketInfo> iter = analysisData.getPackets().iterator();
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
					
					// If end of tail was reached, we need to promote for new packet
					if (timer < curr) {
						timer = promoteLTE(result, timer, curr, profile);
					}
				}
				
				// Save current packet time as last packet for next iteration
				last = curr;
			}
			
			// Do final LTE tail
			double traceDuration = analysisData.getTraceData().getTraceDuration();
			timer = tailLTE(result, timer, last, traceDuration, profile);
			
			// Check for final idle time
			if (timer < traceDuration) {
				result.add(new RrcStateRange(timer, traceDuration, RRCState.LTE_IDLE));				
			}
		} else {
			
			// State is idle for the entire trace
			result.add(new RrcStateRange(0.0, analysisData.getTraceData()
					.getTraceDuration(), RRCState.LTE_IDLE));
		}

		return result;
	}

	/**
	 * Private utility method that creates RRC state range entries for promoting
	 * between LTE idle and continuous reception.  This method will create the
	 * IDLE and PROMOTION state ranges.  The 
	 * @param result List where state ranges will be added
	 * @param start Indicates time of end of last LTE long tail state or beginning
	 * of trace  
	 * @param end Indicates time of packet that is causing the promotion.
	 * @param profile LTE profile being used to model state ranges
	 * @return The time at which the promotion is complete
	 */
	private static double promoteLTE(List<RrcStateRange> result, double start, double end, ProfileLTE profile) {
		
		// Find the time that the promotion started before the packet was received
		double promoStart = Math.max(start, end - profile.getPromotionTime());
		
		// Check to see if there was some IDLE time
		if (promoStart > start) {
			result.add(new RrcStateRange(start, promoStart, RRCState.LTE_IDLE));
		}
		
		// Add the promotion state range
		result.add(new RrcStateRange(promoStart, end, RRCState.LTE_PROMOTION));
		return end;
	}

	/**
	 * Utility method that creates RRC state ranges for an LTE tail sequence.
	 * @param result List where state ranges will be added
	 * @param timer Time at which first packet was received for LTE continuous
	 * reception
	 * @param start Time at which last packet was received for LTE continuous
	 * reception and the tail sequence begins
	 * @param end Time at which tail sequence is stopped (either by new
	 * continuous reception state or end of trace).
	 * @param profile LTE profile being used to model state ranges
	 * @return The time at which the tail sequence was completed or stopped
	 */
	private static double tailLTE(List<RrcStateRange> result, double timer, double start, double end, ProfileLTE profile) { ;
	
		// Add the continuous reception time
		result.add(new RrcStateRange(timer, start, RRCState.LTE_CONTINUOUS));
		
		// Check for CR tail time
		timer = Math.min(start + profile.getInactivityTimer(), end);
		if (timer > start) {
			result.add(new RrcStateRange(start, timer, RRCState.LTE_CR_TAIL));

			// Check for DRX short tail time
			start = timer;
			timer = Math.min(start + profile.getDrxShortTime(), end);
			if (timer > start) {
				result.add(new RrcStateRange(start, timer, RRCState.LTE_DRX_SHORT));
				
				// Check for DRX long tail time
				start = timer;
				timer = Math.min(start + profile.getDrxLongTime(), end);
				if (timer > start) {
					result.add(new RrcStateRange(start, timer, RRCState.LTE_DRX_LONG));
				}
			}
		}
		return timer;
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
	private static List<RrcStateRange> runTrace3G(TraceData.Analysis analysisData, Profile3G profile) {

		List<PacketInfo> packetInfos = analysisData.getPackets();

		List<RrcStateRange> result = new ArrayList<RrcStateRange>();
		if (packetInfos != null && packetInfos.size() > 0) {

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
				Direction dir;
				int currLen;
				if (i >= packetInfos.size()) {

					// The last iteration of this loop
					packet = null;
					dir = Direction.UPLINK;
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
				switch (promoState) {
				case PROMO_IDLE_DCH:
				case PROMO_FACH_DCH: {
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

					if (dir == Direction.UPLINK && timer + deltaTime <= promoMin) { // Case
																					// 1
						prevTimeStamp = addStateRangeEx(result, prevTimeStamp, Double.MAX_VALUE,
								promoState, currTimeStamp);
						state = promoState;
						timer += deltaTime;
					} else if (dir == Direction.DOWNLINK && timer + deltaTime <= promoMin) {
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
						if (dir == Direction.DOWNLINK) {
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
						if (dir == Direction.UPLINK) {
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
					break;
				}

				case STATE_DCH: {
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
						if (dir == Direction.DOWNLINK) { // downlink
							fachQueue.init();
							if (fachQueue.simFACH(currTimeStamp, dir, currLen)) {
								double tMax0 = currTimeStamp - fachDchPromoAvg;
								changeStateRangeBack(result, dchFachTimer - dchTail,
										RRCState.STATE_DCH, RRCState.TAIL_DCH);
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
										RRCState.STATE_DCH, RRCState.TAIL_DCH);
								prevTimeStamp = addStateRangeEx(result, prevTimeStamp, dchTail,
										RRCState.TAIL_DCH, currTimeStamp);
								prevTimeStamp = addStateRangeEx(result, prevTimeStamp,
										Double.MAX_VALUE, RRCState.STATE_FACH, currTimeStamp);
								state = RRCState.STATE_FACH;
							}
						} else { // uplink
							fachQueue.init();
							changeStateRangeBack(result, dchFachTimer - dchTail,
									RRCState.STATE_DCH, RRCState.TAIL_DCH);
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
						if (dir == Direction.UPLINK) { // uplink
							changeStateRangeBack(result, dchFachTimer - dchTail,
									RRCState.STATE_DCH, RRCState.TAIL_DCH);
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
									RRCState.STATE_DCH, RRCState.TAIL_DCH);
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
					break;
				}

				case STATE_FACH: {
					if (deltaTime <= fachIdleTimer) {
						if (dir == Direction.UPLINK) {
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

												double t0 = packetInfos.get(ii).getTimeStamp();
												if (earlierPacket.getDir() == Direction.UPLINK
														&& currTimeStamp >= t0 + fachDchPromoMin) {
													int nn = result.size() - 1;
													// boolean bDone = false;
													for (int jj = nn; jj > 0; jj--) {
														// double EPS = 1e-4;
														if (result.get(jj).getBeginTime() == t0) {

															for (int k = 0; k < nn - jj + 1; k++)
																result.remove(result.size() - 1);

															double yy;
															if (currTimeStamp >= t0
																	+ fachDchPromoAvg) {
																yy = fachDchPromoAvg;
															} else {
																yy = fachDchPromoMin;
															}

															result.add(new RrcStateRange(t0, t0
																	+ yy, RRCState.PROMO_FACH_DCH));
															result.add(new RrcStateRange(t0 + yy,
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
						if (dir == Direction.UPLINK) {
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
		double traceDuration = analysisData.getTraceData().getTraceDuration();
		while (iter.hasNext()) {
			RrcStateRange rrc = iter.next();
			if (rrc.getBeginTime() >= traceDuration || rrc.getEndTime() == Double.MAX_VALUE) {
				iter.remove();
			}
			if (rrc.getEndTime() > traceDuration) {
				rrc.endTime = traceDuration;
			}
		}

		return result;
	}

	/**
	 * This method adds the an RrcStateRange to the list rrc.
	 * 
	 * @param t
	 * @param duration
	 *            The duration of the state.
	 * @param state
	 *            The RRC state
	 * @param tMax
	 */
	private static double addStateRangeEx(List<RrcStateRange> rrc, double t, double duration,
			RRCState state, double tMax) {
		if (t >= tMax || duration <= 0)
			return t;
		if (t + duration > tMax)
			duration = tMax - t;

		double end = t + duration;
		rrc.add(new RrcStateRange(t, end, state));
		return end;
	}

	private static void changeStateRangeBack(List<RrcStateRange> rrc, double duration,
			RRCState oldState, RRCState newState) {

		final double EPS = 1e-5;

		if (duration < EPS)
			return;

		int n = rrc.size();
		for (int i = n - 1; i >= 0; i--) {
			rrc.get(i).state = newState;
			duration -= rrc.get(i).getEndTime() - rrc.get(i).getBeginTime();

			if (duration > EPS)
				continue;
			return;
		}

	}

	private static List<RrcStateRange> compressStateRanges(List<RrcStateRange> rrc) {
		Collections.sort(rrc);
		int n = rrc.size();

		List<RrcStateRange> rrc2 = new ArrayList<RrcStateRange>();
		if (n > 0) {
			int i, j = 0;
			rrc2.add(rrc.get(0));

			for (i = 1; i < n; i++) {
				if (rrc.get(i).getState() != rrc2.get(j).getState()) {
					rrc2.get(j++).endTime = rrc.get(i - 1).getEndTime();
					rrc2.add(rrc.get(i));
				}
			}

			rrc2.get(j).endTime = rrc.get(n - 1).getEndTime();
		}

		return rrc2;
	}

	private double beginTime;
	private double endTime;
	private RRCState state;

	public RrcStateRange(double beginTime, double endTime, RRCState state) {
		this.beginTime = beginTime;
		this.endTime = endTime;
		this.state = state;
	}

	/**
	 * Public Comparison method for RRCStateRanges. Sorts from earliest time to
	 * latest time.
	 * 
	 * @return rsr.
	 */
	public int compareTo(RrcStateRange rsr) {
		return Double.valueOf(beginTime).compareTo(rsr.beginTime);
	}

	/**
	 * Returns RRC beginTime.
	 * 
	 * @return beginTime.
	 */
	public double getBeginTime() {
		return beginTime;
	}

	/**
	 * Returns RRC endTime.
	 * 
	 * @return endTime.
	 */
	public double getEndTime() {
		return endTime;
	}

	/**
	 * Returns RRC state.
	 * 
	 * @return state.
	 */
	public RRCState getState() {
		return state;
	}

}
