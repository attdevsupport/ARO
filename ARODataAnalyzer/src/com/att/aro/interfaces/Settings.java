/*
 * Copyright 2012 AT&T
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
package com.att.aro.interfaces;

import java.io.IOException;

public interface Settings {

	/**
	 * Add new or update setting item. Call Save() to save all data to the file.
	 * @param key
	 * @param value
	 */
	public abstract void addNewOrUpdate(String key, String value);

	/**
	 * get a setting property by key
	 * @param key
	 * @return null if not found or a String
	 */
	public abstract String getProperty(String key);

	/**
	 * Save all data back to config.properties file
	 * @throws IOException
	 */
	public abstract void save() throws IOException;

}