package com.att.aro.core.android.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.ddmlib.MultiLineReceiver;

public class ShellOutputReceiver extends MultiLineReceiver {
	private final Pattern FAILURE = Pattern.compile("failed");
	private final Pattern SDCARDFULL = Pattern.compile("No space left on device");
	private final Pattern ERROR = Pattern.compile("error");
	private final Pattern SEG_ERROR = Pattern.compile("Segmentation fault");
	private final Pattern NO_ARO = Pattern.compile("does not exist");
	private final Pattern ACTIVITY_RUNNING = Pattern.compile("current task has been brought to the front");
	private final Pattern FILE_EXISTS = Pattern.compile("File exists");
	private final Pattern NOT_EXECUTABLE = Pattern.compile("not executable");
	private final Pattern ENFORCING = Pattern.compile("Enforcing");
	private final Pattern NOT_ENFORCING = Pattern.compile("getenforce: not found");

	private boolean shellError;
	private boolean sdcardFull;
	private boolean failedExecute;
	private boolean seLinuxEnforce = false;
	private boolean noARO = false; //To check collector is not installed on the device
	private boolean isRunning = false; //To check collector is still on the memory of the device
	private boolean logReturnedData = false;
	private List<String> list = new ArrayList<String>();

	public void setLogReturnedData(boolean logdata) {
		this.logReturnedData = logdata;
	}

	public List<String> getReturnedData() {
		return list;
	}

	@Override
	public void processNewLines(String[] lines) {
		
		for (String line : lines) {

			if (line.length() > 0) {
				
				if (logReturnedData) {
					list.add(line);
				}
				
				noARO = false;
				isRunning = false;
				
				if (line.contains("Permission denied")) {
					shellError = true;
				}
				//Check if file already exists
				Matcher fileExists = FILE_EXISTS.matcher(line);
				if (fileExists.find()) {
					return;
				}
				// set Android SD card memory full flag
				Matcher sdcardfull = SDCARDFULL.matcher(line);
				if (sdcardfull.find()) {
					sdcardFull = true;
					return;
				}
				// set Android shell error flag
				if (setShellError(FAILURE.matcher(line))) {
					return;
				}
				if (setShellError(ERROR.matcher(line))) {
					return;
				}
				if (setShellError(SEG_ERROR.matcher(line))) {
					return;
				}
				if (setShellError(NOT_EXECUTABLE.matcher(line))) {
					failedExecute = true;
					return;
				}
				if (setShellError(ENFORCING.matcher(line))) {
					seLinuxEnforce = true;
					return;
				}
				if (setShellError(NOT_ENFORCING.matcher(line))) {
					seLinuxEnforce = false;
					return;
				}
				if (setShellError(NO_ARO.matcher(line))) {
					noARO = true;
					return;
				}
				if (setShellError(ACTIVITY_RUNNING.matcher(line))) {
					isRunning = true;
					return;
				}
			}
		}
	}

	/**
	 * Sets shell error flag in case of a failure.
	 * 
	 * @param errorMatcher
	 * @return
	 */
	private boolean setShellError(Matcher errorMatcher) {
		if (errorMatcher.find()) {
			this.shellError = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	public boolean isShellError() {
		return shellError;
	}

	public boolean isSdcardFull() {
		return sdcardFull;
	}

	public boolean failedToExecute() {
		return failedExecute;
	}

	public boolean isSELinuxEnforce() {
		return seLinuxEnforce;
	}

	public boolean isNoARO() {
		return noARO;
	}

	public void setNoARO(boolean noARO) {
		this.noARO = noARO;
	}

	public boolean isActivityRunning() {
		return isRunning;
	}

	public void setActivityRunning(boolean isActivityRunning) {
		this.isRunning = isActivityRunning;
	}

}
