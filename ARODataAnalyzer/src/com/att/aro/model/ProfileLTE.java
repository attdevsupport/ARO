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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Represents an LTE device profile used in analysis modeling.
 */
public class ProfileLTE extends Profile {
	private static final long serialVersionUID = 1L;

	/**
	 * Time from Idle to CR (sec).
	 */
	public static final String T_PROMOTION = "T_PROMOTION";

	/**
	 * Time of inactivity from CR before DRX (sec).
	 */
	public static final String INACTIVITY_TIMER = "INACTIVITY_TIMER";

	/**
	 * Time in short DRX (sec).
	 */
	public static final String T_SHORT_DRX = "T_SHORT_DRX";

	/**
	 * How long ping is during DRX (sec).
	 */
	public static final String T_DRX_PING = "T_DRX_PING";

	/**
	 * Time in Long DRX (sec).
	 */
	public static final String T_LONG_DRX = "T_LONG_DRX";

	/**
	 * Ping length in IDLE (sec).
	 */
	public static final String T_IDLE_PING = "T_IDLE_PING";

	/**
	 * Period between pings DRX Short (sec).
	 */
	public static final String T_SHORT_DRX_PING_PERIOD = "T_SHORT_DRX_PING_PERIOD";

	/**
	 * Period between pings DRX Long (sec).
	 */
	public static final String T_LONG_DRX_PING_PERIOD = "T_LONG_DRX_PING_PERIOD";

	/**
	 * Period between pings IDLE (sec).
	 */
	public static final String T_IDLE_PING_PERIOD = "T_IDLE_PING_PERIOD";

	/**
	 * Average power during promotion (W).
	 */
	public static final String P_PROMOTION = "P_PROMOTION";

	/**
	 * Average power of ping during short DRX (W).
	 */
	public static final String P_SHORT_DRX_PING = "P_SHORT_DRX_PING";

	/**
	 * Average power of ping during long DRX (W).
	 */
	public static final String P_LONG_DRX_PING = "P_LONG_DRX_PING";

	/**
	 * Average power during tail (baseline) (W).
	 */
	public static final String P_TAIL = "P_TAIL";

	/**
	 * Average power of ping in idle (W).
	 */
	public static final String P_IDLE_PING = "P_IDLE_PING";

	/**
	 * Average power during idle (W).
	 */
	public static final String P_IDLE = "P_IDLE";

	/**
	 * Multiplier for throughput upload energy calc (mW/Mbps).
	 */
	public static final String LTE_ALPHA_UP = "LTE_ALPHA_UP";

	/**
	 * Multiplier for throughput download energy calc (mW/Mbps).
	 */
	public static final String LTE_ALPHA_DOWN = "LTE_ALPHA_DOWN";

	/**
	 * Baseline for CR energy (before throughput modifiers added) (W).
	 */
	public static final String LTE_BETA = "LTE_BETA";

	private double promotionTime;
	private double inactivityTimer;
	private double drxShortTime;
	private double drxPingTime;
	private double drxLongTime;
	private double idlePingTime;
	private double drxShortPingPeriod;
	private double drxLongPingPeriod;
	private double idlePingPeriod;

	private double ltePromotionPower;
	private double drxShortPingPower;
	private double drxLongPingPower;
	private double lteTailPower;
	private double lteIdlePingPower;
	private double lteIdlePower;

	private double lteAlphaUp;
	private double lteAlphaDown;
	private double lteBeta;

	/**
	 * Default constructor
	 */
	public ProfileLTE() {
		super();
	}

	/**
	 * Initializes a new instace of LTE Profile with the specified profile file
	 * and and profile properties.
	 * 
	 * @param file
	 *            The file where profile properties can be saved. Can be null.
	 * @param properties
	 *            The profile properties that are to be set to the profile.
	 * @throws ProfileException
	 */
	public ProfileLTE(File file, Properties properties) throws ProfileException {
		super(file, properties);
	}

	/**
	 * Initializes a new instace of LTE Profile with the specified profile name
	 * 
	 * @param name
	 *            The name of the profile.
	 * @param properties
	 *            the properties that are to be set to the profile.
	 * @throws ProfileException
	 */
	public ProfileLTE(String name, Properties properties)
			throws ProfileException {
		super(name, properties);
	}

