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
package com.att.aro.ui.utils;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ResourceBundleHelper {
	
	/**
	 * The default resource bundle
	 */
	
	
	private static ResourceBundle defaultBundle = ResourceBundle
			.getBundle("messages");

	/**
	 * language different can use messages_us, or messages_de,.....
	 * just need to switch the language and didn't switch other things
	 */
	private static ResourceBundle messageBundle = ResourceBundle.getBundle("messages");

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
	 * The images resource bundle
	 */
	private static ResourceBundle imagesBundle = ResourceBundle
			.getBundle("images");

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
	 * only give string, don't need to give bundle
	 * @param key
	 * @return
	 */
	public static String getMessageString(String key){
		return messageBundle.getString(key);
	}

	public static String getKeyFromEnum(Enum<?> enumParm) {
		return enumParm.name().replaceAll("_", ".");
	}

	public static String getMessageString(Enum<?> key) {
		return getMessageString(getKeyFromEnum(key));
	}

	public static String getImageString(String key){
		return imagesBundle.getString(key);
	}
	
	public static String getBuildString(String key){
		return buildBundle.getString(key);
	}
	
	public static String getProfileString(String key){
		return profilesBundle.getString(key);
	}
	
	/** 
	 * @return A ResourceBundle that contains the information for the
	 *         Images.
	 */
	public static ResourceBundle getImagesBundle() {
		return imagesBundle;
	}

	/** 
	 * @return A ResourceBundle that contains the information for the
	 *         Profiles.
	 */
	public static ResourceBundle getProfilesBundle() {
		return profilesBundle;
	}

	/** 
	 * @return A ResourceBundle that contains the information for the
	 *         Messages.
	 */
	public static ResourceBundle getMessagesBundle() {
		return messageBundle;
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
