/*
 * TimerActivity
 *
 * Copyright (c) 2014 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import tm.android.chronos.R;
import tm.android.chronos.core.Digit;
import tm.android.chronos.core.Units;
import tm.android.chronos.preference.TimerPreferenceFragment;
import tm.android.chronos.preference.PreferenceActivity;
import tm.android.chronos.preference.PreferenceCst;
import tm.android.chronos.uicomponent.WatchTimer;

/**
 * The activity of the Timer.
 */
public class ActivityTimer extends Activity {

    private WatchTimer watchTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timerlayout);
        int screenWidth =  Resources.getSystem().getDisplayMetrics().widthPixels;
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        if (screenWidth >1024)
            screenWidth = 1024;
        RelativeLayout rl = (RelativeLayout)findViewById(R.id.rlayout);
        rl.getLayoutParams().width= screenWidth;
        ((FrameLayout.LayoutParams)rl.getLayoutParams()).gravity = Gravity.CENTER;
        watchTimer = (WatchTimer)findViewById(R.id.watch);

        if(screenWidth>screenHeight)
            watchTimer.getLayoutParams().width = screenHeight;
         else
            watchTimer.getLayoutParams().width = screenWidth;

        watchTimer.getLayoutParams().height = screenHeight-50;
        //watch.setActivity(this);
        Digit.setInitilaDigitFormat(Units.DIGIT_FORMAT.NO_MS_SHORT);
    }



    public void onClick(View  view){

        switch (view.getId()){
            case R.id.btn_start :
                watchTimer.start(System.currentTimeMillis());
                break;
            case R.id.btn_stop :
                watchTimer.stop();
                break;
            case R.id.btn_reset :
                watchTimer.reset();
        }
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        Intent intent = new Intent(getBaseContext(),PreferenceActivity.class);
        intent.putExtra(PreferenceCst.PREFIX_BUNDLE_KEY, PreferenceCst.PREFIX_TIMER);
        intent.putExtra(PreferenceCst.PREF_FRAGMENT_CLASS_NAME, TimerPreferenceFragment.class.getName());
        startActivity(intent);
        return true;
    }


    @Override
    protected void onDestroy() {
        watchTimer.releaseResources();

        super.onDestroy();
    }
}
