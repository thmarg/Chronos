/*
 * AudioVolumeVariator
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.audio;

import android.media.AudioManager;
import tm.android.chronos.core.DelayedActionListener;


/**
 * Dedicated to run into DelayedActionRunner, to smoothly raised up the audio volume.
 */
public class AudioVolumeVariator implements DelayedActionListener {
	private double start=-1.0;
	private double end = -1.0;
	private double step=-1.0;
	private AudioManager audioManager;
	private int maxVol;
	private int iter=0;
	@Override
	public void onDelayedAction(Object... obj) {
		if (obj==null || obj.length<3 || !(obj[0] instanceof AudioManager && obj[1] instanceof Double && obj[2] instanceof Double && obj[3] instanceof Double))
			return;
		if (start==-1.0){
			start = (double)obj[1];
			end = (double)obj[2];
			step= (double)obj[3];
			audioManager = (AudioManager)obj[0];
			maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		}
		double volume= start+iter*step;
		if (volume<=end) {
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (maxVol * (volume)), AudioManager.FLAG_ALLOW_RINGER_MODES);
			iter++;
		}

	}
}
