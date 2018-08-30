/*
 * ${NAME}
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;

import tm.android.chronos.uicomponent.CalendarUI;
import tm.android.chronos.util.DateTime;


/**
 * Created by thmarg on 10/02/15.
 */
public class CalendarImpl  {


    private DateTime toDay;
    private DateTime dayToShow;


    private CalendarUI calendarUI;

    public CalendarImpl() {
        toDay = new DateTime();
        dayToShow = new DateTime();

        calendarUI = new CalendarUI();

    }



    public DateTime getDayToShow() {
        return dayToShow;
    }

    public DateTime getToDay() {
        return toDay;
    }

    public DateTime.Day[] getDaysToDisplay(DateTime selected) {
        return dayToShow.getDaysOftodayToShowInCalendar(toDay, selected);
    }

    public boolean isSameMonthYear() {
        return (toDay.get(DateTime.YEAR) == dayToShow.get(DateTime.YEAR) && toDay.get(DateTime.MONTH) == dayToShow.get(DateTime.MONTH));

    }

    public void addMonth() {
        dayToShow.addMonth();

    }

    public void subMonth() {
        if (dayToShow.get(DateTime.YEAR) == 1970 && dayToShow.get(DateTime.MONTH) == 1)
            return;
        dayToShow.subMonth();

    }

    public void addYear() {
        dayToShow.addYear();

    }

    public void subYear() {
        if (dayToShow.get(DateTime.YEAR) == 1970)
            return;

        dayToShow.subYear();

    }

}
