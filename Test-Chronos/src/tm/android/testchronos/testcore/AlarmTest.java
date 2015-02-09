/*
 * AlarmTest
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.testchronos.testcore;

import org.testng.annotations.Test;
import tm.android.chronos.core.Alarm;

import static org.testng.Assert.*;


/**
 * Test class Alarm
 */
public class AlarmTest {

	@Test
	public void testTime() throws Exception {
		Alarm alarm = new Alarm();
		long now = System.currentTimeMillis();
		alarm.stopTime(now+10000);
		alarm.start(now);
		try {
			Thread.sleep(5010);
			//System.out.println("Internal remaining time -> " + alarm.getTime().getInternal());
			assertTrue(alarm.getTime().getInternal() < 5000);
			Thread.sleep(5000);
			assertTrue(alarm.getTime().getInternal()==0 && alarm.isStopped());
		} catch (InterruptedException e){
			e.printStackTrace();
		}

		alarm.reset();
		assertTrue(alarm.isWaitingStart());

		now = System.currentTimeMillis();
		alarm.stopTime(now+5000);
		alarm.start(now);
		try {
			while (alarm.isRunning() && alarm.getTime().getInternal()!=0) {
				Thread.sleep(20);
			}
			assertTrue(alarm.isStopped()&&alarm.getTime().getInternal()==0);
			now = System.currentTimeMillis();
			assertTrue(now>=(alarm.getStartTime()+5000));
			//System.out.println(now + " >= " +(alarm.getStartTime()+5000));
		} catch (InterruptedException e){
			e.printStackTrace();
		}
	}

	@Test
	public void testName(){
		Alarm alarm = new Alarm();
		String s = "Name";
		alarm.setName(s);
		assertEquals(alarm.getName(),s);
	}

	@Test
	public void testId(){
		Alarm alarm = new Alarm();
		long id = 6987532156l;
		alarm.setId(id);
		assertEquals(alarm.getId(),id);
	}

	@Test
	public void testStartTime(){
		Alarm alarm = new Alarm();
		long now = System.currentTimeMillis();
		alarm.setStartTime(now);
		assertEquals(alarm.getStartTime(),now);
	}




}