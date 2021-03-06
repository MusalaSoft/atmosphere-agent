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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;

/**
 * A Profiling class. Returns hardware usage statistics for a device.
 *
 * @author georgi.gaydarov
 *
 */
public class DeviceProfiler {
    public static String FREE_MEMORY_ID = "Free";

    private IDevice profiledDevice;

    private final String DUMP_MEMORY_INFO_COMMAND = "cat /proc/meminfo ; dumpsys meminfo";

    public DeviceProfiler(IDevice deviceToProfile) {
        profiledDevice = deviceToProfile;
    }

    /**
     * Gets information about memory allocated by all processes on the profiled device.
     *
     * @return a map, containing processes and kilobytes of allocated memory by them.
     * @throws IOException
     *         when an I/O error occurs.
     * @throws TimeoutException
     *         Exception thrown when a connection to Adb failed with a timeout.
     * @throws AdbCommandRejectedException
     *         thrown when adb refuses a command.
     *
     * @throws ShellCommandUnresponsiveException
     *         thrown when a shell command executed on a device takes too long to send its output.
     */
    public Map<String, Long> getMeminfoDataset()
        throws TimeoutException,
            AdbCommandRejectedException,
            ShellCommandUnresponsiveException,
            IOException {
        Map<String, Long> dataset = new HashMap<>();
        CollectingOutputReceiver outputReceiver = new CollectingOutputReceiver();
        profiledDevice.executeShellCommand(DUMP_MEMORY_INFO_COMMAND, outputReceiver);
        String output = outputReceiver.getOutput();

        BufferedReader br = new BufferedReader(new StringReader(output));
        long total = 0;

        // Scan /proc/meminfo
        Pattern valuePattern = Pattern.compile("(\\d+) kB");
        while (true) {

            String line = br.readLine();
            if (line == null) {
                // End of input
                break;
            }
            Matcher match = valuePattern.matcher(line);
            if (match.find()) {
                long kb = Long.parseLong(match.group(1));
                if (line.startsWith("MemTotal:")) {
                    total = kb;
                } else if (line.startsWith("MemFree:")) {
                    dataset.put(FREE_MEMORY_ID, kb);
                    total -= kb;
                } else if (line.startsWith("Slab:")) {
                    dataset.put("Slab", kb);
                    total -= kb;
                } else if (line.startsWith("PageTables:")) {
                    dataset.put("PageTables", kb);
                    total -= kb;
                } else if (line.startsWith("Buffers:") && kb > 0) {
                    dataset.put("Buffers", kb);
                    total -= kb;
                } else if (line.startsWith("Inactive:")) {
                    dataset.put("Inactive", kb);
                    total -= kb;
                }
            } else {
                break;
            }
        }

        // Scan 'dumpsys meminfo'
        Pattern processPattern = Pattern.compile("(\\d+) kB: (\\S+) (.+)");
        while (true) {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            if (line.startsWith("Total PSS by OOM")) {
                // end of 'Total PSS by process' section
                break;
            }

            Matcher match = processPattern.matcher(line);
            if (!match.find()) {
                continue;
            }

            // Extract pss field
            long pss = Long.parseLong(match.group(1));
            // Get the process name
            String process = match.group(2);

            dataset.put(process, pss);
            total -= pss;
        }

        // The Pss calculation is not necessarily accurate as accounting memory to
        // a process is not accurate. So only if there really is unaccounted for memory do we
        // add it to the pie.
        if (total > 0) {
            dataset.put("Unknown", total);
        }

        return dataset;
    }
}
