/*
 *  Copyright 2015 AT&T
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
package com.att.aro.core.datacollector;

import java.util.List;

import org.springframework.context.ApplicationContext;

/**
 * convenient class to manage data collectors.
 * 
 * @author Borey Sao February 14, 2015
 */
public interface IDataCollectorManager {

	/**
	 * Returns a list of available collectors
	 * 
	 * @param context
	 *            - application context
	 * @return List of collectors
	 */
	List<IDataCollector> getAvailableCollectors(ApplicationContext context);

	/**
	 * Returns the iOS collector
	 * 
	 * @return the iOS collector
	 */
	IDataCollector getIOSCollector();

	/**
	 * Returns the Android rooted collector
	 * 
	 * @return the Android rooted collector
	 */
	IDataCollector getRootedDataCollector();

	/**
	 * Returns the Android non rooted collector
	 * 
	 * @return the Android non rooted collector
	 */
	IDataCollector getNorootedDataCollector();
}
