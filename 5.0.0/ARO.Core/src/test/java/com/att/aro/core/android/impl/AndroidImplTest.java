package com.att.aro.core.android.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.powermock.classloading.spi.DoNotClone;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IDevice.DeviceState;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.SyncService;
import com.android.ddmlib.SyncService.ISyncProgressMonitor;
import com.android.ddmlib.TimeoutException;
import com.att.aro.core.BaseTest;
import com.att.aro.core.android.IAndroid;
import com.att.aro.core.android.pojo.ShellCommandCheckSDCardOutputReceiver;
import com.att.aro.core.android.pojo.ShellOutputReceiver;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.util.Util;

public class AndroidImplTest extends BaseTest{

	@Spy
	@InjectMocks
	AndroidImpl androidImpl;
	
	@Mock
	IFileManager filereader;
	
	static TemporaryFolder _tempFolder2; 

	@Rule
	public TemporaryFolder folder= new TemporaryFolder();

	
    @Before 
    public void setUp()
    {	
    	androidImpl = (AndroidImpl)context.getBean(IAndroid.class);
    	MockitoAnnotations.initMocks(this);
    	
    }
    
    @After
    public void after() {
    }

    @AfterClass
    public static void  reset(){
    	Mockito.reset();
    }
 
    @Test
    public void isEmulator_ResultIsTrue(){
    	IDevice device = mock(IDevice.class);
    	when(device.isEmulator()).thenReturn(true);
    	assertTrue (androidImpl.isEmulator(device));
    }
    
    @Test
    public void isEmulator_ResultIsFalse(){
    	IDevice device = mock(IDevice.class);
    	when(device.isEmulator()).thenReturn(false);
    	assertFalse (androidImpl.isEmulator(device));
    }
    
