package com.musala.atmosphere.agent.webview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.musala.atmosphere.commons.exceptions.AtmosphereConfigurationException;
import com.musala.atmosphere.commons.geometry.Point;
import com.musala.atmosphere.commons.util.Pair;
import com.musala.atmosphere.commons.webelement.action.WebElementAction;
import com.musala.atmosphere.commons.webelement.action.WebElementWaitCondition;
import com.musala.atmosphere.commons.webelement.exception.WebElementNotPresentException;

/**
 * Class responsible for initializing {@link ChromeDriver chrome driver} used for retrieving information for the
 * WebViews on the device screen.
 *
 * @author filareta.yordanova
 *
 */
public class WebElementManager {
    private static final long DEFAULT_WAIT_STEP = 100;

    private static final String USE_RUNNING_APPLICATION = "androidUseRunningApp";

    private static final String DEVICE_SERIAL_NUMBER = "androidDeviceSerial";

    private static final String APPLICATION_PACKAGE_NAME = "androidPackage";

    private static final String HIDDEN_ELEMENT = "hidden";

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
    public WebElementManager(ChromeDriverService service, String deviceSerialNumber) {
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
    public void initDriver(String packageName) {
        Map<String, Object> chromeOptions = new HashMap<String, Object>();
        chromeOptions.put(APPLICATION_PACKAGE_NAME, packageName);
        chromeOptions.put(DEVICE_SERIAL_NUMBER, deviceSerialNumber);
        chromeOptions.put(USE_RUNNING_APPLICATION, true);

        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);

        try {
            driver = new ChromeDriver(service, capabilities);
        } catch (SessionNotCreatedException e) {
            throw new AtmosphereConfigurationException(String.format("Another instance of the WebView in %s you are trying to interact with might already be opened for debugging.",
                                                                     packageName));
        } catch (WebDriverException e) {
            throw new AtmosphereConfigurationException(String.format("Applicaton with the provided package name %s may not contain a WebView or no WebView instance is present on the current screen.",
                                                                     packageName));
        }
    }

    /**
     * Finds a web element by the given xpath query.
     *
     * @param xpathQuery
     *        - the xpath query used for matching
     * @return {@link Map map} containing all attributes of the found element
     */
    public Map<String, Object> findElement(String xpathQuery) {
        WebElement foundElement = null;

        try {
            foundElement = driver.findElement(By.xpath(xpathQuery));
        } catch (NoSuchElementException e) {
            throw new WebElementNotPresentException(String.format("Web element for the requested query %s is not present on the screen!",
                                                                  xpathQuery));
        }

        return foundElement != null ? getWebElementAttributes(foundElement) : null;
    }

    /**
     * Finds a list of web elements by the given xpath query.
     *
     * @param xpathQuery
     *        - the xpath query used for matching
     * @return list of {@link Map attributes} corresponding to the found elements
     */
    public List<Map<String, Object>> findElements(String xpathQuery) {
        List<WebElement> webElements = driver.findElements(By.xpath(xpathQuery));

        List<Map<String, Object>> foundElements = new ArrayList<Map<String, Object>>();
        for (WebElement element : webElements) {
            foundElements.add(getWebElementAttributes(element));
        }

        return foundElements;
    }

    /**
     * Gets the value of a given CSS property.
     *
     * @param xpathQuery
     *        - the xpath query used for matching
     * @param cssProperty
     *        - the given CSS property
     * @return String representing the value of the wanted property
     */
    public String getCssValue(String xpathQuery, String cssProperty) {
        WebElement element = getWebElement(xpathQuery);

        return element.getCssValue(cssProperty);
    }

