package tm.android.chronos.audio.test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tm.android.chronos.audio.CommonMediaPlayer;

import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class CommonMediaPlayerTest {
    private Context appContext;


    @Before
    public void init(){
        appContext = InstrumentationRegistry.getTargetContext();
    }


    @Test
    public void instance() {
        CommonMediaPlayer.build(appContext);
        assertNotNull(CommonMediaPlayer.Instance());
    }


    @Test
    public void getThreadStateAndReuse() {
//        CommonMediaPlayer.build(appContext);
//        assertSame(CommonMediaPlayer.Instance().getThreadState(),Thread.State.RUNNABLE);
//        CommonMediaPlayer.Instance().releasePlayer();
//        assertSame(CommonMediaPlayer.Instance().getThreadState(),Thread.State.TERMINATED);
//        CommonMediaPlayer.build(appContext);
//        assertSame(CommonMediaPlayer.Instance().getThreadState(),Thread.State.RUNNABLE);

    }


    @Test
    public void getAudioManager() {
        CommonMediaPlayer.build(appContext);
        assertNotNull(CommonMediaPlayer.Instance().getAudioManager());
    }



    @Test
    public void play() throws IOException {
        String datSource = "../resources/Celebration Of The Lizard - A Little Game.mp3";
        CommonMediaPlayer.build(appContext);
        CommonMediaPlayer.Instance().setTrack(datSource);
        CommonMediaPlayer.Instance().startPlayer(1,datSource,0,null);
        assertTrue(CommonMediaPlayer.Instance().isPlaying());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        CommonMediaPlayer.Instance().stopPlayer();
        assertFalse(CommonMediaPlayer.Instance().isPlaying());

        CommonMediaPlayer.Instance().setTrack("../resources/Celebration Of The Lizard - A Little Game.mp3");
        CommonMediaPlayer.Instance().startPlayer(1,datSource,0,null);
        assertTrue(CommonMediaPlayer.Instance().isPlaying());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        CommonMediaPlayer.Instance().stopPlayer();
        assertFalse(CommonMediaPlayer.Instance().isPlaying());
        CommonMediaPlayer.Instance().releasePlayer();
    }

    @After
    public void end(){
        CommonMediaPlayer.Instance().releasePlayer();
    }

}