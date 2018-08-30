package tm.android.chronos.core;

import tm.android.chronos.audio.AudioProperties;

import java.io.Serializable;

public class Reminder implements Serializable {
    final static long MINUTE = 60*1000;
    final static long HEURE = 60*MINUTE;
    final static long JOUR = 24*HEURE;
    private long date;
    private AudioProperties audioProperties;


    public Reminder(long date, AudioProperties audioProperties) {
        this.date = date;
        this.audioProperties = audioProperties;
    }

    public void setDate(int jour, int heure, int minutes, long alarmDate) {
        date = alarmDate - jour*JOUR - heure*HEURE - minutes*minutes;
    }

    public long getDate() {
        return date;
    }
}
