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

package com.musala.atmosphere.agent;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.android.ddmlib.Log.ILogOutput;
import com.android.ddmlib.Log.LogLevel;

/**
 * Log listener for debug purposes.
 * 
 * @author georgi.gaydarov
 * 
 */
public class DdmLibLogListener implements ILogOutput {
    private static boolean fileLogging = false;

    private final static Logger LOGGER = Logger.getLogger(AgentManager.class.getName());

    public DdmLibLogListener(Level logLevel, boolean logToFile) {
        LOGGER.setLevel(logLevel);
        if (logToFile && fileLogging == false) {
            try {
                Handler fileHandler = new FileHandler("DdmLibLogListener.log");
                LOGGER.addHandler(fileHandler);
                fileLogging = true;
            } catch (SecurityException | IOException e) {
                // Could not create the log file.
                // Well, we can't log this...
                e.printStackTrace();
            }
        }
    }

    @Override
    public void printAndPromptLog(LogLevel logLevel, String arg1, String arg2) {
        Level loggerInputLevel;
        switch (logLevel.getStringValue()) {
            case "VERBOSE":
                loggerInputLevel = Level.FINE;
                break;
            case "DEBUG":
                loggerInputLevel = Level.CONFIG;
                break;
            case "INFO":
                loggerInputLevel = Level.INFO;
                break;
            case "WARN":
                loggerInputLevel = Level.WARNING;
                break;
            case "ERROR":
                loggerInputLevel = Level.WARNING;
                break;
            case "ASSERT":
                loggerInputLevel = Level.SEVERE;
                break;
            default:
                loggerInputLevel = Level.INFO;
                break;
        }
        LOGGER.log(loggerInputLevel, arg1 + " " + arg2);
    }

    @Override
    public void printLog(LogLevel logLevel, String arg1, String arg2) {
        Level loggerInputLevel;
        switch (logLevel.getStringValue()) {
            case "VERBOSE":
                loggerInputLevel = Level.FINE;
                break;
            case "DEBUG":
                loggerInputLevel = Level.CONFIG;
                break;
            case "INFO":
                loggerInputLevel = Level.INFO;
                break;
            case "WARN":
                loggerInputLevel = Level.WARNING;
                break;
            case "ERROR":
                loggerInputLevel = Level.WARNING;
                break;
            case "ASSERT":
                loggerInputLevel = Level.SEVERE;
                break;
            default:
                loggerInputLevel = Level.INFO;
                break;
        }
        LOGGER.log(loggerInputLevel, arg1 + " " + arg2);
    }

}
