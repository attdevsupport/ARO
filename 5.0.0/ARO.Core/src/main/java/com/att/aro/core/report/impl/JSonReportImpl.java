/*
 *  Copyright 2015 AT&T
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
package com.att.aro.core.report.impl;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import com.att.aro.core.ILogger;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.core.report.IReport;

public class JSonReportImpl implements IReport {
	

	@InjectLogger
	private static ILogger logger;
	@Autowired
	private IFileManager filereader;

	@Override
	public boolean reportGenerator(String resultFilePath, AROTraceData results) {
		if (resultFilePath == null || results == null) {
			return false;
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.writeValue(filereader.createFile(resultFilePath), results);
			return true;
		} catch (JsonGenerationException e) {
			logger.error(e.getMessage());
		} catch (JsonMappingException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return false;
	}

}
