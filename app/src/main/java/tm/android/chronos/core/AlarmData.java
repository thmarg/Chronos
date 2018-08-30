package tm.android.chronos.core;

import tm.android.chronos.audio.AudioProperties;
import tm.android.chronos.util.Couple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static tm.android.chronos.core.AlarmData.ALARM_TYPE.ONCE;

public class AlarmData implements Serializable {
    public enum ALARM_TYPE {ONCE, REPEATED, REPEATED_LOOP, REPEATED_LOOP_SPEC_TIME}

    private String description;
    private ALARM_TYPE type = ONCE;
    private AudioProperties audioProperties;
    private DaysOfWeek daysOfWeek;
    private List<Reminder> reminders;
    private SortedMap<Long, AudioProperties> repeatedSpecDays;

    public AlarmData() {
        reminders = new ArrayList<>(5);
        daysOfWeek = new DaysOfWeek();
        repeatedSpecDays = new TreeMap<>();
    }

    public AlarmData(ALARM_TYPE type) {
        this.type = type;
        reminders = new ArrayList<>(5);
        daysOfWeek = new DaysOfWeek();
        repeatedSpecDays = new TreeMap<>();
    }

    public void addReminder(Reminder reminder) {
        if (!reminders.contains(reminder))
            reminders.add(reminder);
    }

    public Reminder getNextReminder() {
        long next = Long.MAX_VALUE;
        Reminder result = null;
        for (Reminder reminder : reminders)
            if (reminder.getDate() < next) {
                next = reminder.getDate();
                result = reminder;
            }
        return result;
    }

    public String getDescription() {
        return description;
    }

    public ALARM_TYPE getType() {
        return type;
    }

//    private List<Reminder> getReminders() {
//        return reminders;
//    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(ALARM_TYPE type) {
        this.type = type;
    }

    /**
     * Return AudioProperties depending on alarm type
     *
     * @param datetime, unused for type ONCE, day for type REPEATED_LOOP, and planned endTime for type REPEATED
     * @return AudioProperties
     */
    public AudioProperties getAudioProperties(long datetime) {
        switch (type) {
            case ONCE:
                return audioProperties;
            case REPEATED_LOOP:
            case REPEATED_LOOP_SPEC_TIME:
                return getDaysOfWeek().getAudio((int) datetime);
            case REPEATED:
                return getRepeatedSpecDays().get(datetime);
        }
        return null;
    }

    public void setAudioProperties(AudioProperties audioProperties) {
        this.audioProperties = audioProperties;
    }

    public DaysOfWeek getDaysOfWeek() {
        return daysOfWeek;
    }

    public SortedMap<Long, AudioProperties> getRepeatedSpecDays() {
        return repeatedSpecDays;
    }

    long getNextSpecDay(long now) {
        for (long dateTime : repeatedSpecDays.keySet())
            if (dateTime > now)
                return dateTime;
        return -1;
    }

    public static List<Couple<ALARM_TYPE, String>> getAlarmTypeForSpinner() {
        List<Couple<ALARM_TYPE, String>> lst = new ArrayList<>(3);
        lst.add(new Couple<>(AlarmData.ALARM_TYPE.ONCE, Units.getLocalizedText(ALARM_TYPE.ONCE.name())));
        lst.add(new Couple<>(AlarmData.ALARM_TYPE.REPEATED_LOOP, Units.getLocalizedText(ALARM_TYPE.REPEATED_LOOP.name())));
        lst.add(new Couple<>(ALARM_TYPE.REPEATED_LOOP_SPEC_TIME, Units.getLocalizedText(ALARM_TYPE.REPEATED_LOOP_SPEC_TIME.name())));
        lst.add(new Couple<>(AlarmData.ALARM_TYPE.REPEATED, Units.getLocalizedText(ALARM_TYPE.REPEATED.name())));
        return lst;
    }

    public static int getTypePosition(ALARM_TYPE type) {
        switch (type) {
            case ONCE:
                return 0;
            case REPEATED_LOOP:
                return 1;
            case REPEATED_LOOP_SPEC_TIME:
                return 2;
            case REPEATED:
                return 3;
        }
        return -1;
    }


}
