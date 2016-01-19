package com.att.aro.datacollector.rootedandroidcollector.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.SyncService;
import com.android.ddmlib.TimeoutException;
import com.att.aro.core.adb.IAdbService;
import com.att.aro.core.android.IAndroid;
import com.att.aro.core.concurrent.IThreadExecutor;
import com.att.aro.core.datacollector.DataCollectorType;
import com.att.aro.core.datacollector.IVideoImageSubscriber;
import com.att.aro.core.datacollector.pojo.StatusResult;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.mobiledevice.IAndroidDevice;
import com.att.aro.core.resourceextractor.IReadWriteFileExtractor;
import com.att.aro.core.video.IVideoCapture;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/plugins.xml" })
public class RootedAndroidCollectorImplTest {
	
	@Autowired
	protected ApplicationContext context;
	
	@Test
	public void testSuppression() {

	}

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void reset() {

	}

	@Mock
	private IFileManager filemanager;
	@Mock
	private IDevice device;
	@Mock
	private IAdbService adbservice;
	@Mock
	private IAndroid android;
	@Mock
	private IReadWriteFileExtractor extractor;
	@Mock
	private IThreadExecutor threadexecutor;
	@Mock
	private TcpdumpRunner runner;
	@Mock
	private IVideoCapture videocapture;
	@Mock
	private IAndroidDevice androidev;

	@InjectMocks
	@Autowired
	@Spy
	RootedAndroidCollectorImpl rootedAndroidCollectorImpl;


//	@Spy
//	RootedAndroidCollectorImpl rootedAndroidCollectorImpl;
//	@Before
//	public void setup(){
//		rootedAndroidCollectorImpl = (RootedAndroidCollectorImpl) context.getBean(IDataCollector.class);
//	}
	

	@Test
	public void testgetMilliSecondsForTimeout(){
		int timeout = rootedAndroidCollectorImpl.getMilliSecondsForTimeout();

		assertEquals(30000, timeout);
	}
	
	@Test
	public void testdirectoryExists(){
		
		when(filemanager.directoryExist(any(String.class))).thenReturn(true);
		when(filemanager.directoryExistAndNotEmpty(any(String.class))).thenReturn(true);
		StatusResult result = rootedAndroidCollectorImpl.startCollector(true, "", true, false, "abc", null, null);

		assertEquals(false, result.isSuccess());
	}
	
