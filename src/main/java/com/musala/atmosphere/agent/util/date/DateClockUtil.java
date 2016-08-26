package com.musala.atmosphere.agent.util.date;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * This class is used to store constants and methods, used for managing dates and times.
 * 
 * @author vladimir.vladimirov
 * 
 */
public class DateClockUtil {

    public static final String CLOCK_FORMAT = "HH:mm:ss";

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final String SEPARATOR = " ";

    public static final String DATE_AND_CLOCK_FORMAT = CLOCK_FORMAT + SEPARATOR + DATE_FORMAT;

    /**
     * 
     * @param since
     *        - Date, representing a moment in time, in which we want to know how much time has passed since this moment
     *        until now.
     * @return - the time, passed between the current time moment and the passed date, in format {@value #CLOCK_FORMAT}.
     */
    public static String getTimeInterval(Date since) {
        Date currentTime = new Date();
        long timeInterval = currentTime.getTime() - since.getTime();

        Calendar cal = Calendar.getInstance();
        cal.set(0, 0, 0, 0, 0);
        cal.setTimeInMillis(cal.getTimeInMillis() + timeInterval);

        SimpleDateFormat timeIntervalFormatter = new SimpleDateFormat(DateClockUtil.CLOCK_FORMAT);
        String formattedTimeInterval = timeIntervalFormatter.format(cal.getTime());
        return formattedTimeInterval;
    }

    /**
     * 
     * @param time
     *        - a {@link Date} object, which represents given moment in time.
     * @return String representation of the passed date, done in format {@value #DATE_AND_CLOCK_FORMAT}.
     */
    public static String formatDateAndTime(Date time) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(DateClockUtil.DATE_AND_CLOCK_FORMAT);
        String formattedDateAndTime = dateFormatter.format(time);
        return formattedDateAndTime;
    }
}