	/**
	 * @see com.att.aro.model.Profile#getProfileType()
	 */
	@Override
	public ProfileType getProfileType() {
		return ProfileType.LTE;
	}

	/**
	 * Calculates the energy used by the specific RRC state over a period of
	 * time using this LTE profile
	 * 
	 * @param time1
	 *            The begin time.
	 * @param time2
	 *            The end time.
	 * @param state
	 *            The radio state.
	 * @param packets List of packets passed over the timeline and may be used
	 * in determining energy used
	 * @see com.att.aro.model.Profile#energy(double, double,
	 *      com.att.aro.model.RRCState)
	 */
	@Override
	public double energy(double time1, double time2, RRCState state,
			List<PacketInfo> packets) {

		double deltaTime = time2 - time1;
		double result = 0.0;

		switch (state) {
		case LTE_PROMOTION :
			return deltaTime * ltePromotionPower;
		case LTE_CR_TAIL:
			// Assume no throughput
			return deltaTime * lteBeta;
		case LTE_CONTINUOUS:
			for (Throughput t : Throughput.calculateThroughput(time1, time2,
					getThroughputWindow(), packets)) {
				result += (((lteAlphaUp / 1000.0) * t.getUploadMbps())
						+ ((lteAlphaDown / 1000.0) * t.getDownloadMbps()) + lteBeta)
						* t.getSamplePeriod();
			}
			break;
		case LTE_DRX_SHORT :
			return (deltaTime / drxShortPingPeriod)
					* ((drxPingTime * drxShortPingPower) + ((drxShortPingPeriod - drxPingTime) * lteTailPower));
		case LTE_DRX_LONG :
			return (deltaTime / drxLongPingPeriod)
					* ((drxPingTime * drxLongPingPower) + ((drxLongPingPeriod - drxPingTime) * lteTailPower));
		case LTE_IDLE :
			
			// Add energy for full idle ping periods
			result = ((int) (deltaTime / idlePingPeriod))
					* ((idlePingTime * lteIdlePingPower) + ((idlePingPeriod - idlePingTime) * lteIdlePower));

			// Add residual energy for partial idle ping period
			double tres = deltaTime % idlePingPeriod;
			result += tres <= idlePingTime ? tres * lteIdlePingPower
					: ((idlePingTime * lteIdlePingPower) + ((tres - idlePingTime) * lteIdlePower));
			break;
		}
		return result;
	}

	/**
	 * Initialize the Profile values from the provided Properties object.
	 * 
	 * @param properties
	 *            Object that contains profile values.
	 * @throws ProfileException
	 */
	@Override
	protected void setProperties(Properties properties) {
		promotionTime = readDouble(properties, T_PROMOTION, 0.26);
		inactivityTimer = readDouble(properties, INACTIVITY_TIMER,
				0.1);
		drxShortTime = readDouble(properties, T_SHORT_DRX, 0.02);
		drxPingTime = readDouble(properties, T_DRX_PING, 0.002);
		drxLongTime = readDouble(properties, T_LONG_DRX, 10);
		idlePingTime = readDouble(properties, T_IDLE_PING, 0.043);
		drxShortPingPeriod = readDouble(properties, T_SHORT_DRX_PING_PERIOD,
				0.02);
		drxLongPingPeriod = readDouble(properties, T_LONG_DRX_PING_PERIOD,
				0.04);
		idlePingPeriod = readDouble(properties, T_IDLE_PING_PERIOD,
				1.28);

		ltePromotionPower = readDouble(properties, P_PROMOTION,
				1.21);
		drxShortPingPower = readDouble(properties, P_SHORT_DRX_PING,
				1.68);
		drxLongPingPower = readDouble(properties, P_LONG_DRX_PING,
				1.68);
		lteTailPower = readDouble(properties, P_TAIL, 1.06);
		lteIdlePingPower = readDouble(properties, P_IDLE_PING, 0.594);
		lteIdlePower = readDouble(properties, P_IDLE, 0.0);

		lteAlphaUp = readDouble(properties, LTE_ALPHA_UP, 438.39);
		lteAlphaDown = readDouble(properties, LTE_ALPHA_DOWN, 51.97);
		lteBeta = readDouble(properties, LTE_BETA, 1.2);
	}

