/**
 * 
 */
package com.att.aro.core.settings.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.EnumSet;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.att.aro.core.exception.AROInvalidAttributeException;
import com.att.aro.core.exception.ARORuntimeException;
import com.att.aro.core.settings.ISettings;
import com.att.aro.core.settings.impl.AROSettingsImpl;
import com.att.aro.core.settings.impl.SettingsImpl;

/**
 * @author Nathan F Syfrig
 *
 */
public class SettingsTest {
	private static final String configFilePath = AROSettingsImpl.CONFIG_FILE_PATH + ".test";

	private ISettings settings;
	private static File configFileFile;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		configFileFile = new File(configFilePath);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		settings = new SettingsImpl(configFilePath);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		configFileFile.delete();
	}


	@Test
	public void testAROConfigFile() {
		boolean saved = false;
		File defaultFile = new File(AROSettingsImpl.CONFIG_FILE_PATH);
		File savedFile = new File(AROSettingsImpl.CONFIG_FILE_PATH + ".save");

		if (defaultFile.exists()) {
			defaultFile.renameTo(savedFile);
			saved = true;
		}

		assertTrue(!defaultFile.exists());
		new AROSettingsImpl();
		assertTrue(defaultFile.exists());
		defaultFile.delete();
		assertTrue(!defaultFile.exists());

		if (saved) {
			savedFile.renameTo(defaultFile);
			assertTrue(defaultFile.exists());
		}
	}

	@Test
	public void testDefaultConstructor() {
		boolean saved = false;
		File defaultFile = new File(AROSettingsImpl.CONFIG_FILE_PATH);
		File savedFile = new File(AROSettingsImpl.CONFIG_FILE_PATH + ".save");

		if (defaultFile.exists()) {
			defaultFile.renameTo(savedFile);
			saved = true;
		}

		assertTrue(!defaultFile.exists());
		settings = new SettingsImpl();
		assertTrue(defaultFile.exists());
		defaultFile.delete();
		assertTrue(!defaultFile.exists());

		if (saved) {
			savedFile.renameTo(defaultFile);
			assertTrue(defaultFile.exists());
		}
	}

	@Test
	public void testNoConfigFilePresent() {
		configFileFile.delete();
		settings = new SettingsImpl(configFilePath);
		assertTrue(configFileFile.exists());
		Properties properties = ((SettingsImpl) settings).configProperties;
		assertEquals(0, properties.size());
	}

	@Test
	public void testAttributeReadWrite() {
		settings.setAndSaveAttribute("myTestAttribute", "myTestValue");
		settings = new SettingsImpl(configFilePath);
		Properties properties = ((SettingsImpl) settings).configProperties;
		assertEquals(properties.getProperty("myTestAttribute"), "myTestValue");
		assertEquals(settings.getAttribute("myTestAttribute"), "myTestValue");
	}

	@Test
	public void testAttributeNotEmptyReadWrite() {
		settings.setAttribute("myOtherAttribute", "myOtherValue");
		settings.setAndSaveAttribute("myTestAttribute", "myTestValue");
		settings = new SettingsImpl(configFilePath);
		assertEquals(settings.getAttribute("myOtherAttribute"), "myOtherValue");
		assertEquals(settings.getAttribute("myTestAttribute"), "myTestValue");
	}

	@Test
	public void testRemoveAttribute() {
		settings.setAndSaveAttribute("myTestAttribute", "myTestValue");
		settings.removeAndSaveAttribute("myTestAttribute");
		settings = new SettingsImpl(configFilePath);
		assertTrue(!"myTestValue".equals(settings.getAttribute("myTestAttribute")));
	}

	private enum ValidAttributesTestEnum {
		myOtherAttribute,
		myTestAttribute
	}

	@Test
	public void testBadAttribute() {
		settings = new SettingsImpl(configFilePath,
				EnumSet.allOf(ValidAttributesTestEnum.class));
		settings.setAttribute("myOtherAttribute", "myOtherValue");
		settings.setAndSaveAttribute("myTestAttribute", "myTestValue");
		settings = new SettingsImpl(configFilePath,
				EnumSet.allOf(ValidAttributesTestEnum.class));
		assertEquals(settings.getAttribute("myOtherAttribute"), "myOtherValue");
		assertEquals(settings.getAttribute("myTestAttribute"), "myTestValue");
		try {
			settings.setAttribute("myBadAttribute", "myBadValue");
			fail("Bad config attribute was allowed");
		}
		catch(AROInvalidAttributeException e) {
			if (e.getExceptionType() != ARORuntimeException.ExceptionType.invalidAttribute) {
				fail("Bad exception type for invalid attribute");
			}
		}
	}

}
