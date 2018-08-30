package tm.android.chronos.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import tm.android.chronos.activity.Chronos;
import tm.android.chronos.core.Alarm;

import java.util.Date;

/**
 * Not a service in the meaning of android services but
 * a class with static method to operate with Alarms
 * Alarm are identified by there Id.
 * In AlarmManager both the trigger time and the pending intent are use to identify an alarm
 * Pending Intent receive a data "alarmId@value" that is part of identify an intent
 */
public class AlarmServices {
private static AlarmManager alarmManager;


    private static void setAlarmManager(Context context){
        if (alarmManager == null)
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }
    /**
     * Remove an alarm from AlarmManager
     *
     * @param context {@link Context}
     * @param alarmId {@link Long}
     */
    public static void removeFromAlarmManager(Context context, long alarmId) {
        setAlarmManager(context);
        PendingIntent pendingIntent = getPendingIntent(context, alarmId);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

    }

    /**
     * Define an {@link android.app.AlarmManager.AlarmClockInfo} and set it into AlarmManager.
     * There is only a trigger to define, it is a RTC_WAKE_UP alarm style.
     *
     * @param context {@link Context}
     * @param alarm   {@link Alarm}
     */
    public static void registerIntoAlarmManager(Context context, Alarm alarm) {
        setAlarmManager(context);
        PendingIntent pendingIntent = getPendingIntent(context, alarm.getId());
        AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(alarm.getEndTime(), pendingIntent);
        alarmManager.setAlarmClock(info, pendingIntent);
        Log.i(Chronos.name + "-AlarmServices", "register alarm " + alarm.getId() + ", EndTime: " + new Date(alarm.getEndTime()).toString());
    }

    /**
     * Return always the same (in matter of equality , not in matter of reference) pending intent for an alarmId.
     *
     * @param context {@link Context}
     * @param alarmId {@link Long}
     * @return PendingIntent
     */
    private static PendingIntent getPendingIntent(Context context, long alarmId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setData(Uri.parse("alarmId@" + String.valueOf(alarmId)));
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    /**
     * Method to 'replan' an alarm,
     * We do like this because there is no methode to query alarms set into {@link AlarmManager}
     * Looping over the method getNextAlarmClock hang the system !
     * @param context {@link Context}
     * @param alarm {@link Alarm}
     */
    public static void updateAlarm(Context context, Alarm alarm) {
        setAlarmManager(context);
        PendingIntent pendingIntent = getPendingIntent(context, alarm.getId());
        alarmManager.cancel(pendingIntent);
        AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(alarm.getEndTime(), pendingIntent);
        alarmManager.setAlarmClock(info, pendingIntent);

    }

}
