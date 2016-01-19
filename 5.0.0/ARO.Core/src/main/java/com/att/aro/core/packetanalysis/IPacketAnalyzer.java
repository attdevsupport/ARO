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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.packetanalysis.pojo.AnalysisFilter;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.Statistic;

public interface IPacketAnalyzer {
	PacketAnalyzerResult analyzeTraceFile(String traceFilePath, Profile profile, 
			AnalysisFilter filter) throws IOException;
	PacketAnalyzerResult analyzeTraceDirectory(String traceDirectory, Profile profile, 
			AnalysisFilter filter) throws FileNotFoundException;
	Statistic getStatistic(List<PacketInfo> packetlist);
}
