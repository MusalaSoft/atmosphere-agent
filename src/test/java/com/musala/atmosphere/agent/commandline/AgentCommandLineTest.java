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

package com.musala.atmosphere.agent.commandline;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;

import org.junit.BeforeClass;
import org.junit.Test;

import com.musala.atmosphere.commons.commandline.CommandLineOption;
import com.musala.atmosphere.commons.exceptions.ArgumentParseException;
import com.musala.atmosphere.commons.exceptions.CommandLineParseException;
import com.musala.atmosphere.commons.exceptions.OptionNotPresentException;

/**
 * 
 * @author yordan.petrov
 * 
 */
public class AgentCommandLineTest {
    private static final String INVALID_HOSTNAME = "www.google.atmosphere";

    private static final String VALID_HOSTNAME = "localhost";

    private static InetAddress expectedParsedHostname;

    private static final String INVALID_PORT = "70000";

    private static final String VALID_PORT = "1980";

    private static Integer expectedParsedPort;

    private static AgentOption hostnameOption = AgentOption.HOSTNAME;

    private static AgentOption portOption = AgentOption.PORT;

    private static CommandLineOption hostnameClOption = hostnameOption.getOption();

    private static String hostnameLongName = "--" + hostnameClOption.getLongName();

    private static String hostnameShortName = "-" + hostnameClOption.getShortName();

    private static CommandLineOption portClOption = portOption.getOption();

    private static String portLongName = "--" + portClOption.getLongName();

    private static String portShortName = "-" + portClOption.getShortName();

    private static String[] emptyArguments = new String[] {};

    private static String[] validShortArguments = new String[] {hostnameShortName, VALID_HOSTNAME, portShortName,
            VALID_PORT};

    private static String[] validLongArguments = new String[] {hostnameLongName, VALID_HOSTNAME, portLongName,
            VALID_PORT};

    private static String[] missingPortArguments = new String[] {hostnameLongName, VALID_HOSTNAME};

    private static String[] missingHostnameArguments = new String[] {portLongName, VALID_PORT};

    private static String[] invalidOptionArguments = new String[] {hostnameLongName, INVALID_HOSTNAME, portLongName,
            INVALID_PORT};

    private static String[] invalidArguments = new String[] {"-i", "am", "not", "valid"};

    private static AgentCommandLine commandLine = new AgentCommandLine();

    @BeforeClass
    public static void setUp() throws Exception {
        expectedParsedHostname = InetAddress.getByName(VALID_HOSTNAME);
        expectedParsedPort = Integer.parseInt(VALID_PORT);
    }

    @Test
    public void testNoExceptionIsThrownOnParseEmptyArguments() throws Exception {
        commandLine.parseArguments(emptyArguments);
    }

    @Test(expected = OptionNotPresentException.class)
    public void testGetHostnameFromEmptyArgumentsThrowsException() throws Exception {
        commandLine.parseArguments(emptyArguments);

        commandLine.getHostname();
    }

    @Test(expected = OptionNotPresentException.class)
    public void testGetPortFromEmptyArgumentsThrowsException() throws Exception {
        commandLine.parseArguments(emptyArguments);

        commandLine.getPort();
    }

    @Test(expected = OptionNotPresentException.class)
    public void testGetHostnameFromMissingHostnameArgumentsThrowsException() throws Exception {
        commandLine.parseArguments(missingHostnameArguments);

        commandLine.getHostname();
    }

    @Test(expected = OptionNotPresentException.class)
    public void testGetPortFromMissingPortArgumentsThrowsException() throws Exception {
        commandLine.parseArguments(missingPortArguments);

        commandLine.getPort();
    }

    @Test(expected = CommandLineParseException.class)
    public void testParseInvalidArgumentsThrowsException() throws Exception {
        commandLine.parseArguments(invalidArguments);
    }

    @Test
    public void testParseValidShortArguments() throws Exception {
        commandLine.parseArguments(validShortArguments);

        assertEquals("Parsed host name did not match expected value.",
                     expectedParsedHostname,
                     commandLine.getHostname());
        assertEquals("Parsed port did not match expected value.", expectedParsedPort, commandLine.getPort());
    }

    @Test(expected = ArgumentParseException.class)
    public void testGetHostnameFromInvalidOptionArgumentsThrowsException() throws Exception {
        commandLine.parseArguments(invalidOptionArguments);

        commandLine.getHostname();
    }

    @Test(expected = ArgumentParseException.class)
    public void testGetPortFromInvalidOptionArgumentsThrowsException() throws Exception {
        commandLine.parseArguments(invalidOptionArguments);

        commandLine.getPort();
    }
}
