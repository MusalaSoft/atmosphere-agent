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

import com.musala.atmosphere.commons.util.PropertiesLoader;

/**
 * Reads properties from the FTP Server properties config file.
 *
 * @author dimcho.nedev
 *
 */
public class FtpServerPropertiesLoader {
    private static final int DEFAULT_FTP_SERVER_PORT = 21;

    private static final String FTP_SERVER_PROPERTIES_FILE = "./ftpserver.properties";

    /**
     * Returns the desired property from the properties file in String type.
     *
     * @param property
     *        - the FTP Server property to be returned.
     * @return the desired FTP server property value.
     */
    private synchronized static String getPropertyString(FtpServerProperties property) {
        PropertiesLoader propertiesLoader = PropertiesLoader.getInstance(FTP_SERVER_PROPERTIES_FILE);
        String propertyString = property.toString();
        String resultProperty = propertiesLoader.getPropertyString(propertyString);

        return resultProperty;
    }

    /**
     * Gets the FTP Server name from the properties file.
     *
     * @return the FTP Server name
     */
    public static String getFtpName() {
        return getPropertyString(FtpServerProperties.FTP_NAME);
    }

    /**
     * Gets the username for the FTP connection.
     *
     * @return - the path to the Android Debug Bridge.
     */
    public static String getUsername() {
        String username = getPropertyString(FtpServerProperties.FTP_USERNAME).trim();

        return username;
    }

    /**
     * Gets the user password for the FTP connection.
     *
     * @return - the user password for the FTP connection
     */
    public static String getPassword() {
        String password = getPropertyString(FtpServerProperties.FTP_PASSWORD).trim();

        return password;
    }

    /**
     * Gets the FTP Server port from the properties file.
     *
     * @return - int, the specified FTP server port or the default FTP Server port(21) if the port is not specified in
     *         the config file.
     */
    public static int getFtpPort() {
        String ftpPort = getPropertyString(FtpServerProperties.FTP_PORT).trim();

        if (ftpPort.isEmpty()) {
            return DEFAULT_FTP_SERVER_PORT;
        }

        return Integer.parseInt(ftpPort);
    }

    /**
     * Return whether the FTP server is secured with SSL/TLS (FTPS).
     *
     * @return <code>true</code> if the FTP server is secured with SSL/TLS, otherwise returns <code>false</code>
     */
    public static boolean isFtps() {
        String isFtpsString = getPropertyString(FtpServerProperties.FTPS).trim();
        boolean isFtps = Boolean.parseBoolean(isFtpsString);

        return isFtps;
    }

    /**
     * Return the FTP home directory where all the user files and directories will be uploaded. Make sure this directory
     * exists and is writable.
     *
     * @return the FTP home directory
     */
    public static String getFtpHomeDirectory() {
        String ftpHomeDirectory = getPropertyString(FtpServerProperties.FTP_HOME_DIR).trim();

        return ftpHomeDirectory;
    }

}
