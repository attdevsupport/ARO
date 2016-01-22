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

package com.att.aro.core.settings.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Properties;

import com.att.aro.core.ILogger;
import com.att.aro.core.exception.AROInvalidAttributeException;
import com.att.aro.core.exception.ARORuntimeException;
import com.att.aro.core.impl.LoggerImpl;
import com.att.aro.core.settings.ISettings;

/**
 * Generic Configuration File implementation - assumes the configuration file is a .properties
 * file.  Supports optional specification of allowed attributes.
 */

public class SettingsImpl implements ISettings {
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
	static final String CONFIG_FILE_PATH = System.getProperty("user.home") +
			FILE_SEPARATOR + "AroLibrary" + FILE_SEPARATOR + "config.properties";

	private final ILogger logger = new LoggerImpl(SettingsImpl.class.getName());

	private final String currentConfigFilePath;

	final Properties configProperties;
	final EnumSet<? extends Enum<?>> validAttributes;

	/**
	 * Entry point to specify configuration .properties file
	 * 
	 * @param CONFIG_FILE_PATH
	 * @param validAttributes enum of valid attribute names (not enforced if null)
	 */
	public SettingsImpl(String configFilePath, EnumSet<?> validAttributes) {
		this.validAttributes = validAttributes;
		currentConfigFilePath = configFilePath;
		configProperties = loadProperties(configFilePath);
	}

	/**
	 * Entry point to specify configuration .properties file
	 * 
	 * @param CONFIG_FILE_PATH
	 */
	public SettingsImpl(String configFilePath) {
		this(configFilePath, (EnumSet<?>) null);
	}

	/**
	 * 
	 * @param validAttributes enum of valid attribute names (not enforced if null)
	 */
	public SettingsImpl(EnumSet<?> validAttributes) {
		this(CONFIG_FILE_PATH, validAttributes);
	}

	public SettingsImpl() {
		this((EnumSet<?>) null);
	}


	private void createConfigFile(File configFile) {
		File parent = configFile.getParentFile();
		if (parent != null) {
			parent.mkdirs();
			try {
				new FileWriter(configFile);
			} catch (IOException e) {
				throw new ARORuntimeException("Could not create config file: " +
						e.getLocalizedMessage(), e);
			}
		}
	}

	private Properties loadProperties(String configFilePath) {
		File configFile = new File(configFilePath);
		logger.debug("Reading properties from: " + configFilePath);

		if (!configFile.exists()) {
			createConfigFile(configFile);
		}

		Properties configProperties = new Properties();
		try {
			FileReader configReader = new FileReader(configFilePath);
			configProperties.load(configReader);
			configReader.close();
		} catch (FileNotFoundException e) {
			throw new ARORuntimeException(
				"Could not find config file file (real problem - should never happen): " +
					e.getLocalizedMessage(), e);
		} catch (IOException e) {
			throw new ARORuntimeException(
				"Could not read config file: " + e.getLocalizedMessage(), e);
		}
		logger.debug(getPropertiesReadString(configProperties));
		return configProperties;
	}

	@Override
	public String getAttribute(String name) {
		String propertyValue = configProperties.getProperty(name);
		logger.debug("Value of property " + name + ": " + propertyValue);
		return propertyValue;
	}
	private void checkForValidAttribute(String name) {
		if (validAttributes != null) {
			boolean valid = false;
			for (Enum<?> currentAttribute : validAttributes) {
				if (name.equals(currentAttribute.name())) {
					valid = true;
					break;
				}
			}
			if (!valid) {
				throw new AROInvalidAttributeException("Attribute '" + name + "' invalid\n" +
						"(specified by " + validAttributes.getClass().getCanonicalName() + ")");
			}
		}
	}
	@Override
	public String setAttribute(String name, String value) {
		checkForValidAttribute(name);
		logger.debug("Replacing property " + name + " with " + value);
		return (String) configProperties.setProperty(name, value);
	}
	@Override
	public String removeAttribute(String name) {
		checkForValidAttribute(name);
		logger.debug("Removing property " + name);
		return (String) configProperties.remove(name);
	}
	@Override
	public String setAndSaveAttribute(String name, String value) {
		String attribute = setAttribute(name, value);
		saveConfigFile();
		return attribute;
	}
	@Override
	public String removeAndSaveAttribute(String name) {
		String attribute = removeAttribute(name);
		saveConfigFile();
		return attribute;
	}

	@Override
	public void saveConfigFile() {
		try {
			FileWriter writer = new FileWriter(currentConfigFilePath);
			logger.debug(getPropertiesReadString());
			logger.debug("Persisting properties to: " + currentConfigFilePath);
			configProperties.store(writer, null);
			writer.close();
		} catch (IOException e) {
			throw new ARORuntimeException("Could not save config file: " +
					e.getLocalizedMessage(), e);
		}
	}

	private String getPropertiesReadString(Properties configProperties) {
		StringBuilder propertiesString = new StringBuilder("current properties:\n");
		for (Enumeration<?> iter = configProperties.propertyNames(); iter.hasMoreElements();) {
			String propertyName = (String) iter.nextElement();
			propertiesString.append(propertyName + " = " +
					configProperties.getProperty(propertyName) + "\n");
		}
		return propertiesString.toString();
	}
	private String getPropertiesReadString() {
		return getPropertiesReadString(configProperties);
	}
}
