package com.att.aro.libimobiledevice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ExternalScreenshotReader extends Thread {
	private InputStream is;
	List<ScreenshotPubSub> pubsub;
    public ExternalScreenshotReader(InputStream stream) {
        this.is = stream;
        pubsub = new ArrayList<ScreenshotPubSub>();
    }
    public void addSubscriber(ScreenshotPubSub subscriber){
    	pubsub.add(subscriber);
    }

    @Override
    public void run() {
        String line = null;
        try {
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader breader = new BufferedReader(reader);
            
            while(true){
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
        Out(">Exited ExternalProcessReader");
        notifyExit();
    }
    void Out(String str){
    	for(ScreenshotPubSub pub : pubsub){
    		pub.newMessage(str);
    	}
    }
    void notifyExit(){
    	for(ScreenshotPubSub pub : pubsub){
    		pub.willExit();
    	}
    }
}
