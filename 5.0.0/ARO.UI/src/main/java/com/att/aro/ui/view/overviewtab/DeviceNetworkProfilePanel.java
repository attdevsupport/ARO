package com.att.aro.ui.view.overviewtab;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.packetanalysis.pojo.TraceResultType;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.AROUIManager;
import com.att.aro.ui.commonui.TabPanelJPanel;
import com.att.aro.ui.model.overview.TraceInfo;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class DeviceNetworkProfilePanel extends TabPanelJPanel {
	private static final long serialVersionUID = 1L;
	
	private JLabel dateValueLabel;
	private JLabel traceValueLabel;
	private JLabel byteCountTotalLabel; // GregStory

	private JLabel networkTypeValueLabel;
	private JLabel profileValueLabel;
	private static final Font LABEL_FONT = new Font("TEXT_FONT", Font.BOLD, 12);
	private static final Font TEXT_FONT = new Font("TEXT_FONT", Font.PLAIN, 12);

	public JPanel layoutDataPanel() {
		final int borderGap = 10;
		setLayout(new BorderLayout(borderGap, borderGap));
		setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));

		dateValueLabel = new JLabel();
		dateValueLabel.setFont(TEXT_FONT);

		traceValueLabel = new JLabel();
		traceValueLabel.setFont(TEXT_FONT);

		byteCountTotalLabel = new JLabel();
		byteCountTotalLabel.setFont(TEXT_FONT);

		networkTypeValueLabel = new JLabel();
		networkTypeValueLabel.setFont(TEXT_FONT);
		profileValueLabel = new JLabel();
		profileValueLabel.setFont(TEXT_FONT);
		add(getDataPanel(), BorderLayout.CENTER);
		
		return this;
	}

	/**
	 * Creates the JPanel containing the Date , Trace, network profile and
	 * profile name.
	 * 
	 * @return the dataPanel
	 */
	private JPanel getDataPanel() {

		final int gridX = 5;
		final double wightX = 0.5;

		JPanel dataPanel;dataPanel = new JPanel(new GridBagLayout());

		Insets insets = new Insets(2, 2, 2, 2);
		JLabel dateLabel = new JLabel(
				ResourceBundleHelper.getMessageString("bestPractices.date"));
		dateLabel.setFont(LABEL_FONT);
		dataPanel.add(dateLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						insets, 0, 0));

		dataPanel.add(dateValueLabel, new GridBagConstraints(1, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				insets, 0, 0));

		JLabel networkTypeLabel = new JLabel(
				ResourceBundleHelper
						.getMessageString("bestPractices.networktype"),
				JLabel.RIGHT);
		networkTypeLabel.setFont(LABEL_FONT);
		dataPanel.add(networkTypeLabel, new GridBagConstraints(4, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets,
				0, 0));
		dataPanel.add(networkTypeValueLabel, new GridBagConstraints(gridX, 0,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, insets, 0, 0));
		JLabel traceLabel = new JLabel(
				ResourceBundleHelper.getMessageString("bestPractices.trace"));
		traceLabel.setFont(LABEL_FONT);
		dataPanel.add(traceLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						insets, 0, 0));
		dataPanel.add(traceValueLabel, new GridBagConstraints(1, 1, 1, 1,
				wightX, 0.0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		JLabel countLabel = new JLabel(
				ResourceBundleHelper
						.getMessageString("overview.info.bytecounttotal"),
				JLabel.CENTER);
		countLabel.setFont(LABEL_FONT);
		dataPanel.add(countLabel, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.NONE,
						insets, 0, 0));
		dataPanel.add(byteCountTotalLabel, new GridBagConstraints(3, 1, 1, 1,
				wightX, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				insets, 0, 0));

		JLabel profileLabel = new JLabel(
				ResourceBundleHelper.getMessageString("overview.info.profile"),
				JLabel.RIGHT);
		profileLabel.setFont(LABEL_FONT);
		dataPanel.add(profileLabel, new GridBagConstraints(4, 1, 1, 1, 0.0,
						0.0, // Change made here 2 to 4
						GridBagConstraints.EAST, GridBagConstraints.NONE,
						insets, 0, 0));
		dataPanel.add(profileValueLabel, new GridBagConstraints(gridX, 1, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				insets, 0, 0));

		return dataPanel;
	}
	
	public void setData(TraceInfo info){
		this.dateValueLabel.setText(info.getDateValue());
		this.traceValueLabel.setText(info.getTraceValue());
		this.byteCountTotalLabel.setText(info.getByteCountTotal().toString());
		this.profileValueLabel.setText(info.getProfileValue());
		this.networkTypeValueLabel.setText(info.getNetworkType());
		
	}
	
	public void refresh(AROTraceData aModel){
		
		TraceInfo tInfo = new TraceInfo();
		tInfo.setDateValue(aModel.getAnalyzerResult().getTraceresult().getTraceDateTime().toString());
		tInfo.setTraceValue(aModel.getAnalyzerResult().getTraceresult().getTraceDirectory());
		//tInfo.setByteCountTotal(aModel.getAnalyzerResult().getCacheAnalysis().getTotalBytesDownloaded());
		tInfo.setByteCountTotal(aModel.getAnalyzerResult().getStatistic().getTotalByte());
		tInfo.setProfileValue(aModel.getAnalyzerResult().getProfile().getName());
		if(aModel.getAnalyzerResult().getTraceresult().getTraceResultType().equals(TraceResultType.TRACE_DIRECTORY)){
			TraceDirectoryResult tracedirectoryResult = (TraceDirectoryResult)aModel.getAnalyzerResult().getTraceresult();
			tInfo.setNetworkType(tracedirectoryResult.getNetworkTypesList());
		}
		
		setData(tInfo);
	}

}