    @Test
    public void pushFile_ServiceHasReturn() throws SyncException, IOException, TimeoutException{
    	IDevice device = mock(IDevice.class);
    	SyncService service = mock(SyncService.class);
    	try {
			when(device.getSyncService()).thenReturn(service);
		} catch (AdbCommandRejectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	doNothing().when(service).pushFile(any(String.class),any(String.class), any(ISyncProgressMonitor.class));
    	assertTrue (androidImpl.pushFile(device, "", ""));
    	doThrow(SyncException.class).when(service)
			.pushFile(any(String.class),any(String.class), any(ISyncProgressMonitor.class));
    	assertFalse (androidImpl.pushFile(device, "", ""));
    	
       	doThrow(TimeoutException.class).when(service)
    			.pushFile(any(String.class),any(String.class), any(ISyncProgressMonitor.class));
        	assertFalse (androidImpl.pushFile(device, "", ""));

        doThrow(IOException.class).when(service)
        			.pushFile(any(String.class),any(String.class), any(ISyncProgressMonitor.class));
            	assertFalse (androidImpl.pushFile(device, "", ""));

    }
    
    @Test
    public void pushFile_ServiceIsNull()throws SyncException, IOException, TimeoutException{
    	IDevice device = mock(IDevice.class);
    	try {
			when(device.getSyncService()).thenReturn(null);
		} catch (AdbCommandRejectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	assertFalse (androidImpl.pushFile(device, "", ""));
    }
    
    
    @Test
    public void getState_returnIsState(){
    	IDevice device  = mock(IDevice.class);
    	when(device.getState()).thenReturn(DeviceState.ONLINE);
    	assertEquals(DeviceState.ONLINE, androidImpl.getState(device));
    }
    
    @Test
    public void isSDCardAttached_returnShellIsNull(){
    	IDevice device = mock(IDevice.class);
    	ShellCommandCheckSDCardOutputReceiver shelloutPut = mock(ShellCommandCheckSDCardOutputReceiver.class);
    	doReturn(shelloutPut).when(androidImpl).getOutputReturn();
    	when(shelloutPut.getResultOutput()).thenReturn(null);
    	assertFalse(androidImpl.isSDCardAttached(device));   	

    }    
    
    @Test
    public void isSDCardAttached_returnIsFalse(){
    	IDevice device = mock(IDevice.class);
    	String[] mockReturn = {""};
    	ShellCommandCheckSDCardOutputReceiver shelloutPut = mock(ShellCommandCheckSDCardOutputReceiver.class);
    	doReturn(shelloutPut).when(androidImpl).getOutputReturn();
    	when(shelloutPut.getResultOutput()).thenReturn(mockReturn);
    	assertFalse(androidImpl.isSDCardAttached(device));   	

    }
  
    @Test
    public void isSDCardAttached_returnIsTrue1(){
    	IDevice device = mock(IDevice.class);
    	String[] mockReturn = {"/storage/sdcard"};
    	ShellCommandCheckSDCardOutputReceiver shelloutPut = mock(ShellCommandCheckSDCardOutputReceiver.class);
    	doReturn(shelloutPut).when(androidImpl).getOutputReturn();
    	when(shelloutPut.getResultOutput()).thenReturn(mockReturn);
    	assertTrue(androidImpl.isSDCardAttached(device));   	

    }
    
    @Test
    public void isSDCardAttached_returnIsTrue2(){
    	IDevice device = mock(IDevice.class);
    	String[] mockReturn = {"mnt/shell"};
    	ShellCommandCheckSDCardOutputReceiver shelloutPut = mock(ShellCommandCheckSDCardOutputReceiver.class);
    	doReturn(shelloutPut).when(androidImpl).getOutputReturn();
    	when(shelloutPut.getResultOutput()).thenReturn(mockReturn);
    	assertTrue(androidImpl.isSDCardAttached(device));   	

    }

    @Test
    public void isSDCardEnoughSpace_returnShellIsNull(){
    	IDevice device = mock(IDevice.class);
    	
    	ShellCommandCheckSDCardOutputReceiver shelloutPut = mock(ShellCommandCheckSDCardOutputReceiver.class);
    	doReturn(shelloutPut).when(androidImpl).getOutputReturn();
    	when(shelloutPut.getResultOutput()).thenReturn(null);
    	assertFalse(androidImpl.isSDCardEnoughSpace(device,5120L));   	

    }
  
    @Test
    public void isSDCardEnoughSpace_return_emulator(){
    	IDevice device = mock(IDevice.class);
    	String[] mockReturn = {"/storage/sdcard 9.8M 8.0K 9.8M 512"};
    	ShellCommandCheckSDCardOutputReceiver shelloutPut = mock(ShellCommandCheckSDCardOutputReceiver.class);
    	doReturn(shelloutPut).when(androidImpl).getOutputReturn();
    	when(shelloutPut.getResultOutput()).thenReturn(mockReturn);
    	assertTrue(androidImpl.isSDCardEnoughSpace(device,5120L));   	

    }
    
    @Test
    public void isSDCardEnoughSpace_return_usbdevice(){
    	IDevice device = mock(IDevice.class);
    	String[] mockReturn = {"/mnt/shell/emulated 3.98G 993.46M 3.01G 4096"};
//    	String[] mockReturn = {"/sdcard: 13660864K total, 449824K used, 13211040K available (block size 32768)"};

    	ShellCommandCheckSDCardOutputReceiver shellOutput = mock(ShellCommandCheckSDCardOutputReceiver.class);
    	doReturn(shellOutput).when(androidImpl).getOutputReturn();
    	when(shellOutput.getResultOutput()).thenReturn(mockReturn);
    	assertTrue(androidImpl.isSDCardEnoughSpace(device,5120L));   	

    }
    
    @Test
    public void isSDCardEnoughSpace_return_USBDevice2_1(){
    	IDevice device = mock(IDevice.class);
    	String[] mockReturn = {"/sdcard: 13660864K total, 449824K used, 13211040K available (block size 32768)"};
    	ShellCommandCheckSDCardOutputReceiver shellOutput = mock(ShellCommandCheckSDCardOutputReceiver.class);
    	doReturn(shellOutput).when(androidImpl).getOutputReturn();
    	when(shellOutput.getResultOutput()).thenReturn(mockReturn);
    	assertTrue(androidImpl.isSDCardEnoughSpace(device,4096L));   	

    }
    

	@Test
	public void makeAROTraceDirectoryReturnIsFalse() throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
		IDevice device = mock(IDevice.class);

		ShellOutputReceiver shelloutPut = Mockito.mock(ShellOutputReceiver.class);
		Mockito.doReturn(shelloutPut).when(androidImpl).getShellOutput();
		
		doThrow(TimeoutException.class).when(device).executeShellCommand(any(String.class), any(IShellOutputReceiver.class), Mockito.anyInt(), any(TimeUnit.class) );
		assertFalse(androidImpl.makeAROTraceDirectory(device, "/sdcard/ARO/" + "temp.abc"));
    
		doThrow(ShellCommandUnresponsiveException.class).when(device).executeShellCommand(any(String.class), any(IShellOutputReceiver.class), Mockito.anyInt(), any(TimeUnit.class) );
		assertFalse(androidImpl.makeAROTraceDirectory(device, "/sdcard/ARO/" + "temp.abc"));
    
		doThrow(AdbCommandRejectedException.class).when(device).executeShellCommand(any(String.class), any(IShellOutputReceiver.class), Mockito.anyInt(), any(TimeUnit.class) );
		assertFalse(androidImpl.makeAROTraceDirectory(device, "/sdcard/ARO/" + "temp.abc"));
    
		doThrow(IOException.class).when(device).executeShellCommand(any(String.class), any(IShellOutputReceiver.class), Mockito.anyInt(), any(TimeUnit.class) );
		assertFalse(androidImpl.makeAROTraceDirectory(device, "/sdcard/ARO/" + "temp.abc"));

	}

 
    @Test
    public void isSDCardEnoughSpace_returnIsFalse(){
       	IDevice device = mock(IDevice.class);
    	String[] mockReturn = {"/system: 282432K total, 256620K used, 25812K available (block size 4096)"};
    	ShellCommandCheckSDCardOutputReceiver shellOutput = mock(ShellCommandCheckSDCardOutputReceiver.class);
    	doReturn(shellOutput).when(androidImpl).getOutputReturn();
    	when(shellOutput.getResultOutput()).thenReturn(mockReturn);
    	assertFalse(androidImpl.isSDCardEnoughSpace(device,4096L));
    }
    
    @Test
    public void pullTraceFilesFromEmulator_returnIsTrue() throws TimeoutException, AdbCommandRejectedException, IOException, SyncException{
    	IDevice device = mock(IDevice.class);
    	SyncService syncServ = mock(SyncService.class);
    	when(device.getSyncService()).thenReturn(syncServ);
    	doNothing().when(syncServ).pullFile(any(String.class),any(String.class),any(ISyncProgressMonitor.class));
    	when(filereader.createFile(any(String.class),any(String.class))).thenReturn(folder.newFile("cpu"));
    	assertTrue(androidImpl.pullTraceFilesFromEmulator(device, " ", " "));
    }
    
    @Test
    public void pullTraceFilesFromEmulator_returnIsFalse() throws TimeoutException, AdbCommandRejectedException, IOException, SyncException{
    	IDevice device = mock(IDevice.class);
    	SyncService syncServ = mock(SyncService.class);
    	when(device.getSyncService()).thenReturn(syncServ);
    	when(filereader.createFile(any(String.class),any(String.class))).thenReturn(folder.newFile("cpu"));
    	doThrow(SyncException.class).when(syncServ).pullFile(any(String.class),any(String.class),any(ISyncProgressMonitor.class));   	
    	assertFalse(androidImpl.pullTraceFilesFromEmulator(device, " ", " "));
    	doThrow(TimeoutException.class).when(syncServ).pullFile(any(String.class),any(String.class),any(ISyncProgressMonitor.class));   	
    	assertFalse(androidImpl.pullTraceFilesFromEmulator(device, " ", " "));
    	doThrow(IOException.class).when(syncServ).pullFile(any(String.class),any(String.class),any(ISyncProgressMonitor.class));   	
    	assertFalse(androidImpl.pullTraceFilesFromEmulator(device, " ", " "));
    }
   
    @Test
    public void pullTraceFilesFromDevice_returnIsTrue() throws TimeoutException, AdbCommandRejectedException, IOException, SyncException{
    	IDevice device = mock(IDevice.class);
    	SyncService syncServ = mock(SyncService.class);
    	when(device.getSyncService()).thenReturn(syncServ);
    	when(filereader.createFile(any(String.class),any(String.class))).thenReturn(folder.newFile("cpu"));
    	doNothing().when(syncServ).pullFile(any(String.class),any(String.class),any(ISyncProgressMonitor.class));
    	assertTrue(androidImpl.pullTraceFilesFromDevice(device, " ", " "));
    }
        
    @Test
    public void pullTraceFilesFromDevice_returnIsFalse() throws TimeoutException, AdbCommandRejectedException, IOException, SyncException{
    	IDevice device = mock(IDevice.class);
    	SyncService syncServ = mock(SyncService.class);
    	when(device.getSyncService()).thenReturn(syncServ);
    	when(filereader.createFile(any(String.class),any(String.class))).thenReturn(folder.newFile("cpu"));
       	doThrow(SyncException.class).when(syncServ).pullFile(any(String.class),any(String.class),any(ISyncProgressMonitor.class));   	
    	assertFalse(androidImpl.pullTraceFilesFromDevice(device, " ", " "));
    	doThrow(TimeoutException.class).when(syncServ).pullFile(any(String.class),any(String.class),any(ISyncProgressMonitor.class));   	
    	assertFalse(androidImpl.pullTraceFilesFromDevice(device, " ", " "));
    	doThrow(IOException.class).when(syncServ).pullFile(any(String.class),any(String.class),any(ISyncProgressMonitor.class));   	
    	assertFalse(androidImpl.pullTraceFilesFromDevice(device, " ", " "));
    }
    
    @Test
    public void makeAROTraceDirectory_returnIsTrue() throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
    	IDevice device = mock(IDevice.class);
    	doNothing().when(device).executeShellCommand(any(String.class)
    			,any(IShellOutputReceiver.class));
    	assertTrue(androidImpl.makeAROTraceDirectory(device,"/sdcard/ARO/"+"temp.abc"));
    }
    
