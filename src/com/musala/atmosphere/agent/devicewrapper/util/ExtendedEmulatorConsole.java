package com.musala.atmosphere.agent.devicewrapper.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

import com.android.ddmlib.EmulatorConsole;
import com.android.ddmlib.IDevice;
import com.musala.atmosphere.commons.BatteryState;
import com.musala.atmosphere.commons.DeviceAcceleration;
import com.musala.atmosphere.commons.DeviceOrientation;
import com.musala.atmosphere.commons.MobileDataState;
import com.musala.atmosphere.commons.PhoneNumber;
import com.musala.atmosphere.commons.SmsMessage;
import com.musala.atmosphere.commons.sa.exceptions.NotPossibleForDeviceException;

/**
 * <p>
 * Establishes connection to the console of an emulator and transfers commands to it.
 * </p>
 * 
 * @author georgi.gaydarov
 * 
 */
public class ExtendedEmulatorConsole
{
	private final static String LOCALHOST = "127.0.0.1";

	private static final String DEFAULT_ENCODING = "ISO-8859-1";

	private static final long WAIT_TIME = 10; // spin-wait time, in ms

	private static final long SOCKET_TIMEOUT = 5000; // maximum socket delay, in ms

	private final static String COMMAND_PING_FORMAT = "help\r\n";

	private final static String COMMAND_POWER_CAPACITY_FORMAT = "power capacity %d\r\n";

	private final static String COMMAND_POWER_STATUS_FORMAT = "power status %s\r\n";

	private final static String COMMAND_NETOWRK_SPEED_FORMAT = "network speed %d:%d\r\n";

	private final static String COMMAND_POWER_STATE_FORMAT = "power ac %s\r\n";

	private final static String COMMAND_SET_ORIENTATION_FORMAT = "sensor set orientation %s\r\n";

	private final static String COMMAND_SET_ACCELERATION_FORMAT = "sensor set acceleration %s\r\n";

	private final static String COMMAND_SET_MOBILE_DATA_STATE = "gsm data %s\r\n";

	private static final String COMMAND_SET_MAGNETIC_FIELD = "sensor set magnetic-field %d:%d:%d";

	private final static String COMMAND_GSM_STATUS = "gsm status\r\n";

	private final static String COMMAND_SMS_SEND = "sms send %s %s\r\n";

	private final static String COMMAND_RECEIVE_CALL = "gsm call %s\r\n";

	private final static String COMMAND_ACCEPT_CALL = "gsm accept %s\r\n";

	private final static String COMMAND_CANCEL_CALL = "gsm cancel %s\r\n";

	private final static String COMMAND_HOLD_CALL = "gsm hold %s\r\n";

	/**
	 * Socket read/write buffer.
	 */
	private final byte[] buffer = new byte[1024];

	private SocketChannel socketChannel;

	private final int port;

	/**
	 * Hash map that maps emulator console ports to ExtendedEmulatorConsole instances. Used for the singleton pattern.
	 * (one ExtendedEmulatorConsole for a port)
	 */
	private final static HashMap<Integer, ExtendedEmulatorConsole> emulatorConsoles = new HashMap<Integer, ExtendedEmulatorConsole>();

