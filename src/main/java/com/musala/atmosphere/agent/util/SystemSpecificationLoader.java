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
