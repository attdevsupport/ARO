package com.att.aro.core.android.pojo;

import java.util.ArrayList;
import java.util.List;

import com.android.ddmlib.MultiLineReceiver;

/**
 * Class to get the output from the native process and display when
 * determining SD card available space.
 * It detects both emulator and USB device
 */

public class ShellCommandCheckSDCardOutputReceiver extends MultiLineReceiver{
	// name confused...
	private boolean sdCardAttached;
	private Long sdCardMemoryAvailable;
	private String[] resultOutput = new String[0];
	private List<String> list;
	public ShellCommandCheckSDCardOutputReceiver(){
		list = new ArrayList<String>();
	}
	public String[] getResultOutput() {
		return resultOutput;
	}

	public void setResultOutput(String[] resultOutput) {
		this.resultOutput = resultOutput;
	}

	@Override
	public void processNewLines(String[] arr) {		
		for(String line: arr){
			list.add(line);
		}
		this.resultOutput = list.toArray(new String[list.size()]);
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	public boolean isSDCardAttached() {
		return sdCardAttached;
	}

	public boolean isSDCardEnoughSpace(long kbs) {
		return isSDCardAttached()
				&& (sdCardMemoryAvailable == null || sdCardMemoryAvailable > kbs);//questions
	}


}
