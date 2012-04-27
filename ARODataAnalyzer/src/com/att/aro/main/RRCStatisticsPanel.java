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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.jfree.ui.tabbedui.VerticalLayout;

import com.att.aro.commonui.AROUIManager;
import com.att.aro.model.Profile;
import com.att.aro.model.Profile3G;
import com.att.aro.model.ProfileLTE;
import com.att.aro.model.RRCStateMachine;
import com.att.aro.model.TraceData;

/**
 * Represents a Panel that displays the RRC statistics on the Statistics tab.
 */
public class RRCStatisticsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();
	private static final Font HEADER_FONT = new Font("HeaderFont", Font.BOLD,
			16);
	private static final Font TEXT_FONT = new Font("TEXT_FONT", Font.PLAIN, 12);
	private static final int HEADER_DATA_SPACING = 10;

	private JLabel rrcParam1Label;
	private JLabel rrcParam1ValueLabel;
	private JLabel rrcParam2Label;
	private JLabel rrcParam2ValueLabel;
	private JLabel rrcParam3Label;
	private JLabel rrcParam3ValueLabel;
	private JLabel rrcParam4Label;
	private JLabel rrcParam4ValueLabel;
	private JLabel rrcParam5Label;
	private JLabel rrcParam5ValueLabel;
	private JLabel rrcParam6Label;
	private JLabel rrcParam6ValueLabel;
	private JLabel rrcParam7Label;
	private JLabel rrcParam7ValueLabel;
	private JLabel rrcParam8Label;
	private JLabel rrcParam8ValueLabel;

	Map<String, String> rrcContent = new LinkedHashMap<String, String>();

	/**
	 * Initializes a new instance of the RRCStatisticsPanel class.
	 */
	public RRCStatisticsPanel() {
		super(new BorderLayout(10, 10));
		setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		add(createRRCStatisticsPanel(), BorderLayout.WEST);
	}

	/**
	 * Creates the JPanel that contains the RRC statistics data.
	 */
	private JPanel createRRCStatisticsPanel() {

		JPanel rrcStatisticsLeftAlligmentPanel = new JPanel(new BorderLayout());
		rrcStatisticsLeftAlligmentPanel.setBackground(UIManager
				.getColor(AROUIManager.PAGE_BACKGROUND_KEY));

		JPanel rrcStatisticsPanel = new JPanel();
		rrcStatisticsPanel.setLayout(new VerticalLayout());
		rrcStatisticsPanel.setBackground(UIManager
				.getColor(AROUIManager.PAGE_BACKGROUND_KEY));

		rrcParam1Label = new JLabel(rb.getString("rrc.dch"));
		rrcParam1Label.setFont(TEXT_FONT);
		rrcParam1ValueLabel = new JLabel();
		rrcParam1ValueLabel.setFont(TEXT_FONT);
		rrcParam2Label = new JLabel(rb.getString("rrc.fach"));
		rrcParam2Label.setFont(TEXT_FONT);
		rrcParam2ValueLabel = new JLabel();
		rrcParam2ValueLabel.setFont(TEXT_FONT);
		rrcParam3Label = new JLabel(rb.getString("rrc.idle"));
		rrcParam3Label.setFont(TEXT_FONT);
		rrcParam3ValueLabel = new JLabel();
		rrcParam3ValueLabel.setFont(TEXT_FONT);
		rrcParam4Label = new JLabel(rb.getString("rrc.idle2dch"));
		rrcParam4Label.setFont(TEXT_FONT);
		rrcParam4ValueLabel = new JLabel();
		rrcParam4ValueLabel.setFont(TEXT_FONT);
		rrcParam5Label = new JLabel(rb.getString("rrc.fach2dch"));
		rrcParam5Label.setFont(TEXT_FONT);
		rrcParam5ValueLabel = new JLabel();
		rrcParam5ValueLabel.setFont(TEXT_FONT);
		rrcParam6Label = new JLabel(rb.getString("rrc.dchTailRatio"));
		rrcParam6Label.setFont(TEXT_FONT);
		rrcParam6ValueLabel = new JLabel();
		rrcParam6ValueLabel.setFont(TEXT_FONT);
		rrcParam7Label = new JLabel(rb.getString("rrc.fachTailRatio"));
		rrcParam7Label.setFont(TEXT_FONT);
		rrcParam7ValueLabel = new JLabel();
		rrcParam7ValueLabel.setFont(TEXT_FONT);
		rrcParam8Label = new JLabel(rb.getString("rrc.promotionRatio"));
		rrcParam8Label.setFont(TEXT_FONT);
		rrcParam8ValueLabel = new JLabel();
		rrcParam8ValueLabel.setFont(TEXT_FONT);

		JLabel rrcStatisticsHeaderLabel = new JLabel(rb.getString("rrc.title"));
		rrcStatisticsHeaderLabel.setFont(HEADER_FONT);

		rrcStatisticsPanel.add(rrcStatisticsHeaderLabel);

		JPanel spacePanel = new JPanel();
		spacePanel.setPreferredSize(new Dimension(this.getWidth(),
				HEADER_DATA_SPACING));
		spacePanel.setBackground(UIManager
				.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		rrcStatisticsPanel.add(spacePanel);

		JPanel rrcStatDataPanel = new JPanel(new GridLayout(8, 2, 5, 5));
		rrcStatDataPanel.setBackground(UIManager
				.getColor(AROUIManager.PAGE_BACKGROUND_KEY));

		rrcStatDataPanel.add(rrcParam1Label);
		rrcStatDataPanel.add(rrcParam1ValueLabel);
		rrcStatDataPanel.add(rrcParam2Label);
		rrcStatDataPanel.add(rrcParam2ValueLabel);
		rrcStatDataPanel.add(rrcParam3Label);
		rrcStatDataPanel.add(rrcParam3ValueLabel);
		rrcStatDataPanel.add(rrcParam4Label);
		rrcStatDataPanel.add(rrcParam4ValueLabel);
		rrcStatDataPanel.add(rrcParam5Label);
		rrcStatDataPanel.add(rrcParam5ValueLabel);
		rrcStatDataPanel.add(rrcParam6Label);
		rrcStatDataPanel.add(rrcParam6ValueLabel);
		rrcStatDataPanel.add(rrcParam7Label);
		rrcStatDataPanel.add(rrcParam7ValueLabel);
		rrcStatDataPanel.add(rrcParam8Label);
		rrcStatDataPanel.add(rrcParam8ValueLabel);

		rrcStatisticsPanel.add(rrcStatDataPanel);

		rrcStatisticsLeftAlligmentPanel.add(rrcStatisticsPanel);
		return rrcStatisticsLeftAlligmentPanel;
	}

	/**
	 * Refreshes the content of the RRCStatisticsPanel with the specified trace
	 * data.
	 * 
	 * @param analysis
	 *            - The Analysis object containing the trace data.
	 */
	public void refresh(TraceData.Analysis analysis) {

		if (analysis != null) {
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(2);
			nf.setMinimumFractionDigits(2);
			RRCStateMachine rrc = analysis.getRrcStateMachine();
			String valueAndPct = null;

			Profile profile = analysis.getProfile();
			if (profile instanceof Profile3G) {
				// Showing additional labels in case of 3G profile
				rrcParam6Label.setVisible(true);
				rrcParam6ValueLabel.setVisible(true);
				rrcParam7Label.setVisible(true);
				rrcParam7ValueLabel.setVisible(true);
				rrcParam8Label.setVisible(true);
				rrcParam8ValueLabel.setVisible(true);

				rrcParam1Label.setText(rb.getString("rrc.dch"));
				rrcParam2Label.setText(rb.getString("rrc.fach"));
				rrcParam3Label.setText(rb.getString("rrc.idle"));
				rrcParam4Label.setText(rb.getString("rrc.idle2dch"));
				rrcParam5Label.setText(rb.getString("rrc.fach2dch"));
				rrcParam6Label.setText(rb.getString("rrc.dchTailRatio"));

				valueAndPct = rb.getString("rrc.valueAndPct");
				rrcParam1ValueLabel.setText(MessageFormat.format(valueAndPct,
						nf.format(rrc.getDchTime()),
						nf.format(rrc.getDchTimeRatio() * 100.0)));
				rrcParam2ValueLabel.setText(MessageFormat.format(valueAndPct,
						nf.format(rrc.getFachTime()),
						nf.format(rrc.getFachTimeRatio() * 100.0)));
				rrcParam3ValueLabel.setText(MessageFormat.format(valueAndPct,
						nf.format(rrc.getIdleTime()),
						nf.format(rrc.getIdleTimeRatio() * 100.0)));
				rrcParam4ValueLabel.setText(MessageFormat.format(valueAndPct,
						nf.format(rrc.getIdleToDchTime()),
						nf.format(rrc.getIdleToDchTimeRatio() * 100.0)));
				rrcParam5ValueLabel.setText(MessageFormat.format(valueAndPct,
						nf.format(rrc.getFachToDchTime()),
						nf.format(rrc.getFachToDchTimeRatio() * 100.0)));
				rrcParam6ValueLabel.setText(nf.format(rrc.getDchTailRatio()));
				rrcParam7ValueLabel.setText(nf.format(rrc.getFachTailRatio()));
				rrcParam8ValueLabel.setText(nf.format(rrc.getPromotionRatio()));

				rrcContent.put(rb.getString("rrc.dch"),
						rrcParam1ValueLabel.getText());
				rrcContent.put(rb.getString("rrc.fach"),
						rrcParam2ValueLabel.getText());
				rrcContent.put(rb.getString("rrc.idle"),
						rrcParam3ValueLabel.getText());
				rrcContent.put(rb.getString("rrc.idle2dch"),
						rrcParam4ValueLabel.getText());
				rrcContent.put(rb.getString("rrc.fach2dch"),
						rrcParam5ValueLabel.getText());
				rrcContent.put(rb.getString("rrc.dchTailRatio"),
						rrcParam6ValueLabel.getText());
				rrcContent.put(rb.getString("rrc.fachTailRatio"),
						rrcParam7ValueLabel.getText());
				rrcContent.put(rb.getString("rrc.promotionRatio"),
						rrcParam8ValueLabel.getText());
			} else if (profile instanceof ProfileLTE) {

				// Hiding additional labels in case of LTE profile
				rrcParam6Label.setVisible(false);
				rrcParam6ValueLabel.setVisible(false);
				rrcParam7Label.setVisible(false);
				rrcParam7ValueLabel.setVisible(false);
				rrcParam8Label.setVisible(false);
				rrcParam8ValueLabel.setVisible(false);

				rrcParam1Label.setText(rb
						.getString("rrc.continuousReceptionIdle"));
				rrcParam2Label.setText(rb.getString("rrc.continuousReception"));
				rrcParam3Label.setText(rb.getString("rrc.shortDRX"));
				rrcParam4Label.setText(rb.getString("rrc.longDRX"));
				rrcParam5Label.setText(rb.getString("rrc.idle"));

				valueAndPct = rb.getString("rrc.valueAndPctLTE");
				rrcParam1ValueLabel
						.setText(MessageFormat.format(
								valueAndPct,
								nf.format(rrc.getLteIdleToCRPromotionTime()),
								nf.format(rrc
										.getLteIdleToCRPromotionTimeRatio() * 100.0)));
				rrcParam2ValueLabel.setText(MessageFormat.format(valueAndPct,
						nf.format(rrc.getLteCrTime()),
						nf.format(rrc.getLteCrTimeRatio() * 100.0)));
				rrcParam3ValueLabel.setText(MessageFormat.format(valueAndPct,
						nf.format(rrc.getLteDrxShortTime()),
						nf.format(rrc.getLteDrxShortTimeRatio() * 100.0)));
				rrcParam4ValueLabel.setText(MessageFormat.format(valueAndPct,
						nf.format(rrc.getLteDrxLongTime()),
						nf.format(rrc.getLteDrxLongTimeRatio() * 100.0)));
				rrcParam5ValueLabel.setText(MessageFormat.format(valueAndPct,
						nf.format(rrc.getLteIdleTime()),
						nf.format(rrc.getLteIdleTimeRatio() * 100.0)));

				rrcContent.put(rb.getString("rrc.continuousReceptionIdle"),
						rrcParam1ValueLabel.getText());
				rrcContent.put(rb.getString("rrc.continuousReception"),
						rrcParam2ValueLabel.getText());
				rrcContent.put(rb.getString("rrc.continuousReceptionTail"),
						rrcParam3ValueLabel.getText());
				rrcContent.put(rb.getString("rrc.shortDRX"),
						rrcParam4ValueLabel.getText());
				rrcContent.put(rb.getString("rrc.longDRX"),
						rrcParam5ValueLabel.getText());
				rrcContent.put(rb.getString("rrc.idle"),
						rrcParam6ValueLabel.getText());
			}
		} else {
			rrcParam1ValueLabel.setText(null);
			rrcParam2ValueLabel.setText(null);
			rrcParam3ValueLabel.setText(null);
			rrcParam4ValueLabel.setText(null);
			rrcParam5ValueLabel.setText(null);
			rrcParam6ValueLabel.setText(null);
			rrcParam7ValueLabel.setText(null);
			rrcParam8ValueLabel.setText(null);
			rrcContent.clear();
		}
	}

	/**
	 * Returns a Map of strings containing the RRC states data.
	 * 
	 * @return A Map containing the RRC states statistics.
	 */
	public Map<String, String> getRrcContent() {
		return rrcContent;
	}
} // @jve:decl-index=0:visual-constraint="10,10"
