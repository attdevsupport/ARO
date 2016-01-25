/**
 * 
 */
package com.att.aro.commonui;

import java.awt.Component;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;


/**
 * Adding the checkbox at the heading 
 * @author hy0910
 *
 */
public class CheckBoxHeader extends JCheckBox  
							implements TableCellRenderer, MouseListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3729190165623928780L;
	protected CheckBoxHeader rendererComponent;     
	protected int column;     
	protected boolean mousePressed = false;
	private boolean headerSlection = true;
	
	public CheckBoxHeader(){
		super();
		init();
	}
	
	public CheckBoxHeader(ItemListener itemListener) {     
		setSelected(headerSlection);
		rendererComponent = this;     
		rendererComponent.addItemListener(itemListener);     
	}     
	public Component getTableCellRendererComponent(JTable table, Object value,     
			boolean isSelected, boolean hasFocus, int row, int column) {     
		if (table != null) {     
		  JTableHeader header = table.getTableHeader();     
		  if (header != null) {     
		    rendererComponent.setForeground(header.getForeground());     
		    rendererComponent.setBackground(header.getBackground());     
		    rendererComponent.setFont(header.getFont());     
		    header.addMouseListener(rendererComponent);     
		  }     
		}     

		setColumn(column); //For enable the header check box     
		setHorizontalAlignment(SwingConstants.CENTER);
		return rendererComponent;     
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
	  mousePressed=false;     
	  JTableHeader header = (JTableHeader)(e.getSource());     
	  JTable tableView = header.getTable();     
	  TableColumnModel columnModel = tableView.getColumnModel();     
	  int viewColumn = columnModel.getColumnIndexAtX(e.getX());     
	  int column = tableView.convertColumnIndexToModel(viewColumn);     
	  if (viewColumn == this.column && e.getClickCount() == 1 && column != -1) {     
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
