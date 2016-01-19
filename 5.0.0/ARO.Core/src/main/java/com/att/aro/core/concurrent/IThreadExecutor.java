package com.att.aro.core.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface IThreadExecutor {
	void execute(Runnable task);
	Future<?> executeFuture(Runnable task);
	<T> Future<T> executeCallable(Callable<T> task);
}