	@Test
	public void teststopCollector_emulator_returnIsSuccess() throws TimeoutException, AdbCommandRejectedException, IOException {

		doReturn(5).when(rootedAndroidCollectorImpl).getMilliSecondsForTimeout();
				
//		when(rootedAndroidCollectorImpl.getMilliSecondsForTimeout()).thenReturn(5);
		
		when(filemanager.directoryExist(any(String.class))).thenReturn(true);
		IDevice mockDevice = mock(IDevice.class);
		when(mockDevice.isEmulator()).thenReturn(true);
		SyncService service = mock(SyncService.class);
		when(mockDevice.getSyncService()).thenReturn(service);
		IDevice[] devlist = { mockDevice };
		when(mockDevice.getSerialNumber()).thenReturn("abc");

		try {
			when(adbservice.getConnectedDevices()).thenReturn(devlist);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			when(androidev.isAndroidRooted(any(IDevice.class))).thenReturn(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		when(android.isSDCardEnoughSpace(any(IDevice.class), any(long.class))).thenReturn(true);

		when(filemanager.fileExist(any(String.class))).thenReturn(true);
		when(extractor.extractFiles(any(String.class), any(String.class), any(ClassLoader.class))).thenReturn(true);

		when(android.pushFile(any(IDevice.class), any(String.class), any(String.class))).thenReturn(true);
		when(android.setExecutePermission(any(IDevice.class), any(String.class))).thenReturn(true);
//		doReturn(5).when(rootedAndroidCollectorImpl).getMilliSecondsForTimeout();
		rootedAndroidCollectorImpl.startCollector(true, "", true, false, "abc", null, null);

		when(android.checkTcpDumpRunning(any(IDevice.class))).thenReturn(true);
		when(android.stopTcpDump(any(IDevice.class))).thenReturn(true);
		when(android.isTraceRunning()).thenReturn(false);

		Date date = new Date();
		when(mockDevice.isEmulator()).thenReturn(true);
		when(videocapture.getVideoStartTime()).thenReturn(date);
		when(videocapture.isVideoCaptureActive()).thenReturn(true);
		StatusResult testResult = rootedAndroidCollectorImpl.stopCollector();
		assertEquals(true, testResult.isSuccess());

	}

	@Test
	public void teststopCollector_returnIsSuccess() throws TimeoutException, AdbCommandRejectedException, IOException {

		doReturn(5).when(rootedAndroidCollectorImpl).getMilliSecondsForTimeout();
		
		when(filemanager.directoryExist(any(String.class))).thenReturn(true);
		IDevice mockDevice = mock(IDevice.class);
		when(mockDevice.isEmulator()).thenReturn(true);
		SyncService service = mock(SyncService.class);
		when(mockDevice.getSyncService()).thenReturn(service);
		IDevice[] devlist = { mockDevice };
		when(mockDevice.getSerialNumber()).thenReturn("abc");
		try {
			when(adbservice.getConnectedDevices()).thenReturn(devlist);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			when(androidev.isAndroidRooted(any(IDevice.class))).thenReturn(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		when(android.isSDCardEnoughSpace(any(IDevice.class), any(long.class))).thenReturn(true);

		when(filemanager.fileExist(any(String.class))).thenReturn(true);
		when(extractor.extractFiles(any(String.class), any(String.class), any(ClassLoader.class))).thenReturn(true);

		when(android.pushFile(any(IDevice.class), any(String.class), any(String.class))).thenReturn(true);
		when(android.setExecutePermission(any(IDevice.class), any(String.class))).thenReturn(true);
		rootedAndroidCollectorImpl.startCollector(true, "", true, false, "abc", null, null);

		when(android.checkTcpDumpRunning(any(IDevice.class))).thenReturn(true);
		when(android.stopTcpDump(any(IDevice.class))).thenReturn(true);
		when(android.isTraceRunning()).thenReturn(false);

		Date date = new Date();
		when(mockDevice.isEmulator()).thenReturn(false);
		when(videocapture.getVideoStartTime()).thenReturn(date);
		when(videocapture.isVideoCaptureActive()).thenReturn(true);
		StatusResult testResult = rootedAndroidCollectorImpl.stopCollector();
		assertEquals(true, testResult.isSuccess());

	}


	@Test
	public void testgetName_noError() {
		String testResult = rootedAndroidCollectorImpl.getName();
		assertEquals("Rooted Android Data Collector", testResult);
	}

	@Test
	public void testaddVideoImageSubscriber_returnIsTrue() {
		IVideoImageSubscriber mockSubscriber = mock(IVideoImageSubscriber.class);
		rootedAndroidCollectorImpl.addVideoImageSubscriber(mockSubscriber);
	}

	@Test
	public void testgetMajorVersion_returnIsCorrect() {
		int testResult = rootedAndroidCollectorImpl.getMajorVersion();
		assertEquals(3, testResult);
	}

	@Test
	public void testgetMinorVersion_returnIsCorrect() {
		String testResult = rootedAndroidCollectorImpl.getMinorVersion();
		assertEquals("1.1.11", testResult);
	}

	@Test
	public void testDataCoolectorType_returnIsCorrect() {
		DataCollectorType testResult = rootedAndroidCollectorImpl.getType();
		assertEquals(DataCollectorType.ROOTED_ANDROID, testResult);
	}

	@Test
	public void testIsRunning_returnIsFalse() {
		boolean testResult = rootedAndroidCollectorImpl.isRunning();
		assertEquals(false, testResult);
	}

	//	@Ignore
	@Test
	public void teststartCollector_returnIsErrorCode200() throws Exception {
		when(filemanager.directoryExist(any(String.class))).thenReturn(true);
		when(adbservice.getConnectedDevices()).thenThrow(new Exception("AndroidDebugBridge failed to start"));
		StatusResult testResult = rootedAndroidCollectorImpl.startCollector(true, "", false, null);
		assertEquals(200, testResult.getError().getCode());
	}

	//	@Ignore
	@Test
	public void teststartCollector_returnIsErrorCode201() {
		when(filemanager.directoryExist(any(String.class))).thenReturn(true);
		IDevice mockDevice = mock(IDevice.class);
		when(mockDevice.isEmulator()).thenReturn(false);
		IDevice[] devlist = { mockDevice };
		when(mockDevice.getSerialNumber()).thenReturn("abc");
		try {
			when(adbservice.getConnectedDevices()).thenReturn(devlist);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			when(androidev.isAndroidRooted(any(IDevice.class))).thenReturn(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		when(android.checkPackageExist(any(IDevice.class), any(String.class))).thenReturn(false);

		StatusResult testResult = rootedAndroidCollectorImpl.startCollector(true, "", false, false, "abc", null, null);
		assertEquals(201, testResult.getError().getCode());

	}

	@Test
	public void test_getLog() {
		String[] logcatResponse = new String[]{"loggy","last"};
		when(android.getShellReturn(any(IDevice.class), any(String.class))).thenReturn(logcatResponse);
		String[] testResult = rootedAndroidCollectorImpl.getLog();
		assertTrue(testResult[0].equals(logcatResponse[0]));

	}

	@Test
	public void test_captureVideo() throws Exception {

		doNothing().when(videocapture).init(any(IDevice.class), any(String.class));
		List<IVideoImageSubscriber> videoImageSubscribers = mock(List.class);
		IVideoImageSubscriber vImageSubscriber = mock(IVideoImageSubscriber.class);
	
		when(videoImageSubscribers.isEmpty()).thenReturn(false);
		doNothing().when(threadexecutor).execute(any(Runnable.class));

		rootedAndroidCollectorImpl.captureVideo();
		assertTrue(true);

	}

	//	@Ignore
	@Test
	public void teststartCollector_returnIsErrorCode203() {
		when(filemanager.directoryExist(any(String.class))).thenReturn(true);
		IDevice[] devlist = {};
		try {
			when(adbservice.getConnectedDevices()).thenReturn(devlist);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StatusResult testResult = rootedAndroidCollectorImpl.startCollector(true, "", false, false, "abc", null, null);
		assertEquals(203, testResult.getError().getCode());
	}

	//	@Ignore
	@Test
	public void teststartCollector_returnIsErrorCode204() {
		when(filemanager.directoryExist(any(String.class))).thenReturn(true);
		IDevice mockDevice = mock(IDevice.class);
		IDevice[] devlist = { mockDevice };
		when(mockDevice.getSerialNumber()).thenReturn("abc");
		try {
			when(adbservice.getConnectedDevices()).thenReturn(devlist);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StatusResult testResult = rootedAndroidCollectorImpl.startCollector(true, "", false, false, "cde", null, null);
		assertEquals(204, testResult.getError().getCode());
	}

	//	@Ignore
	@Test
	public void teststartCollector_resultIsErrorCode205() {
		when(filemanager.directoryExist(any(String.class))).thenReturn(true);
		IDevice mockDevice = mock(IDevice.class);
		when(mockDevice.isEmulator()).thenReturn(true);
		IDevice[] devlist = { mockDevice };
		when(mockDevice.getSerialNumber()).thenReturn("abc");
		try {
			when(adbservice.getConnectedDevices()).thenReturn(devlist);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			when(androidev.isAndroidRooted(any(IDevice.class))).thenReturn(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		when(android.isSDCardEnoughSpace(any(IDevice.class), any(long.class))).thenReturn(false);

		StatusResult testResult = rootedAndroidCollectorImpl.startCollector(true, "", false, false, "abc", null, null);
		assertEquals(205, testResult.getError().getCode());

	}

	//	@Ignore
	@Test
	public void teststartCollector_returnIsErrorCode206() {
		when(filemanager.directoryExist(any(String.class))).thenReturn(true);
		IDevice mockDevice = mock(IDevice.class);
		IDevice[] devlist = { mockDevice };
		when(mockDevice.getSerialNumber()).thenReturn("abc");
		try {
			when(adbservice.getConnectedDevices()).thenReturn(devlist);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			when(androidev.isAndroidRooted(any(IDevice.class))).thenReturn(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		when(android.checkTcpDumpRunning(any(IDevice.class))).thenReturn(true);
		StatusResult testResult = rootedAndroidCollectorImpl.startCollector(true, "", false, false, "abc", null, null);
		assertEquals(206, testResult.getError().getCode());

	}

	//	@Ignore
	@Test
	public void teststartCollector_returnIsErrorCode207() {
		when(filemanager.directoryExist(any(String.class))).thenReturn(false);
		StatusResult testResult = rootedAndroidCollectorImpl.startCollector(true, "", false, false, "abc", null, null);
		assertEquals(207, testResult.getError().getCode());

	}

//	//	@Ignore
//	@Test
//	public void teststartCollector_resultIsErrorCode208() {
//		when(filemanager.directoryExist(any(String.class))).thenReturn(true);
//		IDevice mockDevice = mock(IDevice.class);
//		when(mockDevice.isEmulator()).thenReturn(true);
//		IDevice[] devlist = { mockDevice };
//		when(mockDevice.getSerialNumber()).thenReturn("abc");
//		try {
//			when(adbservice.getConnectedDevices()).thenReturn(devlist);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		try {
//			when(androidev.isAndroidRooted(any(IDevice.class))).thenReturn(true);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		when(android.isSDCardEnoughSpace(any(IDevice.class), any(long.class))).thenReturn(true);
//		File mockFile = mock(File.class);
//		when(mockFile.length()).thenReturn(1000L);
//		when(filemanager.createFile(any(String.class))).thenReturn(mockFile);
//		when(extractor.extractFiles(any(String.class), any(String.class), any(ClassLoader.class))).thenReturn(false);
//		StatusResult testResult = rootedAndroidCollectorImpl.startCollector(true, "", false, "abc", null);
//		assertEquals(208, testResult.getError().getCode());
//	}

	//	@Ignore
	@Test
	public void teststartCollector_returnIsErrorCode209() {
		when(filemanager.directoryExist(any(String.class))).thenReturn(true);
		IDevice mockDevice = mock(IDevice.class);
		when(mockDevice.isEmulator()).thenReturn(true);
		IDevice[] devlist = { mockDevice };
		when(mockDevice.getSerialNumber()).thenReturn("abc");
		try {
			when(adbservice.getConnectedDevices()).thenReturn(devlist);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			when(androidev.isAndroidRooted(any(IDevice.class))).thenReturn(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		when(android.isSDCardEnoughSpace(any(IDevice.class), any(long.class))).thenReturn(true);

		when(filemanager.fileExist(any(String.class))).thenReturn(true);
		when(android.pushFile(any(IDevice.class), any(String.class), any(String.class))).thenReturn(false);

		StatusResult testResult = rootedAndroidCollectorImpl.startCollector(true, "", false, false, "abc", null, null);
		assertEquals(209, testResult.getError().getCode());

	}

	//	@Ignore
	@Test
	public void teststartCollector_returnIsErrorCode210() {
		when(filemanager.directoryExist(any(String.class))).thenReturn(true);
		IDevice mockDevice = mock(IDevice.class);
		when(mockDevice.isEmulator()).thenReturn(false);
		IDevice[] devlist = { mockDevice };
		when(mockDevice.getSerialNumber()).thenReturn("abc");
		try {
			when(adbservice.getConnectedDevices()).thenReturn(devlist);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			when(androidev.isAndroidRooted(any(IDevice.class))).thenReturn(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		when(android.checkPackageExist(any(IDevice.class), any(String.class))).thenReturn(true);
		when(filemanager.fileExist(any(String.class))).thenReturn(true);
		String[] testarray = { "does not exist" };
		when(android.getShellReturn(any(IDevice.class), any(String.class))).thenReturn(testarray);
		StatusResult testResult = rootedAndroidCollectorImpl.startCollector(true, "", false, false, "abc", null, null);
		assertEquals(210, testResult.getError().getCode());
	}

	//	@Ignore
	@Test
	public void teststartCollector_returnIsErrorCode212() {
		when(filemanager.directoryExist(any(String.class))).thenReturn(true);
		IDevice mockDevice = mock(IDevice.class);
		when(mockDevice.isEmulator()).thenReturn(true);
		IDevice[] devlist = { mockDevice };
		when(mockDevice.getSerialNumber()).thenReturn("abc");
		try {
			when(adbservice.getConnectedDevices()).thenReturn(devlist);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			when(androidev.isAndroidRooted(any(IDevice.class))).thenReturn(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		when(android.isSDCardEnoughSpace(any(IDevice.class), any(long.class))).thenReturn(true);

		when(filemanager.fileExist(any(String.class))).thenReturn(true);
		when(extractor.extractFiles(any(String.class), any(String.class), any(ClassLoader.class))).thenReturn(true);
		when(android.pushFile(any(IDevice.class), any(String.class), any(String.class))).thenReturn(true);
		when(android.setExecutePermission(any(IDevice.class), any(String.class))).thenReturn(false);
		StatusResult testResult = rootedAndroidCollectorImpl.startCollector(true, "", false, false, "abc", null, null);
		assertEquals(212, testResult.getError().getCode());

	}

	//	@Ignore
	@Test
	public void teststartCollector_returnIsErrorCode213() {
		when(filemanager.directoryExist(any(String.class))).thenReturn(true);
		IDevice mockDevice = mock(IDevice.class);
		IDevice[] devlist = { mockDevice };
		when(mockDevice.getSerialNumber()).thenReturn("abc");
		try {
			when(adbservice.getConnectedDevices()).thenReturn(devlist);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StatusResult testResult = rootedAndroidCollectorImpl.startCollector(true, "", false, false, "abc", null, null);
		assertEquals(213, testResult.getError().getCode());
	}

	@Test
	public void teststopCollector_returnIsErrorCode211() {
		StatusResult testResult = rootedAndroidCollectorImpl.stopCollector();
		assertEquals(211, testResult.getError().getCode());
	}

	//	@Ignore

	//	@Ignore
	@Test
	public void teststartCollector_returnIsSuccess() {
		when(filemanager.directoryExist(any(String.class))).thenReturn(true);
		IDevice mockDevice = mock(IDevice.class);
		when(mockDevice.isEmulator()).thenReturn(true);
		IDevice[] devlist = { mockDevice };
		when(mockDevice.getSerialNumber()).thenReturn("abc");
		try {
			when(adbservice.getConnectedDevices()).thenReturn(devlist);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			when(androidev.isAndroidRooted(any(IDevice.class))).thenReturn(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		when(android.isSDCardEnoughSpace(any(IDevice.class), any(long.class))).thenReturn(true);

		when(filemanager.fileExist(any(String.class))).thenReturn(true);
		when(extractor.extractFiles(any(String.class), any(String.class), any(ClassLoader.class))).thenReturn(true);

		when(android.pushFile(any(IDevice.class), any(String.class), any(String.class))).thenReturn(true);
		when(android.setExecutePermission(any(IDevice.class), any(String.class))).thenReturn(true);
		when(android.checkTcpDumpRunning(any(IDevice.class))).thenReturn(false).thenReturn(true);

		StatusResult testResult = rootedAndroidCollectorImpl.startCollector(true, "", false, false, "abc", null, null);
		assertEquals(true, testResult.isSuccess());

	}

	@Test
	public void teststopCollector_ThrowException() throws TimeoutException, AdbCommandRejectedException, IOException {
		when(device.getSyncService()).thenThrow(new IOException());

		Date date = new Date();
		when(videocapture.getVideoStartTime()).thenReturn(date);
		StatusResult testResult = rootedAndroidCollectorImpl.stopCollector();
		assertEquals(211, testResult.getError().getCode());

	}

	@Test
	public void false_pushApk() throws Exception{
		when(device.getSyncService()).thenThrow(new IOException());

		
		boolean testResult = rootedAndroidCollectorImpl.pushApk(device);
		assertEquals(false, testResult);

	}

	@Test
	public void gotLocalAPK_pushApk() throws Exception{
		when(device.getSyncService()).thenThrow(new IOException());
		when(filemanager.fileExist(any(String.class))).thenReturn(true);
		
		boolean testResult = rootedAndroidCollectorImpl.pushApk(device);
		assertEquals(true, testResult);

	}

	@Test
	public void teststartCollector_ThrowException() throws TimeoutException, AdbCommandRejectedException, IOException {
		when(filemanager.directoryExist(any(String.class))).thenReturn(true);
		IDevice mockDevice = mock(IDevice.class);
		IDevice[] devlist = { mockDevice };
		when(mockDevice.getSerialNumber()).thenReturn("abc");
		doThrow(new TimeoutException()).when(mockDevice).createForward(any(int.class), any(int.class));
		try {
			when(adbservice.getConnectedDevices()).thenReturn(devlist);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			when(androidev.isAndroidRooted(any(IDevice.class))).thenReturn(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StatusResult testResult = rootedAndroidCollectorImpl.startCollector(true, "", false, false, "abc", null, null);
		assertEquals(201, testResult.getError().getCode());

	}

}