	/**
	 * Returns the {@link ExtendedEmulatorConsole ExtendedEmulatorConsole} instance (or creates one and then returns it)
	 * for a specific emulator.
	 * 
	 * @param forEmulator
	 *        Emulator that we want to send commands to.
	 * @return {@link ExtendedEmulatorConsole} for the passed IDevice.
	 * @throws EmulatorConnectionFailedException
	 * @throws NotPossibleForDeviceException
	 */
	public static synchronized ExtendedEmulatorConsole getExtendedEmulatorConsole(IDevice forEmulator)
		throws EmulatorConnectionFailedException,
			NotPossibleForDeviceException
	{
		if (forEmulator.isEmulator() == false)
		{
			throw new NotPossibleForDeviceException("Cannot create emulator console class for real devices.");
		}

		// Using the static getEmulatorPort method from the EmulatorConsole in ddmlib
		String emulatorSerialNumber = forEmulator.getSerialNumber();
		Integer port = EmulatorConsole.getEmulatorPort(emulatorSerialNumber);
		if (port == null)
		{
			throw new EmulatorConnectionFailedException("Could not get/parse the port for the emulator console on '"
					+ emulatorSerialNumber + "'.");
		}

		// If such a console already exists and it is responsive, use it
		if (emulatorConsoles.containsKey(port))
		{
			ExtendedEmulatorConsole console = emulatorConsoles.get(port);
			try
			{
				if (console.ping() == true)
				{
					return console;
				}
				// Else, the console is bad and it will be discarded.
			}
			catch (EmulatorConnectionFailedException e)
			{
				// The console for this port is invalid.
				// Do nothing, we will just create a new ExtendedEmulatorConsole next.
			}
			// As the console is not responsive, discard it.
			console.close();
		}

		// Create a new console
		ExtendedEmulatorConsole console = new ExtendedEmulatorConsole(port);

		return console;
	}

	/**
	 * Private constructor for the extended emulator console.
	 * 
	 * @param port
	 *        Port to which this class will connect.
	 * @throws EmulatorConnectionFailedException
	 */
	private ExtendedEmulatorConsole(int port) throws EmulatorConnectionFailedException
	{
		this.port = port;

		InetSocketAddress socketAddress;
		try
		{
			InetAddress hostAddress = InetAddress.getByName(LOCALHOST);
			socketAddress = new InetSocketAddress(hostAddress, this.port);
		}
		catch (UnknownHostException e)
		{
			throw new EmulatorConnectionFailedException("Connecting to the emulator console failed. See the enclosed exception for more information.",
														e);
		}

		try
		{
			socketChannel = SocketChannel.open(socketAddress);
		}
		catch (IOException e)
		{
			throw new EmulatorConnectionFailedException("Connecting to the emulator console failed. See the enclosed exception for more information.",
														e);
		}

		// read the welcome message from the console.
		if (responseIsFine() == false)
		{
			throw new EmulatorConnectionFailedException("Connecting to the emulator console failed. The console failed to respond correctly.");
		}

		// And finally, if everything is OK, put this instance in the singleton map
		emulatorConsoles.put(port, this);
	}

	/**
	 * Closes all open resources and removes this instance from the {@link #emulatorConsoles emulatorConsoles} hash map.
	 */
	private synchronized void close()
	{
		if (emulatorConsoles.containsKey(port))
		{
			emulatorConsoles.remove(port);
		}
	}

	/**
	 * Checks if the connection to the emulator console is OK by sending a 'help' command and inspecting the response.
	 * 
	 * @return True if ping was OK, false if the response was unexpected.
	 * @throws EmulatorConnectionFailedException
	 */
	protected synchronized boolean ping() throws EmulatorConnectionFailedException
	{
		String command = String.format(COMMAND_PING_FORMAT);
		return executeCommand(command);
	}

	/**
	 * Sets the battery level of the emulator.
	 * 
	 * @param level
	 *        Integer between 0 and 100 inclusive.
	 * @return True if setting the level was successful, false otherwise.
	 * @throws EmulatorConnectionFailedException
	 */
	public synchronized boolean setBatteryLevel(int level)
		throws EmulatorConnectionFailedException,
			IllegalArgumentException
	{
		if (level < 0 || level > 100)
		{
			throw new IllegalArgumentException("Battery level should be in the range [0, 100].");
		}
		String command = String.format(COMMAND_POWER_CAPACITY_FORMAT, level);
		return executeCommand(command);
	}

