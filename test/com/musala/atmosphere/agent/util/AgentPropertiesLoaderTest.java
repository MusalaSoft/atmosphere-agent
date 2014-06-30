package com.musala.atmosphere.agent.util;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AgentPropertiesLoaderTest {
    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetProperty() throws IOException {
        String pathToADB = AgentPropertiesLoader.getADBPath();
        assertNotNull("Returns property.", pathToADB);
    }
}
