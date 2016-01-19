/*
 *  Copyright 2015 AT&T
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
package com.att.aro.ui.view.bestpracticestab;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseWheelListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.BestPracticeType;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.AROUIManager;
import com.att.aro.ui.commonui.IARODiagnosticsOverviewRoute;
import com.att.aro.ui.commonui.RoundedBorder;
import com.att.aro.ui.commonui.UIComponent;
import com.att.aro.ui.model.bestpractice.BpTestsConductedModel;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.AROModelObserver;
import com.att.aro.ui.view.statistics.DateTraceAppDetailPanel;

/**
 * 
 * @author Barry Nelson
 *
 */
public  class BpDetail extends AbstractBpPanel {
	
	private static final long serialVersionUID = 1L;

	static final Font TEXT_FONT = new Font("TextFont", Font.PLAIN, 12);

	BpHeaderPanel header;

	DateTraceAppDetailPanel dateTraceAppDetailPanel;

	BpTestsConductedModel bpTestsConductedModel;

	List<AbstractBestPracticeResult> bpResults;

	Insets insets = new Insets(0, 0, 0, 0);

	AROModelObserver overViewObservable;
	
	JPanel detailPanel;

	JPanel fullPanel;

	JPanel dataPanel;
	
	ArrayList<BpDetailItem> expandableList = new ArrayList<BpDetailItem>();

	HashSet<BestPracticeType> bpFileDownloadTypes = new HashSet<BestPracticeType>();
	
	private IARODiagnosticsOverviewRoute diagnosticsOverviewRoute;


	public BpDetail(String title, IARODiagnosticsOverviewRoute diagnosticsOverviewRoute) {
		this.diagnosticsOverviewRoute = diagnosticsOverviewRoute;
		
		setLayout(new BorderLayout());
		setBorder(new RoundedBorder(new Insets(10, 10, 10, 10), UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY)));
		
		overViewObservable = new AROModelObserver();
		
		fullPanel = new JPanel(new BorderLayout());
		
		fullPanel.setOpaque(false);
		
		// Create the header bar
		header = new BpHeaderPanel(ResourceBundleHelper.getMessageString(title));
		fullPanel.add(header, BorderLayout.NORTH);

		// Create the data panel
		dataPanel = new JPanel(new BorderLayout());
		dataPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		dataPanel.setOpaque(false);

		dateTraceAppDetailPanel = new DateTraceAppDetailPanel();
		dataPanel.add(dateTraceAppDetailPanel, BorderLayout.NORTH);
		
		// Separator
		dataPanel.add(UIComponent.getInstance().getSeparator());

		detailPanel = new JPanel(new GridBagLayout());
		detailPanel.setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));

	}

	/**
	 * @param row
	 * @param itemDetail
	 */
	void addPanel(int row, BpDetailItem itemDetail) {
		itemDetail.addTablePanelRoute(diagnosticsOverviewRoute);
		detailPanel.add(itemDetail, new GridBagConstraints(0, row, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		overViewObservable.registerObserver(itemDetail);
		expandableList.add(itemDetail);
	}

	@Override
	public JPanel layoutDataPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	
	 JTextArea createJTextArea(String textToDisplay) {
		JTextArea jTextArea = new JTextArea(textToDisplay, 0, 0);
		jTextArea.setEditable(false);
		jTextArea.setFont(TEXT_FONT);
		jTextArea.setWrapStyleWord(true);
		jTextArea.setLineWrap(true);

		// Determine appropriate size for text area
		jTextArea.setSize(700, 9999);
		jTextArea.setPreferredSize(jTextArea.getPreferredSize());
		jTextArea.setMinimumSize(jTextArea.getPreferredSize());
		return jTextArea;
	}
	
	 static void removeMouseWheelListeners(JScrollPane scrollPane) {
		for (MouseWheelListener mwl : scrollPane.getMouseWheelListeners()) {
			scrollPane.removeMouseWheelListener(mwl);
		}
	}

	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void refresh(AROTraceData analyzerResult) {
		
	}
	
	/**
	 * forwards expand() command to All BpDetailItem
	 */
	public void expand() {
		for (BpDetailItem bpDetailItem:expandableList){
			bpDetailItem.expand();
		}
		
	}

	public BpHeaderPanel getHeader() {
		return header;
	}

	void updateHeader(AROTraceData analyzerResult){
		boolean fail = false, warning = false;
		if (analyzerResult.getBestPracticeResults()!=null){
			List<AbstractBestPracticeResult> results = analyzerResult.getBestPracticeResults();
			// analysis run: either pass --> green, warning --> yellow, fail --> red
			for (int i=0; i<results.size(); i++){
				if (bpFileDownloadTypes.contains(results.get(i).getBestPracticeType())){
					if (results.get(i).getResultType().compareTo(BPResultType.FAIL)==0){
						fail = true;
						break;
					} else if (results.get(i).getResultType().compareTo(BPResultType.WARNING)==0){
						warning = true;
					}
				}
			}
			header.setPass(!fail,warning);				
		} else {
			// analysis not run --> gray
			header.setPass(null,false);
		}	
	}

}
