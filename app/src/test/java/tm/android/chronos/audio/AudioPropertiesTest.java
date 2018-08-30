package tm.android.chronos.audio;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AudioPropertiesTest {
    private String musicPath;
    private String ringtonePath;


    @Before
    public void init() {
        musicPath = "/emulate/storage/0/Musics/test.ogg";
        ringtonePath = "/emulate/storage/0/ringtone/ringtone_test.ogg";

    }

    @Test
    public void getMusicPath() {
        AudioProperties audioProperties = new AudioProperties(musicPath,null,0,0,0,0,false,false,false);
        assertEquals(musicPath,audioProperties.getMusicPath());
    }

    @Test
    public void setMusicPath() {
        AudioProperties audioProperties = new AudioProperties(musicPath,null,0,0,0,0,false,false,false);
        audioProperties.setMusicPath(ringtonePath);
        assertEquals(ringtonePath,audioProperties.getMusicPath());
    }

    @Test
    public void getRingtonePath() {
        AudioProperties audioProperties = new AudioProperties(null,ringtonePath,0,0,0,0,false,false,false);
        assertEquals(audioProperties.getRingtonePath(),ringtonePath);
    }

    @Test
    public void setRingtonePath() {
        AudioProperties audioProperties = new AudioProperties(null,ringtonePath,0,0,0,0,false,false,false);
        audioProperties.setRingtonePath(musicPath);
        assertEquals(musicPath,audioProperties.getRingtonePath());

    }

    @Test
    public void isRingTone() {
        AudioProperties audioProperties = new AudioProperties(null,ringtonePath,0,0,0,0,false,false,false);
        assertFalse(audioProperties.isRingTone());
    }

    @Test
    public void setRingTone() {
        AudioProperties audioProperties = new AudioProperties(null,ringtonePath,0,0,0,0,false,false,false);
        audioProperties.setRingTone(true);
        assertTrue(audioProperties.isRingTone());
    }

    @Test
    public void getMinVolume() {
        AudioProperties audioProperties = new AudioProperties(null,ringtonePath,0,0,0,0,false,false,false);
        assertEquals(audioProperties.getMinVolumeVariable(),0);
    }

    @Test
    public void setMinVolume() {
        AudioProperties audioProperties = new AudioProperties(null,ringtonePath,0,0,0,0,false,false,false);
        audioProperties.setMinVolumeVariable(5);
        assertEquals(audioProperties.getMinVolumeVariable(),5);
    }

    @Test
    public void getMaxVolume() {
        AudioProperties audioProperties = new AudioProperties(null,ringtonePath,0,0,0,0,false,false,false);
        assertEquals(audioProperties.getMaxVolumeVariable(),0);
    }

    @Test
    public void setMaxVolume() {
        AudioProperties audioProperties = new AudioProperties(null,ringtonePath,0,0,0,0,false,false,false);
        audioProperties.setMaxVolumeVariable(12);
        assertEquals(audioProperties.getMaxVolumeVariable(),12);
    }

    @Test
    public void getDuration() {
        AudioProperties audioProperties = new AudioProperties(null,ringtonePath,0,0,0,0,false,false,false);
        assertEquals(0,audioProperties.getSoundDuration());
    }

    @Test
    public void setDuration() {
        AudioProperties audioProperties = new AudioProperties(null,ringtonePath,0,0,0,0,false,false,false);
        audioProperties.setSoundDuration(1250000000);
        assertEquals(1250000000,audioProperties.getSoundDuration());
    }

    @Test
    public void isVibrate() {
        AudioProperties audioProperties = new AudioProperties(null,ringtonePath,0,0,0,0,false,false,false);
        assertFalse(audioProperties.isVibrate());
    }

    @Test
    public void setVibrate() {
        AudioProperties audioProperties = new AudioProperties(null,ringtonePath,0,0,0,0,false,false,false);
        audioProperties.setVibrate(true);
        assertTrue(audioProperties.isVibrate());
    }

    @Test
    public void isPlaysound() {
        AudioProperties audioProperties = new AudioProperties(null,ringtonePath,0,0,0,0,false,false,false);
        assertFalse(audioProperties.isPlaysound());
    }

    @Test
    public void setPlaysound() {
        AudioProperties audioProperties = new AudioProperties(null,ringtonePath,0,0,0,0,false,false,false);
        //audioProperties.setPlaysound(true);
        assertTrue(audioProperties.isPlaysound());
    }

    @Test
    public void getRepeatCount() {
        AudioProperties audioProperties = new AudioProperties(null,ringtonePath,0,0,0,0,false,false,false);
        assertEquals(0,audioProperties.getSoundRepeatCount());
    }

    @Test
    public void setRepeatCount() {
        AudioProperties audioProperties = new AudioProperties(null,ringtonePath,0,0,0,0,false,false,false);
        audioProperties.setSoundRepeatCount(2);
        assertEquals(2,audioProperties.getSoundRepeatCount());
    }

    @Test
    public void getVolumeType() {
//        AudioProperties audioProperties = new AudioProperties(null,ringtonePath,0,0,0,0,false,false,false);
//        assertEquals(audioProperties.getVolumeType(),AudioProperties.VOLUME_TYPE.FIXED);
//        audioProperties.setMinVolumeVariable(7);
//        audioProperties.setMaxVolumeVariable(10);
//        assertNotEquals(audioProperties.getVolumeType(),AudioProperties.VOLUME_TYPE.FIXED);
//        assertEquals(audioProperties.getVolumeType(),AudioProperties.VOLUME_TYPE.VARIABLE);
//        audioProperties.setMinVolumeVariable(6);
//        audioProperties.setMaxVolumeVariable(6);
//        assertEquals(audioProperties.getVolumeType(),AudioProperties.VOLUME_TYPE.FIXED);
//        assertNotEquals(audioProperties.getVolumeType(),AudioProperties.VOLUME_TYPE.VARIABLE);
    }

    @Test
    public void getDataSource() {
        AudioProperties audioProperties = new AudioProperties(musicPath,ringtonePath,0,0,0,0,false,false,false);
        assertEquals(audioProperties.getDataSource(),musicPath);
        audioProperties.setRingTone(true);
        assertEquals(audioProperties.getDataSource(),ringtonePath);
    }
}