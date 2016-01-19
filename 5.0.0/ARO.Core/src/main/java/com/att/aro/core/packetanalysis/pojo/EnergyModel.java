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

import java.io.Serializable;

/**
 * Models energy usage of peripherals. All energy is in joules.
 * 
 * <pre>
 *   gpsActiveEnergy         //   gps Active Energy
 *   gpsStandbyEnergy        //   gps Standby Energy
 *   bluetoothActiveEnergy   //   bluetooth Active Energy
 *   bluetoothStandbyEnergy  //   bluetooth Standby Energy
 *   totalGpsEnergy          //   total Gps Energy
 *   totalCameraEnergy       //   total Camera Energy
 *   totalBluetoothEnergy    //   total Bluetooth Energy
 *   totalScreenEnergy       //   total Screen Energy
 *   totalRrcEnergy          //   total Rrc Energy
 * </pre>
 *
 */
public class EnergyModel implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * gps Active Energy
	 */
	private double gpsActiveEnergy = 0;

	/**
	 * gps Standby Energy
	 */
	private double gpsStandbyEnergy = 0;

	/**
	 * total Gps Energy
	 */
	private double totalGpsEnergy = 0;

	/**
	 * total Camera Energy
	 */
	private double totalCameraEnergy = 0;

	/**
	 * bluetooth Active Energy
	 */
	private double bluetoothActiveEnergy = 0;

	/**
	 * bluetooth Standby Energy
	 */
	private double bluetoothStandbyEnergy = 0;

	/**
	 * total Bluetooth Energy
	 */
	private double totalBluetoothEnergy = 0;

	/**
	 * total Screen Energy
	 */
	private double totalScreenEnergy = 0;

	/**
	 * total Rrc Energy
	 */
	private double totalRrcEnergy = 0;

	/**
	 * Returns the amount of energy consumed during the GPS active state.
	 * 
	 * @return The amount of GPS active energy in joules.
	 */
	public double getGpsActiveEnergy() {
		return gpsActiveEnergy;
	}

	/**
	 * Returns the amount of energy consumed during GPS standby mode.
	 * 
	 * @return The amount of GPS standby energy in joules.
	 */
	public double getGpsStandbyEnergy() {
		return gpsStandbyEnergy;
	}

	/**
	 * Returns the total amount of energy consumed by GPS. This value is the sum
	 * of the energy consumption amounts for GPS Active and GPS Standby.
	 * 
	 * @return The total amount of GPS energy consumed, in joules.
	 */
	public double getTotalGpsEnergy() {
		return totalGpsEnergy;
	}

	/**
	 * Returns the total amount of energy consumed while the Camera state is ON.
	 * 
	 * @return The total amount of Camera energy in joules.
	 */
	public double getTotalCameraEnergy() {
		return totalCameraEnergy;
	}

	/**
	 * Returns the amount of energy consumed while Bluetooth is in the connected
	 * state.
	 * 
	 * @return The amount of Bluetooth active energy, in joules.
	 */
	public double getBluetoothActiveEnergy() {
		return bluetoothActiveEnergy;
	}

	/**
	 * Returns the amount of energy consumed while Bluetooth is in the
	 * disconnected state.
	 * 
	 * @return The amount of Bluetooth standby energy, in joules.
	 */
	public double getBluetoothStandbyEnergy() {
		return bluetoothStandbyEnergy;
	}

	/**
	 * Returns the total amount of energy consumed by Bluetooth. This value is
	 * the sum of the energy consumption values for Bluetooth Active and
	 * Bluetooth Standby.
	 * 
	 * @return The total amount of Bluetooth energy consumed, in joules.
	 */
	public double getTotalBluetoothEnergy() {
		return totalBluetoothEnergy;
	}

	/**
	 * Returns the amount of energy consumed while the device screen is in the
	 * ON state.
	 * 
	 * @return The total screen energy consumed, in joules.
	 */
	public double getTotalScreenEnergy() {
		return totalScreenEnergy;
	}

	/**
	 * Sets gps Active Energy
	 * 
	 * @param gpsActiveEnergy
	 *            - gps Active Energy
	 */
	public void setGpsActiveEnergy(double gpsActiveEnergy) {
		this.gpsActiveEnergy = gpsActiveEnergy;
	}

	/**
	 * Sets gps Standby Energy
	 * 
	 * @param gpsStandbyEnergy
	 *            - gps Standby Energy
	 */
	public void setGpsStandbyEnergy(double gpsStandbyEnergy) {
		this.gpsStandbyEnergy = gpsStandbyEnergy;
	}

	/**
	 * Sets total Gps Energy
	 * 
	 * @param totalGpsEnergy
	 *            - total Gps Energy
	 */
	public void setTotalGpsEnergy(double totalGpsEnergy) {
		this.totalGpsEnergy = totalGpsEnergy;
	}

	/**
	 * Sets total Camera Energy
	 * 
	 * @param totalCameraEnergy
	 *            - total Camera Energy
	 */
	public void setTotalCameraEnergy(double totalCameraEnergy) {
		this.totalCameraEnergy = totalCameraEnergy;
	}

	/**
	 * Sets bluetooth Active Energy
	 * 
	 * @param bluetoothActiveEnergy
	 *            - bluetooth Active Energy
	 */
	public void setBluetoothActiveEnergy(double bluetoothActiveEnergy) {
		this.bluetoothActiveEnergy = bluetoothActiveEnergy;
	}

	/**
	 * Sets bluetooth Standby Energy
	 * 
	 * @param bluetoothStandbyEnergy
	 *            - bluetooth Standby Energy
	 */
	public void setBluetoothStandbyEnergy(double bluetoothStandbyEnergy) {
		this.bluetoothStandbyEnergy = bluetoothStandbyEnergy;
	}

	/**
	 * Sets total Bluetooth Energy
	 * 
	 * @param totalBluetoothEnergy
	 *            - total Bluetooth Energy
	 */
	public void setTotalBluetoothEnergy(double totalBluetoothEnergy) {
		this.totalBluetoothEnergy = totalBluetoothEnergy;
	}

	/**
	 * Sets total Screen Energy
	 * @param totalScreenEnergy - total Screen Energy
	 */
	public void setTotalScreenEnergy(double totalScreenEnergy) {
		this.totalScreenEnergy = totalScreenEnergy;
	}

	/**
	 * Returns the total amount of energy consumed by Radio Resource Control
	 * 
	 * @return total Rrc Energy
	 */
	public double getTotalRrcEnergy() {
		return totalRrcEnergy;
	}

	/**
	 * Sets the total amount of energy consumed by Radio Resource Control
	 * 
	 * @param totalRrcEnergy
	 *            - total Rrc Energy
	 */
	public void setTotalRrcEnergy(double totalRrcEnergy) {
		this.totalRrcEnergy = totalRrcEnergy;
	}

	/**
	 * Returns the total amount of energy consumed.
	 * 
	 * @return The total energy consumed in joules.
	 */
	public double getTotalEnergyConsumed() {

		return totalBluetoothEnergy + totalCameraEnergy + totalGpsEnergy + totalRrcEnergy + totalScreenEnergy;
	}

}
