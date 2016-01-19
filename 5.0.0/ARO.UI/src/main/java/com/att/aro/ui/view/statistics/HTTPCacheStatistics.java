package com.att.aro.ui.view.statistics;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.UIManager;

import com.att.aro.core.packetanalysis.pojo.CacheAnalysis;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.packetanalysis.pojo.Statistic;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.AROUIManager;
import com.att.aro.ui.commonui.TabPanelCommon;
import com.att.aro.ui.commonui.TabPanelCommonAttributes;
import com.att.aro.ui.commonui.TabPanelJPanel;
import com.att.aro.ui.view.statistics.model.CacheAnalysisDerived;

public class HTTPCacheStatistics extends TabPanelJPanel {
	private static final long serialVersionUID = 1L;

	private enum LabelKeys {
		cache_title,
		cache_pctResponses,
		cache_pctBytes,
		cache_cacheableSection,
		cache_cacheable,
		cache_nonCachable,
		cache_simulationSection,
		cache_acceptableBehavior,
		cache_cacheMiss,
		cache_notCacheable,
		cache_cacheHitExpiredDup304,
		cache_cacheHitRespChanged,
		cache_duplicateFileDownload,
		cache_cacheHitNotExpiredDup,
		cache_cacheHitExpiredClientDup,
		cache_cacheHitExpiredServerDup,
		cache_duplicateStreaming,
		cache_partialHitNotExpiredDup,
		cache_partialHitExpiredClientDup,
		cache_partialHitExpiredServerDup,
		cache_duplicateAnalysis,
		cache_notExpired,
		cache_notExpiredHeur,
		cache_expired,
		cache_expiredHeur
	}

	private JPanel dataPanel;
	private final TabPanelCommon tabPanelCommon = new TabPanelCommon(true);

	public HTTPCacheStatistics() {
		tabPanelCommon.initTabPanel(this);
		add(layoutDataPanel(), BorderLayout.WEST);
	}

