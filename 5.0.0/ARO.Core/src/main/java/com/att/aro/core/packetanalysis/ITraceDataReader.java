package com.att.aro.core.packetanalysis;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.packetanalysis.pojo.TraceFileResult;

public interface ITraceDataReader {
	
	TraceFileResult readTraceFile(String traceFilePath) throws IOException;
	
	TraceDirectoryResult readTraceDirectory(String directoryPath) throws FileNotFoundException;
	
}
