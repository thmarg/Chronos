/*
 * Alarm
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;


import tm.android.chronos.activity.Chronos;
import tm.android.chronos.core.Units.STATUS;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Calendar;

import static tm.android.chronos.core.Alarm.MODE.STOPWATCH;
import static tm.android.chronos.core.Alarm.MODE.TIMER;
import static tm.android.chronos.core.Units.STATUS.*;

/**
 * This class implement alarm behavior, so it "wait" until the stop time is reached.
 * But It beacome a stopwatch after this event to show the elapsed time since the stop time has been reached.
 * So prior the start method, you must absolutely call endTime to define endTime.
 */
public class Alarm implements Clock, Serializable, WithId {
    private final static long DAY_IN_MS = 86400000;
    private long startTime;
    private long endTime; // end Datetime of the alarm
    private long firstEndTime = -1; // because endTime is changed when the alarm is repeated, and we need the original planned entime for display
    private long endTimeInDay; // needed for repeating alarm over days of week.
    private Digit time;
    private String name;
    private STATUS status;


    public enum MODE {TIMER, STOPWATCH}

    private MODE mode;
    private AlarmData alarmData;
    private int repeatCount;
    transient private long id;
    transient private boolean planned; // checked when parsing alarm list from db, then set here for display purpose


    public Alarm() {
        mode = TIMER;
        status = WAIT_TO_START;
        endTime = 0L;
        startTime = 0L;
        alarmData = new AlarmData();
        id = System.currentTimeMillis() / 1000; // time in seconds and because a job id is an integer and not long when submitted into the JobScheduler.
        // Doing so is good up to Tue Jan 19 04:14:07 CET 2038
    }

    @Override
    public void start(long startTime) {
        long delta = endTime - startTime;
        if (status == WAIT_TO_START && delta > 0) {
            this.startTime = startTime;
            time = Digit.split(delta);
            status = RUNNING;
        } else if (delta < 0) {
            mode = STOPWATCH;
        }

    }

    @Override
    public void stopTime(long stopTime) {
        if (mode == TIMER) {
            if (stopTime < this.endTime) status = STOPPED;
        } else {
            if (stopTime > this.endTime) status = STOPPED;
        }
    }


    @Override
    public Digit getTime() {
        long now = System.currentTimeMillis();
        long delta = endTime - now;
        if (mode == TIMER && status == RUNNING) {
            if (delta > 0) {
                time = Digit.split(delta);
            } else {
                mode = STOPWATCH;
                time = Digit.split(-delta);
            }
        } else if (mode == STOPWATCH) { // stopwatch
            if (status == RUNNING)
                time = Digit.split(-delta);
            else time = Digit.ZERRO;
        }
        return time;

    }

    @Override
    public void reset() {
        endTime = 0;
        startTime = 0;
        time.reset();
        status = WAIT_TO_START;

    }

    @Override
    public boolean isRunning() {
        getTime();
        return status == RUNNING;
    }

    @Override
    public boolean isStopped() {
        getTime();
        return status == STOPPED;
    }

    @Override
    public boolean isWaitingStart() {
        getTime();
        return status == WAIT_TO_START;
    }

    public boolean isPassed() {
        if (getAlarmData().getType() == AlarmData.ALARM_TYPE.ONCE)
            return endTime < System.currentTimeMillis();
        else
            return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setFirstEndTime(long firstEndTime) {
        this.firstEndTime = firstEndTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getFirstEndTime() {
        return firstEndTime;
    }

    public void restart() {
        if (mode == TIMER && status == STOPPED)
            status = RUNNING;
        else if (mode == STOPWATCH && System.currentTimeMillis() < endTime) {
            mode = TIMER;
            status = RUNNING;
        }
    }

    public MODE getMode() {
        getTime();
        return mode;
    }

    public AlarmData getAlarmData() {
        return alarmData;
    }


    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    public void setEndTimeInDay(long endTimeInDay) {
        this.endTimeInDay = endTimeInDay;
    }

    public void updateEndTimeForRepeatedLoop() {
        endTime = getNextAlarmDateTime();
    }

    public void updateEndTimeForRepeated() {
        endTime = alarmData.getNextSpecDay(System.currentTimeMillis());
        if (endTime == -1) {
            endTime = 0;
            status = STOPPED;
        }
    }

    public boolean hasBeenRepeated() {
        return repeatCount > 0;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void addRepeatCount() {
        repeatCount++;
    }

    public boolean isPlanned() {
        return planned;
    }

    public void setPlanned(boolean planned) {
        this.planned = planned;
    }

    /**
     * WARNING : Call this absolutely before calling setEndTime again.
     */
    public void resetRepeatCount() {
        repeatCount = 0;
        firstEndTime = -1;
    }

    private long getNextAlarmDateTime() {
        if (alarmData.getType() == AlarmData.ALARM_TYPE.ONCE)
            return 0;

        // enTimeInDay is already set for an alarm type REPEATED_LOOP (unique value)
        // but for alarm type REPEATED_LOOP_SPEC_TIME, each day, time can be different.
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int next = alarmData.getDaysOfWeek().getNextDay(day);
        long date;
        long time;
        try {
            date = Chronos.fdate.parse(Chronos.fdate.format(System.currentTimeMillis())).getTime();
        } catch (ParseException e) {
            date = 0;
        }
        if (next == day) {
            time = Digit.getTimeFromString(Chronos.ftime.format(System.currentTimeMillis()));
            if (alarmData.getType() == AlarmData.ALARM_TYPE.REPEATED_LOOP_SPEC_TIME)
                endTimeInDay = alarmData.getDaysOfWeek().getTime(day);
            if (endTimeInDay > time) {
                return date + endTimeInDay;
            } else {
                next = day + 1;
                if (next > 7) next = 1;
                next = alarmData.getDaysOfWeek().getNextDay(next);
            }

        }
        int delta = (next > day ? next - day : 7 - day + next);
        if (alarmData.getType() == AlarmData.ALARM_TYPE.REPEATED_LOOP_SPEC_TIME)
            endTimeInDay = alarmData.getDaysOfWeek().getTime(next);
        return date + endTimeInDay + delta * DAY_IN_MS;

    }
}
