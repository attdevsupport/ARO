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
package com.att.aro.ui.view.bestpracticestab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;

import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.AROUIManager;
import com.att.aro.ui.commonui.IARODiagnosticsOverviewRoute;
import com.att.aro.ui.commonui.IAROExpandable;
import com.att.aro.ui.commonui.TabPanelJPanel;
import com.att.aro.ui.model.DataTable;
import com.att.aro.ui.model.DataTableModel;
import com.att.aro.ui.utils.ResourceBundleHelper;


public abstract class AbstractBpDetailTablePanel extends TabPanelJPanel implements IAROExpandable, MouseListener {
//	private static final Logger logger = Logger.getLogger(AbstractBpDetailTablePanel.class.getName()); 

	@Override
	public void refresh(AROTraceData analyzerResult) {
		// TODO Auto-generated method stub
		
	}

	private static final long serialVersionUID = 1L;

	final static int ROW_HEIGHT = 20;
	final static int MINIMUM_ROWS = 5;

	final String zoomOut = ResourceBundleHelper.getMessageString("button.ZoomOut"); // "+"
	final String zoomIn = ResourceBundleHelper.getMessageString("button.ZoomIn");	// "-"

	private JButton zoomBtn;
	private IARODiagnosticsOverviewRoute diagnosticsOverviewRoute;
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	@SuppressWarnings("rawtypes")
	DataTableModel tableModel;
	@SuppressWarnings("rawtypes")
	DataTable contentTable;
	
	int noOfRecords;

	private JPanel contentPanel;

	private JScrollPane scrollPane;
	
	public AbstractBpDetailTablePanel() {
		initTableModel();
		setLayout(new BorderLayout());
		add(layoutDataPanel(), BorderLayout.CENTER);
	}
	
	/**
	 * Instantiate table model
	 */
	abstract void initTableModel();

	public void addTablePanelRoute(IARODiagnosticsOverviewRoute diagnosticsOverviewRoute) {
		this.diagnosticsOverviewRoute = diagnosticsOverviewRoute;
		getContentTable().addMouseListener(this);
	}

	@Override
	public JPanel layoutDataPanel() {
		
		JPanel layout = new JPanel();
		
		layout.setLayout(new BorderLayout());
		
		layout.add(getContentPanel(), BorderLayout.CENTER);
		
		JPanel contentPanelWidth = new JPanel(new GridLayout(2, 1, 5, 5));
		JPanel contentPanelWidthAdjust = new JPanel(new GridBagLayout());
		contentPanelWidthAdjust.add(contentPanelWidth
				, new GridBagConstraints(
						0, 0
						, 1, 1
						, 1.0, 1.0
						, GridBagConstraints.NORTH
						, GridBagConstraints.NONE
						, new Insets(5, 5, 5, 0)
						, 0, 0));
		contentPanelWidthAdjust.setBackground(Color.BLUE);
		layout.add(contentPanelWidthAdjust, BorderLayout.EAST);
		
		return layout;
	}

	/**
	 * Initializes and returns the DuplicateContentPanel.
	 */
	private JPanel getContentPanel() {
		if (this.contentPanel == null) {
			this.contentPanel = new JPanel(new BorderLayout());
			this.contentPanel.add(getScrollPane(), BorderLayout.CENTER);
			this.contentPanel.add(getButtonsPanel(), BorderLayout.EAST);
		}
		return this.contentPanel;}

	/**
	 * Returns the panel that contains the view and save button.
	 */
	private JPanel getButtonsPanel() {

		JPanel bpButtonPanel = new JPanel(new GridBagLayout());

		JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
		
		panel.setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		bpButtonPanel.setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		
		panel.add(getZoomBtn());

		bpButtonPanel.add(panel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

		return bpButtonPanel;
	}

	/**
	 * Returns the zoom button.
	 */
	private JButton getZoomBtn() {
		//TODO		
		zoomBtn = new JButton(zoomOut);
		zoomBtn.setEnabled(false);
		zoomBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String val = zoomBtn.getText();
				if (val.equals(zoomOut)) {
					zoomBtn.setText(zoomIn);
					setScrollSize(tableModel.getRowCount());
				} else {
					zoomBtn.setText(zoomOut);
					setScrollSize(MINIMUM_ROWS);
				}
			}

		});

		return zoomBtn;
	}
	
	/**
	 * 
	 */
	void autoSetZoomBtn(){
		zoomBtn.setEnabled(tableModel.getRowCount() > MINIMUM_ROWS);
	}

	/**
	 * Set preferred height of scrollPane to match requested number of rows of JTable
	 * 
	 * @param scrollHeight 
	 * 
	 */
	void setScrollSize(int scrollHeight) {
		Dimension currentDimensions = scrollPane.getSize();
		double panelWidth = screenSize.getWidth();
		if(panelWidth>500){
			panelWidth = panelWidth-400;
		}
		currentDimensions.setSize(panelWidth, ROW_HEIGHT * (scrollHeight + 1));
		scrollPane.setPreferredSize(currentDimensions);
	}

	/**
	 * Returns the Scroll Pane for the FileCompressionTable.
	 */
	private JScrollPane getScrollPane() {
		if(scrollPane==null){
			scrollPane = new JScrollPane();
			scrollPane.getViewport().add(getContentTable());
		}
		return scrollPane;
	}

	@SuppressWarnings("rawtypes")
	public abstract DataTable getContentTable();

	/**
	 * clicks the "+" button if table needs to expand
	 */
	@Override
	public void expand() {
		if (zoomBtn.getText().equals(zoomOut) && tableModel.getRowCount() >= MINIMUM_ROWS) {
			zoomBtn.doClick();
			this.scrollPane.revalidate();
		}
	
	}


	@Override
	public void mousePressed(MouseEvent event) {
		if (diagnosticsOverviewRoute != null && event.getClickCount() == 2) {
 			if (event.getSource() instanceof JTable){
				int selectionIndex = ((JTable)event.getSource()).getSelectedRow();
				int original = ((JTable)event.getSource()).convertRowIndexToModel(selectionIndex);
//				logger.info("BP selectionIndex: "+ selectionIndex);
//				logger.info("Table Model: "+ tableModel);
				if(selectionIndex > -1){
					diagnosticsOverviewRoute.route(tableModel,
							tableModel.getValueAt(original));
				}
			}
		}
	}
	@Override
	public void mouseClicked(MouseEvent event) {
	}
	@Override
	public void mouseReleased(MouseEvent event) {
	}
	@Override
	public void mouseEntered(MouseEvent event) {
	}
	@Override
	public void mouseExited(MouseEvent event) {
	}

}
