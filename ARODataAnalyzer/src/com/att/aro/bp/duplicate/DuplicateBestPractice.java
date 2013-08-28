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
package com.att.aro.bp.duplicate;

import java.net.URI;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;

import com.att.aro.bp.BestPracticeDisplay;
import com.att.aro.bp.BestPracticeExport;
import com.att.aro.main.ApplicationResourceOptimizer;
import com.att.aro.main.ResourceBundleManager;
import com.att.aro.model.BestPractices;
import com.att.aro.model.TraceData;
import com.att.aro.model.TraceData.Analysis;

/**
 * Represents Duplicate content Best Practice.
 */
public class DuplicateBestPractice implements BestPracticeDisplay {


	private static final int DUPLICATE_CONTENT_DENOMINATOR = 1048576;
	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	
	private DuplicateResultPanel resultPanel;

	@Override
	public String getOverviewTitle() {
		return rb.getString("caching.duplicateContent.title");
	}

	@Override
	public String getDetailTitle() {
		return rb.getString("caching.duplicateContent.detailedTitle");
	}

	@Override
	public boolean isSelfTest() {
		return false;
	}

	@Override
	public String getAboutText() {
		return rb.getString("caching.duplicateContent.desc");
	}

	@Override
	public URI getLearnMoreURI() {
		return URI.create(rb.getString("caching.duplicateContent.url"));
	}

	@Override
	public boolean isPass(TraceData.Analysis analysis) {
		return analysis.getBestPractice().getDuplicateContent();
	}

	@Override
	public String resultText(Analysis analysisData) {
		if (isPass(analysisData)) {
			return rb.getString("caching.duplicateContent.pass");
		} else {
			BestPractices bp = analysisData.getBestPractice();
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(1);
			NumberFormat nf2 = NumberFormat.getInstance();
			nf2.setMaximumFractionDigits(3);

			return MessageFormat.format(
					rb.getString("caching.duplicateContent.results"),
					nf.format(bp.getDuplicateContentBytesRatio() * 100.0),
					bp.getDuplicateContentsize(),
					nf2.format(((double) bp.getDuplicateContentBytes())
							/ DUPLICATE_CONTENT_DENOMINATOR),
					nf2.format(((double) bp.getTotalContentBytes())
							/ DUPLICATE_CONTENT_DENOMINATOR));
		}
	}

	@Override
	public void performAction(HyperlinkEvent h, ApplicationResourceOptimizer parent) {
		parent.displaySimpleTab();
	}

	@Override
	public List<BestPracticeExport> getExportData(Analysis analysisData) {
		BestPractices bp = analysisData.getBestPractice();
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		NumberFormat nf2 = NumberFormat.getInstance();
		nf2.setMaximumFractionDigits(3);

		List<BestPracticeExport> result = new ArrayList<BestPracticeExport>(3);
		result.add(new BestPracticeExport(nf.format(bp
				.getDuplicateContentBytesRatio() * 100.0), rb
				.getString("exportall.csvPct")));
		result.add(new BestPracticeExport(String.valueOf(bp
				.getDuplicateContentsize()), rb
				.getString("exportall.csvFiles")));
		result.add(new BestPracticeExport(nf2.format(((double) bp
				.getDuplicateContentBytes())
				/ DUPLICATE_CONTENT_DENOMINATOR), rb
				.getString("statics.csvUnits.mbytes")));
		return result;
	}

	@Override
	public JPanel getTestResults() {
		if (this.resultPanel == null) {
			this.resultPanel = new DuplicateResultPanel();
		}
		return this.resultPanel;
	}
	


}
