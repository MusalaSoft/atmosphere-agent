package com.musala.atmosphere.agent.entity;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.devicewrapper.util.ondevicecomponent.ServiceCommunicator;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.geometry.Point;
import com.musala.atmosphere.commons.ime.KeyboardAction;
import com.musala.atmosphere.commons.util.AtmosphereIntent;

import android.bluetooth.BluetoothClass.Device;

/**
 * Entity responsible for operations related with the input method engine.
 *
 * @author yavor.stankov
 *
 */
public class ImeEntity {
    private static final Logger LOGGER = Logger.getLogger(ImeEntity.class.getCanonicalName());

    private ServiceCommunicator communicator;

    ImeEntity(ServiceCommunicator communicator) {
        this.communicator = communicator;
    }

    /**
     * Simulates text typing in the element on focus for this device.
     *
     * @param text
     *        - text to be input
     * @param interval
     *        - time interval in milliseconds between typing each symbol
     * @return <code>true</code> if the text input is successful, <code>false</code> if it fails
     */
    public boolean inputText(String text, long interval) {
        if (text.isEmpty()) {
            String message = "Text input requested, but an empty String is given.";
            LOGGER.warn(message);
            return true;
        }

        AtmosphereIntent intent = new AtmosphereIntent(KeyboardAction.INPUT_TEXT.intentAction);
        intent.putExtra(KeyboardAction.INTENT_EXTRA_TEXT, text);
        intent.putExtra(KeyboardAction.INTENT_EXTRA_INPUT_SPEED, interval);

        boolean result = sendBroadcast(intent);

        waitForTaskCompletion(text.length() * interval);

        return result;
    }

    /**
     * Clears the content of the focused text field.
     *
     * @return <code>true</code> if clear text is successful, <code>false</code> if it fails
     */
    public boolean clearText() {
        AtmosphereIntent intent = new AtmosphereIntent(KeyboardAction.DELETE_ALL.intentAction);

        return sendBroadcast(intent);
    }

    /**
     * Selects the content of the focused text field.
     *
     * @return <code>true</code> if the text selecting is successful, <code>false</code> if it fails
     */
    public boolean selectAllText() {
        AtmosphereIntent intent = new AtmosphereIntent(KeyboardAction.SELECT_ALL.intentAction);

        return sendBroadcast(intent);
    }

    /**
     * Copies the selected content of the focused text field.
     *
     * @return <code>true</code> if copy operation is successful, <code>false</code> if it fails
     */
    public boolean copyText() {
        AtmosphereIntent intent = new AtmosphereIntent(KeyboardAction.COPY_TEXT.intentAction);

        return sendBroadcast(intent);
    }

    /**
     * Paste a copied text in the current focused text field.
     *
     * @return <code>true</code> if the operation is successful, <code>false</code> if it fails
     */
    public boolean pasteText() {
        AtmosphereIntent intent = new AtmosphereIntent(KeyboardAction.PASTE_TEXT.intentAction);

        return sendBroadcast(intent);
    }

    /**
     * Cuts the selected text from the current focused text field.
     *
     * @return <code>true</code> if the operation is successful, <code>false</code> if it fails
     */
    public boolean cutText() {
        AtmosphereIntent intent = new AtmosphereIntent(KeyboardAction.CUT_TEXT.intentAction);

        return sendBroadcast(intent);
    }

    private boolean sendBroadcast(AtmosphereIntent intent) {

        try {
            communicator.sendBroadcast(new Object[] {intent});
        } catch (CommandFailedException e) {
            return false;
        }

        return true;
    }

    private void waitForTaskCompletion(long timeoutInMs) {
        try {
            Thread.sleep(timeoutInMs);
        } catch (InterruptedException e) {
            // Nothing to do here
        }
    }
}
