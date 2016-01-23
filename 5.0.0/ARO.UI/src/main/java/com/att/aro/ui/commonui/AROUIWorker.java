/**
 * Copyright 2016 AT&T
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
