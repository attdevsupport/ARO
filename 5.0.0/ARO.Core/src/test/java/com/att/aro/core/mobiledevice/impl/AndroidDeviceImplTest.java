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
package com.att.aro.core.mobiledevice.impl;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.android.ddmlib.IDevice;
import com.att.aro.core.BaseTest;
import com.att.aro.core.ILogger;
import com.att.aro.core.android.pojo.ShellOutputReceiver;
import com.att.aro.core.mobiledevice.IAndroidDevice;
import com.att.aro.core.mobiledevice.pojo.RootCheckOutputReceiver;
import com.att.aro.core.model.InjectLogger;

public class AndroidDeviceImplTest extends BaseTest {


	private boolean isSeLinux;
	
	private AndroidDeviceImpl androidDeviceImpl;

	private RootCheckOutputReceiver receiverSU;

	@InjectLogger
	private ILogger logger;

	@Before
	public void init() {
		androidDeviceImpl = (AndroidDeviceImpl) context.getBean(IAndroidDevice.class);
		logger = Mockito.mock(ILogger.class);
		androidDeviceImpl.setLogger(logger);
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * emulator: uid=0(root) gid=0(root) context=u:r:su:s0 HTC: uid=0(root)
	 * gid=0(root) Samsung: /system/bin/sh: su: not found Android5.0:
	 * uid=0(root) gid=0(root) context=u:r:init:s0
	 * 
	 * @throws Exception
	 */
	@Test
	public void mockRooted() throws Exception {
		IDevice device = Mockito.mock(IDevice.class);
		receiverSU = Mockito.mock(RootCheckOutputReceiver.class);
		
		AndroidDeviceImpl spied = Mockito.spy(new AndroidDeviceImpl());

		logger = Mockito.mock(ILogger.class);
		spied.setLogger(logger);
		
		Mockito.when(spied.makeRootCheckOutputReceiver()).thenReturn(receiverSU);
		Mockito.when(receiverSU.isRootId()).thenReturn(true);
		
		boolean rooted = spied.isAndroidRooted(device);
		assertTrue(rooted == true);
		
	}

	@Test(expected = Exception.class)
	public void isSeLinuxEnforced_null() throws Exception {
		IDevice device = null;
		AndroidDeviceImpl androidDeviceImpl = (AndroidDeviceImpl)context.getBean(IAndroidDevice.class);
		
		boolean enforcement = androidDeviceImpl.isSeLinuxEnforced(device);
		assertTrue(enforcement == true);

	}

	@Test
	public void isSeLinuxEnforced_yes() throws Exception {
		IDevice device = Mockito.mock(IDevice.class);
		//ShellOutputReceiver shellOutputReceiver = Mockito.mock(ShellOutputReceiver.class);

		AndroidDeviceImpl androidDeviceImpl = (AndroidDeviceImpl) context.getBean(IAndroidDevice.class);
		ShellOutputReceiver shellOutputReceiver = Mockito.spy( new ShellOutputReceiver()); 
		
		Mockito.doAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				isSeLinux = true;
				return null;
			}
		}).when(device).executeShellCommand("getenforce", shellOutputReceiver);
		
		Mockito.when(shellOutputReceiver.isSELinuxEnforce()).thenReturn(isSeLinux);

		boolean enforcement = androidDeviceImpl.isSeLinuxEnforced(device);
		
		assertTrue(enforcement == false);

	}

	/**
	 * emulator: uid=0(root) gid=0(root) context=u:r:su:s0 HTC: uid=0(root)
	 * gid=0(root) Samsung: /system/bin/sh: su: not found Android5.0:
	 * uid=0(root) gid=0(root) context=u:r:init:s0
	 * 
	 * @throws Exception
	 */
	@Test
	public void mockNotRooted() throws Exception {
		IDevice device = Mockito.mock(IDevice.class);
		receiverSU = Mockito.mock(RootCheckOutputReceiver.class);
		
		AndroidDeviceImpl spied = Mockito.spy(new AndroidDeviceImpl());

		logger = Mockito.mock(ILogger.class);
		spied.setLogger(logger);
		
		Mockito.when(spied.makeRootCheckOutputReceiver()).thenReturn(receiverSU);
		Mockito.when(receiverSU.isRootId()).thenReturn(false);
		
		boolean rooted = spied.isAndroidRooted(device);
		rooted = spied.isAndroidRooted(device);
		assertTrue(rooted == false);

	}

	/**
	 * emulator: uid=0(root) gid=0(root) context=u:r:su:s0 HTC: uid=0(root)
	 * gid=0(root) Samsung: /system/bin/sh: su: not found Android5.0:
	 * uid=0(root) gid=0(root) context=u:r:init:s0
	 * 
	 * @throws Exception
	 */
	@Test(expected = Exception.class)
	public void failedCommand() throws Exception {
		IDevice device = Mockito.mock(IDevice.class);
		receiverSU = Mockito.mock(RootCheckOutputReceiver.class);
		
		AndroidDeviceImpl spied = Mockito.spy(new AndroidDeviceImpl());

		logger = Mockito.mock(ILogger.class);
		spied.setLogger(logger);
		
		Mockito.when(spied.makeRootCheckOutputReceiver()).thenReturn(receiverSU);
		Mockito.when(receiverSU.isRootId()).thenReturn(false);
		
		Mockito.doAnswer(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				throw new Exception("failed on purpose");
			}
		}).when(device).executeShellCommand("su -c id", receiverSU);
		
		boolean rooted = spied.isAndroidRooted(device);
		
		rooted = spied.isAndroidRooted(device);
		assertTrue(rooted == false);

	}

	/**
	 * 
	 * null: Exception("device is null")
	 * 
	 * @throws Exception
	 */
	@Test(expected = Exception.class)
	public void nullDevice() throws Exception {
		boolean rooted = androidDeviceImpl.isAndroidRooted(null);
		assertTrue(rooted == false);
	}

}
