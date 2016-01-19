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
package com.att.aro.core.peripheral;

import java.util.Date;
import java.util.List;

import com.att.aro.core.peripheral.pojo.WakelockInfo;

/* 
 * Method to read the Wakelock data from the batteryinfo file and store it in the
 * wakelockInfos list.
 *
 * pre: call readAlarmDumpsysTimestamp(), it requires a timestamp for alignment.
 *
 * */
public interface IWakelockInfoReader {
	List<WakelockInfo> readData(String directory, String osVersion, double dumpsysEpochTimestamp, Date traceDateTime);
}
