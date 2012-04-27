/*
 * Copyright 2012 AT&T
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


package com.att.aro.main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import com.att.aro.commonui.DataTableModel;
import com.att.aro.model.ConfigurationData;
import com.att.aro.model.Profile;
import com.att.aro.model.Profile3G;
import com.att.aro.model.ProfileException;
import com.att.aro.model.ProfileLTE;
import com.att.aro.model.ProfileType;

/**
 * Represents the data table model for the Configuration dialog.
 */
public class ConfigurationTableModel extends DataTableModel<ConfigurationData> {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

	/**
	 * An integer that identifies the attribute column.
	 */
	public static final int PROFILE_ATTRIBUTE_COLUMN = 0;
	/**
	 * An integer that identifies the data column.
	 */
	public static final int PROFILE_DATA_COLUMN = 1;

	private static final String[] columns = {
			rb.getString("configurationTableModel.PROFILE_ATTRIBUTE_COLUMN"),
			rb.getString("configurationTableModel.PROFILE_DATA_COLUMN") };

	private File file;
	private String name;
	private ProfileType profileType;

	/**
	 * Initializes a new instance of the ConfigurationTableModel class.
	 */
	public ConfigurationTableModel() {
		super(columns);
	}

	/**
	 * Initializes a new instance of the ConfigurationTableModel class using the
	 * specified profile. Sets the default profile for the Configuration table.
	 * 
	 * @param profile
	 *            - The new default profile for the Configuration table.
	 * 
	 * @see Profile
	 */
	public ConfigurationTableModel(Profile profile) {
		super(columns);
		setProfile(profile);
	}

