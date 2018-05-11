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
