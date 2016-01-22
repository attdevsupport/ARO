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
package com.att.aro.core.android.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IDevice.DeviceState;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.SyncService;
import com.android.ddmlib.TimeoutException;
import com.att.aro.core.ILogger;
import com.att.aro.core.android.IAndroid;
import com.att.aro.core.android.pojo.ShellCommandCheckSDCardOutputReceiver;
import com.att.aro.core.android.pojo.ShellOutputReceiver;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.pojo.TraceDataConst;

public class AndroidImpl implements IAndroid {

	@InjectLogger
	private static ILogger logger;
	@Autowired
	private IFileManager filereader;

	/**
	 * Indicates if an trace is running on the device. Used to detect when it is safe to pull a trace from the device.
	 */
	private boolean traceRunning = false;

	/** State of the device. */
	private final static String TCPDUMP = "tcpdump";

	/**
	 * 
	 * Used to launch tcpdump
	 */
	private final static String TCPDUMPPATH = "/data/data/com.att.android.arodatacollector/" + TCPDUMP;
	private final static int TCPDUMP_PORT = 50999;

	private final static String KBYTE = "K";
	private final static String MBYTE = "M";
	private final static String GBYTE = "G";

	private static final String[] DATAEMULATORCOLLECTORTRACEFILENAMES = { TraceDataConst.FileName.CPU_FILE, TraceDataConst.FileName.APPID_FILE,
			TraceDataConst.FileName.APPNAME_FILE, TraceDataConst.FileName.TIME_FILE, TraceDataConst.FileName.USER_EVENTS_FILE, TraceDataConst.FileName.PCAP_FILE, };

	private static final String[] DATADEVICECOLLECTORTRACEFILENAMES = {
		TraceDataConst.FileName.CPU_FILE, TraceDataConst.FileName.APPID_FILE, TraceDataConst.FileName.APPNAME_FILE,
		TraceDataConst.FileName.TIME_FILE,TraceDataConst.FileName.SSLKEY_FILE, TraceDataConst.FileName.USER_EVENTS_FILE,
		TraceDataConst.FileName.ACTIVE_PROCESS_FILE, TraceDataConst.FileName.BATTERY_FILE,
		TraceDataConst.FileName.BLUETOOTH_FILE, TraceDataConst.FileName.CAMERA_FILE,
		TraceDataConst.FileName.DEVICEDETAILS_FILE, TraceDataConst.FileName.DEVICEINFO_FILE,
		TraceDataConst.FileName.GPS_FILE, TraceDataConst.FileName.NETWORKINFO_FILE,
		TraceDataConst.FileName.PROP_FILE, TraceDataConst.FileName.RADIO_EVENTS_FILE,
		TraceDataConst.FileName.SCREEN_STATE_FILE, TraceDataConst.FileName.SCREEN_ROTATIONS_FILE,
		TraceDataConst.FileName.TIME_FILE, TraceDataConst.FileName.USER_INPUT_LOG_EVENTS_FILE,
		TraceDataConst.FileName.ALARM_START_FILE, TraceDataConst.FileName.ALARM_END_FILE,TraceDataConst.FileName.BATTERYINFO_FILE,TraceDataConst.FileName.KERNEL_LOG_FILE,
		TraceDataConst.FileName.VIDEO_TIME_FILE, TraceDataConst.FileName.WIFI_FILE, TraceDataConst.FileName.PCAP_FILE
	};

