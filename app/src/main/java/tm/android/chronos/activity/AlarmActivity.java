/*
 * ${NAME}
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import tm.android.chronos.R;
import tm.android.chronos.audio.AudioProperties;
import tm.android.chronos.core.ui.AbstractListView;
import tm.android.chronos.core.ui.RendererWorker;
import tm.android.chronos.preference.AudioNotificationPreferenceFragment;
import tm.android.chronos.preference.PreferenceCst;
import tm.android.chronos.preference.PreferencesActivity;
import tm.android.chronos.uicomponent.AlarmListView;
import tm.android.chronos.uicomponent.BaseUI;
import tm.android.chronos.uicomponent.event.AlarmListViewController;
import tm.android.chronos.util.Permissions;


/**
 * Created by thmarg on 10/02/15.
 */
public class AlarmActivity extends AppCompatActivity  {
    //private final static long YEAR = 31536000000L;
    private AlarmListView alarmListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarmlayout);
        Toolbar toolbar = findViewById(R.id.chronos_toolbar);
        setSupportActionBar(toolbar);
        alarmListView = new AlarmListView(this);
        alarmListView.setSelectionMode(AbstractListView.SELECT_MODE.SINGLE_SELECT);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(BaseUI.SCREENWIDTH, LinearLayout.LayoutParams.MATCH_PARENT);
        alarmListView.setLayoutParams(layoutParams);
        alarmListView.setPaintBackgroundColor(Color.BLACK);
        alarmListView.setBackgroundColor(Color.TRANSPARENT);
        alarmListView.setOnTouchListener(new AlarmListViewController());
        LinearLayout fond = findViewById(R.id.fond);
        fond.addView(alarmListView);

        RendererWorker rendererWorker = new RendererWorker();
        rendererWorker.setMainGUI(alarmListView);
        alarmListView.setRendererWorker(rendererWorker);
        rendererWorker.setInnerSleepTime(200);
        rendererWorker.start();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_btn_plus:
                // check if prefs are set
                AudioProperties audioProperties = new AudioProperties();
                audioProperties.loadFromPref(PreferenceCst.PREFIX_ALARM,this);
                if (audioProperties.getMusicName().equals("") && audioProperties.getRingtoneName().equals("") && !audioProperties.isVibrate()){
                    Toast.makeText(this,R.string.alarm_setting_no_set,Toast.LENGTH_LONG).show();
                    break;
                }
                alarmListView.addNewAlarm();
                break;
            case R.id.img_btn_moins:
                if (!alarmListView.items.isEmpty())
                    alarmListView.remove();
                break;
            case R.id.btn_play:
                alarmListView.startAlarm();
                break;
            case R.id.btn_stop:
                alarmListView.stopAlarm();
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!Permissions.Instance().hasReadWriteExternalStorage(this))
            return false;
        Intent intent = new Intent(getBaseContext(), PreferencesActivity.class);
        intent.putExtra(PreferenceCst.PREFIX_BUNDLE_KEY, PreferenceCst.PREFIX_ALARM);
        intent.putExtra(PreferenceCst.PREF_FRAGMENT_CLASS_NAME, AudioNotificationPreferenceFragment.class.getName());
        intent.putExtra(PreferenceCst.PREF_TITLE, "audio_pref_title");
        startActivity(intent);
        return true;
    }
}
