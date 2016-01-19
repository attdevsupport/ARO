package com.att.aro.ui.commonui;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.ui.utils.ResourceBundleHelper;

public abstract class AROUIWorker<T,V> {

	@InjectLogger
	ILogger logger;
	
	private String errorMessage;
	private JFrame frame;
	
	public AROUIWorker(JFrame frame, String message){
		super();
		this.errorMessage = message;
		this.frame = frame;
	}
	
	public abstract void before() throws Exception;

	public abstract void doing() throws Exception;

	public abstract void after() throws Exception;

	public void execute(){
		try {
			before();
			doing();
			after();
		} catch(Exception exception){
			handleError(exception);
		}
	}
	
	private void handleError(Exception exception){
		logger.debug(errorMessage, exception);
		MessageDialogFactory.showMessageDialog(frame,
				ResourceBundleHelper.getMessageString(errorMessage),
				ResourceBundleHelper.getMessageString("menu.error.title"),
				JOptionPane.ERROR_MESSAGE);	
	}
}
