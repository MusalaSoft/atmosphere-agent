package com.musala.atmosphere.agent.devicewrapper.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.android.ddmlib.EmulatorConsole;
import com.android.ddmlib.IDevice;
import com.musala.atmosphere.agent.exception.EmulatorConnectionFailedException;
import com.musala.atmosphere.commons.SmsMessage;
import com.musala.atmosphere.commons.beans.BatteryLevel;
import com.musala.atmosphere.commons.beans.BatteryState;
import com.musala.atmosphere.commons.beans.DeviceAcceleration;
import com.musala.atmosphere.commons.beans.DeviceMagneticField;
import com.musala.atmosphere.commons.beans.DeviceOrientation;
import com.musala.atmosphere.commons.beans.MobileDataState;
import com.musala.atmosphere.commons.beans.PhoneNumber;
import com.musala.atmosphere.commons.beans.PowerSource;
import com.musala.atmosphere.commons.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.parameters.CommandParameter;
import com.musala.atmosphere.commons.parameters.IntegerCommandParameter;
import com.musala.atmosphere.commons.parameters.StringCommandParameter;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;

/**
 * <p>
 * Establishes connection to the console of an emulator and transfers commands to it.
 * </p>
 * 
 * @author georgi.gaydarov
 * 
 */
public class ExtendedEmulatorConsole {
    private final static Logger LOGGER = Logger.getLogger(ExtendedEmulatorConsole.class.getCanonicalName());

    private final static String LOCALHOST = "127.0.0.1";

    private static final String DEFAULT_ENCODING = "ISO-8859-1";

    private static final long WAIT_TIME = 10; // spin-wait time, in ms

    private static final long SOCKET_TIMEOUT = 5000; // maximum socket delay, in ms

    private final static String COMMAND_GSM_STATUS = "gsm status\r\n";

    /**
     * Socket read/write buffer.
     */
    private final byte[] buffer = new byte[1024];

    private SocketChannel socketChannel;

    private final int port;

    /** Used for more detailed error logging. */
    private String emulatorSerialNumber;

    /**
     * Hash map that maps emulator console ports to ExtendedEmulatorConsole instances. Used for the singleton pattern.
     * (one ExtendedEmulatorConsole for a port)
     */
    private final static HashMap<Integer, ExtendedEmulatorConsole> emulatorConsoles = new HashMap<Integer, ExtendedEmulatorConsole>();

    /**
     * Returns the {@link ExtendedEmulatorConsole ExtendedEmulatorConsole} instance (or creates one and then returns it)
     * for a specific emulator.
     * 
     * @param targetDevice
     *        The device that we want to send commands to.
     * @return {@link ExtendedEmulatorConsole} for the passed IDevice.
     * @throws EmulatorConnectionFailedException
     * @throws NotPossibleForDeviceException
     *         If the given device is not emulator
     */
    public static synchronized ExtendedEmulatorConsole getExtendedEmulatorConsole(IDevice targetDevice)
        throws EmulatorConnectionFailedException,
            NotPossibleForDeviceException {
        if (!targetDevice.isEmulator()) {
            throw new NotPossibleForDeviceException("Cannot create emulator console class for real devices.");
        }

        // Using the static getEmulatorPort method from the EmulatorConsole in ddmlib
        String emulatorSerialNumber = targetDevice.getSerialNumber();
        Integer port = EmulatorConsole.getEmulatorPort(emulatorSerialNumber);
        if (port == null) {
            throw new EmulatorConnectionFailedException("Could not get/parse the port for the emulator console on '"
                    + emulatorSerialNumber + "'.");
        }

        // If such a console already exists and it is responsive, use it
        if (emulatorConsoles.containsKey(port)) {
            ExtendedEmulatorConsole console = emulatorConsoles.get(port);
            console.setEmulatorSerialNumber(emulatorSerialNumber);
            try {
                // this command will throw if the operation fails
                console.ping();
                return console;
            } catch (CommandFailedException e) {
                // The console for this port is invalid.
                // Do nothing, we will just create a new ExtendedEmulatorConsole next.
            }
            // As the console is not responsive, discard it.
            console.close();
        }

        // Create a new console
        ExtendedEmulatorConsole console = new ExtendedEmulatorConsole(port, emulatorSerialNumber);

        return console;
    }

