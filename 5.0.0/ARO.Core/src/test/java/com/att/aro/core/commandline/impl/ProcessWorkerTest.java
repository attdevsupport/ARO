/*
 *  Copyright 2015 AT&T
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
package com.att.aro.core.commandline.impl;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.att.aro.core.BaseTest;
import com.att.aro.core.commandline.pojo.ProcessWorker;
import com.att.aro.core.concurrent.impl.ThreadExecutorImpl;
import com.att.aro.core.util.Util;

public class ProcessWorkerTest extends BaseTest {

	ProcessWorker processWorker;
	private String result;

	ExecutorService service;

	@Before
	public void setup() {
		service = Executors.newCachedThreadPool();
	}

	@Test
	public void startWorker() throws IOException, InterruptedException {
		ProcessBuilder builder = new ProcessBuilder("");

		ProcessWorker worker = null;
		ProcessBuilder processbuild;

		if (Util.OS_NAME.contains("Windows")) {
			processbuild = builder.command(new String[] { "CMD", "/C",  "timeout", "30" });
		} else {
			processbuild = builder.command(new String[] { "ls", "-laR", "/" });
		}

		Process process = processbuild.start();
		worker = new ProcessWorker(process, 5);
		service.submit(worker);

		assertTrue(worker.isRunning());
		Thread.sleep(100);
		worker.setExit();
		assertTrue(!worker.isRunning());
	}

}
