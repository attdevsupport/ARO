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

package com.att.android.arodatacollector.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.att.android.arodatacollector.R;
import com.att.android.arodatacollector.main.AROCollectorTaskManagerDetailProcess;
import com.att.android.arodatacollector.main.AROCollectorTaskManagerListAdapters.ProcessListAdapter;
import com.att.android.arodatacollector.main.AROCollectorTaskManagerPackagesInfo;
import com.att.android.arodatacollector.main.AROCollectorTaskManagerProcessInfo;
import com.att.android.arodatacollector.main.ARODataCollector;
import com.att.android.arodatacollector.utils.AROLogger;

/**
 * Represents the Task Killer screen of the ARO Data Collector, which lists all
 * of the running processes and provides an option to select and cancel those
 * processes.
 * 
 */

public class AROCollectorTaskManagerActivity extends Activity {

	/**
	 * A string for logging ARO Data Collector task killer activity in an
	 * Android log.
	 */
	public static final String TAG = "AROCollectorTaskManagerActivity";

	/** Task Killer refresh string for reloading the active process package name */
	private static final String ACTION_LOAD_FINISH = "ACTION_LOAD_FINISH";

	/**
	 * AROCollectorTaskManagerProcessInfo object to holds the Task Manager
	 * process details
	 */
	private AROCollectorTaskManagerProcessInfo aROCollectorTaskManagerProcessInfo;

	/**
	 * AROCollectorTaskManagerPackagesInfo object to holds the Task Manager
	 * package details
	 */
	private AROCollectorTaskManagerPackagesInfo packageinfo;

	/**
	 * Boolean flag to holds value if package has to be filtered or not from the
	 * Task Manager listing
	 */
	private boolean mPackageFilter = true;

	/** Task Manager list view adapter */
	private ProcessListAdapter tasksAdapter;

	/** Broadcast receiver to reload task lists */
	private BroadcastReceiver reloadTasks = new LoadFinishReceiver();

	/** ArrayList to hold task killers process list */
	private ArrayList<AROCollectorTaskManagerDetailProcess> detailProcessesList;

	/**
	 * The Application context of the ARo-Data Collector to gets and sets the
	 * application data
	 **/
	private ARODataCollector mApp;

	/** ActivityManager object */
	private ActivityManager aroActivityManager;

	/** List of default package name to be excluded from Task Manager listings **/
	private String m_AROTasksFilter[] = { "system", "com.android.phone",
			"com.android.inputmethod.latin", "com.sec.android.app.callsetting",
			"com.sec.android.app.twlauncher", "com.sec.android.inputmethod.axt9",
			"android.process.acore", "com.sec.android.app.controlpanel", "com.android.settings",
			"com.tecace.tetheringmanager", "com.google.android.googlequicksearchbox",
			"com.svox.pico", "com.sec.android.providers.drm", "com.smlds", "com.wssyncmldm",
			"com.sec.android.app.dialertab", "com.jungle.app.fonts", "com.noshufou.android.su",
			"com.nuance.android.vsuite.vsuiteapp", "sys.DeviceHealth",
			"com.rxnetworks.pgpsdownloader", "com.google.android.partnersetup",
			"com.google.android.location", "com.google.android.gsf", "com.fd.httpd",
			"com.matchboxmobile.wisp", "com.smithmicro.DM", "de.emsys.usbmode.service", "com.lge",
			"com.htc", "com.motorola", "com.smithmicro.DM", "com.pv", };

