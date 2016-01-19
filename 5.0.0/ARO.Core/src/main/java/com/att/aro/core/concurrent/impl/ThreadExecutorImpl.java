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
