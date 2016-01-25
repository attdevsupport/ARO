/**
 * Copyright 2016 AT&T
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
package com.att.aro.ui.view.statistics;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.att.aro.core.packetanalysis.pojo.AbstractTraceResult;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.packetanalysis.pojo.TraceResultType;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.core.util.Util;
import com.att.aro.ui.commonui.AROUIManager;
import com.att.aro.ui.commonui.TabPanelCommon;
import com.att.aro.ui.commonui.TabPanelCommonAttributes;
import com.att.aro.ui.commonui.TabPanelJPanel;

public class DateTraceAppDetailPanel extends TabPanelJPanel {
	private enum LabelKeys {
		bestPractices_sideTitle,
		bestPractices_date,
		bestPractices_trace,
		bestPractices_application,
		bestPractices_applicationversion,
		bestPractices_devicemodel,
		bestPractices_os_version,
		bestPractices_networktype,
		bestPractices_profile
	}

	private static final long serialVersionUID = 1L;
	private final TabPanelCommon tabPanelCommon = new TabPanelCommon();
	
	
	/**
	 * Initializes a new instance of the DateTraceAppDetailPanel class.
	 */
	public DateTraceAppDetailPanel() {
		tabPanelCommon.initTabPanel(this);
		add(getTitlePanel(), BorderLayout.EAST);
		add(layoutDataPanel(), BorderLayout.WEST);
	}

	public JPanel getTitlePanel() {
		JPanel titlePanel = new JPanel();
		titlePanel.setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		GridBagConstraints sideConstraints = TabPanelCommonAttributes.getDefaultLabelConstraints();
		sideConstraints.gridx = 1;
		sideConstraints.anchor = GridBagConstraints.NORTHWEST;
		sideConstraints.weightx = 1.0;
		JLabel titleLabel = new JLabel(tabPanelCommon.getText(LabelKeys.bestPractices_sideTitle),
				JLabel.RIGHT);
		titlePanel.add(titleLabel, BorderLayout.EAST);
		return titlePanel;
	}

	/**
	 * Creates the JPanel containing the Date , Trace and Application details
	 * 
	 * @return the dataPanel
	 */
	@Override
	public JPanel layoutDataPanel() {

		TabPanelCommonAttributes attributes = tabPanelCommon.addLabelLine(
			new TabPanelCommonAttributes.Builder()
				.enumKey(LabelKeys.bestPractices_date)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.bestPractices_trace)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
			.copyNextLine(attributes)
			.enumKey(LabelKeys.bestPractices_application)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.bestPractices_applicationversion)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.bestPractices_devicemodel)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.bestPractices_os_version)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.bestPractices_networktype)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.bestPractices_profile)
			.build());

		return tabPanelCommon.getTabPanel();
	}

	private String getAppVersion(AbstractTraceResult traceResults, String appName) {
		StringBuilder appVersion = new StringBuilder();
		if (traceResults.getTraceResultType() == TraceResultType.TRACE_DIRECTORY) {
			Map<String, String> appVersionMap =
					((TraceDirectoryResult) traceResults).getAppVersionMap();
			if (appVersionMap != null &&
					appVersionMap.get(appName) != null) {
				appVersion.append(" :" + appVersionMap.get(appName));
			}
		}
		return appVersion.toString();
	}
	
	private void refreshCommon(AbstractTraceResult traceResults,Set<String> appNames ) {
		tabPanelCommon.setText(LabelKeys.bestPractices_date, String.format("%1$tb %1$te, %1$tY %1$tr", traceResults.getTraceDateTime()));

		String traceDirectory = traceResults.getTraceDirectory();
		int lastIndexOf = traceDirectory.lastIndexOf(Util.FILE_SEPARATOR);
		tabPanelCommon.setText(LabelKeys.bestPractices_trace, lastIndexOf > -1 ? traceDirectory.substring((lastIndexOf + 1)) : traceDirectory);

		StringBuilder appList = new StringBuilder();
//		Set<String> allAppNames = traceResults.getAllAppNames(); //wrong pojo
		boolean firstTimeFlag = true;
		appList.append("<html>");
		
		for (String appName : appNames) {
			if (!firstTimeFlag) {
				appList.append("<br/>");
			}
			firstTimeFlag = false;
			appList.append(appName + getAppVersion(traceResults, appName));
		}
		appList.append("</html>");
		tabPanelCommon.setText(LabelKeys.bestPractices_application, appList.toString());
	}
	
	private void refreshTraceDirectory(TraceDirectoryResult traceDirectoryResults) {
		tabPanelCommon.setText(LabelKeys.bestPractices_applicationversion,
				traceDirectoryResults.getCollectorVersion());
		tabPanelCommon.setText(LabelKeys.bestPractices_devicemodel,
				traceDirectoryResults.getDeviceMake() + " / " +
					traceDirectoryResults.getDeviceModel());
		tabPanelCommon.setText(LabelKeys.bestPractices_os_version,
				traceDirectoryResults.getOsVersion());
		tabPanelCommon.setText(LabelKeys.bestPractices_networktype,
				traceDirectoryResults.getNetworkTypesList());
	}

	@Override
	public void refresh(AROTraceData model) {
		PacketAnalyzerResult analyzerResults = model.getAnalyzerResult();
		Set<String> appNames =  analyzerResults.getStatistic().getAppName();
		AbstractTraceResult traceResults = analyzerResults.getTraceresult();
		if (traceResults != null&&!appNames.isEmpty()) {
			refreshCommon(traceResults,appNames);
			if (traceResults.getTraceResultType() == TraceResultType.TRACE_DIRECTORY) {
				refreshTraceDirectory((TraceDirectoryResult) traceResults);
			}
			// TODO:  Ensure we get profile name populated from the trace
			String profileName = analyzerResults.getProfile().getName() != null ?
					analyzerResults.getProfile().getName() : "TBD";
			tabPanelCommon.setText(LabelKeys.bestPractices_profile, profileName);
		}
	}
}
