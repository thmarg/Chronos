package tm.android.chronos.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.Log;
import tm.android.chronos.activity.Chronos;
import tm.android.chronos.core.DelayedActionListener;
import tm.android.chronos.core.DelayedActionRunner;
import tm.android.chronos.util.Randomizer;

import java.io.IOException;

import static tm.android.chronos.core.DelayedActionRunner.TYPE.RUNNER;

public class CommonMediaPlayer implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener , DelayedActionListener {
    private final static String logname = Chronos.name+"-CommonMediaPlay";
    private static CommonMediaPlayer instance;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private Vibrator vibrator;
    private DelayedActionRunner<CommonMediaPlayer> audioDurationVariator;
    private DelayedActionRunner<AudioVolumeVariator>     audioVolumeVariator ;
    private DelayedActionRunner<VibratorVariator> vibratorRunner;
    private DelayedActionRunner<PauseDelay> pauseRunner;
    private boolean randomPlay = false;
    private Randomizer<String> songs;

    private CommonMediaPlayer() {
    }

    public static CommonMediaPlayer Instance() {
        if (instance == null)
            instance = new CommonMediaPlayer();
        return instance;
    }

    private void init(Context context) {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnErrorListener(instance);
        mediaPlayer.setOnCompletionListener(instance);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);

    }

    private boolean isInitialized() {
        return mediaPlayer != null && audioManager != null;
    }

    public void setTrack(String path) throws IOException {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause(); // this should not happen here.

        }
        mediaPlayer.reset();
        Log.i(logname, "SetDataSource : " + path);
        mediaPlayer.setDataSource(path);
        mediaPlayer.prepare();
    }

    public void stopPlayer() {
        if (mediaPlayer!=null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    public void releasePlayer() {
            stopPlayer();
            mediaPlayer.release();
            mediaPlayer = null;

    }

    public void pause(long duration) {
        if (pauseRunner != null)
            pauseRunner.safeStop();

        pauseRunner = new DelayedActionRunner<>(DelayedActionRunner.TYPE.RUNNER,new PauseDelay(),mediaPlayer);
        pauseRunner.setRunnerDuration(duration);
        pauseRunner.setSleepStepRunner(duration/5);
        pauseRunner.start();
    }

    public void addPause(long duration) {
        if (pauseRunner != null) {
            pauseRunner.setRunnerDuration(duration+pauseRunner.getRunnerDuration());
        }
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public boolean isPlaying() {
        return mediaPlayer!= null && mediaPlayer.isPlaying();
    }

    /**
     * Call this method before doing anything with {@link CommonMediaPlayer} it will properly handle init
     * and start or restart for you. Just provide an Android Context and it's ready.
     *
     * @param context {@link Context} to pass to get an {@link AudioManager}
     */
    public static void build(Context context) {
        if (!CommonMediaPlayer.Instance().isInitialized())
            CommonMediaPlayer.Instance().init(context);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.i(logname, "OnError, what = " + what + ", extra = " + extra);
        mp.reset();
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Chronos.Logdebug(logname, "OnCompletion !!!");
        if (randomPlay){
            stopPlayer();
            String song = songs.getNext();
            try {
                setTrack(song);
                startPlayer(2,song);
            } catch (IOException e){
              logSetDataSourceIssue(song,e);
            }
        }
    }

    private boolean startPlayer(int retryCount, String path) {
        if (retryCount<0 || path == null){
            mediaPlayer.reset();
            return false;
        }

        mediaPlayer.start();
        try {
            Thread.sleep(50);
            if (mediaPlayer.isPlaying()) {
                return true;
            } else {
                if (retryCount > 0) {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(path);
                    return startPlayer(retryCount - 1, path);
                } else return false;
            }
        } catch (InterruptedException | IOException e) {
            mediaPlayer.reset();
            return false;
        }
    }

    public boolean  startPlayer(int retryCount, String path, int retryCount2, String alternatePath) {
        randomPlay = false;
        if (startPlayer(retryCount, path)) {
            return true;
        } else {
            if (alternatePath != null && retryCount2 > 0)
                return startPlayer(retryCount2, alternatePath);
            else return false;
        }
    }

    public void startRandom(Randomizer<String> songs) {
        randomPlay = true;
        this.songs = songs;
        String song = songs.getNext();
        try {
            setTrack(song);
            startPlayer(2,song);
        } catch (IOException e) {
            logSetDataSourceIssue(song,e);
        }



    }

    @Override
    public void onDelayedActionBefore(Object... obj) {

    }

    @Override
    public void onDelayedAction(Object... obj) {
        if (obj != null && obj.length == 1 && obj[0] != null && obj[0] instanceof String) {
            String path = (String)obj[0];
            try {
                Log.i(logname, "Incorporated AudioDurationVariator YEEEPPPP!!!!");
                //stopPlayer();
                setTrack(path);
                startPlayer(1,path,0,null);
            } catch (IOException e){
                logSetDataSourceIssue(path,e);
            }
        }
    }

    @Override
    public void onDelayedActionAfter(Object... objects) {
        stopPlayer();
        stopAudioDurationVariator();
        stopAudioVolumeVariator();
    }

    public void setVariableDurationAndStart(String path, long durationInMilliSeconds, int repeat){
        setVariableDuration(path,durationInMilliSeconds,repeat);
        audioDurationVariator.start();
    }


    private void setVariableDuration(String path, long durationInMilliSeconds, int repeat){
        audioDurationVariator = new DelayedActionRunner<>(RUNNER,Instance(),path);
        audioDurationVariator.setSleepStepRunner(durationInMilliSeconds);
        audioDurationVariator.setRunnerDuration((repeat < 0 ? -1 : (repeat+1)*durationInMilliSeconds));
    }

//    public void startVariableDuration(){
//        if (audioDurationVariator != null && audioDurationVariator.getState() == Thread.State.NEW)
//            audioDurationVariator.start();
//    }

    public void stopAudioDurationVariator(){
        if (audioDurationVariator != null) {
            audioDurationVariator.safeStop();
            audioDurationVariator = null;
        }
    }

    private void setVariableVolume(double minVolume, double maxVolume, int  time, int step){
        audioVolumeVariator = new DelayedActionRunner<>(RUNNER,new AudioVolumeVariator(),Instance().getAudioManager(),minVolume,maxVolume,(double)(time/step));
        audioVolumeVariator.setRunnerDuration(time*1000);
        audioVolumeVariator.setSleepStepRunner(step*1000);
    }

    public void setVariableVolumeAndStart(double minVolume, double maxVolume, int  time, int step){
        setVariableVolume(minVolume,maxVolume,time,step);
        audioVolumeVariator.start();
    }

//    public void startVolumeVariable() {
//        if (audioVolumeVariator != null && audioVolumeVariator.getState() == Thread.State.NEW)
//            audioVolumeVariator.start();
//    }

    public void stopAudioVolumeVariator(){
        if (audioVolumeVariator != null) {
            audioVolumeVariator.safeStop();
            audioVolumeVariator = null;
        }
    }

    public void stopAll(){
        stopPlayer();
        stopVibrator();
        stopAudioVolumeVariator();
        stopAudioDurationVariator();
    }

    public void setFixedVolumeLevel(float level) {
        int max = CommonMediaPlayer.Instance().getAudioManager().getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (level * max), AudioManager.FLAG_ALLOW_RINGER_MODES);
    }

    public void startVibrator(long duration){
        long[] pattern = new long[]{500,1500};
        int[] amplitude = new int[]{255,255};
        vibratorRunner = new DelayedActionRunner<>(DelayedActionRunner.TYPE.RUNNER,
                new VibratorVariator(),vibrator,pattern,amplitude,0);
        vibratorRunner.setRunnerDuration(duration);
        vibratorRunner.setDelay(0);
        vibratorRunner.setSleepStepRunner(1000);
        vibratorRunner.start();
    }

    public void stopVibrator() {
        if (vibrator != null)
            vibrator.cancel();
        if (vibratorRunner != null)
            vibratorRunner.safeStop();

    }

    public boolean isVibrating(){
        return vibratorRunner != null && vibratorRunner.isAlive();
    }

    public int getPosition(){
        if (mediaPlayer != null && mediaPlayer.isPlaying()) return mediaPlayer.getCurrentPosition();
        else return -1;
    }

    private void logSetDataSourceIssue(String song, Exception e){
        Chronos.Logdebug(logname,"can't set data source "+ song,e);
    }

    private class PauseDelay implements DelayedActionListener {
        private MediaPlayer mediaPlayer;
        @Override
        public void onDelayedActionBefore(Object... objects) {
            mediaPlayer = (MediaPlayer)objects[0];
            mediaPlayer.pause();
        }

        @Override
        public void onDelayedAction(Object... objects) {

        }

        @Override
        public void onDelayedActionAfter(Object... objects) {
            mediaPlayer.start();
        }
    }
}
