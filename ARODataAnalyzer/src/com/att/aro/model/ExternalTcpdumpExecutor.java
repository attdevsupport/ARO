package com.att.aro.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import com.att.aro.interfaces.ExternalProcessReaderSubscriber;

public class ExternalTcpdumpExecutor extends Thread implements ExternalProcessReaderSubscriber {
	private static final Logger logger = Logger.getLogger(ExternalTcpdumpExecutor.class.getName());
	BufferedWriter writer;
	Process proc = null;
	String pcappath;
	ExternalProcessReader procreader;
	String sudoPassword = "";
	String tcpdumpCommand;
	ExternalProcessRunner runner;
	volatile boolean shutdownSignal = false;
	int totalpacketCaptured = 0;
	List<Integer> pidlist;
	public ExternalTcpdumpExecutor(String pcappath, String sudopass, ExternalProcessRunner runner) throws Exception{
		this.pcappath = pcappath;
		this.sudoPassword = sudopass;
		this.runner = runner;
		pidlist = new ArrayList<Integer>();
	}
	
	@Override
    public void run() {
		tcpdumpCommand = "echo "+ this.sudoPassword +" | sudo -S tcpdump -i rvi0 -s 0 -w \""+this.pcappath+"\"";
		String[] cmds = new String[]{"bash","-c",tcpdumpCommand};
		
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
		procreader.addSubscriber(ExternalTcpdumpExecutor.this);
		procreader.start();
		
		// find the processID for tcpdump, used to kill the process when done with trace
		if (!findPcapPathProcess(40)){
			logger.severe("failed to locate process number for "+this.pcappath);
		}
	}
	
	/**
	 * find running process for this.pcappath
	 * 
	 * @param attemptCounter - number of times to attempt finding the process
	 * @return process string from ps
	 */
	private boolean findPcapPathProcess(int attemptCounter) {

		String[] pscmd = new String[] { "bash", "-c", "ps ax | grep \"" + this.pcappath + "\"" };
		boolean result = false;

		while ((!result) || (attemptCounter-- > 0)) {
			try {
				String str = runner.runCmd(pscmd);
				String[] strarr = str.split("\n");
				String token;
				// find child process first
				for (int i = 0; i < strarr.length; i++) {
					token = strarr[i];
					if (token.contains(this.pcappath) && !token.contains("grep ") && !token.contains("sudo ")) {
						// record the ProcessID
						this.extractPid(token);
						result = true;
						break;
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	void extractPid(String line){
		line = line.trim();
		int end = line.indexOf(' ');
		int pid;
		if(end > 0){
			String sub = (String) line.subSequence(0, end);
			pid = Integer.parseInt(sub);
			if(!pidlist.contains(pid)){
				pidlist.add(pid);
			}
		}
	}
	
	/**
	 * kill tcpdump which will cause shutdown() to be called after tcpdump is destroyed
	 */
	public void stopTcpdump(){
		
		if(pidlist.size() > 0){
			this.shutdownSignal = false;
			String cmd, str;
			for(Integer pid : pidlist){
				try {
					cmd = "echo "+ this.sudoPassword +" | sudo -S kill -SIGINT "+pid;
					String[] pscmd = new String[]{"bash","-c",cmd};
					str = runner.runCmd(pscmd);
					Out("kill pid: "+pid);
				} catch (IOException e) {
					logger.severe(e.getMessage());
				}
			}
			if(procreader != null){
				procreader.setStop();//signal loop to quit
			}
			
			//wait at max 4 seconds
			int count = 0;
			while(!shutdownSignal && count < 40){
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
				count++;
			}
		}
		shutDown();
		
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
	public synchronized void newMessage(String message) {
		logger.info(message);
		//nn packets captured
		if(message.contains("packets captured")){
			int end = message.indexOf(' ');
			if(end > 0){
				String nstr = message.substring(0, end);
				try{
					this.totalpacketCaptured = Integer.parseInt(nstr);
				}catch(Exception ex){}
			}
		}
	}

	@Override
	public void willExit() {
		this.shutDown();
	}

	public int getTotalPacketCaptured(){
		return this.totalpacketCaptured;
	}
}
