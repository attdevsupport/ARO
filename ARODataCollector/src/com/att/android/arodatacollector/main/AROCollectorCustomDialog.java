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

import com.att.android.arodatacollector.R;
import com.att.android.arodatacollector.utils.AROCollectorUtils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;

/**
 * Represents a custom dialog in the ARO Data Collector which is used for confirmation
 * messages and errors.
 */

public class AROCollectorCustomDialog extends Dialog implements OnKeyListener {

	/** Android log TAG string for ARO-Data Collector task killer activity */
    private static final String TAG = "AROCollectorCustomDialog";
    
    /** BACK_KEY_PRESSED string **/
	private static final String BACK_KEY_PRESSED = "BACK_KEY_PRESSED";
	
	/** HOME_KEY_PRESSED string **/
	private static final String HOME_KEY_PRESSED = "HOME_KEY_PRESSED";
	
	/** The Dialog_Type enumeration specifies constant values that describe the types of custom dialogs used in the ARO Data Collector. */
	public static enum Dialog_Type {
		/**
		 * A dialog that prompts for the trace folder name.
		 */
		TRACE_FOLDERNAME, 
		/**
		 * A dialog indicating that the specified trace folder name already exists.
		 */
		TRACE_FOLDERNAME_EXISTS, 
		/**
		 * A dialog that indicates that there is an error in the trace folder name.
		 */
		TRACE_FOLDERNAME_ERRORMESSAGE, 
		/**
		 * A dialog indicating that rooting is not enabled.
		 */
		ROOT_NOT_ENABLED, 
		/**
		 * An dialog indicating that there is an SD Card error.
		 */
		SDCARD_ERROR, 
		/**
		 * A dialog indicating that an error occurred when the data bearer changed.
		 */
		BEARERCHANGE_ERROR, 
		/**
		 * A dialog indicating that the trace has stopped.
		 */
		TRACE_STOPPED, 
		/**
		 * An error dialog indicating that there is a special character in the specified trace folder name.
		 */
		TRACE_SPECIALCHARERROR, 
		/**
		 * A dialog that confirms that the trace folder name has been saved.
		 */
		TRACE_SAVED, 
		/**
		 * An error dialog indicating that the ARO Data Collector has failed to start.
		 */
		DC_FAILED_START,
		/**
		 * A dialog indicating that the SD Card is mounted.
		 */
		SDCARD_MOUNTED, 
		/**
		 * A dialog indicating that the SD Card is mounted.
		 */
		SDCARD_MOUNTED_MIDTRACE, 
		/**
		 * A dialog indicating that the AIRPANCE_MODE is on.
		 */
		AIRPANCE_MODEON
	}

	/** The Dialog_CallBack_Error enumeration specifies constant values that describe the types of callback methods associated with error message dialogs. */
	public static enum Dialog_CallBack_Error {
		/** 
		 * A callback method associated with the TRACE_FOLDERNAME_ERRORMESSAGE Dialog_Type. 
		 * */
		CALLBACK_TRACEFOLDERERROR, 
		/**
		 * A callback method associated with the TRACE_FOLDERNAME_EXISTS Dialog_Type.
		 */
		CALLBACK_TRACEEXISTSERROR, 
		/**
		 * A callback method associated with the TRACE_FOLDERNAME_ERRORMESSAGE Dialog_Type.
		 */
		CALLBACK_SHOWTRACENAMEERROR, 
		/**
		 * A callback method associated with the TRACE_SPECIALCHARERROR Dialog_Type.
		 */
		CALLBACK_SPECIALCHARERROR, 
		/**
		 * A default callback method.
		 */
		CALLBACK_DEFAULT
	}

	/** ARO-Data Collector current dialog type ID **/
	private Dialog_Type m_current_dialog = Dialog_Type.TRACE_FOLDERNAME;
	
	/** Call Back listener for Dialogs **/
	private ReadyListener readyListener;
	
	/** Call Back listener for Dialogs **/
	private DialogKeyListner keylistner;
	
	/** The Application context of the ARO-Data Collector to gets and sets the application data **/
	private ARODataCollector mApp;
	
	/** ARO Data Collector utilities class object */
	private AROCollectorUtils mAROUtils;
	
	/** ARO Data Collector SD card trace folder path i.e /SDCARD/ARO/<-tracefoldername-> */
	private File traceFolderPath;
	
	/** ARO Data Collector trace folder name */
	private String mTraceFolderName;
	
