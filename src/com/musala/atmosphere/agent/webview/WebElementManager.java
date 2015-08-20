package com.musala.atmosphere.agent.webview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.musala.atmosphere.commons.webelement.actions.WebElementAction;
import com.musala.atmosphere.commons.webelement.selection.WebElementNotPresentException;
import com.musala.atmosphere.commons.webelement.selection.WebElementSelectionCriterion;

/**
 * Class responsible for initializing {@link ChromeDriver chrome driver} used for retrieving information for the
 * WebViews on the device screen.
 * 
 * @author filareta.yordanova
 *
 */
public class WebElementManager {
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

        driver = new ChromeDriver(service, capabilities);
    }

    /**
     * Finds a web element by the given {@link WebElementSelectionCriterion selection criterion}, matching the requested
     * criterion value.
     * 
     * @param selectionCriterion
     *        - the type of the criterion used for selection
     * @param criterionValue
     *        - value of the criterion used for matching
     * @return {@link Map map} containing all attributes of the found element
     */
    public Map<String, Object> findElement(WebElementSelectionCriterion selectionCriterion, String criterionValue) {
        By criterion = findBy(selectionCriterion, criterionValue);
        WebElement foundElement = null;

        try {
            foundElement = driver.findElement(criterion);
        } catch (NoSuchElementException e) {
            throw new WebElementNotPresentException(String.format("Web element for the requested search criterion  %s and value %s is not present on the screen!",
                                                                  selectionCriterion,
                                                                  criterionValue));
        }

        return foundElement != null ? getWebElementAttributes(foundElement) : null;
    }

    /**
     * Finds a list of web elements by the given {@link WebElementSelectionCriterion selection criterion}, matching the
     * requested criterion value.
     * 
     * @param selectionCriterion
     *        - the type of the criterion used for selection
     * @param criterionValue
     *        - value of the criterion used for matching
     * @return list of {@link Map attributes} corresponding to the found elements
     */
    public List<Map<String, Object>> findElements(WebElementSelectionCriterion selectionCriterion, String criterionValue) {
        By criterion = findBy(selectionCriterion, criterionValue);
        List<WebElement> webElements = driver.findElements(criterion);

        List<Map<String, Object>> foundElements = new ArrayList<Map<String, Object>>();
        for (WebElement element : webElements) {
            foundElements.add(getWebElementAttributes(element));
        }

        return foundElements;
    }

    /**
     * Checks if the element by the given criterion is displayed.
     * 
     * @param selectionCriterion
     *        - criterion by which the element will be selected
     * @param criterionValue
     *        - value of the criterion
     * @return <code>true</code> if the element is displayed, <code>false</code> otherwise
     */
    public boolean isDisplayed(WebElementSelectionCriterion selectionCriterion, String criterionValue) {
        WebElement element = getWebElement(selectionCriterion, criterionValue);
        return element.isDisplayed();
    }

    /**
     * Checks if the element by the given criterion is selected.
     * 
     * @param selectionCriterion
     *        - criterion by which the element will be selected
     * @param criterionValue
     *        - value of the criterion
     * @return <code>true</code> if the element is selected, <code>false</code> otherwise
     */
    public boolean isSelected(WebElementSelectionCriterion selectionCriterion, String criterionValue) {
        WebElement element = getWebElement(selectionCriterion, criterionValue);
        return element.isSelected();
    }

    /**
     * Executes action on the web element selected by the given criterion.
     * 
     * @param webElementAction
     *        - type of the action that will be executed
     * @param selectionCriterion
     *        - criterion by which the element will be selected
     * @param criterionValue
     *        - value of the criterion
     * @return {@link Object} containing the result of the executed action
     */
    public Object executeAction(WebElementAction webElementAction,
                                WebElementSelectionCriterion selectionCriterion,
                                String criterionValue) {
        switch (webElementAction) {
            case IS_SELECTED:
                return isSelected(selectionCriterion, criterionValue);
            case IS_DISPLAYED:
                return isDisplayed(selectionCriterion, criterionValue);
            default:
                return null;
        }
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
     * Gets a web element by the given criteria.
     * 
     * @param selectionCriterion
     *        - criterion by which the element will be selected
     * @param criterionValue
     *        - value of the criterion
     * @return the web element instance
     */
    private WebElement getWebElement(WebElementSelectionCriterion selectionCriterion, String criterionValue) {
        By criterion = findBy(selectionCriterion, criterionValue);
        WebElement foundElement = null;

        try {
            foundElement = driver.findElement(criterion);
        } catch (NoSuchElementException e) {
            throw new WebElementNotPresentException(String.format("The Web element for the requested search criterion  %s and value %s is no longer present on the screen!",
                                                                  selectionCriterion,
                                                                  criterionValue));
        }

        return foundElement;
    }

    private By findBy(WebElementSelectionCriterion selectionCriterion, String criterionValue) {
        switch (selectionCriterion) {
            case CLASS:
                return By.className(criterionValue);
            case TAG:
                return By.tagName(criterionValue);
            case XPATH:
                return By.xpath(criterionValue);
            case CSS_SELECTOR:
                return By.cssSelector(criterionValue);
            case LINK:
                return By.linkText(criterionValue);
            case PARTIAL_LINK:
                return By.partialLinkText(criterionValue);
            case ID:
                return By.id(criterionValue);
            case NAME:
                return By.name(criterionValue);
            default:
                return By.id(criterionValue);
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
}
