package com.att.aro.bp.flash;

import java.net.URI;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;

import com.att.aro.bp.BestPracticeDisplay;
import com.att.aro.bp.BestPracticeExport;
import com.att.aro.main.ApplicationResourceOptimizer;
import com.att.aro.model.HttpRequestResponseInfo;
import com.att.aro.model.TraceData;
import com.att.aro.model.TraceData.Analysis;
import com.att.aro.util.Util;

public class FlashBestPractice implements BestPracticeDisplay {
	@Override
	public String getOverviewTitle() {
		return Util.RB.getString("flash.title");
	}

	@Override
	public String getDetailTitle() {
		return Util.RB.getString("flash.detailedTitle");
	}

	@Override
	public boolean isSelfTest() {
		return false;
	}

	@Override
	public String getAboutText() {
		return Util.RB.getString("flash.desc");
	}

	@Override
	public URI getLearnMoreURI() {
		return URI.create(Util.RB.getString("flash.url"));
	}

	@Override
	public boolean isPass(TraceData.Analysis analysis) {
		return analysis.getFlashAnalysis().isTestPassed();
	}

	@Override
	public String resultText(Analysis analysisData) {
		int numOfFiles = analysisData.getBestPractice().getNumberOfFlashFiles();
		String key = isPass(analysisData) ? "flash.pass" : "flash.results";
		return MessageFormat.format(Util.RB.getString(key), numOfFiles);
	}

	@Override
	public void performAction(HyperlinkEvent h, ApplicationResourceOptimizer parent) {
		parent.displayDiagnosticTab();
		HttpRequestResponseInfo FlashReqResInfo;
		FlashReqResInfo = parent.getAnalysisData().getFlashAnalysis().getFirstFlashReqResInfo();
		parent.getAroAdvancedTab().setHighlightedRequestResponse(FlashReqResInfo);
	}

	@Override
	public List<BestPracticeExport> getExportData(Analysis analysisData) {

		int numberOfFiles = analysisData.getBestPractice().getNumberOfFlashFiles();
		BestPracticeExport bpe;
		bpe = new BestPracticeExport(NumberFormat.getIntegerInstance().format(numberOfFiles),
				                     Util.RB.getString("exportall.csvNumberOfFlashFiles"));

		List<BestPracticeExport> result = new ArrayList<BestPracticeExport>(1);
		result.add(bpe);

		return result;
	}

	@Override
	public JPanel getTestResults() {
		return null;
	}
}
