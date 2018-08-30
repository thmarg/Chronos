/*
 * AudioNotificationPreference
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.preference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.*;
import tm.android.chronos.R;
import tm.android.chronos.activity.Chronos;
import tm.android.chronos.audio.AudioProperties;
import tm.android.chronos.audio.CommonMediaPlayer;
import tm.android.chronos.core.Alarm;
import tm.android.chronos.core.AlarmData;
import tm.android.chronos.core.Digit;
import tm.android.chronos.core.Units;
import tm.android.chronos.dialogs.AlarmDialog;
import tm.android.chronos.uicomponent.MinMaxSeekBar;
import tm.android.chronos.uicomponent.event.OnSeekBarChangeListener;
import tm.android.chronos.uicomponent.event.SeekBarEvent;
import tm.android.chronos.util.PathFinder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Preferences for sound notification<br>
 * Choices are :
 * <ul>
 * <li>Music file any audio file format supported by the system.</li>
 * <li>Ringtone from the internal storage system</li>
 * <li>Volume is fixed</li>
 * <li> or variable between min and max. This variation is smooth,  has a start time and an end time, between 0 and sound's duration</li>
 * </ul>
 */
public class AudioNotificationPreference extends Preference implements PreferenceManager.OnActivityResultListener, PreferenceManager.OnActivityStopListener, PreferenceManager.OnActivityDestroyListener {

    private final static int SELECT_FROM_FILE = 123456; // for result id in startActivityForResult
    private final static int SELECT_FROM_RINGTONE = 654321;
    //
    private String prefix;
    //
    private CheckBox ckb_music_file;
    private TextView txv_selected_music_file;
    private TextView txv_song_duration;
    private CheckBox ckb_ringtone;
    private TextView txv_selected_ringtone;

    private CheckBox ckb_vol_fixe;

    private CheckBox ckb_vol_variable;
    private LinearLayout vol_variable_params;
    private EditText edt_vol_variable_time;
    private EditText edt_vol_variable_step;

    private MinMaxSeekBar volumeBar;
    private EditText edt_repeatCount;
    private EditText edt_duration;

    private int MaxVolumeValue;
    private double currentUpperVolumeValue;
    private double currentLowerVolumeValue;

    private CheckBox ckb_vibrate;
    private EditText edt_vibrate_duration;

    private ImageButton btn_play;
    private ImageButton btn_stop;
    private TextView txv_time;

    private CheckBox ckb_all_days;

    private boolean firstTime = true;

    private AudioProperties audioProperties;

    public enum MODE {PREFERENCES, PROPERTIES}

    private MODE mode = MODE.PREFERENCES;
    private int day;
    private Alarm alarm;
    private TimeDisplay timeDisplay;

    public AudioNotificationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @SuppressLint("InflateParams")
    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        Digit.setInitilaDigitFormat(Units.DIGIT_FORMAT.NO_MS_SHORT);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        setEnabled(true);
        prefix = ((Activity) getContext()).getIntent().getStringExtra(PreferenceCst.PREFIX_BUNDLE_KEY);
        // by default mode is preferences. A caller can set properties mode by setting this string extra
        String smode = ((Activity) getContext()).getIntent().getStringExtra("mode");
        Object raw0 = ((Activity) getContext()).getIntent().getSerializableExtra("Alarm");
        if (raw0 != null)
            alarm = (Alarm) raw0;

        if (smode != null && smode.equals(MODE.PROPERTIES.toString())) {
            mode = MODE.PROPERTIES;
            Object raw = ((Activity) getContext()).getIntent().getSerializableExtra("AudioProperties");
            if (raw != null)
                audioProperties = (AudioProperties) raw;
            else {
                audioProperties = new AudioProperties();
                audioProperties.loadFromPref(PreferenceCst.PREFIX_ALARM, getContext());
            }
            day = ((Activity) getContext()).getIntent().getIntExtra("dayOfWeek", -1);
        }

        if (mode == MODE.PREFERENCES)
            audioProperties = new AudioProperties();

        Log.i(Chronos.name + "-AudioPref", "OnCreateView Done");

