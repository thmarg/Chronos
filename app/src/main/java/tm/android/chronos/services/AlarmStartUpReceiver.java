package tm.android.chronos.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import tm.android.chronos.activity.Chronos;
import tm.android.chronos.core.Alarm;
import tm.android.chronos.core.AlarmData;
import tm.android.chronos.sql.DbConstant;
import tm.android.chronos.sql.DbLiveObject;

import java.util.List;

/**
 * Broadcast receiver called by the system when the user unlock the device after start up
 * Re-planned alarm as needed.
 */
public class AlarmStartUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (!intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.w(Chronos.name+"-AlarmStartUpRec", "!!! Some one try to call me with " + intent.getAction()+". This is forbidden, bye");
            return;
        }
        Log.i(Chronos.name+"-AlarmStartUpRec", "onReceive " + intent.getAction());
        DbLiveObject<Alarm> dbLiveObject = new DbLiveObject<>(context);
        List<Alarm> lst = dbLiveObject.getRunningLiveObjectsWithId(DbConstant.RUNNING_ALARMS_TABLE_NAME);
        if (!lst.isEmpty()) {
            for (Alarm alarm : lst) {
                Log.i(Chronos.name+"-AlarmStartUpRec", "Checking Alarm Id : "+alarm.getId());
                if (alarm.isRunning()) {
                    if (alarm.isPassed() && alarm.getAlarmData().getType() ==AlarmData.ALARM_TYPE.REPEATED_LOOP)
                        alarm.updateEndTimeForRepeatedLoop();

                    if (!alarm.isPassed()) {
                        AlarmServices.updateAlarm(context, alarm);
                        Log.i(Chronos.name+"-AlarmStartUpRec", "Alarm planned, TriggerTime : "+alarm.getEndTime());
                    }
                }
            }
        }
        dbLiveObject.close();
    }
}
