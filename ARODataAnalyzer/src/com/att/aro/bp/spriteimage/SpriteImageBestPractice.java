package com.att.aro.bp.spriteimage;

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
import com.att.aro.model.TraceData;
import com.att.aro.model.TraceData.Analysis;
import com.att.aro.util.Util;

public class SpriteImageBestPractice extends BestPracticeDisplay {
	private SpriteImageResultPanel resultPanel;

	@Override
	public String getOverviewTitle() {
		return Util.RB.getString("spriteimages.title");
	}

	@Override
	public String getDetailTitle() {
		return Util.RB.getString("spriteimages.detailedTitle");
	}

	@Override
	public boolean isSelfTest() {
		return false;
	}

	@Override
	public String getAboutText() {
		return Util.RB.getString("spriteimages.desc");
	}

	@Override
	public URI getLearnMoreURI() {
		return URI.create(Util.RB.getString("spriteimages.url"));
	}

	@Override
	public boolean isPass(TraceData.Analysis analysis) {
		return analysis.getSpriteImageAnalysis().isTestPassed();
	}

	@Override
	public String resultText(Analysis analysisData) {
		int numOfFiles = analysisData.getBestPractice().getNumberOfFilesToBeSprited();
		String key = isPass(analysisData) ? "spriteimages.pass" : "spriteimages.results";
		return MessageFormat.format(Util.RB.getString(key), numOfFiles);
	}

	@Override
	public void performAction(HyperlinkEvent h, ApplicationResourceOptimizer parent) {
	}

	@Override
	public List<BestPracticeExport> getExportData(Analysis analysisData) {

		int numberOfFiles = analysisData.getBestPractice().getNumberOfFilesToBeSprited();
		BestPracticeExport bpe;
		bpe = new BestPracticeExport(NumberFormat.getIntegerInstance().format(numberOfFiles), 
				                     Util.RB.getString("exportall.csvNumberOfSpriteFiles"));

		List<BestPracticeExport> result = new ArrayList<BestPracticeExport>(1);
		result.add(bpe);
		return result;
	}

	@Override
	public JPanel getTestResults() {
		if (this.resultPanel == null) {
			this.resultPanel = new SpriteImageResultPanel();
		}
		return this.resultPanel;
	}
}