	/**
	 * Initializes data members with a saved instance of an AROCollectorTaskManagerActivity 
	 * object. Overrides the android.app.Activity#onCreate method.
	 * @param savedInstanceState  A saved instance of an AROCollectorTaskManagerActivity object.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initializeTaskManagerControls();
	}

	/**
	 * Initializes the task manager main screen controls and control event
	 * handling.
	 */
	private void initializeTaskManagerControls() {
		// Select All checkbox for task killer
		final CheckBox mAROselectAllTasks;
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.arocollector_taskmanager_home);
		aroActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		mAROselectAllTasks = (CheckBox) findViewById(R.id.aro_selectall);
		// Kill selected tasks button for task Killer
		final Button mAROkillTask = (Button) findViewById(R.id.btn_arotaskmanagerkill);
		mApp = (ARODataCollector) getApplication();
		mApp.setectTaskKillerAllTasks(false);
		mAROkillTask.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Kills the selected tasks
				tasksAdapter.killSelectedTasks();
				mAROselectAllTasks.setChecked(false);
				selectTaskstobeKilled(false);
			}
		});

		mAROselectAllTasks.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks, depending on whether it's
				// now checked
				if (((CheckBox) v).isChecked()) {
					mApp.setectTaskKillerAllTasks(true);
					selectTaskstobeKilled(true);
				} else {
					mApp.setectTaskKillerAllTasks(false);
					selectTaskstobeKilled(false);
				}
			}
		});
		packageinfo = new AROCollectorTaskManagerPackagesInfo(this);
	}

	/**
	 * Gets the list view body of Task Killer
	 * 
	 * @return the list of running packages rows for list view
	 */
	private ListView getListView() {
		return (ListView) this.findViewById(R.id.listbody);
	}

	/**
	 * Kills all the selected tasks for Task killer
	 * 
	 * @param selectvalue
	 */
	private void selectTaskstobeKilled(boolean selectvalue) {
		final ListView taskManagerList = getListView();
		final RelativeLayout firstItem = (RelativeLayout) taskManagerList.getChildAt(0);
		boolean currentTask = true;
		final TextView taskName = (TextView) firstItem.findViewById(R.id.task_name);
		// Checking if the first index is current application
		if (taskName.getText().toString().contains("ARO")) {
			currentTask = true;
		} else
			currentTask = false;
		for (int index = 0; index < taskManagerList.getChildCount(); index++) { // for
																				// (int
																				// index
																				// =
																				// 0;
																				// index
																				// <
																				// taskManagerList.getChildCount();
																				// index++)
			if (!detailProcessesList.get(index).getPackageName()
					.equalsIgnoreCase(this.getPackageName())
					|| !currentTask) {
				detailProcessesList.get(index).setSelected(selectvalue);
				final RelativeLayout itemLayout = (RelativeLayout) taskManagerList
						.getChildAt(index);
				final CheckBox selectTask = (CheckBox) itemLayout.findViewById(R.id.task_select);
				selectTask.setChecked(selectvalue);
			}
			for (int indextokill = 0; indextokill < detailProcessesList.size(); indextokill++) { // for
																									// (int
																									// index
																									// =
																									// 0;
																									// index
																									// <
																									// taskManagerList.getChildCount();
																									// index++)
				if (!detailProcessesList.get(indextokill).getPackageName()
						.equalsIgnoreCase(this.getPackageName())) {
					detailProcessesList.get(indextokill).setSelected(selectvalue);
				}
			}
		}
	}

	/**
	 * Refreshes the Task Killer screen by getting and displaying the latest
	 * list of running tasks.
	 */
	public void refreshTaskManagerTasks() {
		setProgressBarIndeterminateVisibility(true);

		final Thread taskKillerRefreshThread = new Thread(new Runnable() {
			@Override
			public void run() {
				aROCollectorTaskManagerProcessInfo = new AROCollectorTaskManagerProcessInfo();
				getRunningProcess();
				Intent in = new Intent(ACTION_LOAD_FINISH);
				AROCollectorTaskManagerActivity.this.sendBroadcast(in);
			}
		});
		taskKillerRefreshThread.start();
	}

	/**
	 * Overrides the onConfigurationChanged method.
	 * 
	 * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	/**
	 * Overrides the android.app.Activity#onResume method. 
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		final IntentFilter filter = new IntentFilter(ACTION_LOAD_FINISH);
		this.registerReceiver(reloadTasks, filter);
		packageinfo = new AROCollectorTaskManagerPackagesInfo(this);
		refreshTaskManagerTasks();
	}

	/**
	 * Gets all the running application packages for the task manager, ignores
	 * the list specified in ignore list of task manager listing
	 * 
	 */
	private void getRunningProcess() {
		String test[] = new String[100];
		final List<RunningAppProcessInfo> list2 = aroActivityManager.getRunningAppProcesses();
		detailProcessesList = new ArrayList<AROCollectorTaskManagerDetailProcess>();
		for (RunningAppProcessInfo ti : list2) {
			for (int j = 0; j < m_AROTasksFilter.length; j++) {
				// Need to have filter to ignore OEM specific tasks as per
				// requirement
				if (ti.processName.equals(m_AROTasksFilter[j])
						|| ti.processName.startsWith(m_AROTasksFilter[j])
						|| ti.processName.contains("inputmethod")
						|| ti.processName.contains("samsung")) {
					mPackageFilter = false;
					test[j] = ti.processName;

				}
			}
			if (mPackageFilter) {
				final AROCollectorTaskManagerDetailProcess dp = new AROCollectorTaskManagerDetailProcess(
						this, ti);
				dp.fetchApplicationInfo(packageinfo);
				dp.fetchPackageInfo();
				dp.fetchPsRow(aROCollectorTaskManagerProcessInfo);
				if (dp.isGoodProcess()) {
					if (dp.getPackageName().equalsIgnoreCase(this.getPackageName())) {
						dp.setCurrentApplication(true);
					} else {
						dp.setCurrentApplication(false);
					}
					// ignoring Google Framework Services and keyboard tasks
					// from OEM
					// TODO : Need to re-visit this
					if (!dp.getPackageName().equalsIgnoreCase("com.google.android.gsf")
							&& !dp.getPackageName().equalsIgnoreCase("com.google.android.location")) {
						detailProcessesList.add(dp);
						AROLogger.d(TAG, "Task Manager Tasks added:" + dp.getPackageName());
					}
				}
			} else
				mPackageFilter = true;
		}
		Collections.sort(detailProcessesList);
		tasksAdapter = new ProcessListAdapter(this, detailProcessesList, mApp);
	}

	/**
	 * LoadFinishReceiver class updates the task manager when broadcast is
	 * completed for loading new list of running process
	 * 
	 */
	private class LoadFinishReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(final Context ctx, Intent intent) {
			AROCollectorTaskManagerActivity.this.setProgressBarIndeterminateVisibility(false);
			AROCollectorTaskManagerActivity.this.getListView().setAdapter(tasksAdapter);
		}
	}

	/**
	 * Overrides the android.app.Activity#onPause method. 
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		this.unregisterReceiver(reloadTasks);
	}

}