	/**
	 * Sets the power state of the emulator.
	 * 
	 * @param status
	 *        {@link BatteryState BatteryState} enumerated constant.
	 * @return True if setting the power state was successful, false otherwise.
	 * @throws EmulatorConnectionFailedException
	 */
	public synchronized boolean setBatteryState(BatteryState status) throws EmulatorConnectionFailedException
	{
		String statusString = status.toString();

		// NOT_CHARGING battery state string in emulator console differs from that for real devices
		if (status.equals(BatteryState.NOT_CHARGING))
		{
			statusString = "not-charging";
		}

		String command = String.format(COMMAND_POWER_STATUS_FORMAT, statusString);
		return executeCommand(command);
	}

	/**
	 * Sets the power state of the emulator to connected or disconnected.
	 * 
	 * @param state
	 * @return True if setting the power state was successful and false if not.
	 * @throws EmulatorConnectionFailedException
	 */
	public synchronized boolean setPowerState(boolean state) throws EmulatorConnectionFailedException
	{

		String stateToAppend = state ? "on" : "off";
		String command = String.format(COMMAND_POWER_STATE_FORMAT, stateToAppend);
		return executeCommand(command);
	}

	/**
	 * Sets the network up/down speed of the emulator.
	 * 
	 * @param uploadSpeed
	 * @param downloadSpeed
	 * @return True if setting the network speed was successful, false otherwise.
	 * @throws EmulatorConnectionFailedException
	 */
	public synchronized boolean setNetworkSpeed(int uploadSpeed, int downloadSpeed)
		throws EmulatorConnectionFailedException
	{
		String command = String.format(COMMAND_NETOWRK_SPEED_FORMAT, uploadSpeed, downloadSpeed);
		return executeCommand(command);
	}

	/**
	 * Gets the mobile data state of the emulator through the emulator console and returns the response.
	 * 
	 * @return the response from the emulator console.
	 * @throws EmulatorConnectionFailedException
	 */
	public synchronized String getMobileDataState() throws EmulatorConnectionFailedException
	{
		String command = COMMAND_GSM_STATUS;
		String response = executeCommandWithResponse(command);
		return response;
	}

	/**
	 * Sets the orientation in space of the testing device.
	 * 
	 * @param deviceOrientation
	 *        - desired device orientation.
	 * @return
	 * @throws EmulatorConnectionFailedException
	 */
	public synchronized boolean setOrientation(DeviceOrientation deviceOrientation)
		throws EmulatorConnectionFailedException
	{
		String orientationToAppend = deviceOrientation.parseCommand();
		String command = String.format(COMMAND_SET_ORIENTATION_FORMAT, orientationToAppend);
		return executeCommand(command);
	}

	/**
	 * Sets the acceleration of the testing device.
	 * 
	 * @param deviceAcceleration
	 *        - desired device acceleration.
	 * @return
	 * @throws EmulatorConnectionFailedException
	 */
	public synchronized boolean setAcceleration(DeviceAcceleration deviceAcceleration)
		throws EmulatorConnectionFailedException
	{
		String accelerationToAppend = deviceAcceleration.parseCommand();
		String command = String.format(COMMAND_SET_ACCELERATION_FORMAT, accelerationToAppend);
		return executeCommand(command);
	}

	/**
	 * Sets the mobile data state of an emulator through the emulator console.
	 * 
	 * @return the command response from the emulator console.
	 * @throws EmulatorConnectionFailedException
	 */
	public synchronized boolean setMobileDataState(MobileDataState state) throws EmulatorConnectionFailedException
	{
		String stateToAppend = state.toString();
		String command = String.format(COMMAND_SET_MOBILE_DATA_STATE, stateToAppend);
		boolean commandIsOk = executeCommand(command);
		return commandIsOk;
	}