    /**
     * Taps on a web element.
     *
     * @param xpathQuery
     *        - the xpath query used for matching
     */
    private void tap(String xpathQuery) {
        WebElement element = getWebElement(xpathQuery);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    /**
     * Checks if the element by the xpath query is displayed.
     *
     * @param xpathQuery
     *        - the xpath query used for matching
     * @return <code>true</code> if the element is displayed, <code>false</code> otherwise
     */
    private boolean isDisplayed(String xpathQuery) {
        WebElement element = getWebElement(xpathQuery);

        if (element.getAttribute("type").equals(HIDDEN_ELEMENT)) {
            return false;
        }

        if (element.getCssValue("display").equals("none")) {
            return false;
        }

        if (element.getCssValue("visibility").equals(HIDDEN_ELEMENT)) {
            return false;
        }

        if (element.getCssValue("opacity").equals("0")) {
            return false;
        }

        Dimension elementSize = element.getSize();
        if (elementSize.getHeight() <= 0 || elementSize.getWidth() <= 0) {
            return false;
        }

        return true;
    }

    /**
     * Checks if the element by the xpath query is selected.
     *
     * @param xpathQuery
     *        - the xpath query used for matching
     * @return <code>true</code> if the element is selected, <code>false</code> otherwise
     */
    private boolean isSelected(String xpathQuery) {
        WebElement element = getWebElement(xpathQuery);
        return element.isSelected();
    }

    /**
     * Gets the tag name of the element.
     *
     * @param xpathQuery
     *        - the xpath query used for matching
     * @return String representing the tag name of the element
     */
    private String getTagName(String xpathQuery) {
        WebElement element = getWebElement(xpathQuery);
        return element.getTagName();
    }

    /**
     * Get the visible (i.e. not hidden by CSS) innerText of this element, including sub-elements, without any leading
     * or trailing whitespace.
     *
     * @param xpathQuery
     *        - the xpath query used for matching
     * @return the inner text of this element
     */
    private String getText(String xpathQuery) {
        WebElement element = getWebElement(xpathQuery);
        return element.getAttribute("textContent");
    }

    /**
     * Gets the upper left corner location of the element relative to the WebView that contains the element.
     *
     * @param xpathQuery
     *        - the xpath query used for matching
     * @return {@link Point} containing the relative position of the element
     */
    private Point getRelativePosition(String xpathQuery) {
        WebElement element = getWebElement(xpathQuery);
        return new Point(element.getLocation().getX(), element.getLocation().getY());
    }

    /**
     * Executes action on the web element selected by the xpath query.
     *
     * @param webElementAction
     *        - type of the action that will be executed
     * @param xpathQuery
     *        - the xpath query used for matching
     * @return {@link Object} containing the result of the executed action
     */
    public Object executeAction(WebElementAction webElementAction, String xpathQuery) {
        switch (webElementAction) {
            case IS_SELECTED:
                return isSelected(xpathQuery);
            case IS_DISPLAYED:
                return isDisplayed(xpathQuery);
            case GET_TAG_NAME:
                return getTagName(xpathQuery);
            case GET_SIZE:
                return getSize(xpathQuery);
            case GET_TEXT:
                return getText(xpathQuery);
            case TAP:
                tap(xpathQuery);
                break;
            case FOCUS:
                focus(xpathQuery);
                break;
            case GET_POSITION:
                return getRelativePosition(xpathQuery);
            default:
                return null;
        }
        return null;
    }

    /**
     * Closes the instance of the driver used for retrieving data from the corresponding WebView present on the screen.
     */
    public void closeDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Gets a web element by the given xpath query.
     *
     * @param xpathQuery
     *        - the xpath query used for matching
     * @return the web element instance
     */
    private WebElement getWebElement(String xpathQuery) {
        try {
            return driver.findElement(By.xpath(xpathQuery));
        } catch (NoSuchElementException e) {
            throw new WebElementNotPresentException(String.format("The Web element for the requested query is no longer present on the screen!",
                                                                  xpathQuery));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getWebElementAttributes(WebElement element) {
        JavascriptExecutor javascriptDriver = (JavascriptExecutor) driver;

        Map<Object, Object> attributes = (Map<Object, Object>) javascriptDriver.executeScript("var items = {}; for (index = 0; index < arguments[0].attributes.length; ++index) { items[arguments[0].attributes[index].name] = arguments[0].attributes[index].value }; return items;",
                                                                                              element);

        // The class of the returned map is Maps.TransformedEntriesMap<K,V1,V2>, which is not serializable and needs to
        // be converted to a hash map
        Map<String, Object> elementAttributes = new HashMap<String, Object>();

        for (Entry<Object, Object> entry : attributes.entrySet()) {
            elementAttributes.put((String) entry.getKey(), entry.getValue());
        }

        return elementAttributes;
    }

    /**
     * Gets the size of a web element by the given xpath query.
     *
     * @param xpathQuery
     *        - the xpath query used for matching
     * @return size of the web element
     */
    private Pair<Integer, Integer> getSize(String xpathQuery) {
        WebElement element = getWebElement(xpathQuery);
        Dimension dimensions = element.getSize();
        Pair<Integer, Integer> size = new Pair<Integer, Integer>(dimensions.getWidth(), dimensions.getHeight());
        return size;
    }

    /**
     * Focuses a web element by the given xpath query.
     *
     * @param xpathQuery
     *        - the xpath query used for matching
     */
    private void focus(String xpathQuery) {
        WebElement element = getWebElement(xpathQuery);
        ((JavascriptExecutor) driver).executeScript("arguments[0].focus();", element);
    }

    /**
     * Waits for a web element to meet a given {@link WebElementWaitCondition}, with given timeout.
     *
     * @param xpathQuery
     *        - the xpath query used for matching
     * @param condition
     *        - {@link WebElementWaitCondition} that the web element needs to meet
     * @param timeout
     *        - the given timeout
     * @return <code>true</code> if the element meets the condition, before the end of the given timeout,
     *         <code>false</code> otherwise
     */
    public boolean waitForCondition(String xpathQuery, WebElementWaitCondition condition, int timeout) {
        switch (condition) {
            case ELEMENT_EXISTS:
                return waitForElementExists(xpathQuery, timeout);
            default:
                return false;
        }
    }

    /**
     * Waits for the existence of a given web element with a given timeout.
     *
     * @param xpathQuery
     *        - the xpath query used for matching
     * @param timeout
     *        - the given timeout in milliseconds
     * @return <code>true</code> if the element is present in the screen, before the given timeout, <code>false</code>
     *         otherwise
     */
    private boolean waitForElementExists(String xpathQuery, int timeout) {
        WebElement element = null;

        while (timeout > 0) {
            try {
                element = driver.findElement(By.xpath(xpathQuery));
                getWebElementAttributes(element);
                break;
            } catch (NoSuchElementException | StaleElementReferenceException e) {
                try {
                    Thread.sleep(DEFAULT_WAIT_STEP);
                    timeout -= DEFAULT_WAIT_STEP;
                } catch (InterruptedException e1) {
                }
            }
        }

        return element != null;
    }
}