	/**
	 * Initializes a new instance of the AROCollectorCustomDialog class using the following parameters:
	 * 
	 * @param context The application context.
	 * @param theme The dialog theme.
	 * @param type The dialog type.  One of the values of the AROCollectorCustomDialog.Dialog_Type enumeration.
	 * @param readyListener The dialog call back listener on main activity.
	 * @param listner The dialog event listener.
	 * 
	 */
	public AROCollectorCustomDialog(Context context, int theme, Dialog_Type type,
			ReadyListener readyListener, DialogKeyListner listner) {
		
		super(context, theme);
		
		final WindowManager.LayoutParams lp = this.getWindow().getAttributes();
		m_current_dialog = type;
		this.readyListener = readyListener;
		keylistner = listner;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		lp.dimAmount = 0.5f;
		this.getWindow().setAttributes(lp);
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		mApp = (ARODataCollector) context.getApplicationContext();
		mAROUtils = new AROCollectorUtils();
	}

	/**
	 * Handles processing when an AROCollectorCustomDialog is created. Overrides the android.app.Dialog#onCreate method.
	 * @see android.app.Dialog#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		switch (m_current_dialog) {
		
		case SDCARD_MOUNTED:
			createAROSDCardMountedErrorDialog(false);
			break;
		case SDCARD_MOUNTED_MIDTRACE:
			createAROSDCardMountedErrorDialog(true);
			break;
		case DC_FAILED_START:
			createAROFailedStartErrorDialog();
			break;
		case TRACE_FOLDERNAME:
			createAROGetTraceFolderDialog();
			break;
		case TRACE_FOLDERNAME_EXISTS:
			createAROTraceFolderExistsDialog();
			break;
		case TRACE_FOLDERNAME_ERRORMESSAGE:
			createAROTraceFolderErrorDialog();
			break;
		case SDCARD_ERROR:
			createAROSDCardErrorDialog();
			break;
		case AIRPANCE_MODEON:
			createAROAirPlanceModeErrorDialog();
			break;
		case BEARERCHANGE_ERROR:
			createAROBearerErrorDialog();
			break;
		case TRACE_STOPPED:
			createTraceStoppedErrorDialog();
			break;
		case TRACE_SPECIALCHARERROR:
			createSpecialCharErrorDialog();
			break;
		}
	}
	
	/**
	 * Creates the error message dialog to notify SD card is mounted mid trace or 
	 * before start of ARO Data Collector trace
	 * 
	 */
	private void createAROSDCardMountedErrorDialog(boolean isMidtrace) {
		setContentView(R.layout.arocollector_errormessage);
		final TextView mAroErrorText = (TextView) findViewById(R.id.aro_error_message_text);
		final Button buttonOK = (Button) findViewById(R.id.dialog_button_ok);

		if (isMidtrace) {
			mAroErrorText.setText(R.string.aro_sdcardmountedmidtrace);
		} else {
			mAroErrorText.setText(R.string.aro_sdcardmountedstart);
		}

		buttonOK.setOnClickListener(new OKListener());
	}

	/**
	 * Creates the error dialog to notify  ARO- Data Collector failed to start within 15 seconds
	 * after starting data collector from main screen
	 * 
	 */
	private void createAROFailedStartErrorDialog() {
		setContentView(R.layout.arocollector_errormessage);
		final TextView mAroErrorText = (TextView) findViewById(R.id.aro_error_message_text);
		final Button buttonOK = (Button) findViewById(R.id.dialog_button_ok);
		mAroErrorText.setText(R.string.aro_failedstart);
		buttonOK.setOnClickListener(new OKListener());
	}

	/**
	 * Creates the input dialog for getting trace folder name for ARO-Data Collector.
	 * 
	 */
	private void createAROGetTraceFolderDialog() {
		setContentView(R.layout.arodialog_foldername);
		final Button buttonOK = (Button) findViewById(R.id.dialog_button_ok);
		final Button buttonCancel = (Button) findViewById(R.id.dialog_button_cancel);
		buttonOK.setOnClickListener(new OKListener());
		buttonCancel.setOnClickListener(new CancelListener());
		// Setting the current timestamp as the default name of trace folder
		final EditText mTraceFolderName = (EditText) findViewById(R.id.dialog_tracefoldername);
		if (mApp.getErrorTraceFoldername() != null) {
			mTraceFolderName.setText(mApp.getErrorTraceFoldername());
			mApp.setErrorTraceFoldername(null);
		} else {
			mTraceFolderName.setText(mAROUtils.getDefaultTraceFolderName());
		}
	}

	/**
	 * Creates the error dialog cases where given traces folder already exists 
	 * on the device sd card
	 */
	private void createAROTraceFolderExistsDialog() {
		setContentView(R.layout.arodialog_folderexists);
		final Button buttonOK = (Button) findViewById(R.id.dialog_button_ok);
		final Button buttonCancel = (Button) findViewById(R.id.dialog_button_cancel);
		buttonOK.setOnClickListener(new OKListener());
		buttonCancel.setOnClickListener(new CancelListener());
	}

