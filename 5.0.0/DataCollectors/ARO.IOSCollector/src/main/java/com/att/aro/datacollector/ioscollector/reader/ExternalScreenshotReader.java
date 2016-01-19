package com.att.aro.datacollector.ioscollector.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.att.aro.core.impl.LoggerImpl;
import com.att.aro.datacollector.ioscollector.IScreenshotPubSub;

public class ExternalScreenshotReader extends Thread {
	

	LoggerImpl log = new LoggerImpl(this.getClass().getName());
	
	private InputStream is;
	List<IScreenshotPubSub> pubsub;

	public ExternalScreenshotReader(InputStream stream) {
		this.is = stream;
		pubsub = new ArrayList<IScreenshotPubSub>();
	}

	public void addSubscriber(IScreenshotPubSub subscriber) {
		pubsub.add(subscriber);
	}

	@Override
	public void run() {
		String line = null;
		try {
			InputStreamReader reader = new InputStreamReader(is);
			BufferedReader bufferedReader = new BufferedReader(reader);

			// connection recognized here
			while (true) {
				line = bufferedReader.readLine();
				log.debug("    >>"+line);
				if (line == null) {
					break;
				} else {
					line = line.trim();
					if (line.length() > 0) {
						updateSubsribers(line);
					}
				}
			}
			
		} catch (IOException exception) {
			log.error("IOException - updateSubsribers :", exception);
			updateSubsribers("Error: " + exception.getMessage());
		}
		updateSubsribers(">Exited ExternalProcessReader");
		notifyExit();
	}

	void updateSubsribers(String str) {
		for (IScreenshotPubSub pub : pubsub) {
		//	log.debug("pub to subscriber :"+pub.getClass().getName()+ " str:"+str);
			pub.newMessage(str);
		}
	}

	void notifyExit() {
		for (IScreenshotPubSub pub : pubsub) {
		//	log.debug("notify willExit :"+pub.getClass().getName());
			pub.willExit();
		}
	}
}
