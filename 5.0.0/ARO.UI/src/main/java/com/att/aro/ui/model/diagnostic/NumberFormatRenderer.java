package com.att.aro.ui.model.diagnostic;

import java.awt.Component;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JTable;

public class NumberFormatRenderer extends DataTableCellRenderer {
	private static final long serialVersionUID = 1L;

	private NumberFormat formatter = new DecimalFormat("0.0");

	public NumberFormatRenderer() {
		super();
	}

	public NumberFormatRenderer(NumberFormat formatter) {
		super();
		this.formatter = formatter == null ? this.formatter : formatter;
		
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		return super.getTableCellRendererComponent(table,
				value == null?null:formatter.format(value), isSelected,
				hasFocus, row, column);
	}
}
