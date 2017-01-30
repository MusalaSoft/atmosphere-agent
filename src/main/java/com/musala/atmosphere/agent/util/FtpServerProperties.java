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
    FTPS("ftps");

    private String value;

    private FtpServerProperties(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
