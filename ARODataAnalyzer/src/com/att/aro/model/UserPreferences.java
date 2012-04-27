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
 * User Preferences to handle the Preferences to store the default values such
 * as profile details, visible plots and default directory path.
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
	 * Returns the UserPreferences instance which contains the user settings for
	 * the Analyzer.
	 * 
	 * @return The UserPreferences instance.
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
	 * Gets the directory from where the last trace was loaded
	 * 
	 * @return The trace directory selected by the user.
	 */
	public File getLastTraceDirectory() {
		String path = prefs.get(TD_PATH, null);
		return path != null ? new File(path) : null;
	}

	/**
	 * Sets the directory from where the last trace was loaded
	 * 
	 * @param tdPath
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
	 * Gets either the pre-defined profile name or absolute path to file which
	 * contains the last profile used of the specified type.
	 * 
	 * @param profileType
	 *            Specifies profile type to get. If null, last profile of any
	 *            type is returned.
	 * @return The profile name
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
	 * Sets either the pre-defined profile name or absolute path to file which
	 * contains profile data
	 * 
	 * @param profile
	 *            The last used profile to be stored in user preferences
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
	 * Gets the directory from where the last profile was loaded or saved
	 * 
	 * @return The profile directory.
	 */ 
	public File getLastProfileDirectory() {
		String path = prefs.get(PROFILE_PATH, null);
		return path != null ? new File(path) : null;
	}

	/**
	 * Sets the directory where user device profiles have last been stored
	 * 
	 * @param profilePath
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
	 * @param chartPlotOptions
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
	 * Retrieve the list of chart plot options.
	 * 
	 * @return The list of chart options.
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
	 * @return The last profile exported directory. 
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
	 * @param f
	 *            if is a file, the parent directory is used. If null, the
	 *            preference is cleared.
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
