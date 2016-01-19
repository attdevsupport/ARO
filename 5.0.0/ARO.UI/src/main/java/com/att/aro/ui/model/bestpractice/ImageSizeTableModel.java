/*
 * Copyright 2013 AT&T
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
package com.att.aro.ui.model.bestpractice;

import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.att.aro.core.bestpractice.pojo.ImageSizeEntry;
import com.att.aro.ui.model.DataTableModel;
import com.att.aro.ui.model.NumberFormatRenderer;
import com.att.aro.ui.utils.ResourceBundleHelper;

/**
 * Represents the data table model for text Image Size result table. This
 * class implements the aro.commonui.DataTableModel class using ImageSizeEntry
 * objects.
 */
public class ImageSizeTableModel extends DataTableModel<ImageSizeEntry> {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(ImageSizeTableModel.class.getName());

	private static final int COL1_MIN = 70;
	private static final int COL1_MAX = 100;
	private static final int COL1_PREF = 70;

	private static final int COL2_MIN = 150;
	private static final int COL2_MAX = 200;
	private static final int COL2_PREF = 200;

	private static final int COL3_MIN = 70;
	private static final int COL3_MAX = 100;
	private static final int COL3_PREF = 70;

	private static final int COL4_MIN = 500;
	private static final int COL4_PREF = 500;

	private static final int COL_1 = 0;
	private static final int COL_2 = 1;
	private static final int COL_3 = 2;
	private static final int COL_4 = 3;
	private static final String[] COLUMNS = { ResourceBundleHelper.getMessageString("imageSize.table.col1"),
											  ResourceBundleHelper.getMessageString("imageSize.table.col2"),
											  ResourceBundleHelper.getMessageString("imageSize.table.col3"),
											  ResourceBundleHelper.getMessageString("imageSize.table.col4") };

	/**
	 * Initializes a new instance of the ImageSizeTableModel.
	 */
	public ImageSizeTableModel() {
		super(ImageSizeTableModel.COLUMNS);
	}

	/**
	 * Returns a TableColumnModel that is based on the default table column
	 * model for the DataTableModel class. The TableColumnModel returned by this
	 * method has the same number of columns in the same order and structure as
	 * the table column model in the DataTableModel. When a DataTable object is
	 * created, this method is used to create the TableColumnModel if one is not
	 * specified. This method may be overridden in order to provide
	 * customizations to the default column model, such as providing a default
	 * column width and/or adding column renderers and editors.
	 * 
	 * @return TableColumnModel object
	 */
	@Override
	public TableColumnModel createDefaultTableColumnModel() {
		TableColumnModel cols = super.createDefaultTableColumnModel();
		TableColumn col;

		col = cols.getColumn(ImageSizeTableModel.COL_1);
		col.setCellRenderer(new NumberFormatRenderer(new DecimalFormat("0.000")));
		col.setMinWidth(ImageSizeTableModel.COL1_MIN);
		col.setPreferredWidth(ImageSizeTableModel.COL1_PREF);
		col.setMaxWidth(ImageSizeTableModel.COL1_MAX);

		col = cols.getColumn(ImageSizeTableModel.COL_2);
		col.setMinWidth(ImageSizeTableModel.COL2_MIN);
		col.setPreferredWidth(ImageSizeTableModel.COL2_PREF);
		col.setMaxWidth(ImageSizeTableModel.COL2_MAX);

		col = cols.getColumn(ImageSizeTableModel.COL_3);
		col.setMinWidth(ImageSizeTableModel.COL3_MIN);
		col.setPreferredWidth(ImageSizeTableModel.COL3_PREF);
		col.setMaxWidth(ImageSizeTableModel.COL3_MAX);

		col = cols.getColumn(ImageSizeTableModel.COL_4);
		col.setMinWidth(ImageSizeTableModel.COL4_MIN);
		col.setPreferredWidth(ImageSizeTableModel.COL4_PREF);

		return cols;
	}

	/**
	 * Returns a class representing the specified column. This method is
	 * primarily used to sort numeric columns.
	 * 
	 * @param columnIndex
	 *            The index of the specified column.
	 * 
	 * @return A class representing the specified column.
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		ImageSizeTableModel.LOGGER.log(Level.FINE, "getColumnClass, idx: {0}", columnIndex);
		switch (columnIndex) {
		case COL_1:
			return Double.class;
		case COL_2:
			return String.class;
		case COL_3:
			return Integer.class;
		case COL_4:
			return String.class;
		default:
			return super.getColumnClass(columnIndex);
		}
	}

	/**
	 * Defines how the data object managed by this table model is mapped to its
	 * columns when displayed in a row of the table.
	 * 
	 * @param item
	 *            An object containing the column information.
	 * @param columnIndex
	 *            The index of the specified column.
	 * 
	 * @return The table column value calculated for the object.
	 */
	@Override
	protected Object getColumnValue(ImageSizeEntry item, int columnIndex) {
		ImageSizeTableModel.LOGGER.log(Level.FINEST, "getColumnValue, idx:{0}", columnIndex);
		switch (columnIndex) {
		case COL_1:
			return item.getTimeStamp();
		case COL_2:
			return item.getHostName();
		case COL_3:
			return item.getImageSize();
		case COL_4:
			return item.getHttpObjectName();
		default:
			return null;
		}
	}

}
