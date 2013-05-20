package com.musala.atmosphere.agent.devicewrapper;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.musala.atmosphere.agent.DevicePropertyStringConstants;
import com.musala.atmosphere.commons.sa.IWrapDevice;
import com.musala.atmosphere.commons.sa.exceptions.CommandFailedException;
import com.musala.atmosphere.commons.sa.util.Pair;

public abstract class AbstractWrapDevice extends UnicastRemoteObject implements IWrapDevice
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9122701818928360023L;

	protected IDevice wrappedDevice;

	public AbstractWrapDevice(IDevice deviceToWrap) throws RemoteException
	{
		wrappedDevice = deviceToWrap;
	}

	@Override
	public Pair<Integer, Integer> getNetworkSpeed() throws RemoteException
	{
		// TODO get network speed for abstract devices
		return null;
	}

	@Override
	public int getBatteryLevel() throws RemoteException, CommandFailedException
	{
		int level;
		try
		{
			level = wrappedDevice.getBatteryLevel();
		}
		catch (TimeoutException | AdbCommandRejectedException | IOException | ShellCommandUnresponsiveException e)
		{
			// Redirect the exception to the server
			throw new CommandFailedException(	"getBatteryLevel failed. See the enclosed exception for more information.",
												e);
		}
		return level;
	}

	@Override
	public String getCPUType() throws RemoteException
	{
		String cpu = wrappedDevice.getProperty(DevicePropertyStringConstants.PROPERTY_CPU_TYPE.toString());
		return cpu;
	}

	@Override
	public int getFreeRAM() throws RemoteException
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String executeShellCommand(String command) throws RemoteException, CommandFailedException
	{
		String response = "";

		try
		{
			CollectingOutputReceiver outputReceiver = new CollectingOutputReceiver();
			wrappedDevice.executeShellCommand(command, outputReceiver);
			response = outputReceiver.getOutput();
		}
		catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e)
		{
			// Redirect the exception to the server
			throw new CommandFailedException(	"Shell command execution failed. See the enclosed exception for more information.",
												e);
		}

		return response;
	}

	@Override
	public Byte[] getScreenshot() throws RemoteException
	{
		// TODO screenshot
		return null;
	}

	@Override
	public void initAPKInstall() throws RemoteException
	{
		// TODO init apk install
	}

	@Override
	public void appendToAPK(Byte[] bytes) throws RemoteException
	{
		// TODO append to apk
	}

	@Override
	public void buildAndInstallAPK() throws RemoteException
	{
		// TODO build and install apk

	}

	@Override
	public void discardAPK() throws RemoteException
	{
		// TODO discard apk

	}

	@Override
	public abstract void setNetworkSpeed(Pair<Integer, Integer> speeds) throws RemoteException;

	@Override
	public abstract void setBatteryLevel(int level) throws RemoteException;
}
