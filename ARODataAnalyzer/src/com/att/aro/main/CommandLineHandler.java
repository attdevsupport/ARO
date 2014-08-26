package com.att.aro.main;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;

import com.att.aro.util.Util;


/**
 * Singleton class that handle all command line arguments passed in the ARO analyzer.  
 * This class may be modified or extended to allow for the addition of custom data 
 * structures and methods.
 */
public class CommandLineHandler {
	/**
	 * Singleton instance
	 */
	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	private static final CommandLineHandler instance = new CommandLineHandler();
	private static String traceDirectoryName = null;
	private static ApplicationResourceOptimizer appParent = null;
	private static boolean isCommandLineEvent = false;
	private static Properties traceInfoProp = null;

	/**
	 * Returns singleton instance
	 * @return the instance
	 */
	public static CommandLineHandler getInstance() {
		return instance;
	}

	/**
	 * Protected constructor allows for extensions
	 */
	protected CommandLineHandler() {
		
	}
	
	/**
	 * Sets the trace directory name.
	 */	
	public void setTraceDirectoryName(String argTraceDirectoryName) {
		traceDirectoryName = argTraceDirectoryName;
	}
	
	/**
	 * Returns the trace directory name.
	 */	
	public String getTraceDirectoryName() {
		return traceDirectoryName;
	}
	
	/**
	 * Sets the parent object ApplicationResourceOptimizer.
	 */	
	public void setParent(ApplicationResourceOptimizer parent) {
		appParent = parent;
	}
	
	/**
	 * Returns the parent object ApplicationResourceOptimizer.
	 */	
	public ApplicationResourceOptimizer getParent() {
		return appParent;
	}
	
	/**
	 * Sets the flag whether this is initiated by command line event.
	 */	
	public void SetCommandLineEvent(boolean isArgCommandLineEvent) {
		isCommandLineEvent = isArgCommandLineEvent;
	}
	
	/**
	 * Returns the flag whether this is initiated by command line event.
	 */	
	public boolean IsCommandLineEvent() {
		return isCommandLineEvent;
	}
	
	/**
	 * Initializes the Trace Info file.
	 */	
	public void InitializeTraceInfoFile()
    {
		try {
			File file = new File(Util.TEMP_DIR + rb.getString("cmdline.logFile"));
    		file.delete();
    		
    		traceInfoProp = new Properties();
    	   	} catch (Exception ex) {
    		ex.printStackTrace();
        }
    }
	
	/**
	 * Updates the Trace Info file.
	 */	
	public void UpdateTraceInfoFile(String key, String value) {
		try {
			if (traceInfoProp != null) {
	    		//set the properties value and save properties to project root folder.
	    		traceInfoProp.setProperty(key, value);
	    		traceInfoProp.store(new FileOutputStream(Util.TEMP_DIR + rb.getString("cmdline.logFile")), null);
	    		
	    		//Get current date time with Date()
	    		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH-mm-ss");
	    		Date date = new Date();
	    		traceInfoProp.setProperty(rb.getString("cmdline.lastUpdatedDateTime"), dateFormat.format(date));
	    		traceInfoProp.store(new FileOutputStream(Util.TEMP_DIR + rb.getString("cmdline.logFile")), null);
			}
    	} catch (Exception ex) {
    		ex.printStackTrace();
        }
	}
}
