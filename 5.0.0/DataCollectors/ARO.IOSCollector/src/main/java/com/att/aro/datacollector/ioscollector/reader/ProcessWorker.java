package com.att.aro.datacollector.ioscollector.reader;

public class ProcessWorker extends Thread {
	  private final Process process;
	  long timeout = 100;
	  volatile boolean exit = false;
	  int maxcount = 1;
	  public ProcessWorker(Process process, long timeout) {
	    this.process = process;
	    if(timeout > this.timeout){
	    	this.timeout = timeout;
	    	maxcount = (int) (timeout / 100);
	    }
	  }
	  public void setExit(){
		  this.exit = true;
	  }
	  public void run() {
		int counter = 0;
	    while(!exit){
	    	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
	    	counter++;
	    	if(counter >= maxcount){
	    		if(process != null){
	    			process.destroy();
	    		}
	    		break;
	    	}
	    }
	  }  
}
