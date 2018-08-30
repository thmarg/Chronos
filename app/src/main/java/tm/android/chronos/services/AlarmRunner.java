package tm.android.chronos.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import tm.android.chronos.activity.AlarmResultActivity;
import tm.android.chronos.activity.Chronos;
import tm.android.chronos.audio.AudioProperties;
import tm.android.chronos.audio.CommonMediaPlayer;
import tm.android.chronos.core.Alarm;
import tm.android.chronos.core.AlarmData;
import tm.android.chronos.preference.PreferenceCst;
import tm.android.chronos.sql.DbConstant;
import tm.android.chronos.sql.DbLiveObject;

import java.util.Calendar;

public class AlarmRunner {
    private Context context;
    private long alarmId;

    public AlarmRunner(Context context, long alarmId) {
        this.context = context;
        this.alarmId = alarmId;
    }


    public void run() {
        Log.i(Chronos.name+"-AlarmRunner","onStart start");
        CommonMediaPlayer.build(context);
        DbLiveObject<Alarm> dbLiveObject = new DbLiveObject<>(context);
        Alarm alarm = dbLiveObject.getRunningLiveObjectById(DbConstant.RUNNING_ALARMS_TABLE_NAME, alarmId);
        dbLiveObject.close();
        if (alarm == null) {
            alarm = new Alarm();
            alarm.setName("Sorry but Alarm With error");
            alarm.getAlarmData().setDescription("Alarm with id " + alarmId + " not found in database !");
            AudioProperties audioProperties = new AudioProperties();
            audioProperties.loadFromPref(PreferenceCst.PREFIX_ALARM,context);
            alarm.getAlarmData().setAudioProperties(audioProperties);
            alarm.getAlarmData().setType(AlarmData.ALARM_TYPE.ONCE);
        }

        long dateTime = alarm.getAlarmData().getType() == AlarmData.ALARM_TYPE.ONCE ?
                -1L : alarm.getAlarmData().getType() == AlarmData.ALARM_TYPE.REPEATED ? alarm.getFirstEndTime() : Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        AudioProperties audioProperties = alarm.getAlarmData().getAudioProperties(dateTime);
        if (audioProperties.isPlaysound()) {
            if (audioProperties.isVolumeVariable()) {
                CommonMediaPlayer.Instance().
                        setVariableVolumeAndStart(
                                audioProperties.getMinVolumeVariable(),
                                audioProperties.getMaxVolumeVariable(),
                                audioProperties.getVolumeVariableDuration(),
                                audioProperties.getVolumeVariableStep());
            } else {
                CommonMediaPlayer.Instance().setFixedVolumeLevel(audioProperties.getLevelVolumeFixe());
            }

            CommonMediaPlayer.Instance().
                    setVariableDurationAndStart(
                            audioProperties.getDataSource(),
                            audioProperties.getSoundDuration(),
                            audioProperties.getSoundRepeatCount());
            Log.i(Chronos.name+"-AlarmRunner","CommonMediaPlayer started");
        }

        if (audioProperties.isVibrate())
            CommonMediaPlayer.Instance().startVibrator(audioProperties.getVibrateDuration());
        Log.i(Chronos.name+"-AlarmRunner","onStart done");

        Intent intent = new Intent(context, Chronos.class);
        intent.putExtra(Chronos.DIRECT_CALL, AlarmResultActivity.class.getName());
        intent.putExtra("AlarmId", alarmId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        context.startActivity(intent);
    }


}
