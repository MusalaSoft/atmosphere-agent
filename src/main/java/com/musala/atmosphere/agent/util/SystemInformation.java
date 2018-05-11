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

import java.io.File;

import org.apache.log4j.Logger;
import org.hyperic.sigar.Cpu;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 *
 * @author valyo.yolovski
 *
 */
public class SystemInformation {
    private final static Logger LOGGER = Logger.getLogger(SystemInformation.class.getCanonicalName());

    private static final Sigar sigar = new Sigar();

    private static final long FALLBACK_TOTAL_RAM = 0;

    private static final long FALLBACK_FREE_RAM = 0;

    private static final int FALLBACK_CPU_COUNT = 1;

    /**
     * Returns true if Intel HAXM (Hardware Accelerated Execution Manager) is available and false if it is not.
     *
     * @return - true if HAXM is available; false otherwise.
     */
    public static boolean isHaxm() {
        // TODO This method should be refactored so we maintain cross-platform support.
        // We'll probably need a class that implements logic for cross-platform command execution.
        // Or maybe a low level libraries (dum dum dum...).

        // try {
        // String[] haxmValidationCommand = {"cmd.exe", "/C", "sc query intelhaxm"};
        // Runtime runtime = Runtime.getRuntime();
        // Process haxmValidationProcess = runtime.exec(haxmValidationCommand);
        // BufferedReader haxmValidationReader = new BufferedReader(new
        // InputStreamReader(haxmValidationProcess.getInputStream()));
        // String readLine = haxmValidationReader.readLine();
        // while (readLine != null) {
        // if (readLine.contains("SERVICE_NAME: intelhaxm")) {
        // LOGGER.info("Haxm available.");
        // return true;
        // }
        //
        // readLine = haxmValidationReader.readLine();
        // }
        // } catch (IOException e) {
        // LOGGER.warn("Could not validate HAXM.", e);
        // }

        return false;
    }

    /**
     * Returns the free disk space on the hard disk in MBs.
     *
     * @return the free disk space on the hard disk in MBs.
     */
    public static long getFreeDiskSpace() {
        File[] roots = File.listRoots();
        long freeSpace = 0;
        for (File root : roots) {
            freeSpace = freeSpace + root.getFreeSpace();
        }

        // Convert the free space from bytes to MBs.
        freeSpace /= 1024 * 1024;

        return freeSpace;
    }

    /**
     * Gets the total RAM memory available on the device.
     *
     * @return the total RAM memory in MBs.
     */
    public static long getTotalRam() {
        try {
            Mem memory = sigar.getMem();
            long totalMemory = memory.getTotal();

            // Convert the total memory from bytes to MBs.
            totalMemory /= 1024 * 1024;
            return totalMemory;
        } catch (SigarException e) {
            LOGGER.warn("Could not get total RAM memory.", e);
        }

        return FALLBACK_TOTAL_RAM;
    }

    /**
     * Gets the free RAM memory available on the device.
     *
     * @return the free RAM memory in MBs.
     */
    public static long getFreeRam() {
        try {
            Mem memory = sigar.getMem();
            long freeMemory = memory.getFree();

            // Convert the free memory from bytes to MBs.
            freeMemory /= 1024 * 1024;
            return freeMemory;
        } catch (SigarException e) {
            LOGGER.warn("Could not get the free RAM memory.", e);
        }

        return FALLBACK_FREE_RAM;
    }

    /**
     * Gets the number of CPUs available on the device.
     *
     * @return the number of CPUs available on the device.
     */
    public static int getCpuCount() {
        try {
            Cpu[] cpus = sigar.getCpuList();
            return cpus.length;
        } catch (SigarException e) {
            LOGGER.warn("Could not get the number of CPUs.", e);
        }

        return FALLBACK_CPU_COUNT;
    }

    /**
     * Executes SciMark benchmark tests and returns the benchmark score.
     *
     * @return SciMark benchmark score.
     */
    public static double getScimarkScore() {
        LOGGER.info("Benchmarking system. This may take a while...");
        // TODO uncomment this line
        double score = 100;// Benchmark.getScore();
        LOGGER.info("Benchmarking system done. Result : " + score);

        return score;
    }
}
