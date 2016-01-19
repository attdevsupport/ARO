package com.att.aro.datacollector.ioscollector.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.att.aro.datacollector.ioscollector.IExternalProcessReaderSubscriber;

public class ExternalProcessReader extends Thread {
	private InputStream is;
	List<IExternalProcessReaderSubscriber> pubsub;
	volatile boolean willStop = false;
	InputStreamReader reader;
	BufferedReader breader;
	public ExternalProcessReader(InputStream stream){
		this.is = stream;
		pubsub = new ArrayList<IExternalProcessReaderSubscriber>();
	}
	public void addSubscriber(IExternalProcessReaderSubscriber subscriber){
		this.pubsub.add(subscriber);
	}
	public void setStop(){
		this.willStop = true;
	}
	@Override
    public void run() {
        String line = null;
        try {
            reader = new InputStreamReader(is);
            breader = new BufferedReader(reader);
            
            while(!willStop){
            	line = breader.readLine();
            	
            	if(line == null){
            		break;
            	}else{
            		line = line.trim();
            		if(line.length() > 0){
            			updateSubsribers(line);
            		}
            	}
            }
        }
        catch(IOException exception) {
            updateSubsribers("Error: " + exception.getMessage());
        }
        notifyExit();
    }
	void updateSubsribers(String message){
		for(IExternalProcessReaderSubscriber sub: pubsub){
			sub.newMessage(message);
		}
	}
	void notifyExit(){
		for(IExternalProcessReaderSubscriber sub: pubsub){
			sub.willExit();
		}
	}
}//end class
