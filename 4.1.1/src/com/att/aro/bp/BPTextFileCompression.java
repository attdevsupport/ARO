/*
 *  Copyright 2013 AT&T
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
package com.att.aro.bp;

import java.net.URI;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;

import com.att.aro.main.ApplicationResourceOptimizer;
import com.att.aro.main.TextFileCompressionResultPanel;
import com.att.aro.model.BestPractices;
import com.att.aro.model.TextFileCompressionAnalysis;
import com.att.aro.model.TextFileCompressionAnalysis.TextCompressionAnalysisResult;
import com.att.aro.model.TraceData;
import com.att.aro.model.TraceData.Analysis;
import com.att.aro.util.Util;

/**
 * Text File Compression best practice.
 */
public class BPTextFileCompression extends BestPracticeDisplay {
	
	private static final String EXPORTALL_CSV_TEXT_FILE_COMPRESSION = "exportall.csvTextFileCompression";
	private static final String EXPORTALL_CSV_TEXT_FILE_COMPRESSION_KB = "exportall.csvTextFileCompressionKb";
	private static final String FILE_COMPRESSION_PASS_TEXT = "textFileCompression.pass";
	private static final String FILE_COMPRESSION_FAIL_WARNING_TEXT = "textFileCompression.results";
	public static final String TEXT_FILE_COMPRESSION_DETAILED_TITLE = "textFileCompression.detailedTitle";
	public static final String TEXT_FILE_COMPRESSION_OVERVIEW_TITLE = "textFileCompression.title";
	private TextFileCompressionResultPanel resultPanel;

	@Override
	public String getOverviewTitle() {
		return Util.RB.getString(TEXT_FILE_COMPRESSION_OVERVIEW_TITLE);
	}

	@Override
	public String getDetailTitle() {
		return Util.RB.getString(TEXT_FILE_COMPRESSION_DETAILED_TITLE);
	}

	@Override
	public boolean isSelfTest() {
		return false;
	}

	@Override
	public String getAboutText() {
		return Util.RB.getString("textFileCompression.desc");
	}

	@Override
	public URI getLearnMoreURI() {
		return URI.create(Util.RB.getString("textFileCompression.url"));
	}

	@Override
	public boolean isPass(TraceData.Analysis analysis) {
		return analysis.getBestPractice().getTextCompressionAnalysisResult() == TextCompressionAnalysisResult.PASS;
	}
	
	@Override
	public boolean isWarning(TraceData.Analysis analysis){
		return analysis.getBestPractice().getTextCompressionAnalysisResult() == TextCompressionAnalysisResult.WARNING;
	}

	@Override
	public String resultText(Analysis analysisData) {
		BestPractices bp = analysisData.getBestPractice();
		if (isPass(analysisData)){
			
			return MessageFormat.format(Util.RB.getString(FILE_COMPRESSION_PASS_TEXT),
   				    TextFileCompressionAnalysis.FILE_SIZE_THRESHOLD_BYTES,
   				    TextFileCompressionAnalysis.FILE_SIZE_THRESHOLD_BYTES);
		}
		
		//warning or fail has same message format
		return MessageFormat.format(Util.RB.getString(FILE_COMPRESSION_FAIL_WARNING_TEXT),
				   				    bp.getTotalUncompressedSizeKB(),
				   				    TextFileCompressionAnalysis.FILE_SIZE_THRESHOLD_BYTES);
	}

	@Override
	public void performAction(HyperlinkEvent h, ApplicationResourceOptimizer parent) {
	}

	@Override
	public List<BestPracticeExport> getExportData(Analysis analysisData) {
		
		BestPractices bp = analysisData.getBestPractice();
		List<BestPracticeExport> result = new ArrayList<BestPracticeExport>(1);
		
		BestPracticeExport bpe;
		String compressionFileCountDesc = MessageFormat.format(Util.RB.getString(EXPORTALL_CSV_TEXT_FILE_COMPRESSION),
				    TextFileCompressionAnalysis.FILE_SIZE_THRESHOLD_BYTES);
		
		final int noOfUncompressedFiles = analysisData.getTextFileCompressionAnalysis().getNoOfUncompressedFiles();
		bpe = new BestPracticeExport(String.valueOf(noOfUncompressedFiles), compressionFileCountDesc);
		result.add(bpe);
		
		bpe = new BestPracticeExport(NumberFormat.getIntegerInstance().format(bp.getTotalUncompressedSizeKB()),
                Util.RB.getString(EXPORTALL_CSV_TEXT_FILE_COMPRESSION_KB));
		result.add(bpe);
		
		return result;
	}

	@Override
	public JPanel getTestResults() {
		if (resultPanel == null) {
			resultPanel = new TextFileCompressionResultPanel();
		}
		return resultPanel;
	}

}
