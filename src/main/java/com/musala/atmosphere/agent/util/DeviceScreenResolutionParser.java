// This file is part of the ATMOSPHERE mobile testing framework.
// Copyright (C) 2016 MusalaSoft
//
// ATMOSPHERE is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// ATMOSPHERE is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ATMOSPHERE.  If not, see <http://www.gnu.org/licenses/>.

package com.musala.atmosphere.agent.util;

import com.musala.atmosphere.commons.util.Pair;

/**
 * <p>
 * Provides static methods that parse input and return a pair of integers - device screen resolution.
 * </p>
 * 
 * @author georgi.gaydarov
 * 
 */
public class DeviceScreenResolutionParser {
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
    public static Pair<Integer, Integer> parseScreenResolutionFromShell(String shellResponse) {
        // Isolate the important line from the response
        int importantLineStart = shellResponse.indexOf("mUnrestrictedScreen");
        int importantLineEnd = shellResponse.indexOf('\r', importantLineStart);
        if (importantLineEnd == -1) {
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

        Pair<Integer, Integer> screenResolution = new Pair<Integer, Integer>(width, height);
        return screenResolution;
    }
}
