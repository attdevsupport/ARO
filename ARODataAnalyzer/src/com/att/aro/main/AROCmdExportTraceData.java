package com.att.aro.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.ui.tabbedui.VerticalLayout;

public class AROCmdExportTraceData extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	private static final Font TEXT_FONT = new Font("TEXT_FONT", Font.PLAIN, 12);
	private static final int HEADER_DATA_SPACING = 10;

	private JLabel CmdExportLabel;
	
	/**
	 * Initializes a new instance of the AROCmdExportTraceData class
	 */
	public AROCmdExportTraceData() {
		super(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		add(createSummaryPanel(), BorderLayout.WEST);
	}

	private JPanel createSummaryPanel() {
		JPanel summaryAlligmentPanel = new JPanel(new BorderLayout());

		JPanel emulatorSummaryDataPanel = new JPanel();
		emulatorSummaryDataPanel.setLayout(new VerticalLayout());

		JPanel spacePanel = new JPanel();
		spacePanel.setPreferredSize(new Dimension(this.getWidth(), HEADER_DATA_SPACING));

		JPanel summaryTitlePanel = new JPanel(new BorderLayout());
		CmdExportLabel = new JLabel(rb.getString("cmdline.rtcexport"));
		CmdExportLabel.setFont(TEXT_FONT);
		summaryTitlePanel.add(CmdExportLabel, BorderLayout.CENTER);

		emulatorSummaryDataPanel.add(summaryTitlePanel);		
		summaryAlligmentPanel.add(emulatorSummaryDataPanel, BorderLayout.SOUTH);

		return summaryAlligmentPanel;
	}
}