    /**
     * Private constructor for the extended emulator console.
     * 
     * @param port
     *        Port to which this class will connect.
     * @param emulatorSerialNumber
     *        The serial number of the emulator a new console is created for
     * @throws EmulatorConnectionFailedException
     */
    private ExtendedEmulatorConsole(int port, String emulatorSerialNumber) throws EmulatorConnectionFailedException {
        this.port = port;
        this.emulatorSerialNumber = emulatorSerialNumber;

        InetSocketAddress socketAddress;
        try {
            InetAddress hostAddress = InetAddress.getByName(LOCALHOST);
            socketAddress = new InetSocketAddress(hostAddress, this.port);
        } catch (UnknownHostException e) {
            throw new EmulatorConnectionFailedException("Connecting to the emulator console failed. See the enclosed exception for more information.",
                                                        e);
        }

        try {
            socketChannel = SocketChannel.open(socketAddress);
        } catch (IOException e) {
            throw new EmulatorConnectionFailedException("Connecting to the emulator console failed. See the enclosed exception for more information.",
                                                        e);
        }

        // read the welcome message from the console.
        if (!responseIsFine()) {
            throw new EmulatorConnectionFailedException("Connecting to the emulator console failed. The console failed to respond correctly.");
        }

        // And finally, if everything is OK, put this instance in the singleton map
        emulatorConsoles.put(port, this);
    }

    private void setEmulatorSerialNumber(String emulatorSerialNumber) {
        this.emulatorSerialNumber = emulatorSerialNumber;
    }

    /**
     * Closes all open resources and removes this instance from the {@link #emulatorConsoles emulatorConsoles} hash map.
     */
    private synchronized void close() {
        if (emulatorConsoles.containsKey(port)) {
            emulatorConsoles.remove(port);
        }
    }

    /**
     * Checks if the connection to the emulator console is OK by sending a 'help' command and inspecting the response.
     * 
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    protected synchronized void ping() throws CommandFailedException {
        adaptAndExecuteCommand(EmulatorCommand.PING);
    }

    /**
     * Sets the battery level of the emulator.
     * 
     * @param level
     *        Integer between 0 and 100 inclusive.
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    public synchronized void setBatteryLevel(BatteryLevel level) throws CommandFailedException {
        adaptAndExecuteCommand(EmulatorCommand.SET_BATTERY_LEVEL, level);
    }

    /**
     * Sets the power state of the emulator.
     * 
     * @param status
     *        {@link BatteryState BatteryState} enumerated constant.
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    public synchronized void setBatteryState(BatteryState status) throws CommandFailedException {
        adaptAndExecuteCommand(EmulatorCommand.SET_BATTERY_STATE, status);
    }

    /**
     * Sets the power state of the emulator to AC connected or disconnected.
     * 
     * @param source
     *        - The source to set - a {@link PowerSource} enum instance.
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    public synchronized void setPowerSource(PowerSource source) throws CommandFailedException {
        adaptAndExecuteCommand(EmulatorCommand.SET_POWER_STATE, source);
    }

    /**
     * Sets the network up/down speed of the emulator.
     * 
     * @param uploadSpeed
     * @param downloadSpeed
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    public synchronized void setNetworkSpeed(int uploadSpeed, int downloadSpeed) throws CommandFailedException {
        adaptAndExecuteCommand(EmulatorCommand.SET_NETWORK_SPEED,
                               new IntegerCommandParameter(uploadSpeed),
                               new IntegerCommandParameter(downloadSpeed));
    }

    /**
     * Gets the mobile data state of the emulator through the emulator console and returns the response.
     * 
     * @return the response from the emulator console.
     * @throws EmulatorConnectionFailedException
     */
    public synchronized String getMobileDataState() throws EmulatorConnectionFailedException {
        String command = COMMAND_GSM_STATUS;
        String response = executeCommandWithResponse(command);
        return response;
    }

