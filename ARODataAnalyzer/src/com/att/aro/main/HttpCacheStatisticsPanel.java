/*
 * Copyright 2012 AT&T
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


package com.att.aro.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import com.att.aro.commonui.AROUIManager;
import com.att.aro.model.CacheAnalysis;
import com.att.aro.model.CacheEntry;
import com.att.aro.model.TraceData;

/**
 * Represents the panel that displays the Cache statistics data in the
 * Statistics tab.
 */
public class HttpCacheStatisticsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();
	private static final Font TEXT_FONT = new Font("TEXT_FONT", Font.PLAIN, 12);

	private JLabel cacheableLabel;
	private JLabel pctResponsesLabel;
	private JLabel pctBytesLabel;
	private JLabel cacheableSectionLabel;
	private JLabel pctCacheableLabel;
	private JLabel pctCacheableBytesLabel;
	private JLabel nonCachableLabel;
	private JLabel pctNonCacheableLabel;
	private JLabel pctNonCacheableBytesLabel;
	private JLabel simulationSectionLabel;
	private JLabel acceptableLabel;
	private JLabel cacheMissLabel;
	private JLabel pctCacheMissLabel;
	private JLabel pctCacheMissBytesLabel;
	private JLabel notCacheableLabel;
	private JLabel pctNotCacheableLabel;
	private JLabel pctNotCacheableBytesLabel;
	private JLabel cacheHitExpiredDup304Label;
	private JLabel pctCacheHitDup304Label;
	private JLabel pctCacheHitDup304BytesLabel;
	private JLabel cacheHitRespChangedLabel;
	private JLabel pctCacheHitRespChangedLabel;
	private JLabel pctCacheHitRespChangedBytesLabel;
	private JLabel duplicateFileDownload;
	private JLabel cacheHitNotExpiredDupLabel;
	private JLabel pctCacheHitNotExpiredDupLabel;
	private JLabel pctCacheHitNotExpiredDupBytesLabel;
	private JLabel cacheHitExpiredClientDupLabel;
	private JLabel pctCacheHitExpiredDupClientLabel;
	private JLabel pctCacheHitExpiredDupClientBytesLabel;
	private JLabel cacheHitExpiredServerDupLabel;
	private JLabel pctCacheHitExpiredDupServerLabel;
	private JLabel pctCacheHitExpiredDupServerBytesLabel;
	private JLabel duplicateStreaming;
	private JLabel partialHitExpiredClientDupLabel;
	private JLabel pctPartialHitExpiredDupClientLabel;
	private JLabel pctPartialHitExpiredDupClientBytesLabel;
	private JLabel partialHitExpiredServerDupLabel;
	private JLabel pctPartialHitExpiredDupServerLabel;
	private JLabel pctPartialHitExpiredDupServerBytesLabel;
	private JLabel partialHitNotExpiredDupLabel;
	private JLabel pctPartialHitNotExpiredDupLabel;
	private JLabel pctPartialHitNotExpiredDupBytesLabel;
	private JLabel duplicateAnalysis;
	private JLabel expiredLabel;
	private JLabel pctExpiredLabel;
	private JLabel pctExpiredBytesLabel;
	private JLabel notExpiredLabel;
	private JLabel pctNotExpiredLabel;
	private JLabel pctNotExpiredBytesLabel;
	private JLabel expiredHeurLabel;
	private JLabel pctExpiredHeurLabel;
	private JLabel pctExpiredHeurBytesLabel;
	private JLabel notExpiredHeurLabel;
	private JLabel pctNotExpiredHeurLabel;
	private JLabel pctNotExpiredHeurBytesLabel;

	Map<String, String> cacheContent = new LinkedHashMap<String, String>();

	/**
	 * Initializes a new instance of the HttpCacheStatisticsPanel class.
	 */
	public HttpCacheStatisticsPanel() {
		super(new BorderLayout());
		setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		initialize();
	}

	/**
	 * Initializes the Http cache statistics panel and its various components.
	 */
	private void initialize() {
		Insets insets = new Insets(2, 5, 2, 5);
		Insets headerInsets = new Insets(10, 5, 2, 5);
		pctCacheHitDup304BytesLabel = new JLabel();
		pctCacheHitDup304BytesLabel.setFont(TEXT_FONT);
		pctCacheHitDup304Label = new JLabel();
		pctCacheHitDup304Label.setFont(TEXT_FONT);
		cacheHitExpiredDup304Label = new JLabel(
				rb.getString("cache.cacheHitExpiredDup304"));
		cacheHitExpiredDup304Label.setFont(TEXT_FONT);
		pctCacheHitExpiredDupClientBytesLabel = new JLabel();
		pctCacheHitExpiredDupClientBytesLabel.setFont(TEXT_FONT);
		pctCacheHitExpiredDupClientLabel = new JLabel();
		pctCacheHitExpiredDupClientLabel.setFont(TEXT_FONT);
		cacheHitExpiredClientDupLabel = new JLabel(
				rb.getString("cache.cacheHitExpiredClientDup"));
		cacheHitExpiredClientDupLabel.setFont(TEXT_FONT);
		pctCacheHitExpiredDupServerBytesLabel = new JLabel();
		pctCacheHitExpiredDupServerBytesLabel.setFont(TEXT_FONT);
		pctCacheHitExpiredDupServerLabel = new JLabel();
		pctCacheHitExpiredDupServerLabel.setFont(TEXT_FONT);
		cacheHitExpiredServerDupLabel = new JLabel(
				rb.getString("cache.cacheHitExpiredServerDup"));
		cacheHitExpiredServerDupLabel.setFont(TEXT_FONT);
		pctPartialHitExpiredDupClientBytesLabel = new JLabel();
		pctPartialHitExpiredDupClientBytesLabel.setFont(TEXT_FONT);
		pctPartialHitExpiredDupClientLabel = new JLabel();
		pctPartialHitExpiredDupClientLabel.setFont(TEXT_FONT);
		partialHitExpiredClientDupLabel = new JLabel(
				rb.getString("cache.partialHitExpiredClientDup"));
		partialHitExpiredClientDupLabel.setFont(TEXT_FONT);
		pctPartialHitExpiredDupServerBytesLabel = new JLabel();
		pctPartialHitExpiredDupServerBytesLabel.setFont(TEXT_FONT);
		pctPartialHitExpiredDupServerLabel = new JLabel();
		pctPartialHitExpiredDupServerLabel.setFont(TEXT_FONT);
		partialHitExpiredServerDupLabel = new JLabel(
				rb.getString("cache.partialHitExpiredServerDup"));
		partialHitExpiredServerDupLabel.setFont(TEXT_FONT);
		pctPartialHitNotExpiredDupBytesLabel = new JLabel();
		pctPartialHitNotExpiredDupBytesLabel.setFont(TEXT_FONT);
		pctPartialHitNotExpiredDupLabel = new JLabel();
		pctPartialHitNotExpiredDupLabel.setFont(TEXT_FONT);
		partialHitNotExpiredDupLabel = new JLabel(
				rb.getString("cache.partialHitNotExpiredDup"));
		partialHitNotExpiredDupLabel.setFont(TEXT_FONT);
		pctCacheHitRespChangedBytesLabel = new JLabel();
		pctCacheHitRespChangedBytesLabel.setFont(TEXT_FONT);
		pctCacheHitRespChangedLabel = new JLabel();
		pctCacheHitRespChangedLabel.setFont(TEXT_FONT);
		cacheHitRespChangedLabel = new JLabel(
				rb.getString("cache.cacheHitRespChanged"));
		cacheHitRespChangedLabel.setFont(TEXT_FONT);
		pctCacheHitNotExpiredDupBytesLabel = new JLabel();
		pctCacheHitNotExpiredDupBytesLabel.setFont(TEXT_FONT);
		pctCacheHitNotExpiredDupLabel = new JLabel();
		pctCacheHitNotExpiredDupLabel.setFont(TEXT_FONT);
		cacheHitNotExpiredDupLabel = new JLabel(
				rb.getString("cache.cacheHitNotExpiredDup"));
		cacheHitNotExpiredDupLabel.setFont(TEXT_FONT);
		pctNotCacheableBytesLabel = new JLabel();
		pctNotCacheableBytesLabel.setFont(TEXT_FONT);
		pctNotCacheableLabel = new JLabel();
		pctNotCacheableLabel.setFont(TEXT_FONT);
		notCacheableLabel = new JLabel(rb.getString("cache.notCacheable"));
		notCacheableLabel.setFont(TEXT_FONT);
		pctCacheMissBytesLabel = new JLabel();
		pctCacheMissBytesLabel.setFont(TEXT_FONT);
		pctCacheMissLabel = new JLabel();
		pctCacheMissLabel.setFont(TEXT_FONT);
		cacheMissLabel = new JLabel(rb.getString("cache.cacheMiss"));
		cacheMissLabel.setFont(TEXT_FONT);
		simulationSectionLabel = new JLabel(
				rb.getString("cache.simulationSection"));
		simulationSectionLabel.setFont(TEXT_FONT);
		acceptableLabel = new JLabel(rb.getString("cache.acceptableBehavior"));
		acceptableLabel.setFont(TEXT_FONT);
		duplicateFileDownload = new JLabel(
				rb.getString("cache.duplicateFileDownload"));
		duplicateFileDownload.setFont(TEXT_FONT);
		duplicateStreaming = new JLabel(
				rb.getString("cache.duplicateStreaming"));
		duplicateStreaming.setFont(TEXT_FONT);
		duplicateAnalysis = new JLabel(rb.getString("cache.duplicateAnalysis"));
		duplicateAnalysis.setFont(TEXT_FONT);
		pctNotExpiredHeurBytesLabel = new JLabel();
		pctNotExpiredHeurBytesLabel.setFont(TEXT_FONT);
		pctNotExpiredHeurLabel = new JLabel();
		pctNotExpiredHeurLabel.setFont(TEXT_FONT);
		notExpiredHeurLabel = new JLabel(rb.getString("cache.notExpiredHeur"));
		notExpiredHeurLabel.setFont(TEXT_FONT);
		pctExpiredHeurBytesLabel = new JLabel();
		pctExpiredHeurBytesLabel.setFont(TEXT_FONT);
		pctExpiredHeurLabel = new JLabel();
		pctExpiredHeurLabel.setFont(TEXT_FONT);
		expiredHeurLabel = new JLabel(rb.getString("cache.expiredHeur"));
		expiredHeurLabel.setFont(TEXT_FONT);
		pctNotExpiredBytesLabel = new JLabel();
		pctNotExpiredBytesLabel.setFont(TEXT_FONT);
		pctNotExpiredLabel = new JLabel();
		pctNotExpiredLabel.setFont(TEXT_FONT);
		notExpiredLabel = new JLabel(rb.getString("cache.notExpired"));
		notExpiredLabel.setFont(TEXT_FONT);
		pctExpiredBytesLabel = new JLabel();
		pctExpiredBytesLabel.setFont(TEXT_FONT);
		pctExpiredLabel = new JLabel();
		pctExpiredLabel.setFont(TEXT_FONT);
		expiredLabel = new JLabel(rb.getString("cache.expired"));
		expiredLabel.setFont(TEXT_FONT);
		pctNonCacheableBytesLabel = new JLabel();
		pctNonCacheableBytesLabel.setFont(TEXT_FONT);
		pctNonCacheableLabel = new JLabel();
		pctNonCacheableLabel.setFont(TEXT_FONT);
		nonCachableLabel = new JLabel(rb.getString("cache.nonCachable"));
		nonCachableLabel.setFont(TEXT_FONT);
		pctCacheableBytesLabel = new JLabel();
		pctCacheableBytesLabel.setFont(TEXT_FONT);
		pctCacheableLabel = new JLabel();
		pctCacheableLabel.setFont(TEXT_FONT);
		cacheableSectionLabel = new JLabel(
				rb.getString("cache.cacheableSection"));
		cacheableSectionLabel.setFont(TEXT_FONT);
		pctBytesLabel = new JLabel(rb.getString("cache.pctBytes"));
		pctBytesLabel.setFont(TEXT_FONT);
		pctResponsesLabel = new JLabel(rb.getString("cache.pctResponses"));
		pctResponsesLabel.setFont(TEXT_FONT);
		cacheableLabel = new JLabel(rb.getString("cache.cacheable"));
		cacheableLabel.setFont(TEXT_FONT);

		this.setBorder(BorderFactory.createTitledBorder(null,
				rb.getString("cache.title"),
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION,
				new Font("Dialog", Font.BOLD, 16), new Color(51, 51, 51)));
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBackground(UIManager
				.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		panel.add(pctResponsesLabel, new GridBagConstraints(1, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				insets, 0, 0));
		panel.add(pctBytesLabel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0,
				0));
		panel.add(new JSeparator(), new GridBagConstraints(0, 1, 3, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				insets, 0, 0));
		panel.add(cacheableSectionLabel, new GridBagConstraints(0, 2, 3, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				headerInsets, 0, 0));
		panel.add(cacheableLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		panel.add(pctCacheableLabel, new GridBagConstraints(1, 3, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				insets, 0, 0));
		panel.add(pctCacheableBytesLabel, new GridBagConstraints(2, 3, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				insets, 0, 0));
		panel.add(nonCachableLabel, new GridBagConstraints(0, 4, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets,
				0, 0));
		panel.add(pctNonCacheableLabel, new GridBagConstraints(1, 4, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				insets, 0, 0));
		panel.add(pctNonCacheableBytesLabel, new GridBagConstraints(2, 4, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				insets, 0, 0));
		panel.add(new JSeparator(), new GridBagConstraints(0, 5, 3, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				insets, 0, 0));
		panel.add(simulationSectionLabel, new GridBagConstraints(0, 6, 3, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				headerInsets, 0, 0));
		panel.add(acceptableLabel, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, headerInsets,
				0, 0));
		panel.add(cacheMissLabel, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		panel.add(pctCacheMissLabel, new GridBagConstraints(1, 8, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				insets, 0, 0));
		panel.add(pctCacheMissBytesLabel, new GridBagConstraints(2, 8, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				insets, 0, 0));
		panel.add(notCacheableLabel, new GridBagConstraints(0, 9, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets,
				0, 0));
		panel.add(pctNotCacheableLabel, new GridBagConstraints(1, 9, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				insets, 0, 0));
		panel.add(pctNotCacheableBytesLabel, new GridBagConstraints(2, 9, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				insets, 0, 0));
		panel.add(cacheHitExpiredDup304Label, new GridBagConstraints(0, 10, 1,
				1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				insets, 0, 0));
		panel.add(pctCacheHitDup304Label, new GridBagConstraints(1, 10, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				insets, 0, 0));
		panel.add(pctCacheHitDup304BytesLabel, new GridBagConstraints(2, 10, 1,
				1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, insets, 0, 0));
		panel.add(cacheHitRespChangedLabel, new GridBagConstraints(0, 11, 1, 1,
				0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				insets, 0, 0));
		panel.add(pctCacheHitRespChangedLabel, new GridBagConstraints(1, 11, 1,
				1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, insets, 0, 0));
		panel.add(pctCacheHitRespChangedBytesLabel, new GridBagConstraints(2,
				11, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, insets, 0, 0));
		panel.add(duplicateFileDownload, new GridBagConstraints(0, 12, 1, 1,
				0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				headerInsets, 0, 0));
		panel.add(cacheHitNotExpiredDupLabel, new GridBagConstraints(0, 13, 1,
				1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				insets, 0, 0));
		panel.add(pctCacheHitNotExpiredDupLabel, new GridBagConstraints(1, 13,
				1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, insets, 0, 0));
		panel.add(pctCacheHitNotExpiredDupBytesLabel, new GridBagConstraints(2,
				13, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, insets, 0, 0));
		panel.add(cacheHitExpiredClientDupLabel, new GridBagConstraints(0, 14,
				1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.NONE, insets, 0, 0));
		panel.add(pctCacheHitExpiredDupClientLabel, new GridBagConstraints(1,
				14, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, insets, 0, 0));
		panel.add(pctCacheHitExpiredDupClientBytesLabel,
				new GridBagConstraints(2, 14, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						insets, 0, 0));
		panel.add(cacheHitExpiredServerDupLabel, new GridBagConstraints(0, 15,
				1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.NONE, insets, 0, 0));
		panel.add(pctCacheHitExpiredDupServerLabel, new GridBagConstraints(1,
				15, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, insets, 0, 0));
		panel.add(pctCacheHitExpiredDupServerBytesLabel,
				new GridBagConstraints(2, 15, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						insets, 0, 0));
		panel.add(duplicateStreaming, new GridBagConstraints(0, 16, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				headerInsets, 0, 0));
		panel.add(partialHitNotExpiredDupLabel, new GridBagConstraints(0, 17,
				1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.NONE, insets, 0, 0));
		panel.add(pctPartialHitNotExpiredDupLabel, new GridBagConstraints(1,
				17, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, insets, 0, 0));
		panel.add(pctPartialHitNotExpiredDupBytesLabel, new GridBagConstraints(
				2, 17, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, insets, 0, 0));
		panel.add(partialHitExpiredClientDupLabel, new GridBagConstraints(0,
				18, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.NONE, insets, 0, 0));
		panel.add(pctPartialHitExpiredDupClientLabel, new GridBagConstraints(1,
				18, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, insets, 0, 0));
		panel.add(pctPartialHitExpiredDupClientBytesLabel,
				new GridBagConstraints(2, 18, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						insets, 0, 0));
		panel.add(partialHitExpiredServerDupLabel, new GridBagConstraints(0,
				19, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.NONE, insets, 0, 0));
		panel.add(pctPartialHitExpiredDupServerLabel, new GridBagConstraints(1,
				19, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, insets, 0, 0));
		panel.add(pctPartialHitExpiredDupServerBytesLabel,
				new GridBagConstraints(2, 19, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						insets, 0, 0));
		panel.add(new JSeparator(), new GridBagConstraints(0, 20, 3, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				insets, 0, 0));
		panel.add(duplicateAnalysis, new GridBagConstraints(0, 21, 3, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				headerInsets, 0, 0));
		panel.add(notExpiredLabel, new GridBagConstraints(0, 22, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets,
				0, 0));
		panel.add(pctNotExpiredLabel, new GridBagConstraints(1, 22, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				insets, 0, 0));
		panel.add(pctNotExpiredBytesLabel, new GridBagConstraints(2, 22, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				insets, 0, 0));
		panel.add(notExpiredHeurLabel, new GridBagConstraints(0, 23, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets,
				0, 0));
		panel.add(pctNotExpiredHeurLabel, new GridBagConstraints(1, 23, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				insets, 0, 0));
		panel.add(pctNotExpiredHeurBytesLabel, new GridBagConstraints(2, 23, 1,
				1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, insets, 0, 0));
		panel.add(expiredLabel, new GridBagConstraints(0, 24, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		panel.add(pctExpiredLabel, new GridBagConstraints(1, 24, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				insets, 0, 0));
		panel.add(pctExpiredBytesLabel, new GridBagConstraints(2, 24, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				insets, 0, 0));
		panel.add(expiredHeurLabel, new GridBagConstraints(0, 25, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets,
				0, 0));
		panel.add(pctExpiredHeurLabel, new GridBagConstraints(1, 25, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				insets, 0, 0));
		panel.add(pctExpiredHeurBytesLabel, new GridBagConstraints(2, 25, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				insets, 0, 0));
		panel.add(new JSeparator(), new GridBagConstraints(0, 26, 3, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				insets, 0, 0));
		this.add(panel, BorderLayout.WEST);
	}

	/**
	 * Refreshes the content of the HTTPCacheStatistics panel with the specified
	 * trace data.
	 * 
	 * @param analysis
	 *            - An Analysis object containing the trace data.
	 */
	public void refresh(TraceData.Analysis analysis) {
		if (analysis != null) {

			CacheAnalysis cacheAnalysis = analysis.getCacheAnalysis();
			List<CacheEntry> results = cacheAnalysis.getDiagnosisResults();
			int cacheable = 0;
			long cacheableBytes = 0;
			int notCacheable = 0;
			long notCacheableBytes = 0;
			int expired = 0;
			long expiredBytes = 0;
			int notExpired = 0;
			long notExpiredBytes = 0;
			int expiredHeur = 0;
			long expiredHeurBytes = 0;
			int notExpiredHeur = 0;
			long notExpiredHeurBytes = 0;
			int cacheMiss = 0;
			long cacheMissBytes = 0;
			int hitNotExpiredDup = 0;
			long hitNotExpiredDupBytes = 0;
			int hitResponseChanged = 0;
			long hitResponseChangedBytes = 0;
			int hitExpiredDupClient = 0;
			long hitExpiredDupClientBytes = 0;
			int hitExpiredDupServer = 0;
			long hitExpiredDupServerBytes = 0;
			int hitExpired304 = 0;
			long hitExpired304Bytes = 0;
			int partialHitExpiredDupClient = 0;
			long partialHitExpiredDupClientBytes = 0;
			int partialHitExpiredDupServer = 0;
			long partialHitExpiredDupServerBytes = 0;
			int partialHitNotExpiredDup = 0;
			long partialHitNotExpiredDupBytes = 0;

			for (CacheEntry entry : results) {
				long bytes = entry.getBytesInCache();
				switch (entry.getDiagnosis()) {
				case CACHING_DIAG_REQUEST_NOT_FOUND:
				case CACHING_DIAG_INVALID_OBJ_NAME:
				case CACHING_DIAG_INVALID_REQUEST:
				case CACHING_DIAG_INVALID_RESPONSE:
					break;
				case CACHING_DIAG_CACHE_MISSED:
					++cacheMiss;
					cacheMissBytes += bytes;
					++cacheable;
					cacheableBytes += bytes;
					break;
				case CACHING_DIAG_NOT_CACHABLE:
					++notCacheable;
					notCacheableBytes += entry.getBytesNotInCache();
					break;
				case CACHING_DIAG_NOT_EXPIRED_DUP:
					++hitNotExpiredDup;
					hitNotExpiredDupBytes += bytes;
					++cacheable;
					cacheableBytes += bytes;
					break;
				case CACHING_DIAG_OBJ_NOT_CHANGED_DUP_CLIENT:
					++hitExpiredDupClient;
					hitExpiredDupClientBytes += bytes;
					++cacheable;
					cacheableBytes += bytes;
					break;
				case CACHING_DIAG_OBJ_NOT_CHANGED_DUP_SERVER:
					++hitExpiredDupServer;
					hitExpiredDupServerBytes += bytes;
					++cacheable;
					cacheableBytes += bytes;
					break;
				case CACHING_DIAG_OBJ_CHANGED:
					++hitResponseChanged;
					hitResponseChangedBytes += bytes;
					++cacheable;
					cacheableBytes += bytes;
					break;
				case CACHING_DIAG_OBJ_NOT_CHANGED_304:
					++hitExpired304;
					hitExpired304Bytes += bytes;
					++cacheable;
					cacheableBytes += bytes;
					break;
				case CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_CLIENT:
					++partialHitExpiredDupClient;
					partialHitExpiredDupClientBytes += bytes;
					++cacheable;
					cacheableBytes += bytes;
					break;
				case CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_SERVER:
					++partialHitExpiredDupServer;
					partialHitExpiredDupServerBytes += bytes;
					++cacheable;
					cacheableBytes += bytes;
					break;
				case CACHING_DIAG_NOT_EXPIRED_DUP_PARTIALHIT:
					++partialHitNotExpiredDup;
					partialHitNotExpiredDupBytes += bytes;
					++cacheable;
					cacheableBytes += bytes;
					break;
				}
			}

			// Categorize expiration types on responses
			Map<CacheAnalysis.CacheExpiration, List<CacheEntry>> expirations = cacheAnalysis
					.getCacheExpirationResponses();
			for (Map.Entry<CacheAnalysis.CacheExpiration, List<CacheEntry>> entry : expirations
					.entrySet()) {
				int count = 0;
				int bytes = 0;
				for (CacheEntry cacheEntry : entry.getValue()) {
					++count;
					bytes += cacheEntry.getBytesInCache();
				}
				switch (entry.getKey()) {
				case CACHE_EXPIRED:
					expired = count;
					expiredBytes = bytes;
					break;
				case CACHE_EXPIRED_HEURISTIC:
					expiredHeur = count;
					expiredHeurBytes = bytes;
					break;
				case CACHE_NOT_EXPIRED:
					notExpired = count;
					notExpiredBytes = bytes;
					break;
				case CACHE_NOT_EXPIRED_HEURISTIC:
					notExpiredHeur = count;
					notExpiredHeurBytes = bytes;
					break;
				}
			}

			NumberFormat format = NumberFormat.getNumberInstance();
			format.setMaximumFractionDigits(1);
			format.setMinimumFractionDigits(1);

			int total;
			long totalBytes;
			total = cacheable + notCacheable;
			totalBytes = cacheableBytes + notCacheableBytes;
			pctCacheableLabel.setText(format.format(pct(cacheable, total)));
			pctCacheableBytesLabel.setText(format.format(pct(cacheableBytes,
					totalBytes)));
			pctNonCacheableLabel.setText(format
					.format(pct(notCacheable, total)));
			pctNonCacheableBytesLabel.setText(format.format(pct(
					notCacheableBytes, totalBytes)));

			total = expired + expiredHeur + notExpired + notExpiredHeur;
			totalBytes = expiredBytes + expiredHeurBytes + notExpiredBytes
					+ notExpiredHeurBytes;
			pctExpiredLabel.setText(format.format(pct(expired, total)));
			pctExpiredBytesLabel.setText(format.format(pct(expiredBytes,
					totalBytes)));
			pctNotExpiredLabel.setText(format.format(pct(notExpired, total)));
			pctNotExpiredBytesLabel.setText(format.format(pct(notExpiredBytes,
					totalBytes)));
			pctExpiredHeurLabel.setText(format.format(pct(expiredHeur, total)));
			pctExpiredHeurBytesLabel.setText(format.format(pct(
					expiredHeurBytes, totalBytes)));
			pctNotExpiredHeurLabel.setText(format.format(pct(notExpiredHeur,
					total)));
			pctNotExpiredHeurBytesLabel.setText(format.format(pct(
					notExpiredHeurBytes, totalBytes)));

			total = cacheMiss + notCacheable + hitNotExpiredDup
					+ hitResponseChanged + hitExpiredDupClient
					+ hitExpiredDupServer + hitExpired304
					+ partialHitExpiredDupClient + partialHitExpiredDupServer
					+ partialHitNotExpiredDup;
			totalBytes = cacheMissBytes + notCacheableBytes
					+ hitNotExpiredDupBytes + hitResponseChangedBytes
					+ hitExpiredDupClientBytes + hitExpiredDupServerBytes
					+ hitExpired304Bytes + partialHitExpiredDupClientBytes
					+ partialHitExpiredDupServerBytes
					+ partialHitNotExpiredDupBytes;
			pctCacheMissLabel.setText(format.format(pct(cacheMiss, total)));
			pctCacheMissBytesLabel.setText(format.format(pct(cacheMissBytes,
					totalBytes)));
			pctNotCacheableLabel.setText(format
					.format(pct(notCacheable, total)));
			pctNotCacheableBytesLabel.setText(format.format(pct(
					notCacheableBytes, totalBytes)));
			pctCacheHitNotExpiredDupLabel.setText(format.format(pct(
					hitNotExpiredDup, total)));
			pctCacheHitNotExpiredDupBytesLabel.setText(format.format(pct(
					hitNotExpiredDupBytes, totalBytes)));
			pctCacheHitRespChangedLabel.setText(format.format(pct(
					hitResponseChanged, total)));
			pctCacheHitRespChangedBytesLabel.setText(format.format(pct(
					hitResponseChangedBytes, totalBytes)));
			pctCacheHitExpiredDupClientLabel.setText(format.format(pct(
					hitExpiredDupClient, total)));
			pctCacheHitExpiredDupClientBytesLabel.setText(format.format(pct(
					hitExpiredDupClientBytes, totalBytes)));
			pctCacheHitExpiredDupServerLabel.setText(format.format(pct(
					hitExpiredDupServer, total)));
			pctCacheHitExpiredDupServerBytesLabel.setText(format.format(pct(
					hitExpiredDupServerBytes, totalBytes)));
			pctCacheHitDup304Label.setText(format.format(pct(hitExpired304,
					total)));
			pctCacheHitDup304BytesLabel.setText(format.format(pct(
					hitExpired304Bytes, totalBytes)));
			pctPartialHitExpiredDupClientLabel.setText(format.format(pct(
					partialHitExpiredDupClient, total)));
			pctPartialHitExpiredDupClientBytesLabel.setText(format.format(pct(
					partialHitExpiredDupClientBytes, totalBytes)));
			pctPartialHitExpiredDupServerLabel.setText(format.format(pct(
					partialHitExpiredDupServer, total)));
			pctPartialHitExpiredDupServerBytesLabel.setText(format.format(pct(
					partialHitExpiredDupServerBytes, totalBytes)));
			pctPartialHitNotExpiredDupLabel.setText(format.format(pct(
					partialHitNotExpiredDup, total)));
			pctPartialHitNotExpiredDupBytesLabel.setText(format.format(pct(
					partialHitNotExpiredDupBytes, totalBytes)));

			cacheContent.put(
					rb.getString("statics.csvsubTitle.httpState"),
					rb.getString("statics.csvFormat.response")
							+ rb.getString("fileType.filters.forwardSlash")
							+ rb.getString("statics.csvFormat.bytes"));
			cacheContent.put(
					rb.getString("cache.cacheable"),
					pctCacheableLabel.getText()
							+ rb.getString("fileType.filters.forwardSlash")
							+ pctCacheableBytesLabel.getText());
			cacheContent.put(
					rb.getString("cache.nonCachable"),
					pctNonCacheableLabel.getText()
							+ rb.getString("fileType.filters.forwardSlash")
							+ pctNonCacheableBytesLabel.getText());

			cacheContent.put(
					rb.getString("statics.csvsubTitle.cacheSimulationState"),
					rb.getString("fileType.filters.backwardSlash"));
			cacheContent
					.put(rb.getString("statics.csvsubTitle.acceptableBehaviour"),
							"");
			cacheContent.put(
					rb.getString("cache.cacheMiss"),
					pctCacheMissLabel.getText()
							+ rb.getString("fileType.filters.forwardSlash")
							+ pctCacheMissBytesLabel.getText());
			cacheContent.put(
					rb.getString("cache.notCacheable"),
					pctNotCacheableLabel.getText()
							+ rb.getString("fileType.filters.forwardSlash")
							+ pctNotCacheableBytesLabel.getText());
			cacheContent.put(
					rb.getString("cache.cacheHitExpiredDup304"),
					pctCacheHitDup304Label.getText()
							+ rb.getString("fileType.filters.forwardSlash")
							+ pctCacheHitDup304BytesLabel.getText());
			cacheContent.put(
					rb.getString("cache.cacheHitRespChanged"),
					pctCacheHitRespChangedLabel.getText()
							+ rb.getString("fileType.filters.forwardSlash")
							+ pctCacheHitRespChangedBytesLabel.getText());

			cacheContent.put(rb.getString("statics.csvsubTitle.dupDownload"),
					"");
			cacheContent.put(
					rb.getString("cache.cacheHitNotExpiredDup"),
					pctCacheHitNotExpiredDupLabel.getText()
							+ rb.getString("fileType.filters.forwardSlash")
							+ pctCacheHitNotExpiredDupBytesLabel.getText());
			cacheContent.put(
					rb.getString("cache.cacheHitExpiredClientDup"),
					pctCacheHitExpiredDupClientLabel.getText()
							+ rb.getString("fileType.filters.forwardSlash")
							+ pctCacheHitExpiredDupClientBytesLabel.getText());
			cacheContent.put(
					rb.getString("cache.cacheHitExpiredServerDup"),
					pctCacheHitExpiredDupServerLabel.getText()
							+ rb.getString("fileType.filters.forwardSlash")
							+ pctCacheHitExpiredDupServerBytesLabel.getText());

			cacheContent.put(rb.getString("statics.csvsubTitle.dupStreaming"),
					"");
			cacheContent.put(
					rb.getString("cache.partialHitNotExpiredDup"),
					pctPartialHitNotExpiredDupLabel.getText()
							+ rb.getString("fileType.filters.forwardSlash")
							+ pctPartialHitNotExpiredDupBytesLabel.getText());
			cacheContent
					.put(rb.getString("cache.partialHitExpiredClientDup"),
							pctPartialHitExpiredDupClientLabel.getText()
									+ rb.getString("fileType.filters.forwardSlash")
									+ pctPartialHitExpiredDupClientBytesLabel
											.getText());
			cacheContent
					.put(rb.getString("cache.partialHitExpiredServerDup"),
							pctPartialHitExpiredDupServerLabel.getText()
									+ rb.getString("fileType.filters.forwardSlash")
									+ pctPartialHitExpiredDupServerBytesLabel
											.getText());

			cacheContent.put(rb.getString("statics.csvsubTitle.dupFile"), "");
			cacheContent.put(
					rb.getString("cache.notExpired"),
					pctNotExpiredLabel.getText()
							+ rb.getString("fileType.filters.forwardSlash")
							+ pctNotExpiredBytesLabel.getText());
			cacheContent.put(
					rb.getString("cache.notExpiredHeur"),
					pctNotExpiredHeurLabel.getText()
							+ rb.getString("fileType.filters.forwardSlash")
							+ pctNotExpiredHeurBytesLabel.getText());
			cacheContent.put(
					rb.getString("cache.expired"),
					pctExpiredLabel.getText()
							+ rb.getString("fileType.filters.forwardSlash")
							+ pctExpiredBytesLabel.getText());
			cacheContent.put(
					rb.getString("cache.expiredHeur"),
					pctExpiredHeurLabel.getText()
							+ rb.getString("fileType.filters.forwardSlash")
							+ pctExpiredHeurBytesLabel.getText());
		} else {
			pctCacheableLabel.setText(null);
			pctCacheableBytesLabel.setText(null);
			pctNonCacheableLabel.setText(null);
			pctNonCacheableBytesLabel.setText(null);

			pctExpiredLabel.setText(null);
			pctExpiredBytesLabel.setText(null);
			pctNotExpiredLabel.setText(null);
			pctNotExpiredBytesLabel.setText(null);
			pctExpiredHeurLabel.setText(null);
			pctExpiredHeurBytesLabel.setText(null);
			pctNotExpiredHeurLabel.setText(null);
			pctNotExpiredHeurBytesLabel.setText(null);
			pctCacheMissLabel.setText(null);
			pctCacheMissBytesLabel.setText(null);
			pctNotCacheableLabel.setText(null);
			pctNotCacheableBytesLabel.setText(null);
			pctCacheHitNotExpiredDupLabel.setText(null);
			pctCacheHitNotExpiredDupBytesLabel.setText(null);
			pctCacheHitRespChangedLabel.setText(null);
			pctCacheHitRespChangedBytesLabel.setText(null);
			pctCacheHitExpiredDupClientLabel.setText(null);
			pctCacheHitExpiredDupClientBytesLabel.setText(null);
			pctCacheHitExpiredDupServerLabel.setText(null);
			pctCacheHitExpiredDupServerBytesLabel.setText(null);
			pctCacheHitDup304Label.setText(null);
			pctCacheHitDup304BytesLabel.setText(null);
			pctPartialHitExpiredDupClientLabel.setText(null);
			pctPartialHitExpiredDupClientBytesLabel.setText(null);
			pctPartialHitExpiredDupServerLabel.setText(null);
			pctPartialHitExpiredDupServerBytesLabel.setText(null);
			pctPartialHitNotExpiredDupLabel.setText(null);
			pctPartialHitNotExpiredDupBytesLabel.setText(null);

			cacheContent.clear();
		}
	}

	/**
	 * This method calculates the percentage of x in y.
	 */
	private double pct(long x, long y) {
		return y != 0.0 ? (double) x / y * 100.0 : 0.0;
	}

	/**
	 * Returns a Map object containing the detailed data related to Http
	 * caching. This method is provided so that the data can be exported to a
	 * .csv file when the user clicks the “Save As” button.
	 * 
	 * @return A Map object containing the collection of detailed Http caching
	 *         data.
	 */
	public Map<String, String> getCacheContent() {
		return cacheContent;
	}
}
