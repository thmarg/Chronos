/*
 * DateTime
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.util;

import java.text.DateFormatSymbols;
import java.util.Locale;
import java.util.TimeZone;


/**
 * DateTime
 */
public class DateTime {

    public final static int YEAR = 1;
    public final static int MONTH = 2;
    public final static int HOURS = 4;
    public final static int MINUTES = 5;
    public final static int SECONDS = 6;
    public final static int MILLISECONDS = 7;
    public final static int DAY_OF_WEEK = 8;
    public final static int DAY_OF_YEAR = 9;
    public final static int DAY_OF_MONTH = 10;
    public final static int WEEK_OF_YEAR = 11;
    public final static int FIRSTDAY_OFTHEYEAR_INWEEK = 12;
    public final static int FIRST_DAY_OF_THE_MONTH_INWEEK = 13;

    private final static long DAY_IN_MILLISECONDS = 86400000;
    private final static int[] LAST_DAY_OF_MONTH = new int[]{0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private final static String[] DAYS_NAME = new String[]{"", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};

    private int year;
    private int dayInYear;
    private int monthInYear;
    private int dayInMonth;
    private int dayInWeek;
    private int firstDayOfYearInWeek;
    private int firstDayOfMonthInweek;
    private int lastDayOfMonthInWeek;
    private int weekInYear;

    private long now;
    private long days;
    private long hours;
    private long minutes;
    private long seconds;
    private long milliSeconds;


    public DateTime() {
        int offset = TimeZone.getDefault().getRawOffset();
        addMillisSeconds(System.currentTimeMillis() + TimeZone.getDefault().getRawOffset());
    }

    public DateTime(int offSetHourFromUTC) {
        offSetHourFromUTC = offSetHourFromUTC % 12;
        addMillisSeconds(System.currentTimeMillis() + offSetHourFromUTC * TimeZone.getDefault().getRawOffset());
    }

    public DateTime(long ms, boolean utc) {
        addMillisSeconds(ms + (utc ? TimeZone.getDefault().getRawOffset() : 0));
    }


    public void addMillisSeconds(long ms) {
        // assert (ms>=0);
        if (ms < 0) {
            subMilliseconds(-ms);
            return;
        }
        now += ms;
        milliSeconds += ms;
        if (milliSeconds < 1000)
            return;


        addSeconds(milliSeconds / 1000);
        milliSeconds = milliSeconds % 1000;
        computeDate();
    }

    private void addSeconds(long ss) {
        seconds += ss;
        if (seconds < 60)
            return;


        addMinutes(seconds / 60);
        seconds = seconds % 60;
    }

    private void addMinutes(long mm) {
        minutes += mm;
        if (minutes < 60)
            return;

        addHours(minutes / 60);
        minutes = minutes % 60;
    }

    private void addHours(long hh) {
        hours += hh;
        if (hours < 24)
            return;

        addDays(hours / 24);
        hours = hours % 24;
    }

    private void addDays(long dd) {
        days += dd;
    }

    public void subMilliseconds(long ms) {
        if (ms < 0) {
            addMillisSeconds(-ms);
            return;
        }
        now -= ms;
        milliSeconds -= ms;
        if (milliSeconds >= 0)
            return;
        //

        subSeconds((milliSeconds / 1000) - (milliSeconds % 1000 == 0 ? 0 : 1));
        milliSeconds = (milliSeconds % 1000 == 0 ? 0 : milliSeconds % 1000 + 1000);
    }


    private void subSeconds(long ss) {
        seconds += ss;// ss < 0!
        if (seconds >= 0)
            return;
        //

        subMinutes((seconds / 60) - (seconds % 60 == 0 ? 0 : 1));
        seconds = (seconds % 60 == 0 ? 0 : seconds % 60 + 60);
        computeDate();
    }

    private void subMinutes(long mm) {
        minutes += mm;// mm < 0 !
        if (minutes >= 0)
            return;
        //

        subHours((minutes / 60) - (minutes % 60 == 0 ? 0 : 1));
        minutes = (minutes % 60 == 0 ? 0 : minutes % 60 + 60);
    }

    private void subHours(long hh) {
        hours += hh; // hh < 0 !
        if (hours >= 0)
            return;
        //

        subDays((hours / 24) - (hours % 24 == 0 ? 0 : 1));
        hours = (hours % 24 == 0 ? 0 : hours % 24 + 24);
    }

    private void subDays(long dd) {
        days += dd; // dd < 0 !
    }


    private void computeDate() {
        if (days == 0) {
            year = 0;
            dayInYear = 0;
        } else {
            dayInYear = (int) days + 1;
            year = 1970;
            firstDayOfYearInWeek = 4;
            int sub;
            while (dayInYear > 365) {
                if (isBissextile(year))
                    sub = 366;
                else
                    sub = 365;

                dayInYear -= sub;
                year++;
                firstDayOfYearInWeek = (firstDayOfYearInWeek + (sub - 364)) % 7;
                if (firstDayOfYearInWeek == 0)
                    firstDayOfYearInWeek = 7;
            }

            LAST_DAY_OF_MONTH[2] = (isBissextile(year) ? 29 : 28);
            dayInMonth = dayInYear;
            monthInYear = 1;
            dayInWeek = firstDayOfYearInWeek;
            weekInYear = 0;
            sub = LAST_DAY_OF_MONTH[monthInYear];
            while (dayInMonth > sub) {
                dayInMonth -= sub;
                monthInYear++;
                weekInYear += 4;
                dayInWeek = (dayInWeek + sub) % 7;
                if (dayInWeek == 0)
                    dayInWeek = 7;
                sub = LAST_DAY_OF_MONTH[monthInYear];
            }
            firstDayOfMonthInweek = dayInWeek;

            dayInWeek = (dayInWeek + dayInMonth - 1) % 7;
            if (dayInWeek == 0)
                dayInWeek = 7;
            lastDayOfMonthInWeek = (dayInWeek + LAST_DAY_OF_MONTH[monthInYear] - dayInMonth) % 7;
            if (lastDayOfMonthInWeek == 0)
                lastDayOfMonthInWeek = 7;

            weekInYear += dayInMonth / 7;
            if (firstDayOfYearInWeek != 1)
                weekInYear++;

            //System.out.println(DAYS_NAME[dayInWeek] + " " + dayInMonth + " " + monthInYear + " " + year);
        }
    }

    private boolean isBissextile(int year) {
        return ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0);
    }

