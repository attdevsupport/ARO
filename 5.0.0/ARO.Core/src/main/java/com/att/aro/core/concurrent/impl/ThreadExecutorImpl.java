/**
 *  Copyright 2016 AT&T
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
package com.att.aro.core.concurrent.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;

import com.att.aro.core.ILogger;
import com.att.aro.core.concurrent.IThreadExecutor;
import com.att.aro.core.model.InjectLogger;

public class ThreadExecutorImpl implements IThreadExecutor {
	
	@InjectLogger
	private static ILogger LOG;
	
	@Autowired
	public void setLogger(ILogger logger) {
		this.LOG = logger;
	}
	
	ExecutorService service;
	
	public ThreadExecutorImpl(){
		service = Executors.newCachedThreadPool();
	}
	@Override
	public void execute(Runnable task) {
		LOG.debug("executing task :"+task.getClass().getName());
		service.submit(task);
		LOG.debug("done exec task");
	}
	@Override
	public Future<?> executeFuture(Runnable task) {
		return service.submit(task);
	}
	@Override
	public <T> Future<T> executeCallable(Callable<T> task) {
		return service.submit(task);
	}

}
