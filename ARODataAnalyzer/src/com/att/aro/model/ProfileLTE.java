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
 * Represents a device profile that is used as a model of an  LTE device when analyzing trace data.
 */
public class ProfileLTE extends Profile {
	private static final long serialVersionUID = 1L;

	/**
	 * The amount of time (in seconds) spent in promotion from the Idle state to the CR state. 
	 */
	public static final String T_PROMOTION = "T_PROMOTION";

	/**
	 * The amount of inactive time (in seconds) spent in the CR state before changing to the DRX state. 
	 */
	public static final String INACTIVITY_TIMER = "INACTIVITY_TIMER";

	/**
	 * The amount of time (in seconds) spent in the short DRX state. 
	 */
	public static final String T_SHORT_DRX = "T_SHORT_DRX";

	/**
	 * The length of a ping (in seconds) during the DRX state. 
	 */
	public static final String T_DRX_PING = "T_DRX_PING";

	/**
	 * The amount of time (in seconds) spent in the Long DRX state. 
	 */
	public static final String T_LONG_DRX = "T_LONG_DRX";

	/**
	 * The length of a ping (in seconds) during the IDLE state. 
	 */
	public static final String T_IDLE_PING = "T_IDLE_PING";

	/**
	 * The length of the period between pings (in seconds) spent in the Short DRX state.
	 */
	public static final String T_SHORT_DRX_PING_PERIOD = "T_SHORT_DRX_PING_PERIOD";

	/**
	 * The length of the period between pings (in seconds) spent in the Long DRX state. 
	 */
	public static final String T_LONG_DRX_PING_PERIOD = "T_LONG_DRX_PING_PERIOD";

	/**
	 * The length of the period between pings (in seconds) during the IDLE state. 
	 */
	public static final String T_IDLE_PING_PERIOD = "T_IDLE_PING_PERIOD";

	/**
	 * The average power (in watts) during promotion.
	 */
	public static final String P_PROMOTION = "P_PROMOTION";

	/**
	 * The average power (in watts) of a ping during the short DRX state. 
	 */
	public static final String P_SHORT_DRX_PING = "P_SHORT_DRX_PING";

	/**
	 * The average power (in watts) of a ping during the long DRX state. 
	 */
	public static final String P_LONG_DRX_PING = "P_LONG_DRX_PING";

	/**
	 * The average power (in watts) during a tail state. 
	 */
	public static final String P_TAIL = "P_TAIL";

	/**
	 * The average power (in watts) of a ping in IDLE state. 
	 */
	public static final String P_IDLE_PING = "P_IDLE_PING";

	/**
	 * The average power (in watts) during the IDLE state.
	 */
	public static final String P_IDLE = "P_IDLE";

	/**
	 * The multiplier used for calculating throughput upload energy (expressed as mW/Mbps).
	 */
	public static final String LTE_ALPHA_UP = "LTE_ALPHA_UP";

	/**
	 * The multiplier used for calculating throughput download energy (expressed in mW/Mbps).
	 */
	public static final String LTE_ALPHA_DOWN = "LTE_ALPHA_DOWN";

	/**
	 * The baseline value (in watts) for CR state energy before throughput modifiers 
	 * (ALPHA_DOWN and ALPHA_UP) are added.
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
	 * Initializes an instance of the ProfileLTE class.
	 */
	public ProfileLTE() {
		super();
	}

	/**
	 * Initializes a new instance of the ProfileLTE class using the specified properties. 
	 * This constructor also allows you to specify a file where profile properties can be 
	 * saved for persistence.
	 * 
	 * @param file
	 *            A file where the profile can be saved. This argument can be null.
	 * @param properties
	 *            The properties to be set in the profile. 
	 * @throws ProfileException
	 */
	public ProfileLTE(File file, Properties properties) throws ProfileException {
		super(file, properties);
	}

	/**
	 * Initializes a new instance of the ProfileLTE class, using the specified profile name and properties. 
	 * 
	 * @param name
	 *            The name of the profile.
	 * @param properties
	 *            The properties to be set in the profile. 
	 * @throws ProfileException
	 */
	public ProfileLTE(String name, Properties properties)
			throws ProfileException {
		super(name, properties);
	}

	/**
	 * Returns the type of profile. Subclasses of this class must identify the profile type. 
	 * @see com.att.aro.model.Profile#getProfileType()
	 * 
	 * @return The profile type. One of the values of the ProfileType enumeration.
	 */
	@Override
	public ProfileType getProfileType() {
		return ProfileType.LTE;
	}