   @Test
    public void removeEmulatorData() throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException{
    	IDevice device = mock(IDevice.class);
    	doNothing().when(device).executeShellCommand(any(String.class)
    			,any(IShellOutputReceiver.class));
    	assertTrue(androidImpl.removeEmulatorData(device, Util.getCurrentRunningDir()));
 
    }

    @Test
    public void startTcpDump() throws TimeoutException, IOException, AdbCommandRejectedException, ShellCommandUnresponsiveException{
    	IDevice device = mock(IDevice.class);
    	doNothing().when(device).executeShellCommand(any(String.class)
    			,any(IShellOutputReceiver.class));

    	assertTrue(androidImpl.startTcpDump(device, false, Util.getCurrentRunningDir()));   	

    }
    
    @Test
    public void startTcpDumpSELinux() throws TimeoutException, IOException, AdbCommandRejectedException, ShellCommandUnresponsiveException{
    	IDevice device = mock(IDevice.class);
    	doNothing().when(device).executeShellCommand(any(String.class)
    			,any(IShellOutputReceiver.class));

    	assertTrue(androidImpl.startTcpDump(device, true, Util.getCurrentRunningDir()));   	

    }
    
    @Test
    public void checkTcpDumpRunning_returnIsFalse(){
    	
    	IDevice device = mock(IDevice.class);
    	String[] mockReturn = {""};
    	ShellCommandCheckSDCardOutputReceiver shelloutPut = mock(ShellCommandCheckSDCardOutputReceiver.class);
    	doReturn(shelloutPut).when(androidImpl).getOutputReturn();
    	when(shelloutPut.getResultOutput()).thenReturn(mockReturn);
    	assertFalse(androidImpl.checkTcpDumpRunning(device));   	
    	
    }
    
