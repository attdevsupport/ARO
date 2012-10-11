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
package com.att.aro.model;

import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;

import com.att.aro.main.ChartPlotOptions;

/**
 * A class used to store user preferences for the Data Analyzer, like profile details, 
 * visible plots, and the default directory path. 
 */
public class UserPreferences {

	private static final String ourNodeName = "/com/att/aro";
	private static UserPreferences instance = new UserPreferences();

	private static final String TD_PATH = "TD_PATH";
	private static final String PROFILE_PATH = "PROFILE_PATH";
	private static final String PROFILE = "PROFILE";
	private static final String PROFILE_3G = "PROFILE_3G";
	private static final String PROFILE_LTE = "PROFILE_LTE";
	private static final String CHART_PLOT_OPTIONS = "CHART_PLOT_OPTIONS";
	private static final String EXPORT_PATH = "EXPORT_PATH";

	private Preferences prefs;

	/**
	 * Gets a static instance of the UserPreferences class.
	 * 
	 * @return A static UserPreferences object.
	 */
	public static UserPreferences getInstance() {
		return instance;
	}

	/**
	 * Private constructor. Use getInstance()
	 */
	private UserPreferences() {
		prefs = Preferences.userRoot().node(ourNodeName);
	}

	/**
	 * Gets the directory from which the last trace was loaded. 
	 * 
	 * @return The directory of the last loaded trace.
	 */
	public File getLastTraceDirectory() {
		String path = prefs.get(TD_PATH, null);
		return path != null ? new File(path) : null;
	}

	/**
	 * Sets the directory where the last trace was loaded from.
	 * 
	 * @param tdPath The absolute path of the last trace directory.
	 */
	public void setLastTraceDirectory(File tdPath) {
		if (tdPath != null && !tdPath.isDirectory()) {
			throw new IllegalArgumentException(
					"Trace directory must be a valid directory: "
							+ tdPath.getAbsolutePath());
		}
		if (tdPath != null) {
			prefs.put(TD_PATH, tdPath.getAbsolutePath());
		} else {
			prefs.remove(TD_PATH);
		}
	}

	/**
	 * Gets either the pre-defined profile name or absolute path to file which
	 * contains profile data
	 * 
	 * @return The used profile.
	 */
	public String getLastProfile() {
		return prefs.get(PROFILE, null);
	}

	/**
	 * Returns either the name or the absolute path to the last used profile of the specified 
	 * type. If no type is specified, the name or path to the last used profile of any type is returned.
	 * 
	 * @param profileType
	 *            Specifies the profile type to return. If null, the last profile of 
	 *            any type is returned. 
	 * @return TThe name of the last profile used.
	 */
	public String getLastProfile(ProfileType profileType) {
		if (profileType != null) {
			switch (profileType) {
			case T3G:
				return prefs.get(PROFILE_3G, null);
			case LTE:
				return prefs.get(PROFILE_LTE, null);
			default:
				return null;
			}
		} else {
			return prefs.get(PROFILE, null);
		}
	}

	/**
	 * Sets the last profile used to the specified profile. 
	 * 
	 * @param profile The profile that is to be set as the last profile.
	 */
	public void setLastProfile(Profile profile) {
		String name = profile != null ? profile.getName() : null;
		if (name != null) {
			prefs.put(PROFILE, name);
			if (profile instanceof Profile3G) {
				prefs.put(PROFILE_3G, name);
			} else if (profile instanceof ProfileLTE) {
				prefs.put(PROFILE_LTE, name);
			}
		}
	}

	/**
	 * Returns the directory from which the last profile was loaded or saved. 
	 * 
	 * @return The last profile directory.
	 */ 
	public File getLastProfileDirectory() {
		String path = prefs.get(PROFILE_PATH, null);
		return path != null ? new File(path) : null;
	}

	/**
	 * Sets the directory where the user device profiles were last stored. 
	 * 
	 * @param profilePath The absolute path of the last profile directory.
	 */
	public void setLastProfileDirectory(File profilePath) {
		if (profilePath != null && !profilePath.isDirectory()) {
			throw new IllegalArgumentException(
					"Profile directory must be a valid directory: "
							+ profilePath.getAbsolutePath());
		}
		prefs.put(PROFILE_PATH,
				profilePath != null ? profilePath.getAbsolutePath() : null);
	}

	/**
	 * Set the list of chart plot options. 
	 * 
	 * @param chartPlotOptions A List of ChartPlotOptions objects containing the user 
	 * configurable list of items to plot on the Diagnostic Chart.
	 */
	public void setChartPlotOptions(List<ChartPlotOptions> chartPlotOptions) {
		if (chartPlotOptions == null) {
			throw new IllegalArgumentException(
					"List of chart plot options must be a non-null object.");
		}
		String optionsString = ChartPlotOptions
				.toUserPrefsListString(chartPlotOptions);
		prefs.put(CHART_PLOT_OPTIONS, optionsString);
	}

	/**
	 * Retrieves the chart plot options. The user configurable list of items to plot on 
	 * the Diagnostic Chart.
	 * 
	 * @return A List of ChartPlotOptions objects containing the information.
	 */
	public List<ChartPlotOptions> getChartPlotOptions() {
		String chartPlotsOptionPrefsString = prefs
				.get(CHART_PLOT_OPTIONS, null);
		List<ChartPlotOptions> list = ChartPlotOptions
				.toUserPrefsList(chartPlotsOptionPrefsString);
		return list == null ? ChartPlotOptions.getDefaultList() : list;
	}

	/**
	 * Returns the directory where the last table export occurred
	 * 
	 * @return The last export directory.
	 */
	public File getLastExportDirectory() {
		String s = prefs.get(EXPORT_PATH, null);
		if (s != null) {
			File f = new File(s);
			if (f.isDirectory()) {
				return f;
			}
		}
		return null;
	}

	/**
	 * Sets the directory where the last table export occurred
	 * 
	 * @param f The last table export directory. If this parameter is a file, the parent 
	 * directory is used. If it is null, the previous user preference is cleared.
	 */
	public void setLastExportDirectory(File f) {
		if (f != null && f.exists()) {
			if (!f.isDirectory()) {
				f = f.getParentFile();
			}
			prefs.put(EXPORT_PATH, f.getAbsolutePath());
		} else {
			prefs.remove(EXPORT_PATH);
		}
	}
}