	/**
	 * Sets the specified properties
	 * 
	 * @param props
	 *            The properties associated with yhe profile that are to be
	 *            saved.
	 * @throws IOException
	 */
	@Override
	public synchronized void saveProperties(Properties props) {
		props.setProperty(T_PROMOTION, String.valueOf(promotionTime));
		props.setProperty(INACTIVITY_TIMER, String.valueOf(inactivityTimer));
		props.setProperty(T_SHORT_DRX, String.valueOf(drxShortTime));
		props.setProperty(T_DRX_PING, String.valueOf(drxPingTime));
		props.setProperty(T_LONG_DRX, String.valueOf(drxLongTime));
		props.setProperty(T_IDLE_PING, String.valueOf(idlePingTime));
		props.setProperty(T_SHORT_DRX_PING_PERIOD,
				String.valueOf(drxShortPingPeriod));
		props.setProperty(T_LONG_DRX_PING_PERIOD,
				String.valueOf(drxLongPingPeriod));
		props.setProperty(T_IDLE_PING_PERIOD, String.valueOf(idlePingPeriod));

		props.setProperty(P_PROMOTION, String.valueOf(ltePromotionPower));
		props.setProperty(P_SHORT_DRX_PING, String.valueOf(drxShortPingPower));
		props.setProperty(P_LONG_DRX_PING, String.valueOf(drxLongPingPower));
		props.setProperty(P_TAIL, String.valueOf(lteTailPower));
		props.setProperty(P_IDLE_PING, String.valueOf(lteIdlePingPower));
		props.setProperty(P_IDLE, String.valueOf(lteIdlePower));

		props.setProperty(LTE_ALPHA_UP, String.valueOf(lteAlphaUp));
		props.setProperty(LTE_ALPHA_DOWN, String.valueOf(lteAlphaDown));
		props.setProperty(LTE_BETA, String.valueOf(lteBeta));
	}

	/**
	 * @return the tPromotionTimer
	 */
	public double getPromotionTime() {
		return promotionTime;
	}

	/**
	 * @param tPromotionTimer
	 *            the tPromotionTimer to set
	 */
	public void setPromotionTime(double tPromotionTimer) {
		this.promotionTime = tPromotionTimer;
	}

	/**
	 * @return the inactivityTimer
	 */
	public double getInactivityTimer() {
		return inactivityTimer;
	}

	/**
	 * @param inactivityTimer
	 *            the inactivityTimer to set
	 */
	public void setInactivityTimer(double inactivityTimer) {
		this.inactivityTimer = inactivityTimer;
	}

	/**
	 * @return the shortDRXTimer
	 */
	public double getDrxShortTime() {
		return drxShortTime;
	}

	/**
	 * @param shortDRXTimer
	 *            the shortDRXTimer to set
	 */
	public void setDrxShortTime(double shortDRXTimer) {
		this.drxShortTime = shortDRXTimer;
	}

	/**
	 * @return the pingDurationDRXTimer
	 */
	public double getDrxPingTime() {
		return drxPingTime;
	}

	/**
	 * @param pingDurationDRXTimer
	 *            the pingDurationDRXTimer to set
	 */
	public void setDrxPingTime(double pingDurationDRXTimer) {
		this.drxPingTime = pingDurationDRXTimer;
	}

	/**
	 * @return the longTailDRXTimer
	 */
	public double getDrxLongTime() {
		return drxLongTime;
	}

	/**
	 * @param longTailDRXTimer
	 *            the longTailDRXTimer to set
	 */
	public void setDrxLongTime(double longTailDRXTimer) {
		this.drxLongTime = longTailDRXTimer;
	}

	/**
	 * @return the pingLengthTimer
	 */
	public double getIdlePingTime() {
		return idlePingTime;
	}

	/**
	 * @param pingLengthTimer
	 *            the pingLengthTimer to set
	 */
	public void setIdlePingTime(double pingLengthTimer) {
		this.idlePingTime = pingLengthTimer;
	}

