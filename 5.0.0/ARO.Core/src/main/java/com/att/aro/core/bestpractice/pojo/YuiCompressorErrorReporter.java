package com.att.aro.core.bestpractice.pojo;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

public class YuiCompressorErrorReporter implements ErrorReporter {


	@Override
	public EvaluatorException runtimeError(String message,
			String sourceName, int line, String lineSource, int lineOffset) {
		error(message, sourceName, line, lineSource, lineOffset);
		return new EvaluatorException(message);
	}

	@Override
	public void error(String arg0, String arg1, int arg2, String arg3, int arg4) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void warning(String arg0, String arg1, int arg2, String arg3,
			int arg4) {
		// TODO Auto-generated method stub
		
	}

}