    @Test
    public void checkTcpDumpRunning_returnIsTrue(){
    	
    	IDevice device = mock(IDevice.class);
    	String[] mockReturn = {"root","14","arodatacollector"};
    	ShellCommandCheckSDCardOutputReceiver shelloutPut = mock(ShellCommandCheckSDCardOutputReceiver.class);
    	doReturn(shelloutPut).when(androidImpl).getOutputReturn();
    	when(shelloutPut.getResultOutput()).thenReturn(mockReturn);
    	assertTrue(androidImpl.checkTcpDumpRunning(device));   	
    	
    }
    
    @Test
    public void checkPackageExist_returnIsTrue(){
    	IDevice device = mock(IDevice.class);
    	String[] mockReturn = {"packege:com.android.launcher","packege:com.android.music"};
    	ShellCommandCheckSDCardOutputReceiver shelloutPut = mock(ShellCommandCheckSDCardOutputReceiver.class);
    	doReturn(shelloutPut).when(androidImpl).getOutputReturn();
    	when(shelloutPut.getResultOutput()).thenReturn(mockReturn);
    	assertTrue(androidImpl.checkPackageExist(device, "com.android.launcher"));
    }
 
    @Test
    public void checkPackageExist_returnIsFalse(){
    	IDevice device = mock(IDevice.class);
    	String[] mockReturn = {"packege:com.android.launcher","packege:com.android.music"};
    	ShellCommandCheckSDCardOutputReceiver shelloutPut = mock(ShellCommandCheckSDCardOutputReceiver.class);
    	doReturn(shelloutPut).when(androidImpl).getOutputReturn();
    	when(shelloutPut.getResultOutput()).thenReturn(mockReturn);
    	assertFalse(androidImpl.checkPackageExist(device, "com.android.mms"));

    }
    
