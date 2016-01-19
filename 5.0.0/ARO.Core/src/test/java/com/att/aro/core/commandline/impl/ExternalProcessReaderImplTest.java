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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.att.aro.core.BaseTest;
import com.att.aro.core.commandline.IExternalProcessReaderSubscriber;

public class ExternalProcessReaderImplTest extends BaseTest {

	public static String message = null;
	//@InjectMocks
	ExternalProcessReaderImpl externalProcessReader;

	@Mock
	IExternalProcessReaderSubscriber subscriber;

	@Before
	public void setup() {
		ExternalProcessReaderImplTest.message = "";
	}

	/**
	 * <p>
	 * Tests all methods.
	 * </p
	 * . Except the termination of run()
	 * 
	 * @throws IOException
	 */
	@Test
	public void run() throws IOException {

		String aMessage = "helloFromRun";
		InputStream stream = new ByteArrayInputStream(aMessage.getBytes());
		externalProcessReader = new ExternalProcessReaderImpl();
		externalProcessReader.setInputStream(stream);

		subscriber = Mockito.mock(IExternalProcessReaderSubscriber.class);
		externalProcessReader.addSubscriber(subscriber);

		Mockito.doAnswer(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String method = invocation.getMethod().getName();
				Object[] args = invocation.getArguments();

				//System.out.println(method);

				if (method.equals("newMessage")) {
					if (args[0] != null) {
						//System.out.println((String)args[0]);
						ExternalProcessReaderImplTest.message += (String) args[0];
					}
				}
				return null;
			}
		}).when(subscriber).newMessage(Mockito.anyString());

		externalProcessReader.run();

		externalProcessReader.setStop();

		assertTrue(ExternalProcessReaderImplTest.message.equals(aMessage));
	}

	/**
	 * <p>
	 * Tests two subscribers.
	 * </p
	 * .
	 * 
	 * @throws IOException
	 */
	@Test
	public void runDouble() throws IOException {

		String aMessage1 = "helloFromFirst";
		InputStream stream1 = new ByteArrayInputStream(aMessage1.getBytes());
		externalProcessReader = new ExternalProcessReaderImpl();
		externalProcessReader.setInputStream(stream1);

		IExternalProcessReaderSubscriber subscriber1 = Mockito.mock(IExternalProcessReaderSubscriber.class);
		externalProcessReader.addSubscriber(subscriber1);

		Mockito.doAnswer(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String method = invocation.getMethod().getName();
				Object[] args = invocation.getArguments();

				//System.out.println(method);

				if (method.equals("newMessage")) {
					if (args[0] != null) {
						//System.out.println((String)args[0]);
						ExternalProcessReaderImplTest.message += "1:" + (String) args[0];
					}
				}
				return null;
			}
		}).when(subscriber1).newMessage(Mockito.anyString());

		externalProcessReader.run();
		externalProcessReader.removeSubscriber(subscriber1);

		String aMessage2 = "helloFromSecond";
		InputStream stream2 = new ByteArrayInputStream(aMessage2.getBytes());
		externalProcessReader.setInputStream(stream2);

		IExternalProcessReaderSubscriber subscriber2 = Mockito.mock(IExternalProcessReaderSubscriber.class);
		externalProcessReader.addSubscriber(subscriber2);

		Mockito.doAnswer(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String method = invocation.getMethod().getName();
				Object[] args = invocation.getArguments();

				//System.out.println(method);

				if (method.equals("newMessage")) {
					if (args[0] != null) {
						//System.out.println((String)args[0]);
						ExternalProcessReaderImplTest.message += "2:" + (String) args[0];
					}
				}
				return null;
			}
		}).when(subscriber2).newMessage(Mockito.anyString());

		externalProcessReader.run();
		externalProcessReader.setStop();
		externalProcessReader.removeSubscriber(subscriber1); // won't find so covers not removing what isn't there
		externalProcessReader.removeSubscriber(subscriber2);

	//	System.out.println(ExternalProcessReaderImplTest.message);
		assertTrue(ExternalProcessReaderImplTest.message.equals("1:" + aMessage1 + "2:" + aMessage2));
	}

	/**
	 * Tests setStop and the termination of run().
	 * 
	 * @throws IOException
	 */
	@Test
	public void stop() throws IOException {

		String aMessage = "helloFromRun";
		InputStream stream = new ByteArrayInputStream(aMessage.getBytes());
		externalProcessReader = new ExternalProcessReaderImpl();
		externalProcessReader.setInputStream(stream);

		subscriber = Mockito.mock(IExternalProcessReaderSubscriber.class);
		externalProcessReader.addSubscriber(subscriber);

		Mockito.doAnswer(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String method = invocation.getMethod().getName();
				Object[] args = invocation.getArguments();

				//System.out.println(method);

				if (method.equals("newMessage")) {
					if (args[0] != null) {
						//System.out.println((String)args[0]);
						ExternalProcessReaderImplTest.message += (String) args[0];
					}
				}
				return null;
			}
		}).when(subscriber).newMessage(Mockito.anyString());

		externalProcessReader.setStop();
		externalProcessReader.run();

		// never got to line = bufferedReader.readLine();
		assertTrue(ExternalProcessReaderImplTest.message.equals(""));
	}
}