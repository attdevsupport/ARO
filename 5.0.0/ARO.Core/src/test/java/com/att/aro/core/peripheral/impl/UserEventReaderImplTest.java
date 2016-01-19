package com.att.aro.core.peripheral.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.peripheral.IUserEventReader;
import com.att.aro.core.peripheral.pojo.UserEvent;

public class UserEventReaderImplTest extends BaseTest {

	UserEventReaderImpl traceDataReader;

	private IFileManager filereader;
	private String traceFolder = "traceFolder";

	@Before
	public void setup() {
		filereader = Mockito.mock(IFileManager.class);
		traceDataReader = (UserEventReaderImpl)context.getBean(IUserEventReader.class);
		traceDataReader.setFileReader(filereader);
	}

	@Test
	public void readData() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] {             
				 "1414108436.923000 screen press"            // 00	0
				,"1414108436.957000 screen release"          // 01
				,""
				,"1414108463.785000 key volup press"         // 02	1
				,"1414108464.059000 key volup release"       // 03
				,"1414108466.065000 key voldown press"       // 04	2
				,"1414108466.396000 key voldown release"     // 05
				,"1414108473.096000 key power press"         // 06	3
				,"1414108473.512000 key power release"       // 07
				,"1414108473.512000 key ball press"          // 08	4
				,"1414108473.512000 key ball release"        // 09
				,"1414108473.512000 key home press"          // 20	5
				,"1414108473.512000 key home release"        // 21
				,"1414108473.512000 key menu press"          // 22	6
				,"1414108473.512000 key menu release"        // 23
				,"1414108473.512000 key back press"          // 24	7
				,"1414108473.512000 key back release"        // 25
				,"1414108473.512000 key search press"        // 26  8 
				,"1414108473.512000 key search release"      // 27   
				,"1414108473.512000 key green press"         // 28  9 
				,"1414108473.512000 key green release"       // 29   
				,"1414108473.512000 key red press"           // 30  10 
				,"1414108473.512000 key red release"         // 31   
				,"1414108473.512000 key key press"           // 32  10 
				,"1414108473.512000 key key release"         // 33   
				,""
		});
		
		List<UserEvent> listUserEvent = traceDataReader.readData(traceFolder, 0, 0);
		assertEquals(12.0, listUserEvent.size(), 0);
		
		assertEquals("SCREEN_TOUCH", listUserEvent.get(0).getEventType().toString());		
		assertEquals("KEY_VOLUP",    listUserEvent.get(1).getEventType().toString());
		assertEquals("KEY_VOLDOWN",  listUserEvent.get(2).getEventType().toString());
		assertEquals("KEY_POWER",    listUserEvent.get(3).getEventType().toString());
		assertEquals("KEY_BALL",    listUserEvent.get(4).getEventType().toString());
		assertEquals("KEY_HOME",    listUserEvent.get(5).getEventType().toString());
		assertEquals("KEY_MENU",    listUserEvent.get(6).getEventType().toString());
		assertEquals("KEY_BACK",    listUserEvent.get(7).getEventType().toString());

		assertEquals(1414108436.923000, listUserEvent.get(0).getPressTime(), 0);
		assertEquals(1414108436.957000, listUserEvent.get(0).getReleaseTime(), 0);
		assertEquals(1414108463.785000, listUserEvent.get(1).getPressTime(), 0);
		assertEquals(1414108464.059000, listUserEvent.get(1).getReleaseTime(), 0);
		assertEquals(1414108466.065000, listUserEvent.get(2).getPressTime(), 0);
		assertEquals(1414108466.396000, listUserEvent.get(2).getReleaseTime(), 0);
		assertEquals(1414108473.096000, listUserEvent.get(3).getPressTime(), 0);
		assertEquals(1414108473.512000, listUserEvent.get(3).getReleaseTime(), 0);

	}
	
	
	@Test
	public void readData_InvalidUserEvent() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] {             
				 "1414108436.923000 screen press"            // 00	0
				,"1414108436.957000 screen release invalid"          // 01
				,"1414108436.957000 screen release"          // 01
				,"1414108463.785000 key green press invalid"         // 02	1
				,"1414108464.059000 key green release invalid"       // 03
		});
		
		List<UserEvent> listUserEvent = traceDataReader.readData(traceFolder, 0, 0);
		assertEquals(1, listUserEvent.size(), 0);
	}

	/**
	 * Test shows a problem with code.<p>
	 * Parsing from String into a Number should have try/catch handling<li>
	 * See UserEventReaderImpl.java:74
	 * @throws IOException
	 */
	@Ignore
	@Test(expected=NumberFormatException.class)
	public void readData_FailedToParse() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] {             
				 "14141z8436.923000 screen press"            // 00	0
				,"1414108436.957000 screen release invalid"          // 01
				,"1414108436.957000 screen release"          // 01
				,"1414108463.785000 key green press invalid"         // 02	1
				,"1414108464.059000 key green release invalid"       // 03
		});
		
		List<UserEvent> listUserEvent = traceDataReader.readData(traceFolder, 0, 0);
		assertEquals(0, listUserEvent.size(), 0);
	}

	@Test
	public void readData_NoFile() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(false);

		List<UserEvent> listUserEvent = traceDataReader.readData(traceFolder, 0, 0);
		assertEquals(0, listUserEvent.size(), 0);
	}

	@Test
	public void readData_IOException() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenThrow(new IOException("test exception"));
		List<UserEvent> listUserEvent = null;
		listUserEvent = traceDataReader.readData(traceFolder, 0, 0);
		assertEquals(0, listUserEvent.size(), 0);
	}

}           
