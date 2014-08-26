package com.att.aro.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import com.att.aro.interfaces.IOSDeviceStatus;
import com.att.aro.interfaces.ExternalProcessReaderSubscriber;
import com.att.aro.util.Util;

public class ExternalDeviceMonitorIOS extends Thread implements ExternalProcessReaderSubscriber{
	private static final Logger logger = Logger.getLogger(ExternalDeviceMonitorIOS.class.getName());
	BufferedWriter writer;
	Process proc = null;
	String exepath;
	ExternalProcessReader procreader;
	int pid = 0;
	ExternalProcessRunner runner;
	volatile boolean shutdownSignal = false;
	List<IOSDeviceStatus> subscribers;
	public ExternalDeviceMonitorIOS(){
		this.runner = new ExternalProcessRunner();
		init();
	}
	public ExternalDeviceMonitorIOS(ExternalProcessRunner runner){
		this.runner = runner;
		init();
	}
	void init(){
		subscribers = new ArrayList<IOSDeviceStatus>();
		String dir = Util.getCurrentRunningDir();
		File dirfile = new File(dir);
		dir = dirfile.getParent();
		exepath = dir + Util.FILE_SEPARATOR +"bin" + Util.FILE_SEPARATOR + "libimobiledevice" + Util.FILE_SEPARATOR + "idevicesyslog_aro";
	}
	public void subscribe(IOSDeviceStatus subscriber){
		this.subscribers.add(subscriber);
	}
	@Override
    public void run() {
		String[] cmds = new String[]{"bash","-c",this.exepath};
		
		ProcessBuilder builder = new ProcessBuilder(cmds);
		builder.redirectErrorStream(true);
		
		try {
			proc = builder.start();
		} catch (IOException e) {
			e.printStackTrace();
			Out(e.getMessage());
			return;
		}
		
		writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
		procreader = new ExternalProcessReader(proc.getInputStream());
		procreader.addSubscriber(ExternalDeviceMonitorIOS.this);
		procreader.start();
		
		String[] pscmd = new String[]{"bash","-c","ps -o pid -o command | grep "+this.exepath};
		
		try {
			String str = runner.runCmd(pscmd);
			String[] strarr = str.split("\n");
			String token;
			for(int i =0;i<strarr.length;i++){
				token = strarr[i];
				if(token.contains(this.exepath)){
					this.extractPid(token);
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			proc.waitFor();
		} catch (InterruptedException e) {
		}
	}
	void extractPid(String line){
		line = line.trim();
		int end = line.indexOf(' ');
		if(end > 0){
			String sub = (String) line.subSequence(0, end);
			pid = Integer.parseInt(sub);
		}
	}
	public void stopMonitoring(){
		if(pid > 0){
			this.shutdownSignal = false;
			try {
				Runtime.getRuntime().exec("kill -SIGINT "+pid);
				Out("kill device monitor process pid: "+pid);
			} catch (IOException e) {
				e.printStackTrace();
				logger.severe(e.getMessage());
			}
			int count = 0;
			while(!shutdownSignal && count < 10){
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
				count++;
			}
		}
	}
	void Out(String str){
		logger.info(str);
	}
	/**
	 * stop everything and exit
	 */
	public void shutDown(){
		
		if(procreader != null){
			procreader.interrupt();
			procreader = null;
			proc.destroy();
		}
		this.shutdownSignal = true;
	}
	@Override
	public void newMessage(String message) {
		if(message.equals("[connected]")){
			logger.info("Device connected");
			notifyConnected();
		}else if(message.equals("[disconnected]")){
			logger.info("Device disconnected");
			notifyDisconnected();
		}
	}

	@Override
	public void willExit() {
		this.shutDown();
	}
	void notifyDisconnected(){
		for(IOSDeviceStatus sub: subscribers){
			sub.onDisconnected();
		}
	}
	void notifyConnected(){
		for(IOSDeviceStatus sub: subscribers){
			sub.onConnected();
		}
	}
}