	/**
	 * Sets the magnetic field sensor reading through the emulator console. Note : This sensor provides raw field
	 * strength data (in uT) for each of the three coordinate axes
	 * 
	 * @param x
	 *        - x coordinate magnetic field sensor data.
	 * @param y
	 *        - y coordinate magnetic field sensor data.
	 * @param z
	 *        - z coordinate magnetic field sensor data.
	 * @return the command response from the emulator console.
	 * @throws EmulatorConnectionFailedException
	 */
	public synchronized boolean setMagneticField(int x, int y, int z) throws EmulatorConnectionFailedException
	{
		String command = String.format(COMMAND_SET_MAGNETIC_FIELD, x, y, z);
		boolean commandIsOk = executeCommand(command);
		return commandIsOk;
	}

	/**
	 * Sends SMS to the emulator.
	 * 
	 * @param smsMessage
	 *        - the SMS message, that will be sent to emulator.
	 * 
	 * @return the command response from the emulator console.
	 * @throws EmulatorConnectionFailedException
	 */
	public synchronized boolean receiveSms(SmsMessage smsMessage) throws EmulatorConnectionFailedException
	{
		String command = String.format(COMMAND_SMS_SEND, smsMessage.getPhoneNumber().toString(), smsMessage.getText());
		boolean commandIsOk = executeCommand(command);
		return commandIsOk;
	}

	/**
	 * Sends a call to the emulator.
	 * 
	 * @param phoneNumber
	 *        - the phone number, that will call the emulator.
	 * 
	 * @return the command response from the emulator console.
	 * @throws EmulatorConnectionFailedException
	 */
	public synchronized boolean receiveCall(PhoneNumber phoneNumber) throws EmulatorConnectionFailedException
	{
		String command = String.format(COMMAND_RECEIVE_CALL, phoneNumber.toString());
		boolean commandIsOk = executeCommand(command);
		return commandIsOk;
	}

	/**
	 * Accepts a call to the emulator.
	 * 
	 * @param phoneNumber
	 *        - the phone number, that calls the emulator.
	 * 
	 * @return the command response from the emulator console.
	 * @throws EmulatorConnectionFailedException
	 */
	public synchronized boolean acceptCall(PhoneNumber phoneNumber) throws EmulatorConnectionFailedException
	{
		String command = String.format(COMMAND_ACCEPT_CALL, phoneNumber.toString());
		boolean commandIsOk = executeCommand(command);
		return commandIsOk;
	}

	/**
	 * Holds a call to the emulator.
	 * 
	 * @param phoneNumber
	 *        - the phone number, that calls the emulator.
	 * 
	 * @return the command response from the emulator console.
	 * @throws EmulatorConnectionFailedException
	 */
	public synchronized boolean holdCall(PhoneNumber phoneNumber) throws EmulatorConnectionFailedException
	{
		String command = String.format(COMMAND_HOLD_CALL, phoneNumber.toString());
		boolean commandIsOk = executeCommand(command);
		return commandIsOk;
	}

	/**
	 * Cancels a call to the emulator.
	 * 
	 * @param phoneNumber
	 *        - the phone number, that calls the emulator.
	 * 
	 * @return the command response from the emulator console.
	 * @throws EmulatorConnectionFailedException
	 */
	public synchronized boolean cancelCall(PhoneNumber phoneNumber) throws EmulatorConnectionFailedException
	{
		String command = String.format(COMMAND_CANCEL_CALL, phoneNumber.toString());
		boolean commandIsOk = executeCommand(command);
		return commandIsOk;
	}

