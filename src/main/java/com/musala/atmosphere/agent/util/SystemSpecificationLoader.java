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

import org.apache.log4j.Logger;

import com.musala.atmosphere.commons.sa.SystemSpecification;

/**
 * Loads the system specifications of the machine.
 * 
 * @author yordan.petrov
 * 
 */
public class SystemSpecificationLoader {
    private final static Logger LOGGER = Logger.getLogger(SystemSpecificationLoader.class.getCanonicalName());

    private SystemSpecification systemSpecification;

    /**
     * Gets the {@link SystemSpecification} of the machine. The first time this method is called all specifications are
     * updated. Later calls update only the specifications that can change during runtime.
     * 
     * @return the {@link SystemSpecification} of the machine.
     */
    public SystemSpecification getSpecification() {
        if (systemSpecification == null) {
            systemSpecification = loadSpecification();
        } else {
            systemSpecification = updateSpecification(systemSpecification);
        }

        return systemSpecification;
    }

    /**
     * Loads all system specifications.
     * 
     * @return the {@link SystemSpecification} of the machine.
     */
    private SystemSpecification loadSpecification() {
        LOGGER.info("Loading system specifications...");

        systemSpecification = new SystemSpecification();

        // Get the total RAM memory in bytes and convert it to MBs.
        long totalRam = SystemInformation.getTotalRam();
        systemSpecification.setTotalRam(totalRam);

        // Get the free RAM memory in bytes and convert it to MBs.
        long freeRam = SystemInformation.getFreeRam();
        systemSpecification.setFreeRam(freeRam);

        int cpuCount = SystemInformation.getCpuCount();
        systemSpecification.setCpuCount(cpuCount);

        boolean isHaxm = SystemInformation.isHaxm();
        systemSpecification.setHaxm(isHaxm);

        // Get the free disk space in bytes and convert it to MBs.
        long freeDiskSpace = SystemInformation.getFreeDiskSpace();
        systemSpecification.setFreeDiskSpace(freeDiskSpace);

        double scimarkScore = SystemInformation.getScimarkScore();
        systemSpecification.setScimarkScore(scimarkScore);

        return systemSpecification;
    }

    /**
     * Updates all system specifications that can change during runtime.
     * 
     * @return the {@link SystemSpecification} of the machine.
     */
    private SystemSpecification updateSpecification(SystemSpecification systemSpecification) {
        // Get the free RAM memory in bytes and convert it to MBs.
        long freeRam = SystemInformation.getFreeRam();
        systemSpecification.setFreeRam(freeRam);

        // Get the free disk space in bytes and convert it to MBs.
        long freeDiskSpace = SystemInformation.getFreeDiskSpace();
        systemSpecification.setFreeDiskSpace(freeDiskSpace);

        return systemSpecification;
    }
}
