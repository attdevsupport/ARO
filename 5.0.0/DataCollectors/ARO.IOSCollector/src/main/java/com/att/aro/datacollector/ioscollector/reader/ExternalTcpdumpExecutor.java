package com.att.aro.datacollector.ioscollector.reader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import com.att.aro.core.ILogger;
import com.att.aro.core.impl.LoggerImpl;
import com.att.aro.datacollector.ioscollector.IExternalProcessReaderSubscriber;

public class ExternalTcpdumpExecutor extends Thread implements IExternalProcessReaderSubscriber {
	
	private ILogger log = new LoggerImpl("IOSCollector");
	BufferedWriter writer;
	Process proc = null;
	String pcappath;
	ExternalProcessReader processReader;
	String sudoPassword = "";
	String tcpdumpCommand;
	ExternalProcessRunner runner;
	volatile boolean shutdownSignal = false;
	int totalpacketCaptured = 0;
	List<Integer> pidlist;

	public ExternalTcpdumpExecutor(String pcappath, String sudopass, ExternalProcessRunner runner) throws Exception {
		this.pcappath = pcappath;
		this.sudoPassword = sudopass;
		this.runner = runner;
		pidlist = new ArrayList<Integer>();
	}

	@Override
	public void run() {
		log.debug("run");
		tcpdumpCommand = "echo " + this.sudoPassword + " | sudo -S tcpdump -i rvi0 -s 0 -w \"" + this.pcappath + "\"";
		log.debug(tcpdumpCommand);
		String[] cmds = new String[] { "bash", "-c", tcpdumpCommand };

		ProcessBuilder builder = new ProcessBuilder(cmds);
		builder.redirectErrorStream(true);

		try {
			proc = builder.start();
		} catch (IOException e) {
			log.debug("IOException:", e);
			return;
		}

		writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
		processReader = new ExternalProcessReader(proc.getInputStream());
		processReader.addSubscriber(ExternalTcpdumpExecutor.this);
		processReader.start();

		// find the processID for tcpdump, used to kill the process when done with trace
		if (!findPcapPathProcess(40)) {
			log.warn("failed to locate process number for " + this.pcappath);
		}
	}

	/**
	 * find running process for this.pcappath
	 * 
	 * @param attemptCounter
	 *            - number of times to attempt finding the process
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
				log.debug("IOException:", e1);
			}
			try {
				sleep(50);

			
			} catch (InterruptedException e) {
				log.debug("InterruptedException:", e);
			}
		}
		return result;
	}

	void extractPid(String line) {
		line = line.trim();
		int end = line.indexOf(' ');
		int pid;
		if (end > 0) {
			String sub = (String) line.subSequence(0, end);
			pid = Integer.parseInt(sub);
			if (!pidlist.contains(pid)) {
				pidlist.add(pid);
			}
		}
	}

	/**
	 * kill tcpdump which will cause shutdown() to be called after tcpdump is
	 * destroyed
	 */
	public void stopTcpdump() {

		log.info("shutting down tcpdump");
		
		if (pidlist.size() > 0) {
			this.shutdownSignal = false;
			String cmd, str;
			for (Integer pid : pidlist) {
				try {
					cmd = "echo " + this.sudoPassword + " | sudo -S kill -SIGINT " + pid;
					String[] pscmd = new String[] { "bash", "-c", cmd };
					str = runner.runCmd(pscmd);
			//		log.info("kill pid: " + pid);
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
			if (processReader != null) {
				processReader.setStop();//signal loop to quit
			}

			//wait at max 4 seconds
			int count = 0;
			while (!shutdownSignal && count < 40) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					log.debug("InterruptedException:", e);
					break;
				}
				count++;
			}
		}
		shutDown();

	}

	/**
	 * stop everything and exit
	 */
	public void shutDown() {

		if (processReader != null) {
			processReader.interrupt();
			processReader = null;
			proc.destroy();
		}
		this.shutdownSignal = true;
	}

	@Override
	public synchronized void newMessage(String message) {
	//	log.info(message);
		//nn packets captured
		if (message.contains("packets captured")) {
			int end = message.indexOf(' ');
			if (end > 0) {
				String nstr = message.substring(0, end);
				try {
					this.totalpacketCaptured = Integer.parseInt(nstr);
				} catch (Exception ex) {
				}
			}
		}
	}

	@Override
	public void willExit() {
		this.shutDown();
	}

	public int getTotalPacketCaptured() {
		return this.totalpacketCaptured;
	}
}
