package com.att.aro.core.peripheral.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.peripheral.IBluetoothInfoReader;
import com.att.aro.core.peripheral.pojo.BluetoothInfo;

public class BluetoothInfoReaderImplTest extends BaseTest {
	
	BluetoothInfoReaderImpl bluetoothReader;
	
	private IFileManager filereader;
	private String traceFolder = "traceFolder";

	@Before
	public void setup() {
		filereader = Mockito.mock(IFileManager.class);
		bluetoothReader = (BluetoothInfoReaderImpl)context.getBean(IBluetoothInfoReader.class);
		bluetoothReader.setFileReader(filereader);
	}

	@Test
	public void ioException() throws IOException {
		
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenThrow(new IOException("failed on purpose"));
		bluetoothReader.setFileReader(filereader);
		List<BluetoothInfo> bluetoothInfo = bluetoothReader.readData("/", 0, 0);
		
		assertTrue(bluetoothInfo.size() == 0);
	
	}

	@Test
	public void nullEntrys() throws IOException {
		
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(null);
		bluetoothReader.setFileReader(filereader);
		List<BluetoothInfo> bluetoothInfo = bluetoothReader.readData("/", 0, 0);
		
		assertTrue(bluetoothInfo.size() == 0);
	
	}

	@Test
	public void noLines() throws IOException {
		
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] {});
		bluetoothReader.setFileReader(filereader);
		List<BluetoothInfo> bluetoothInfo = bluetoothReader.readData("/", 0, 0);
		
		assertTrue(bluetoothInfo.size() == 0);
	
	}

	@Test
	public void invalidBluetoothEntry1() throws IOException {
		
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] { //                                                   delta    +delta (sum)
				 "1.413913535177E9 this is InVaLiD"       //   1413913591.806 - 1413913535.177 = 56.629	56.629
				,"1.413913591806E9 CONNECTED"       //   1413913610.613 - 1413913591.806 = 18.807	75.436
				,"1.413913610613E9 OFF"             //   1413913620.683 - 1413913610.613 = 10.070
				,"1.413913620683E9 DISCONNECTED"    //   1413913620.883 - 1413913620.683 =  0.200
				,"1.413913620883E9 CONNECTED"       //   1413913622.533 - 1413913620.883 =  1.650	77.086
				,"1.413913622533E9 CONNECTED"       		//   1413913622.533
				,""
		});
		bluetoothReader.setFileReader(filereader);
		List<BluetoothInfo> bluetoothInfo = bluetoothReader.readData("/", 0, 0);
		
		assertTrue(bluetoothInfo.size() > 0);
	
	}

	@Test
	public void invalidBluetoothEntry2() throws IOException {
		
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] { //                                                   delta    +delta (sum)
				 "1.413913535177E9 CONNECTED"       //   1413913591.806 - 1413913535.177 = 56.629	56.629
				,"1.413913591806E9 this is InVaLiD"       //   1413913610.613 - 1413913591.806 = 18.807	75.436
				,"1.413913610613E9 OFF"             //   1413913620.683 - 1413913610.613 = 10.070
				,"1.413913620683E9 DISCONNECTED"    //   1413913620.883 - 1413913620.683 =  0.200
				,"1.413913620883E9 CONNECTED"       //   1413913622.533 - 1413913620.883 =  1.650	77.086
				,"1.413913622533E9 CONNECTED"       		//   1413913622.533
				,""
		});
		bluetoothReader.setFileReader(filereader);
		List<BluetoothInfo> bluetoothInfo = bluetoothReader.readData("/", 0, 0);
		
		assertTrue(bluetoothInfo.size() > 0);
	
	}

	@Test
	public void invalidTimeStamp1() throws IOException {
		
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] { //                                                   delta    +delta (sum)
				 "1.413913535177z9 CONNECTED"       //   1413913591.806 - 1413913535.177 = 56.629	56.629
				,"1.413913591806E9 CONNECTED"       //   1413913610.613 - 1413913591.806 = 18.807	75.436
				,"1.413913610613E9 OFF"             //   1413913620.683 - 1413913610.613 = 10.070
				,"1.413913620683E9 DISCONNECTED"    //   1413913620.883 - 1413913620.683 =  0.200
				,"1.413913620883E9 CONNECTED"       //   1413913622.533 - 1413913620.883 =  1.650	77.086
				,"1.413913622533E9 CONNECTED"       		//   1413913622.533
				,""
		});
		bluetoothReader.setFileReader(filereader);
		List<BluetoothInfo> bluetoothInfo = bluetoothReader.readData("/", 0, 0);
		
		assertTrue(bluetoothInfo.size() == 6);
	
	}

	@Test
	public void invalidTimeStamp2() throws IOException {
		
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] { //                                                   delta    +delta (sum)
				 "1.413913535177E9 CONNECTED"       //   1413913591.806 - 1413913535.177 = 56.629	56.629
				,"1.413913591806z9 CONNECTED"       //   1413913610.613 - 1413913591.806 = 18.807	75.436
				,"1.413913610613E9 OFF"             //   1413913620.683 - 1413913610.613 = 10.070
				,"1.413913620683E9 DISCONNECTED"    //   1413913620.883 - 1413913620.683 =  0.200
				,"1.413913620883E9 CONNECTED"       //   1413913622.533 - 1413913620.883 =  1.650	77.086
				,"1.413913622533E9 CONNECTED"       		//   1413913622.533
				,""
		});
		bluetoothReader.setFileReader(filereader);
		List<BluetoothInfo> bluetoothInfo = bluetoothReader.readData("/", 0, 0);
		
		assertTrue(bluetoothInfo.size() == 5);
	
	}

	
	@Test
	public void readData1() throws IOException {
		
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		String[] arr = new String[] { //                                                   delta    +delta (sum)
				 "1.413913535177E9 CONNECTED"       //   1413913591.806 - 1413913535.177 = 56.629	56.629
				,"1.413913591806E9 CONNECTED"       //   1413913610.613 - 1413913591.806 = 18.807	75.436
				,"1.413913610613E9 OFF"             //   1413913620.683 - 1413913610.613 = 10.070
				,"1.413913620683E9 DISCONNECTED"    //   1413913620.883 - 1413913620.683 =  0.200
				,"1.413913620883E9 CONNECTED"       //   1413913622.533 - 1413913620.883 =  1.650	77.086
				,"1.413913622533E9 JUNK"       		//   1413913622.533
				,""
		};

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		List<BluetoothInfo> bluetoothInfo = bluetoothReader.readData("/", 0, 0);
		
		assertTrue(bluetoothInfo.size() > 0);
		
		assertEquals(1.413913622533E9, bluetoothInfo.get(5).getBeginTimeStamp(), 0);
		assertEquals(1.413913622533E9, bluetoothInfo.get(4).getEndTimeStamp(), 0);
		assertEquals(0, bluetoothInfo.get(5).getEndTimeStamp(), 0);

		assertEquals("BLUETOOTH_UNKNOWN", bluetoothInfo.get(5).getBluetoothState().toString());
		assertEquals("BLUETOOTH_DISCONNECTED", bluetoothInfo.get(3).getBluetoothState().toString());
		assertEquals("BLUETOOTH_CONNECTED", bluetoothInfo.get(1).getBluetoothState().toString());
		assertEquals("BLUETOOTH_TURNED_OFF", bluetoothInfo.get(2).getBluetoothState().toString());
		
	}
	
	@Test
	public void readData2() throws IOException {
		
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] { //                                                   delta    +delta (sum)
				 "1.413913535177E9 CONNECTED"       //   1413913591.806 - 1413913535.177 = 56.629	56.629
				,"1.413913591806E9 CONNECTED"       //   1413913610.613 - 1413913591.806 = 18.807	75.436
				,"1.413913610613E9 OFF"             //   1413913620.683 - 1413913610.613 = 10.070
				,"1.413913620683E9 DISCONNECTED"    //   1413913620.883 - 1413913620.683 =  0.200
				,"1.413913620883E9 CONNECTED"       //   1413913622.533 - 1413913620.883 =  1.650	77.086
				,"1.413913622533E9 CONNECTED"       		//   1413913622.533
	//			,""
		});
		bluetoothReader.setFileReader(filereader);
		List<BluetoothInfo> bluetoothInfo = bluetoothReader.readData("/", 0, 0);
		
		assertTrue(bluetoothInfo.size() > 0);
	
	}
	
	@Test
	public void readData3() throws IOException {
				
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] {
				 "1.413913535177E9 DISCONNECTED"   
				,"1.413913591806E9 CONNECTED"
		});
		bluetoothReader.setFileReader(filereader);
		List<BluetoothInfo> bluetoothInfo = bluetoothReader.readData("/", 0, 0);
		
		assertTrue(bluetoothInfo.size() > 0);
		
		
	}
	
	@Test
	public void readData4() throws IOException {
				
		
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] {
				 "1.413913535177E9 OFF"      
				,"1.413913591806E9 CONNECTED"
		});
		bluetoothReader.setFileReader(filereader);
		List<BluetoothInfo> bluetoothInfo =  bluetoothReader.readData("/", 0, 0);
		
		assertTrue(bluetoothInfo.size() > 0);
		
	}
	
	@Test
	public void readData5() throws IOException {
				
		
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] {
				 "1.413913535177E9 UNKNOWN"      
				,"1.413913591806E9 CONNECTED"
		});
		bluetoothReader.setFileReader(filereader);
		List<BluetoothInfo> bluetoothInfo =  bluetoothReader.readData("/", 0, 0);
		
		assertTrue(bluetoothInfo.size() > 0);
		
	}
	
	@Test
	public void readData6() throws IOException {
				
		
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] {
				 "1.413913535177E9 DISCONNECTED"      
				,"this should generate \"Invalid Bluetooth trace entry:...\""      
				,"1.413913591806E9 CONNECTED"
		});
		bluetoothReader.setFileReader(filereader);
		List<BluetoothInfo> bluetoothInfo =  bluetoothReader.readData("/", 0, 0);
		
		assertTrue(bluetoothInfo.size() > 0);
		}

	@Test
	public void getBluetoothActiveDuration() throws IOException {
		
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		String[] arr = new String[] { //                                                   delta    +delta (sum)
				 "1.413913535177E9 CONNECTED"       //   1413913591.806 - 1413913535.177 = 56.629	56.629
				,"1.413913591806E9 CONNECTED"       //   1413913610.613 - 1413913591.806 = 18.807	75.436
				,"1.413913610613E9 OFF"             //   1413913620.683 - 1413913610.613 = 10.070
				,"1.413913620683E9 DISCONNECTED"    //   1413913620.883 - 1413913620.683 =  0.200
				,"1.413913620883E9 CONNECTED"       //   1413913622.533 - 1413913620.883 =  1.650	77.086
				,"1.413913622533E9 JUNK"       		//   1413913622.533
				,""
		};

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		List<BluetoothInfo> bluetoothInfo = bluetoothReader.readData("/", 0, 0);

		double activeDuration = bluetoothReader.getBluetoothActiveDuration();

		assertEquals(77.086, ((double)Math.round(activeDuration*1000.0))/1000, 0); // bcn faked
	//	assertEquals(77.086, activeDuration, 0);
		
	}	
	
	@Test
	public void fileDoesntExist(){
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(false);
		List<BluetoothInfo> bluetoothInfo = bluetoothReader.readData("/", 0, 0);
		assertEquals(0, bluetoothInfo.size());
	}
	
}
