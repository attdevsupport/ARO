/*
 *  Copyright 2014 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.att.aro.core;

import java.io.IOException;
import java.util.List;

import com.att.aro.core.bestpractice.pojo.BestPracticeType;
import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.packetanalysis.pojo.AnalysisFilter;
import com.att.aro.core.pojo.AROTraceData;

/**
 * This class provides access to ARO.Core functionality for analyzing and
 * generating reports.
 * 
 * <pre>
 * To analyze a trace use analyzeDirectory or analyzeFile. These will generate an AROTraceData object.
 * Html and Json formatted reports can be generated once the analysis has been performed.
 * </pre>
 * 
 * <pre>
 * Example:
 *   IAROService serv = context.getBean(IAROService.class);
 *   List&lt;BestPracticeType&gt; listOfBestPractices = new new ArrayList&lt;BestPracticeType&gt;();
 *   listOfBestPractices.add(BestPracticeType.UNNECESSARY_CONNECTIONS);
 *   listOfBestPractices.add(BestPracticeType.SCREEN_ROTATION);
 *   AROTraceData data = serv.analyzeFile(listOfBestPractices, "/yourTracePath/AROTraceAndroid/trace1");
 *   
 *   // generate json report
 *   serv.getJSonReport("/yourPath/output.json", data);
 *   
 *   // generate html report
 *   serv.getHtmlReport("/yourPath/output.html", data);
 * </pre>
 * 
 * @author Borey Sao Date: March 27, 2014
 *
 */
public interface IAROService {

	/**
	 * Returns the name of the Application
	 * 
	 * @return name of Application
	 */
	String getName();

	/**
	 * Returns the version of the Application
	 * 
	 * @return Application version
	 */
	String getVersion();

	/**
	 * Indicates if this file represents a file on the underlying file system.
	 * 
	 * @param path
	 *            of file to examine
	 * @return true is file is a file, false if not
	 */
	boolean isFile(String path);

	/**
	 * Indicates if file exists
	 * 
	 * @param path
	 *            of file, to include file name
	 * @return true if file exists, false otherwise
	 */
	boolean isFileExist(String path);

	/**
	 * Indicates if folder/directory exists
	 * 
	 * @param path
	 *            of folder/directory
	 * @return true if exists, false if not
	 */
	boolean isFolderExist(String path);

	/**
	 * Determine if parent directory of file exists
	 * 
	 * @param path
	 *            of file
	 * @return true if parent directory exists, false otherwise
	 */
	boolean isFileDirExist(String path);

	/**
	 * Determine if path is to a file or directory. Returns the parent directory
	 * if a file
	 * 
	 * @param path
	 *            to examine
	 * @return path or parent directory if a file or null if path does not exist
	 */
	String getDirectory(String path);

	/**
	 * Generate Packet Analysis Report in HTML format
	 * 
	 * @param resultFilePath
	 *            the path for the output report
	 * @param results
	 *            the AROTraceData path
	 * @return true if report generated, false if AROTraceData is null or failed
	 *         to create/write output file
	 */
	boolean getHtmlReport(String resultFilePath, AROTraceData results);

	/**
	 * Generate Packet Analysis Report in JSON format
	 * 
	 * @param resultFilePath
	 *            the path for the output report
	 * @param results
	 *            the AROTraceData path
	 * @return true if report generated, false if AROTraceData is null or failed
	 *         to create/write output file
	 */
	boolean getJSonReport(String resultFilePath, AROTraceData results);

	/**
	 * Launches an analysis of a traceFile with the results populating an
	 * AROTraceData object
	 * 
	 * @param requests
	 *            list of BestPracticeType bestPractices to analyze
	 * @param traceFile
	 *            path to a pcap trace file, usually traffic.cap
	 * @return AROTraceData object
	 * @throws IOException
	 *             if trace file not found
	 */
	AROTraceData analyzeFile(List<BestPracticeType> requests, String traceFile) throws IOException;

	/**
	 * Launches an analysis of a trace directory with the results populating an
	 * AROTraceData object.
	 * <p>
	 * Other trace files depend on capture method, platform and version of
	 * device.
	 * </p>
	 * 
	 * @param requests
	 *            list of BestPracticeType bestPractices to analyze
	 * @param traceDirectory
	 *            path to a trace directory, usually contains traffic.cap
	 * @return AROTraceData object
	 * @throws IOException
	 *             if trace file not found
	 */
	AROTraceData analyzeDirectory(List<BestPracticeType> requests, String traceDirectory) throws IOException;

	/**
	 * Launches an analysis of a traceFile with the results populating an
	 * AROTraceData object
	 * 
	 * @param requests
	 *            list of BestPracticeType bestPractices to analyze
	 * @param traceFile
	 *            path to a pcap trace file, usually traffic.cap
	 * @param profile
	 * 
	 * @param filter
	 * 
	 * @return AROTraceData object
	 * @throws IOException
	 *             if trace file not found
	 */
	AROTraceData analyzeFile(List<BestPracticeType> requests, String traceFile, Profile profile, AnalysisFilter filter) throws IOException;

	/**
	 * Launches an analysis of a trace directory with the results populating an
	 * AROTraceData object.
	 * <p>
	 * Other trace files depend on capture method, platform and version of
	 * device.
	 * </p>
	 * 
	 * @param requests
	 *            list of BestPracticeType bestPractices to analyze
	 * @param traceDirectory
	 *            path to a trace directory, usually contains traffic.cap
	 * @param profile
	 * 
	 * @param filter
	 * 
	 * @return AROTraceData object
	 * @throws IOException
	 *             if trace file not found
	 */
	AROTraceData analyzeDirectory(List<BestPracticeType> requests, String traceDirectory, Profile profile, AnalysisFilter filter) throws IOException;

}
