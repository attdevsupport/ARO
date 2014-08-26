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
import java.io.Serializable;
import java.util.List;
import java.util.Properties;

/**
 * Represents a device profile that is used as a model of a 3G device when analyzing trace data.
 */
public class Profile3G extends Profile implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * The value of the timer for (direct channel) DCH to (forward access channel) FACH.
	 */
	public static final String DCH_FACH_TIMER = "DCH_FACH_TIMER";

	/**
	 * The FACH (Forward access channel) to IDLE timer. 
	 */
	public static final String FACH_IDLE_TIMER = "FACH_IDLE_TIMER";

	/**
	 * The minimum amount of energy used when the RRC state is promoted from 
	 * IDLE to DCH (Active).
	 */
	public static final String IDLE_DCH_PROMO_MIN = "IDLE_DCH_PROMO_MIN";

	/**
	 * The average amount of energy used when the RRC state is promoted from 
	 * IDLE to DCH (Active).
	 */
	public static final String IDLE_DCH_PROMO_AVG = "IDLE_DCH_PROMO_AVG";

	/**
	 * The maximum amount of energy used when the RRC state is promoted from 
	 * IDLE to DCH (Active).
	 */
	public static final String IDLE_DCH_PROMO_MAX = "IDLE_DCH_PROMO_MAX";

	/**
	 * The minimum amount of energy used when the RRC state is promoted from IDLE to 
	 * DCH (Active) and FACH (Forward access channel) to DCH (Active).
	 */
	public static final String FACH_DCH_PROMO_MIN = "FACH_DCH_PROMO_MIN";

	/**
	 * The average amount of energy used when the RRC state is promoted from IDLE to 
	 * DCH (Active) and FACH (Forward access channel) to DCH (Active).
	 */
	public static final String FACH_DCH_PROMO_AVG = "FACH_DCH_PROMO_AVG";

	/**
	 * The maximum amount of energy used when the RRC state is promoted from IDLE to 
	 * DCH (Active) and FACH (Forward access channel) to DCH (Active).
	 */
	public static final String FACH_DCH_PROMO_MAX = "FACH_DCH_PROMO_MAX";

	/**
	 * The RLC threshold value for up link.
	 */
	public static final String RLC_UL_TH = "RLC_UL_TH";

	/**
	 * The RLC threshold value for down link.
	 */
	public static final String RLC_DL_TH = "RLC_DL_TH";

	/**
	 * The threshold for resetting the DCH (Active) timer (in bytes).
	 */
	public static final String DCH_TIMER_RESET_SIZE = "DCH_TIMER_RESET_SIZE";

	/**
	 * The timing window for the resetting DCH (Active) timer (in seconds).
	 */
	public static final String DCH_TIMER_RESET_WIN = "DCH_TIMER_RESET_WIN";

	/**
	 * The RLC consumption rate (^2) for up link.
	 */
	public static final String RLC_UL_RATE_P2 = "RLC_UL_RATE_P2";

	/**
	 * The RLC consumption rate (^1) for up link.
	 */
	public static final String RLC_UL_RATE_P1 = "RLC_UL_RATE_P1";

	/**
	 * The RLC consumption rate (^0) for up link.
	 */
	public static final String RLC_UL_RATE_P0 = "RLC_UL_RATE_P0";

	/**
	 * The RLC consumption rate (^2) for down link.
	 */
	public static final String RLC_DL_RATE_P2 = "RLC_DL_RATE_P2";

	/**
	 * The RLC consumption rate (^1) for down link.
	 */
	public static final String RLC_DL_RATE_P1 = "RLC_DL_RATE_P1";

	/**
	 * The RLC consumption rate (^0) for down link.
	 */
	public static final String RLC_DL_RATE_P0 = "RLC_DL_RATE_P0";

	/**
	 * The amount of power (in watts) that should be used when the RRC state is DCH (Active).
	 */
	public static final String POWER_DCH = "POWER_DCH";

	/**
	 * The amount of power (in watts) that should be used when the RRC state is FACH (Standby).
	 */
	public static final String POWER_FACH = "POWER_FACH";

	/**
	 * The amount of power (in watts) that should be used when the RRC state is IDLE.
	 */
	public static final String POWER_IDLE = "POWER_IDLE";

	/**
	 * The average amount of power (in watts) that should be used when the RRC state is promoted 
	 * from IDLE to-> DCH (Active) 
	 */
	public static final String POWER_IDLE_DCH = "POWER_IDLE_DCH";

	/**
	 * The average amount of power (in watts) that should be used when the RRC state is promoted 
	 * from FACH (Standby) to ->DCH (Active).
	 */
	public static final String POWER_FACH_DCH = "POWER_FACH_DCH";

	private double dchFachTimer;
	private double fachIdleTimer;
	private double idleDchPromoMin;
	private double idleDchPromoAvg;
	private double idleDchPromoMax;
	private double fachDchPromoMin;
	private double fachDchPromoAvg;
	private double fachDchPromoMax;

	private int rlcUlTh;
	private int rlcDlTh;
	private int dchTimerResetSize;
	private double dchTimerResetWin;
	private double rlcUlRateP2;
	private double rlcUlRateP1;
	private double rlcUlRateP0;
	private double rlcDlRateP2;
	private double rlcDlRateP1;
	private double rlcDlRateP0;

	private double powerDch;
	private double powerFach;
	private double powerIdle;
	private double powerIdleDch;
	private double powerFachDch;

	/**
	 * Initializes an instance of the Profile3G class.
	 */
	public Profile3G() {
		super();
	}

	/**
	 * Initializes an instance of the Profile3G class, using the specified properties.
	 * 
	 * @param file
	 *            The file where the profile can be saved. Can be null.
	 * @param properties
	 * 			  The properties to be set in the profile.
	 * @throws ProfileException
	 */
	public Profile3G(File file, Properties properties) throws ProfileException {
		super(file, properties);
	}

	/**
	 * Initializes a new instance of the Profile3G class, using the specified name and
	 * properties.
	 * 
	 * @param name
	 *            The profile name.
	 * @param properties
	 *            The properties to be set in the profile.
	 * @throws ProfileException
	 */
	public Profile3G(String name, Properties properties) throws ProfileException {
		super(name, properties);
	}

	/**
	 * Returns the type of profile. Subclasses of this class must identify the profile type.
	 * @see com.att.aro.model.Profile#getProfileType()
	 * 
	 * @return One of the values of the ProfileType enumeration.
	 */
	@Override
	public ProfileType getProfileType() {
		return ProfileType.T3G;
	}

	/**
	 * Initializes Profile class members using values from the specified Properties object. 
	 * This method is overridden by Sub-classes to initialize properties in a Profile object.
	 * 
	 * @param properties
	 *            The profile values to be set.
	 * @throws ProfileException
	 */
	@Override
	protected void setProperties(Properties properties) {

		dchFachTimer = readDouble(properties, DCH_FACH_TIMER, 5);
		fachIdleTimer = readDouble(properties, FACH_IDLE_TIMER, 12);
		idleDchPromoMin = readDouble(properties, IDLE_DCH_PROMO_MIN, 1.5);
		idleDchPromoAvg = readDouble(properties, IDLE_DCH_PROMO_AVG, 2.0);
		idleDchPromoMax = readDouble(properties, IDLE_DCH_PROMO_MAX, 4.0);
		fachDchPromoMin = readDouble(properties, FACH_DCH_PROMO_MIN, 0.8);
		fachDchPromoAvg = readDouble(properties, FACH_DCH_PROMO_AVG, 1.5);
		fachDchPromoMax = readDouble(properties, FACH_DCH_PROMO_MAX, 3.0);
		rlcUlTh = readInt(properties, RLC_UL_TH, 543);
		rlcDlTh = readInt(properties, RLC_DL_TH, 475);
		dchTimerResetSize = readInt(properties, DCH_TIMER_RESET_SIZE, 320);
		dchTimerResetWin = readDouble(properties, DCH_TIMER_RESET_WIN, 0.3);
		rlcUlRateP2 = readDouble(properties, RLC_UL_RATE_P2, 0.0014);
		rlcUlRateP1 = readDouble(properties, RLC_UL_RATE_P1, 1.6);
		rlcUlRateP0 = readDouble(properties, RLC_UL_RATE_P0, 20.0);
		rlcDlRateP2 = readDouble(properties, RLC_DL_RATE_P2, 0);
		rlcDlRateP1 = readDouble(properties, RLC_DL_RATE_P1, 0.1);
		rlcDlRateP0 = readDouble(properties, RLC_DL_RATE_P0, 10);
		powerDch = readDouble(properties, POWER_DCH, 0.7);
		powerFach = readDouble(properties, POWER_FACH, 0.35);
		powerIdle = readDouble(properties, POWER_IDLE, 0);
		powerIdleDch = readDouble(properties, POWER_IDLE_DCH, 0.53);
		powerFachDch = readDouble(properties, POWER_FACH_DCH, 0.55);
	}

	/**
	 * Saves the current profile values in the specified  Properties object. This method is 
	 * used by sub-classes to save member values to a properties object for persistence.
	 * 
	 * @param props
	 *            The properties object in which to store the values.
	 * 
	 * @throws IOException
	 */
	@Override
	protected synchronized void saveProperties(Properties props) {

		props.setProperty(DCH_FACH_TIMER, String.valueOf(dchFachTimer));
		props.setProperty(FACH_IDLE_TIMER, String.valueOf(fachIdleTimer));
		props.setProperty(IDLE_DCH_PROMO_MIN, String.valueOf(idleDchPromoMin));
		props.setProperty(IDLE_DCH_PROMO_AVG, String.valueOf(idleDchPromoAvg));
		props.setProperty(IDLE_DCH_PROMO_MAX, String.valueOf(idleDchPromoMax));
		props.setProperty(FACH_DCH_PROMO_MIN, String.valueOf(fachDchPromoMin));
		props.setProperty(FACH_DCH_PROMO_AVG, String.valueOf(fachDchPromoAvg));
		props.setProperty(FACH_DCH_PROMO_MAX, String.valueOf(fachDchPromoMax));
		props.setProperty(RLC_UL_TH, String.valueOf(rlcUlTh));
		props.setProperty(RLC_DL_TH, String.valueOf(rlcDlTh));
		props.setProperty(DCH_TIMER_RESET_SIZE, String.valueOf(dchTimerResetSize));
		props.setProperty(DCH_TIMER_RESET_WIN, String.valueOf(dchTimerResetWin));
		props.setProperty(RLC_UL_RATE_P2, String.valueOf(rlcUlRateP2));
		props.setProperty(RLC_UL_RATE_P1, String.valueOf(rlcUlRateP1));
		props.setProperty(RLC_UL_RATE_P0, String.valueOf(rlcUlRateP0));
		props.setProperty(RLC_DL_RATE_P2, String.valueOf(rlcDlRateP2));
		props.setProperty(RLC_DL_RATE_P1, String.valueOf(rlcDlRateP1));
		props.setProperty(RLC_DL_RATE_P0, String.valueOf(rlcDlRateP0));
		props.setProperty(POWER_DCH, String.valueOf(powerDch));
		props.setProperty(POWER_FACH, String.valueOf(powerFach));
		props.setProperty(POWER_IDLE, String.valueOf(powerIdle));
		props.setProperty(POWER_IDLE_DCH, String.valueOf(powerIdleDch));
		props.setProperty(POWER_FACH_DCH, String.valueOf(powerFachDch));
	}

	/**
	 * Returns the value of the timer for DCH and FACH.
	 * 
	 * @return The timer value.
	 */
	public double getDchFachTimer() {
		return dchFachTimer;
	}

	/**
	 * Returns the idle timer for FACH.
	 * 
	 * @return The timer value.
	 */
	public double getFachIdleTimer() {
		return fachIdleTimer;
	}

	/**
	 * Returns the minimum amount of energy used when the RRC state is promoted from IDLE to DCH (Active).
	 * 
	 * @return A double value that is the minimum.
	 */
	public double getIdleDchPromoMin() {
		return idleDchPromoMin;
	}

	/**
	 * Returns the average amount of energy used when the RRC state is promoted from IDLE to DCH (Active).
	 * 
	 * @return A double value that is the average.
	 */
	public double getIdleDchPromoAvg() {
		return idleDchPromoAvg;
	}

	/**
	 * Returns the maximum amount of energy used when the RRC state is promoted from IDLE to DCH (Active).  
	 * 
	 * @return A double value that is the maximum.
	 */
	public double getIdleDchPromoMax() {
		return idleDchPromoMax;
	}

	/**
	 * Returns the minimum amount of energy used when the RRC state is promoted from IDLE to DCH 
	 * (Active) and FACH (Standby) to DCH (Active).
	 * 
	 * @return A double value that is the minimum.
	 */
	public double getFachDchPromoMin() {
		return fachDchPromoMin;
	}

	/**
	 * Returns the average amount of energy used when the RRC state is promoted from IDLE to DCH 
	 * (Active) and FACH (Standby) to DCH (Active).
	 * 
	 * @return  A double value that is the average.
	 */
	public double getFachDchPromoAvg() {
		return fachDchPromoAvg;
	}

	/**
	 * Returns the maximum amount of energy used when the RRC state is promoted from IDLE to 
	 * DCH (Active) and FACH (Standby) to DCH (Active).  
	 * 
	 * @return A double value that is the maximum.
	 */
	public double getFachDchPromoMax() {
		return fachDchPromoMax;
	}

	/**
	 * Returns the uplink threshold.
	 * 
	 * @return The uplink threshold value.
	 */
	public int getRlcUlTh() {
		return rlcUlTh;
	}

	/**
	 * Returns the downlink threshold.
	 * 
	 * @return The downlink threshold value.
	 */
	public int getRlcDlTh() {
		return rlcDlTh;
	}

	/**
	 * Returns the size of DCH timer.
	 * 
	 * @return The size of the DCH timer.
	 */
	public int getDchTimerResetSize() {
		return dchTimerResetSize;
	}

	/**
	 * Returns the value of DCH timer for ResetWin.
	 * 
	 * @return The timer value.
	 */
	public double getDchTimerResetWin() {
		return dchTimerResetWin;
	}

	/**
	 * Returns the value of uplink rateP2.
	 * 
	 * @return The P2 uplink rate value.
	 */
	public double getRlcUlRateP2() {
		return rlcUlRateP2;
	}

	/**
	 * Returns the value of uplink rateP1.
	 * 
	 * @return The P1 uplink rate value.
	 */
	public double getRlcUlRateP1() {
		return rlcUlRateP1;
	}

	/**
	 * Returns the value of uplink rateP0.
	 * 
	 * @return The P0 uplink rate value.
	 */
	public double getRlcUlRateP0() {
		return rlcUlRateP0;
	}

	/**
	 * Returns the value of downlink rateP2.
	 * 
	 * @return The P2 downlink rate value.
	 */
	public double getRlcDlRateP2() {
		return rlcDlRateP2;
	}

	/**
	 * Returns the value of downlink rateP1.
	 * 
	 * @return The P1 downlink rate value.
	 */
	public double getRlcDlRateP1() {
		return rlcDlRateP1;
	}

	/**
	 * Returns the value of downlink rateP0.
	 * 
	 * @return . The P0 downlink rate value.
	 */
	public double getRlcDlRateP0() {
		return rlcDlRateP0;
	}

	/**
	 * Returns the total amount of energy used when the device is in the DCH RRC state.
	 * 
	 * @return  The DCH power value.
	 */
	public double getPowerDch() {
		return powerDch;
	}

	/**
	 * Returns the total amount of energy used when the device is in the FACH RRC state.
	 * 
	 * @return The FACH power value.
	 */
	public double getPowerFach() {
		return powerFach;
	}

	/**
	 * Returns the total amount of energy used when the device is idle.  
	 * 
	 * @return The idle power value.
	 */
	public double getPowerIdle() {
		return powerIdle;
	}

	/**
	 * Returns the total amount of energy used when the device is idle during the DCH RRC state.
	 * 
	 * @return The idle DCH power value.
	 */
	public double getPowerIdleDch() {
		return powerIdleDch;
	}

	/**
	 * Returns the total amount of energy used when the device is in the DCH and FACH RRC states.
	 * 
	 * @return The DCH and FACH power value.
	 */
	public double getPowerFachDch() {
		return powerFachDch;
	}

	/**
	 * A utility method for calculating RRC energy.
	 * 
	 * @param time1
	 *            The begin time for the RRC state.
	 * @param time2
	 *            The end time for the RRC state.
	 * @param state
	 *            An RRCState enumeration value that indicates the RRC energy state.
	 * @return The energy consumption value for the RRC state.
	 */
	@Override
	public double energy(double time1, double time2, RRCState state, List<PacketInfo> packets) {
		double deltaTime = time2 - time1;

		switch (state) {
		case STATE_DCH:
		case TAIL_DCH:
			return deltaTime * powerDch;
		case STATE_FACH:
		case TAIL_FACH:
			return deltaTime * powerFach;
		case STATE_IDLE:
			return deltaTime * powerIdle;
		case PROMO_IDLE_DCH:
			return deltaTime * powerIdleDch;
		case PROMO_FACH_DCH:
			return deltaTime * powerFachDch;
		default:
			assert (true);
		}
		return 0.0f;
	}
}// end Profile Class