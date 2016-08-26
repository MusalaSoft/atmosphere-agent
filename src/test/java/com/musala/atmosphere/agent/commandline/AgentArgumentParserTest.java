package com.musala.atmosphere.agent.commandline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;

import org.junit.Test;

import com.musala.atmosphere.commons.exceptions.ArgumentParseException;

/**
 * 
 * @author yordan.petrov
 * 
 */
public class AgentArgumentParserTest {
    private AgentArgumentParser argumentParser = new AgentArgumentParser();

    @Test
    public void testGetHostnameByValidAddress() throws Exception {
        String validHostnameAddress = "www.google.com";

        InetAddress parsedAddress = argumentParser.getHostname(validHostnameAddress);
        assertNotNull("Got a null result after parsing a valid hostname address.", parsedAddress);
        assertEquals("The host name of the parsed address is not correct.",
                     validHostnameAddress,
                     parsedAddress.getHostName());
    }

    @Test
    public void testGetHostnameByValidIp() throws Exception {
        String validHostnameIp = "10.0.7.29";

        InetAddress parsedAddress = argumentParser.getHostname(validHostnameIp);
        assertNotNull("Got a null result after parsing a valid hostname address.", parsedAddress);
        assertEquals("The host address of the parsed address is not correct.",
                     validHostnameIp,
                     parsedAddress.getHostAddress());
    }

    @Test
    public void testGetHostnameByLocalhost() throws Exception {
        String localhost = "localhost";

        InetAddress parsedAddress = argumentParser.getHostname(localhost);
        assertNotNull("Got a null result after parsing a valid hostname address.", parsedAddress);
        assertEquals("The host name of the parsed address is not 'localhost'.", localhost, parsedAddress.getHostName());
    }

    @Test(expected = ArgumentParseException.class)
    public void testGetHostnameByInvalidAddressThrowsException() throws Exception {
        String invalidHostnameAddress = "www.google.atmosphere";

        InetAddress parsedAddress = argumentParser.getHostname(invalidHostnameAddress);
    }

    @Test(expected = ArgumentParseException.class)
    public void testGetHostnameByInvalidIpThrowsException() throws Exception {
        String invalidHostnameIp = "257.0.0.1";

        InetAddress parsedAddress = argumentParser.getHostname(invalidHostnameIp);
    }

    @Test(expected = ArgumentParseException.class)
    public void testGetNegativePortThrowsException() throws Exception {
        String negativePort = "-1";

        Integer parsedPort = argumentParser.getPort(negativePort);
    }

    @Test(expected = ArgumentParseException.class)
    public void testGetTooBigPortThrowsException() throws Exception {
        String tooBigPort = "70000";

        Integer parsedPort = argumentParser.getPort(tooBigPort);
    }

    @Test
    public void testGetValidPort() throws Exception {
        String validPort = "40000";
        Integer validPortInteger = Integer.parseInt(validPort);

        Integer parsedPort = argumentParser.getPort(validPort);
        assertNotNull("Got a null result after parsing a valid port number.", parsedPort);
        assertEquals("The integer representation of the parsed port is not correct.", validPortInteger, parsedPort);
    }

    @Test
    public void testGetOptionHostnameByValidAddress() throws Exception {
        String validHostnameAddress = "www.google.com";
        AgentOption hostnameOption = AgentOption.HOSTNAME;

        InetAddress parsedAddress = (InetAddress) argumentParser.getOption(hostnameOption, validHostnameAddress);
        assertNotNull("Got a null result after parsing a valid hostname address.", parsedAddress);
        assertEquals("The host name of the parsed address is not correct.",
                     validHostnameAddress,
                     parsedAddress.getHostName());
    }

    @Test(expected = ArgumentParseException.class)
    public void testGetOptionHostnameByInvalidAddressThrowsException() throws Exception {
        String invalidHostnameAddress = "www.google.atmosphere";
        AgentOption hostnameOption = AgentOption.HOSTNAME;

        InetAddress parsedAddress = (InetAddress) argumentParser.getOption(hostnameOption, invalidHostnameAddress);
    }

    @Test
    public void testGetValidOptionPort() throws Exception {
        String validPort = "40000";
        Integer validPortInteger = Integer.parseInt(validPort);
        AgentOption portOption = AgentOption.PORT;

        Integer parsedPort = (Integer) argumentParser.getOption(portOption, validPort);
        assertNotNull("Got a null result after parsing a valid port number.", parsedPort);
        assertEquals("The integer representation of the parsed port is not correct.", validPortInteger, parsedPort);
    }

    @Test(expected = ArgumentParseException.class)
    public void testGetInvalidOptionPortThrowsException() throws Exception {
        String invalidPort = "70000";
        AgentOption portOption = AgentOption.PORT;

        Integer parsedPort = (Integer) argumentParser.getOption(portOption, invalidPort);
    }

    private void assertNotNull(String message, Object actual) {
        assertTrue(message, actual != null);
    }

}
