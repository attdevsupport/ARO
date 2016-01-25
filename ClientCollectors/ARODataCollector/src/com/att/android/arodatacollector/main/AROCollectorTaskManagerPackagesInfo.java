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

package com.att.android.arodatacollector.main;

import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * Represents a package of application information for the ARO Data Collector
 * task killer.
 */

public class AROCollectorTaskManagerPackagesInfo {

	/** ApplicationInfo applist */
	private List<ApplicationInfo> appList;

	/**
	 * Initializes a new instance of the AROCollectorTaskManagerPackagesInfo
	 * object for the specified application context.
	 * 
	 * @param ctx
	 *            The application context.
	 */
	public AROCollectorTaskManagerPackagesInfo(Context ctx) {
		final PackageManager pm = ctx.getApplicationContext().getPackageManager();
		appList = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
	}

	/**
	 * Gets the application information for the specified package name.
	 * 
	 * @param name
	 *            The name of the application information package.
	 * @return An ApplicationInfo object containing the application information
	 *         for the package.
	 */
	public ApplicationInfo getInfo(String name) {
		if (name == null) {
			return null;
		}
		for (ApplicationInfo appinfo : appList) {
			if (name.equals(appinfo.processName)) {
				return appinfo;
			}
		}
		return null;
	}

}
