package com.att.aro.core.util;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.att.aro.core.util.Util;

public class UtilTest {
	String aroJpcapLibName,aroJpcapLibFileName;
	@Before
	public void setup(){
		String osname = Util.OS_NAME, osarch = Util.OS_ARCHYTECTURE;
		if (Util.OS_NAME != null && Util.OS_ARCHYTECTURE != null) {
			if (osname.contains("Windows") && osarch.contains("64")) { // _______ 64 bit Windows jpcap64.DLL
				aroJpcapLibName = "jpcap64";
				aroJpcapLibFileName = aroJpcapLibName + ".dll";

			} else if (osname.contains("Windows")) { // _________________________ 32 bit Windows jpcap.DLL
				aroJpcapLibName = "jpcap";
				aroJpcapLibFileName = aroJpcapLibName + ".dll";

			} else if (osname.contains("Linux") && osarch.contains("amd64")) { // 64 bit Linux libjpcap64.so
				aroJpcapLibName = "jpcap64";
				aroJpcapLibFileName = "lib" + aroJpcapLibName + ".so";

			} else if (osname.contains("Linux") && osarch.contains("i386")) { //  32 bit Linux libjpcap.so
				aroJpcapLibName = "jpcap32";
				aroJpcapLibFileName = "lib" + aroJpcapLibName + ".so";

			} else { // _________________________________________________________ Mac OS X libjpcap.jnilib
				aroJpcapLibName = "jpcap";
				aroJpcapLibFileName = "lib" + aroJpcapLibName + ".jnilib";
			}
		}
	}
	@Test
	public void isMacOS(){
		boolean ismac = Util.isMacOS();
		String os = System.getProperty("os.name");
		boolean hasmac = os.contains("Mac");
		assertEquals(hasmac, ismac);
	}
	@Test
	public void isWindowsOS(){
		boolean iswin = Util.isWindowsOS();
		String os = System.getProperty("os.name");
		boolean haswin = os.contains("Windows");
		assertEquals(haswin, iswin);
	}
	@Test
	public void getAROTraceDirIOS(){
		String dirname = Util.getAROTraceDirIOS();
		boolean hasname = dirname.contains("AROTraceIOS");
		assertTrue(hasname);
	}
	@Test
	public void getAROTraceDirAndroid(){
		String dirname = Util.getAROTraceDirAndroid();
		boolean hasname = dirname.contains("AROTraceAndroid");
		assertTrue(hasname);
	}
	@Test
	public void getCurrentRunningDir(){
		String dir = Util.getCurrentRunningDir();
		assertNotNull(dir);
	}
	@Test
	public void escapeRegularExpressionChar(){
		String str = "string with regex . char $ % *";
		String newstr = Util.escapeRegularExpressionChar(str);
		boolean hasspecialchar = newstr.contains("\\$");
		assertEquals(true, hasspecialchar);
	}
	@Test
	public void getDefaultAppName(){
		String name = Util.getDefaultAppName("");
		assertEquals("unknown", name);
		name = Util.getDefaultAppName("test");
		assertEquals("test",name);
	}
	@Test
	public void getDefaultString(){
		String name = Util.getDefaultString("", "default");
		assertEquals("default",name);
		name = Util.getDefaultString(null, "default");
		assertEquals("default",name);
	}
	@Test
	public void isEmptyIsBlank(){
		boolean isemptyorblank = Util.isEmptyIsBlank(null);
		assertTrue(isemptyorblank);
		isemptyorblank = Util.isEmptyIsBlank(" ");
		assertTrue(isemptyorblank);
	}
	@Test
	public void normalizeTime(){
		double value = Util.normalizeTime(-0.00, 1.2);
		assertNotNull(value > 0);
	}
	@Test
	public void makeLibFilesFromJar(){
		String foldername = Util.makeLibFilesFromJar(aroJpcapLibFileName);
		assertNotNull(foldername);
		foldername = Util.makeLibFilesFromJar(null);
		assertNull(foldername);
	}
	@Test
	public void makeLibFolder(){
		String libfolder = Util.makeLibFolder(aroJpcapLibFileName, new File(aroJpcapLibName));
		assertNotNull(libfolder);
		libfolder = Util.makeLibFolder(aroJpcapLibFileName, new File(""));
		assertTrue(libfolder.equals(""));
		libfolder = Util.makeLibFolder("", new File(""));
		assertTrue(libfolder.equals(""));
	}
	@Test
	public void makeLibFile() {
		boolean result = Util.makeLibFile(aroJpcapLibFileName, aroJpcapLibFileName, null);
		assertFalse(result);
		result = Util.makeLibFile(null, null, null);
		assertFalse(result);
	}	
}
