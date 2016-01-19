/**
 * 
 */
package com.att.aro.ui.view.statistics;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.UIManager;

import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.AROUIManager;
import com.att.aro.ui.commonui.TabPanelCommon;
import com.att.aro.ui.commonui.TabPanelCommonAttributes;
import com.att.aro.ui.commonui.TabPanelJPanel;
import com.att.aro.ui.view.menu.ApplicationSampling;
import com.att.aro.ui.view.statistics.model.ApplicationScoreDerived;

/**
 * @author Nathan F Syfrig
 *
 */
@Deprecated
public class TraceScorePanel extends TabPanelJPanel {
	private enum LabelKeys {
		appscore_title,
		appscore_subtitle_causes,
		appscore_causes_cacheControl,
		appscore_causes_connectionClosing,
		appscore_causes_tightlyGroupedConnection,
		appscore_causes_periodicTransfers,
		appscore_effects_contentExpiration,
		appscore_causes_causeSubTotal,
		appscore_subtitle_effects,
		appscore_effects_duplicateContent,
		appscore_causes_signalingOverhead,
		appscore_effects_averageRate,
		appscore_effects_energyConsumption,
		appscore_effect_effectSubTotal,
		appscore_subtitle_total,
		appscore_valueoutoftotal
	}
	private static final long serialVersionUID = 1L;
	private JPanel dataPanel;
	private final TabPanelCommon tabPanelCommon = new TabPanelCommon();

	public TraceScorePanel() {
		tabPanelCommon.initTabPanel(this);
//		add(getDataPanel(), BorderLayout.CENTER);
		add(layoutDataPanel(), BorderLayout.WEST);
	}

	/**
	 * Creates the JPanel containing the Date , Trace and Application details
	 * 
	 * @return the dataPanel
	 */
	@Override
	public JPanel layoutDataPanel() {

		dataPanel = tabPanelCommon.initDataPanel(UIManager.getColor(
				AROUIManager.PAGE_BACKGROUND_KEY));

		Insets insets = new Insets(2, 2, 2, 2);
		Insets bottomBlankLineInsets = new Insets(2, 2, 8, 2);
		Insets topBlankLineInsets = new Insets(8, 2, 2, 2);
		TabPanelCommonAttributes attributes = tabPanelCommon.addLabelLine(
			new TabPanelCommonAttributes.Builder()
				.enumKey(LabelKeys.appscore_title)
				.contentsWidth(1)
				.insets(insets)
				.insetsOverride(bottomBlankLineInsets)
				.header()
	.		build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.appscore_subtitle_causes)
				.subheader()
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.appscore_causes_cacheControl)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.appscore_causes_connectionClosing)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.appscore_causes_tightlyGroupedConnection)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.appscore_causes_periodicTransfers)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.appscore_effects_contentExpiration)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.appscore_causes_causeSubTotal)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.appscore_subtitle_effects)
				.subheader()
				.insetsOverride(topBlankLineInsets)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.appscore_effects_duplicateContent)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.appscore_causes_signalingOverhead)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.appscore_effects_averageRate)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.appscore_effects_energyConsumption)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.appscore_effect_effectSubTotal)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.appscore_subtitle_total)
				.subheader()
				.insetsOverride(topBlankLineInsets)
			.build());

		return dataPanel;
	}


	private void setTraceScoreDataEntry(LabelKeys key, int score, String outOfString) {
		String[] valueTotal = new String[2];

		valueTotal[0] = String.format("%1$4d", score);
		valueTotal[1] = outOfString;
		tabPanelCommon.setText(key,
				tabPanelCommon.getText(LabelKeys.appscore_valueoutoftotal, valueTotal));
	}

	@Override
	public void refresh(AROTraceData model) {
		ApplicationScoreDerived derived =
				new ApplicationScoreDerived(model, ApplicationSampling.getInstance());
		model.getAnalyzerResult().getBurstcollectionAnalysisData();

		setTraceScoreDataEntry(LabelKeys.appscore_causes_cacheControl,
				derived.getCacheHeaderControlScore(), "75");
		setTraceScoreDataEntry(LabelKeys.appscore_causes_connectionClosing,
				derived.getConnectionClosingScore(), "75");
		setTraceScoreDataEntry(LabelKeys.appscore_causes_tightlyGroupedConnection,
				derived.getTightlyGroupedConnectionScore(), "150");
		setTraceScoreDataEntry(LabelKeys.appscore_causes_periodicTransfers,
				derived.getPeriodicTransferScore(), "150");
		setTraceScoreDataEntry(LabelKeys.appscore_effects_contentExpiration,
				derived.getConnectionExpirationScore(), "50");
		setTraceScoreDataEntry(LabelKeys.appscore_causes_causeSubTotal,
				derived.getCausesScore(), "500");

		setTraceScoreDataEntry(LabelKeys.appscore_effects_duplicateContent,
				derived.getDuplicateContentScore(), "500");
		// TODO:  Understand why numbers differ from old product and fix where necessary!
		setTraceScoreDataEntry(LabelKeys.appscore_causes_signalingOverhead,
				derived.getSignalingOverheadScore(), "500");
		setTraceScoreDataEntry(LabelKeys.appscore_effects_averageRate,
				derived.getAverageRateScore(), "62.5");
		setTraceScoreDataEntry(LabelKeys.appscore_effects_energyConsumption,
				derived.getEnergyEfficiencyScore(), "187.5");
		setTraceScoreDataEntry(LabelKeys.appscore_effect_effectSubTotal,
				derived.getEffectScore(), "500");
		setTraceScoreDataEntry(LabelKeys.appscore_subtitle_total,
				derived.getTotalApplicationScore(), "1000");
	}
}
