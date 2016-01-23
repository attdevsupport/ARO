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
package com.att.aro.ui.view.overviewtab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.att.aro.core.packetanalysis.pojo.CacheEntry;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.ContentViewer;
import com.att.aro.ui.commonui.MessageDialogFactory;
import com.att.aro.ui.commonui.TabPanelJPanel;
import com.att.aro.ui.model.DataTable;
import com.att.aro.ui.model.overview.DuplicateContentTableModel;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class DuplicateContentTablePanel extends TabPanelJPanel implements MouseListener {
	
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DuplicateContentTablePanel.class.getName()); 

	private DuplicateContentTableModel duplicateContentTableModel = new DuplicateContentTableModel();
	private DataTable<CacheEntry> duplicateContentTable;
	
	private AROTraceData traceDataModel;
	private OverviewTab overviewTab;
	private JButton	viewBtn;
	private JButton	saveBtn;
	
	public DuplicateContentTablePanel(OverviewTab overviewTab) {
		super();
		this.overviewTab = overviewTab;
	}
	
	public JPanel layoutDataPanel(){
		setLayout(new BorderLayout());
		
		add(getjDuplicateTitleLabel(), BorderLayout.NORTH);
		add(getjDuplicateContentPanel(), BorderLayout.CENTER);
		
		return this;
	}
	
	/**
	 * Returns the duplicate content header label.
	 */
	private JLabel getjDuplicateTitleLabel() {
		JLabel duplicateTitleLabel = new JLabel(ResourceBundleHelper.getMessageString("duplicate.title"), JLabel.CENTER);

		return duplicateTitleLabel;
	}

	/**
	 * Initializes and returns the DuplicateContentPanel.
	 */
	private JPanel getjDuplicateContentPanel() {

		JPanel duplicateContentPanel = new JPanel(new BorderLayout());
		duplicateContentPanel.add(getScrollPane(), BorderLayout.CENTER);
		duplicateContentPanel.add(getButtonsPanel(), BorderLayout.EAST);
		
		return duplicateContentPanel;
	}
	
	/**
	 * Returns the Scroll Pane for the DuplicateContentTable.
	 */
	private JScrollPane getScrollPane() {
		JScrollPane	scrollPane = new JScrollPane();
		
		scrollPane.getViewport().add(getJDuplicateContentTable());
	
		return scrollPane;
	}
	
	/**
	 * Sets the data for the Duplicate Content table.
	 * 
	 * @param data
	 *            - The data to be displayed in the Duplicate Content table.
	 */
	public void setData(Collection<CacheEntry> data) {
		duplicateContentTableModel.setData(data);
	}

	/**
	 * Initializes and returns the RequestResponseTable.
	 */
	public DataTable<CacheEntry> getJDuplicateContentTable() {
		if (duplicateContentTable == null) {
			duplicateContentTable = new DataTable<CacheEntry>(duplicateContentTableModel);
			duplicateContentTable.setAutoCreateRowSorter(true);
			duplicateContentTable.setGridColor(Color.LIGHT_GRAY);
			duplicateContentTable.addMouseListener(this);
			duplicateContentTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				
				@Override
				public void valueChanged(ListSelectionEvent listEvent) {
					CacheEntry entry = duplicateContentTable.getSelectedItem();
					HttpRequestResponseInfo rrInfo = entry != null ? entry.getResponse() : null;
					boolean enabled = rrInfo != null && rrInfo.getContentLength() > 0 && rrInfo.getDirection() == HttpDirection.RESPONSE && entry.getSession()!=null;
					getViewBtn().setEnabled(enabled);
					getSaveBtn().setEnabled(enabled);
				}
			});
		}
		
		return duplicateContentTable;
	}
	
	/**
	 * Returns the panel that contains the view and save button.
	 */
	private JPanel getButtonsPanel() {
	
		JPanel	buttonsPanel = new JPanel(new GridBagLayout());

		JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
		panel.add(getViewBtn());
		panel.add(getSaveBtn());

		buttonsPanel.add(panel,
					new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH,
							GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		return buttonsPanel;
	}
	
	/**
	 * Returns the view button.
	 */
	private JButton getViewBtn() {
		if(viewBtn == null){
			viewBtn = new JButton(ResourceBundleHelper.getMessageString("button.View"));
			viewBtn.setEnabled(false);
			viewBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						
						try{
							CacheEntry cEntry = duplicateContentTable.getSelectedItem();							
							if(cEntry != null){
								if(!cEntry.getResponse().getContentType().contains("video")){
									if(cEntry.getResponse().getRawSize() < 5242880){
										ContentViewer.getInstance().viewContent(cEntry.getSession(), cEntry.getResponse());
									} else {
									MessageDialogFactory.getInstance().showErrorDialog(new Window(new Frame()), ResourceBundleHelper.getMessageString("Error.fileSize"));
									}
								} else {
									MessageDialogFactory.getInstance().showErrorDialog(new Window(new Frame()), MessageFormat.format(ResourceBundleHelper.getMessageString("Error.videofile"), cEntry.getResponse().getContentType()));
								}
							} 
							
						} catch (Exception ioExp){
						 	MessageDialogFactory.showMessageDialog(DuplicateContentTablePanel.this.getTopLevelAncestor(), ioExp);
							
						}
					}
				});
		}
		return viewBtn;
	}
	
	public Collection<CacheEntry> getDublicateContentTableData(){
		List<CacheEntry> dupContent = new ArrayList<CacheEntry>();
		if(traceDataModel != null && traceDataModel.getAnalyzerResult()!= null){
			dupContent = traceDataModel.getAnalyzerResult().getCacheAnalysis().getDuplicateContentWithOriginals();
		}
		
		return dupContent;
	}
	
	/**
	 * Returns the save button.
	 */
	private JButton getSaveBtn() {
		if(saveBtn == null){
			saveBtn = new JButton(ResourceBundleHelper.getMessageString("button.Save"));
			saveBtn.setEnabled(false);
			saveBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						CacheEntry entry = duplicateContentTable.getSelectedItem();
						if(entry != null){
							ContentViewer.getInstance().saveContent(DuplicateContentTablePanel.this.getTopLevelAncestor(), entry.getSession(), entry.getResponse());
						}
					}
				});
			}
		return saveBtn;
	}
	
	public void refresh(AROTraceData aModel){
		traceDataModel = aModel;
		setData(getDublicateContentTableData());
	}

	public void setHighlightedDuplicate(CacheEntry duplicateEntry) {
		duplicateContentTable.selectItem(duplicateEntry);
	}

 
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent event) {
		if(event.getClickCount()==2){
 			if (event.getSource() instanceof JTable){
				int selectionIndex = ((JTable)event.getSource()).getSelectedRow();
				int original = ((JTable)event.getSource()).convertRowIndexToModel(selectionIndex);

				logger.info("selectionIndex: "+ selectionIndex);
				if(selectionIndex!=-1){
					CacheEntry cacheEntry = duplicateContentTableModel.getValueAt(original);
					overviewTab.updateDiagnosticsTab(cacheEntry);
				}
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
