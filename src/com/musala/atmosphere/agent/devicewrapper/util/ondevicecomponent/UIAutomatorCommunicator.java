package com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent;

import java.util.List;

import com.musala.atmosphere.agent.devicewrapper.util.FileTransferService;
import com.musala.atmosphere.agent.devicewrapper.util.ShellCommandExecutor;
import com.musala.atmosphere.commons.ad.Request;
import com.musala.atmosphere.commons.ad.uiautomator.UIAutomatorConstants;
import com.musala.atmosphere.commons.ad.uiautomator.UIAutomatorRequest;
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
    private final ShellCommandExecutor executor;

    private final FileTransferService transferService;

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
        // TODO Maybe add run validation in the future
        String executionResponse = starter.run(executor, transferService);
    }

    public void clearField(UiElementDescriptor descriptor) throws CommandFailedException {
        Object[] arguments = new Object[] {descriptor};
        Request<UIAutomatorRequest> uiAutomatorRequest = new Request<UIAutomatorRequest>(UIAutomatorRequest.CLEAR_FIELD);
        uiAutomatorRequest.setArguments(arguments);

        UIAutomatorProcessStarter starter = new UIAutomatorProcessStarter();
        starter.attachObject(UIAutomatorConstants.PARAM_REQUEST, uiAutomatorRequest);
        // TODO Maybe add run validation in the future
        String executionResponse = starter.run(executor, transferService);
    }

    public void swipeElement(UiElementDescriptor descriptor, SwipeDirection direction) throws CommandFailedException {
        Object[] arguments = new Object[] {descriptor, direction};
        Request<UIAutomatorRequest> uiAutomatorRequest = new Request<UIAutomatorRequest>(UIAutomatorRequest.ELEMENT_SWIPE);
        uiAutomatorRequest.setArguments(arguments);

        UIAutomatorProcessStarter starter = new UIAutomatorProcessStarter();
        starter.attachObject(UIAutomatorConstants.PARAM_REQUEST, uiAutomatorRequest);
        // TODO Maybe add run validation in the future
        String executionResponse = starter.run(executor, transferService);
    }
}
