package tm.android.chronos.core;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClockTimerTest {
    private ClockTimer clockTimer;
    @Before
    public void setUp()  {
        clockTimer = new ClockTimer();
    }

    @Test
    public void cycleStartWaitUntilStop() {
        clockTimer.setDuration(5000);
        assertTrue(clockTimer.isWaitingStart());
        clockTimer.start(System.currentTimeMillis());
        assertTrue(clockTimer.isRunning());
        assertFalse(clockTimer.isStopped());
        assertFalse(clockTimer.isWaitingStart());
        try {
            Thread.sleep(4000);
            assertTrue(clockTimer.isRunning());
            assertTrue(clockTimer.getTime().getInternal()<= 1000);
        } catch (Exception e){}

        try {
            Thread.sleep(1500);
            assertTrue(clockTimer.isStopped());
            assertSame(clockTimer.getTime().getInternal(),0L);
        } catch (Exception e){}


    }

    @Test
    public void reset(){
        clockTimer.reset();
        assertTrue(clockTimer.isWaitingStart());
    }

    @Test
    public void nameTest(){
        clockTimer.setName("Me");
        assertEquals(clockTimer.getName(),"Me");
    }

    @Test
    public void forceStop(){
        clockTimer = new ClockTimer();
        clockTimer.setDuration(4000);
        clockTimer.start(System.currentTimeMillis()+2000);
        clockTimer.stopTime(System.currentTimeMillis()+500);
        assertTrue(clockTimer.isWaitingStart());

    }
    @Test
    public void startTimeTest(){

        clockTimer = new ClockTimer();
        clockTimer.setDuration(4000);
        long startTime = System.currentTimeMillis();
        clockTimer.start(System.currentTimeMillis());
        assertEquals(clockTimer.getStartTime(),startTime);
    }

    @Test
    public void durationTest(){
        clockTimer = new ClockTimer();
        clockTimer.setDuration(4000);
        assertEquals(clockTimer.getDuration(),4000);
        clockTimer.start(System.currentTimeMillis());
        clockTimer.setDuration(1000);
        assertEquals(clockTimer.getDuration(),4000);
    }
}