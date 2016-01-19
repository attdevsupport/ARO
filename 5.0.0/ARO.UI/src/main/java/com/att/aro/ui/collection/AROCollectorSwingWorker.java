package com.att.aro.ui.collection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingWorker;

import com.att.aro.core.ILogger;
import com.att.aro.core.datacollector.IDataCollector;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.mvc.AROCollectorActionEvent;
import com.att.aro.ui.commonui.AROProgressDialog;
import com.att.aro.ui.commonui.MessageDialogFactory;
import com.att.aro.ui.utils.ResourceBundleHelper;

/**
 * Manages launching & stopping 
 * 
 * @author Barry Nelson
 *
 * @param <T>
 * @param <V>
 */
public class AROCollectorSwingWorker<T, V> extends SwingWorker<T, V>{

	@InjectLogger
	private ILogger log;
	
	private JFrame parentUI;
	private AROProgressDialog progress;
	List<PropertyChangeListener> changeListeners = null;
	private int eventId;
	private String command;
	private String msg;
	List<ActionListener> actionListeners = null;
	
	IDataCollector collector;
	private int deviceId;
	private String traceFolderPath;
	private boolean videoCapture;

	private String udid;
	
	/**
	 *  Stop Collector
	 * @param IAROView 
	 * @param frmApplicationResourceOptimizer
	 * @param actionListeners
	 * @param eventId
	 * @param command
	 * @param msg
	 */
	public AROCollectorSwingWorker(JFrame frmApplicationResourceOptimizer
						, List<ActionListener> actionListeners
						, int eventId
						, String command
						, String msg) {
		this.parentUI = frmApplicationResourceOptimizer;
		this.progress = new AROProgressDialog(parentUI, ResourceBundleHelper.getMessageString("Message.stopcollector"));
		progress.setVisible(true);
		this.msg = msg;
		this.actionListeners = actionListeners;
		this.eventId = eventId;
		this.command = command;
	}
	
	
	/**
	 * Start Collector Android
	 * 
	 * @param IAROView 
	 * @param frmApplicationResourceOptimizer
	 * @param actionListeners
	 * @param eventId
	 * @param command
	 * @param deviceId
	 * @param trace
	 * @param videoCapture
	 */
	public AROCollectorSwingWorker( JFrame frmApplicationResourceOptimizer
									, List<ActionListener> actionListeners
									, int eventId
									, String command
									, int deviceId
									, String trace
									, boolean videoCapture) {
		this.parentUI = frmApplicationResourceOptimizer;
		this.progress = new AROProgressDialog(parentUI, ResourceBundleHelper.getMessageString("Message.startcollectorOnDevice"));
		progress.setVisible(true);
		this.msg = msg;
		this.actionListeners = actionListeners;
		this.eventId = eventId;
		this.command = command;
		this.deviceId = deviceId;
		this.traceFolderPath = trace;
		this.videoCapture = videoCapture;
	}

	/**
	 * Start Collector iOS
	 * 
	 * @param IAROView 
	 * @param frmApplicationResourceOptimizer
	 * @param actionListeners
	 * @param eventId
	 * @param command
	 * @param udid
	 * @param trace
	 * @param videoCapture
	 */
	public AROCollectorSwingWorker(JFrame frmApplicationResourceOptimizer
									, List<ActionListener> actionListeners
									, int eventId
									, String command
									, IDataCollector iOsCollector
									, String udid
									, String trace
									, boolean videoCapture) {
		this.parentUI = frmApplicationResourceOptimizer;
		this.progress = new AROProgressDialog(parentUI, ResourceBundleHelper.getMessageString("Message.startcollectorOnDevice"));
		progress.setVisible(true);
		this.msg = msg;
		this.actionListeners = actionListeners;
		this.eventId = eventId;
		this.command = command;
		this.collector = iOsCollector;
		this.udid = udid;
		this.traceFolderPath = trace;
		this.videoCapture = videoCapture;
	}


	@Override
	protected T doInBackground() throws Exception {
		if (actionListeners != null) {
			for (ActionListener name : actionListeners) {
				if (eventId == 1) {
					// start collector
					name.actionPerformed(new AROCollectorActionEvent(this, eventId, command, deviceId, traceFolderPath, videoCapture));
				} else if (eventId == 2) {
					// start collector
					name.actionPerformed(new AROCollectorActionEvent(this, eventId, command, collector, udid, traceFolderPath, videoCapture));
				} else {
					// stop collector
					name.actionPerformed(new ActionEvent(this, eventId, command));
				}
			}
		}
		return null;
	}
	
	@Override
	protected void done(){
		changeListeners = null;
		actionListeners = null;
		progress.dispose();

		if (msg != null) {
			MessageDialogFactory.showMessageDialog(parentUI, msg);
		}

	}

}
