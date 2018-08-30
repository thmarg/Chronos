package tm.android.chronos.core;

import android.annotation.SuppressLint;
import tm.android.chronos.audio.AudioProperties;

import java.io.Serializable;
import java.util.*;

/**
 * Day in week goes from 1 to 7 starting sunday
 */
public class DaysOfWeek implements Serializable {
    private HashMap<Integer,Boolean> days;
    private HashMap<Integer,AudioProperties> audioDays;
    private final static  List<Integer> weekDays;
    private HashMap<Integer,Long> timesDays; // for Type REPEATED_LOOP_SPEC_TIME

    static {
        weekDays = Collections.unmodifiableList(Arrays.asList(2,3,4,5,6,7,1));
    }
    public enum DAYS {
        MONDAY(2),TUESDAY(3), WEDNESDAY(4), THURSDAY(5),  FRIDAY(6), SATURDAY(7), SUNDAY(1);
        private int num;
        DAYS(int num) {
            this.num = num;
        }

        public int getNum() {
            return num;
        }
    }

    @SuppressLint("UseSparseArrays")
    DaysOfWeek(){
        days =  new HashMap<>(7);
        audioDays = new HashMap<>(7);
        timesDays = new HashMap<>(7);
        for (int i : weekDays) {
            days.put(i, false);
            audioDays.put(i,null);
            timesDays.put(i,-1L);
        }
    }

    public void setDay(int numDayInWeek, boolean enable){
        if (numDayInWeek>0 && numDayInWeek<8)
            days.put(numDayInWeek,enable);
    }

    public boolean isDayEnable(int numDayInWeek){
        if (numDayInWeek>0 && numDayInWeek<8)
        return days.get(numDayInWeek);
        else return false;
    }

    public void setAudio(int day, AudioProperties audioProperties){
        if (days.get(day))
            audioDays.put(day,audioProperties);
    }

    public void setTime(int day, long time){
        timesDays.put(day,time);
    }
    public long getTime(int day){
        return timesDays.get(day);
    }
    public AudioProperties getAudio(int day){
        if (days.get(day))
            return audioDays.get(day);
        else return null;
    }

    /**
     * Next enabled day including param day
     * @param day : int from 1 to 7, not checked.
     * @return  day if day is enable, else the next available (rotating if needed) else 0
     */
    int getNextDay(int day){
        if (getSelectedDays().isEmpty()) return 0;

        if (isDayEnable(day)) return day;

        for (int j : getSelectedDays())
            if (j > day) return j;

        return getSelectedDays().get(0);

    }

    /**
     * Return selected days in order  of the week (
     * @return List<Integer>
     */
    public List<Integer> getSelectedDays(){
        List<Integer> ret = new ArrayList<>(5);
        for (int i : weekDays)
            if (days.get(i))
                ret.add(i);
        return ret;
    }

    /**
     * Return a string containing the short name of the selected days of the week, separated by a comma.
     * @return String
     */
    public String getSelectedDaysAsString() {
        String ret ="";
        for (int i : getSelectedDays())
            ret += Units.getLocalizedText("day" +i)+", ";
        return ret.substring(0,ret.lastIndexOf(","));
    }

    /**
     * Return true is there is more than one selected days.
     * @return boolean
     */
    public boolean hasModeThanOneDay(){
        return getSelectedDays().size() > 1;
    }

    /**
     * Return the list of the days of the week from monday to sunday
     * @return List<Integer>
     */
    public static List<Integer> getWeekDays(){
        return weekDays;
    }
}
