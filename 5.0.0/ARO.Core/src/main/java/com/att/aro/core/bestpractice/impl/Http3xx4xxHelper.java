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
package com.att.aro.core.bestpractice.impl;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;

/**
 * helper class for Http3xxCodeImpl and Http4xx5xxImpl
 */
public final class Http3xx4xxHelper {
	//Suppress pmd warning
	private Http3xx4xxHelper(){}
	public static String createFailResult(Map<Integer, Integer> map, String textResults, 
			String errorPlural, String errorSingular){
		Iterator<Map.Entry<Integer, Integer>> iterator = map.entrySet().iterator();
		String errormessage = "";
		StringBuilder errorlist = new StringBuilder();
		int index = 1;
		int size = map.size();
		while(iterator.hasNext()){
			Map.Entry<Integer, Integer> entry = iterator.next();
			errormessage = formatError(entry, errorPlural, errorSingular);
			
			if(index > 1 && index < size){
				errorlist.append(", ");
			}else if(index > 1 && index >= size){
				errorlist.append(" and ");
			}
			errorlist.append(errormessage);
			index++;
			
		}
		return MessageFormat.format(textResults, errorlist.toString());
	}
	public static String formatError(Map.Entry<Integer, Integer> entry, String errorPlural, String errorSingular) {
		int count = entry.getValue();
		if(count > 1){
			//3 301 status responses
			return MessageFormat.format(errorPlural, count,entry.getKey());
		}else{
			//1 301 status response
			return MessageFormat.format(errorSingular, entry.getKey());
		}
	}
}
