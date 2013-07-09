package com.musala.atmosphere.agent.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AgentPropertiesLoaderTest
{
	@Before
	public void setUp()
	{

	}

	@After
	public void tearDown()
	{

	}

	@Test(expected = NullPointerException.class)
	public void testSetPropertyWithNullValueProperty() throws IOException
	{
		AgentPropertiesLoader.setProperty(AgentProperties.PATH_TO_ADB, null);
	}

	@Test
	public void testSetPropertyWithValidParameters() throws IOException
	{
		final String path = "C:\\dev\\prj";
		AgentPropertiesLoader.setProperty(AgentProperties.PATH_TO_ADB, path);
	}

	@Test(expected = NullPointerException.class)
	public void testSetPropertyWithNullProperyName() throws IOException
	{
		AgentPropertiesLoader.setProperty(null, null);
	}

	@Test
	public void testSetAndGetProperty() throws IOException
	{
		final String path = "C:\\dev\\prj";
		AgentPropertiesLoader.setProperty(AgentProperties.PATH_TO_ADB, path);
		assertEquals(AgentPropertiesLoader.getPropertyString(AgentProperties.PATH_TO_ADB), path);
	}

	@Test
	public void testGetProperty() throws IOException
	{
		final String path = "C:\\dev\\prj";
		AgentPropertiesLoader.setProperty(AgentProperties.PATH_TO_ADB, path);
		AgentPropertiesLoader.setProperty(AgentProperties.PATH_TO_ADB, "bqlh");
		AgentPropertiesLoader.setProperty(AgentProperties.ADBRIDGE_TIMEOUT, "12050");
		AgentPropertiesLoader.setProperty(AgentProperties.PATH_TO_ADB, path);
		assertEquals(AgentPropertiesLoader.getPropertyString(AgentProperties.PATH_TO_ADB), "C:\\dev\\prj");
	}

	@Test
	public void testGetPropertyWithChangedValues() throws IOException
	{
		final String path = "C:\\dev\\prj";
		final String returnString = "blqh";
		AgentPropertiesLoader.setProperty(AgentProperties.PATH_TO_ADB, path);
		AgentPropertiesLoader.setProperty(AgentProperties.PATH_TO_ADB, returnString);
		AgentPropertiesLoader.setProperty(AgentProperties.ADBRIDGE_TIMEOUT, "12050");
		assertEquals(AgentPropertiesLoader.getPropertyString(AgentProperties.PATH_TO_ADB), returnString);
	}

	@Test
	public void testSetAndGetPropertyWithEqulasSign() throws IOException
	{
		final String randomSymbols = "C\\=pet1";
		AgentPropertiesLoader.setProperty(AgentProperties.PATH_TO_ADB, randomSymbols);
		assertEquals("", AgentPropertiesLoader.getPropertyString(AgentProperties.PATH_TO_ADB), randomSymbols);
	}
}
