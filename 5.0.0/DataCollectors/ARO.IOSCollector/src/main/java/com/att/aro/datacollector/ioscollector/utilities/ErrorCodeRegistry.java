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

package com.att.aro.datacollector.ioscollector.utilities;

import java.util.ResourceBundle;

import com.att.aro.core.pojo.ErrorCode;

/**
 * error code for ios Collector start from 500
 */
public final class ErrorCodeRegistry {
	private ErrorCodeRegistry(){}
	private static ResourceBundle defaultBundle = ResourceBundle.getBundle("messages");	

	public static ErrorCode getIOSInvalidSudoPassword(){
		ErrorCode error = new ErrorCode();
		error.setCode(500);
		error.setName("Error.validatepassword");
		error.setDescription("There was an error validating password.");
		return error;
	}
	public static ErrorCode getTraceDirExist(){
		ErrorCode err = new ErrorCode();
		err.setCode(501);
		err.setName("Found existing trace directory that is not empty");
		err.setDescription("ARO found an existing directory that contains files and did not want to override it. Some files may be hidden.");
		return err;
	}
	public static ErrorCode getNoDeviceConnected(){
		ErrorCode err = new ErrorCode();
		err.setCode(502);
		err.setName("No iOS device found.");
		err.setDescription("ARO cannot find any iOS deviced plugged into the machine.");
		return err;
	}
	/**
	 * failed to create local directory in user's machine to save trace data to.
	 * @return
	 */
	public static ErrorCode getFailedToCreateLocalTraceDirectory(){
		ErrorCode err = new ErrorCode();
		err.setCode(503);
		err.setName("Failed to create local trace directory");
		err.setDescription("ARO tried to create local directory for saving trace data, but failed.");
		return err;
	}
	/**
	 * failed to find XCode on local machine
	 * @return
	 */
	public static ErrorCode getFailedToLoadXCode(){
		ErrorCode err = new ErrorCode();
		err.setCode(505);
		err.setName("Failed to find XCode on local machine");
		err.setDescription("ARO tried to load XCode on local machine, but failed because XCode is not installed.");
		return err;
	}
	/**
	 * Unsupported XCode Version 
	 * @return
	 */
	public static ErrorCode getUnsupportedXCodeVersion(){
		ErrorCode err = new ErrorCode();
		err.setCode(506);
		err.setName("Unsupported XCode Version");
		err.setDescription(defaultBundle.getString("Error.xcodeversionunsupported"));
		return err;
	}
	/**
	 * Incorrect Serial Number 
	 * @return
	 */
	public static ErrorCode getIncorrectSerialNumber(){
		ErrorCode err = new ErrorCode();
		err.setCode(507);
		err.setName("Incorrect Serial Number");
		err.setDescription(defaultBundle.getString("Error.incorrectserialnumber"));
		return err;
	}
	/**
	 * No sudo Password 
	 * @return
	 */
	public static ErrorCode getNoSudoPassword(){
		ErrorCode err = new ErrorCode();
		err.setCode(508);
		err.setName("No sudo Password");
		err.setDescription(defaultBundle.getString("Error.nosudopassword"));
		return err;
	}	
	/**
	 * sudo Password Issue 
	 * @return
	 */
	public static ErrorCode getSudoPasswordIssue(){
		ErrorCode err = new ErrorCode();
		err.setCode(504);
		err.setName("sudo Password Issue");
		err.setDescription(defaultBundle.getString("Error.sudopasswordissue"));
		return err;
	}
	/**
	 * Device Info Issue 
	 * @return
	 */
	public static ErrorCode getDeviceInfoIssue(){
		ErrorCode err = new ErrorCode();
		err.setCode(509);
		err.setName("Device Info Issue");
		err.setDescription(defaultBundle.getString("Error.deviceinfoissue"));
		return err;
	}	
	/**
	 * Device Version Issue
	 * @return
	 */
	public static ErrorCode getDeviceVersionIssue(){
		ErrorCode err = new ErrorCode();
		err.setCode(510);
		err.setName("Device Version Issue");
		err.setDescription(defaultBundle.getString("Error.deviceversionissue"));
		return err;
	}
	/**
	 * Unsupported iOS version 
	 * @return
	 */
	public static ErrorCode getiOSUnsupportedVersion(){
		ErrorCode err = new ErrorCode();
		err.setCode(511);
		err.setName("iOS Unsupported Version");
		err.setDescription(defaultBundle.getString("Error.iosunsupportedversion"));
		return err;
	}
	/**
	 * Remote virtual interface 
	 * @return
	 */
	public static ErrorCode getrviError(){
		ErrorCode err = new ErrorCode();
		err.setCode(512);
		err.setName("rvi error");
		err.setDescription(defaultBundle.getString("Error.rvierror"));
		return err;
	}	
	/**
	 * Missing folder name
	 * @return
	 */
	public static ErrorCode getMissingFolderName(){
		ErrorCode err = new ErrorCode();
		err.setCode(512);
		err.setName("missing folder name");
		err.setDescription(defaultBundle.getString("Error.foldernamerequired"));
		return err;
	}	
}
