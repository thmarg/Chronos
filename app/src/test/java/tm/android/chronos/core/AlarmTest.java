package tm.android.chronos.core;


import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AlarmTest {
    private Alarm alarm;
    private long startTime;

    @Before
    public void init() {
        startTime = System.currentTimeMillis();
        alarm = new Alarm();
    }

    @Test
    public void isWaitingStart() {

        alarm.setEndTime(startTime + 60000);
        assertTrue(alarm.isWaitingStart());

    }

    @Test
    public void start() {
        alarm = new Alarm();
        alarm.setEndTime(startTime + 60000);
        alarm.start(startTime + 20);
        assertFalse(alarm.isWaitingStart());
        assertTrue(alarm.isRunning());
        assertFalse(alarm.isStopped());
    }


    @Test
    public void stopTime() {
        alarm = new Alarm();
        alarm.setEndTime(startTime + 60000);
        alarm.start(startTime + 10);
        alarm.stopTime(startTime + 10000);
        assertFalse(alarm.isRunning());
        assertFalse(alarm.isWaitingStart());
        assertTrue(alarm.isStopped());

    }

    @Test
    public void restart() {
        stopTime();
        alarm.restart();
        assertTrue(alarm.isRunning());
    }


    @Test
    public void modeStopwatch() {
        alarm = new Alarm();
        alarm.setEndTime(startTime + 60000);
        alarm.start(startTime + 10);
        try {
            Thread.sleep(60100);
            assertSame(alarm.getMode(), Alarm.MODE.STOPWATCH);
            assertTrue(alarm.isRunning());
            assertTrue(alarm.getTime().getInternal() > 0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void Full() {

        boolean run = true;

        Alarm alarm = new Alarm();
        long startTime = System.currentTimeMillis();
        alarm.setEndTime(startTime + 20000);
        alarm.start(startTime);
        System.out.println(alarm.getTime().toString());
        while (run) {
            if (System.currentTimeMillis() < alarm.getEndTime()) {
                assertSame(alarm.getMode(), Alarm.MODE.TIMER);

            } else {
                assertSame(alarm.getMode(), Alarm.MODE.STOPWATCH);
                if (System.currentTimeMillis() - alarm.getEndTime() > 10000)
                    run = false;
            }
            assertTrue(alarm.isRunning());
            System.out.println(alarm.getTime().toString().trim());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}