	public boolean setExecutePermission(IDevice device, String remotePath) {
		return executeShellCommand(device, "chmod 777 " + remotePath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.att.aro.core.android.impl.IAndroid#isEmulator(com.android.ddmlib.
	 * IDevice)
	 */
	@Override
	public boolean isEmulator(IDevice device) {
		return device.isEmulator();
	}

	public ShellOutputReceiver getShellOutput() {
		return new ShellOutputReceiver();
	}

	public ShellCommandCheckSDCardOutputReceiver getOutputReturn() {
		return new ShellCommandCheckSDCardOutputReceiver();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.att.aro.core.android.impl.IAndroid#pushFile
	 * (com.android.ddmlib.IDevice, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean pushFile(IDevice emulator, String local, String remote) {

		SyncService service = getSyncService(emulator);
		boolean success = false;
		if (service != null) {
			try {
				service.pushFile(local, remote, SyncService.getNullProgressMonitor());
				success = true;
				return success;
			} catch (SyncException e) {
				logger.error(e.getMessage());
			} catch (IOException e) {
				logger.error(e.getMessage());
			} catch (TimeoutException e) {
				logger.error(e.getMessage());
			}
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.att.aro.core.android.impl.IAndroid#getState()
	 */
	@Override
	public DeviceState getState(IDevice device) {
		return device.getState();
	}

	//check sd card
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.att.aro.core.android.impl.IAndroid#isSDCardAttached(com.android.ddmlib
	 * .IDevice)
	 */
	@Override
	public boolean isSDCardAttached(IDevice device) {
		String[] shellResult = getShellReturn(device, "df");
		boolean sdCardAttached = false;
		if (shellResult == null) {
			return sdCardAttached;
		}
		for (String oneLine : shellResult) {
			//Checks the SDCard line from 'DF' command output  
			if ((oneLine.toLowerCase().contains("/sdcard"))) {
				// We find SD card is attached to emulator instance
				sdCardAttached = true;
			} else if (oneLine.toLowerCase().contains("mnt/shell")) {
				// We find SD card is attached to USB device (/mnt/shell/emulated)
				sdCardAttached = true;
			} else {
				sdCardAttached = false;
			}
		}
		return sdCardAttached;
	}

	//checkemulatorSDCard
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.att.aro.core.android.impl.IAndroid#isSDCardEnoughSpace(com.android
	 * .ddmlib.IDevice, long)
	 */
	@Override
	public boolean isSDCardEnoughSpace(IDevice device, long kbs) {
		String[] shellResult = getShellReturn(device, "df");
		Long sdCardMemoryAvailable = 0L;
		boolean sdCardHasEnoughSpace = false;
		if (shellResult == null) {
			return sdCardHasEnoughSpace;
		}
		for (String oneLine : shellResult) {
			if ((oneLine.toLowerCase().contains("/sdcard")) || oneLine.toLowerCase().contains("mnt/shell")) {
				//				sdCardMemoryAvailable = getNumber(oneLine, "\\s+");
				//			}else if(oneLine.toLowerCase().contains("mnt/shell")){				
				//				sdCardMemoryAvailable = getNumber(oneLine, "/emu+");
				sdCardMemoryAvailable = getNumber(oneLine, "\\s+");
				break;
			} else {
				sdCardMemoryAvailable = 0L;
			}
		}
		return (sdCardMemoryAvailable > kbs);
	}

	private long getNumber(String oneLine, String split) {
		String strFileSize = null;
		Long sdCardMemoryAvailable = 0L;
		String strValues[] = oneLine.split(split);
		if (strValues.length < 3) {
			return 0L;
		}
		try {
			if (oneLine.contains("available")) {
				// 5th value in  strValues array for 2.1 and 2.2 v of  emulator
				strFileSize = strValues[5];
			} else {
				// 3rd value instrValues array for 2.3 and above of emulator
				strFileSize = strValues[3];
			}
		} catch (IndexOutOfBoundsException e) {
			logger.error("Error parsing SD card df output", e);
		}

		if (strFileSize != null) {
			//logger.debug("found free space: "+strFileSize);
			try {
				// Checks if the available size in KB/MB/GB, we
				// convert the SD
				// card available space in KB before checking for
				// minimum 5 MB space requirement on sd card
				double iFileSizeInKB = -1;
				if (strFileSize.contains(KBYTE)) {
					final String iFileSizeKB[] = strFileSize.split(KBYTE);
					iFileSizeInKB = Double.valueOf(iFileSizeKB[0]);
				} else if (strFileSize.contains(MBYTE)) {
					final String iFileSizeMB[] = strFileSize.split(MBYTE);
					iFileSizeInKB = Double.valueOf(iFileSizeMB[0]);
					// Converting to KB
					iFileSizeInKB = iFileSizeInKB * 1024;
				} else if (strFileSize.contains(GBYTE)) {
					final String iFileSizeGB[] = strFileSize.split(GBYTE);
					iFileSizeInKB = Double.valueOf(iFileSizeGB[0]);
					// Converting to KB
					iFileSizeInKB = iFileSizeInKB * (1024 * 1024);
				}
				//logger.debug("fileSizeInKb: "+iFileSizeInKB);
				if (iFileSizeInKB >= 0) {
					sdCardMemoryAvailable = (new Double(Math.round(iFileSizeInKB))).longValue();
				}
			} catch (Exception e) {
				logger.error("get number format exception", e);
			}
		}
		return sdCardMemoryAvailable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.att.aro.core.android.impl.IAndroid#pullTraceFilesFromEmulator
	 * (com.android.ddmlib.IDevice, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean pullTraceFilesFromEmulator(IDevice device, String remoteFilepath, String localFilename) {
		SyncService service = getSyncService(device);
		boolean success = false;
		if (service != null && remoteFilepath != null && localFilename != null) {
			for (String file : DATAEMULATORCOLLECTORTRACEFILENAMES) {
				try {
					//v24 api return change 
					File tempfile = filereader.createFile(localFilename, file);
					service.pullFile(remoteFilepath + "/" + file, tempfile.getAbsolutePath(), SyncService.getNullProgressMonitor());
					success = true;
				} catch (SyncException e) {
					success = false;
					logger.error(e.getMessage());
				} catch (TimeoutException e) {
					success = false;
					logger.error(e.getMessage());
				} catch (IOException e) {
					success = false;
					logger.error(e.getMessage());
				}
			}
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.att.aro.core.android.impl.IAndroid#pullTraceFilesFromDevice(com.android
	 * .ddmlib.IDevice , java.lang.String, java.lang.String)
	 */
	@Override
	public boolean pullTraceFilesFromDevice(IDevice device, String remoteFilepath, String localFilename) {
		SyncService service = getSyncService(device);
		boolean success1 = false;
		boolean success2 = false;
		if (service != null && remoteFilepath != null && localFilename != null) {
			String srcFile = null;
			String dstFile = null;
			for (String file : DATADEVICECOLLECTORTRACEFILENAMES) {
				srcFile = remoteFilepath + "/" + file;
				File tempfile2 = filereader.createFile(localFilename, file);
				dstFile = tempfile2.getAbsolutePath();
				try {
					service.pullFile(srcFile, dstFile, SyncService.getNullProgressMonitor());
					success1 = true;
				} catch (SyncException e) {
					success1 = false;
					logger.error(e.getMessage());
				} catch (TimeoutException e) {
					success1 = false;
					logger.error(e.getMessage());
				} catch (IOException e) {
					success1 = false;
					logger.error(e.getMessage());
				}
			}

			//We do need to pull multiple pcap files if they are 
			//available in trace directory (traffic1.cap,traffic2.cap ...)
			for (int index = 1; index < 50; index++) {
				try {
					String fileName = "traffic" + index + ".cap";
					service.pullFile(remoteFilepath + "/" + fileName, filereader.createFile(localFilename, fileName).getAbsolutePath(), SyncService.getNullProgressMonitor());
					success2 = true;
				} catch (SyncException e) {
					success2 = false;
					logger.error(e.getMessage());
				} catch (TimeoutException e) {
					success2 = false;
					logger.error(e.getMessage());
				} catch (IOException e) {
					success2 = false;
					logger.error(e.getMessage());
				}

			}
		}
		return (success1 && success2);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.att.aro.core.android.impl.IAndroid#makeAROTraceDirectory
	 * (com.android.ddmlib.IDevice, java.lang.String)
	 */
	@Override
	public boolean makeAROTraceDirectory(IDevice device, String traceName) {
		boolean success = makeDirectory(device, "/sdcard/ARO/");
		if (success) {
			success = makeDirectory(device, "/sdcard/ARO/" + traceName);
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.att.aro.core.android.impl.IAndroid#makeDirectory
	 * (com.android.ddmlib.IDevice, java.lang.String)
	 */
	@Override
	public boolean makeDirectory(IDevice device, String dirpath) {
		String arocmd = "mkdir " + dirpath;
		return executeShellCommand(device, arocmd);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.att.aro.core.android.impl.IAndroid#getProperty(com.android.ddmlib
	 * .IDevice, java.lang.String)
	 */
	@Override
	public String getProperty(IDevice device, String property) {
		return device.getProperty(property);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.att.aro.core.android.impl.IAndroid#getSyncService(com.android.ddmlib
	 * .IDevice)
	 */
	@Override
	public SyncService getSyncService(IDevice device) {
		try {
			return device.getSyncService();
		} catch (TimeoutException e) {
			logger.error(e.getMessage());
		} catch (AdbCommandRejectedException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.att.aro.core.android.impl.IAndroid#removeEmulatorData(com.android
	 * .ddmlib.IDevice, java.lang.String)
	 */
	@Override
	public boolean removeEmulatorData(IDevice device, String deviceTracePath) {
		boolean success = false;
		boolean result1 = executeShellCommand(device, "rm " + deviceTracePath + "/*");
		logger.debug("remove files under " + deviceTracePath + ": " + result1);
		boolean result2 = executeShellCommand(device, "rm -fr " + deviceTracePath);
		logger.debug("remove dir " + deviceTracePath + ": " + result2);
		if (result1 && result2) {
			success = true;
		}
		return success;

	}

	@Override
	public boolean stopTcpDump(IDevice device) {
		Socket emulatorSocket = null;
		try {
			emulatorSocket = getLocalSocket();
			OutputStream out = emulatorSocket.getOutputStream();
			if (out != null) {
				out.write("STOP".getBytes("ASCII"));
				out.flush();
				out.close();
			}
			out.close();
			emulatorSocket.close();
			return true;
		} catch (UnknownHostException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return false;
	}

	public Socket getLocalSocket() throws UnknownHostException, IOException {
		return new Socket("127.0.0.1", TCPDUMP_PORT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.att.aro.core.android.impl.IAndroid#checkTcpDumpRunning
	 * (com.android.ddmlib.IDevice)
	 */
	@Override
	public boolean checkTcpDumpRunning(IDevice device) {
		String psCmd = isEmulator(device)?"ps|grep tcpdump":"ps tcpdump";
		String[] shellLineResult = getShellReturn(device, psCmd);
		return isCollectorRunningInShell(shellLineResult);
	}

	/* Comparing with shellOuput to make sure arodatacollector running on device */
	private boolean isCollectorRunningInShell(String[] shellOutput) {
		boolean collectorRunning = false;
		for (int index = 0; index < shellOutput.length; index++) {
			if (shellOutput[index].contains("arodatacollector")) {
				collectorRunning = true;
				return collectorRunning;
			}
		}
		return collectorRunning;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.att.aro.core.android.impl.IAndroid#startTcpDump
	 * (com.android.ddmlib.IDevice, java.lang.String)
	 */
	@Override
	public boolean startTcpDump(IDevice device, boolean seLinuxEnforced, String traceFolderName) {
		
		// if SELinux is enforced append the correct command to ignore port 5555
		String tcpName = TCPDUMPPATH + (seLinuxEnforced ? "_pie" : "");
		String strTcpDumpCommand = tcpName + " -w " + traceFolderName + (seLinuxEnforced ? " \"tcp port not 5555\"" : " port not 5555");
		logger.info("tcp >> >" + strTcpDumpCommand + ">");
		traceRunning = true;
		// this executeShellCommand is blocking and will terminate once tcpdump has completed or terminated
		boolean result = executeShellCommand(device, strTcpDumpCommand);
		traceRunning = false;
		return result;
	}

	/**
	 * Used to detect if a trace is in progress.
	 * 
	 * @return true indicates a trace is in progress, false indicates no trace is active
	 */
	@Override
	public boolean isTraceRunning() {
		return traceRunning;
	}

	private boolean executeShellCommand(IDevice device, String arocmd) {
		ShellOutputReceiver shelloutPut = getShellOutput();
		if (arocmd.contains("chmod")) {
			shelloutPut.setLogReturnedData(true);
		}
		boolean success = true;
		try {
			logger.debug(">>---> executeShellCommand :"+arocmd);
			device.executeShellCommand(arocmd, shelloutPut, 0, null);
		} catch (TimeoutException e) {
			success = false;
			logger.error(e.getMessage());
		} catch (AdbCommandRejectedException e) {
			success = false;
			logger.error(e.getMessage());
		} catch (ShellCommandUnresponsiveException e) {
			success = false;
			logger.error(e.getMessage());
		} catch (IOException e) {
			success = false;
			logger.error(e.getMessage());
		}
		if (success && shelloutPut.isShellError()) {
			success = false;
		}
		if (shelloutPut.getReturnedData().size() > 0) {
			logger.debug("========= Shell output data ===========");
			for (String line : shelloutPut.getReturnedData()) {
				logger.debug(line);
			}
			logger.debug("========= end Shell output data ===========");
		}
		return success;

	}

	@Override
	public String[] getShellReturn(IDevice device, String arocmd) {
		ShellCommandCheckSDCardOutputReceiver shellout = getOutputReturn();
		try {
			logger.debug("excute :" + arocmd);
			device.executeShellCommand(arocmd, shellout);
		} catch (TimeoutException e) {
			logger.error("TimeoutException", e);
		} catch (AdbCommandRejectedException e) {
			logger.error("AdbCommandRejectedException", e);
		} catch (ShellCommandUnresponsiveException e) {
			logger.error("ShellCommandUnresponsiveException", e);
		} catch (IOException e) {
			logger.error("IOException", e);
		}
		return shellout.getResultOutput();
	}

	/**
	 * check if a package exist on device
	 */
	@Override
	public boolean checkPackageExist(IDevice device, String fullpackageName) {
		String[] list = getShellReturn(device, "pm list packages");
		if (list != null && list.length > 0) {
			for (String line : list) {
				if (line.contains(fullpackageName)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean runApkInDevice(IDevice device, String cmd) {
		boolean success = true;
		clearLogcat(device);
		String[] results = getShellReturn(device, cmd);
		if (results == null || results.length < 1) {
			success = false;
		} else {
			for (String line : results) {
				if (line.contains("does not exist") || line.contains("Error")) {
					success = false;
				}
			}
		}
		return success;
	}


	@Override
	public boolean runVpnApkInDevice(IDevice device) {
		String cmd = "am start -n com.att.arocollector/com.att.arocollector.AROCollectorActivity";
		return runApkInDevice(device, cmd);
	}

	/**
	 * excutes "logcat -c" on Android to clear the logcat
	 * 
	 * @param device - Android device
	 */
	private void clearLogcat(IDevice device) {
		getShellReturn(device, "logcat -c");
	}

}