    public int get(int filed) {
        switch (filed) {
            case DAY_OF_MONTH:
                return dayInMonth;
            case DAY_OF_WEEK:
                return dayInWeek;
            case DAY_OF_YEAR:
                return dayInYear;
            case YEAR:
                return year;
            case MONTH:
                return monthInYear;
            case HOURS:
                return (int) hours;
            case MINUTES:
                return (int) minutes;
            case SECONDS:
                return (int) seconds;
            case MILLISECONDS:
                return (int) milliSeconds;
            case FIRSTDAY_OFTHEYEAR_INWEEK:
                return firstDayOfYearInWeek;
            case WEEK_OF_YEAR:
                return weekInYear;
            case FIRST_DAY_OF_THE_MONTH_INWEEK:
                return firstDayOfMonthInweek;
        }
        return 0;
    }

    public void addYear() {
        long offset = (long) 365 * DAY_IN_MILLISECONDS;
        if (isBissextile(year))
            offset += DAY_IN_MILLISECONDS;
        addMillisSeconds(offset);
    }

    public void addMonth() {
        long offset = DAY_IN_MILLISECONDS * LAST_DAY_OF_MONTH[monthInYear];
        addMillisSeconds(offset);
    }

    public void addDays(int days) {
        long offset = (long) days * DAY_IN_MILLISECONDS;
        addMillisSeconds(offset);
    }

