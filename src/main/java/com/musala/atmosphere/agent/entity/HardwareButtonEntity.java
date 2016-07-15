package com.musala.atmosphere.agent.entity;

import com.musala.atmosphere.agent.devicewrapper.util.ShellCommandExecutor;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;

/**
 * Entity responsible for operations related with hardware buttons.
 *
 * @author filareta.yordanova
 *
 */
public class HardwareButtonEntity {
    private ShellCommandExecutor shellCommandExecutor;

    HardwareButtonEntity(ShellCommandExecutor shellCommandExecutor) {
        this.shellCommandExecutor = shellCommandExecutor;
    }

    /**
     * Presses hardware button on this device.
     *
     * @param keyCode
     *        - button key code as specified by the Android KeyEvent KEYCODE_ constants
     * @return <code>true</code> if the hardware button press is successful, <code>false</code> if it fails
     */
    public Boolean pressButton(int keyCode) {
        String query = "input keyevent " + Integer.toString(keyCode);
        boolean response = true;
        try {
            shellCommandExecutor.execute(query);
        } catch (CommandFailedException e) {
            response = false;
        }

        return response;
    }
}
