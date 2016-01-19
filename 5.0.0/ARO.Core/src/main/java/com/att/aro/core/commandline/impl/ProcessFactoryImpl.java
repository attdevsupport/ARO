package com.att.aro.core.commandline.impl;

import java.io.File;
import java.io.IOException;

import com.att.aro.core.commandline.IProcessFactory;

public class ProcessFactoryImpl implements IProcessFactory{
	public Process create(String cmd) throws IOException{
		return Runtime.getRuntime().exec(cmd);
	}

	@Override
	public Process create(String command, String directory) throws IOException {
		return Runtime.getRuntime().exec(command, null, new File(directory));
	}

	@Override
	public Process create(String[] commands) throws IOException {
		return Runtime.getRuntime().exec(commands);
	}
}
