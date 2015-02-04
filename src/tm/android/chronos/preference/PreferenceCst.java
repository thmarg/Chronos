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
	public static final String PREF_STORE_NAME="CHRONOS_PREF_STORE";
	public static enum  PREF_KEYS {RINGTONE_NAME, RINGTONE_URI,RINGTONE_CKB, MUSIC_NAME,MUSIC_PATH, MUSIC_CKB, VOL_FIXE_CKB,VOL_VARIABLE_CKB,MIN_VOLUME,MAX_VOLUME}
	public static String PREFIX_STOPWATCHES="ST_";// Some preference are conceptually identical from one activity to another, but values can be different
	public static String PREFIX_TIMER="TM_";//		So we need a prefix to distinguish key, as we choose to have one single pref file.
	public static String PREFIX_BUNDLE_KEY="PREFIX";
	public static String PREF_TITLE="PREF_TITLE";
	public static String PREF_FRAGMENT_CLASS_NAME="PFC_NAME";


}
