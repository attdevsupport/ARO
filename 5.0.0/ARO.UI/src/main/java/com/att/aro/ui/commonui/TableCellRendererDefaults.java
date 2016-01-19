/*
 *  Copyright 2012 AT&T
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
package com.att.aro.ui.commonui;

import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * A static class that defines the defaults used for rendering table cells.
 */
public final class TableCellRendererDefaults extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;

	private static Border focusBorder = UIManager
			.getBorder("Table.focusCellHighlightBorder");

	/**
	 * Returns the non focus border object.
	 * 
	 * @return The non focus border object.
	 */
	public static Border getNoFocusBorder() {
		return noFocusBorder;
	}

	/**
	 * Returns the focus border object.
	 * 
	 * @return The focus border object.
	 */
	public static Border getFocusBorder() {
		return focusBorder;
	}

	/**
	 * Static access only
	 */
	private TableCellRendererDefaults() {
	}
}
