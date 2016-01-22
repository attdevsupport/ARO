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
package com.att.aro.core.concurrent.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;

import com.att.aro.core.BaseTest;
import com.att.aro.core.concurrent.IThreadExecutor;

public class ThreadExcecutorImplTest extends BaseTest {
	IThreadExecutor exec;
	
	@Before
	public void setup(){
		exec = context.getBean(IThreadExecutor.class);
	}
	
	@Test
	public void execute(){
		MyWorker worker = new MyWorker();
		exec.execute(worker);
	}
	@Test
	public void executeFuture() throws InterruptedException, ExecutionException{
		MyWorker worker = new MyWorker();
		Future<?> result = exec.executeFuture(worker);
	}
	private class MyWorker implements Runnable{

		boolean finished = false;
		@Override
		public void run() {
			this.finished = true;
		}
		public boolean isFinished(){
			return finished;
		}
	}
}
