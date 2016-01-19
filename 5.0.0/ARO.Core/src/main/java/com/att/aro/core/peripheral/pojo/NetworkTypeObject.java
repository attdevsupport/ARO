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
package com.att.aro.core.peripheral.pojo;

import java.util.List;

import com.att.aro.core.packetanalysis.pojo.NetworkBearerTypeInfo;

/**
 * a wrapper class for reading network detail data
 * 
 * @author Borey Sao Date: October 7, 2014
 *
 */
public class NetworkTypeObject {
	List<NetworkBearerTypeInfo> networkTypeInfos;
	List<NetworkType> networkTypesList;

	public List<NetworkBearerTypeInfo> getNetworkTypeInfos() {
		return networkTypeInfos;
	}

	public void setNetworkTypeInfos(List<NetworkBearerTypeInfo> networkTypeInfos) {
		this.networkTypeInfos = networkTypeInfos;
	}

	public List<NetworkType> getNetworkTypesList() {
		return networkTypesList;
	}

	public void setNetworkTypesList(List<NetworkType> networkTypesList) {
		this.networkTypesList = networkTypesList;
	}

}