	/**
	 * Creates the error dialogs UI for ARO-Data Collector
	 */
	private void createAROTraceFolderErrorDialog() {
		setContentView(R.layout.arocollector_errormessage);
		final TextView mAroErrorText = (TextView) findViewById(R.id.aro_error_message_text);
		final Button buttonOK = (Button) findViewById(R.id.dialog_button_ok);
		mAroErrorText.setText(R.string.aro_foldername);
		buttonOK.setOnClickListener(new OKListener());

	}

	/**
	 * Creates the error dialog to unexpected stops of ARO-Data Collector trace during trace cycle
	 * 
	 */
	private void createTraceStoppedErrorDialog() {
		setContentView(R.layout.arocollector_errormessage);
		final TextView mAroErrorText = (TextView) findViewById(R.id.aro_error_message_text);
		final Button buttonOK = (Button) findViewById(R.id.dialog_button_ok);
		mAroErrorText.setText(R.string.aro_stopped);
		buttonOK.setOnClickListener(new OKListener());

	}

	/**
	 * Creates the error dialog for special character in trace folder name
	 */
	private void createSpecialCharErrorDialog() {
		setContentView(R.layout.arocollector_errormessage);
		final TextView mAroErrorText = (TextView) findViewById(R.id.aro_error_message_text);
		final Button buttonOK = (Button) findViewById(R.id.dialog_button_ok);
		mAroErrorText.setText(R.string.aro_spcharerror);
		buttonOK.setOnClickListener(new OKListener());
	}

	/**
	 * Creates the error dialog to air plane mode ON 
	 */
	private void createAROAirPlanceModeErrorDialog() {
		setContentView(R.layout.arocollector_errormessage);
		final TextView mAroErrorText = (TextView) findViewById(R.id.aro_error_message_text);
		final Button buttonOK = (Button) findViewById(R.id.dialog_button_ok);
		mAroErrorText.setText(R.string.aro_flightmodeerror);
		buttonOK.setOnClickListener(new OKListener());
	}

	/**
	 * Creates the error dialog for SD card error
	 */
	private void createAROSDCardErrorDialog() {
		setContentView(R.layout.arocollector_errormessage);
		final TextView mAroErrorText = (TextView) findViewById(R.id.aro_error_message_text);
		final Button buttonOK = (Button) findViewById(R.id.dialog_button_ok);
		mAroErrorText.setText(R.string.aro_sdcarderror);
		buttonOK.setOnClickListener(new OKListener());
	}

	/**
	 * Creates the error dialog for case when data collector is stopped due to 
	 * bearer change 
	 */
	private void createAROBearerErrorDialog() {
		setContentView(R.layout.arocollector_errormessage);
		final TextView mAroErrorText = (TextView) findViewById(R.id.aro_error_message_text);
		final Button buttonOK = (Button) findViewById(R.id.dialog_button_ok);
		mAroErrorText.setText(R.string.aro_bearerchangeerror);
		buttonOK.setOnClickListener(new OKListener());
	}

	/**
	 * Handles processing when a key is pressed in an AROCollectorCustomDialog. Overrides the android.content.DialogInterface.OnKeyListener method.
	 * @see android.content.DialogInterface.OnKeyListener#onKey(android.content.DialogInterface, int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent arg2) {
		boolean ret = false;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (keylistner != null) {
				keylistner.HandleKeyEvent(BACK_KEY_PRESSED, m_current_dialog);
			}
			AROCollectorCustomDialog.this.dismiss();
			ret = false;
		} else if (keyCode == KeyEvent.KEYCODE_HOME) {
			if (keylistner != null) {
				keylistner.HandleKeyEvent(HOME_KEY_PRESSED, m_current_dialog);
			}
			AROCollectorCustomDialog.this.dismiss();
			ret = false;
		}
		try {
			AROCollectorCustomDialog.this.dismiss();
		} catch (IllegalArgumentException e) {
			Log.e(TAG,"exception in onKey", e);
		} 
		return ret;
	}

	/**
	 * Listener class for dialog cancel button even
	 * 
	 */
	private class CancelListener implements android.view.View.OnClickListener {
		@Override
		public void onClick(View v) {

			try {
				switch (m_current_dialog) {
				case TRACE_FOLDERNAME: {
					AROCollectorCustomDialog.this.dismiss();
				}
					break;
				case TRACE_FOLDERNAME_EXISTS: {
					AROCollectorCustomDialog.this.dismiss();
					readyListener.ready(
							Dialog_CallBack_Error.CALLBACK_SHOWTRACENAMEERROR,
							false);
				}
				default: {
					AROCollectorCustomDialog.this.dismiss();
				}
				}
			} catch (IllegalArgumentException e) {
				Log.e(TAG,"exception in IllegalArgumentException", e);
			} 
		}
	}
	
