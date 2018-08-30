/*
 * AudioProperties
 *
 * Copyright (c) 2018 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */
package tm.android.chronos.audio;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import tm.android.chronos.preference.PreferenceCst;

import java.io.Serializable;

import static tm.android.chronos.preference.PreferenceCst.PREF_KEYS.*;

/**
 * Audio propertie used to store audio pref.
 */
public class AudioProperties implements Serializable {


    private String musicPath;
    private String musicName;
    private long musicDuration;
    private String ringtonePath;
    private String ringtoneName;
    private boolean ringTone;
    private boolean music;
    private float minVolumeVariable;
    private float maxVolumeVariable;
    private float levelVolumeFixe;
    private long soundDuration;
    private int soundRepeatCount;
    private long delayBetweenSoundRepeat;
    private boolean vibrate;
    private long vibrateDuration;
    private boolean volumeFixe;
    private boolean volumeVariable;
    private int volumeVariableDuration;
    private int volumeVariableStep;


    public AudioProperties(String musicPath, String ringtonePath, int minVolumeVariable, int maxVolumeVariable, long soundDuration, int soundRepeatCount, boolean vibrate, boolean playsound, boolean ringTone) {
        this.musicPath = musicPath;
        this.ringtonePath = ringtonePath;
        this.ringTone = ringTone;
        this.minVolumeVariable = minVolumeVariable;
        this.maxVolumeVariable = maxVolumeVariable;
        this.soundDuration = soundDuration;
        this.soundRepeatCount = soundRepeatCount;
        this.vibrate = vibrate;
    }

    public AudioProperties(){}

    public String getMusicPath() {
        return musicPath;
    }

    public void setMusicPath(String musicPath) {
        this.musicPath = musicPath;
    }

    public String getRingtonePath() {
        return ringtonePath;
    }

    public void setRingtonePath(String ringtonePath) {
        this.ringtonePath = ringtonePath;
    }

    public boolean isRingTone() {
        return ringTone;
    }

    public void setRingTone(boolean ringTone) {
        this.ringTone = ringTone;
    }

    public float getMinVolumeVariable() {
        return minVolumeVariable;
    }

    public void setMinVolumeVariable(float minVolumeVariable) {
        this.minVolumeVariable = minVolumeVariable;
    }

    public float getMaxVolumeVariable() {
        return maxVolumeVariable;
    }

    public void setMaxVolumeVariable(float maxVolumeVariable) {
        this.maxVolumeVariable = maxVolumeVariable;
    }

    public long getSoundDuration() {
        return soundDuration;
    }

    public void setSoundDuration(long soundDuration) {
        this.soundDuration = soundDuration;
    }