	/**
	 * Calculates the energy used by the specified RRC state over a period of
	 * time using this LTE profile.
	 * 
	 * @param time1
	 *            The begin time.
	 * @param time2
	 *            The end time.
	 * @param state
	 *            The RRC state. An RRCState enumeration value that indicates the RRC energy state.
	 * @param packets 
	 * 			  A List of packets passed during the specified timeline that may be 
	 * 			  used in determining the amount of energy used. 
	 * @return The energy consumed for the specified RRC state.
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
	 * Initializes Profile class members using values from the specified Properties object.
	 * 
	 * @param properties
	 *            The profile values to be set.
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
	 * Saves the current profile values in the specified Properties object. This method is used 
	 * by sub-classes to save member values to a properties object for persistence.
	 * 
	 * @param props
	 *             The properties object in which to store the values.
	 * @throws IOException
	 */
	@Override
	protected synchronized void saveProperties(Properties props) {
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
	 * Returns the amount of time spent in promotion from an IDLE or low power RRC state 
	 * to an active or high power RRC state.
	 * @return The promotion time, in seconds.
	 */
	public double getPromotionTime() {
		return promotionTime;
	}

	/**
	 * Sets the amount of time (in seconds) spent in promotion from IDLE or low power RRC 
	 * state to an active or high power RRC state.
	 * @param tPromotionTimer
	 *           The promotion time to set.
	 */
	public void setPromotionTime(double tPromotionTimer) {
		this.promotionTime = tPromotionTimer;
	}

	/**
	 * Returns the amount of time spent in an Inactive state.
	 * @return The total inactive time, in seconds.
	 */
	public double getInactivityTimer() {
		return inactivityTimer;
	}

	/**
	 * Sets the amount of time (in seconds) spent in an Inactive state.
	 * @param inactivityTimer
	 *            The total inactive time to set.
	 */
	public void setInactivityTimer(double inactivityTimer) {
		this.inactivityTimer = inactivityTimer;
	}

	/**
	 * Returns the amount of time spent in the Short DRX state.
	 * @return The Short DRX timer, in seconds.
	 */
	public double getDrxShortTime() {
		return drxShortTime;
	}

	/**
	 * Sets the amount of time (in seconds) spent in the Short DRX state.
	 * @param shortDRXTimer
	 *           The Short DRX timer to set.
	 */
	public void setDrxShortTime(double shortDRXTimer) {
		this.drxShortTime = shortDRXTimer;
	}

	/**
	 * Returns the amount of time used by pings in the Long DRX state.
	 * @return The Long DRX ping duration time, in seconds.
	 */
	public double getDrxPingTime() {
		return drxPingTime;
	}

	/**
	 * Sets the amount of time (in seconds) used by pings in the Long DRX state.
	 * @param pingDurationDRXTimer
	 *            The Long DRX ping duration time to set.
	 */
	public void setDrxPingTime(double pingDurationDRXTimer) {
		this.drxPingTime = pingDurationDRXTimer;
	}

	/**
	 * Returns the amount of time spent in the Long DRX tail state.
	 * @return The Long DRX tail timer, in seconds.
	 */
	public double getDrxLongTime() {
		return drxLongTime;
	}

	/**
	 * Sets the amount of time (in seconds) spent in the Long DRX tail state.
	 * @param longTailDRXTimer
	 *           The long DRX tail timer to set.
	 */
	public void setDrxLongTime(double longTailDRXTimer) {
		this.drxLongTime = longTailDRXTimer;
	}

	/**
	 * Returns the amount of time used by pings in the IDLE state.
	 * @return The IDLE ping duration time, in seconds.
	 */
	public double getIdlePingTime() {
		return idlePingTime;
	}

	/**
	 * Sets the amount of time (in seconds) used by pings in the IDLE state.
	 * @param pingLengthTimer
	 *           The IDLE ping time to set.
	 */
	public void setIdlePingTime(double pingLengthTimer) {
		this.idlePingTime = pingLengthTimer;
	}

	/**
	 * Returns the period of time spent between pings in the Short DRX state.
	 * @return The Short DRX ping period, in seconds.
	 */
	public double getDrxShortPingPeriod() {
		return drxShortPingPeriod;
	}

	/**
	 * Sets the period of time (in seconds) spent between pings in the Short DRX state.
	 * @param shortPingsTimer
	 *           The Short DRX ping period to set.
	 */
	public void setDrxShortPingPeriod(double shortPingsTimer) {
		this.drxShortPingPeriod = shortPingsTimer;
	}

	/**
	 * Returns the period of time spent between pings in the Long DRX state.
	 * @return The Long DRX ping period, in seconds.
	 */
	public double getDrxLongPingPeriod() {
		return drxLongPingPeriod;
	}

	/**
	 * Sets the period of time (in seconds ) spent between pings in the Long DRX state.
	 * @param longPingsTimer
	 *           The Long DRX ping period to set.
	 */
	public void setDrxLongPingPeriod(double longPingsTimer) {
		this.drxLongPingPeriod = longPingsTimer;
	}

	/**
	 * Returns the period of time spent between pings in the IDLE state.
	 * @return The IDLE pings period, in seconds.
	 */
	public double getIdlePingPeriod() {
		return idlePingPeriod;
	}

	/**
	 * Sets the period of time (in seconds) spent between pings in the IDLE state.
	 * @param idlePingsTimer
	 *           idlePingsTimer - The IDLE pings period to set
	 */
	public void setIdlePingPeriod(double idlePingsTimer) {
		this.idlePingPeriod = idlePingsTimer;
	}

	/**
	 * Returns the average power used during promotion from an IDLE or low power 
	 * RRC state to an active or high power RRC state.
	 * @return The LTE promotion power, in watts.
	 */
	public double getLtePromotionPower() {
		return ltePromotionPower;
	}

	/**
	 * Sets the average power (in watts) used during promotion from an IDLE or 
	 * low power RRC state to an active or high power RRC state.
	 * @param ltePromotionPower
	 *            The LTE promotion power to set.
	 */
	public void setLtePromotionPower(double ltePromotionPower) {
		this.ltePromotionPower = ltePromotionPower;
	}

	/**
	 * Returns the amount of power used by pings in the Short DRX state.
	 * @return  The Short DRX ping power, in watts.
	 */
	public double getDrxShortPingPower() {
		return drxShortPingPower;
	}

	/**
	 * Sets the amount of power (in watts) used by pings in the Short DRX state.
	 * @param lteShortDRXPower
	 *           The Short DRX ping power to set.
	 */
	public void setDrxShortPingPower(double lteShortDRXPower) {
		this.drxShortPingPower = lteShortDRXPower;
	}

	/**
	 * Returns the amount of power used by pings in the Long DRX state.
	 * @return The Long DRX ping power, in watts.
	 */
	public double getDrxLongPingPower() {
		return drxLongPingPower;
	}

	/**
	 * Sets the amount of power (in watts) used by pings in the Long DRX state.
	 * @param lteLongDRXPower
	 *            The Long DRX ping power to set.
	 */
	public void setDrxLongPingPower(double lteLongDRXPower) {
		this.drxLongPingPower = lteLongDRXPower;
	}

	/**
	 * Returns the average power used during a tail state.
	 * @return The LTE tail power, in watts.
	 */
	public double getLteTailPower() {
		return lteTailPower;
	}

	/**
	 * Sets the average power (in watts) used during a tail state.
	 * @param lteTailPower
	 *            The LTE tail power to set.
	 */
	public void setLteTailPower(double lteTailPower) {
		this.lteTailPower = lteTailPower;
	}

	/**
	 * Returns, the average power of a ping in the IDLE state.
	 * @return The LTE IDLE ping power, in watts.
	 */
	public double getLteIdlePingPower() {
		return lteIdlePingPower;
	}

	/**
	 * Sets the average power (in watts) of a ping in the IDLE state.
	 * @param lteIDLEPower
	 *            The LTE IDLE ping power to set.
	 */
	public void setLteIdlePingPower(double lteIDLEPower) {
		this.lteIdlePingPower = lteIDLEPower;
	}

	/**
	 * Returns the average power used during the IDLE state.
	 * @return The LTE IDLE power, in watts.
	 */
	public double getLteIdlePower() {
		return lteIdlePower;
	}

	/**
	 * Sets the average power (in watts) used during the IDLE state.
	 * @param lteIdlePower
	 *            The LTE IDLE power to set.
	 */
	public void setLteIdlePower(double lteIdlePower) {
		this.lteIdlePower = lteIdlePower;
	}

	/**
	 * Returns the constant multiplier used for calculating throughput upload energy.
	 * @return The LTE Alpha Up constant, in mW/Mbps.
	 */
	public double getLteAlphaUp() {
		return lteAlphaUp;
	}

	/**
	 * Sets the constant multiplier, expressed in mW/Mbps, used for calculating throughput upload energy.
	 * @param alphaUpConstant
	 *           The LTE Alpha Up constant to set.
	 */
	public void setLteAlphaUp(double alphaUpConstant) {
		this.lteAlphaUp = alphaUpConstant;
	}

	/**
	 * Returns the constant multiplier used for calculating throughput download energy.
	 * @return The LTE Alpha Down constant, in mW/Mbps.
	 */
	public double getLteAlphaDown() {
		return lteAlphaDown;
	}

	/**
	 * Sets the constant multiplier, expressed in mW/Mbps, used for calculating throughput download energy.
	 * @param alphaDownConstant
	 *            The LTE Alpha Down constant to set.
	 */
	public void setLteAlphaDown(double alphaDownConstant) {
		this.lteAlphaDown = alphaDownConstant;
	}

	/**
	 * Returns the constant baseline value for CR state energy before the throughput 
	 * modifiers(ALPHA_DOWN and ALPHA_UP) are added.
	 * @return The LTE Beta constant, in watts.
	 */
	public double getLteBeta() {
		return lteBeta;
	}

	/**
	 * Sets the constant baseline value, expressed in watts, for CR state energy before the throughput 
	 * modifiers(ALPHA_DOWN and ALPHA_UP) are added.
	 * @param betaConstant
	 *           The LTE Beta constant to set.
	 */
	public void setLteBeta(double betaConstant) {
		this.lteBeta = betaConstant;
	}

}