	/**
	 * ARO-Data Collector dialogs OK button event listener class
	 * 
	 */
	private class OKListener implements android.view.View.OnClickListener {
		@Override
		public void onClick(View v) {
			try {
				
				Dialog_CallBack_Error errrocode = Dialog_CallBack_Error.CALLBACK_DEFAULT;
				switch (m_current_dialog) {
				case TRACE_FOLDERNAME: {
					final EditText givenTraceFolderName = (EditText) findViewById(R.id.dialog_tracefoldername);
					mTraceFolderName = givenTraceFolderName.getText().toString();
					mApp.setTcpDumpTraceFolderName(mTraceFolderName);
					traceFolderPath = new File(mApp.getTcpDumpTraceFolderName());
					if (mAROUtils.isContainsSpecialCharacterorSpace(mTraceFolderName)) {
						errrocode = Dialog_CallBack_Error.CALLBACK_SPECIALCHARERROR;
						AROCollectorCustomDialog.this.dismiss();
						mApp.setErrorTraceFoldername(mTraceFolderName);
						readyListener.ready(errrocode, false);
						break;
					} else if (mTraceFolderName != null && !traceFolderPath.isDirectory()) {
						//Setting the trace folder name at application context
						mApp.setTcpDumpTraceFolderName(givenTraceFolderName.getText().toString());
						AROCollectorCustomDialog.this.dismiss();
						readyListener.ready(
								Dialog_CallBack_Error.CALLBACK_DEFAULT, true);

					} else {
						mApp.setTcpDumpTraceFolderName(givenTraceFolderName.getText().toString());
						if (mTraceFolderName.equalsIgnoreCase("") || mTraceFolderName == null) {
							errrocode = Dialog_CallBack_Error.CALLBACK_TRACEFOLDERERROR; //"Please enter valid trace folder name";
							AROCollectorCustomDialog.this.dismiss();
						} else if (traceFolderPath.isDirectory()) {
							errrocode = Dialog_CallBack_Error.CALLBACK_TRACEEXISTSERROR; //"Trace folder already exists";
							AROCollectorCustomDialog.this.dismiss();
						}
						readyListener.ready(errrocode, false);
					}
				}
					break;

				case TRACE_FOLDERNAME_EXISTS:
					if (mApp.getTcpDumpTraceFolderName() != null) {
						mAROUtils.deleteDirectory(new File(mApp.getTcpDumpTraceFolderName()));
					}
					readyListener.ready(Dialog_CallBack_Error.CALLBACK_DEFAULT,true);
					AROCollectorCustomDialog.this.dismiss();
					break;
				case TRACE_FOLDERNAME_ERRORMESSAGE:
					readyListener.ready(Dialog_CallBack_Error.CALLBACK_SHOWTRACENAMEERROR,false);
					AROCollectorCustomDialog.this.dismiss();
					break;
				case DC_FAILED_START:
				case SDCARD_MOUNTED:
				case SDCARD_MOUNTED_MIDTRACE:
				case SDCARD_ERROR:
				case AIRPANCE_MODEON:
				case BEARERCHANGE_ERROR:
				case TRACE_SAVED:
					AROCollectorCustomDialog.this.dismiss();
					break;
				case TRACE_STOPPED:
					AROCollectorCustomDialog.this.dismiss();
					readyListener.ready(
							Dialog_CallBack_Error.CALLBACK_TRACEFOLDERERROR,
							false);
					break;

				case TRACE_SPECIALCHARERROR:
					readyListener.ready(
							Dialog_CallBack_Error.CALLBACK_SHOWTRACENAMEERROR,
							false);
					AROCollectorCustomDialog.this.dismiss();
					break;

				}
			} catch (IllegalArgumentException e) {
				Log.e(TAG, "exception in IllegalArgumentException", e);
			} 
		}
	}

	/**
	 * Exposes a method for listening to responses from AROCollectorCustomDialog.Dialog_CallBack_Error dialogs.
	 */
	public interface ReadyListener {
		/**
		 * Handles responses to the specified AROCollectorCustomDialog.Dialog_CallBack_Error dialog.
		 * @param errorcode The type of error dialog that handles the responses. One of the types defined in the AROCollectorCustomDialog.Dialog_CallBack_Error enumeration.
		 * @param success A boolean value that indicates whether the error response has been handled.
		 */
		public void ready(Dialog_CallBack_Error errorcode, boolean success);

	}

	/**
	 * Exposes a method for listening to Key events from custom ARO Data Collector dialogs.
	 */
	public interface DialogKeyListner {
		/**
		 * Handles Key events for any of the AROCollectorCustomDialog.Dialog_Type dialogs.
		 * @param str A string that is passed to the custom dialog.
		 * @param type The type of dialog that the key events are being handled for. One of the types defined in the AROCollectorCustomDialog.Dialog_Type enumeration. 
		 */
		public void HandleKeyEvent(String str, Dialog_Type type);
	}

}
