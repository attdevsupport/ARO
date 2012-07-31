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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.att.android.arodatacollector.R;
import com.att.android.arodatacollector.activities.AROCollectorTaskManagerActivity;
import com.att.android.arodatacollector.main.AROCollectorTaskManagerProcessInfo.PsRow;
import com.att.android.arodatacollector.utils.AROCollectorUtils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Represents a custom list adapter for the ARO Data Collector task killer which
 * is used to store list view data.
 * 
 */
public class AROCollectorTaskManagerListAdapters {

	/**
	 * Represents a custom list adapter for the ARO Data Collector task killer
	 * that contains a list of processes.
	 */
	public final static class ProcessListAdapter extends BaseAdapter {

		/**
		 * Log TAG string for ARO-Data Collector
		 * AROCollectorTaskManagerListAdapters class
		 */
		private static final String TAG = "ProcessListAdapter";

		/** Layout inflater for ARO-Data Collector task killer */
		private LayoutInflater mInflater;

		/** detail process list array for task killer application lists rows */
		private ArrayList<AROCollectorTaskManagerDetailProcess> detailProcesslist;

		/** Task Manager application context */
		private AROCollectorTaskManagerActivity mContext;

		/** Package manager */
		private PackageManager packageManager;

		/**
		 * The Application context of the ARo-Data Collector to gets and sets
		 * the application data
		 **/
		private ARODataCollector mApp;

		/** ARO Data Collector utilities class object */
		private AROCollectorUtils mAroUtils;

		/** ActivityManager object */
		private ActivityManager aroActivityManager;

		/**
		 * Initializes a new instance of the
		 * AROCollectorTaskManagerListAdapters.ProcessListAdapter class using
		 * the specified application context and list of processes.
		 * 
		 * @param context
		 *            The application context.
		 * @param detailProcesslist
		 *            An array containing the list of processes.
		 * @param mApp
		 *            The application class to which the list should be added.
		 */
		public ProcessListAdapter(AROCollectorTaskManagerActivity context,
				ArrayList<AROCollectorTaskManagerDetailProcess> detailProcesslist,
				ARODataCollector mApp) {
			// Cache the LayoutInflate to avoid asking for a new one each time.
			mInflater = LayoutInflater.from(context);
			mAroUtils = new AROCollectorUtils();
			aroActivityManager = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			;
			this.detailProcesslist = detailProcesslist;
			this.mContext = context;
			this.packageManager = mContext.getPackageManager();
			this.mApp = mApp;

		}

		/**
		 * Gets the total number of rows in the task killer.
		 * 
		 * @return An int, that is the total number of task killer rows.
		 */
		public int getCount() {
			return detailProcesslist.size();
		}

		/**
		 * Gets the Task Manager item at the specified index position.
		 * 
		 * @param position
		 *            The index of the Task Manager item to return.
		 * @return The Task Manager item.
		 */
		public Object getItem(int position) {
			return position;
		}

		/**
		 * Gets the ID of the Task Manager item for the specified index
		 * position.
		 * 
		 * @param position
		 *            The index of the Task Manager item.
		 * @return The ID of the specified Task Manager item.
		 */
		public long getItemId(int position) {
			return position;
		}

		/**
		 * Kills the currently selected task killer tasks.
		 * 
		 */
		public void killSelectedTasks() {
			mApp.setectTaskKillerAllTasks(false);
			for (int packagecount = 0; packagecount < getCount(); packagecount++) {
				final AROCollectorTaskManagerDetailProcess dp = detailProcesslist.get(packagecount);
				if (dp.getSelected()) {
					aroActivityManager.restartPackage(dp.getPackageName());
					new Thread(new Runnable() {
						@Override
						public void run() {
							if (!dp.getPackageName().equals(mContext.getPackageName())) {
								killPackage(Integer.toString(dp.getRuninfo().pid),
										dp.getPackageName());
							}
						}
					}).start();
				}
			}
			mContext.refreshTaskManagerTasks();
		}