    @Test
    public void runVpnApkInDevice_returnIsFalse(){
    	IDevice device = mock(IDevice.class);
    	String[] mockReturn = {"Error: Activity class {com.att.arocollector/com.att.arocollector.AROCollectorActivity} does not exist."};
    	ShellCommandCheckSDCardOutputReceiver shelloutPut = mock(ShellCommandCheckSDCardOutputReceiver.class);
    	doReturn(shelloutPut).when(androidImpl).getOutputReturn();
    	when(shelloutPut.getResultOutput()).thenReturn(mockReturn);
    	assertFalse(androidImpl.runVpnApkInDevice(device));
    }
    
    @Test
	public void stopTcpDump() throws Exception {
    	
		Socket tcpDumpSocket = Mockito.mock(Socket.class);
		OutputStream out = Mockito.mock(OutputStream.class);
		
		Mockito.doReturn(tcpDumpSocket).when(androidImpl).getLocalSocket();
		Mockito.doReturn(out).when(tcpDumpSocket).getOutputStream();
		Mockito.doNothing().when(tcpDumpSocket).close();
		Mockito.doNothing().when(out).close();
		
		IDevice device = mock(IDevice.class);
		
		boolean res = androidImpl.stopTcpDump(device);
		assertTrue(res == true);
	}
	
    @Test
	public void chmod() throws Exception {

		IDevice device = mock(IDevice.class);
		ShellOutputReceiver shelloutPut = Mockito.mock(ShellOutputReceiver.class);
		Mockito.doReturn(shelloutPut).when(androidImpl).getShellOutput();
		
		doNothing().when(device).executeShellCommand(any(String.class), any(IShellOutputReceiver.class), Mockito.anyInt(), any(TimeUnit.class) );	
		
		boolean res = androidImpl.setExecutePermission(device, "tcpdump");
		assertTrue(res == true);
	}
	
}
