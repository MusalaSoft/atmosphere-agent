package com.musala.atmosphere.agent.util;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SystemInformationTest
{

	@Before
	public void setUp()
	{

	}

	@After
	public void tearDown()
	{

	}

	@Test
	public void getFreeDiskSpaceTest()
	{
		long freeDiskSpace = SystemInformation.getFreeDiskSpace();
		assertNotNull("Returns free disk space.", freeDiskSpace);
	}

	@Test
	public void checkHaxmAvailabilityTest()
	{
		assertNotNull("Checks HAXM Availability successfully.", SystemInformation.checkHaxmAvailability());
	}

}
