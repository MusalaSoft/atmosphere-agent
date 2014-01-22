package com.musala.atmosphere.agent.devicewrapper;

import java.io.IOException;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.TimeoutException;
import com.musala.atmosphere.agent.devicewrapper.util.ForwardingPortFailedException;
import com.musala.atmosphere.agent.exception.RemovePortForwardFailedException;
import com.musala.atmosphere.agent.util.PortAllocator;
import com.musala.atmosphere.commons.ad.gestureplayer.GesturePlayerConstants;
import com.musala.atmosphere.commons.ad.service.ServiceConstants;

/**
 * Class that handles device port forwarding. Used to forward a local port (issued to a device) to a remote (on-device)
 * port so one local port can be reused to create socket connection to several on-device applications.
 * 
 * @author georgi.gaydarov
 * 
 */
public class PortForwardingService
{
	private final IDevice device;

	private final int localForwardedPort;

	private final static int NOT_FORWARDED_PORT = -1;

	private int remoteForwardedPort = NOT_FORWARDED_PORT;

	/**
	 * Creates a port forwarding service for the passed device and allocates a local port that will be used for
	 * forwarding.
	 * 
	 * @param device
	 *        - device for which to create a port forwarding service.
	 */
	public PortForwardingService(IDevice device)
	{
		this.device = device;
		localForwardedPort = PortAllocator.getFreePort();
	}

	/**
	 * Forwards the allocated local port to the ATMOSPHERE service's port on the wrapped device.
	 * 
	 * @throws ForwardingPortFailedException
	 */
	public void forwardServicePort() throws ForwardingPortFailedException
	{
		forwardPort(ServiceConstants.SERVICE_PORT);
	}

	/**
	 * Forwards the allocated local port to the ATMOSPHERE gesture player's port on the wrapped device.
	 * 
	 * @throws ForwardingPortFailedException
	 */
	public void forwardGesturePlayerPort() throws ForwardingPortFailedException
	{
		forwardPort(GesturePlayerConstants.PLAYER_PORT);
	}

	private void forwardPort(int remotePort) throws ForwardingPortFailedException
	{
		if (remoteForwardedPort == remotePort)
		{
			return;
		}

		try
		{
			device.createForward(localForwardedPort, remotePort);
			remoteForwardedPort = remotePort;
		}
		catch (TimeoutException | AdbCommandRejectedException | IOException e)
		{
			String errorMessage = String.format("Could not forward port for %s.", device.getSerialNumber());
			throw new ForwardingPortFailedException(errorMessage, e);
		}
	}

	/**
	 * Removes the port forwarding.
	 * 
	 * @throws RemovePortForwardFailedException
	 */
	public void removeForward() throws RemovePortForwardFailedException
	{
		if (remoteForwardedPort == NOT_FORWARDED_PORT)
		{
			return;
		}

		try
		{
			device.removeForward(localForwardedPort, remoteForwardedPort);
			remoteForwardedPort = NOT_FORWARDED_PORT;
		}
		catch (TimeoutException | AdbCommandRejectedException | IOException e)
		{
			String errorMessage = String.format("Could not remove port forwarding for %s.", device.getSerialNumber());
			throw new RemovePortForwardFailedException(errorMessage, e);
		}
	}

	/**
	 * @return the allocated local port for this forwarding service.
	 */
	public int getLocalForwardedPort()
	{
		return localForwardedPort;
	}

	@Override
	public void finalize()
	{
		removeForward();
	}
}
