/*
 * TimerRunner
 *
 *   Copyright (c) 2018 Thierry Margenstern under MIT license
 *   http://opensource.org/licenses/MIT
 */
package tm.android.chronos.services;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import tm.android.chronos.R;
import tm.android.chronos.activity.Chronos;
import tm.android.chronos.activity.TimerActivity;
import tm.android.chronos.audio.AudioProperties;
import tm.android.chronos.audio.CommonMediaPlayer;
import tm.android.chronos.core.Clock;
import tm.android.chronos.core.ClockTimer;
import tm.android.chronos.core.Units;
import tm.android.chronos.preference.PreferenceCst;
import tm.android.chronos.sql.DbConstant;
import tm.android.chronos.sql.DbLiveObject;
import tm.android.chronos.util.Couple;
import tm.android.chronos.util.Permissions;

import java.util.List;

/**
 * This class do the task when a timer reached his end :
 * check the state of the app and decide if notification is needed or not
 * Launch (if possible) sound or vibrate, then if needed the notification
 */
@SuppressWarnings("ConstantConditions")
class TimerRunner {
    private boolean launchNotification = false;
    private boolean playSound = false;
    private Context context;
    private Intent intent;
    private AudioProperties audioProperties;

    TimerRunner(Context context) {
        this.context = context;
    }

    private void setActivityState() {
        //Log.i("Timer Service", "Entering setActivityState at " + ftime.format(System.currentTimeMillis()));
        int importance = 230;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);


        for (ActivityManager.RunningAppProcessInfo appProcessInfo : activityManager.getRunningAppProcesses())
            for (String pkg : appProcessInfo.pkgList)
                if (pkg.equals(context.getPackageName()))
                    importance = appProcessInfo.importance;

        // only this value ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND say that the application is visible on the foreground
        launchNotification = importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
    }

    private void createNotification(long duration) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "channel-01";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String channelName = "Chronos Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        int min = (int) (duration / 60000);
        if (Units.hasNoResources())
            Units.setResources(context.getResources());
        String text = Units.getLocalizedTextWithParams("timer_notification", min + "", (min > 1 ? "s" : ""));//"Timer of " + min + " minute" + (min > 1 ? "s" : "") + " is finished";
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(context.getResources().getString(R.string.app_name_timer))
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_ALARM);

        notificationManager.notify(0, mBuilder.build());
    }

    private void prepareSound() {
        playSound = false;
        if (Permissions.Instance().hasReadWriteExternalStorage(context)) {
            Couple<Boolean, Integer> result = Permissions.Instance().checkZenModeAccess(context);
            if (result.getValue() == AudioManager.RINGER_MODE_NORMAL ||
                    (result.getValue() != AudioManager.RINGER_MODE_NORMAL) && result.getKey()) {
                playSound = true;
            }
        }
        if (playSound) {
            audioProperties = new AudioProperties();
            audioProperties.loadFromPref(PreferenceCst.PREFIX_TIMER, context);
            CommonMediaPlayer.build(context);
        }
    }

    private void prepareIntent() {
        intent = new Intent(context, Chronos.class);
        intent.putExtra(Chronos.DIRECT_CALL, TimerActivity.class.getName());
    }

    void run() {
        Log.i(Chronos.name + "-TimerRunner", "Entering run at " + Chronos.ftime.format(System.currentTimeMillis()));
        //preferences = context.getSharedPreferences(PreferenceCst.PREF_STORE_NAME, Context.MODE_PRIVATE);

        DbLiveObject<Clock> dbLiveObject = new DbLiveObject<>(context);
        List<Clock> list = dbLiveObject.getRunningLiveObjects(DbConstant.RUNNING_TIMER_TABLE_NAME);
        ClockTimer clockTimer1 = (ClockTimer) list.get(0);
        dbLiveObject.close();

        long duration = clockTimer1.getDuration();
        Log.i(Chronos.name + "-TimerRunner", "Timer duration : " + duration);
        ClockTimer clockTimer = new ClockTimer();
        clockTimer.setDuration(duration);
        clockTimer.start(clockTimer1.getStartTime());

        setActivityState();
        prepareIntent();
        prepareSound();

        if (playSound && audioProperties.isPlaysound()) {
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
            Log.i(Chronos.name+"-TimerRunner","CommonMediaPlayer started");
        }

        if (audioProperties.isVibrate())
            CommonMediaPlayer.Instance().startVibrator(audioProperties.getVibrateDuration());


        if (launchNotification) {
            createNotification(duration);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        }

        while (clockTimer1.isRunning()){
            try {
                Thread.sleep(50);
            } catch (InterruptedException e){
            }
        }
        Log.i(Chronos.name + "-TimerRunner", "Finished at " + Chronos.ftime.format(System.currentTimeMillis()));
    }
}
