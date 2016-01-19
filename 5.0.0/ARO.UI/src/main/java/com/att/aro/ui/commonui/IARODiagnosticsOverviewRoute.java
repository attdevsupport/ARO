/*
 * Copyright 2015 AT&T
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

import com.att.aro.core.bestpractice.pojo.BestPracticeType;
import com.att.aro.ui.model.DataTableModel;

/**
 *
 */
public interface IARODiagnosticsOverviewRoute {
	/**
	 * <p>
	 * Interface to route to the Diagnostics Tab based on a passed in type of either:
	 * </p><ol>
	 * 	<li>CacheEntry</li>
	 * 	<li>Session</li>
	 * 	<li>HttpResponseInfo</li>
	 * </ol>
	 * 
	 * @param cacheEntryOrSessionOrHttpResponseInfo
	 */
	void updateDiagnosticsTab(Object cacheEntryOrSessionOrHttpResponseInfo);
	/**
	 * Returns which JPanel index <em>tableModel</em> would route to (-1 == none).
	 * 
	 * @param tableModel
	 * @return
	 */
	int getRoutedJTabbedPaneIndex(DataTableModel<? extends DataTableModel<?>> tableModel);
	/**
	 * <p>
	 * Interface to route to the Diagnostics or Overview Tab based on the column name of
	 * <em>tableModel</em>.
	 * 
	 * @param tableModel
	 * @param cacheEntryOrSessionOrHttpResponseInfo
	 * @return the tab index of where the request was routed (-1 == nowhere).
	 */
	public int route(DataTableModel<? extends DataTableModel<?>> tableModel,
		Object cacheEntryOrSessionOrHttpResponseInfo);	/**
	 * <p>
	 * Interface to route to the another Tab based on clicking a hyperlink 
	 * 
	 * @param bpType
	 */
	public void routeHyperlink(BestPracticeType bpType);
}