    /**
     * Sets the orientation in space of the testing device.
     * 
     * @param deviceOrientation
     *        - desired device orientation.
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    public synchronized void setOrientation(DeviceOrientation deviceOrientation) throws CommandFailedException {
        adaptAndExecuteCommand(EmulatorCommand.SET_ORIENTATION, deviceOrientation);
    }

    /**
     * Sets the acceleration of the testing device.
     * 
     * @param deviceAcceleration
     *        The desired device acceleration.
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    public synchronized void setAcceleration(DeviceAcceleration deviceAcceleration) throws CommandFailedException {
        adaptAndExecuteCommand(EmulatorCommand.SET_ACCELERATION, deviceAcceleration);
    }

    /**
     * Sets the mobile data state of an emulator through the emulator console.
     * 
     * @param state
     *        The mobile data state to set
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    public synchronized void setMobileDataState(MobileDataState state) throws CommandFailedException {
        adaptAndExecuteCommand(EmulatorCommand.SET_MOBILE_DATA_STATE, state);
    }

    /**
     * Sets the magnetic field sensor reading through the emulator console. Note : This sensor provides raw field
     * strength data (in uT) for each of the three coordinate axes
     * 
     * @param deviceMagneticField
     *        The desired magnetic field.
     * 
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    public synchronized void setMagneticField(DeviceMagneticField deviceMagneticField) throws CommandFailedException {
        adaptAndExecuteCommand(EmulatorCommand.SET_MAGNETIC_FIELD, deviceMagneticField);
    }

    /**
     * Sends SMS to the emulator.
     * 
     * @param smsMessage
     *        - the SMS message, that will be sent to emulator.
     * 
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    public synchronized void receiveSms(SmsMessage smsMessage) throws CommandFailedException {
        adaptAndExecuteCommand(EmulatorCommand.SEND_SMS,
                               smsMessage.getPhoneNumber(),
                               new StringCommandParameter(smsMessage.getText()));
    }

    /**
     * Sends a call to the emulator.
     * 
     * @param phoneNumber
     *        - the phone number, that will call the emulator.
     * 
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    public synchronized void receiveCall(PhoneNumber phoneNumber) throws CommandFailedException {
        adaptAndExecuteCommand(EmulatorCommand.RECEIVE_CALL, phoneNumber);
    }

    /**
     * Accepts a call to the emulator.
     * 
     * @param phoneNumber
     *        - the phone number, that calls the emulator.
     * 
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    public synchronized void acceptCall(PhoneNumber phoneNumber) throws CommandFailedException {
        adaptAndExecuteCommand(EmulatorCommand.ACCEPT_CALL, phoneNumber);
    }

    /**
     * Holds a call to the emulator.
     * 
     * @param phoneNumber
     *        - the phone number, that calls the emulator.
     * 
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    public synchronized void holdCall(PhoneNumber phoneNumber) throws CommandFailedException {
        adaptAndExecuteCommand(EmulatorCommand.HOLD_CALL, phoneNumber);
    }

    /**
     * Cancels a call to the emulator.
     * 
     * @param phoneNumber
     *        - the phone number, that calls the emulator.
     * 
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    public synchronized void cancelCall(PhoneNumber phoneNumber) throws CommandFailedException {
        adaptAndExecuteCommand(EmulatorCommand.CANCEL_CALL, phoneNumber);
    }

    /**
     * A generic method for execution of console commands.
     * 
     * @param command
     *        The {@link EmulatorCommand} to execute
     * @param parameters
     *        The parameters to use for the command
     * @throws CommandFailedException
     *         In case of an error in the execution
     */
    private synchronized void adaptAndExecuteCommand(EmulatorCommand command, CommandParameter... parameters)
        throws CommandFailedException {
        String[] parameterValues = getParameterValues(parameters);
        String formattedCommand = String.format(command.getCommandString(), (Object[]) parameterValues);
        try {
            boolean success = executeCommand(formattedCommand);
            if (!success) {
                String errorMessage = String.format("Execution of operation '%s' on device '%s' failed",
                                                    command.getErrorHumanReadableDescription(),
                                                    emulatorSerialNumber);
                LOGGER.error(errorMessage);
                throw new CommandFailedException(errorMessage);
            }
        } catch (IllegalArgumentException e) {
            throw new CommandFailedException("Illegal argument has been passed to the emulator console class. "
                    + "See the enclosed exception for more information.", e);
        } catch (EmulatorConnectionFailedException e) {
            throw new CommandFailedException("Connection to the emulator console failed. "
                    + "See the enclosed exception for more information.", e);
        }
    }

