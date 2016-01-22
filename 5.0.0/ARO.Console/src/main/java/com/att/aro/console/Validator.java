/*
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

package com.att.aro.console;

import org.springframework.context.ApplicationContext;

import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.pojo.ErrorCode;

/**
 * validate commands against arguments
 *
 */
public class Validator {
	public ErrorCode validate(Commands cmd, ApplicationContext context) {
		if (cmd.getStartcollector() != null) {
			String colname = cmd.getStartcollector();
			if (!"rooted_android".equals(colname) 
			 && !"vpn_android".equals(colname) 
			 && !"ios".equals(colname)) {
				return ErrorCodeRegistry.getUnsupportedCollector();
			}
			if (cmd.getOutput() == null) {
				return ErrorCodeRegistry.getOutputRequired();
			}
		}
		if (cmd.getAnalyze() != null) {
			//check something
			if (cmd.getFormat().equals("json") && cmd.getFormat().equals("html")) {
				return ErrorCodeRegistry.getUnsupportedFormat();
			}

			if (cmd.getOutput() == null) {
				return ErrorCodeRegistry.getOutputRequired();
			}
			IFileManager filemg = context.getBean(IFileManager.class);
			if (filemg.fileExist(cmd.getOutput())) {
				return ErrorCodeRegistry.getFileExist();
			}
		}
		if (cmd.getVideo() != null && !cmd.getVideo().equals("yes") && !cmd.getVideo().equals("no")) {
			return ErrorCodeRegistry.getInvalidVideoOption();
		}
		return null;
	}
}