        return inflater.inflate(R.layout.audionotificationpref, null);
    }


    @Override
    protected void onBindView(@SuppressWarnings("NonNull") View view) {
        super.onBindView(view);

        if (firstTime) {
            firstTime = false;
            if (mode == MODE.PREFERENCES)
                audioProperties.loadFromPref(prefix, getContext());
        } else {
            screenToAudioProperties();
        }
        loadScreen(view);

        Log.i(Chronos.name + "-AudioPref", "OnBindView Done");
    }


    private void loadScreen(View view) {
        ClickListener clickListener = new ClickListener();

        ckb_music_file = view.findViewById(R.id.ckb_music_file);
        ckb_music_file.setChecked(audioProperties.isMusic());
        ckb_music_file.setOnClickListener(clickListener);

        txv_selected_music_file = view.findViewById(R.id.txv_selected_music_file);
        txv_selected_music_file.setText(audioProperties.getMusicName());
        txv_selected_music_file.setOnClickListener(clickListener);

        txv_song_duration = view.findViewById(R.id.txv_song_duration);
        txv_song_duration.setText(Digit.split(audioProperties.getMusicDuration()).toString().trim());
        txv_song_duration.setOnClickListener(clickListener);

        //
        boolean checked = ckb_music_file.isChecked();
        txv_selected_music_file.setEnabled(checked);
        txv_song_duration.setEnabled(checked);


        ckb_ringtone = view.findViewById(R.id.ckb_ringtone);
        ckb_ringtone.setChecked(audioProperties.isRingTone());
        ckb_ringtone.setOnClickListener(clickListener);

        txv_selected_ringtone = view.findViewById(R.id.txv_selected_ringtone);
        txv_selected_ringtone.setText(audioProperties.getRingtoneName());
        txv_selected_ringtone.setOnClickListener(clickListener);

        checked = ckb_ringtone.isChecked();
        txv_selected_ringtone.setEnabled(checked);

        //
        ckb_vol_fixe = view.findViewById(R.id.ckb_vol_fixe);
        ckb_vol_fixe.setChecked(audioProperties.isVolumeFixe());
        ckb_vol_fixe.setOnClickListener(clickListener);
        ckb_vol_variable = view.findViewById(R.id.ckb_vol_variable);
        ckb_vol_variable.setChecked(audioProperties.isVolumeVariable());
        ckb_vol_variable.setOnClickListener(clickListener);

        vol_variable_params = view.findViewById(R.id.vol_variable_params);
        vol_variable_params.setVisibility((ckb_vol_fixe.isChecked() ? LinearLayout.GONE : LinearLayout.VISIBLE));

        edt_vol_variable_time = view.findViewById(R.id.edt_vol_variable_time);
        edt_vol_variable_time.setText(String.valueOf(audioProperties.getVolumeVariableDuration()));

        edt_vol_variable_step = view.findViewById(R.id.edt_vol_variable_step);
        edt_vol_variable_step.setText(String.valueOf(audioProperties.getVolumeVariableStep()));

        volumeBar = view.findViewById(R.id.seek_bar_custom);
        volumeBar.addOnSeekBarChangeListener(new SeekBarListener());

        view.findViewById(R.id.btn_play).setOnClickListener(clickListener);
        view.findViewById(R.id.btn_stop).setOnClickListener(clickListener);

        CommonMediaPlayer.build(getContext());

        MaxVolumeValue = CommonMediaPlayer.Instance().getAudioManager().getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        updateVolume();

        btn_play = view.findViewById(R.id.btn_play);
        btn_stop = view.findViewById(R.id.btn_stop);

        txv_time = view.findViewById(R.id.txv_time);

        edt_repeatCount = view.findViewById(R.id.edt_repeatCount);
        edt_repeatCount.setText(String.valueOf(audioProperties.getSoundRepeatCount()));

        edt_duration = view.findViewById(R.id.edt_duration);
        edt_duration.setText(Digit.split(audioProperties.getSoundDuration()).toString().trim());

        ckb_vibrate = view.findViewById(R.id.ckb_vibrate);
        ckb_vibrate.setChecked(audioProperties.isVibrate());

        edt_vibrate_duration = view.findViewById(R.id.edt_vibrate_duration);
        edt_vibrate_duration.setText(Digit.split(audioProperties.getVibrateDuration()).toString().trim());

        enableSound(ckb_music_file.isChecked() || ckb_ringtone.isChecked());

        ckb_all_days = view.findViewById(R.id.ckb_all_days);
        view.findViewById(R.id.all_selected_days).setVisibility(View.GONE);
        if (alarm != null && ((alarm.getAlarmData().getType() == AlarmData.ALARM_TYPE.REPEATED_LOOP && alarm.getAlarmData().getDaysOfWeek().hasModeThanOneDay()) ||
                alarm.getAlarmData().getType() == AlarmData.ALARM_TYPE.REPEATED)) {
            view.findViewById(R.id.all_selected_days).setVisibility(View.VISIBLE);
            if (alarm.getAlarmData().getType() == AlarmData.ALARM_TYPE.REPEATED_LOOP)
                ((TextView) view.findViewById(R.id.txv_all_days)).setText(alarm.getAlarmData().getDaysOfWeek().getSelectedDaysAsString());
        }
    }

    private void screenToAudioProperties() {
        audioProperties.setRingTone(ckb_ringtone.isChecked());
        audioProperties.setRingtoneName(txv_selected_ringtone.getText().toString());
        audioProperties.setMusic(ckb_music_file.isChecked());
        audioProperties.setMusicName(txv_selected_music_file.getText().toString());
        audioProperties.setMusicDuration(Digit.getTimeFromString(txv_song_duration.getText().toString()));
        audioProperties.setVolumeFixe(ckb_vol_fixe.isChecked());
        audioProperties.setLevelVolumeFixe((float) currentUpperVolumeValue);
        audioProperties.setVolumeVariable(ckb_vol_variable.isChecked());
        audioProperties.setVolumeVariableDuration(Integer.valueOf(edt_vol_variable_time.getText().toString()));
        audioProperties.setVolumeVariableStep(Integer.valueOf(edt_vol_variable_step.getText().toString()));
        if (audioProperties.isVolumeVariable()) {
            audioProperties.setMaxVolumeVariable((float) currentUpperVolumeValue);
            audioProperties.setMinVolumeVariable((float) currentLowerVolumeValue);
        } else
            audioProperties.setLevelVolumeFixe((float) currentUpperVolumeValue);
        audioProperties.setSoundDuration(Digit.getTimeFromString(edt_duration.getText().toString()));
        audioProperties.setSoundRepeatCount(Integer.valueOf(edt_repeatCount.getText().toString()));
        audioProperties.setVibrate(ckb_vibrate.isChecked());
        audioProperties.setVibrateDuration(Digit.getTimeFromString(edt_vibrate_duration.getText().toString()));
    }

    private void updateVolume() {
        if (ckb_vol_variable.isChecked()) {
            volumeBar.setMode(MinMaxSeekBar.SEEKBAR_MODE.MIN_MAX);
            currentLowerVolumeValue = audioProperties.getMinVolumeVariable();
            currentUpperVolumeValue = audioProperties.getMaxVolumeVariable();
        } else {
            volumeBar.setMode(MinMaxSeekBar.SEEKBAR_MODE.MAX);
            currentUpperVolumeValue = audioProperties.getLevelVolumeFixe();
            currentLowerVolumeValue = 0.0f;
        }
        volumeBar.setInitialMinRatio(currentLowerVolumeValue);
        volumeBar.setInitialMaxRatio(currentUpperVolumeValue);
    }


    private void blinkit() {
        View t = ckb_music_file.isChecked() ? ckb_music_file : ckb_ringtone;
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setRepeatCount(5);
        anim.setDuration(1000);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setStartOffset(200);
        t.startAnimation(anim);
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        // result code 0 is nothing selected and -1 is selection !
        if (resultCode == 0)
            return true;
        String path;
        SharedPreferences.Editor preferencesEditor = getContext().getSharedPreferences(PreferenceCst.PREF_STORE_NAME, 0).edit();
        switch (requestCode) {
            case SELECT_FROM_FILE:
                // test whether this is a playable file.
                try {
                    MediaMetadataRetriever mediaRetriever = new MediaMetadataRetriever();
                    mediaRetriever.setDataSource(getContext(), data.getData());
                    String artist = mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    String title = mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    String duration = mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    Log.i("AudioPref", "duration : " + duration);
                    mediaRetriever.release();
                    path = PathFinder.getPath(getContext(), data.getData());
                    String name = (artist == null ? Units.getLocalizedText(R.string.unknown_artist) : artist) + " > " +
                            (title == null ? Units.getLocalizedText(R.string.unknown_title) : title);
                    txv_selected_music_file.setText(name);
                    txv_song_duration.setText(Digit.split(Long.valueOf(duration)).toString().trim());
                    audioProperties.setMusicName(name);
                    audioProperties.setMusicPath(path);
                    audioProperties.setMusicDuration(Long.valueOf(duration));
                } catch (RuntimeException e) {
                    blinkit();
                    Toast.makeText(getContext(), R.string.bad_audio_file, Toast.LENGTH_LONG).show();
                    return true;
                }
                break;
            case SELECT_FROM_RINGTONE:
                if (data.getExtras() != null && data.getExtras().size() > 0) {
                    Object raw = data.getExtras().get(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    Uri uri = Uri.parse(String.valueOf(raw));
                    Ringtone rt = RingtoneManager.getRingtone(getContext(), uri);
                    txv_selected_ringtone.setText(rt.getTitle(getContext()));
                    path = PathFinder.getPath(getContext(), uri);
                    audioProperties.setRingtoneName(rt.getTitle(getContext()));
                    audioProperties.setRingtonePath(path);
                }
                break;
        }
        preferencesEditor.apply();
        return true;
    }

    @Override
    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        Log.i(Chronos.name + "-AudioPref", "onAttachedToHierarchy");
        Class<? extends PreferenceManager> cl = preferenceManager.getClass();
        try {
            // On day, may be, android will put the register method on PreferenceManager in public state
            // But thank's to Java reflect we can do the work.
            Method m = cl.getDeclaredMethod("registerOnActivityResultListener", PreferenceManager.OnActivityResultListener.class);
            m.setAccessible(true);
            m.invoke(preferenceManager, AudioNotificationPreference.this);

            m = cl.getDeclaredMethod("registerOnActivityStopListener", PreferenceManager.OnActivityStopListener.class);
            m.setAccessible(true);
            m.invoke(preferenceManager, this);

            m = cl.getDeclaredMethod("registerOnActivityDestroyListener", PreferenceManager.OnActivityDestroyListener.class);
            m.setAccessible(true);
            m.invoke(preferenceManager, this);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            System.out.println("AudioNotificationPreference.onAttachedToHierarchy: Failed something by introspection");//TODO something better
        }
    }

    @Override
    public void onActivityStop() {
        Log.i(Chronos.name + "-AudioPref", "onActivityStop");
        if (timeDisplay != null){
            txv_time.setText("");
            timeDisplay = null;
        }
        // always store the following even if nothing has changed
        screenToAudioProperties();
        if (mode == MODE.PREFERENCES)
            audioProperties.storeToPreferences(prefix, getContext());
        else {
            switch (alarm.getAlarmData().getType()) {
                case ONCE:
                    AlarmDialog.getInstance().getAlarm().getAlarmData().setAudioProperties(audioProperties);
                    break;
                case REPEATED_LOOP:
                case REPEATED_LOOP_SPEC_TIME:
                    if (ckb_all_days.isChecked()) {
                        List<Integer> lst = AlarmDialog.getInstance().getAlarm().getAlarmData().getDaysOfWeek().getSelectedDays();
                        for (int aday : lst)
                            AlarmDialog.getInstance().getAlarm().getAlarmData().getDaysOfWeek().setAudio(aday, audioProperties);
                    } else {
                        AlarmDialog.getInstance().getAlarm().getAlarmData().getDaysOfWeek().setAudio(day, audioProperties);
                    }
                    break;
                case REPEATED:
                    if (ckb_all_days.isChecked()) {
                        for (int key : AlarmDialog.getInstance().getRepeatedAudioPref().keySet())
                            AlarmDialog.getInstance().getRepeatedAudioPref().put(key,audioProperties);
                    } else {
                        AlarmDialog.getInstance().getRepeatedAudioPref().put(day, audioProperties);
                    }
                    break;
            }
        }
    }

    @Override
    protected void onPrepareForRemoval() {
        Log.i(Chronos.name + "-AudioPref", "onPrepareForRemoval");
        super.onPrepareForRemoval();
    }

    @Override
    public void onActivityDestroy() {
        Log.i(Chronos.name + "-AudioPref", "onActivityDestroy");
        CommonMediaPlayer.Instance().stopPlayer();
        CommonMediaPlayer.Instance().stopAudioVolumeVariator();
        CommonMediaPlayer.Instance().stopAudioDurationVariator();
        // display message even if nothing has changed.
        if (mode == MODE.PREFERENCES) {
            if (!audioProperties.isMusic() && !audioProperties.isRingTone() && !audioProperties.isVibrate()) {
                Toast.makeText(getContext(), R.string.audio_pref_stored_but, Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(getContext(), R.string.audio_pref_stored, Toast.LENGTH_SHORT).show();
        }
    }

    private void enableSound(boolean enable) {
        ckb_vol_fixe.setEnabled(enable);
        ckb_vol_variable.setEnabled(enable);

        edt_duration.setEnabled(enable);
        edt_duration.setFocusable(enable);
        edt_repeatCount.setEnabled(enable);
        edt_vol_variable_time.setEnabled(enable);
        edt_vol_variable_step.setEnabled(enable);

        volumeBar.setEnabled(enable);

        btn_play.setEnabled(enable);
        btn_stop.setEnabled(enable);
    }

    private class ClickListener implements View.OnClickListener {

        public void onClick(View view) {
            Intent intent;

            boolean musicChecked = ckb_music_file.isChecked();
            boolean ringtoneChecked = ckb_ringtone.isChecked();
            switch (view.getId()) {
                case R.id.ckb_ringtone:
                case R.id.ckb_music_file:
                    if (view.getId() == R.id.ckb_music_file) {
                        if (CommonMediaPlayer.Instance().isPlaying()) {
                            ckb_music_file.setChecked(!ckb_music_file.isChecked());
                            return;
                        }
                        if (musicChecked) {
                            ckb_ringtone.setChecked(false);
                            ringtoneChecked = false;
                        }
                    } else {
                        if (CommonMediaPlayer.Instance().isPlaying()) {
                            ckb_ringtone.setChecked(!ckb_ringtone.isChecked());
                            return;
                        }
                        if (ringtoneChecked) {
                            ckb_music_file.setChecked(false);
                            musicChecked = false;
                        }
                    }
                    audioProperties.setMusic(ckb_music_file.isChecked());
                    audioProperties.setRingTone(ckb_ringtone.isChecked());
                    txv_selected_ringtone.setEnabled(ringtoneChecked);
                    txv_selected_ringtone.setClickable(ringtoneChecked);

                    txv_selected_music_file.setEnabled(musicChecked);
                    txv_song_duration.setEnabled(musicChecked);

                    enableSound(musicChecked || ringtoneChecked);
                    break;
                case R.id.txv_selected_music_file:
                case R.id.txv_song_duration:
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("audio/*");
                    PreferencesActivity activity = (PreferencesActivity) getContext();
                    activity.getFragment().startActivityForResult(intent, SELECT_FROM_FILE);
                    break;
                case R.id.txv_selected_ringtone:
                    intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    //intent.addFlags(RingtoneManager.TYPE_ALL);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, Units.getLocalizedText("snd_ringtone_notif_pref_title", null));
                    activity = (PreferencesActivity) getContext();
                    activity.getFragment().startActivityForResult(intent, SELECT_FROM_RINGTONE);
                    break;
                case R.id.ckb_vol_fixe:
                case R.id.ckb_vol_variable:
                    if (view.getId() == R.id.ckb_vol_fixe) {
                        if (CommonMediaPlayer.Instance().isPlaying()) {
                            ckb_vol_fixe.setChecked(!ckb_vol_fixe.isChecked());
                            return;
                        }
                        ckb_vol_variable.setChecked(!ckb_vol_fixe.isChecked());
                    } else {
                        if (CommonMediaPlayer.Instance().isPlaying()) {
                            ckb_vol_variable.setChecked(!ckb_vol_variable.isChecked());
                            return;
                        }
                        ckb_vol_fixe.setChecked(!ckb_vol_variable.isChecked());
                    }
                    vol_variable_params.setVisibility((ckb_vol_fixe.isChecked() ? LinearLayout.GONE : LinearLayout.VISIBLE));
                    audioProperties.setVolumeFixe(ckb_vol_fixe.isChecked());
                    audioProperties.setVolumeVariable(ckb_vol_variable.isChecked());
                    updateVolume();
                    break;
                case R.id.edt_vibrate_duration:
                    view.performClick();

                case R.id.btn_play:
                    if (CommonMediaPlayer.Instance().isPlaying())
                        break;
                    String path = audioProperties.isMusic() ? audioProperties.getMusicPath() : audioProperties.getRingtonePath();
                    if (path == null) {
                        blinkit();
                        break;
                    }
                    if (ckb_vol_fixe.isChecked()) {
                        CommonMediaPlayer.Instance().getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC, (int) (currentUpperVolumeValue * MaxVolumeValue), AudioManager.FLAG_ALLOW_RINGER_MODES);
                    } else if (ckb_vol_variable.isChecked()) {
                        int time = Integer.valueOf(edt_vol_variable_time.getText().toString());
                        int step = Integer.valueOf(edt_vol_variable_step.getText().toString());
                        CommonMediaPlayer.Instance().setVariableVolumeAndStart(currentLowerVolumeValue, currentUpperVolumeValue, time, step);

                    }
                    long d = Digit.getTimeFromString(edt_duration.getText().toString());
                    int playCount = Integer.valueOf(edt_repeatCount.getText().toString()) + 1;
                    CommonMediaPlayer.Instance().setVariableDurationAndStart(path, d, playCount);
                    timeDisplay = new TimeDisplay();
                    txv_song_duration.postDelayed(timeDisplay,1000);
                    if (audioProperties.isVolumeVariable())
                        volumeBar.setEnabled(false);
                    break;
                case R.id.btn_stop:
                    CommonMediaPlayer.Instance().stopPlayer();
                    CommonMediaPlayer.Instance().stopAudioVolumeVariator();
                    CommonMediaPlayer.Instance().stopAudioDurationVariator();
                    if (timeDisplay != null){
                        txv_time.setText("");
                        timeDisplay = null;
                    }
                    if (audioProperties.isVolumeVariable())
                        volumeBar.setEnabled(true);
                    break;
            }
        }

    }

    private class SeekBarListener implements OnSeekBarChangeListener {
        @Override
        public void OnSeekBarChange(SeekBarEvent event) {
            if (ckb_vol_fixe.isChecked() || (ckb_vol_variable.isChecked() && !CommonMediaPlayer.Instance().isPlaying())) {
                if (event.getType() == SeekBarEvent.TYPE.MAX)
                    currentUpperVolumeValue = event.getValue();
                else if (event.getType() == SeekBarEvent.TYPE.MIN)
                    currentLowerVolumeValue = event.getValue();
            }

            if (ckb_vol_fixe.isChecked())
                CommonMediaPlayer.Instance().getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC, (int) (MaxVolumeValue * currentUpperVolumeValue), AudioManager.FLAG_ALLOW_RINGER_MODES);
        }
    }

    private class TimeDisplay implements Runnable {
        @Override
        public void run() {
            long time = CommonMediaPlayer.Instance().getPosition();
            if (time>0) {
                txv_time.setText(Digit.split(time).toString());
                txv_time.postDelayed(TimeDisplay.this,1000);
            }
        }
    }
}

