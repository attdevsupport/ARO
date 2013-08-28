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
package com.att.aro.bp.http4xx5xxrespcodes;

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;

import com.att.aro.bp.BestPracticeDisplay;
import com.att.aro.bp.BestPracticeExport;
import com.att.aro.main.ApplicationResourceOptimizer;
import com.att.aro.model.BestPractices;
import com.att.aro.model.HttpRequestResponseInfo;
import com.att.aro.model.TraceData;
import com.att.aro.model.TraceData.Analysis;
import com.att.aro.util.Util;

/**
 * Implementation of HTTP error codes abstract This abstract class should be
 * extended for a specific error code best practice (i.e. 3xx/4xx/5xx)
 * 
 * @author ns5254
 * 
 */
public abstract class HttpRspCdBestPractice implements BestPracticeDisplay {
	private String rbPrefix;
	private Http4xx5xxStatusResponseCodesResultPanel resultPanel;
	
	public HttpRspCdBestPractice(String rbKey) {
		rbPrefix = "connections." + rbKey;
	}

	/**
	 * Abstract getter method to be overridden by implementing best practice
	 * class to get specific map for corresponding rsp codes
	 * 
	 * @param bp
	 * @return Map<Integer, Integer>
	 */
	abstract protected Map<Integer, Integer> getHttpRspCdCountMap(
			BestPractices bp);

	/**
	 * Abstract getter method to be overridden by implementing best practice
	 * class to get specific map for corresponding rsp codes
	 * 
	 * @param bp
	 * @return Map<Integer, HttpRequestResponseInfo>
	 */
	abstract protected Map<Integer, HttpRequestResponseInfo> getFirstRespMap(
			BestPractices bp);

	@Override
	public String getOverviewTitle() {
		StringBuilder rbStrBuilder = new StringBuilder(rbPrefix);
		rbStrBuilder.append(".title");
		return Util.RB.getString(rbStrBuilder.toString());
	}

	@Override
	public String getDetailTitle() {
		StringBuilder rbStrBuilder = new StringBuilder(rbPrefix);
		rbStrBuilder.append(".detailedTitle");
		return Util.RB.getString(rbStrBuilder.toString());
	}

	@Override
	public boolean isSelfTest() {
		return false;
	}

	@Override
	public String getAboutText() {
		StringBuilder rbStrBuilder = new StringBuilder(rbPrefix);
		rbStrBuilder.append(".desc");
		return Util.RB.getString(rbStrBuilder.toString());
	}

	@Override
	public URI getLearnMoreURI() {
		StringBuilder rbStrBuilder = new StringBuilder(rbPrefix);
		rbStrBuilder.append(".url");
		return URI.create(Util.RB.getString("connections.http4xx5xx.url"));
	}

	@Override
	public boolean isPass(TraceData.Analysis analysis) {
		Map<Integer, Integer> map = getHttpRspCdCountMap(analysis
				.getBestPractice());
		if (map != null) {
			return map.isEmpty();
		} else {
			return true;
		}
	}

	@Override
	public String resultText(Analysis analysisData) {
		Map<Integer, Integer> map = getHttpRspCdCountMap(analysisData
				.getBestPractice());
		Iterator<Map.Entry<Integer, Integer>> i = map.entrySet().iterator();
		StringBuilder rbStrBuilder = new StringBuilder(rbPrefix);
		if (i.hasNext()) {
			Map.Entry<Integer, Integer> entry = i.next();
			String message = formatError(entry);
			if (i.hasNext()) {
				entry = i.next();
				rbStrBuilder.append(".errorList");
				while (i.hasNext()) {
					message = MessageFormat.format(
							Util.RB.getString(rbStrBuilder.toString()),
							message, formatError(entry));
					entry = i.next();
				}
				rbStrBuilder = new StringBuilder(rbPrefix);
				rbStrBuilder.append(".errorListEnd");
				message = MessageFormat.format(
						Util.RB.getString(rbStrBuilder.toString()), message,
						formatError(entry));
			}

			rbStrBuilder = new StringBuilder(rbPrefix);
			rbStrBuilder.append(".results");
			return MessageFormat.format(
					Util.RB.getString(rbStrBuilder.toString()), message);
		} else {
			rbStrBuilder.append(".pass");
			return Util.RB.getString(rbStrBuilder.toString());
		}
	}

	@Override
	public void performAction(HyperlinkEvent h,
			ApplicationResourceOptimizer parent) {
		try {

			// Find a response with the selected status code
			int status = Integer.parseInt(h.getDescription());
			parent.displayDiagnosticTab();
			parent.getAroAdvancedTab().setHighlightedRequestResponse(
					getFirstRespMap(parent.getAnalysisData().getBestPractice())
							.get(status));
		} catch (NumberFormatException e) {
			// Ignore
		}
	}

	@Override
	public List<BestPracticeExport> getExportData(Analysis analysisData) {
		Map<Integer, Integer> map = analysisData.getBestPractice()
				.getHttpRedirectCounts();
		List<BestPracticeExport> result = new ArrayList<BestPracticeExport>(
				map.size());
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			result.add(new BestPracticeExport(String.valueOf(entry.getValue()),
					MessageFormat.format(
							Util.RB.getString("exportall.csvHttpError"),
							entry.getKey())));
		}
		return result;
	}

	@Override
	public JPanel getTestResults() {
		if ((rbPrefix.compareToIgnoreCase("connections.http4xx5xx") == 0)) {
			if(this.resultPanel == null) {
				this.resultPanel = new Http4xx5xxStatusResponseCodesResultPanel();
			}
			this.resultPanel.setVisible(false);
			return this.resultPanel;
		}
		return null;
	}
	
	/**
	 * Format best practice failure message
	 * 
	 * @param entry
	 * @return Formatted message
	 */
	private String formatError(Map.Entry<Integer, Integer> entry) {
		int count = entry.getValue();
		StringBuilder rbStrBuilder = new StringBuilder(rbPrefix);
		if (count > 1) {
			rbStrBuilder.append(".errorPlural");
			return MessageFormat.format(
					Util.RB.getString(rbStrBuilder.toString()), count,
					entry.getKey());
		} else {
			rbStrBuilder.append(".errorSingular");
			return MessageFormat.format(
					Util.RB.getString(rbStrBuilder.toString()), entry.getKey());
		}
	}
}

