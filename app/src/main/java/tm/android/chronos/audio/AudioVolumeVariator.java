/*
 * AudioVolumeVariator
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.audio;

import android.media.AudioManager;
import android.util.Log;
import tm.android.chronos.activity.Chronos;
import tm.android.chronos.core.DelayedActionListener;


/**
 * Dedicated to run into DelayedActionRunner, to smoothly raised up the audio volume.
 */
public class AudioVolumeVariator implements DelayedActionListener {
    private double start = -1.0;
    private double end = -1.0;
    private double step = -1.0;
    private AudioManager audioManager;
    private int maxVol;
    private int iter = 0;
    private final static String logname = Chronos.name+"-AudioVolVariato";
    @Override
    public void onDelayedAction(Object... obj) {
        // no ckeck. Done in actionBefore, and no reuse of params

        Log.i(logname,"Actione audioManager@" +(audioManager == null ? "null" : audioManager.hashCode()) );
        double volume = start + iter * step;
        if (volume <= end) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (maxVol * (volume)), AudioManager.FLAG_ALLOW_RINGER_MODES);
            iter++;
        }
    }

    @Override
    public void onDelayedActionBefore(Object... obj) {
        Log.i(logname,"ActionBefore ");
        if (obj == null || obj.length != 4 || !(obj[0] instanceof AudioManager && obj[1] instanceof Double && obj[2] instanceof Double && obj[3] instanceof Double))
            return;
        start = (double) obj[1];
        end = (double) obj[2];
        step = (end - start) / (double)obj[3];
        audioManager = (AudioManager) obj[0];
        Log.i(logname,"ActionBefore audioManager@" +audioManager.hashCode() );
        maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onDelayedActionAfter(Object... objects) {
        //unused
    }
}