    /**
     * Fetches the values to use for the given parameters.
     * 
     * @param parameters
     *        the parameters whose values to get
     * @return The values to use for each parameter
     */
    private String[] getParameterValues(CommandParameter... parameters) {
        if (parameters == null) {
            return null;
        }
        String[] parameterValues = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterValues[i] = parameters[i].getParameterValue(true);
        }
        return parameterValues;
    }

    /**
     * Sends a string to the emulator console and returns a value indicating if the response was OK or KO.
     * 
     * @param command
     *        The command string.
     * @return true if OK was found, false if KO was found.
     * @throws EmulatorConnectionFailedException
     */
    protected synchronized boolean executeCommand(String command) throws EmulatorConnectionFailedException {
        // if the command does not end with a newline, this call will block.
        // so we make sure this never happens.
        if (!command.endsWith("\n")) {
            command = command + "\n";
        }
        sendCommand(command);
        return responseIsFine();
    }

    /**
     * Executes command in the emulator console and returns the output.
     * 
     * @param command
     *        - the command to be executed.
     * @return - the command output.
     * @throws EmulatorConnectionFailedException
     */
    protected synchronized String executeCommandWithResponse(String command) throws EmulatorConnectionFailedException {
        executeCommand(command);
        String commandResponse = new String(buffer);
        return commandResponse;
    }

    /**
     * Sends a string to the emulator console.
     * 
     * @param command
     *        The command string. <b>MUST BE TERMINATED BY \n</b>.
     * @throws EmulatorConnectionFailedException
     */
    private void sendCommand(String command) throws EmulatorConnectionFailedException {
        try {
            byte[] commandBuffer;
            try {
                commandBuffer = command.getBytes(DEFAULT_ENCODING);
            } catch (UnsupportedEncodingException e) {
                throw new EmulatorConnectionFailedException("Exception occured while writing to the socket channel : Unsupported encoding.",
                                                            e);
            }

            ByteBuffer wrappedBuffer = ByteBuffer.wrap(commandBuffer, 0, commandBuffer.length);

            // Send the command
            int numberOfAttempts = 0;
            while (wrappedBuffer.position() != wrappedBuffer.limit()) {
                int writeCount = socketChannel.write(wrappedBuffer);

                if (writeCount < 0) {
                    throw new EmulatorConnectionFailedException("Exception occured while writing to the socket channel : EOF.");
                } else if (writeCount == 0) {
                    if (numberOfAttempts * WAIT_TIME > SOCKET_TIMEOUT) {
                        throw new EmulatorConnectionFailedException("Exception occured while writing to the socket channel : Timeout.");
                    }
                    // non-blocking spin
                    try {
                        Thread.sleep(WAIT_TIME);
                    } catch (InterruptedException ie) {
                    }
                    numberOfAttempts++;
                } else {
                    numberOfAttempts = 0;
                }
            }
        } catch (EmulatorConnectionFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new EmulatorConnectionFailedException("Exception occured while writing to the socket channel. See the enclosed exception for more information.",
                                                        e);
        }
    }

    /**
     * Reads lines from the console socket. This call is blocking until we time out or read the lines:
     * <ul>
     * <li>OK\r\n</li>
     * <li>KO\r\n</li>
     * </ul>
     * 
     * @return true if we found OK, false if we found KO.
     * @throws EmulatorConnectionFailedException
     */
    private boolean responseIsFine() throws EmulatorConnectionFailedException {
        try {
            ByteBuffer wrappedBuffer = ByteBuffer.wrap(buffer, 0, buffer.length);
            int numberOfAttempts = 0;

            while (wrappedBuffer.position() != wrappedBuffer.limit()) {
                int readCount = socketChannel.read(wrappedBuffer);

                if (readCount < 0) {
                    throw new EmulatorConnectionFailedException("Exception occured while reading from the socket channel : EOF.");
                } else if (readCount == 0) {
                    if (numberOfAttempts * WAIT_TIME > SOCKET_TIMEOUT) {
                        throw new EmulatorConnectionFailedException("Exception occured while reading from the socket channel : Timeout.");
                    }
                    // non-blocking spin
                    try {
                        Thread.sleep(WAIT_TIME);
                    } catch (InterruptedException ie) {
                    }
                    numberOfAttempts++;
                } else {
                    numberOfAttempts = 0;
                }

                // check the last few char aren't OK. For a valid message to test
                // we need at least 4 bytes (OK/KO + \r\n)
                if (wrappedBuffer.position() >= 4) {
                    int pos = wrappedBuffer.position();
                    if (endsWithOK(pos)) {
                        return true;
                    }
                    if (endsWithKO(pos)) {
                        return false;
                    }
                    // TODO research for existence of emulator console responses not containing "OK" or "KO"
                }
            }
        } catch (EmulatorConnectionFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new EmulatorConnectionFailedException("Exception occured while reading from the socket channel. See the enclosed exception for more information.",
                                                        e);
        }
        return false;
    }

    /**
     * Returns true if the 4 characters *before* the current position are "OK\r\n".
     * 
     * @param currentPosition
     *        The current position.
     */
    private boolean endsWithOK(int currentPosition) {
        if (buffer[currentPosition - 1] == '\n' && buffer[currentPosition - 2] == '\r'
                && buffer[currentPosition - 3] == 'K' && buffer[currentPosition - 4] == 'O') {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the last line starts with KO and is also terminated by \r\n.
     * 
     * @param currentPosition
     *        The current position.
     */
    private boolean endsWithKO(int currentPosition) {
        // now loop backward looking for the previous CRLF, or the beginning of the buffer
        int i = 0;
        for (i = currentPosition - 3; i >= 0; i--) {
            if (buffer[i] == '\n') {
                // found \n!
                if (i > 0 && buffer[i - 1] == '\r') {
                    // found \r!
                    break;
                }
            }
        }

        // here it is either -1 if we reached the start of the buffer without finding
        // a CRLF, or the position of \n. So in both case we look at the characters at i+1 and i+2
        if (buffer[i + 1] == 'K' && buffer[i + 2] == 'O') {
            // found KO (error)
            return true;
        }
        return false;
    }

    // TODO Auto-generated method stub

}
