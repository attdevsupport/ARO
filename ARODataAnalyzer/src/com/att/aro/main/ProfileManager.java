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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.att.aro.model.Profile;
import com.att.aro.model.Profile3G;
import com.att.aro.model.ProfileException;
import com.att.aro.model.ProfileLTE;
import com.att.aro.model.ProfileType;
import com.att.aro.model.UserPreferences;

/**
 * Manages the saving and retrieving of device profiles.
 */
public class ProfileManager {

	private static final String DEFAULT_PROFILE = "i997";
	private static final String DEFAULT_PROFILE_LTE = "lte";
	private static final Logger logger = Logger.getLogger(ProfileManager.class
			.getName());
	private static final ProfileManager instance = new ProfileManager();

	// Create map of pre-defined profile names to file name where profile info
	// is stored
	private ResourceBundle aroProfiles = ResourceBundleManager
			.getProfilesBundle();
	private SortedMap<String, String> predefinedProfiles = new TreeMap<String, String>(
			String.CASE_INSENSITIVE_ORDER);

	/**
	 * Private constructor. Use getInstance()
	 */
	private ProfileManager() {
		super();
		for (String key : aroProfiles.keySet()) {
			predefinedProfiles.put(aroProfiles.getString(key), key + ".conf");
		}
	}

	/**
	 * Returns a static instance of the ProfileManager.
	 * 
	 * @return The ProfileManager instance.
	 */
	public static ProfileManager getInstance() {
		return instance;
	}

	/**
	 * Returns the friendly names for all of the pre-defined profiles.
	 * 
	 * @return A Set containing the collection of pre-defined profile names.
	 */
	public Set<String> getPredefinedProfileNames() {
		return predefinedProfiles.keySet();
	}

	/**
	 * Loads the pre-defined profile with the specified name.
	 * 
	 * @param name
	 *            The name of a pre-defined profile returned by the
	 *            getPredefinedProfileNames() method.
	 * 
	 * @return The pre-defined profile, or null, if the profile is not found.
	 * 
	 * @throws java.io.IOException
	 *             - An unexpected exception that, occurs when there is an error
	 *             reading the profile.
	 * 
	 * @throws ProfileException
	 */
	public Profile getPredefinedProfile(String name) throws IOException,
			ProfileException {
		String filename = predefinedProfiles.get(name);
		if (filename != null) {
			InputStream input = ProfileManager.class.getClassLoader()
					.getResourceAsStream(filename);
			if (input != null) {
				try {
					return Profile.createFromInputStream(name, input);
				} finally {
					input.close();
				}
			}
		}
		// Return null when not successful
		return null;
	}

	/**
	 * Loads the profile that is stored in the specified file.
	 * 
	 * @param file
	 *            The profile file to load.
	 * 
	 * @return A Profile object containing the profile, or null, if the profile
	 *         file is not found.
	 * 
	 * @throws java.io.IOException
	 *             An unexpected exception that occurs when there is an error
	 *             reading the profile.
	 * 
	 * @throws ProfileException
	 */
	public Profile getProfile(File file) throws FileNotFoundException,
			IOException, ProfileException, IllegalArgumentException {
		Profile result = Profile.createFromFile(file);

		UserPreferences userPreferences = UserPreferences.getInstance();
		userPreferences.setLastProfileDirectory(file.getParentFile());
		return result;
	}

	/**
	 * Gets the last profile used for the analysis.
	 * 
	 * @return A Profile object that is the last profile used.
	 * @throws ProfileException
	 *             - If data in the profile is corrupt.
	 * 
	 * @throws java.io.IOException
	 *             - If the application is unable to load the profile.
	 */
	public Profile getLastUserProfile(ProfileType profileType)
			throws ProfileException, IOException {
		String profile = UserPreferences.getInstance().getLastProfile(
				profileType);

		Profile result = null;
		if (profile != null) {

			File file = new File(profile);
			if (file.isAbsolute()) {
				try {
					result = getProfile(file);
				} catch (IOException e) {
					logger.warning("Unable to load previous profile file: "
							+ file.getAbsolutePath());
					throw new IOException(e);
				}
			}

			if (result == null) {

				// Not in file, try pre-defined
				try {
					result = getPredefinedProfile(profile);
				} catch (IOException e) {
					logger.log(Level.WARNING,
							"Unable to load previous pre-defined profile: "
									+ profile);
				}
			}
		}

		return result != null ? result : getDefaultProfile(profileType);
	}

	/**
	 * Returns the default device profile for the specified profile type (i.e. 3G or LTE)
	 * 
	 * @param profileType
	 *            The device profile type. One of the values of the Profiletype enumeration.
	 * 
	 * @return Profile The default profile.
	 */
	public Profile getDefaultProfile(ProfileType profileType) {

		Profile result = null;

		// Try default pre-defined
		String name = profileType == ProfileType.LTE ? DEFAULT_PROFILE_LTE
				: DEFAULT_PROFILE;
		try {
			result = getPredefinedProfile(aroProfiles.getString(name));
		} catch (ProfileException e) {
			logger.log(Level.WARNING,
					"Unable to load default pre-defined profile: " + name, e);
		} catch (IOException e) {
			logger.log(Level.WARNING,
					"Unable to load default pre-defined profile: " + name, e);
		}

		if (result == null) {
			return profileType == ProfileType.LTE ? new ProfileLTE()
					: new Profile3G();
		} else {
			return result;
		}
	}

	/**
	 * Returns the list of predefined profiles that are displayed on the Select
	 * Profile dialog.
	 * 
	 * @return The collection of predefined device profiles.
	 * @throws IOException
	 * @throws ProfileException
	 */
	public Collection<Profile> getPredefinedProfilesList() throws IOException,
			ProfileException {
		Collection<Profile> predefinedProfies = new ArrayList<Profile>();
		Set<String> profileNames = ProfileManager.getInstance()
				.getPredefinedProfileNames();
		for (String profileName : profileNames) {
			Profile profile = ProfileManager.getInstance()
					.getPredefinedProfile(profileName);
			predefinedProfies.add(profile);
		}
		return predefinedProfies;

	}

	/**
	 * Returns the default device profile.
	 * 
	 * @return The default profile.
	 */
	public Profile getDefaultProfile() {
		return getDefaultProfile(null);
	}

}
