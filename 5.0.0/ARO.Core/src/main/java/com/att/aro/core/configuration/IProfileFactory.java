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
package com.att.aro.core.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.configuration.pojo.Profile3G;
import com.att.aro.core.configuration.pojo.ProfileLTE;
import com.att.aro.core.configuration.pojo.ProfileType;
import com.att.aro.core.configuration.pojo.ProfileWiFi;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.RRCState;

/**
 * methods to handle mobile profile creation and saving for 3G, LTE and WiFi
 */
public interface IProfileFactory {
	Profile create(ProfileType type, Properties prop);
	
	double energy3G(double time1, double time2, RRCState state, Profile3G prof);
	Profile create3Gdefault();
	Profile create3GFromDefaultResourceFile() throws IOException;
	Profile create3GFromFilePath(String filepath) throws IOException;
	Profile create3G(InputStream input) throws IOException;
	Profile create3G(Properties properties);
	void save3G(OutputStream output, Profile3G prof) throws IOException;
	void save3G(String filepath, Profile3G prof) throws IOException;
	
	double energyLTE(double time1, double time2, RRCState state,ProfileLTE prof, List<PacketInfo> packets);
	Profile createLTEdefault();
	Profile createLTEFromDefaultResourceFile() throws IOException;
	Profile createLTEFromFilePath(String filepath) throws IOException;
	Profile createLTE(InputStream input) throws IOException;
	Profile createLTE(Properties properties);
	void saveLTE(OutputStream output, ProfileLTE prof) throws IOException;
	void saveLTE(String filepath, ProfileLTE prof) throws IOException;
	
	double energyWiFi(double time1, double time2, RRCState state , ProfileWiFi prof);
	Profile createWiFidefault();
	Profile createWiFiFromFilePath(String filepath) throws IOException;
	Profile createWiFiFromDefaultResourceFile() throws IOException;
	Profile createWiFi(InputStream input) throws IOException;
	Profile createWiFi(Properties properties);
	void saveWiFi(OutputStream output, ProfileWiFi prof) throws IOException;
	void saveWiFi(String filepath, ProfileWiFi prof) throws IOException;
}
