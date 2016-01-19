package com.att.aro.ui.view.diagnostictab;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import com.att.aro.ui.model.DataTable;
import com.att.aro.ui.model.DataTableModel;
import com.att.aro.ui.model.diagnostic.TCPUDPFlowsTableModel;
/**
 * Added this class for adding the check boxes for existing TCP flow table.  
 * @author hy0910
 *
 * @param <T>
 */
public class TCPFlowsDataTable<T> extends DataTable<T> 
 {

	private static final long serialVersionUID = 1L;
	
	private JTable table;  
	CheckBoxHeader rendererComponent;
	private MyItemListener it;
	
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

	      DataTableModel<T> dataModel = getDataTableModel(); 
	      TCPUDPFlowsTableModel tcpmodel = (TCPUDPFlowsTableModel)dataModel; //get the specific model for this table
	      for(int x = 0;  x < table.getRowCount(); x++)     
	      {	  	    	  
	    	  tcpmodel.setValueAt(checked, x, 1);
	    	  tcpmodel.fireTableCellUpdated(x, 1);
	      }
	      
	      
	    }     
	  }  
	 

	/**
	 * Method is for return the select rows. Used this from table common table listner. 
	 * @param column
	 * @return
	 */
	public List<T> getSelectedCheckboxRows(int column){
		
		List<T> selectedRows = new ArrayList<T>();
		DataTableModel<T> dataModel = getDataTableModel();
		//Condition avoid index out of bonds when we reopen the trace 
		if(dataModel.getRowCount() == table.getRowCount()){ 
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
		
		  ((JCheckBox)rendererComponent).setSelected(true);
		  table.getTableHeader().repaint();  
	  }

	
 }
