package uk.ac.manchester.cs.snee.common;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SNEEPropertiesTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Configure logging
		PropertyConfigurator.configure(
				SNEEPropertiesTest.class.getClassLoader().getResource(
						"etc/log4j.properties"));
	}

	private Properties props;

	@Before
	public void setUp() throws Exception {
		props = new Properties();
		props.setProperty(
				SNEEPropertyNames.INPUTS_TYPES_FILE, "etc/Types.xml");
		props.setProperty(
				SNEEPropertyNames.INPUTS_UNITS_FILE, "etc/units.xml");
		props.setProperty(
				SNEEPropertyNames.GENERAL_OUTPUT_ROOT_DIR, "output");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected=SNEEConfigurationException.class)
	public void testSNEEProperties_noProps()
	throws SNEEConfigurationException {
		SNEEProperties.initialise(new Properties());
	}
	
	@Test(expected=SNEEConfigurationException.class)
	public void testSNEEProperties_noTypes()
	throws SNEEConfigurationException {
		Properties properties = new Properties();
		properties.setProperty(
				SNEEPropertyNames.INPUTS_UNITS_FILE, "etc/units.xml");
		properties.setProperty(
				SNEEPropertyNames.GENERAL_OUTPUT_ROOT_DIR, "output");
		SNEEProperties.initialise(properties);
	}	
	
	@Test(expected=SNEEConfigurationException.class)
	public void testSNEEProperties_noUnits()
	throws SNEEConfigurationException {
		Properties properties = new Properties();
		properties.setProperty(
				SNEEPropertyNames.INPUTS_TYPES_FILE, "etc/Types.xml");
		properties.setProperty(
				SNEEPropertyNames.GENERAL_OUTPUT_ROOT_DIR, "output");
		SNEEProperties.initialise(properties);
	}	
	
	@Test(expected=SNEEConfigurationException.class)
	public void testSNEEProperties_noOutput()
	throws SNEEConfigurationException {
		Properties properties = new Properties();
		properties.setProperty(
				SNEEPropertyNames.INPUTS_TYPES_FILE, "etc/Types.xml");
		properties.setProperty(
				SNEEPropertyNames.INPUTS_UNITS_FILE, "etc/units.xml");
		SNEEProperties.initialise(properties);
	}	
	
	@Test
	public void testSNEEProperties() 
	throws SNEEConfigurationException, URISyntaxException {
		SNEEProperties.initialise(props);
		File dir = new File(props.getProperty(
				SNEEPropertyNames.GENERAL_OUTPUT_ROOT_DIR));
//		System.out.println(dir);
		assertTrue(dir.exists());
	}
	
	@Test
	public void testGenerateGraphsFalse() 
	throws SNEEConfigurationException {
		props.setProperty(
				SNEEPropertyNames.GENERATE_QEP_IMAGES, "false");
		SNEEProperties.initialise(props);
		assertEquals("false", 
				SNEEProperties.getSetting("compiler.generate_graphs"));
	}

	@Test
	public void testGenerateGraphsTrue() 
	throws SNEEConfigurationException {
		props.setProperty(
				SNEEPropertyNames.GENERATE_QEP_IMAGES, "true");
		props.setProperty(
				SNEEPropertyNames.GRAPHVIZ_EXE, "/usr/local/bin/dot");
		SNEEProperties.initialise(props);
		assertEquals("true", 
				SNEEProperties.getSetting(
						SNEEPropertyNames.GENERATE_QEP_IMAGES));
	}
	
	@Test
	public void testIsSet_true() throws SNEEConfigurationException {
		SNEEProperties.initialise(props);
		assertTrue(SNEEProperties.isSet(
				SNEEPropertyNames.INPUTS_TYPES_FILE));
	}
	
	@Test
	public void testIsSet_false() throws SNEEConfigurationException {
		SNEEProperties.initialise(props);
		assertFalse(SNEEProperties.isSet(
				SNEEPropertyNames.GRAPHVIZ_EXE));
	}

	@Test(expected=SNEEConfigurationException.class)
	public void testGenerateAndConvertGraphsTrueNoGraphViz() 
	throws SNEEConfigurationException {
		props.setProperty(
				SNEEPropertyNames.GENERATE_QEP_IMAGES, "true");
		props.setProperty(
				SNEEPropertyNames.CONVERT_QEP_IMAGES, "true");
		SNEEProperties.initialise(props);
//		props.list(System.out);
		SNEEProperties.getSetting(
				SNEEPropertyNames.GENERATE_QEP_IMAGES);
	}

	@Test
	public void testGenerateGraphsTrueNoGraphViz() 
	throws SNEEConfigurationException {
		props.setProperty(
				SNEEPropertyNames.GENERATE_QEP_IMAGES, "true");
		SNEEProperties.initialise(props);
//		props.list(System.out);
		SNEEProperties.getSetting(
				SNEEPropertyNames.GENERATE_QEP_IMAGES);
	}

	@Test
	public void testGenerateGraphsFalseGraphViz() 
	throws SNEEConfigurationException {
		props.setProperty(
				SNEEPropertyNames.GENERATE_QEP_IMAGES, "false");
		props.setProperty(
				SNEEPropertyNames.CONVERT_QEP_IMAGES, "false");
		props.setProperty(
				SNEEPropertyNames.GRAPHVIZ_EXE, "something");
		SNEEProperties.initialise(props);
//		props.list(System.out);
		SNEEProperties.getSetting(
				SNEEPropertyNames.GENERATE_QEP_IMAGES);
	}

	@Test
	public void testGenerateGraphsTrueGraphViz() 
	throws SNEEConfigurationException {
		props.setProperty(
				SNEEPropertyNames.GENERATE_QEP_IMAGES, "true");
		props.setProperty(
				SNEEPropertyNames.CONVERT_QEP_IMAGES, "true");
		props.setProperty(
				SNEEPropertyNames.GRAPHVIZ_EXE, "something");
		SNEEProperties.initialise(props);
//		props.list(System.out);
		SNEEProperties.getSetting(
				SNEEPropertyNames.GENERATE_QEP_IMAGES);
	}
	
	@Test(expected=SNEEConfigurationException.class)
	public void testGetFile_notExists() 
	throws SNEEConfigurationException {
		SNEEProperties.initialise(props);
//		props.list(System.out);
		SNEEProperties.getFilename(
				SNEEPropertyNames.INPUTS_LOGICAL_SCHEMA_FILE);
	}
	
	@Test
	public void testGetFile_exists() 
	throws SNEEConfigurationException, MalformedURLException {
		props.setProperty(
				SNEEPropertyNames.INPUTS_LOGICAL_SCHEMA_FILE, 
				"etc/logical-schema.xml");
		SNEEProperties.initialise(props);
//		props.list(System.out);
		URL fileURL = 
			SNEEPropertiesTest.class.getClassLoader().
			getResource("etc/logical-schema.xml");
//		System.out.println(fileURL);
		/* 
		 * Have to test as a URL otherwise there is a 
		 * file separator issue when this test is run on windows.
		 */
		File file = new File(SNEEProperties.getFilename(
				SNEEPropertyNames.INPUTS_LOGICAL_SCHEMA_FILE));
		assertEquals(fileURL, file.toURI().toURL());
	}

	@Test(expected=SNEEConfigurationException.class)
	public void testGetIntSetting_notExist() 
	throws SNEEConfigurationException {
		SNEEProperties.initialise(props);
		SNEEProperties.getIntSetting("intSetting");
	}

	@Test
	public void testGetIntSetting_exist() 
	throws SNEEConfigurationException {
		props.setProperty("intSetting", "20");
		SNEEProperties.initialise(props);
		assertEquals(20, SNEEProperties.getIntSetting("intSetting"));
	}

	@Test(expected=SNEEConfigurationException.class)
	public void testGetIntSetting_numberConversionIssue() 
	throws SNEEConfigurationException {
		props.setProperty("intSetting", "twenty");
		SNEEProperties.initialise(props);
		SNEEProperties.getIntSetting("intSetting");
	}
	
}
