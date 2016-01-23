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
package com.att.aro.ui.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicReference;

import com.att.aro.core.pojo.AROTraceData;

public class AROModelObserver extends Observable{

	private final AtomicReference<AROTraceData> atomicModel = new AtomicReference<AROTraceData>();

	private List<Observer> observers = Collections.synchronizedList(new ArrayList<Observer>());

	/**
	 * @return the model
	 */
	public AROTraceData getaModel() {
		return atomicModel.get();
	}

	/**
	 * @param model the model to set
	 */
	public void refreshModel(AROTraceData model) {
		atomicModel.set(model);
		setChanged();
		notifyObservers(model);
	}

	public void notifyObservers(AROTraceData aModelupdate) {
		synchronized(observers) {
	         for (Observer ob : observers) {  
	             ob.update(this,aModelupdate);  
	         }
		}
      }  

	public void registerObserver(Observer observer) {
		observers.add(observer);  
	}  

	public void removeObserver(Observer observer) {  
		observers.remove(observer);  
	}  

}
