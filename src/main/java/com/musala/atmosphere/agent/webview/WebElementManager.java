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

package com.musala.atmosphere.agent.webview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.geometry.Point;
import com.musala.atmosphere.commons.util.Pair;
import com.musala.atmosphere.commons.webelement.action.WebElementAction;
import com.musala.atmosphere.commons.webelement.action.WebElementWaitCondition;
import com.musala.atmosphere.commons.webelement.exception.WebElementNotPresentException;
import com.musala.atmosphere.commons.webview.selection.WebViewSelectionCriterion;

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

    private int implicitWaitTimeout;

    /**
     * The window handlers are set of unique web view identifiers. The identifiers are different for each WebDriver
     * instance.
     */
    private Set<String> windowHandlers;

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
     */
    public void initDriver(String packageName) {
        Map<String, Object> chromeOptions = new HashMap<>();
        chromeOptions.put(APPLICATION_PACKAGE_NAME, packageName);
        chromeOptions.put(DEVICE_SERIAL_NUMBER, deviceSerialNumber);
        chromeOptions.put(USE_RUNNING_APPLICATION, true);

        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
        try {
            driver = new ChromeDriver(service, capabilities);
            this.windowHandlers = driver.getWindowHandles();
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

        List<Map<String, Object>> foundElements = new ArrayList<>();
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

        if (HIDDEN_ELEMENT.equals(element.getAttribute("type"))) {
            return false;
        }

        String hiddentElementDisplayValue = "none";
        if (hiddentElementDisplayValue.equals(element.getCssValue("display"))) {
            return false;
        }

        if (HIDDEN_ELEMENT.equals(element.getCssValue("visibility"))) {
            return false;
        }

        String hiddentElementOpacityValue = "0";
        if (hiddentElementOpacityValue.equals(element.getCssValue("opacity"))) {
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
        Map<String, Object> elementAttributes = new HashMap<>();

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
        Pair<Integer, Integer> size = new Pair<>(dimensions.getWidth(), dimensions.getHeight());
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
     * The timeout will override the implicit wait timeout if is specified.
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
        int cachedImplicitWaitTimeout = implicitWaitTimeout;

        // The explicit wait should override the implicit wait
        if(implicitWaitTimeout != 0) {
            setImplicitWait(0);
        }

        // TODO: avoid this "while" by using the WebDriver capabilities
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

        if (cachedImplicitWaitTimeout != 0) {
        	setImplicitWait(cachedImplicitWaitTimeout);
        }

        return element != null;
    }

    /**
     * Gets the window handlers for the current screen.
     *
     * @return a set of handlers(identifiers).
     */
    public Set<String> getWindowHandlers() {
        return this.windowHandlers;
    }

    /**
     * Switches the WebDriver to another web view window by xPath query of a child WebElement.
     *
     * @param xpathQuery
     *        - the xPath query used for matching
     * @throws CommandFailedException
     *         - thrown when fails to switch to another window by xPath query.
     */
    public void switchToWebViewByXpathQuery(String xpathQuery) throws CommandFailedException {
        this.switchToWebViewBy(WebViewSelectionCriterion.CHILD_ELEMENT, xpathQuery);
    }

    /**
     * Switches the WebDriver to another web view window by {@link WebViewSelectionCriterion criterion} and value.
     *
     * @param criterion
     *        - a criterion used for the web view selection
     * @param value
     *        - the value of the criterion
     * @throws CommandFailedException
     *         - thrown when fails to switch to another window
     * @return <code>true</code> if the switching is successful, <code>false</code> if it fails.
     */
    public boolean switchToWebViewBy(WebViewSelectionCriterion criterion, String value) throws CommandFailedException {
        if (windowHandlers.size() == 1) {
            return false;
        }
        String targetWindowHandler = null;

        for (String handler : windowHandlers) {
            driver.switchTo().window(handler);
            String searchResult = null;

            switch (criterion) {
                case URL:
                    searchResult = driver.getCurrentUrl();
                    if (searchResult.equals(value)) {
                        targetWindowHandler = checkTargetHandler(handler, criterion, targetWindowHandler);
                    }
                    break;
                case TITLE:
                    searchResult = driver.getTitle();
                    if (searchResult.equals(value)) {
                        targetWindowHandler = checkTargetHandler(handler, criterion, targetWindowHandler);
                    }
                    break;
                case CHILD_ELEMENT:
                    List<WebElement> elements = driver.findElements(By.xpath(value));
                    if (elements.size() == 1) {
                        targetWindowHandler = checkTargetHandler(handler, criterion, targetWindowHandler);
                    } else if (elements.size() > 1) {
                        throw new CommandFailedException("The child element selector is not unique.");
                    }
                    break;
                default:
                    throw new CommandFailedException("Command failed due a non implemented criterion.");
            }
        }

        if (targetWindowHandler != null) {
            driver.switchTo().window(targetWindowHandler);
            return true;
        }

        return false;
    }

    private String checkTargetHandler(String currentHandler,
                                      WebViewSelectionCriterion criterion,
                                      String targetWindowHandler)
        throws CommandFailedException {
        if (targetWindowHandler != null) {
            throw new CommandFailedException(String.format("More the one web views have the same %s value",
                                                           criterion.getName()));
        }
        return currentHandler;
    }

    /**
     * Gets the title of the current web view.
     *
     * @return the title of the current web view or empty string if the title is not set.
     */
    public String getWebViewTitle() {
        String title = driver.getTitle();

        return title;
    }

    /**
     * Gets the URL of the current web view.
     *
     * @return the URL of the current web view of <code>null</code> if failed to get the URL.
     */
    public String getWebViewCurrentUrl() {
        String currentUrl = driver.getCurrentUrl();

        return currentUrl;
    }

    /**
     * Sets an implicit wait timeout to the {@link WebDriver} driver
     *
     * @param implicitWaitTimeout
     *        - an implicit wait timeout in milliseconds
     */
    public void setImplicitWait(int implicitWaitTimeout) {
        this.implicitWaitTimeout = implicitWaitTimeout;
        driver.manage().timeouts().implicitlyWait(implicitWaitTimeout, TimeUnit.MILLISECONDS);
    }
}
