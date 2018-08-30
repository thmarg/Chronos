/*
 * TimerActivity
 *
 * Copyright (c) 2014-2018 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;
import tm.android.chronos.R;
import tm.android.chronos.audio.CommonMediaPlayer;
import tm.android.chronos.core.Clock;
import tm.android.chronos.core.ClockTimer;
import tm.android.chronos.core.ui.RendererWorker;
import tm.android.chronos.preference.AudioNotificationPreferenceFragment;
import tm.android.chronos.preference.PreferenceCst;
import tm.android.chronos.preference.PreferencesActivity;
import tm.android.chronos.services.TimerReceiver;
import tm.android.chronos.sql.DbConstant;
import tm.android.chronos.sql.DbLiveObject;
import tm.android.chronos.uicomponent.BaseUI;
import tm.android.chronos.uicomponent.TimerView;
import tm.android.chronos.uicomponent.event.TimerViewController;
import tm.android.chronos.util.Permissions;

import java.util.ArrayList;
import java.util.List;

import static tm.android.chronos.preference.PreferenceCst.PREF_KEYS.MUSIC_PATH;
import static tm.android.chronos.preference.PreferenceCst.PREF_KEYS.RINGTONE_URI;

/**
 * The activity of the Timer.
 */
public class TimerActivity extends AppCompatActivity {
    private final static String logname = Chronos.name + "-TimerActivity";
    private TimerView timerView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.timerlayout2);
        Toolbar toolbar = findViewById(R.id.chronos_toolbar);
        setSupportActionBar(toolbar);
        LinearLayout fond = findViewById(R.id.fond);
        timerView = new TimerView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(BaseUI.SCREENWIDTH, LinearLayout.LayoutParams.MATCH_PARENT);
        timerView.setLayoutParams(layoutParams);
        fond.addView(timerView);
        RendererWorker rendererWorker = new RendererWorker();
        rendererWorker.setInnerSleepTime(100);
        timerView.setRendererWorker(rendererWorker);
        rendererWorker.setMainGUI(timerView);
        rendererWorker.start();
        timerView.setOnTouchListener(new TimerViewController());
        Log.i(Chronos.name + "-TimerActivity", "TimerActivity onCreate @" + hashCode());
