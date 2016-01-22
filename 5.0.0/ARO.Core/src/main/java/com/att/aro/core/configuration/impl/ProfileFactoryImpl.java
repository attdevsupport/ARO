/**
 * Copyright 2016 AT&T
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
package com.att.aro.core.configuration.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;

import com.att.aro.core.configuration.IProfileFactory;
import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.configuration.pojo.Profile3G;
import com.att.aro.core.configuration.pojo.ProfileLTE;
import com.att.aro.core.configuration.pojo.ProfileType;
import com.att.aro.core.configuration.pojo.ProfileWiFi;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.packetanalysis.IThroughputCalculator;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.RRCState;
import com.att.aro.core.packetanalysis.pojo.Throughput;

/**
 * methods to handle mobile profile creation and saving for 3G, LTE and WiFi
 */
public class ProfileFactoryImpl implements IProfileFactory {

	@Autowired
	private IThroughputCalculator throughputcalculator;
	@Autowired
	private IFileManager filemanager;
	
	@Override
	public Profile create(ProfileType typeParm, Properties prop) {
		Profile prof = null;
		ProfileType type = typeParm == null ? ProfileType.LTE : typeParm;
		switch(type){
		case LTE:
			prof = createLTE(prop);
			break;
		case T3G:
			prof = create3G(prop);
			break;
		case WIFI:
			prof = createWiFi(prop);
			break;
		default:
			return null;
		}
		return prof;
	}
	@Override
	public double energy3G(double time1, double time2, RRCState state, Profile3G prof) {
		double deltaTime = time2 - time1;

		switch (state) {
		case STATE_DCH:
		case TAIL_DCH:
			return deltaTime * prof.getPowerDch();
		case STATE_FACH:
		case TAIL_FACH:
			return deltaTime * prof.getPowerFach();
		case STATE_IDLE:
			return deltaTime * prof.getPowerIdle();
		case PROMO_IDLE_DCH:
			return deltaTime * prof.getPowerIdleDch();
		case PROMO_FACH_DCH:
			return deltaTime * prof.getPowerFachDch();
		default:
			assert (true);
		}
		return 0.0f;
	}
	/**
	 * create 3G profile using default hardcoded value
	 * @return
	 */
	@Override
	public Profile create3Gdefault(){
		Properties props = new Properties();//empty => use default value hardcoded
		return create3G(props);
	}
	/**
	 * create 3G profile using embedded resource file i997.conf
	 * @return
	 * @throws IOException
	 */
	@Override
	public Profile create3GFromDefaultResourceFile() throws IOException{
		InputStream input = getClass().getResourceAsStream("/i997.conf"); 
		return this.create3G(input);
	}
	/**
	 * get 3G profile from a file path
	 * @param filepath
	 * @return
	 * @throws IOException
	 */
	@Override
	public Profile create3GFromFilePath(String filepath) throws IOException{
		InputStream input = filemanager.getFileInputStream(filepath);
		return this.create3G(input);
	}
	/**
	 * create 3G profile from java InputStream
	 * @param input
	 * @return
	 * @throws IOException
	 */
	@Override
	public Profile create3G(InputStream input) throws IOException{
		Properties prop = new Properties();
		prop.load(input);
		return this.create3G(prop);
	}
	@Override
	public Profile create3G(Properties properties){
		Profile3G prof = new Profile3G();
		this.createBaseData(prof, properties);
		
		prof.setDevice(readString(properties, ProfileLTE.DEVICE, "Default 3G Device"));
		prof.setDchFachTimer(readDouble(properties, Profile3G.DCH_FACH_TIMER, 5));
		prof.setFachIdleTimer(readDouble(properties, Profile3G.FACH_IDLE_TIMER, 12));
		prof.setIdleDchPromoMin(readDouble(properties, Profile3G.IDLE_DCH_PROMO_MIN, 1.5));
		prof.setIdleDchPromoAvg(readDouble(properties, Profile3G.IDLE_DCH_PROMO_AVG, 2.0));
		prof.setIdleDchPromoMax(readDouble(properties, Profile3G.IDLE_DCH_PROMO_MAX, 4.0));
		prof.setFachDchPromoMin(readDouble(properties, Profile3G.FACH_DCH_PROMO_MIN, 0.8));
		prof.setFachDchPromoAvg(readDouble(properties, Profile3G.FACH_DCH_PROMO_AVG, 1.5));
		prof.setFachDchPromoAvg(readDouble(properties, Profile3G.FACH_DCH_PROMO_MAX, 3.0));
		prof.setRlcUlTh(readInt(properties, Profile3G.RLC_UL_TH, 543));
		prof.setRlcDlTh(readInt(properties, Profile3G.RLC_DL_TH, 475));
		prof.setDchTimerResetSize(readInt(properties, Profile3G.DCH_TIMER_RESET_SIZE, 320));
		prof.setDchTimerResetWin(readDouble(properties, Profile3G.DCH_TIMER_RESET_WIN, 0.3));
		prof.setRlcUlRateP2(readDouble(properties, Profile3G.RLC_UL_RATE_P2, 0.0014));
		prof.setRlcUlRateP1(readDouble(properties, Profile3G.RLC_UL_RATE_P1, 1.6));
		prof.setRlcUlRateP0(readDouble(properties, Profile3G.RLC_UL_RATE_P0, 20.0));
		prof.setRlcDlRateP1(readDouble(properties, Profile3G.RLC_DL_RATE_P1, 0.1));
		prof.setRlcDlRateP0(readDouble(properties, Profile3G.RLC_DL_RATE_P0, 10));
		prof.setPowerDch(readDouble(properties, Profile3G.POWER_DCH, 0.7));
		prof.setPowerFach(readDouble(properties, Profile3G.POWER_FACH, 0.35));
		prof.setPowerIdle(readDouble(properties, Profile3G.POWER_IDLE, 0));
		prof.setPowerIdleDch(readDouble(properties, Profile3G.POWER_IDLE_DCH, 0.53));
		prof.setPowerFachDch(readDouble(properties, Profile3G.POWER_FACH_DCH, 0.55));
		return prof;
	}
	@Override
	public void save3G(String filepath, Profile3G prof) throws IOException {
		OutputStream output = filemanager.getFileOutputStream(filepath);
		this.save3G(output, prof);
	}
	@Override
	public void save3G(OutputStream output, Profile3G prof) throws IOException{
		Properties props = new Properties();
		this.setBaseData(prof, props);
		
		props.setProperty(Profile3G.DCH_FACH_TIMER, String.valueOf(prof.getDchFachTimer()));
		props.setProperty(Profile3G.FACH_IDLE_TIMER, String.valueOf(prof.getFachIdleTimer()));
		props.setProperty(Profile3G.IDLE_DCH_PROMO_MIN, String.valueOf(prof.getIdleDchPromoMin()));
		props.setProperty(Profile3G.IDLE_DCH_PROMO_AVG, String.valueOf(prof.getIdleDchPromoAvg()));
		props.setProperty(Profile3G.IDLE_DCH_PROMO_MAX, String.valueOf(prof.getIdleDchPromoMax()));
		props.setProperty(Profile3G.FACH_DCH_PROMO_MIN, String.valueOf(prof.getFachDchPromoMin()));
		props.setProperty(Profile3G.FACH_DCH_PROMO_AVG, String.valueOf(prof.getFachDchPromoAvg()));
		props.setProperty(Profile3G.FACH_DCH_PROMO_MAX, String.valueOf(prof.getFachDchPromoMax()));
		props.setProperty(Profile3G.RLC_UL_TH, String.valueOf(prof.getRlcUlTh()));
		props.setProperty(Profile3G.RLC_DL_TH, String.valueOf(prof.getRlcDlTh()));
		props.setProperty(Profile3G.DCH_TIMER_RESET_SIZE, String.valueOf(prof.getDchTimerResetSize()));
		props.setProperty(Profile3G.DCH_TIMER_RESET_WIN, String.valueOf(prof.getDchTimerResetWin()));
		props.setProperty(Profile3G.RLC_UL_RATE_P2, String.valueOf(prof.getRlcUlRateP2()));
		props.setProperty(Profile3G.RLC_UL_RATE_P1, String.valueOf(prof.getRlcUlRateP1()));
		props.setProperty(Profile3G.RLC_UL_RATE_P0, String.valueOf(prof.getRlcUlRateP0()));
		props.setProperty(Profile3G.RLC_DL_RATE_P2, String.valueOf(prof.getRlcDlRateP2()));
		props.setProperty(Profile3G.RLC_DL_RATE_P1, String.valueOf(prof.getRlcDlRateP1()));
		props.setProperty(Profile3G.RLC_DL_RATE_P0, String.valueOf(prof.getRlcDlRateP0()));
		props.setProperty(Profile3G.POWER_DCH, String.valueOf(prof.getPowerDch()));
		props.setProperty(Profile3G.POWER_FACH, String.valueOf(prof.getPowerFach()));
		props.setProperty(Profile3G.POWER_IDLE, String.valueOf(prof.getPowerIdle()));
		props.setProperty(Profile3G.POWER_IDLE_DCH, String.valueOf(prof.getPowerIdleDch()));
		props.setProperty(Profile3G.POWER_FACH_DCH, String.valueOf(prof.getPowerFachDch()));
		
		props.store(output, "save profile3g");
	}
	@Override
	public double energyLTE(double time1, double time2, RRCState state, ProfileLTE prof, List<PacketInfo> packets) {

		double deltaTime = time2 - time1;
		double result = 0.0;

		switch (state) {
		case LTE_PROMOTION :
			return deltaTime * prof.getLtePromotionPower();
		case LTE_CR_TAIL:
			// Assume no throughput
			return deltaTime * prof.getLteBeta();
		case LTE_CONTINUOUS:
			for (Throughput throughput : throughputcalculator.calculateThroughput(time1, time2,
					prof.getThroughputWindow(), packets)) {
				result += (((prof.getLteAlphaUp() / 1000.0) * throughput.getUploadMbps())
						+ ((prof.getLteAlphaDown() / 1000.0) * throughput.getDownloadMbps()) + prof.getLteBeta())
						* throughput.getSamplePeriod();
			}
			break;
		case LTE_DRX_SHORT :
			return (deltaTime / prof.getDrxShortPingPeriod())
					* ((prof.getDrxPingTime() * prof.getDrxShortPingPower()) + 
							((prof.getDrxShortPingPeriod() - prof.getDrxPingTime()) * prof.getLteTailPower()));
		case LTE_DRX_LONG :
			return (deltaTime / prof.getDrxLongPingPeriod())
					* ((prof.getDrxPingTime() * prof.getDrxLongPingPower()) + 
							((prof.getDrxLongPingPeriod() - prof.getDrxPingTime()) * prof.getLteTailPower()));
		case LTE_IDLE :
			
			// Add energy for full idle ping periods
			result = ((int) (deltaTime / prof.getIdlePingPeriod()))
					* ((prof.getIdlePingTime() * prof.getLteIdlePingPower()) + 
							((prof.getIdlePingPeriod() - prof.getIdlePingTime()) * prof.getLteIdlePower()));

			// Add residual energy for partial idle ping period
			double tres = deltaTime % prof.getIdlePingPeriod();
			result += tres <= prof.getIdlePingTime() ? tres * prof.getLteIdlePingPower()
					: ((prof.getIdlePingTime() * prof.getLteIdlePingPower()) + 
							((tres - prof.getIdlePingTime()) * prof.getLteIdlePingPower()));
			break;
		default:
			break;
		}
		return result;
	}
	/**
	 * create LTE profile using hardcoded default value
	 * @return
	 */
	@Override
	public Profile createLTEdefault(){
		try {
			return createLTEFromDefaultResourceFile();
		} catch (IOException e){
			return createLTE(new Properties());//empty properties will force default value			
		}
	}
	/**
	 * create LTE profile using embedded resource file lte.conf
	 * @return
	 * @throws IOException
	 */
	@Override
	public Profile createLTEFromDefaultResourceFile() throws IOException{
		InputStream input = getClass().getResourceAsStream("/lte.conf"); 
		return this.createLTE(input);
	}
	/**
	 * get LTE profile from a file path
	 * @param filepath
	 * @return
	 * @throws IOException
	 */
	@Override
	public Profile createLTEFromFilePath(String filepath) throws IOException{
		InputStream input = filemanager.getFileInputStream(filepath);
		return this.createLTE(input);
	}
	/**
	 * create LTE profile from java InputStream
	 * @param input
	 * @return
	 * @throws IOException
	 */
	@Override
	public Profile createLTE(InputStream input) throws IOException{
		Properties prop = new Properties();
		prop.load(input);
		return this.createLTE(prop);
	}
	@Override
	public Profile createLTE(Properties properties){
		ProfileLTE prof = new ProfileLTE();
		this.createBaseData(prof, properties);

		prof.setDevice(readString(properties, ProfileLTE.DEVICE, "Default LTE Device"));
		prof.setPromotionTime(readDouble(properties, ProfileLTE.T_PROMOTION, 0.26));
		prof.setInactivityTimer(readDouble(properties, ProfileLTE.INACTIVITY_TIMER,0.1));
		prof.setDrxShortTime(readDouble(properties, ProfileLTE.T_SHORT_DRX, 0.02));
		prof.setDrxPingTime(readDouble(properties, ProfileLTE.T_DRX_PING, 0.002));
		prof.setDrxLongTime(readDouble(properties, ProfileLTE.T_LONG_DRX, 10));
		prof.setIdlePingTime(readDouble(properties, ProfileLTE.T_IDLE_PING, 0.043));
		prof.setDrxShortPingPeriod(readDouble(properties, ProfileLTE.T_SHORT_DRX_PING_PERIOD,0.02));
		prof.setDrxLongPingPeriod(readDouble(properties, ProfileLTE.T_LONG_DRX_PING_PERIOD,0.04));
		prof.setIdlePingPeriod(readDouble(properties, ProfileLTE.T_IDLE_PING_PERIOD,1.28));
		prof.setLtePromotionPower(readDouble(properties, ProfileLTE.P_PROMOTION,1.21));
		prof.setDrxShortPingPower(readDouble(properties, ProfileLTE.P_SHORT_DRX_PING,1.68));
		prof.setDrxLongPingPower(readDouble(properties, ProfileLTE.P_LONG_DRX_PING,1.68));
		prof.setLteTailPower(readDouble(properties, ProfileLTE.P_TAIL, 1.06));
		prof.setLteIdlePingPower(readDouble(properties, ProfileLTE.P_IDLE_PING, 0.594));
		prof.setLteIdlePower(readDouble(properties, ProfileLTE.P_IDLE, 0.0));
		prof.setLteAlphaUp(readDouble(properties, ProfileLTE.LTE_ALPHA_UP, 438.39));
		prof.setLteAlphaDown(readDouble(properties, ProfileLTE.LTE_ALPHA_DOWN, 51.97));
		prof.setLteBeta(readDouble(properties, ProfileLTE.LTE_BETA, 1.2));
		
		return prof;
	}
	@Override
	public void saveLTE(String filepath, ProfileLTE prof) throws IOException{
		OutputStream output = filemanager.getFileOutputStream(filepath);
		this.saveLTE(output, prof);
	}
	@Override
	public void saveLTE(OutputStream output, ProfileLTE prof) throws IOException{
		Properties props = new Properties();
		this.setBaseData(prof, props);
		
		props.setProperty(ProfileLTE.T_PROMOTION, String.valueOf(prof.getPromotionTime()));
		props.setProperty(ProfileLTE.INACTIVITY_TIMER, String.valueOf(prof.getInactivityTimer()));
		props.setProperty(ProfileLTE.T_SHORT_DRX, String.valueOf(prof.getDrxShortTime()));
		props.setProperty(ProfileLTE.T_DRX_PING, String.valueOf(prof.getDrxPingTime()));
		props.setProperty(ProfileLTE.T_LONG_DRX, String.valueOf(prof.getDrxLongTime()));
		props.setProperty(ProfileLTE.T_IDLE_PING, String.valueOf(prof.getIdlePingTime()));
		props.setProperty(ProfileLTE.T_SHORT_DRX_PING_PERIOD,String.valueOf(prof.getDrxShortPingPeriod()));
		props.setProperty(ProfileLTE.T_LONG_DRX_PING_PERIOD,String.valueOf(prof.getDrxLongPingPeriod()));
		props.setProperty(ProfileLTE.T_IDLE_PING_PERIOD, String.valueOf(prof.getIdlePingPeriod()));

		props.setProperty(ProfileLTE.P_PROMOTION, String.valueOf(prof.getLtePromotionPower()));
		props.setProperty(ProfileLTE.P_SHORT_DRX_PING, String.valueOf(prof.getDrxShortPingPower()));
		props.setProperty(ProfileLTE.P_LONG_DRX_PING, String.valueOf(prof.getDrxLongPingPower()));
		props.setProperty(ProfileLTE.P_TAIL, String.valueOf(prof.getLteTailPower()));
		props.setProperty(ProfileLTE.P_IDLE_PING, String.valueOf(prof.getLteIdlePingPower()));
		props.setProperty(ProfileLTE.P_IDLE, String.valueOf(prof.getLteIdlePower()));

		props.setProperty(ProfileLTE.LTE_ALPHA_UP, String.valueOf(prof.getLteAlphaUp()));
		props.setProperty(ProfileLTE.LTE_ALPHA_DOWN, String.valueOf(prof.getLteAlphaDown()));
		props.setProperty(ProfileLTE.LTE_BETA, String.valueOf(prof.getLteBeta()));
		
		props.store(output, "save profile LTE");
		
	}
	@Override
	public double energyWiFi(double time1, double time2, RRCState state , ProfileWiFi prof) {
			
		double deltaTime = time2 - time1;
		if(state == RRCState.WIFI_ACTIVE || state == RRCState.WIFI_TAIL){
			return deltaTime * prof.getWifiActivePower();
		}else if(state == RRCState.WIFI_IDLE){
			return deltaTime * prof.getWifiIdlePower();
		}
		return 0;
	}
	/**
	 * create WiFi profile using hardcoded default value
	 * @return
	 */
	@Override
	public Profile createWiFidefault(){
		return createWiFi(new Properties());//empty properties will force default value
	}
	/**
	 * get WiFi profile from a file path
	 * @param filepath
	 * @return
	 * @throws IOException
	 */
	@Override
	public Profile createWiFiFromFilePath(String filepath) throws IOException{
		InputStream input = filemanager.getFileInputStream(filepath);
		return this.createWiFi(input);
	}
	/**
	 * create WiFi profile using embedded resource file WiFi.conf
	 * @return
	 * @throws IOException
	 */
	@Override
	public Profile createWiFiFromDefaultResourceFile() throws IOException{
		InputStream input = getClass().getResourceAsStream("/WiFi.conf"); 
		return this.createWiFi(input);
	}
	/**
	 * create WiFi profile from java InputStream
	 * @param input
	 * @return
	 * @throws IOException
	 */
	@Override
	public Profile createWiFi(InputStream input) throws IOException{
		Properties prop = new Properties();
		prop.load(input);
		return this.createWiFi(prop);
	}
	@Override
	public Profile createWiFi(Properties properties){
		ProfileWiFi prof = new ProfileWiFi();
		this.createBaseData(prof, properties);
		
		prof.setDevice(readString(properties, ProfileLTE.DEVICE, "Default WiFi Device"));
		prof.setWifiTailTime(readDouble(properties, ProfileWiFi.WIFI_TAIL_TIME, 0.25));
		prof.setWifiActivePower(readDouble(properties, ProfileWiFi.POWER_WIFI_ACTIVE, 0.403));
		prof.setWifiIdlePower(readDouble(properties, ProfileWiFi.POWER_WIFI_STANDBY, 0.02));
		
		return prof;
	}
	@Override
	public void saveWiFi(String filepath, ProfileWiFi prof) throws IOException{
		OutputStream output = filemanager.getFileOutputStream(filepath);
		this.saveWiFi(output, prof);
	}
	@Override
	public void saveWiFi(OutputStream output, ProfileWiFi prof) throws IOException{
		Properties props = new Properties();
		this.setBaseData(prof, props);

		props.setProperty(ProfileWiFi.WIFI_TAIL_TIME, String.valueOf(prof.getWifiTailTime()));
		props.setProperty(ProfileWiFi.POWER_WIFI_ACTIVE, String.valueOf(prof.getWifiActivePower()));
		props.setProperty(ProfileWiFi.POWER_WIFI_STANDBY, String.valueOf(prof.getWifiIdlePower()));
		
		props.store(output, "save profile WiFi");
		
	}
	private void createBaseData(Profile profile, Properties properties){
		Profile pro = profile;
		pro.setCarrier(readString(properties, ProfileLTE.CARRIER, "AT&T"));
		pro.setUserInputTh(readDouble(properties, Profile.USER_INPUT_TH, pro.getUserInputTh()));
		pro.setPowerGpsActive(readDouble(properties, Profile.POWER_GPS_ACTIVE, pro.getPowerGpsActive()));
		pro.setPowerGpsStandby(readDouble(properties, Profile.POWER_GPS_STANDBY, pro.getPowerGpsStandby()));
		pro.setPowerCameraOn(readDouble(properties, Profile.POWER_CAMERA_ON, pro.getPowerCameraOn()));
		pro.setPowerBluetoothActive(readDouble(properties, Profile.POWER_BLUETOOTH_ACTIVE, pro.getPowerBluetoothActive()));
		pro.setPowerBluetoothStandby(readDouble(properties, Profile.POWER_BLUETOOTH_STANDBY, pro.getPowerBluetoothStandby()));
		pro.setPowerScreenOn(readDouble(properties, Profile.POWER_SCREEN_ON, pro.getPowerScreenOn()));
		pro.setBurstTh(readDouble(properties, Profile.BURST_TH, pro.getBurstTh()));
		pro.setLongBurstTh(readDouble(properties, Profile.LONG_BURST_TH, pro.getLongBurstTh()));
		pro.setPeriodMinCycle(readDouble(properties, Profile.PERIOD_MIN_CYCLE, pro.getPeriodMinCycle()));
		pro.setPeriodCycleTol(readDouble(properties, Profile.PERIOD_CYCLE_TOL, pro.getPeriodCycleTol()));
		pro.setPeriodMinSamples(readInt(properties, Profile.PERIOD_MIN_SAMPLES, pro.getPeriodMinSamples()));
		pro.setLargeBurstDuration(readDouble(properties, Profile.LARGE_BURST_DURATION, pro.getLargeBurstDuration()));
		pro.setLargeBurstSize(readInt(properties, Profile.LARGE_BURST_SIZE, pro.getLargeBurstSize()));
		pro.setCloseSpacedBurstThreshold(readDouble(properties, Profile.CLOSE_SPACED_BURSTS, pro.getCloseSpacedBurstThreshold()));
		pro.setThroughputWindow(readDouble(properties, Profile.W_THROUGHPUT, pro.getThroughputWindow()));
		
		pro.setInit(true);
	}
	private void setBaseData(Profile profile, Properties properties){
		Profile prof = profile;
		Properties props = properties;
		
		props.setProperty(Profile.CARRIER, prof.getCarrier());
		props.setProperty(Profile.DEVICE, prof.getDevice());
		props.setProperty(Profile.PROFILE_TYPE, prof.getProfileType().name());
		props.setProperty(Profile.USER_INPUT_TH, String.valueOf(prof.getUserInputTh()));
		props.setProperty(Profile.POWER_GPS_ACTIVE, String.valueOf(prof.getPowerGpsActive()));
		props.setProperty(Profile.POWER_GPS_STANDBY, String.valueOf(prof.getPowerGpsStandby()));
		props.setProperty(Profile.POWER_CAMERA_ON, String.valueOf(prof.getPowerCameraOn()));
		props.setProperty(Profile.POWER_BLUETOOTH_ACTIVE, String.valueOf(prof.getPowerBluetoothActive()));
		props.setProperty(Profile.POWER_BLUETOOTH_STANDBY, String.valueOf(prof.getPowerBluetoothStandby()));
		props.setProperty(Profile.POWER_SCREEN_ON, String.valueOf(prof.getPowerScreenOn()));
		props.setProperty(Profile.BURST_TH, String.valueOf(prof.getBurstTh()));
		props.setProperty(Profile.LONG_BURST_TH, String.valueOf(prof.getLongBurstTh()));
		props.setProperty(Profile.PERIOD_MIN_CYCLE, String.valueOf(prof.getPeriodMinCycle()));
		props.setProperty(Profile.PERIOD_CYCLE_TOL, String.valueOf(prof.getPeriodCycleTol()));
		props.setProperty(Profile.PERIOD_MIN_SAMPLES, String.valueOf(prof.getPeriodMinSamples()));
		props.setProperty(Profile.LARGE_BURST_DURATION, String.valueOf(prof.getLargeBurstDuration()));
		props.setProperty(Profile.LARGE_BURST_SIZE, String.valueOf(prof.getLargeBurstSize()));
		props.setProperty(Profile.CLOSE_SPACED_BURSTS, String.valueOf(prof.getCloseSpacedBurstThreshold()));
		props.setProperty(Profile.W_THROUGHPUT, String.valueOf(prof.getThroughputWindow()));
		
	}
	/**
	 * Reads the specified profile properties and returns a double value for the specified attribute.
	 * @param properties
	 *            The profile properties to be read.
	 * @param attribute
	 *            The attribute name whose value is to be read.
	 * @param defaultVal
	 *            The default value for the attribute.
	 * @return The double alue of the specified attribute for the profile.
	 */
	protected double readDouble(Properties properties, String attribute,
			double defaultVal) {
		String value = properties.getProperty(attribute);
		try {
			if (value != null) {
				return Double.parseDouble(value);
			} else {
				return defaultVal;
			}
		} catch (NumberFormatException e) {
			return defaultVal;
		}
	}

	/**
	 * Reads the specified profile properties and returns an int value for the specified attribute.
	 * @param properties
	 *            The profile properties to be read.
	 * @param attribute
	 *            The attribute name whose value is to be read.
	 * @param defaultVal
	 *            The default value for the attribute.
	 * @return The int value of the specified attribute for the profile.
	 */
	protected int readInt(Properties properties, String attribute,
			int defaultVal) {
		String value = properties.getProperty(attribute);
		try {
			if (value != null) {
				return Integer.parseInt(value);
			} else {
				return defaultVal;
			}
		} catch (NumberFormatException e) {
			return defaultVal;
		}
	}

	protected String readString(Properties properties, String attribute, String defaultValue) {
		return properties.getProperty(attribute, defaultValue);
	}
	
}//end
