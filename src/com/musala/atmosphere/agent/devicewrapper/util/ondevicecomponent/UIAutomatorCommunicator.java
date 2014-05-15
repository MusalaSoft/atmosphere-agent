package com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.devicewrapper.util.FileTransferService;
import com.musala.atmosphere.agent.devicewrapper.util.ShellCommandExecutor;
import com.musala.atmosphere.commons.ad.FileObjectTransferManagerConstants;
import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.uiautomator.UIAutomatorConstants;
import com.musala.atmosphere.commons.ad.uiautomator.UIAutomatorRequest;
import com.musala.atmosphere.commons.ad.util.FileObjectTransferManager;
import com.musala.atmosphere.commons.beans.SwipeDirection;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.gesture.Timeline;
import com.musala.atmosphere.commons.ui.UiElementDescriptor;

/**
 * Class that communicates with the ATMOSPHERE UIAutomator-based on-device component.
 * 
 * @author yordan.petrov
 * 
 */
public class UIAutomatorCommunicator {
    private static final Logger LOGGER = Logger.getLogger("UIAutomatorComunicator");

    private final ShellCommandExecutor executor;

    private final FileTransferService transferService;

    private FileObjectTransferManager fileObjectTransferManager = new FileObjectTransferManager();

    // TODO Maybe add run validation in the future

    public UIAutomatorCommunicator(ShellCommandExecutor executor, FileTransferService transferService) {
        this.executor = executor;
        this.transferService = transferService;
    }

    /**
     * Plays the passed list of {@link Timeline} instances.
     * 
     * @param pointerTimelines
     *        - a list of {@link Timeline} instances.
     * 
     * @throws CommandFailedException
     */
    public void playGesture(List<Timeline> pointerTimelines) throws CommandFailedException {
        Object[] arguments = new Object[] {pointerTimelines};
        Request<UIAutomatorRequest> uiAutomatorRequest = new Request<UIAutomatorRequest>(UIAutomatorRequest.PLAY_GESTURE);
        uiAutomatorRequest.setArguments(arguments);

        UIAutomatorProcessStarter starter = new UIAutomatorProcessStarter();
        starter.attachObject(UIAutomatorConstants.PARAM_REQUEST, uiAutomatorRequest);
        String executionResponse = starter.run(executor, transferService);
    }

    public void clearField(UiElementDescriptor descriptor) throws CommandFailedException {
        Object[] arguments = new Object[] {descriptor};
        Request<UIAutomatorRequest> uiAutomatorRequest = new Request<UIAutomatorRequest>(UIAutomatorRequest.CLEAR_FIELD);
        uiAutomatorRequest.setArguments(arguments);

        UIAutomatorProcessStarter starter = new UIAutomatorProcessStarter();
        starter.attachObject(UIAutomatorConstants.PARAM_REQUEST, uiAutomatorRequest);
        String executionResponse = starter.run(executor, transferService);
    }

    public void swipeElement(UiElementDescriptor descriptor, SwipeDirection direction) throws CommandFailedException {
        Object[] arguments = new Object[] {descriptor, direction};
        Request<UIAutomatorRequest> uiAutomatorRequest = new Request<UIAutomatorRequest>(UIAutomatorRequest.ELEMENT_SWIPE);
        uiAutomatorRequest.setArguments(arguments);

        UIAutomatorProcessStarter starter = new UIAutomatorProcessStarter();
        starter.attachObject(UIAutomatorConstants.PARAM_REQUEST, uiAutomatorRequest);
        String executionResponse = starter.run(executor, transferService);
    }

    /**
     * Starts a process on the UIAutomatorBridge that waits for a UI element to appear on the screen with a given
     * timeout.
     * 
     * @param descriptor
     *        - the descriptor of the UI element.
     * @param timeout
     *        - the given timeout.
     * @param deviceSerialNumber
     *        - the serial number of the used device.
     * @return - Returns <code>true</code> if the element exists.<br>
     *         <code>false</code> if there isn't such element on the screen. <br>
     * 
     * @throws CommandFailedException
     */
    public boolean waitForExists(UiElementDescriptor descriptor, Long timeout, String deviceSerialNumber)
        throws CommandFailedException {
        Object[] arguments = new Object[] {descriptor, timeout};
        Request<UIAutomatorRequest> uiAutomatorRequest = new Request<UIAutomatorRequest>(UIAutomatorRequest.WAIT_FOR_EXISTS);
        uiAutomatorRequest.setArguments(arguments);

        UIAutomatorProcessStarter starter = new UIAutomatorProcessStarter();
        starter.attachObject(UIAutomatorConstants.PARAM_REQUEST, uiAutomatorRequest);
        String executionResponse = starter.run(executor, transferService);
        String localFileName = FileObjectTransferManagerConstants.RESPONSE_FILE_NAME + deviceSerialNumber;
        Boolean response = false;

        transferService.pullFile(FileObjectTransferManagerConstants.RESPONSE_FILE_NAME, localFileName);

        try {
            response = (Boolean) fileObjectTransferManager.readObjectFromFile(localFileName);
        } catch (ClassNotFoundException | IOException e) {
            String message = "Failed to read the response from the file";
            LOGGER.error(message, e);
            throw new CommandFailedException(message, e);
        }
        return response;

    }
}
