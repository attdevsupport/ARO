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
