package com.att.aro.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.att.aro.interfaces.ExternalProcessReaderSubscriber;

public class ExternalProcessReader extends Thread {
	private InputStream is;
	List<ExternalProcessReaderSubscriber> pubsub;
	volatile boolean willStop = false;
	InputStreamReader reader;
	BufferedReader breader;
	public ExternalProcessReader(InputStream stream){
		this.is = stream;
		pubsub = new ArrayList<ExternalProcessReaderSubscriber>();
	}
	public void addSubscriber(ExternalProcessReaderSubscriber subscriber){
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
            			Out(line);
            		}
            	}
            }
        }
        catch(IOException exception) {
            Out("Error: " + exception.getMessage());
        }
        notifyExit();
    }
	void Out(String message){
		for(ExternalProcessReaderSubscriber sub: pubsub){
			sub.newMessage(message);
		}
	}
	void notifyExit(){
		for(ExternalProcessReaderSubscriber sub: pubsub){
			sub.willExit();
		}
	}
}//end class
