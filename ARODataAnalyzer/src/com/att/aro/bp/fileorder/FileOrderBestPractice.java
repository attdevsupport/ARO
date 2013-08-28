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
package com.att.aro.bp.fileorder;

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
 * File order best practices - CSS files should be loaded before JS.
 */
public class FileOrderBestPractice implements BestPracticeDisplay {

	private FileOrderResultPanel resultPanel;

	@Override
	public String getOverviewTitle() {
		return Util.RB.getString("html.fileorder.title");
	}

	@Override
	public String getDetailTitle() {
		return Util.RB.getString("html.fileorder.detailedTitle");
	}

	@Override
	public boolean isSelfTest() {
		return false;
	}

	@Override
	public String getAboutText() {
		return Util.RB.getString("html.fileorder.desc");
	}

	@Override
	public URI getLearnMoreURI() {
		return URI.create(Util.RB.getString("html.fileorder.url"));
	}

	@Override
	public boolean isPass(TraceData.Analysis analysis) {
		return !analysis.getBestPractice().isFileOrderTestFailed();
	}

	@Override
	public String resultText(Analysis analysisData) {
		BestPractices bp = analysisData.getBestPractice();
		String key = isPass(analysisData) ? "html.fileorder.pass"
				: analysisData.getBestPractice().getFileOrderCount() > 1 ?"html.fileorder.pluresults" : "html.fileorder.singresults";
		return MessageFormat.format(Util.RB.getString(key),
				bp.getFileOrderCount());
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
				bp.getFileOrderCount()),
				Util.RB.getString("exportall.csvFileOrderCount"));
		result.add(bpe);
		return result;
	}

	@Override
	public JPanel getTestResults() {
		if (resultPanel == null) {
			resultPanel = new FileOrderResultPanel();
		}
		return resultPanel;
	}

}
