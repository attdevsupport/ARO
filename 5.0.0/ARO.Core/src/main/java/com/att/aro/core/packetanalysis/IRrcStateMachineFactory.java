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
package com.att.aro.core.packetanalysis;

import java.util.List;

import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.packetanalysis.pojo.AbstractRrcStateMachine;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.TimeRange;

/**
 * create RrcStateMachine based on profile type and return different implementation of AbstractRrcStateMachine
 * @author Borey Sao
 * Date: November 3, 2014
 */
public interface IRrcStateMachineFactory {
	AbstractRrcStateMachine create(List<PacketInfo> packetlist, Profile profile, double packetDuration, 
			double traceDuration,double totalBytes, TimeRange timerange);
}
