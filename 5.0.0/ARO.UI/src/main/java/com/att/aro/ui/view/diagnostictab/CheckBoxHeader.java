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
package com.att.aro.ui.view.diagnostictab;

import java.awt.Component;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class CheckBoxHeader extends JCheckBox implements TableCellRenderer, MouseListener{
 
	private static final long serialVersionUID = 3729190165623928780L;
//	protected CheckBoxHeader rendererComponent;     
	protected int column;     
	protected boolean mousePressed = false;
	private boolean headerSlection = true;
	
	public CheckBoxHeader(){
		super();
		init();
	}
	
	public CheckBoxHeader(ItemListener itemListener) {     
		setSelected(headerSlection);
		addItemListener(itemListener);     
	}     
	public Component getTableCellRendererComponent(JTable table, Object value,     
			boolean isSelected, boolean hasFocus, int row, int column) {     
		if (table != null) {     
		  JTableHeader header = table.getTableHeader();     
		  if (header != null) {     
		    setForeground(header.getForeground());     
		    setBackground(header.getBackground());     
		    setFont(header.getFont());     
		    header.addMouseListener(this);     
		  }     
		}     

		setColumn(column); //For enable the header check box     
		setHorizontalAlignment(SwingConstants.CENTER);
		return this;     
	}     
	protected void setColumn(int column) {     
		this.column = column;     
	}     
	public int getColumn() {     
		return column;     
	}     
	
	/**
	 * This method is used to give the checkboxes common default settings. Of
	 * course, these settings can be changed.
	 */
	private void init() {
		//setHorizontalAlignment(SwingConstants.CENTER);
		setBorderPainted(true);
	}
	
	/**
	 * for handling mouse events 
	 * @param e
	 */
	protected void handleClickEvent(MouseEvent e) {     
		if (mousePressed) {
			mousePressed = false;
			JTableHeader header = (JTableHeader) (e.getSource());
			JTable tableView = header.getTable();
			TableColumnModel columnModel = tableView.getColumnModel();
			int viewColumn = columnModel.getColumnIndexAtX(e.getX());
			int column = tableView.convertColumnIndexToModel(viewColumn);
			if (viewColumn == this.column && e.getClickCount() == 1
					&& column != -1) {
				doClick();
			}
		}    
	}     
	public void mouseClicked(MouseEvent e) {     
		handleClickEvent(e);     
		((JTableHeader)e.getSource()).repaint();     
	}     
	public void mousePressed(MouseEvent e) {     
		mousePressed = true;     
	}     
	public void mouseReleased(MouseEvent e) {     
	}     
	public void mouseEntered(MouseEvent e) {     
	}     
	public void mouseExited(MouseEvent e) {     
	}

}
