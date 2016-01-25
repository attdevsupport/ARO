//package com.att.aro.bp.smallrequest;
//
//import java.net.URI;
//import java.text.MessageFormat;
//import java.text.NumberFormat;
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.swing.JPanel;
//import javax.swing.event.HyperlinkEvent;
//
//import com.att.aro.bp.BestPracticeDisplay;
//import com.att.aro.bp.BestPracticeExport;
//import com.att.aro.main.ApplicationResourceOptimizer;
//import com.att.aro.model.TraceData;
//import com.att.aro.model.TraceData.Analysis;
//import com.att.aro.util.Util;
//
//public class SmallRequestBestPractice implements BestPracticeDisplay {
//	private SmallRequestResultPanel resultPanel;
//
//	@Override
//	public String getOverviewTitle() {
//		return Util.RB.getString("smallrequest.title");
//	}
//
//	@Override
//	public String getDetailTitle() {
//		return Util.RB.getString("smallrequest.detailedTitle");
//	}
//
//	@Override
//	public boolean isSelfTest() {
//		return false;
//	}
//
//	@Override
//	public String getAboutText() {
//		return Util.RB.getString("smallrequest.desc");
//	}
//
//	@Override
//	public URI getLearnMoreURI() {
//		return URI.create(Util.RB.getString("smallrequest.url"));
//	}
//
//	@Override
//	public boolean isPass(TraceData.Analysis analysis) {
//		return analysis.getSmallRequestAnalysis().isTestPassed();
//	}
//
//	@Override
//	public String resultText(Analysis analysisData) {
//		int numOfFiles = analysisData.getBestPractice().getNumberOfSmallRequest();
//		String key = isPass(analysisData) ? "smallrequest.pass" : "smallrequest.results";
//		return MessageFormat.format(Util.RB.getString(key), numOfFiles);
//	}
//
//	@Override
//	public void performAction(HyperlinkEvent h, ApplicationResourceOptimizer parent) {
//	}
//
//	@Override
//	public List<BestPracticeExport> getExportData(Analysis analysisData) {
//
//		int numberOfFiles = analysisData.getBestPractice().getNumberOfSmallRequest();
//		BestPracticeExport bpe;
//		bpe = new BestPracticeExport(NumberFormat.getIntegerInstance().format(numberOfFiles), 
//				                     Util.RB.getString("exportall.csvNumberOfSmallRequest"));
//
//		List<BestPracticeExport> result = new ArrayList<BestPracticeExport>(1);
//		result.add(bpe);
//		return result;
//	}
//
//	@Override
//	public JPanel getTestResults() {
//		if (this.resultPanel == null) {
//			this.resultPanel = new SmallRequestResultPanel();
//		}
//		return this.resultPanel;
//	}
//}