	/**
	 * @return the shortPingsTimer
	 */
	public double getDrxShortPingPeriod() {
		return drxShortPingPeriod;
	}

	/**
	 * @param shortPingsTimer
	 *            the shortPingsTimer to set
	 */
	public void setDrxShortPingPeriod(double shortPingsTimer) {
		this.drxShortPingPeriod = shortPingsTimer;
	}

	/**
	 * @return the longPingsTimer
	 */
	public double getDrxLongPingPeriod() {
		return drxLongPingPeriod;
	}

	/**
	 * @param longPingsTimer
	 *            the longPingsTimer to set
	 */
	public void setDrxLongPingPeriod(double longPingsTimer) {
		this.drxLongPingPeriod = longPingsTimer;
	}

	/**
	 * @return the idlePingsTimer
	 */
	public double getIdlePingPeriod() {
		return idlePingPeriod;
	}

	/**
	 * @param idlePingsTimer
	 *            the idlePingsTimer to set
	 */
	public void setIdlePingPeriod(double idlePingsTimer) {
		this.idlePingPeriod = idlePingsTimer;
	}

	/**
	 * @return the ltePromotionPower
	 */
	public double getLtePromotionPower() {
		return ltePromotionPower;
	}

	/**
	 * @param ltePromotionPower
	 *            the ltePromotionPower to set
	 */
	public void setLtePromotionPower(double ltePromotionPower) {
		this.ltePromotionPower = ltePromotionPower;
	}

	/**
	 * @return the lteShortDRXPower
	 */
	public double getDrxShortPingPower() {
		return drxShortPingPower;
	}

	/**
	 * @param lteShortDRXPower
	 *            the lteShortDRXPower to set
	 */
	public void setDrxShortPingPower(double lteShortDRXPower) {
		this.drxShortPingPower = lteShortDRXPower;
	}

	/**
	 * @return the lteLongDRXPower
	 */
	public double getDrxLongPingPower() {
		return drxLongPingPower;
	}

	/**
	 * @param lteLongDRXPower
	 *            the lteLongDRXPower to set
	 */
	public void setDrxLongPingPower(double lteLongDRXPower) {
		this.drxLongPingPower = lteLongDRXPower;
	}

	/**
	 * @return the lteTailPower
	 */
	public double getLteTailPower() {
		return lteTailPower;
	}

	/**
	 * @param lteTailPower
	 *            the lteTailPower to set
	 */
	public void setLteTailPower(double lteTailPower) {
		this.lteTailPower = lteTailPower;
	}

	/**
	 * @return the lteIDLEPower
	 */
	public double getLteIdlePingPower() {
		return lteIdlePingPower;
	}

	/**
	 * @param lteIDLEPower
	 *            the lteIDLEPower to set
	 */
	public void setLteIdlePingPower(double lteIDLEPower) {
		this.lteIdlePingPower = lteIDLEPower;
	}

	/**
	 * @return the lteIdlePower
	 */
	public double getLteIdlePower() {
		return lteIdlePower;
	}

	/**
	 * @param lteIdlePower
	 *            the lteIdlePower to set
	 */
	public void setLteIdlePower(double lteIdlePower) {
		this.lteIdlePower = lteIdlePower;
	}

	/**
	 * @return the alphaUpConstant
	 */
	public double getLteAlphaUp() {
		return lteAlphaUp;
	}

	/**
	 * @param alphaUpConstant
	 *            the alphaUpConstant to set
	 */
	public void setLteAlphaUp(double alphaUpConstant) {
		this.lteAlphaUp = alphaUpConstant;
	}

	/**
	 * @return the alphaDownConstant
	 */
	public double getLteAlphaDown() {
		return lteAlphaDown;
	}

	/**
	 * @param alphaDownConstant
	 *            the alphaDownConstant to set
	 */
	public void setLteAlphaDown(double alphaDownConstant) {
		this.lteAlphaDown = alphaDownConstant;
	}

	/**
	 * @return the betaConstant
	 */
	public double getLteBeta() {
		return lteBeta;
	}

	/**
	 * @param betaConstant
	 *            the betaConstant to set
	 */
	public void setLteBeta(double betaConstant) {
		this.lteBeta = betaConstant;
	}

}
