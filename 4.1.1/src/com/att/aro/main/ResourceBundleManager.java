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


package com.att.aro.main;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Provides methods for managing resource bundles that are used for localizing
 * strings in the application.
 */
public class ResourceBundleManager {

	/**
	 * The default resource bundle
	 */
	private static ResourceBundle defaultBundle = ResourceBundle
			.getBundle("messages");

	/**
	 * The build resource bundle
	 */
	private static ResourceBundle buildBundle = ResourceBundle
			.getBundle("build");

	/**
	 * The build resource bundle
	 */
	private static ResourceBundle profilesBundle = ResourceBundle
			.getBundle("profiles");

	/**
	 * Returns the default resource bundle used in the application.
	 * 
	 * @return A ResourceBundle object that is the default resource bundle used
	 *         in the application.
	 */
	public static ResourceBundle getDefaultBundle() {
		return defaultBundle;
	}

	/**
	 * Returns the resource bundle containing build information.
	 * 
	 * @return A ResourceBundle object containing the build information.
	 */
	public static ResourceBundle getBuildBundle() {
		return buildBundle;
	}

	/**
	 * Returns the resource bundle that contains information for the pre-defined
	 * device profiles.
	 * 
	 * @return A ResourceBundle that contains the information for the
	 *         pre-defined device profiles.
	 */
	public static ResourceBundle getProfilesBundle() {
		return profilesBundle;
	}

	/**
	 * Returns a string from the resource bundle that corresponds to the
	 * specified enumeration value. If the specified enumeration value does not
	 * have an associated string in the resource bundle, the enumeration name is
	 * returned.
	 * 
	 * @param val
	 *            An enumeration value that specifies which localized display
	 *            string should be returned from the resource bundle.
	 * 
	 * @return A string that corresponds to the enumeration value, or to the
	 *         enumeration name, if the value cannot be found.
	 */
	public static String getEnumString(Enum<?> val) {
		if (val != null) {
			try {
				return defaultBundle.getString(val.getClass().getSimpleName()
						+ "." + val);
			} catch (MissingResourceException mre) {
				return val.name();
			}
		} else {
			return null;
		}
	}

	/**
	 * Returns a Map of strings that correspond to the specified enumeration
	 * value.
	 * 
	 * @param c
	 *            - The enumeration type for which the corresponding Map of
	 *            display strings should be returned.
	 * 
	 * @return A Map of displayable strings for each value within the specified
	 *         enumeration.
	 */
	public static <T extends Enum<T>> Map<T, String> getEnumStrings(Class<T> c) {
		Map<T, String> result = new EnumMap<T, String>(c);
		for (T t : c.getEnumConstants()) {
			result.put(t, getEnumString(t));
		}
		return result;
	}

	/**
	 * Returns a Map of display strings that correspond to each value in the
	 * specified enumeration type. This Map is essentially a reversed Map of the
	 * one returned by the getEnumStrings() method, and is useful for converting
	 * a user entry into an enumeration value. To get accurate results for this
	 * Map, the String value returned for each value in the enumeration must be
	 * unique.
	 * 
	 * @param c
	 *            - The enumeration class for which the corresponding Map of
	 *            display strings should be returned.
	 * 
	 * @return A Map of displayable strings that correspond to each value in the
	 *         specified enumeration class.
	 */
	public static <T extends Enum<T>> Map<String, T> getStringEnumMap(Class<T> c) {
		T[] constants = c.getEnumConstants();
		Map<String, T> result = new LinkedHashMap<String, T>(constants.length);
		for (T t : constants) {
			result.put(getEnumString(t), t);
		}
		return result;
	}

}
