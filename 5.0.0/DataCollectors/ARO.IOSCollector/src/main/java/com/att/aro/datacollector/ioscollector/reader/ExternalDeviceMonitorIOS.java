package com.att.aro.datacollector.ioscollector.reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.att.aro.core.ILogger;
import com.att.aro.core.impl.LoggerImpl;
import com.att.aro.core.util.Util;
import com.att.aro.datacollector.ioscollector.IExternalProcessReaderSubscriber;
import com.att.aro.datacollector.ioscollector.IOSDeviceStatus;

public class ExternalDeviceMonitorIOS extends Thread implements IExternalProcessReaderSubscriber {
	private ILogger log = new LoggerImpl("IOSCollector");
	Process proc = null;
	String exepath;
	ExternalProcessReader procreader;
	int pid = 0;
	ExternalProcessRunner runner;
	volatile boolean shutdownSignal = false;
	List<IOSDeviceStatus> subscribers;

	public ExternalDeviceMonitorIOS() {
		this.runner = new ExternalProcessRunner();
		init();
	}

	public ExternalDeviceMonitorIOS(ExternalProcessRunner runner) {
		this.runner = runner;
		init();
	}

	void init() {
		subscribers = new ArrayList<IOSDeviceStatus>();
		
		exepath = Util.getAroLibrary()
				+ Util.FILE_SEPARATOR + ".drivers" 
				+ Util.FILE_SEPARATOR + "libimobiledevice" 
				+ Util.FILE_SEPARATOR + "idevicesyslog_aro";
		clearExe();
	}

	public void clearExe() {
		String response = null;
		try {
			String[] cmd = {"bash", "-c", "fuser -f "+exepath+"|xargs kill"};
			response = runner.runCmd(cmd);
		} catch (IOException e) {
			log.error("IOException", e);
		}
	}
	
	public void subscribe(IOSDeviceStatus subscriber) {
		this.subscribers.add(subscriber);
	}

	@Override
	public void run() {
		String[] cmds = new String[] { "bash", "-c", this.exepath };

		ProcessBuilder builder = new ProcessBuilder(cmds);
		builder.redirectErrorStream(true);

		try {
			proc = builder.start();
		} catch (IOException e) {
			log.error("IOException :", e);
			return;
		}

		procreader = new ExternalProcessReader(proc.getInputStream());
		procreader.addSubscriber(ExternalDeviceMonitorIOS.this);
		procreader.start();

		setPid(exepath);
		
		try {
			proc.waitFor();
		} catch (InterruptedException e) {
		}
	}

	/**
	 * locate first instance of theExec process and set the pid value in this.pid
	 * 
	 * @param theExec
	 */
	private void setPid(String theExec) {
		String response = null;
		try {
			String cmd = "fuser -f " + theExec;
			response = runner.runCmd(cmd);
			String[] pids = response.trim().split(" ");
			if (pids.length > 0) {
				pid = Integer.parseInt(pids[0]);
			}
		} catch (IOException e) {
			log.error("IOException", e);
		}
	}

	/**
	 * signals pid process to 
	 */
	public void stopMonitoring() {
		if (pid > 0) {
			this.shutdownSignal = false;
			try {
				Runtime.getRuntime().exec("kill -SIGINT " + pid);
				log.info("kill device monitor process pid: " + pid);
			} catch (IOException e) {
				log.error("IOException :", e);
			}
			int count = 0;
			while (!shutdownSignal && count < 10) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					break;
				}
				count++;
			}
		}
	}

	/**
	 * stop everything and exit
	 */
	public void shutDown() {

		if (procreader != null) {
			procreader.interrupt();
			procreader = null;
			proc.destroy();
		}
		this.shutdownSignal = true;
	}

	@Override
	public void newMessage(String message) {
		if (message.equals("[connected]")) {
			log.info("Device connected");
			notifyConnected();
		} else if (message.equals("[disconnected]")) {
			log.info("Device disconnected");
			notifyDisconnected();
		}
	}

	@Override
	public void willExit() {
		this.shutDown();
	}

	void notifyDisconnected() {
		for (IOSDeviceStatus sub : subscribers) {
			sub.onDisconnected();
		}
	}

	void notifyConnected() {
		for (IOSDeviceStatus sub : subscribers) {
			sub.onConnected();
		}
	}
}