	/**
	 * Sets the profile data for the Configuration table model.
	 * 
	 * @param profile
	 *            - The profile for the Configuration table.
	 */
	public synchronized void setProfile(Profile profile) {
		this.file = profile.getFile();
		this.name = profile.getName();
		List<ConfigurationData> data = new ArrayList<ConfigurationData>();

		if (profile instanceof Profile3G) {
			this.profileType = ProfileType.T3G;
		} else if (profile instanceof ProfileLTE) {
			this.profileType = ProfileType.LTE;
		} else {
			throw new IllegalArgumentException("Invalid Profile type");
		}

		data.add(new ConfigurationData(rb.getString("configuration.CARRIER"),
				Profile.CARRIER, profile.getCarrier()));
		data.add(new ConfigurationData(rb.getString("configuration.DEVICE"),
				Profile.DEVICE, profile.getDevice()));

		if (profile instanceof Profile3G) {
			// Adding 3G Profile
			Profile3G profile3g = (Profile3G) profile;

			data.add(new ConfigurationData(rb
					.getString("configuration.DCH_FACH_TIMER"),
					Profile3G.DCH_FACH_TIMER, Double.toString(profile3g
							.getDchFachTimer())));
			data.add(new ConfigurationData(rb
					.getString("configuration.FACH_IDLE_TIMER"),
					Profile3G.FACH_IDLE_TIMER, Double.toString(profile3g
							.getFachIdleTimer())));
			data.add(new ConfigurationData(rb
					.getString("configuration.IDLE_DCH_PROMO_MIN"),
					Profile3G.IDLE_DCH_PROMO_MIN, Double.toString(profile3g
							.getIdleDchPromoMin())));
			data.add(new ConfigurationData(rb
					.getString("configuration.IDLE_DCH_PROMO_AVG"),
					Profile3G.IDLE_DCH_PROMO_AVG, Double.toString(profile3g
							.getIdleDchPromoAvg())));
			data.add(new ConfigurationData(rb
					.getString("configuration.IDLE_DCH_PROMO_MAX"),
					Profile3G.IDLE_DCH_PROMO_MAX, Double.toString(profile3g
							.getIdleDchPromoMax())));
			data.add(new ConfigurationData(rb
					.getString("configuration.FACH_DCH_PROMO_MIN"),
					Profile3G.FACH_DCH_PROMO_MIN, Double.toString(profile3g
							.getFachDchPromoMin())));
			data.add(new ConfigurationData(rb
					.getString("configuration.FACH_DCH_PROMO_AVG"),
					Profile3G.FACH_DCH_PROMO_AVG, Double.toString(profile3g
							.getFachDchPromoAvg())));
			data.add(new ConfigurationData(rb
					.getString("configuration.FACH_DCH_PROMO_MAX"),
					Profile3G.FACH_DCH_PROMO_MAX, Double.toString(profile3g
							.getFachDchPromoMax())));

			data.add(new ConfigurationData(rb
					.getString("configuration.RLC_UL_TH"), Profile3G.RLC_UL_TH,
					Integer.toString(profile3g.getRlcUlTh())));
			data.add(new ConfigurationData(rb
					.getString("configuration.RLC_DL_TH"), Profile3G.RLC_DL_TH,
					Integer.toString(profile3g.getRlcDlTh())));
			data.add(new ConfigurationData(rb
					.getString("configuration.DCH_TIMER_RESET_SIZE"),
					Profile3G.DCH_TIMER_RESET_SIZE, Integer.toString(profile3g
							.getDchTimerResetSize())));
			data.add(new ConfigurationData(rb
					.getString("configuration.DCH_TIMER_RESET_WIN"),
					Profile3G.DCH_TIMER_RESET_WIN, Double.toString(profile3g
							.getDchTimerResetWin())));
			data.add(new ConfigurationData(rb
					.getString("configuration.RLC_UL_RATE_P2"),
					Profile3G.RLC_UL_RATE_P2, Double.toString(profile3g
							.getRlcUlRateP2())));
			data.add(new ConfigurationData(rb
					.getString("configuration.RLC_UL_RATE_P1"),
					Profile3G.RLC_UL_RATE_P1, Double.toString(profile3g
							.getRlcUlRateP1())));
			data.add(new ConfigurationData(rb
					.getString("configuration.RLC_UL_RATE_P0"),
					Profile3G.RLC_UL_RATE_P0, Double.toString(profile3g
							.getRlcUlRateP0())));
			data.add(new ConfigurationData(rb
					.getString("configuration.RLC_DL_RATE_P2"),
					Profile3G.RLC_DL_RATE_P2, Double.toString(profile3g
							.getRlcDlRateP2())));
			data.add(new ConfigurationData(rb
					.getString("configuration.RLC_DL_RATE_P1"),
					Profile3G.RLC_DL_RATE_P1, Double.toString(profile3g
							.getRlcDlRateP1())));
			data.add(new ConfigurationData(rb
					.getString("configuration.RLC_DL_RATE_P0"),
					Profile3G.RLC_DL_RATE_P0, Double.toString(profile3g
							.getRlcDlRateP0())));

			data.add(new ConfigurationData(rb
					.getString("configuration.POWER_DCH"), Profile3G.POWER_DCH,
					Double.toString(profile3g.getPowerDch())));
			data.add(new ConfigurationData(rb
					.getString("configuration.POWER_FACH"),
					Profile3G.POWER_FACH, Double.toString(profile3g
							.getPowerFach())));
			data.add(new ConfigurationData(rb
					.getString("configuration.POWER_IDLE"),
					Profile3G.POWER_IDLE, Double.toString(profile3g
							.getPowerIdle())));
			data.add(new ConfigurationData(rb
					.getString("configuration.POWER_IDLE_DCH"),
					Profile3G.POWER_IDLE_DCH, Double.toString(profile3g
							.getPowerIdleDch())));
			data.add(new ConfigurationData(rb
					.getString("configuration.POWER_FACH_DCH"),
					Profile3G.POWER_FACH_DCH, Double.toString(profile3g
							.getPowerFachDch())));
			data.add(new ConfigurationData(rb
					.getString("configuration.W_THROUGHPUT"),
					ProfileLTE.W_THROUGHPUT, Double.toString(profile3g
							.getThroughputWindow())));
		} else if (profile instanceof ProfileLTE) {

			// Adding LTE Parameters
			ProfileLTE profileLte = (ProfileLTE) profile;
			data.add(new ConfigurationData(rb
					.getString("configuration.T_PROMOTION"),
					ProfileLTE.T_PROMOTION, Double.toString(profileLte
							.getPromotionTime())));
			data.add(new ConfigurationData(
					rb.getString("configuration.TI_DRX"),
					ProfileLTE.INACTIVITY_TIMER, Double.toString(profileLte
							.getInactivityTimer())));
			data.add(new ConfigurationData(rb
					.getString("configuration.TIS_DRX"),
					ProfileLTE.T_SHORT_DRX, Double.toString(profileLte
							.getDrxShortTime())));
			data.add(new ConfigurationData(rb
					.getString("configuration.TON_DRX"), ProfileLTE.T_DRX_PING,
					Double.toString(profileLte.getDrxPingTime())));
			data.add(new ConfigurationData(rb
					.getString("configuration.T_TAIL_DRX"),
					ProfileLTE.T_LONG_DRX, Double.toString(profileLte
							.getDrxLongTime())));
			data.add(new ConfigurationData(rb
					.getString("configuration.T_ONIDLE"),
					ProfileLTE.T_IDLE_PING, Double.toString(profileLte
							.getIdlePingTime())));
			data.add(new ConfigurationData(rb
					.getString("configuration.TPS_DRX"),
					ProfileLTE.T_SHORT_DRX_PING_PERIOD, Double
							.toString(profileLte.getDrxShortPingPeriod())));
			data.add(new ConfigurationData(rb
					.getString("configuration.TPI_DRX"),
					ProfileLTE.T_LONG_DRX_PING_PERIOD, Double
							.toString(profileLte.getDrxLongPingPeriod())));
			data.add(new ConfigurationData(rb
					.getString("configuration.PPL_DRX"),
					ProfileLTE.T_IDLE_PING_PERIOD, Double.toString(profileLte
							.getIdlePingPeriod())));
			data.add(new ConfigurationData(rb
					.getString("configuration.W_THROUGHPUT"),
					ProfileLTE.W_THROUGHPUT, Double.toString(profileLte
							.getThroughputWindow())));
			data.add(new ConfigurationData(rb
					.getString("configuration.LTE_P_PROMOTION"),
					ProfileLTE.P_PROMOTION, Double.toString(profileLte
							.getLtePromotionPower())));
			data.add(new ConfigurationData(rb
					.getString("configuration.LTE_P_SHORT_DRX"),
					ProfileLTE.P_SHORT_DRX_PING, Double.toString(profileLte
							.getDrxShortPingPower())));
			data.add(new ConfigurationData(rb
					.getString("configuration.LTE_LONG_DRX"),
					ProfileLTE.P_LONG_DRX_PING, Double.toString(profileLte
							.getDrxLongPingPower())));
			data.add(new ConfigurationData(rb
					.getString("configuration.LTE_TAIL"), ProfileLTE.P_TAIL,
					Double.toString(profileLte.getLteTailPower())));
			data.add(new ConfigurationData(rb
					.getString("configuration.LTE_IDLE"),
					ProfileLTE.P_IDLE_PING, Double.toString(profileLte
							.getLteIdlePingPower())));
			data.add(new ConfigurationData(rb
					.getString("configuration.LTE_ALPHA_UP"),
					ProfileLTE.LTE_ALPHA_UP, Double.toString(profileLte
							.getLteAlphaUp())));
			data.add(new ConfigurationData(rb
					.getString("configuration.LTE_ALPHA_DOWN"),
					ProfileLTE.LTE_ALPHA_DOWN, Double.toString(profileLte
							.getLteAlphaDown())));
			data.add(new ConfigurationData(rb
					.getString("configuration.LTE_BETA"), ProfileLTE.LTE_BETA,
					Double.toString(profileLte.getLteBeta())));
		}

		data.add(new ConfigurationData(rb.getString("configuration.BURST_TH"),
				Profile.BURST_TH, Double.toString(profile.getBurstTh())));
		data.add(new ConfigurationData(rb
				.getString("configuration.LONG_BURST_TH"),
				Profile.LONG_BURST_TH,
				Double.toString(profile.getLongBurstTh())));
		data.add(new ConfigurationData(rb
				.getString("configuration.USER_INPUT_TH"),
				Profile.USER_INPUT_TH,
				Double.toString(profile.getUserInputTh())));
		data.add(new ConfigurationData(rb
				.getString("configuration.PERIOD_MIN_CYCLE"),
				Profile.PERIOD_MIN_CYCLE, Double.toString(profile
						.getPeriodMinCycle())));
		data.add(new ConfigurationData(rb
				.getString("configuration.PERIOD_CYCLE_TOL"),
				Profile.PERIOD_CYCLE_TOL, Double.toString(profile
						.getPeriodCycleTol())));
		data.add(new ConfigurationData(rb
				.getString("configuration.PERIOD_MIN_SAMPLES"),
				Profile.PERIOD_MIN_SAMPLES, Integer.toString(profile
						.getPeriodMinSamples())));
		data.add(new ConfigurationData(rb
				.getString("configuration.LARGE_BURST_DURATION"),
				Profile.LARGE_BURST_DURATION, Double.toString(profile
						.getLargeBurstDuration())));
		data.add(new ConfigurationData(rb
				.getString("configuration.LARGE_BURST_SIZE"),
				Profile.LARGE_BURST_SIZE, Integer.toString(profile
						.getLargeBurstSize())));
		data.add(new ConfigurationData(rb
				.getString("configuration.POWER_GPS_ACTIVE"),
				Profile.POWER_GPS_ACTIVE, Double.toString(profile
						.getPowerGpsActive())));
		data.add(new ConfigurationData(rb
				.getString("configuration.POWER_GPS_STANDBY"),
				Profile.POWER_GPS_STANDBY, Double.toString(profile
						.getPowerGpsStandby())));
		data.add(new ConfigurationData(rb
				.getString("configuration.POWER_CAMERA_ON"),
				Profile.POWER_CAMERA_ON, Double.toString(profile
						.getPowerCameraOn())));
		data.add(new ConfigurationData(rb
				.getString("configuration.POWER_WIFI_ACTIVE"),
				Profile.POWER_WIFI_ACTIVE, Double.toString(profile
						.getPowerWifiActive())));
		data.add(new ConfigurationData(rb
				.getString("configuration.POWER_WIFI_STANDBY"),
				Profile.POWER_WIFI_STANDBY, Double.toString(profile
						.getPowerWifiStandby())));
		data.add(new ConfigurationData(rb
				.getString("configuration.POWER_BLUETOOTH_ACTIVE"),
				Profile.POWER_BLUETOOTH_ACTIVE, Double.toString(profile
						.getPowerBluetoothActive())));
		data.add(new ConfigurationData(rb
				.getString("configuration.POWER_BLUETOOTH_STANDBY"),
				Profile.POWER_BLUETOOTH_STANDBY, Double.toString(profile
						.getPowerBluetoothStandby())));
		data.add(new ConfigurationData(rb
				.getString("configuration.POWER_SCREEN_ON"),
				Profile.POWER_SCREEN_ON, Double.toString(profile
						.getPowerScreenOn())));
		setData(data);
	}

