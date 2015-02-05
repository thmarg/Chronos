/*
 * AudioNotificationPreference
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.preference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.*;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import tm.android.chronos.R;
import tm.android.chronos.audio.AudioVolumeVariator;
import tm.android.chronos.core.DelayedActionRunner;
import tm.android.chronos.core.Units;
import tm.android.chronos.uicomponent.MinMaxSeekBar;
import tm.android.chronos.uicomponent.event.OnSeekBarChangeListener;
import tm.android.chronos.uicomponent.event.SeekBarEvent;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static tm.android.chronos.preference.PreferenceCst.PREF_KEYS.*;


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
	private CheckBox ckb_musicfile;
	private CheckBox ckb_ringtone;
	private TextView musicFile_label;
	private TextView ringtone_label;
	private TextView txt_view_selectedMusicFile;
	private TextView txt_view_selectedRingtone;
	private CheckBox ckb_volFixe;
	private CheckBox ckb_volVariable;
	private MinMaxSeekBar volumeBar;
	private MediaPlayer player;
	private AudioManager audioManager;
	private int MaxVolumeValue;
	private double currentUpperVolumeValue;
	private double currentLowerVolumeValue;
	private DelayedActionRunner<AudioVolumeVariator> delayedActionRunner;

	//
	String prefix;

	//
	private PreferenceManager preferenceManager;


	public AudioNotificationPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		setLayoutResource(R.layout.audionotificationpref);
		LayoutInflater inflater = LayoutInflater.from(getContext());

		return inflater.inflate(R.layout.audionotificationpref, null);
	}


	@Override
	protected void onBindView(@SuppressWarnings("NonNull") View view) {
		super.onBindView(view);
		ClickListener clickListener = new ClickListener();
		prefix = ((Activity) getContext()).getIntent().getStringExtra(PreferenceCst.PREFIX_BUNDLE_KEY);
		SharedPreferences preferences = getContext().getSharedPreferences(PreferenceCst.PREF_STORE_NAME, DEFAULT_ORDER);

		txt_view_selectedRingtone = ((TextView) view.findViewById(R.id.txt_view_selectedRingtone));
		ringtone_label = (TextView) view.findViewById(R.id.label_ringtone);
		txt_view_selectedRingtone.setText(preferences.getString(prefix + RINGTONE_NAME.toString(), ""));
		txt_view_selectedRingtone.setOnClickListener(clickListener);
		ringtone_label.setOnClickListener(clickListener);

		ckb_ringtone = (CheckBox) view.findViewById(R.id.ckb_ringtone);
		ckb_ringtone.setChecked(preferences.getBoolean(prefix + RINGTONE_CKB.toString(), false));
		ckb_ringtone.setOnClickListener(clickListener);

		boolean checked = ckb_ringtone.isChecked();
		txt_view_selectedRingtone.setEnabled(checked);
		ringtone_label.setEnabled(checked);

		txt_view_selectedMusicFile = ((TextView) view.findViewById(R.id.txt_view_selectedMusicFile));
		txt_view_selectedMusicFile.setText(preferences.getString(prefix + MUSIC_NAME.toString(), ""));
		txt_view_selectedMusicFile.setOnClickListener(clickListener);
		musicFile_label = (TextView) view.findViewById(R.id.label_music_file);
		musicFile_label.setOnClickListener(clickListener);
		//
		ckb_musicfile = ((CheckBox) view.findViewById(R.id.ckb_music_file));
		ckb_musicfile.setChecked(preferences.getBoolean(prefix + MUSIC_CKB.toString(), true));
		ckb_musicfile.setOnClickListener(clickListener);
		checked = ckb_musicfile.isChecked();
		txt_view_selectedMusicFile.setEnabled(checked);
		musicFile_label.setEnabled(checked);
		//
		ckb_volFixe = (CheckBox) view.findViewById(R.id.ckb_volFixe);
		ckb_volFixe.setChecked(preferences.getBoolean(prefix + VOL_FIXE_CKB.toString(), true));
		ckb_volFixe.setOnClickListener(clickListener);
		ckb_volVariable = (CheckBox) view.findViewById(R.id.ckb_volVariable);
		ckb_volVariable.setChecked(preferences.getBoolean(prefix + VOL_VARIABLE_CKB.toString(), false));
		ckb_volVariable.setOnClickListener(clickListener);

		volumeBar = (MinMaxSeekBar) view.findViewById(R.id.seek_bar_custom);
		volumeBar.addOnSeekBarChangeListener(new SeekBarListener());

		view.findViewById(R.id.btn_play).setOnClickListener(clickListener);
		view.findViewById(R.id.btn_stop).setOnClickListener(clickListener);

		player = MediaPlayer.create(getContext(), Uri.parse(""));
		player.reset();
		player.setOnCompletionListener(new MediaPlayListener());

		audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
		MaxVolumeValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		currentLowerVolumeValue = preferences.getFloat(prefix + MIN_VOLUME, 0);
		currentUpperVolumeValue = preferences.getFloat(prefix + MAX_VOLUME, 1);
		volumeBar.setInitialMaxRatio(currentUpperVolumeValue);
		volumeBar.setInitialMinRatio(currentLowerVolumeValue);
	}

	@Override
	protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
		super.onAttachedToHierarchy(preferenceManager);
		this.preferenceManager = preferenceManager;
		Class cl = preferenceManager.getClass();
		try {
			// On day, may be, bastard maker of android will put the register method on PreferenceManager public.
			// registerOnActivityStopListener is public in version 5.0 but why not from the start ?
			// Otherwise don't speak about custom "complex" preference in the API guide.
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
			System.out.println("FUCK");//TODO something better
		}
	}

	private class ClickListener implements View.OnClickListener {

		public void onClick(View view) {
			Intent intent;

			boolean musicChecked = ckb_musicfile.isChecked();
			boolean ringtoneChecked = ckb_ringtone.isChecked();
			switch (view.getId()) {
				case R.id.ckb_ringtone:
				case R.id.ckb_music_file:
					if (view.getId() == R.id.ckb_music_file) {
						ckb_ringtone.setChecked(!musicChecked);
						ringtoneChecked = ckb_ringtone.isChecked();
					}
					else {
						ckb_musicfile.setChecked(!ringtoneChecked);
						musicChecked = ckb_musicfile.isChecked();
					}
					ringtone_label.setEnabled(ringtoneChecked);
					ringtone_label.setClickable(ringtoneChecked);
					txt_view_selectedRingtone.setEnabled(ringtoneChecked);
					txt_view_selectedRingtone.setClickable(ringtoneChecked);

					musicFile_label.setEnabled(musicChecked);
					musicFile_label.setClickable(musicChecked);
					txt_view_selectedMusicFile.setEnabled(musicChecked);
					txt_view_selectedMusicFile.setClickable(musicChecked);
					break;
				case R.id.txt_view_selectedMusicFile:
				case R.id.label_music_file:
					intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("audio/*");
					Intent chooser = Intent.createChooser(intent, Units.getLocalizedText("snd_file_notif_pref_title", null));
					try { // introspection for forcing access see comment in onAttachedToHierarchy
						Class cl = preferenceManager.getClass();
						Method m = cl.getDeclaredMethod("getFragment", null);
						m.setAccessible(true);
						PreferenceFragment fragment = (PreferenceFragment) m.invoke(preferenceManager, null);
						if (fragment != null)
							fragment.startActivityForResult(chooser, SELECT_FROM_FILE);
						else
							((Activity) getContext()).startActivityForResult(chooser, SELECT_FROM_FILE);
					} catch (Exception e) {
						System.out.println("ZUT"); //TODO something better
					}
					break;
				case R.id.txt_view_selectedRingtone:
				case R.id.label_ringtone:
					intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
					intent.addFlags(RingtoneManager.TYPE_ALL);
					intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, Units.getLocalizedText("snd_ringtone_notif_pref_title", null));
					try { // introspection for forcing access see comment in onAttachedToHierarchy
						Class cl = preferenceManager.getClass();
						Method m = cl.getDeclaredMethod("getFragment", null);
						m.setAccessible(true);
						PreferenceFragment fragment = (PreferenceFragment) m.invoke(preferenceManager, null);
						if (fragment != null)
							fragment.startActivityForResult(intent, SELECT_FROM_RINGTONE);
						else
							((Activity) getContext()).startActivityForResult(intent, SELECT_FROM_RINGTONE);
					} catch (Exception e) {
						System.out.println("ZUT"); //TODO something better
					}
					break;
				case R.id.ckb_volFixe:
				case R.id.ckb_volVariable:
					if (view.getId() == R.id.ckb_volFixe)
						ckb_volVariable.setChecked(!ckb_volFixe.isChecked());
					else
						ckb_volFixe.setChecked(!ckb_volVariable.isChecked());
					break;
				case R.id.btn_play:
					if (player.isPlaying())
						break;
					Uri uri = getSelectedUri();
					if (uri == null) {
						blinkit();
						break;
					}
					if (ckb_volFixe.isChecked())
						audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (currentUpperVolumeValue * MaxVolumeValue), AudioManager.FLAG_ALLOW_RINGER_MODES);
					try {
						player.setDataSource(getContext(), getSelectedUri());
						player.prepare();
						player.start();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (ckb_volVariable.isChecked()){
						delayedActionRunner = new DelayedActionRunner<>(DelayedActionRunner.TYPE.RUNNER,new AudioVolumeVariator(),audioManager,currentLowerVolumeValue,currentUpperVolumeValue,(currentUpperVolumeValue-currentLowerVolumeValue)/30);
						delayedActionRunner.setDelay(1000);
						delayedActionRunner.setSleepStepDelay(500);
						delayedActionRunner.setSleepStepRunner(1000);
						delayedActionRunner.setRunnerDuration(30000);
						delayedActionRunner.start();
					}
					break;
				case R.id.btn_stop:
					if (!player.isPlaying())
						break;
					delayedActionRunner.stopAll();
					player.stop();
					player.reset();
					break;
			}
		}
	}

	private Uri getSelectedUri() {
		SharedPreferences preferences = getContext().getSharedPreferences(PreferenceCst.PREF_STORE_NAME, DEFAULT_ORDER);
		String path;
		if (ckb_ringtone.isChecked())
			path = preferences.getString(prefix + RINGTONE_URI.toString(), "");
		else
			path = preferences.getString(prefix + MUSIC_PATH.toString(), "");

		if (!path.equals(""))
			return Uri.parse(path);

		return null;
	}


	private void blinkit() {
		TextView t = ckb_musicfile.isChecked()?musicFile_label:ringtone_label;
		Animation anim = new AlphaAnimation(0.0f, 1.0f);
		anim.setRepeatCount(5);
		anim.setDuration(50);
		anim.setRepeatMode(Animation.REVERSE);
		anim.setStartOffset(20);
		t.startAnimation(anim);
	}


	private class SeekBarListener implements OnSeekBarChangeListener {

		@Override
		public void OnSeekBarChange(SeekBarEvent event) {
			if (ckb_volFixe.isChecked() || (ckb_volVariable.isChecked() && !player.isPlaying()))
				if (event.getType() == SeekBarEvent.TYPE.MAX)
					currentUpperVolumeValue = event.getValue();
				else
					currentLowerVolumeValue = event.getValue();

			if (!player.isPlaying())
				return;
			if (ckb_volFixe.isChecked())
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (MaxVolumeValue * currentUpperVolumeValue), AudioManager.FLAG_ALLOW_RINGER_MODES);
		}
	}

	private class MediaPlayListener implements MediaPlayer.OnCompletionListener {

		@Override
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.stop();
			mediaPlayer.reset();
		}
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		// result code 0 is nothing selected and -1 is selection !
		if (resultCode == 0)
			return true;
		SharedPreferences.Editor preferencesEditor = getContext().getSharedPreferences(PreferenceCst.PREF_STORE_NAME, Context.MODE_APPEND).edit();
		switch (requestCode) {
			case SELECT_FROM_FILE:
				System.out.println("[EVENT] data uri " + data.getData());
				// test whether this is a playable file.
				MediaPlayer mp = MediaPlayer.create(getContext(), data.getData());
				if (mp == null) {
					Toast.makeText(getContext(), "The selected file is not an audio playable file on this sytem", Toast.LENGTH_LONG).show();
					return true;
				}
				MediaMetadataRetriever mediaRetriever = new MediaMetadataRetriever();
				mediaRetriever.setDataSource(getContext(), data.getData());
				String artist = mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
				String title = mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
				mediaRetriever.release();
				String name = (artist==null ? Units.getLocalizedText("unknown_artist"):artist)+" "+(title==null?Units.getLocalizedText("unknown_title"):title);
				txt_view_selectedMusicFile.setText(name);
				preferencesEditor.putString(prefix + MUSIC_NAME.toString(), name);
				preferencesEditor.putString(prefix + MUSIC_PATH.toString(), data.getDataString());
				break;
			case SELECT_FROM_RINGTONE:
				if (data.getExtras() != null && data.getExtras().size() > 0) {
					Uri uri = (Uri) data.getExtras().get(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
					Ringtone rt = RingtoneManager.getRingtone(getContext(), uri);
					txt_view_selectedRingtone.setText(rt.getTitle(getContext()));
					preferencesEditor.putString(prefix + RINGTONE_NAME.toString(), rt.getTitle(getContext()));
					preferencesEditor.putString(prefix + RINGTONE_URI.toString(), uri.toString());
				}
				break;
		}
		preferencesEditor.apply();
		return true;
	}


	@Override
	public void onActivityStop() {
		// always store the following even if nothing has changed
		SharedPreferences.Editor preferencesEditor = getContext().getSharedPreferences(PreferenceCst.PREF_STORE_NAME, Context.MODE_APPEND).edit();
		preferencesEditor.putBoolean(prefix + RINGTONE_CKB, ckb_ringtone.isChecked());
		preferencesEditor.putBoolean(prefix + MUSIC_CKB, ckb_musicfile.isChecked());
		preferencesEditor.putBoolean(prefix + VOL_FIXE_CKB,ckb_volFixe.isChecked());
		preferencesEditor.putBoolean(prefix + VOL_VARIABLE_CKB,ckb_volVariable.isChecked());
		preferencesEditor.putFloat(prefix + MAX_VOLUME, (float) currentUpperVolumeValue);
		preferencesEditor.putFloat(prefix + MIN_VOLUME, (float) currentLowerVolumeValue);
		preferencesEditor.apply();
	}

	@Override
	public void onActivityDestroy() {
		player.release();
		// display message even if nothing has changed.
		SharedPreferences preferences = getContext().getSharedPreferences(PreferenceCst.PREF_STORE_NAME, DEFAULT_ORDER);
		if (preferences.getString(prefix + RINGTONE_URI.toString(), "").equals("") && preferences.getString(prefix + MUSIC_PATH.toString(), "").equals("")) {
			Toast.makeText(getContext(), Units.getLocalizedText("audio_pref_stored_but"), Toast.LENGTH_LONG).show();
		}
		else
			Toast.makeText(getContext(), Units.getLocalizedText("audio_pref_stored"), Toast.LENGTH_SHORT).show();
	}
}