//        Log.i(Chronos.name + "-TimerActivity", "PID: " + Process.myPid());
//        Log.i(Chronos.name + "-TimerActivity", "TID: " + Process.myTid());
//        Log.i(Chronos.name + "-TimerActivity", "UID: " + Process.myUid());
//        Log.i(Chronos.name + "-TimerActivity", "UserHandler: " + Process.myUserHandle());

    }


    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(Chronos.name + "-TimerActivity", "Activity onRestart @" + hashCode());

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(Chronos.name + "-TimerActivity", "Timer Activity onResume @" + hashCode());

        DbLiveObject<Clock> dbLiveObject = new DbLiveObject<>(getBaseContext());
        List<Clock> list = dbLiveObject.getRunningLiveObjects(DbConstant.RUNNING_TIMER_TABLE_NAME);
        if (!list.isEmpty()) {
            //Log.i("AHHHHH", "Timer Activity onResume live timer list not empty @" + hashCode());
            ClockTimer clockTimer = (ClockTimer) list.get(0);
            //Log.i("AHHHHH", "Timer Activity onResume live timer 0: " + clockTimer.toString() + ", @" + hashCode());
            if (clockTimer.isStopped()) {
                timerView.timerEnd();
                Log.i(Chronos.name + "-TimerActivity", "Timer Activity timerView.timeEnd called @" + hashCode());
            }
        } else {
            Log.i(Chronos.name + "-TimerActivity", "Timer Activity onResume live timer list EMPTY !! @" + hashCode());
        }
        dbLiveObject.close();
    }

    @Override
    protected void onDestroy() {
        Log.i(Chronos.name + "-TimerActivity", "Activity onDestroy " + hashCode());
        super.onDestroy();
        // CommonMediaPlayer.Instance().releasePlayer();
        timerView.resetElapseTimeUI();

    }

    public void onClick(View view) {
        long now = System.currentTimeMillis();
        switch (view.getId()) {
            case R.id.btn_start:
                // check preferences
                SharedPreferences preferences = getSharedPreferences(PreferenceCst.PREF_STORE_NAME, Context.MODE_PRIVATE);
                String prefix = PreferenceCst.PREFIX_TIMER;
                if (preferences.getString(prefix + RINGTONE_URI.toString(), "").equals("") && preferences.getString(prefix + MUSIC_PATH.toString(), "").equals("")) {
                    Toast.makeText(this, R.string.timer_pref_not_set, Toast.LENGTH_LONG).show();
                    return;
                }
                if (!timerView.hasRunningTimer() && timerView.isDurationSet()) {
                    timerView.getTimeSelectionView().startTimer(now);
                    // register clocktimer into DB
                    DbLiveObject<Clock> dbLiveObject = new DbLiveObject<>(this);
                    List<Clock> timer = new ArrayList<>(1);
                    ClockTimer clockTimer = timerView.getTimeSelectionView().getData();
                    timer.add(clockTimer);
                    dbLiveObject.storeLiveObjects(timer, DbConstant.RUNNING_TIMER_TABLE_NAME);
                    dbLiveObject.close();

                    // use alarmManager
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    if (alarmManager != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + clockTimer.getDuration(), getPendingIntent());
                        else
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + clockTimer.getDuration(), getPendingIntent());
                        Toast.makeText(this, R.string.timer_launch_ok, Toast.LENGTH_LONG).show();
                        Log.i(logname, "Timer set, duration: " + clockTimer.getDuration() + " at " + Chronos.ftime.format(System.currentTimeMillis()));
                    } else {
                        Toast.makeText(this, R.string.timer_launch_ko, Toast.LENGTH_LONG).show();
                    }

                }
                break;
            case R.id.btn_stop:
                if (timerView.hasRunningTimer()) {
                    timerView.getTimeSelectionView().stopTimer();
                    cancelJob();
                    Toast.makeText(view.getContext(), R.string.timer_canceled, Toast.LENGTH_LONG).show();
                } else {

                    if (CommonMediaPlayer.Instance().isPlaying()) {
                        CommonMediaPlayer.Instance().stopPlayer();
                        // stop variable volume and duration as well
                        CommonMediaPlayer.Instance().stopAudioDurationVariator();
                        CommonMediaPlayer.Instance().stopAudioVolumeVariator();
                        Toast.makeText(view.getContext(), R.string.timer_sound_stoped, Toast.LENGTH_LONG).show();
                    }
                    if (CommonMediaPlayer.Instance().isVibrating())
                        CommonMediaPlayer.Instance().stopVibrator();
                }
                break;
            case R.id.btn_reset:
                timerView.getTimeSelectionView().resetTimer();
                timerView.resetElapseTimeUI();
                break;
        }
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, TimerReceiver.class);
        return PendingIntent.getBroadcast(this, 0, intent, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!Permissions.Instance().hasReadWriteExternalStorage(this) || timerView.hasRunningTimer() || timerView.hasRunningElapseTimeUI())
            return false;
        Intent intent = new Intent(getBaseContext(), PreferencesActivity.class);
        intent.putExtra(PreferenceCst.PREFIX_BUNDLE_KEY, PreferenceCst.PREFIX_TIMER);
        intent.putExtra(PreferenceCst.PREF_FRAGMENT_CLASS_NAME, AudioNotificationPreferenceFragment.class.getName());
        intent.putExtra(PreferenceCst.PREF_TITLE, "audio_pref_title");
        startActivity(intent);
        return true;
    }

    private void cancelJob() {
        new DbLiveObject<>(this).clearTableAndClose(DbConstant.RUNNING_TIMER_TABLE_NAME);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(getPendingIntent());
        }
    }
}
