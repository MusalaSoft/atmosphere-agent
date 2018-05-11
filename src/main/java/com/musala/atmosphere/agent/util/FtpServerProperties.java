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

/**
 * Enumeration class containing an FTP Server properties.
 *
 * @author dimcho.nedev
 *
 */
public enum FtpServerProperties {
    FTP_NAME("ftp.name"),
    FTP_PORT("ftp.port"),
    FTP_USERNAME("ftp.username"),
    FTP_PASSWORD("ftp.password"),
    FTPS("ftps"),
    FTP_HOME_DIR("ftp.home");

    private String value;

    private FtpServerProperties(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
