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
package com.att.aro.core.video.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.TimeoutException;
import com.att.aro.core.BaseTest;
import com.att.aro.core.concurrent.IThreadExecutor;
import com.att.aro.core.datacollector.IVideoImageSubscriber;
import com.att.aro.core.video.IVideoCapture;
import com.att.aro.core.video.IVideoWriter;
import com.att.aro.core.video.pojo.QuickTimeOutputStream.VideoFormat;


public class VideoCaptureImplTest extends BaseTest{

	
	VideoCaptureImpl videoCapture;
	
	IDevice device;
	IVideoWriter videoWriter;
	IThreadExecutor threadPool;
	//small tiff image that was converted to byte array
	//Size: 600 bytes
	//Dimension: 10x10 pixel
	byte[] imagedata = new byte[]{
    		73,73,42,0,120,0,0,0,-128,57,31,-128,16,3,-4,6,22,0,66,92,1,7,-16,0,6,-17,10,-127,-33,-32,7,80,76,0,-3,2,59,64,-79,112,35,-92,36,121,62,-125,93,111,-16,64,37,-28,-10,86,-99,-46,-60,-9,64,16,16,-77,35,59,-116,-83,18,-70,17,-60,22,117,18,-108,15,18,-53,52,-122,-103,1,-71,20,12,50,10,-107,8,103,9,45,-47,-114,-107,-128,68,30,69,102,-119,
    		-33,-51,50,50,-66,18,0,104,60,31,96,16,40,40,92,-4,0,0,96,32,21,0,0,1,3,0,1,0,0,0,10,0,0,0,1,1,3,0,1,0,0,0,10,0,0,0,2,1,3,0,1,0,0,0,8,0,0,0,3,1,3,0,1,0,0,0,5,0,0,0,6,1,3,0,1,0,0,0,1,0,0,0,10,1,3,0,1,0,0,0,1,0,0,0,13,1,2,0,124,0,
    		0,0,-54,1,0,0,14,1,2,0,18,0,0,0,70,2,0,0,17,1,4,0,1,0,0,0,8,0,0,0,18,1,3,0,1,0,0,0,1,0,0,0,21,1,3,0,1,0,0,0,1,0,0,0,22,1,3,0,1,0,0,0,51,3,0,0,23,1,4,0,1,0,0,0,112,0,0,0,26,1,5,0,1,0,0,0,122,1,0,0,27,1,5,0,1,0,0,0,-126,1,
    		0,0,28,1,3,0,1,0,0,0,1,0,0,0,40,1,3,0,1,0,0,0,3,0,0,0,41,1,3,0,2,0,0,0,0,0,1,0,61,1,3,0,1,0,0,0,2,0,0,0,62,1,5,0,2,0,0,0,-70,1,0,0,63,1,5,0,6,0,0,0,-118,1,0,0,0,0,0,0,-1,-1,-1,-1,96,-33,42,2,-1,-1,-1,-1,96,-33,42,2,0,10,-41,-93,-1,-1,
    		-1,-1,-128,-31,122,84,-1,-1,-1,-1,0,-51,-52,76,-1,-1,-1,-1,0,-102,-103,-103,-1,-1,-1,-1,-128,102,102,38,-1,-1,-1,-1,-16,40,92,15,-1,-1,-1,-1,-128,27,13,80,-1,-1,-1,-1,0,88,57,84,-1,-1,-1,-1,47,115,114,118,47,119,119,119,47,118,104,111,115,116,115,47,111,110,108,105,110,101,45,99,111,110,118,101,114,116,46,99,111,109,47,115,97,118,101,47,113,117,
    		101,117,101,100,47,48,47,56,47,97,47,48,56,97,100,53,56,99,57,100,55,99,56,50,97,52,102,53,102,101,49,97,54,98,97,51,100,100,102,98,101,54,99,47,105,110,116,101,114,109,101,100,105,97,116,101,49,47,111,95,98,51,98,50,48,97,100,97,48,99,48,101,49,51,57,52,46,116,105,102,102,0,67,114,101,97,116,101,100,32,119,105,116,104,32,71,73,77,80,0	
    };

 
	@Before
	public void setUp(){
		videoCapture = (VideoCaptureImpl)context.getBean(IVideoCapture.class);
		
		videoWriter = Mockito.mock(IVideoWriter.class);
		device = Mockito.mock(IDevice.class);
		threadPool = Mockito.mock(IThreadExecutor.class);
		videoCapture.setVideoWriter(videoWriter);		
		videoCapture.setThreadExecutor(threadPool);
		MockitoAnnotations.initMocks(this);
	}
	
	@After
	public void reset(){
		
	}
	 
	@Test
	public void run_test() throws TimeoutException, AdbCommandRejectedException, IOException  {
		videoCapture.setDevice(device);
		when(device.getScreenshot()).thenThrow(new IOException());
		videoCapture.run();		
		assertEquals(6,videoCapture.getiExceptionCount());
	}
	
	@Test
	public void run_test_exception() throws TimeoutException, AdbCommandRejectedException, IOException {
		videoCapture.setDevice(device);
		when(device.getScreenshot()).thenThrow(new TimeoutException());
		videoCapture.run();
		assertEquals(6,videoCapture.getiExceptionCount());
	}
	
	@Test(expected=IOException.class)
	public void init_test_resIsIOException() throws IOException{
		videoCapture.setDevice(device);
		Exception e = new IOException();
		doThrow(e).when(videoWriter)
		.init(any(String.class), any(VideoFormat.class), any(float.class), any(int.class));
		videoCapture.init(device, "");

	}
	
	@Test
	public void init_test_resIsNoError() throws IOException{
		videoCapture.setDevice(device);
		videoCapture.init(device, "");
	}
	
	@Test
	public void run_test2() throws TimeoutException, AdbCommandRejectedException, IOException {
		videoCapture.setDevice(device);
		RawImage testRaw = new RawImage();
		testRaw.data = imagedata;
		testRaw.width = 10;
		testRaw.height = 10;
		testRaw.bpp = 16;
		when(device.getScreenshot()).thenReturn(testRaw);
		when(videoWriter.getTimeUnits()).thenReturn((int) 1000f);
		IVideoImageSubscriber videoImageSub = mock(IVideoImageSubscriber.class);
		videoCapture.addSubscriber(videoImageSub);
		videoCapture.setDeviceManufacturer("somethingHTC");
		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				((Runnable) invocation.getArguments()[0]).run();
				videoCapture.stopRecording();
				return null;
			}
		}).when(threadPool).execute(any(Runnable.class));
		videoCapture.run();
		assertEquals(0, videoCapture.getiExceptionCount());
		assertTrue(videoCapture.isAllDone());
	}
	
	
}