	/**
	 * Sends a string to the emulator console and returns a value indicating if the response was OK or KO.
	 * 
	 * @param command
	 *        The command string.
	 * @return true if OK was found, false if KO was found.
	 * @throws EmulatorConnectionFailedException
	 */
	protected synchronized boolean executeCommand(String command) throws EmulatorConnectionFailedException
	{
		// if the command does not end with a newline, this call will block.
		// so we make sure this never happens.
		if (command.endsWith("\n") == false)
		{
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
	protected synchronized String executeCommandWithResponse(String command) throws EmulatorConnectionFailedException
	{
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
	private void sendCommand(String command) throws EmulatorConnectionFailedException
	{
		try
		{
			byte[] commandBuffer;
			try
			{
				commandBuffer = command.getBytes(DEFAULT_ENCODING);
			}
			catch (UnsupportedEncodingException e)
			{
				throw new EmulatorConnectionFailedException("Exception occured while writing to the socket channel : Unsupported encoding.",
															e);
			}

			ByteBuffer wrappedBuffer = ByteBuffer.wrap(commandBuffer, 0, commandBuffer.length);

			// Send the command
			int numberOfAttempts = 0;
			while (wrappedBuffer.position() != wrappedBuffer.limit())
			{
				int writeCount = socketChannel.write(wrappedBuffer);

				if (writeCount < 0)
				{
					throw new EmulatorConnectionFailedException("Exception occured while writing to the socket channel : EOF.");
				}
				else if (writeCount == 0)
				{
					if (numberOfAttempts * WAIT_TIME > SOCKET_TIMEOUT)
					{
						throw new EmulatorConnectionFailedException("Exception occured while writing to the socket channel : Timeout.");
					}
					// non-blocking spin
					try
					{
						Thread.sleep(WAIT_TIME);
					}
					catch (InterruptedException ie)
					{
					}
					numberOfAttempts++;
				}
				else
				{
					numberOfAttempts = 0;
				}
			}
		}
		catch (EmulatorConnectionFailedException e)
		{
			throw e;
		}
		catch (Exception e)
		{
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
	private boolean responseIsFine() throws EmulatorConnectionFailedException
	{
		try
		{
			ByteBuffer wrappedBuffer = ByteBuffer.wrap(buffer, 0, buffer.length);
			int numberOfAttempts = 0;

			while (wrappedBuffer.position() != wrappedBuffer.limit())
			{
				int readCount = socketChannel.read(wrappedBuffer);

				if (readCount < 0)
				{
					throw new EmulatorConnectionFailedException("Exception occured while reading from the socket channel : EOF.");
				}
				else if (readCount == 0)
				{
					if (numberOfAttempts * WAIT_TIME > SOCKET_TIMEOUT)
					{
						throw new EmulatorConnectionFailedException("Exception occured while reading from the socket channel : Timeout.");
					}
					// non-blocking spin
					try
					{
						Thread.sleep(WAIT_TIME);
					}
					catch (InterruptedException ie)
					{
					}
					numberOfAttempts++;
				}
				else
				{
					numberOfAttempts = 0;
				}

				// check the last few char aren't OK. For a valid message to test
				// we need at least 4 bytes (OK/KO + \r\n)
				if (wrappedBuffer.position() >= 4)
				{
					int pos = wrappedBuffer.position();
					if (endsWithOK(pos))
					{
						return true;
					}
					if (endsWithKO(pos))
					{
						return false;
					}
					// TODO research for existence of emulator console responses not containing "OK" or "KO"
				}
			}
		}
		catch (EmulatorConnectionFailedException e)
		{
			throw e;
		}
		catch (Exception e)
		{
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
	private boolean endsWithOK(int currentPosition)
	{
		if (buffer[currentPosition - 1] == '\n' && buffer[currentPosition - 2] == '\r'
				&& buffer[currentPosition - 3] == 'K' && buffer[currentPosition - 4] == 'O')
		{
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
	private boolean endsWithKO(int currentPosition)
	{
		// now loop backward looking for the previous CRLF, or the beginning of the buffer
		int i = 0;
		for (i = currentPosition - 3; i >= 0; i--)
		{
			if (buffer[i] == '\n')
			{
				// found \n!
				if (i > 0 && buffer[i - 1] == '\r')
				{
					// found \r!
					break;
				}
			}
		}

		// here it is either -1 if we reached the start of the buffer without finding
		// a CRLF, or the position of \n. So in both case we look at the characters at i+1 and i+2
		if (buffer[i + 1] == 'K' && buffer[i + 2] == 'O')
		{
			// found KO (error)
			return true;
		}
		return false;
	}
}