	/**
	 * Returns the profile represented by this table model.
	 * 
	 * @return The profile for the Configuration table.
	 * 
	 * @throws ProfileException
	 */
	public Profile getProfile() throws ProfileException {
		Properties props = new Properties();
		for (ConfigurationData data : getData()) {
			String value = data.getProfileData();
			String attribute = data.getProfileAttr();
			if (value != null) {
				props.setProperty(attribute, value);
			}
		}

		Profile profile = null;
		switch (profileType) {
		case T3G:
			if (file != null) {
				profile = new Profile3G(file, props);
			} else {
				profile = new Profile3G(name, props);
			}
			break;
		case LTE:
			if (file != null) {
				profile = new ProfileLTE(file, props);
			} else {
				profile = new ProfileLTE(name, props);
			}
			break;
		}
		return profile;
	}

	@Override
	protected Object getColumnValue(ConfigurationData item, int columnIndex) {
		switch (columnIndex) {
		case PROFILE_ATTRIBUTE_COLUMN:
			return item.getProfileDesc();
		case PROFILE_DATA_COLUMN:
			return item.getProfileData();
		}
		return null;
	}

	/**
	 * Returns a value that indicates whether the specified data cell is
	 * editable or not.
	 * 
	 * @param row
	 *            – The row number of the cell.
	 * 
	 * @param col
	 *            – The column number of the cell.
	 * 
	 * @return A boolean value that is “true” if the cell is editable, and
	 *         “false” if it is not.
	 */
	@Override
	public boolean isCellEditable(int row, int col) {

		return col == PROFILE_DATA_COLUMN;
	}

	/**
	 * Sets the value of the specified profile data item.
	 * 
	 * @param Value
	 *            – The value to set for the data item.
	 * 
	 * @param rowIndex
	 *            – The row index of the data item.
	 * 
	 * @param columnIndex
	 *            - The column index of the data item.
	 */
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		// data[rowIndex][columnIndex] = value;
		// fireTableCellUpdated(rowIndex, columnIndex);
		super.setValueAt(value, rowIndex, columnIndex);

		if (columnIndex == PROFILE_DATA_COLUMN) {
			String s = value.toString();
			ConfigurationData cd = getValueAt(rowIndex);

			cd.setProfileData(s);
			fireTableCellUpdated(rowIndex, PROFILE_DATA_COLUMN);
		}
	}

}
