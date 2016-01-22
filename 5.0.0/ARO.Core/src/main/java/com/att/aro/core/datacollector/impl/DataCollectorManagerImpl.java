/**
 *  Copyright 2016 AT&T
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
package com.att.aro.core.datacollector.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.att.aro.core.datacollector.DataCollectorType;
import com.att.aro.core.datacollector.IDataCollector;
import com.att.aro.core.datacollector.IDataCollectorManager;

public class DataCollectorManagerImpl implements IDataCollectorManager {

	private List<IDataCollector> plugins;
	private IDataCollector iOSCollector;
	private IDataCollector norootedDataCollector;
	private IDataCollector rootedDataCollector;

	public DataCollectorManagerImpl() {
	}

	@Autowired
	public void setPlugins(List<IDataCollector> plugins) {
		this.plugins = plugins;
		initCollector();
	}

	public DataCollectorManagerImpl(List<IDataCollector> plugins) {
		this.plugins = plugins;
		initCollector();
	}

	private void removeDefaultCollector() {
		int index = -1;
		for (int i = 0; i < plugins.size(); i++) {
			IDataCollector coll = plugins.get(i);
			if (coll.getType() == DataCollectorType.DEFAULT) {
				index = i;
				break;
			}
		}
		if (index > -1) {
			plugins.remove(index);
		}
	}

	private void initCollector() {

		if (this.plugins != null) {
			this.removeDefaultCollector();
			for (int i = 0; i < plugins.size(); i++) {
				IDataCollector coll = plugins.get(i);
				if (coll.getType() == DataCollectorType.IOS) {
					this.iOSCollector = plugins.get(i);
				} else if (coll.getType() == DataCollectorType.NON_ROOTED_ANDROID) {
					this.norootedDataCollector = plugins.get(i);
				} else if (coll.getType() == DataCollectorType.ROOTED_ANDROID) {
					this.rootedDataCollector = plugins.get(i);
				}
			}
		}
	}

	/**
	 * Returns a list of available collectors
	 * 
	 * @param application
	 *            context
	 * @return List of collectors
	 */
	@Override
	public List<IDataCollector> getAvailableCollectors(ApplicationContext context) {
		
		String[] list = context.getBeanNamesForType(IDataCollector.class);
		if (list != null && list.length > 0) {
			List<IDataCollector> collist = new ArrayList<IDataCollector>();
			for (String name : list) {
				IDataCollector coll = (IDataCollector) context.getBean(name);
				if (coll != null) {
					collist.add(coll);
				}
			}
			if (!collist.isEmpty()) {
				this.setPlugins(collist);
			}
		}
		return plugins;
	}

	/**
	 * Returns the iOS collector
	 * 
	 * @return the iOS collector
	 */
	@Override
	public IDataCollector getIOSCollector() {
		return iOSCollector;
	}

	/**
	 * Returns the Android rooted collector
	 * 
	 * @return the Android rooted collector
	 */
	@Override
	public IDataCollector getRootedDataCollector() {
		return rootedDataCollector;
	}

	/**
	 * Returns the Android non rooted collector
	 * 
	 * @return the Android non rooted collector
	 */
	@Override
	public IDataCollector getNorootedDataCollector() {
		return norootedDataCollector;
	}

}
