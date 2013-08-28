package com.att.aro.bp.displaynoneincss;

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
import com.att.aro.model.BestPractices;
import com.att.aro.model.TraceData;
import com.att.aro.model.TraceData.Analysis;
import com.att.aro.util.Util;

/**
 * 
 * Display:none Best Practice
 *
 */
public class DisplayNoneInCSSBestPractice implements BestPracticeDisplay {

	private DisplayNoneInCSSResultPanel resultPanel;

	@Override
	public String getOverviewTitle() {
		return Util.RB.getString("html.displaynoneincss.title");
	}

	@Override
	public String getDetailTitle() {
		return Util.RB.getString("html.displaynoneincss.detailedTitle");
	}

	@Override
	public boolean isSelfTest() {
		return false;
	}

	@Override
	public String getAboutText() {
		return Util.RB.getString("html.displaynoneincss.desc");
	}

	@Override
	public URI getLearnMoreURI() {
		return URI.create(Util.RB.getString("html.displaynoneincss.url"));
	}

	@Override
	public boolean isPass(TraceData.Analysis analysis) {
		return analysis.getDisplayNoneInCSSAnalysis().isTestPassed();
	}
	
	public String resultText(Analysis analysisData) {
		int numOfCSSFiles = analysisData.getBestPractice().getNumberOfCSSFilesWithDisplayNone();
		String key = isPass(analysisData) ? "html.displaynoneincss.pass" : "html.displaynoneincss.results";
		return MessageFormat.format(Util.RB.getString(key), numOfCSSFiles);
	}

	@Override
	public void performAction(HyperlinkEvent h,
			ApplicationResourceOptimizer parent) {
	}

	@Override
	public List<BestPracticeExport> getExportData(Analysis analysisData) {

		BestPractices bp = analysisData.getBestPractice();
		List<BestPracticeExport> result = new ArrayList<BestPracticeExport>(1);

		BestPracticeExport bpe;
		bpe = new BestPracticeExport(NumberFormat.getIntegerInstance().format(
				bp.getNumberOfCSSFilesWithDisplayNone()),
				Util.RB.getString("exportall.csvNumberOfFilesWithDisplayNone"));
		result.add(bpe);
		return result;
	}

	@Override
	public JPanel getTestResults() {
		if (resultPanel == null) {
			resultPanel = new DisplayNoneInCSSResultPanel();
		}
		return resultPanel;
	}
}
