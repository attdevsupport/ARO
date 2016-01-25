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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;

import com.att.aro.ui.utils.CommonHelper;

/**
 * Generates percentage labels for the charts that are displayed on the Overview
 * tab. 
 */
public class PercentLabelGenerator extends StandardCategoryItemLabelGenerator {
	private static final long serialVersionUID = 1L;
	private NumberFormat formatter = new DecimalFormat("0.0");

	/**
	 * Returns a percentage label string using the data in the specified row and
	 * column of the dataset.
	 * 
	 * @param dataset
	 *            The dataset containing the data for the chart.
	 * 
	 * @param row
	 *            The row of data in the dataset.
	 * 
	 * @param column
	 *            The column of data in the dataset.
	 * 
	 * @return A string that is the percentage label.
	 */
	@Override
	public String generateLabel(CategoryDataset dataset, int row, int column) {
		Number value = dataset.getValue(row, column);
		String gLabel = "";
		if(CommonHelper.isNotNull(value)){
			gLabel = formatter.format(value.doubleValue());
		}
		return gLabel;
	}
}

