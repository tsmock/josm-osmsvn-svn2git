// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.ohe;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.plugins.ohe.gui.TimeRect;
import org.openstreetmap.josm.plugins.ohe.parser.OpeningTimeCompiler;

/**
 * Collection of utility methods.
 */
public final class OpeningTimeUtils {
    
    private OpeningTimeUtils() {
        // Hide default constructors for utilities classes
    }
    
    /**
     * Implements the subtraction of daytimes in spans of days when a day in the list occurs direct afterwards 
     */
    public static List<int[]> convert(List<DateTime> dateTimes) {
        ArrayList<int[]> ret = new ArrayList<>(); // the list which is
        // returned
        for (int i = 0; i < dateTimes.size(); ++i) { // iterate over every entry
            DateTime dateTime = dateTimes.get(i);
            ArrayList<DateTime> newDateTimes = new ArrayList<>();

            // test if the given entry is a single dayspan
            if (dateTime.daySpans.size() == 1 && dateTime.daySpans.get(0).isSpan()) {
                ArrayList<DaySpan> partDaySpans = new ArrayList<>();
                int startDay = dateTime.daySpans.get(0).startDay;

                // look in every entry behind
                while (i + 1 < dateTimes.size()) {
                    List<DaySpan> following = dateTimes.get(i + 1).daySpans;
                    if (following.size() == 1 && following.get(0).startDay > dateTime.daySpans.get(0).startDay
                            && following.get(0).endDay < dateTime.daySpans.get(0).endDay) {
                        partDaySpans.add(new DaySpan(startDay, following.get(0).startDay - 1));
                        startDay = following.get(0).endDay + 1;
                        newDateTimes.add(dateTimes.get(i + 1));
                        i++;
                    } else {
                        break;
                    }
                }

                partDaySpans.add(new DaySpan(startDay, dateTime.daySpans.get(0).endDay));
                newDateTimes.add(new DateTime(partDaySpans, dateTime.daytimeSpans));
            }
            if (newDateTimes.isEmpty()) {
                newDateTimes.add(dateTime);
            }

            // create the int-array
            for (DateTime dateTime2 : newDateTimes) {
                for (DaySpan dayspan : dateTime2.daySpans) {
                    for (DaytimeSpan timespan : dateTime2.daytimeSpans) {
                        if (!timespan.isOff()) {
                            ret.add(new int[]{dayspan.startDay, dayspan.endDay, timespan.startMinute,
                                    timespan.endMinute});
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * The span of days
     */
    public static class DaySpan {
        /** The start day */
        public final int startDay;
        /** The end day */
        public final int endDay;

        /**
         * Create a new day span
         * @param startDay The weekday start
         * @param endDay The weekday end
         */
        public DaySpan(int startDay, int endDay) {
            this.startDay = startDay;
            this.endDay = endDay;
        }

        public boolean isSpan() {
            return endDay > startDay;
        }

        public boolean isSingleDay() {
            return startDay == endDay;
        }
    }

    /**
     * A span of time within a day
     */
    public static class DaytimeSpan {
        /** The start minute of the time span */
        public final int startMinute;
        /** The end minute of the time span */
        public final int endMinute;

        /**
         * Create a new time span
         * @param startMinute The start minute
         * @param endMinute The end minute
         */
        public DaytimeSpan(int startMinute, int endMinute) {
            this.startMinute = startMinute;
            this.endMinute = endMinute;
        }

        public boolean isOff() {
            return startMinute == -1;
        }

        public boolean isSpan() {
            return endMinute > startMinute;
        }
    }

    /**
     * A collection of days and time spans
     */
    public static class DateTime {
        /** The weekday spans */
        public final List<DaySpan> daySpans;
        /** The time spans */
        public final List<DaytimeSpan> daytimeSpans;

        /**
         * Create a new collection of time spans
         * @param daySpans The weekday spans
         * @param daytimeSpans The times for the weekday spans
         */
        public DateTime(List<DaySpan> daySpans, List<DaytimeSpan> daytimeSpans) {
            this.daySpans = daySpans;
            this.daytimeSpans = daytimeSpans;
        }
    }

    /**
     * Returns a String (e.g "Mo-Sa 10:00-20:00; Tu off") representing the TimeRects
     */
    public static String makeStringFromRects(List<TimeRect> givenTimeRects) {
        // create an array of booleans representing every minute on all the days in a week
        boolean[][] minuteArray = new boolean[7][24 * 60 + 2];
        for (int day = 0; day < 7; ++day) {
            for (int minute = 0; minute < 24 * 60 + 2; ++minute) {
                minuteArray[day][minute] = false;
            }
        }
        for (TimeRect timeRect : givenTimeRects) {
            for (int day = timeRect.getDayStart(); day <= timeRect.getDayEnd(); ++day) {
                for (int minute = timeRect.getMinuteStart(); minute <= timeRect.getMinuteEnd(); ++minute) {
                    minuteArray[day][minute] = true;
                }
            }
        }

        StringBuilder ret = new StringBuilder();
        int[] days = new int[7]; // an array representing the status of the days
        // 0 means nothing done with this day yet
        // 8 means the day is off
        // 0<x<8 means the day have the openinghours of day x
        // -8<x<0 means nothing done with this day yet, but it intersects a
        // range of days with same opening_hours
        for (int i = 0; i < 7; ++i) {
            String add = "";

            if (isArrayEmpty(minuteArray[i]) && days[i] == 0) {
                days[i] = 8;
            } else if (isArrayEmpty(minuteArray[i]) && days[i] < 0) {
                add = OpeningTimeCompiler.WEEKDAYS[i] + " off";
                days[i] = -8;
            } else if (days[i] <= 0) {
                days[i] = i + 1;
                int lastSameDay = i;
                int sameDayCount = 1;
                for (int j = i + 1; j < 7; ++j) {
                    if (arraysEqual(minuteArray[i], minuteArray[j])) {
                        days[j] = i + 1;
                        lastSameDay = j;
                        sameDayCount++;
                    }
                }
                if (sameDayCount == 1) {
                    // a single Day with this special opening_hours
                    add = OpeningTimeCompiler.WEEKDAYS[i] + " " + makeStringFromMinuteArray(minuteArray[i]);
                } else if (sameDayCount == 2) {
                    // exactly two Days with this special opening_hours
                    add = OpeningTimeCompiler.WEEKDAYS[i] + "," + OpeningTimeCompiler.WEEKDAYS[lastSameDay] + " "
                    + makeStringFromMinuteArray(minuteArray[i]);
                } else if (sameDayCount > 2) {
                    // more than two Days with this special opening_hours
                    add = OpeningTimeCompiler.WEEKDAYS[i] + "-" + OpeningTimeCompiler.WEEKDAYS[lastSameDay] + " "
                    + makeStringFromMinuteArray(minuteArray[i]);
                    for (int j = i + 1; j < lastSameDay; ++j) {
                        if (days[j] == 0) {
                            days[j] = -i - 1;
                        }
                    }
                }
            }

            if (!add.isEmpty()) {
                if (ret.length() != 0) {
                    ret.append("; ");
                }
                ret.append(add);
            }
        }
        return ret.toString();
    }

    /**
     * Returns a String representing the openinghours on one special day (e.g. "10:00-20:00")
     */
    private static String makeStringFromMinuteArray(boolean[] minutes) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < minutes.length; ++i) {
            if (minutes[i]) {
                int start = i;
                while (i < minutes.length && minutes[i]) {
                    ++i;
                }
                String addString = timeString(start);
                if (i - 1 == 24 * 60 + 1) {
                    addString += "+";
                } else if (start != i - 1) {
                    addString += "-" + timeString(i - 1);
                }
                if (ret.length() != 0) {
                    ret.append(',');
                }
                ret.append(addString);
            }
        }
        return ret.toString();
    }

    /**
     * Convert minutes to a string
     * @param minutes integer in range from 0 and 24*60 inclusive
     * @return a formatted string of the time (for example "01:45 PM" or "13:45")
     */
    public static String timeString(int minutes) {
        return timeString(minutes, ClockSystem.TWENTYFOUR_HOURS);
    }

    /**
     * Convert minutes to a string
     * @param minutes integer in range from 0 and 24*60 inclusive
     * @param hourMode 12 or 24 hour clock
     * @return a formatted string of the time (for example "01:45 PM" or "13:45")
     */
    public static String timeString(int minutes, ClockSystem hourMode) {
        return timeString(minutes, hourMode, false);
    }

    /**
     * Convert minutes to a string
     * @param minutes integer in range from 0 and 24*60 inclusive
     * @param hourMode 12 or 24 hour clock
     * @param showPeriod if 12 hour clock is chosen, the "AM"/"PM" will be shown
     * @return a formatted string of the time (for example "01:45 PM" or "13:45")
     */
    public static String timeString(int minutes, ClockSystem hourMode, boolean showPeriod) {
        int h = minutes / 60;
        String period = "";
        if (hourMode == ClockSystem.TWELVE_HOURS) {
            if (h == 24)
                return "midnight";
            else {
                if (showPeriod) {
                    period = h < 12 ? " AM" : " PM";
                }
                h %= 12;
                if (h == 0) {
                    h = 12;
                }
            }
        }
        int m = minutes % 60;
        return (h < 10 ? "0" : "") + h + ":" + (m < 10 ? "0" : "") + m + period;
    }

    private static boolean isArrayEmpty(boolean[] bs) {
        for (boolean b : bs) {
            if (b)
                return false;
        }
        return true;
    }

    private static boolean arraysEqual(boolean[] bs, boolean[] bs2) {
        boolean ret = true;
        for (int i = 0; i < bs.length; i++) {
            ret &= bs[i] == bs2[i];
        }
        return ret;
    }
    
    /**
     * Ensures the given day is comprised between 0 and 6.
     * @param day The day to check
     * @param paramName The parameter name, used in error message
     * @throws IllegalArgumentException if the day is invalid
     */
    public static void ensureValidDay(int day, String paramName) throws IllegalArgumentException {
        if (day < 0 || day > 6) {
            throw new IllegalArgumentException(paramName + " is not a valid day (0-6). Given value is " + day);
        }
    }

    /**
     * Ensures the given minute is comprised between 0 and 24*60+1.
     * @param minute The minute to check
     * @param paramName The parameter name, used in error message
     * @throws IllegalArgumentException if the minute is invalid
     */
    public static void ensureValidMinute(int minute, String paramName) throws IllegalArgumentException {
        if (minute < 0 || minute > 24*60+1) {
            throw new IllegalArgumentException(paramName + " is not a valid minute (0-1441). Given value is " + minute);
        }
    }
}
