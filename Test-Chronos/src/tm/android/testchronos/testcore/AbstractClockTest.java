/*  AbstractClockTest
 *
 *  Copyright (c) 2014 Thierry Margenstern under MIT license
 *  http://opensource.org/licenses/MIT
 */
package tm.android.testchronos.testcore;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import tm.android.chronos.core.AbstractClock;

import static org.testng.Assert.*;

public class AbstractClockTest {
    AbstractClock abstractClock ;
    @BeforeMethod
    public void setUp() throws Exception {
        abstractClock = AbstractClock.getAbstractClock();

    }


    @Test
    public void testName(){
        abstractClock.setName("John Doe");
        assertEquals("John Doe", abstractClock.getName());
    }
    @Test
    public void testStart() throws Exception {

        assertTrue(!abstractClock.isRunning());
        assertTrue(!abstractClock.isStopped());
        assertTrue(abstractClock.isWaitingStart());

        abstractClock.start(0);

        assertTrue(abstractClock.isRunning());
        assertTrue(!abstractClock.isStopped());
        assertTrue(!abstractClock.isWaitingStart());

    }

    @Test
    public void testStop() throws Exception {
        abstractClock.start(0);
        abstractClock.stopTime(1);
        assertTrue(!abstractClock.isRunning());
        assertTrue(!abstractClock.isWaitingStart());
        assertTrue(abstractClock.isStopped());
        if (abstractClock.hasError()){
            System.out.println(abstractClock.getError());
        }
    }

    @Test
    public void testReset() throws Exception {
        abstractClock.start(0);
        abstractClock.stopTime(1);
        abstractClock.reset();
        assertTrue(!abstractClock.isRunning());
        assertTrue(abstractClock.isWaitingStart());
        assertTrue(!abstractClock.isStopped());
        abstractClock.stopTime(1);
    }

    @Test
    public void testGetTime() throws Exception {
            abstractClock.start(System.currentTimeMillis());

            Thread.sleep(1000);

            abstractClock.stopTime(System.currentTimeMillis());
            assertTrue(Math.abs(1000-abstractClock.getTime().getInternal())<=2);
    }
}