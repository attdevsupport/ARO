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
