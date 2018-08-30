/*
 *  ChronometerActivity
 *
 *   Copyright (c) 2014 Thierry Margenstern under MIT license
 *   http://opensource.org/licenses/MIT
 */

package tm.android.chronos.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import tm.android.chronos.R;
import tm.android.chronos.core.Digit;
import tm.android.chronos.core.Stopwatch;
import tm.android.chronos.core.Units;
import tm.android.chronos.core.ui.AbstractListView;
import tm.android.chronos.core.ui.RendererWorker;
import tm.android.chronos.preference.PreferenceCst;
import tm.android.chronos.preference.PreferencesActivity;
import tm.android.chronos.preference.StopwatchPreferenceFragment;
import tm.android.chronos.sql.DbConstant;
import tm.android.chronos.sql.DbLiveObject;
import tm.android.chronos.uicomponent.BaseUI;
import tm.android.chronos.uicomponent.ChronoListView;
import tm.android.chronos.uicomponent.StopWatchUI2;
import tm.android.chronos.uicomponent.event.ChronoListViewController;

import java.util.ArrayList;
import java.util.List;

import static tm.android.chronos.activity.ChronometerActivity.PowerKeyState.*;


/**
 *
 */
public class ChronometerActivity extends AppCompatActivity {

    private ChronoListView listView;
    private RendererWorker rendererWorker;
    private PowerKeyState powerKeyState = START;
    private long now;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chronolayout);
        Toolbar toolbar = findViewById(R.id.chronos_toolbar);
        setSupportActionBar(toolbar);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        listView = new ChronoListView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(BaseUI.SCREENWIDTH, LinearLayout.LayoutParams.MATCH_PARENT);
        listView.setLayoutParams(layoutParams);
        listView.setPaintBackgroundColor(Color.BLACK);
        listView.setBackgroundColor(Color.TRANSPARENT);
        listView.setSelectionMode(AbstractListView.SELECT_MODE.MULTI_SELECT);
        listView.setOnTouchListener(new ChronoListViewController(listView));

        ((LinearLayout) findViewById(R.id.fond)).addView(listView);
        rendererWorker = new RendererWorker();
        rendererWorker.setMainGUI(listView);
        listView.setRendererWorker(rendererWorker);
        rendererWorker.start();

        //adaptation to very wide screen ... to see ...
        RelativeLayout relativeLayout = findViewById(R.id.mainlayout);
        FrameLayout.LayoutParams layoutParams1 = new FrameLayout.LayoutParams(BaseUI.SCREENWIDTH, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams1.gravity = Gravity.CENTER;

        relativeLayout.setLayoutParams(layoutParams1);

        Digit.setInitilaDigitFormat(Units.DIGIT_FORMAT.VERY_SHORT);

        SharedPreferences preferences = getSharedPreferences(PreferenceCst.PREF_STORE_NAME, 0);
        StopWatchUI2.setShowStartTime(preferences.getBoolean(PreferenceCst.PREFIX_STOPWATCHES + PreferenceCst.PREF_KEYS.STOPWATCH_DSP_START_TIME, false));
        StopWatchUI2.setShowStartDate(preferences.getBoolean(PreferenceCst.PREFIX_STOPWATCHES + PreferenceCst.PREF_KEYS.STOPWATCH_DSP_START_DATE, false));

//        locationBroadcast = new LocationBroadcast();
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("Chronos-go");
//        registerReceiver(locationBroadcast,filter);
    }


    @Override
    protected void onDestroy() {
        rendererWorker.finalStop();
//        if (locationBroadcast != null)
//            unregisterReceiver(locationBroadcast);
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && listView.hasRunningStopWatch())
            powerKeyState = STOP;
    }

    /*
        Management of the button at the bottom of the screen
         */
    public void onClick(View view) {
        long now = System.currentTimeMillis();
        switch (view.getId()) {
            case R.id.img_btn_plus:
                listView.addNewStopwatch();
                break;
            case R.id.img_btn_moins:
                if (!listView.items.isEmpty())
                    listView.remove();
                break;
            case R.id.btn_start:
                if (listView.startStopwatches(now))
                    powerKeyState = STOP;
                break;
            case R.id.btn_stop:
                if (listView.stopStopwatches(now))
                    powerKeyState = RESET;
                break;
            case R.id.btn_pause:
                listView.lapTimeStopwatches(now);
                break;
            case R.id.btn_reset:
                if (listView.resetStopwatches())
                    powerKeyState = START;
                break;
        }

    }

    /*
     *Management with physical button volume up (laptime) and volume down (start stop reset)
     */
    @Override
    public boolean dispatchKeyEvent(@SuppressWarnings("NonNull") KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            onBackPressed();
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            now = System.currentTimeMillis();
            return true;
        }

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                switch (powerKeyState) {
                    case START:
                        if (listView.startStopwatches(now))
                            powerKeyState = STOP;
                        return true;
                    case STOP:
                        if (listView.stopStopwatches(now))
                            powerKeyState = RESET;
                        return true;
                    case RESET:
                        if (listView.resetStopwatches())
                            powerKeyState = START;
                        return true;
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                listView.lapTimeStopwatches(now);
                return true;
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (!listView.items.isEmpty()) {
            Stopwatch stopwatch = listView.items.get(0).getData();
            if (stopwatch.getStopwatchData().getChronoType() == Units.CHRONO_TYPE.SEGMENTS && stopwatch.getStopwatchData().isUseGps()) {
                DbLiveObject<Stopwatch> dbLiveObject = new DbLiveObject<>(getBaseContext());
                List<Stopwatch> lst = new ArrayList<>(1);
                lst.add(stopwatch);
                dbLiveObject.storeLiveObjects(lst, DbConstant.RUNNING_STOPWATCHES_TABLE_NAME);
                dbLiveObject.close();
                if (dbLiveObject.hasError()) {
                    Toast.makeText(ChronometerActivity.this, dbLiveObject.getErrorMessage().localiszedMessage, Toast.LENGTH_LONG).show();
                    return;
                }
                super.onBackPressed();
            } else {
                final List<Stopwatch> runners = listView.getRunningStopwatch();
                if (!runners.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(runners.size() == 1 ? getResources().getString(R.string.store_run_stwtc) : getResources().getString(R.string.store_run_stwtces));
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            DbLiveObject<Stopwatch> dbLiveObject = new DbLiveObject<>(getBaseContext());
                            dbLiveObject.storeLiveObjects(runners, DbConstant.RUNNING_STOPWATCHES_TABLE_NAME);
                            dbLiveObject.close();
                            if (dbLiveObject.hasError()) {
                                Toast.makeText(ChronometerActivity.this, dbLiveObject.getErrorMessage().localiszedMessage, Toast.LENGTH_LONG).show();
                                return;
                            }
                            finish();
                        }
                    });

                    builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });


                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    super.onBackPressed();
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    protected enum PowerKeyState {START, STOP, RESET}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings_stopwatch, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (listView.hasRunningStopWatch())
            return false;

        Intent intent;
        switch (item.getItemId()) {
            case R.id.param:
                intent = new Intent(getBaseContext(), PreferencesActivity.class);
                intent.putExtra(PreferenceCst.PREFIX_BUNDLE_KEY, PreferenceCst.PREFIX_STOPWATCHES);
                intent.putExtra(PreferenceCst.PREF_FRAGMENT_CLASS_NAME, StopwatchPreferenceFragment.class.getName());
                intent.putExtra(PreferenceCst.PREF_TITLE, "stopwatch_settings");
                startActivity(intent);
                break;
            case R.id.tracks:
                intent = new Intent(getBaseContext(), ActivityTracks.class);
                startActivity(intent);
                break;

        }
        return true;
    }


}
