/*
 *  Copyright 2015 AT&T
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
package com.att.aro.core.pojo;

import java.util.List;

import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;

/**
 * AROTraceData contains
 * <ul>
 *   <li><b>analyzerResult</b> the results of an analyzed trace<br></li>
 *   <li><b>bestPracticeResults</b> a List&lt;BestPracticeType&gt; <br></li>
 *   <br>
 *   <li><b>success</b> the success of the analysis <br></li>
 *   <li><b>error</b> the ErrorCode if success is false <br></li>
 * </ul>
 *
 */
public class AROTraceData {
	private boolean success = false;
	private ErrorCode error = null;
	private List<AbstractBestPracticeResult> bestPracticeResults;
	private PacketAnalyzerResult analyzerResult;

	/**
	 * Returns the PacketAnalyzerResult, an object that contains the analyzed trace.
	 * 
	 * @return analyzerResult a PacketAnalyzerResult object
	 */
	public PacketAnalyzerResult getAnalyzerResult() {
		return analyzerResult;
	}

	/**
	 * Set the PacketAnalyzerResult, an object that contains the analyzed trace.
	 * 
	 * @param analyzerResult a PacketAnalyzerResult object
	 */
	public void setAnalyzerResult(PacketAnalyzerResult analyzerResult) {
		this.analyzerResult = analyzerResult;
	}

	/**
	 * Returns the result of Trace data analysis.
	 * True if analysis was successful otherwise false. If false, there should
	 * be error message.
	 * 
	 * @return true if successful or false if failure
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Record the success or failure of Trace data analysis.
	 * @param success true if successful or false if failure
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}

	/**
	 * ErrorCode if Trace data analysis regarding failure of Trace data analysis.
	 * ErrorCode is null if there were no errors. see isSuccess()
	 * 
	 * @return instance of ErrorCode or null if no errors
	 */
	public ErrorCode getError() {
		return error;
	}

	/**
	 * Set the ErrorCode if there was a failure during Trace data analysis.
	 * 
	 * @param instance of ErrorCode
	 */
	public void setError(ErrorCode error) {
		this.error = error;
	}

	/**
	 * Retrieve the Best Practice Results List, an ArrayList&lt;AbstractBestPracticeResult&gt;
	 * 
	 * @return List&lt;AbstractBestPracticeResult&gt;
	 */
	public List<AbstractBestPracticeResult> getBestPracticeResults() {
		return bestPracticeResults;
	}

	/**
	 * Set the Best Practice Results List, an ArrayList&lt;AbstractBestPracticeResult&gt;
	 * @param bestPracticeResults an ArrayList&lt;AbstractBestPracticeResult&gt;
	 */
	public void setBestPracticeResults(List<AbstractBestPracticeResult> bestPracticeResults) {
		this.bestPracticeResults = bestPracticeResults;
	}

}
