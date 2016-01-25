package com.att.aro.commonui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;


/**
 * Added this class for adding the check boxes for existing TCP flow table.  
 * @author hy0910
 *
 * @param <T>
 */
public class TCPFlowsDataTable<T> extends DataTable<T>{
	
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	JTable table;  
	private boolean headerValue = false;
	CheckBoxHeader rendererComponent;
	private MyItemListener it;

	/**
	 * USing Item listner for the Header check box for select ALL and deselect ALL
	 * @author hy0910
	 *
	 */
	 class MyItemListener implements ItemListener     
	  {     
	    public void itemStateChanged(ItemEvent e) {     
	      Object source = e.getSource();     
	      if (source instanceof AbstractButton == false) return;     
	      boolean checked = e.getStateChange() == ItemEvent.SELECTED;     

	      DataTableModel<T> dataModel = getDataTableModel(); //for updating the model
	      for(int x = 0;  x < table.getRowCount(); x++)     
	      {
	    	  dataModel.setValueAt(checked, x, 1);
	    	  
  	
	    	  dataModel.fireTableCellUpdated(x, 1);
	      }
	      
	      
	    }     
	  }  
	 
	
	/**
	 * Initializes a new instance of an empty DataTableModel class.
	 * 
	 * @param columns
	 *            An array of java.lang.String objects that are the columns in
	 *            the data table.
	 */
	public TCPFlowsDataTable(DataTableModel<T> dm){
		super(dm);
		table = this;
		TableColumn tc = table.getColumnModel().getColumn(1);     
		 it = new MyItemListener();
		CheckBoxHeader checkBoxHeader = new CheckBoxHeader(it);
		tc.setHeaderRenderer(checkBoxHeader); 
		setDefaultRenderer(Boolean.class, checkBoxHeader);
		this.rendererComponent = checkBoxHeader;
		table.addMouseListener(new MyMouseListener());
		
		
	}
	
	/**
	 * Returns a default table header for the DataTable.
	 * 
	 * @return A JTableHeader object with default properties.
	 */
/*	@Override
	public JTableHeader createDefaultTableHeader() {
		return new JTableHeader(columnModel) {
			private static final long serialVersionUID = 1L;

			@Override
			public String getToolTipText(MouseEvent e) {
				int column = columnAtPoint(e.getPoint());

				// Locate the renderer under the event location
				if (column != -1) {
					TableColumn aColumn = columnModel.getColumn(column);
					Object tip = aColumn.getHeaderValue();
					if (tip != null) {
						return tip.toString();
					}
				}
				return null;
			}
			
		};
	}
*/

	/**
	 * Method is for return the select rows. Used this from table common table listner. 
	 * @param column
	 * @return
	 */
	public List<T> getSelectedCheckboxRows(int column){
		
		List<T> selectedRows = new ArrayList<T>();
		DataTableModel<T> dataModel = getDataTableModel();
		if(dataModel.getRowCount() == table.getRowCount()){ //Condition avoid index out of bonds when we reopen the trace 
			for (int i = 0; i<dataModel.getRowCount(); i++){
				
				if((Boolean)dataModel.getValueAt(i, 1)){
					selectedRows.add(dataModel.getValueAt(convertRowIndexToModel(i)));
				}
			}
		} 
		
		return selectedRows;
	}
	
	/**
	 * Returns header check box component
	 * 
	 * @param value
	 * @return
	 */
	private JCheckBox getComponent(boolean value) {
		return (JCheckBox) table.getTableHeader().getColumnModel().getColumn(1)
				.getHeaderRenderer().getTableCellRendererComponent(table,
						value, false, false, -1, 1);
	}
	
	/**
	 * select and de-select all the rows 
	 * @author hy0910
	 *
	 */
	  class MyMouseListener extends MouseAdapter{  
	      public void mouseClicked(MouseEvent mouseEvent) {  
	          int checkedCount = 0;  
	          
	          rendererComponent.removeItemListener(it);  
	          
	          if (rendererComponent instanceof JCheckBox) {  
	              boolean[] flags = new boolean[table.getRowCount()];  
	              for (int i = 0; i < table.getRowCount(); i++) {  
	                  flags[i] = ((Boolean) table.getValueAt(i, 1)).booleanValue();  
	                  if(flags[i]){  
	                      checkedCount++;  
	                  }  
	              }  
	              if(checkedCount== table.getRowCount()){  
	                  ((JCheckBox)rendererComponent).setSelected(true);                 
	              }  
	              if(checkedCount!= table.getRowCount()){  
	                  ((JCheckBox)rendererComponent).setSelected(false);      
	              }  
	          }  
	          rendererComponent.addItemListener(it);  
	          table.getTableHeader().repaint();  
	      }  
	  }
	  
	  public void setHeaderDefaultValue(){
		
//		  System.out.println("Inside the setting default Value");
		  ((JCheckBox)rendererComponent).setSelected(true);
		  table.getTableHeader().repaint();  
	  }
	
	
}
