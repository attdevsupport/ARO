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

import com.att.android.arodatacollector.utils.AROCollectorUtils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Represents the process details of the rows in the ARO Data Collector task
 * killer.
 * 
 * */

public class AROCollectorTaskManagerDetailProcess implements Comparable<Object> {

	/** AROCollectorTaskManagerProcessInfo row */
	private AROCollectorTaskManagerProcessInfo.PsRow psrow = null;

	/** ApplicationInfo object to store application info */
	private ApplicationInfo appinfo = null;

	/** PackageInfo for task manager application package info */
	private PackageInfo pkginfo = null;

	/** Running process info */
	private ActivityManager.RunningAppProcessInfo runinfo = null;

	/** Task manager application title */
	private String title = null;

	/**
	 * Boolean to check if the current package is ARO-Data collector to exclude
	 * from the listing
	 */
	private boolean currentApp = false;

	/**
	 * Boolean to check if the application package is selected to be killer by
	 * the task manager
	 */
	private boolean isSelected;

	/**
	 * Package manager class object to get current application context package
	 * manager instance
	 */
	private PackageManager packageManager;

	/**
	 * Initializes a new instance of the AROCollectorTaskManagerDetailProcess
	 * class using the current application context and a RunningAppProcessInfo
	 * object.
	 * 
	 * @param ctx
	 *            The current application context.
	 * @param runinfo
	 *            An ActivityManager.RunningAppProcessInfo object that is used
	 *            to initialize the package manager.
	 */
	public AROCollectorTaskManagerDetailProcess(Context ctx,
			ActivityManager.RunningAppProcessInfo runinfo) {
		this.runinfo = runinfo;
		packageManager = ctx.getApplicationContext().getPackageManager();
	}

	/**
	 * Sets the selected task killer row.
	 * 
	 * @param is
	 *            A boolean value that indicates whether or not the task killer
	 *            row is selected.
	 */
	public void setSelected(boolean is) {
		isSelected = is;
	}

	/**
	 * Returns a boolean value that indicates whether the current row of the
	 * task killer is selected or not.
	 * 
	 * @return A boolean value that is "true" if the row is selected, and
	 *         "false" if it is not.
	 */
	public boolean getSelected() {
		return isSelected;
	}

	/**
	 * Gets the current row of process information from the task killer.
	 * 
	 * @return The current row of task killer information.
	 */
	public AROCollectorTaskManagerProcessInfo.PsRow getPsrow() {
		return psrow;
	}

	/**
	 * Sets the row of the task killer to the specified row.
	 * 
	 * @param psrow
	 *            An AROCollectorTaskManagerProcessInfo.PsRow object containing
	 *            the new task killer row.
	 */
	public void setPsrow(AROCollectorTaskManagerProcessInfo.PsRow psrow) {
		this.psrow = psrow;
	}

	/**
	 * Returns the application information object for the task killer process.
	 * 
	 * @return The application information object.
	 */
	public ApplicationInfo getAppinfo() {
		return appinfo;
	}

	/**
	 * Sets the application information.
	 * 
	 * @param appinfo
	 *            An ApplicationInfo object containing the new application
	 *            information.
	 */
	public void setAppinfo(ApplicationInfo appinfo) {
		this.appinfo = appinfo;
	}

	/**
	 * Returns the AROCollectorTaskManagerPackagesInfo object.
	 * 
	 * @return The AROCollectorTaskManagerPackagesInfo object.
	 */
	public PackageInfo getPkginfo() {
		return pkginfo;
	}

	/**
	 * Sets the package information.
	 * 
	 * @param pkginfo
	 *            A pkginfo object containing the new package information.
	 */
	public void setPkginfo(PackageInfo pkginfo) {
		this.pkginfo = pkginfo;
	}

	/**
	 * Returns the application information for the running process.
	 * 
	 * @return A RunningAppProcessInfo object that contains the application
	 *         information.
	 */
	public ActivityManager.RunningAppProcessInfo getRuninfo() {
		return runinfo;
	}

	/**
	 * Sets the application information for the running process.
	 * 
	 * @param runinfo
	 *            A RunningAppProcessInfo object containing the application
	 *            information for the running process.
	 */
	public void setRuninfo(ActivityManager.RunningAppProcessInfo runinfo) {
		this.runinfo = runinfo;
	}

	/**
	 * Fetches the application information for the task killer, and places it in
	 * the supplied AROCollectorTaskManagerPackagesInfo object.
	 * 
	 * @param pkg
	 *            An AROCollectorTaskManagerPackagesInfo object to hold the
	 *            application information.
	 */
	public void fetchApplicationInfo(AROCollectorTaskManagerPackagesInfo pkg) {
		if (appinfo == null)
			appinfo = pkg.getInfo(runinfo.processName);
	}

	/**
	 * Gets the package info
	 */
	public void fetchPackageInfo() {
		if (pkginfo == null && appinfo != null)
			pkginfo = AROCollectorUtils.getPackageInfo(packageManager, appinfo.packageName);
	}

	/**
	 * Fetches the row from the PS command output, and places it in the supplied
	 * AROCollectorTaskManagerProcessInfo object.
	 * 
	 * @param pi
	 *            An AROCollectorTaskManagerProcessInfo object to hold the Task
	 *            Manager process row information.
	 */
	public void fetchPsRow(AROCollectorTaskManagerProcessInfo pi) {
		if (psrow == null)
			psrow = pi.getPsRow(runinfo.processName);
	}

	/**
	 * Returns a boolean value indicating whether the current process is valid
	 * to add for the task killer row.
	 * 
	 * @return A boolean value that is "true" if the process is valid, and
	 *         "false" if it is not.
	 */
	public boolean isGoodProcess() {
		return runinfo != null && appinfo != null && pkginfo != null && pkginfo.activities != null
				&& (pkginfo.activities.length > 0);
	}

	/**
	 * Gets the package name from the application information.
	 * 
	 * @return The package name.
	 */
	public String getPackageName() {
		return appinfo.packageName;
	}

	/**
	 * Sets the row as the current application.
	 * 
	 * @param value
	 *            A Boolean value of "true" sets the row as the current
	 *            application.
	 */
	public void setCurrentApplication(boolean value) {
		currentApp = value;
	}

	/**
	 * Returns a boolean value indicating whether the task killer row is in the
	 * current application package.
	 * 
	 * @return A boolean value that is "true" if the process is in the current
	 *         application, and "false' if it is not.
	 */
	public boolean isCurrentApplication() {
		return currentApp;
	}

	/**
	 * Gets the name of the package that is the base activity.
	 * 
	 * @return The name of the package.
	 */
	public String getBaseActivity() {
		return pkginfo.activities[0].name;
	}

	/**
	 * Gets the title of the current process.
	 * 
	 * @return A string object containing the title of the process.
	 */
	public String getTitle() {
		if (title == null)
			title = appinfo.loadLabel(packageManager).toString();
		return title;
	}

	/**
	 * Compares this AROCollectorTaskManagerDetailProcess object to the
	 * specified Java object.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object another) {
		if (another instanceof AROCollectorTaskManagerDetailProcess && another != null) {
			return this.getTitle().compareTo(
					((AROCollectorTaskManagerDetailProcess) another).getTitle());
		}
		return -1;
	}

}
