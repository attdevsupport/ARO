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
package com.att.aro.core.configuration.pojo;

/**
 * Represents a device profile that is used as a model of a 3G device when analyzing trace data.
 * @author EDS team
 * Refactored by Borey Sao
 * Date: October 15, 2014
 */
public class Profile3G extends Profile {
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
	
	public void setDchFachTimer(double dchFachTimer) {
		this.dchFachTimer = dchFachTimer;
	}
	public void setFachIdleTimer(double fachIdleTimer) {
		this.fachIdleTimer = fachIdleTimer;
	}
	public void setIdleDchPromoMin(double idleDchPromoMin) {
		this.idleDchPromoMin = idleDchPromoMin;
	}
	public void setIdleDchPromoAvg(double idleDchPromoAvg) {
		this.idleDchPromoAvg = idleDchPromoAvg;
	}
	public void setIdleDchPromoMax(double idleDchPromoMax) {
		this.idleDchPromoMax = idleDchPromoMax;
	}
	public void setFachDchPromoMin(double fachDchPromoMin) {
		this.fachDchPromoMin = fachDchPromoMin;
	}
	public void setFachDchPromoAvg(double fachDchPromoAvg) {
		this.fachDchPromoAvg = fachDchPromoAvg;
	}
	public void setFachDchPromoMax(double fachDchPromoMax) {
		this.fachDchPromoMax = fachDchPromoMax;
	}
	public void setRlcUlTh(int rlcUlTh) {
		this.rlcUlTh = rlcUlTh;
	}
	public void setRlcDlTh(int rlcDlTh) {
		this.rlcDlTh = rlcDlTh;
	}
	public void setDchTimerResetSize(int dchTimerResetSize) {
		this.dchTimerResetSize = dchTimerResetSize;
	}
	public void setDchTimerResetWin(double dchTimerResetWin) {
		this.dchTimerResetWin = dchTimerResetWin;
	}
	public void setRlcUlRateP2(double rlcUlRateP2) {
		this.rlcUlRateP2 = rlcUlRateP2;
	}
	public void setRlcUlRateP1(double rlcUlRateP1) {
		this.rlcUlRateP1 = rlcUlRateP1;
	}
	public void setRlcUlRateP0(double rlcUlRateP0) {
		this.rlcUlRateP0 = rlcUlRateP0;
	}
	public void setRlcDlRateP2(double rlcDlRateP2) {
		this.rlcDlRateP2 = rlcDlRateP2;
	}
	public void setRlcDlRateP1(double rlcDlRateP1) {
		this.rlcDlRateP1 = rlcDlRateP1;
	}
	public void setRlcDlRateP0(double rlcDlRateP0) {
		this.rlcDlRateP0 = rlcDlRateP0;
	}
	public void setPowerDch(double powerDch) {
		this.powerDch = powerDch;
	}
	public void setPowerFach(double powerFach) {
		this.powerFach = powerFach;
	}
	public void setPowerIdle(double powerIdle) {
		this.powerIdle = powerIdle;
	}
	public void setPowerIdleDch(double powerIdleDch) {
		this.powerIdleDch = powerIdleDch;
	}
	public void setPowerFachDch(double powerFachDch) {
		this.powerFachDch = powerFachDch;
	}
	@Override
	public ProfileType getProfileType() {
		return ProfileType.T3G;
	}

}