    public boolean isVibrate() {
        return vibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public boolean isPlaysound() {
        return ringTone || music;
    }



    public int getSoundRepeatCount() {
        return soundRepeatCount;
    }

    public void setSoundRepeatCount(int soundRepeatCount) {
        this.soundRepeatCount = soundRepeatCount;
    }

    public boolean isVolumeFixe() {
        return volumeFixe;
    }

    public void setVolumeFixe(boolean volumeFixe) {
        this.volumeFixe = volumeFixe;
    }

    public boolean isVolumeVariable() {
        return volumeVariable;
    }

    public void setVolumeVariable(boolean volumeVariable) {
        this.volumeVariable = volumeVariable;
    }

    public boolean isMusic() {
        return music;
    }

    public void setMusic(boolean music) {
        this.music = music;
    }

    public long getVibrateDuration() {
        return vibrateDuration;
    }

    public void setVibrateDuration(long vibrateDuration) {
        this.vibrateDuration = vibrateDuration;
    }

    public String getDataSource() {
        return (ringTone ? ringtonePath : musicPath);
    }

    public long getDelayBetweenSoundRepeat() {
        return delayBetweenSoundRepeat;
    }

    public void setDelayBetweenSoundRepeat(long delayBetweenSoundRepeat) {
        this.delayBetweenSoundRepeat = delayBetweenSoundRepeat;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public long getMusicDuration() {
        return musicDuration;
    }

    public void setMusicDuration(long musicDuration) {
        this.musicDuration = musicDuration;
    }

    public String getRingtoneName() {
        return ringtoneName;
    }

    public void setRingtoneName(String ringtoneName) {
        this.ringtoneName = ringtoneName;
    }

    public float getLevelVolumeFixe() {
        return levelVolumeFixe;
    }

    public void setLevelVolumeFixe(float levelVolumeFixe) {
        this.levelVolumeFixe = levelVolumeFixe;
    }

    public int getVolumeVariableDuration() {
        return volumeVariableDuration;
    }

    public void setVolumeVariableDuration(int volumeVariableDuration) {
        this.volumeVariableDuration = volumeVariableDuration;
    }

    public int getVolumeVariableStep() {
        return volumeVariableStep;
    }

    public void setVolumeVariableStep(int volumeVariableStep) {
        this.volumeVariableStep = volumeVariableStep;
    }

    public void loadFromPref(String prefix, @NonNull Context context){
        SharedPreferences preferences = context.getSharedPreferences(PreferenceCst.PREF_STORE_NAME, 0);
        ringTone = preferences.getBoolean(prefix + RINGTONE_CKB,false);
        ringtonePath = preferences.getString(prefix+RINGTONE_URI,"");
        ringtoneName = preferences.getString(prefix+RINGTONE_NAME,"");
        music = preferences.getBoolean(prefix+MUSIC_CKB,true);
        musicPath = preferences.getString(prefix+MUSIC_PATH,"");
        musicName = preferences.getString(prefix+MUSIC_NAME,"");
        musicDuration = preferences.getLong(prefix+MUSIC_SIZE,0);
        soundDuration = preferences.getLong(prefix+MUSIC_UNIT_DURATION,60000);
        soundRepeatCount = preferences.getInt(prefix+MUSIC_REPEAT_COUNT,0);
        delayBetweenSoundRepeat = preferences.getLong(prefix+DELAY_BETWEEN_REPEAT,0);
        volumeFixe = preferences.getBoolean(prefix+VOL_FIXE_CKB,true);
        volumeVariable = preferences.getBoolean(prefix+VOL_VARIABLE_CKB,false);
        volumeVariableDuration = preferences.getInt(prefix+VOL_VARIABLE_TIME,30);
        volumeVariableStep = preferences.getInt(prefix+VOL_VARIABLE_STEP,1);
        minVolumeVariable = preferences.getFloat(prefix+MIN_VOLUME_VARIABLE,0.0f);
        maxVolumeVariable = preferences.getFloat(prefix+MAX_VOLUME_VARIABLE,1.0f);
        levelVolumeFixe = preferences.getFloat(prefix+VOLUME_FIXE,1.0f);
        vibrate = preferences.getBoolean(prefix+VIBRATE,false);
        vibrateDuration = preferences.getLong(prefix+VIBRATE_DURATION,20000);

    }


    public void storeToPreferences(String prefix, @NonNull Context context){

        SharedPreferences.Editor preferencesEditor = context.getSharedPreferences(PreferenceCst.PREF_STORE_NAME, 0).edit();
        preferencesEditor.putBoolean(prefix + RINGTONE_CKB, ringTone);
        preferencesEditor.putString(prefix+RINGTONE_URI,ringtonePath);
        preferencesEditor.putString(prefix+RINGTONE_NAME,ringtoneName);
        preferencesEditor.putBoolean(prefix + MUSIC_CKB, music);
        preferencesEditor.putString(prefix + MUSIC_PATH, musicPath);
        preferencesEditor.putString(prefix + MUSIC_NAME, musicName);
        preferencesEditor.putLong(prefix + MUSIC_SIZE, musicDuration);
        preferencesEditor.putBoolean(prefix + VOL_FIXE_CKB, volumeFixe);
        preferencesEditor.putBoolean(prefix + VOL_VARIABLE_CKB, volumeVariable);
        preferencesEditor.putInt(prefix + VOL_VARIABLE_TIME,volumeVariableDuration);
        preferencesEditor.putInt(prefix + VOL_VARIABLE_STEP,volumeVariableStep);
        preferencesEditor.putFloat(prefix + MAX_VOLUME_VARIABLE, maxVolumeVariable);
        preferencesEditor.putFloat(prefix + MIN_VOLUME_VARIABLE, minVolumeVariable);
        preferencesEditor.putFloat(prefix+VOLUME_FIXE,levelVolumeFixe);
        preferencesEditor.putLong(prefix + MUSIC_UNIT_DURATION, soundDuration);
        preferencesEditor.putInt(prefix + MUSIC_REPEAT_COUNT, soundRepeatCount);
        preferencesEditor.putLong(prefix+ DELAY_BETWEEN_REPEAT,delayBetweenSoundRepeat);
        preferencesEditor.putBoolean(prefix + VIBRATE, vibrate);
        preferencesEditor.putLong(prefix+VIBRATE_DURATION,vibrateDuration);
        preferencesEditor.apply();
    }




}
