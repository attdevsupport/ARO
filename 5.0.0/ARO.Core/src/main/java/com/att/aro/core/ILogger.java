package com.att.aro.core;

public interface ILogger {
	void debug(String message);
	void debug(String message, Throwable throwable);
	void error(String error);
	void error(String error, Throwable throwable);
	void info(String info);
	void info(String info, Throwable throwable);
	void warn(String warn);
	void warn(String warn, Throwable throwable);
}