		/**
		 * Kills the system running task using root shell prompt, which was
		 * failed to killed by restartPackage
		 * 
		 * @param process
		 *            id to be killed
		 */
		private void killPackage(String pid, String packagename) {
			Process sh = null;
			DataOutputStream os = null;
			int processId = 0;
			processId = 0;
			try {
				processId = mAroUtils.getProcessID(pid);
			} catch (IOException e) {
				Log.e(TAG, "IOException getting process ID for Kill Package" + e);
			} catch (InterruptedException e) {
				Log.e(TAG, "InterruptedException getting process ID for Kill Package" + e);
			} catch (IndexOutOfBoundsException e) {
				Log.e(TAG, "IndexOutOfBoundsException getting process Id for Kill Package" + e);
			}
			if (processId != 0) {
				try {
					Log.i("BACKGROUND", "Package Name=" + packagename + "PID=" + processId);
					sh = Runtime.getRuntime().exec("su");
					os = new DataOutputStream(sh.getOutputStream());
					final String Command = "kill -9 " + processId + "\n";
					os.writeBytes(Command);
					os.flush();
					sh.waitFor();
				} catch (IOException e) {
					Log.e(TAG, "Failed to kill task" + e);
				} catch (InterruptedException e) {
					Log.e(TAG, "Failed to kill task" + e);
				} finally {
					try {
						os.close();
					} catch (IOException e) {
					}
					sh.destroy();
				}
			} else {
				aroActivityManager.restartPackage(packagename);
			}

		}

		/**
		 * Returns a list view for the specified task killer row that contains
		 * the specified items.
		 * 
		 * @param position
		 *            The index of the list row.
		 * @param convertView
		 *            The view object containing list row items.
		 * @param parent
		 *            The parent view that the specified view should be added
		 *            to.
		 * @return A View object containing a list of view of the task killer
		 *         row.
		 */
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.arocollector_taskmanager_list, null);
				holder = new ViewHolder();
				holder.icon = (ImageView) convertView.findViewById(R.id.task_icon);
				holder.text_name = (TextView) convertView.findViewById(R.id.task_name);
				holder.select_task = (CheckBox) convertView.findViewById(R.id.task_select);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final AROCollectorTaskManagerDetailProcess dp = detailProcesslist.get(position);
			convertView.setVisibility(View.VISIBLE);
			holder.icon.setImageDrawable(dp.getAppinfo().loadIcon(packageManager));
			holder.text_name.setText(dp.getTitle());
			holder.select_task.setChecked(true);
			if (mApp.getTaskKillerAllTasksSelected()) {
				if (!dp.isCurrentApplication()) {
					holder.select_task.setChecked(true);
				}
			} else {
				holder.select_task.setChecked(false);
			}
			if (dp.isCurrentApplication()) {
				holder.text_name.setTextColor(Color.GREEN);
				holder.select_task.setEnabled(false);
				holder.select_task.setChecked(false);
			} else {
				holder.text_name.setTextColor(Color.WHITE);
				holder.select_task.setEnabled(true);
			}
			final PsRow row = dp.getPsrow();
			if (row == null) {
				detailProcesslist.get(position).setSelected(false);
			} else {
				if (mApp.getTaskKillerAllTasksSelected()) {
					detailProcesslist.get(position).setSelected(true);
				} else
					detailProcesslist.get(position).setSelected(false);
				if (dp.isCurrentApplication()) {
					detailProcesslist.get(position).setSelected(false);
				}
			}
			holder.select_task.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// Perform action on clicks, depending on whether it's now
					// checked
					if (((CheckBox) v).isChecked()) {
						detailProcesslist.get(position).setSelected(true);
					} else {
						detailProcesslist.get(position).setSelected(false);
					}
				}
			});

			return convertView;
		}

	}

	/**
	 * View holder class to hold different UI rows of Task killer list
	 * 
	 */
	private static class ViewHolder {
		ImageView icon;
		TextView text_name;
		CheckBox select_task;
	}

}
