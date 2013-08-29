package com.musala.atmosphere.agent.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

/**
 * 
 * @author valyo.yolovski
 * 
 */
public class SystemInformation
{
	private final static Logger LOGGER = Logger.getLogger(AgentPropertiesLoader.class.getCanonicalName());

	/**
	 * Returns true if IntelHaxm is available and false if it is not.
	 * 
	 * @return
	 */
	public static boolean checkHaxmAvailability()
	{
		try
		{
			String[] command = {"cmd.exe", "/C", "sc query intelhaxm"};
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(command);
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = reader.readLine();
			while (line != null)
			{
				if (line.contains("SERVICE_NAME: intelhaxm"))
				{
					LOGGER.info("Haxm available.");
					return true;
				}

				line = reader.readLine();
			}
		}
		catch (IOException e)
		{
			LOGGER.warn("Could not execute haxm availability validation command.", e);
		}
		return false;
	}

	/**
	 * Returns the free disk space on the hard disk in bytes.
	 * 
	 * @return
	 */
	public static long getFreeDiskSpace()
	{
		File[] roots = File.listRoots();
		long freeSpace = 0;
		for (File root : roots)
		{
			freeSpace = freeSpace + root.getFreeSpace();
		}
		return freeSpace;
	}
}
