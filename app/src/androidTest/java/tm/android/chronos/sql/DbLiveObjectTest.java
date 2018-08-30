package tm.android.chronos.sql;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tm.android.chronos.audio.AudioProperties;
import tm.android.chronos.core.Alarm;
import tm.android.chronos.core.AlarmData;
import tm.android.chronos.core.Reminder;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DbLiveObjectTest {
    Context appContext;
    long now ;
    long id;
    String alarmName;
    String alarmDesc;
    long endTime;
    long reminderTime;
    AlarmData.ALARM_TYPE alarmType ;
    @Before
    public void setUp() throws Exception {
        appContext = InstrumentationRegistry.getTargetContext();
        now = System.currentTimeMillis();
        id = 987654321001L;
        alarmName = "Alarm Test";
        alarmDesc = "Manger chez Chantal";
        endTime = now+5*3600000;
        reminderTime = endTime-5*60000;
        alarmType = AlarmData.ALARM_TYPE.ONCE;
    }

    @Test
    public void useAppContext() throws Exception {
        assertEquals("tm.android.chronos", appContext.getPackageName());
    }

    @After
    public void tearDown() throws Exception {
        DbBase.closeDb();
    }

    @Test
    public void storeLiveObjectWithId() {

        Alarm alarm = new Alarm();
        id = alarm.getId();
        alarm.setName(alarmName);
        alarm.setEndTime(endTime);
        alarm.getAlarmData().setType(AlarmData.ALARM_TYPE.ONCE);
        alarm.getAlarmData().setDescription(alarmDesc);
        alarm.start(now);
        AudioProperties audioProperties = new AudioProperties("../resources/Celebration Of The Lizard - A Little Game.mp3",
                "",1,5,30000,0,false,true,false);
        alarm.getAlarmData().addReminder(new Reminder(reminderTime,audioProperties));
        DbLiveObject<Alarm> dbLiveObject= new DbLiveObject<>(appContext);
        dbLiveObject.clearTable(DbConstant.RUNNING_ALARMS_TABLE_NAME);
       // dbLiveObject.storeLiveObjectWithId(alarm,DbConstant.RUNNING_ALARMS_TABLE_NAME);
        dbLiveObject.close();
        assertFalse(dbLiveObject.hasError());

    }

    @Test
    public void getRunningLiveObjectById() {
        storeLiveObjectWithId();
        DbLiveObject<Alarm> dbLiveObject = new DbLiveObject<>(appContext);
        Alarm alarm = dbLiveObject.getRunningLiveObjectById(DbConstant.RUNNING_ALARMS_TABLE_NAME,id);
        dbLiveObject.close();

        assertFalse(dbLiveObject.hasError());
        assertEquals(alarm.getName(),alarmName);
        assertEquals(alarm.getAlarmData().getDescription(),alarmDesc);
        assertEquals(alarm.getAlarmData().getType(), alarmType);
        assertEquals(alarm.getAlarmData().getNextReminder().getDate(), reminderTime);
        assertTrue(alarm.isRunning());
        assertEquals(alarm.getMode(),Alarm.MODE.TIMER);

    }

    @Test
    public void deleteFromTableById() {
        Alarm alarm = new Alarm();
        id  = alarm.getId();
        alarm.setName(alarmName);
        alarm.setEndTime(endTime);
        alarm.getAlarmData().setType(AlarmData.ALARM_TYPE.ONCE);
        alarm.getAlarmData().setDescription(alarmDesc);
        alarm.start(now);
        AudioProperties audioProperties = new AudioProperties("../resources/Celebration Of The Lizard - A Little Game.mp3",
                "",1,5,30000,0,false,true,false);
        alarm.getAlarmData().addReminder(new Reminder(reminderTime,audioProperties));

        DbLiveObject<Alarm> dbLiveObject= new DbLiveObject<>(appContext);

        dbLiveObject.clearTable(DbConstant.RUNNING_ALARMS_TABLE_NAME);
       // dbLiveObject.storeLiveObjectWithId(alarm,DbConstant.RUNNING_ALARMS_TABLE_NAME);
        dbLiveObject.deleteFromTableById(DbConstant.RUNNING_ALARMS_TABLE_NAME,id);
        Alarm alarm2 = dbLiveObject.getRunningLiveObjectById(DbConstant.RUNNING_ALARMS_TABLE_NAME,id);
        dbLiveObject.close();
        assertEquals(alarm2,null);

    }

    @Test
    public void getRunningLiveObjectsWithId() {
        Alarm alarm = new Alarm();
        alarm.setName(alarmName);
        alarm.setEndTime(endTime);
        alarm.getAlarmData().setType(AlarmData.ALARM_TYPE.ONCE);
        alarm.getAlarmData().setDescription(alarmDesc);
        alarm.start(now);
        AudioProperties audioProperties = new AudioProperties("../resources/Celebration Of The Lizard - A Little Game.mp3",
                "",1,5,30000,0,false,true,false);
        alarm.getAlarmData().addReminder(new Reminder(reminderTime,audioProperties));

        DbLiveObject<Alarm> dbLiveObject= new DbLiveObject<>(appContext);

        dbLiveObject.clearTable(DbConstant.RUNNING_ALARMS_TABLE_NAME);
        //dbLiveObject.storeLiveObjectWithId(alarm,DbConstant.RUNNING_ALARMS_TABLE_NAME);

        Alarm alarm2 = new Alarm();
        alarm2.setName(alarmName+alarmName);
        alarm2.setEndTime(endTime+1);
        alarm2.getAlarmData().setType(AlarmData.ALARM_TYPE.ONCE);
        alarm2.getAlarmData().setDescription(alarmDesc+alarmDesc);
        alarm2.start(now);
        AudioProperties audioProperties2 = new AudioProperties("../resources/Celebration Of The Lizard - A Little Game.mp3",
                "",1,6,9000,1,false,true,false);
        alarm2.getAlarmData().addReminder(new Reminder(reminderTime,audioProperties2));
        //dbLiveObject.storeLiveObjectWithId(alarm2,DbConstant.RUNNING_ALARMS_TABLE_NAME);

        List<Alarm> alarmList = dbLiveObject.getRunningLiveObjectsWithId(DbConstant.RUNNING_ALARMS_TABLE_NAME);
        assertNotNull(alarmList);
        assertFalse(alarmList.isEmpty());
        assertEquals(alarmList.size(),2);
        assertEquals(alarm.getId(),alarmList.get(0).getId());
        assertEquals(alarm2.getId(),alarmList.get(1).getId());
        dbLiveObject.close();

    }
}