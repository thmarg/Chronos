/*
 * ${NAME}
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.testchronos.testcore;

import org.testng.annotations.Test;
import tm.android.chronos.util.Pwrapper;

import java.util.Random;

import static org.testng.Assert.*;


public class PwrapperTest {

	@Test
	public void testValue() throws Exception {
		Random rd = new Random(System.currentTimeMillis());
			Pwrapper<Integer> pw = new Pwrapper<Integer>(0);
		int n;
		for (int i=0;i<10000;i++) {
			n=rd.nextInt();
			pw.setValue(n);
			assertTrue(pw.value() == n);
		}
	}


	@Test
	public void testAdd() throws Exception {
		Random rd = new Random(System.currentTimeMillis());
		Pwrapper<Integer> pw = new Pwrapper<Integer>(0);
		int n,acc=0;
		for (int i=0;i<10000;i++) {
			n=rd.nextInt(1000);
			acc+=n;
			pw.add(n);
			assertTrue(pw.value() == acc);
		}
	}

	@Test
	public void testSub() throws Exception {
		Random rd = new Random(System.currentTimeMillis());
		int n,acc=1158796350;
		Pwrapper<Integer> pw = new Pwrapper<Integer>(acc);

		for (int i=0;i<10000;i++) {
			n=rd.nextInt(1000);
			acc-=n;
			pw.sub(n);
			assertTrue(pw.value() ==acc);
		}
	}

	@Test
	public void testFormat() throws Exception {
		double d = 2532.001205;
		Pwrapper<Double> pw = new Pwrapper<Double>(d);
		assertTrue(pw.format(2,true).equals("2532"));
		assertTrue(pw.format(2,false).equals("2532.00"));
		assertTrue(pw.format(5,true).equals("2532.0012"));
		assertTrue(pw.format(5,false).equals("2532.00120"));
		assertTrue(pw.format(8,false).equals("2532.00120500"));
		assertTrue(pw.format(8,true).equals("2532.001205"));

	}
}