    private void addDay() {
        addMillisSeconds(DAY_IN_MILLISECONDS);
    }


    private void subDay() {
        subMilliseconds(DAY_IN_MILLISECONDS);
    }

    public void subDays(int days) {
        long offset = (long) days * DAY_IN_MILLISECONDS;
        subMilliseconds(offset);
    }

    public void subMonth() {
        long offSet = DAY_IN_MILLISECONDS * LAST_DAY_OF_MONTH[monthInYear == 1 ? 12 : monthInYear - 1];
        subMilliseconds(offSet);
    }

    public void subYear() {
        long offset = (long) 365 * DAY_IN_MILLISECONDS;
        if ((dayInYear > 40 && isBissextile(year)) || (dayInYear < 40 && isBissextile(year - 1)))
            offset += DAY_IN_MILLISECONDS;
        subMilliseconds(offset);
    }

    public int getLastDayFromPreviousMonth() {
        return LAST_DAY_OF_MONTH[(monthInYear - 1 == 0 ? 12 : monthInYear - 1)];
    }

    public Day[] getDaysOftodayToShowInCalendar(DateTime today, DateTime selected) {
        int lastDay = LAST_DAY_OF_MONTH[monthInYear];// last day of month (28,29,30,or 31)
        //
        int n = lastDay + firstDayOfMonthInweek - 1 + 7 - lastDayOfMonthInWeek;
        Day[] days = new Day[n];
        int lastDayPrec = getLastDayFromPreviousMonth();
        for (int i = 0; i < firstDayOfMonthInweek - 1; i++) {
            Day day1 = new Day();
            day1.dayNum = lastDayPrec--;
            days[firstDayOfMonthInweek - i - 2] = day1;
        }
        for (int i = 1; i <= lastDay; i++) {
            Day day1 = new Day();
            day1.dayNum = i;
            day1.inCurrentMonth = true;
            if (i == today.dayInMonth && year == today.year && monthInYear == today.monthInYear)
                day1.today = true;
            if (selected != null && i == selected.dayInMonth) {
                if (year == today.year && monthInYear == today.monthInYear && day1.getDayNum() < today.get(DateTime.DAY_OF_MONTH))
                    continue;
                day1.selected = true;
            }
            days[firstDayOfMonthInweek + i - 2] = day1;
        }
        for (int i = 1; i <= (7 - lastDayOfMonthInWeek); i++) {
            Day day1 = new Day();
            day1.dayNum = i;
            days[lastDay + firstDayOfMonthInweek - 2 + i] = day1;
        }
        return days;
    }


    public String getDisplayName(int field) {
        DateFormatSymbols dfs = new DateFormatSymbols(Locale.getDefault());
        if (field == DAY_OF_WEEK)
            return dfs.getWeekdays()[(dayInWeek == 7 ? 0 : dayInWeek - 1)];
        if (field == MONTH)
            return dfs.getMonths()[monthInYear - 1];
        return "";
    }

    public void setField(int field, int value) {
        switch (field) {
            case DAY_OF_MONTH:
                addMillisSeconds((value - dayInMonth) * DAY_IN_MILLISECONDS);
                break;
            case MONTH:
                monthInYear = value;
                break;
            case YEAR:
                year = value;
                break;
        }
    }

    public DateTime cloneIt() {
        return new DateTime(now, false);
    }

    @Override
    public String toString() {
        return "[" + year + "-" + monthInYear + "-" + dayInMonth + "] " + hours + ":" + (minutes < 10 ? 0 : "") + minutes + ":" + seconds + "  local time";
    }

    public class Day {

        int dayNum;
        boolean today = false;
        boolean inCurrentMonth = false;
        boolean selected = false;

        public boolean isInCurrentMonth() {
            return inCurrentMonth;
        }

        public int getDayNum() {
            return dayNum;
        }

        public boolean isToday() {
            return today;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }
}
