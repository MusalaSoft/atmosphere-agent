package com.musala.atmosphere.agent.webview;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Class responsible for initializing {@link ChromeDriver chrome driver} used for retrieving information for the
 * WebViews on the device screen.
 * 
 * @author filareta.yordanova
 *
 */
public class ChromeDriverManager {
    private static final String USE_RUNNING_APPLICATION = "androidUseRunningApp";

    private static final String DEVICE_SERIAL_NUMBER = "androidDeviceSerial";

    private static final String APPLICATION_PACKAGE_NAME = "androidPackage";

    private ChromeDriverService service;

    private WebDriver driver;

    private String deviceSerialNumber;

    /**
     * Creates new instance of the driver manager for a device with the given serial number using the instance of the
     * chrome driver already started as a service.
     * 
     * @param service
     *        - instance of the chrome driver started as a service
     * @param deviceSerialNumber
     *        - serial number of the device
     */
    public ChromeDriverManager(ChromeDriverService service, String deviceSerialNumber) {
        this.service = service;
        this.deviceSerialNumber = deviceSerialNumber;
    }

    /**
     * Creates the suitable configuration using the package name of the application for this device and initializes the
     * instance of the driver used for retrieving information about WebViews present on the screen.
     * 
     * @param packageName
     *        - package name of the application under test
     * @throws IOException
     *         if initialization of the {@link ChromeDriver driver} fails
     */
    public void initDriver(String packageName) throws IOException {
        Map<String, Object> chromeOptions = new HashMap<String, Object>();
        chromeOptions.put(APPLICATION_PACKAGE_NAME, packageName);
        chromeOptions.put(DEVICE_SERIAL_NUMBER, deviceSerialNumber);
        chromeOptions.put(USE_RUNNING_APPLICATION, true);

        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);

        driver = new ChromeDriver(service, capabilities);
    }
}
