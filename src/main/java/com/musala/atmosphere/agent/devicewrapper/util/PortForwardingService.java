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

package com.musala.atmosphere.agent.devicewrapper.util;

import java.io.IOException;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.TimeoutException;
import com.musala.atmosphere.agent.exception.ForwardingPortFailedException;
import com.musala.atmosphere.agent.exception.PortForwardingRemovalException;
import com.musala.atmosphere.agent.util.PortAllocator;

/**
 * Class that handles device port forwarding. Used to forward a local port (issued to a device) to a remote (on-device)
 * port so one local port can be reused to create socket connection to several on-device applications.
 *
 * @author georgi.gaydarov
 *
 */
public class PortForwardingService {
    private final IDevice device;

    private final int localForwardedPort;

    private int remoteForwardedPort;

    private boolean isForwarded;

    /**
     * Creates a port forwarding service for the passed device and allocates a local port that will be used for
     * forwarding.
     *
     * @param device
     *        - device for which to create a port forwarding service.
     * @param remotePort
     *        - the remote port
     */
    public PortForwardingService(IDevice device, int remotePort) {
        PortAllocator portAllocator = new PortAllocator();

        this.device = device;
        this.remoteForwardedPort = remotePort;
        this.isForwarded = false;
        this.localForwardedPort = portAllocator.getPort();
    }

    /**
     * Forwards the allocated local port to the remote port on the wrapped device.
     *
     * @throws ForwardingPortFailedException
     *         Thrown when the forwarding of port to an ATMOSPHERE on-device application fails.
     */
    public void forwardPort() throws ForwardingPortFailedException {
        if (isForwarded) {
            return;
        }

        try {
            device.createForward(localForwardedPort, remoteForwardedPort);
            isForwarded = true;
        } catch (TimeoutException | AdbCommandRejectedException | IOException e) {
            String errorMessage = String.format("Could not forward port for %s.", device.getSerialNumber());
            throw new ForwardingPortFailedException(errorMessage, e);
        }
    }

    /**
     * Removes the port forwarding.
     *
     * @throws PortForwardingRemovalException
     *         Thrown when the port forwarded to the ATMOSPHERE service port can not be freed.
     */
    public void removeForward() throws PortForwardingRemovalException {
        if (!isForwarded) {
            return;
        }

        try {
            device.removeForward(localForwardedPort, remoteForwardedPort);
            isForwarded = false;
        } catch (TimeoutException | AdbCommandRejectedException | IOException e) {
            String errorMessage = String.format("Could not remove port forwarding for %s.", device.getSerialNumber());
            throw new PortForwardingRemovalException(errorMessage, e);
        }
    }

    /**
     * @return the allocated local port for this forwarding service.
     */
    public int getLocalForwardedPort() {
        return localForwardedPort;
    }

    /**
     * Removes port forwarding and releases allocated ports.
     */
    public void stop() {
        new PortAllocator().releasePort(localForwardedPort);
        removeForward();
    }
}
