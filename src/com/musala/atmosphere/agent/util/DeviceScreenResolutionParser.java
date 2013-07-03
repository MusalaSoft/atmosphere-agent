package com.musala.atmosphere.agent.util;

import com.musala.atmosphere.commons.Pair;

/**
 * <p>
 * Provides static methods that parse input and return a pair of integers - device screen resolution.
 * </p>
 * 
 * @author georgi.gaydarov
 * 
 */
public class DeviceScreenResolutionParser
{
	/**
	 * Converts the shell response from the "dumpsys window policy" command to a pair of integers.
	 * 
	 * @param shellResponse
	 *        String response from the shell command execution
	 * @return Pair of integers - key is width, value is height in pixels.
	 */
	/**
	 * Converts the shell response from the "dumpsys window policy" command to a pair of integers.
	 * 
	 * @param shellResponse
	 *        String response from the shell command execution
	 * @return Pair of integers - key is width, value is height in pixels.
	 */
	public static Pair<Integer, Integer> parseScreenResolutionFromShell(String shellResponse)
	{
		// Isolate the important line from the response
		int importantLineStart = shellResponse.indexOf("mUnrestrictedScreen");
		int importantLineEnd = shellResponse.indexOf('\r', importantLineStart);
		if (importantLineEnd == -1)
		{
			importantLineEnd = shellResponse.indexOf('\n', importantLineStart);
		}

		String importantLine = shellResponse.substring(importantLineStart, importantLineEnd);

		// Isolate the values from the line
		int valueStartIndex = importantLine.indexOf(' ');
		String importantValue = importantLine.substring(valueStartIndex + 1);

		// The values are in the form [integer]x[integer]
		int delimiterIndex = importantValue.indexOf('x');
		String widthString = importantValue.substring(0, delimiterIndex);
		String heightString = importantValue.substring(delimiterIndex + 1);

		int width = Integer.parseInt(widthString);
		int height = Integer.parseInt(heightString);

		Pair<Integer, Integer> screenResolution = new Pair<Integer, Integer>(height, width);
		return screenResolution;
	}
}
