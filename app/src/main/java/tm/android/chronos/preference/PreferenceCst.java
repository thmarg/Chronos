/*
 * ${NAME}
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.preference;


/**
 * A simple container to hold some constant needed across preferences classes
 */
public class PreferenceCst {
    public final static  String PREF_STORE_NAME = "CHRONOS_PREF_STORE";
    public final static String PREFIX_STOPWATCHES = "ST_";// Some preference are conceptually identical from one activity to another, but values can be different
    public final static String PREFIX_TIMER = "TM_";//		So we need a prefix to distinguish key, as we choose to have one single pref file.
    public final static String PREFIX_ALARM = "AL_";
    public final static String PREFIX_BUNDLE_KEY = "PREFIX";
    public final static String PREF_TITLE = "PREF_TITLE";
    public final static String PREF_FRAGMENT_CLASS_NAME = "PFC_NAME";

    public enum PREF_KEYS {
        RINGTONE_NAME, RINGTONE_URI, RINGTONE_CKB, MUSIC_NAME, MUSIC_PATH, MUSIC_CKB, MUSIC_SIZE, VOL_FIXE_CKB, VOL_VARIABLE_CKB, MIN_VOLUME, MAX_VOLUME,
        STOPWATCH_DSP_START_TIME, STOPWATCH_DSP_START_DATE, STOPWATCH_ALLOW_RM_RUNNING, MUSIC_REPEAT_COUNT, MUSIC_UNIT_DURATION,DELAY_BETWEEN_REPEAT,
        VOL_VARIABLE_TIME, VOL_VARIABLE_STEP, VIBRATE, VIBRATE_DURATION, MIN_VOLUME_VARIABLE, MAX_VOLUME_VARIABLE, VOLUME_FIXE
    }


}
