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

package com.musala.atmosphere.agent.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.apache.log4j.Logger;

import com.musala.atmosphere.agent.exception.UniqueAgentIdCalculationFailedException;

public class AgentIdCalculator {

    private String Id;

    private final static String AGENTID_HASH_ALGORITHM = "md5";

    private final static Logger LOGGER = Logger.getLogger(AgentIdCalculator.class.getCanonicalName());;

    private String calculateId() {
        String uniqueIdErrorMessage = "Unique ID for the current Agent calculation failed.";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            StringBuilder ipConcatBuilder = new StringBuilder();
            while (interfaces.hasMoreElements()) {
                NetworkInterface netInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    String addressString = address.getHostAddress();
                    ipConcatBuilder.append(addressString);
                }
            }

            String ipConcatString = ipConcatBuilder.toString();
            byte[] ipConcatBytes = ipConcatString.getBytes();
            MessageDigest digest = MessageDigest.getInstance(AGENTID_HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(ipConcatBytes);
            return (new HexBinaryAdapter()).marshal(hashBytes);

        } catch (SocketException e) {
            LOGGER.fatal(uniqueIdErrorMessage, e);
            throw new UniqueAgentIdCalculationFailedException(uniqueIdErrorMessage, e);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.fatal("Could not get instance of MessageDigest for the passed hash algorithm.", e);
            throw new UniqueAgentIdCalculationFailedException(uniqueIdErrorMessage, e);
        }
    }

    public String getId() {
        if (Id == null) {
            Id = calculateId();
        }

        return Id;
    }

}
