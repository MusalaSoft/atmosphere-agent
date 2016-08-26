package com.musala.atmosphere.agent.util;

/**
 * <p>
 * Provides static methods that convert memory units contained in various formats.
 * </p>
 * 
 * @author georgi.gaydarov
 * 
 */
public class MemoryUnitConverter {
    /**
     * Converts a memory indicating string that is in the format "[xx]MB", "[xx] MB", "[xx] Gb" and so on to integer, in
     * megabytes.
     * 
     * @param memoryAmount
     *        String containing a memory amount.
     * @return Memory amount in megabytes.
     */
    public static int convertMemoryToMB(String memoryAmount) {
        // Iterate backwards until we find the end of the number
        String number = "0";
        StringBuilder unitStringBuilder = new StringBuilder();
        for (int i = memoryAmount.length() - 1; i >= 0; i--) {
            char charAtPos = memoryAmount.charAt(i);
            if (charAtPos >= '0' && charAtPos <= '9') {
                number = memoryAmount.substring(0, i + 1);
                number = number.trim();
                break;
            }
            unitStringBuilder.insert(0, charAtPos);
        }

        // Suffix is our memory's unit, in upper case
        String suffix = unitStringBuilder.toString().trim().toUpperCase();

        long amount = Long.parseLong(number);

        // Multiply by the unit
        switch (suffix) {
            case "M":
            case "MB": {
                break;
            }
            case "GB":
            case "G": {
                amount = amount * 1024;
                break;
            }
            case "KB":
            case "K": {
                amount = amount / 1024;
                break;
            }
            case "B": {
                amount = amount / 1048576;
                break;
            }
            case "TB": {
                amount = amount * 1048576;
                break;
            }
        }

        return (int) amount;
    }

}
