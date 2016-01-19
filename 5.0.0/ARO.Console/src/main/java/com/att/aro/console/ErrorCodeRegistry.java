package com.att.aro.console;

import com.att.aro.core.pojo.ErrorCode;


public final class ErrorCodeRegistry {
	private ErrorCodeRegistry(){}
	public static ErrorCode getUnsupportedCollector(){
		ErrorCode error = new ErrorCode();
		error.setCode(300);
		error.setName("Unsupported collector");
		error.setDescription("Collector name passed in is not supported.");
		return error;
	}
	public static ErrorCode getOutputRequired(){
		ErrorCode err = new ErrorCode();
		err.setCode(301);
		err.setName("Output location missing");
		err.setDescription("location to save data to was not given.");
		return err;
	}
	public static ErrorCode getUnsupportedFormat(){
		ErrorCode err = new ErrorCode();
		err.setCode(302);
		err.setName("Unsupported report format");
		err.setDescription("Report format entered is not supported. Type --help to see supported commands and options.");
		return err;
	}
	public static ErrorCode getInvalidVideoOption(){
		ErrorCode err = new ErrorCode();
		err.setCode(303);
		err.setName("Invalid video option");
		err.setDescription("Valid video option is yes or no. Invalid value was entered.");
		return err;
	}
	public static ErrorCode getFileExist(){
		ErrorCode err = new ErrorCode();
		err.setCode(304);
		err.setName("File or directory exists");
		err.setDescription("ARO found existing file or directory");
		return err;
	}
	public static ErrorCode getCollectorNotfound(){
		ErrorCode err = new ErrorCode();
		err.setCode(305);
		err.setName("Collector not found");
		err.setDescription("Data collector specified cannot be found. Make sure data collector is installed.");
		return err;
	}
}
