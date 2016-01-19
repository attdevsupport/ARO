package com.att.aro.core.peripheral.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.att.aro.core.fileio.IFileManager;

@Component
public class PeripheralBase {
	
	@Autowired
	protected IFileManager filereader;
	
	public void setFileReader(IFileManager reader){
		this.filereader = reader;
	}
}
