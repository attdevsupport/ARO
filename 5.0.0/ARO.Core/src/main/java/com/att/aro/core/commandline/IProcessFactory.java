package com.att.aro.core.commandline;

import java.io.IOException;

public interface IProcessFactory {
	Process create(String command) throws IOException;
	Process create(String[] commands) throws IOException;
	Process create(String command, String directory) throws IOException;
}
