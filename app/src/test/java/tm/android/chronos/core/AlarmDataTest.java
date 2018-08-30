package tm.android.chronos.core;

import org.junit.Test;
import tm.android.chronos.audio.AudioProperties;

import static org.junit.Assert.assertEquals;

public class AlarmDataTest {

    @Test
    public void jobId(){
        AlarmData alarmData = new AlarmData();
//        alarmData.setJobId(5421464901L);
//        assertEquals(5421464901L, alarmData.getJobId());
    }

    @Test
    public void type(){
        AlarmData alarmData = new AlarmData();
        for (AlarmData.ALARM_TYPE type : AlarmData.ALARM_TYPE.values()) {
            alarmData.setType(type);
            assertEquals(type, alarmData.getType());
            AlarmData alarmData1 = new AlarmData(type);
            assertEquals(type,alarmData1.getType());
        }
    }


    @Test
    public void addReminder() {
        long date = 1254222111L;
        Reminder reminder = new Reminder(date,new AudioProperties("a","b",5,5,30000,1,true,true,false));
        AlarmData alarmData = new AlarmData(AlarmData.ALARM_TYPE.ONCE);
        alarmData.addReminder(reminder);
//        assertFalse(alarmData.getReminders().isEmpty());
//        assertTrue(alarmData.getReminders().contains(reminder));
        Reminder reminder2 = new Reminder(date+100000,new AudioProperties("a","b",5,5,30000,1,true,true,false));
        alarmData.addReminder(reminder2);
//        assertEquals(alarmData.getReminders().size(),2);
//        assertTrue(alarmData.getReminders().contains(reminder2));

    }

    @Test
    public void getNextReminder() {
        long date = 1254222111L;
        AlarmData alarmData = new AlarmData(AlarmData.ALARM_TYPE.ONCE);
        Reminder reminder1 = new Reminder(date,new AudioProperties("a","b",5,5,30000,1,true,true,false));
        Reminder reminder2 = new Reminder(date+100000,new AudioProperties("a","b",5,5,30000,1,true,true,false));
        Reminder reminder3 = new Reminder(date-200000,new AudioProperties("a","b",5,5,30000,1,true,true,false));
        alarmData.addReminder(reminder1);
        alarmData.addReminder(reminder2);
        alarmData.addReminder(reminder3);
        assertEquals(alarmData.getNextReminder(),reminder3);
    }


    @Test
    public void getReminders() {
    }
}