	/**
	 * Creates the JPanel containing the HTTP Cache Statistics (part of Statistics page)
	 * 
	 * @return the dataPanel
	 */
	@Override
	public JPanel layoutDataPanel() {

		dataPanel = tabPanelCommon.initDataPanel(UIManager.getColor(
				AROUIManager.PAGE_BACKGROUND_KEY));
//		testPanel(dataPanel);

		Insets bottomBlankLineInsets = new Insets(2, 2, 8, 2);
		Insets contentsInsets = new Insets(2, 20, 2, 2);
		Insets contentsBlankLineInsets = new Insets(2, 20, 8, 2);
		TabPanelCommonAttributes attributes = tabPanelCommon.addLabelLine(
			new TabPanelCommonAttributes.Builder()
				.enumKey(LabelKeys.cache_title)
				.insetsOverride(bottomBlankLineInsets)
				.header()
				.contents(tabPanelCommon.getText(LabelKeys.cache_pctResponses))
				.contents2(tabPanelCommon.getText(LabelKeys.cache_pctBytes))
	.		build());
		GridBagConstraints labelConstraints =
				TabPanelCommonAttributes.getDefaultLabelConstraints(
						attributes.getGridy(), attributes.getInsets());
		labelConstraints.anchor = GridBagConstraints.EAST;
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_cacheableSection)
				.labelConstraints(labelConstraints)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_cacheable)
				.contentsInsets(contentsInsets)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_nonCachable)
				.insetsOverride(contentsBlankLineInsets)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_simulationSection)
				.insetsOverride(contentsBlankLineInsets)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_acceptableBehavior)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_cacheMiss)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_notCacheable)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_cacheHitExpiredDup304)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_cacheHitRespChanged)
				.insetsOverride(contentsBlankLineInsets)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_duplicateFileDownload)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_cacheHitNotExpiredDup)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_cacheHitExpiredClientDup)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_cacheHitExpiredServerDup)
				.insetsOverride(contentsBlankLineInsets)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_duplicateStreaming)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_partialHitNotExpiredDup)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_partialHitExpiredClientDup)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_partialHitExpiredServerDup)
				.insetsOverride(contentsBlankLineInsets)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_duplicateAnalysis)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_notExpired)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_notExpiredHeur)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_expired)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.cache_expiredHeur)
			.build());

		return dataPanel;
	}

	private void refreshLine(LabelKeys key, double valueResponse, double valueBytes) {
		String formatString = "%,1.1f";
		tabPanelCommon.setText(key, String.format(formatString, valueResponse));
		tabPanelCommon.setText2(key, String.format(formatString, valueBytes));
	}

	@Override
	public void refresh(AROTraceData model) {
		PacketAnalyzerResult analyzerResult = model.getAnalyzerResult();
		CacheAnalysis cacheAnalysis = analyzerResult.getCacheAnalysis();
		Statistic statistic = analyzerResult.getStatistic();
		List<Session> sessions = analyzerResult.getSessionlist();
		if (cacheAnalysis != null && statistic != null && sessions != null) {
			CacheAnalysisDerived cacheAnalysisDerived =
					new CacheAnalysisDerived(cacheAnalysis, statistic, sessions);
			refreshLine(LabelKeys.cache_cacheable,
					cacheAnalysisDerived.getPctCacheableResponses(),
					cacheAnalysisDerived.getPctCacheableBytes());

			refreshLine(LabelKeys.cache_nonCachable,
					cacheAnalysisDerived.getPctNonCacheableResponses(),
					cacheAnalysisDerived.getPctNonCacheableBytes());

			refreshLine(LabelKeys.cache_cacheMiss,
					cacheAnalysisDerived.getPctCacheMissResponses(),
					cacheAnalysisDerived.getPctCacheMissBytes());

			refreshLine(LabelKeys.cache_notCacheable,
					cacheAnalysisDerived.getPctNotCacheableResponses(),
					cacheAnalysisDerived.getPctNotCacheableBytes());

			refreshLine(LabelKeys.cache_cacheHitExpiredDup304,
					cacheAnalysisDerived.getPctExpiredResponses(),
					cacheAnalysisDerived.getPctExpiredBytes());

			refreshLine(LabelKeys.cache_cacheHitRespChanged,
					cacheAnalysisDerived.getPctNotExpiredResponses(),
					cacheAnalysisDerived.getPctNotExpiredBytes());

			refreshLine(LabelKeys.cache_cacheHitNotExpiredDup,
					cacheAnalysisDerived.getPctCacheMissResponses(),
					cacheAnalysisDerived.getPctCacheMissBytes());

			refreshLine(LabelKeys.cache_cacheHitExpiredClientDup,
					cacheAnalysisDerived.getPctNotCacheableResponses(),
					cacheAnalysisDerived.getPctNotCacheableBytes());

			refreshLine(LabelKeys.cache_cacheHitExpiredServerDup,
					cacheAnalysisDerived.getPctCacheHitNotExpiredDupResponses(),
					cacheAnalysisDerived.getPctCacheHitNotExpiredDupBytes());

			refreshLine(LabelKeys.cache_partialHitNotExpiredDup,
					cacheAnalysisDerived.getPctCacheHitRespChangedResponses(),
					cacheAnalysisDerived.getPctCacheHitRespChangedBytes());

			refreshLine(LabelKeys.cache_partialHitExpiredClientDup,
					cacheAnalysisDerived.getPctCacheHitExpiredDupClientResponses(),
					cacheAnalysisDerived.getPctCacheHitExpiredDupClientBytes());

			refreshLine(LabelKeys.cache_partialHitExpiredServerDup,
					cacheAnalysisDerived.getPctCacheHitExpiredDupServerResponses(),
					cacheAnalysisDerived.getPctCacheHitExpiredDupServerBytes());

			refreshLine(LabelKeys.cache_notExpired,
					cacheAnalysisDerived.getPctCacheHitDup304Responses(),
					cacheAnalysisDerived.getPctCacheHitDup304Bytes());

			refreshLine(LabelKeys.cache_notExpiredHeur,
					cacheAnalysisDerived.getPctPartialHitExpiredDupClientResponses(),
					cacheAnalysisDerived.getPctPartialHitExpiredDupClientBytes());

			refreshLine(LabelKeys.cache_expired,
					cacheAnalysisDerived.getPctPartialHitExpiredDupServerResponses(),
					cacheAnalysisDerived.getPctPartialHitExpiredDupServerBytes());

			refreshLine(LabelKeys.cache_expiredHeur,
					cacheAnalysisDerived.getPctPartialHitNotExpiredDupResponses(),
					cacheAnalysisDerived.getPctPartialHitNotExpiredDupBytes());
		}
	}
}
