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
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import tm.android.chronos.R;
import tm.android.chronos.uicomponent.WatchTimer;

/**
 * The activity of the Timer.
 */
public class ActivityTimer extends Activity {

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
        WatchTimer watch = (WatchTimer)findViewById(R.id.watch);

        if(screenWidth>screenHeight) {
            watch.getLayoutParams().width = screenHeight;
            watch.getLayoutParams().height = screenHeight;
        } else {
            watch.getLayoutParams().width = screenWidth;
            watch.getLayoutParams().height = screenWidth;
        }
        //watch.setActivity(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode==1 && data != null){
//
//            if (data.getExtras()!=null && data.getExtras().size()>0){
//                Uri uri =  (Uri) data.getExtras().get(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
//                Ringtone rt = RingtoneManager.getRingtone(getBaseContext(),uri);
//                rt.play();
//            }
            //RingtoneManager.getRingtone(getBaseContext(), data.getData()).play();

//            MediaPlayer player = MediaPlayer.create(getBaseContext(),data.getData());
//            player.setVolume(1.0f,1.0f);
//            player.start();


        //}
    }

    public void onClick(View  view){
        WatchTimer watchTimer = (WatchTimer)findViewById(R.id.watch);
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

}
