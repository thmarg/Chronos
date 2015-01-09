/*  Clock
 *
 *  Copyright (c) 2014 Thierry Margenstern under MIT license
 *  http://opensource.org/licenses/MIT
 */
package tm.android.testchronos.testcore;

import org.testng.annotations.Test;
import tm.android.chronos.core.Digit;

import java.util.Random;

import static org.testng.Assert.*;

public class DigitTest {

    @Test
    public void testAdd_Sub_MillisSecondes() throws Exception {
        Digit digit = new Digit(1);
        assertEquals(new long[]{0, 0, 0, 0, 1}, digit.toArray());
        digit.addMillisSeconds(999);
        assertEquals(new long[]{0, 0, 0, 1, 0}, digit.toArray());
        digit.addMillisSeconds(59000);
        assertEquals(new long[]{0, 0, 1, 0, 0}, digit.toArray());
        digit.addMillisSeconds(3540000);
        assertEquals(new long[]{0, 1, 0, 0, 0}, digit.toArray());
        digit.addMillisSeconds(82800000);
        assertEquals(new long[]{1, 0, 0, 0, 0}, digit.toArray());

        digit.subMilliseconds(82800000);
        assertEquals(new long[]{0, 1, 0, 0, 0}, digit.toArray());
        digit.subMilliseconds(3540000);
        assertEquals(new long[]{0, 0, 1, 0, 0}, digit.toArray());
        digit.subMilliseconds(59000);
        assertEquals(new long[]{0, 0, 0, 1, 0}, digit.toArray());
        digit.subMilliseconds(999);
        assertEquals(new long[]{0, 0, 0, 0, 1}, digit.toArray());
        digit.subMilliseconds(1);
        assertEquals(new long[]{0, 0, 0, 0, 0}, digit.toArray());




    }

    @Test
    public void randomTest(){

        long start = System.currentTimeMillis();
        Digit digit = new Digit(start);
        Random random = new Random(start);
        int rdMax = 864056;
        start= random.nextInt(rdMax);
        int i=0;
        while (digit.getInternal()>start){
            digit.subMilliseconds(start);
            assertEquals(digit.toArray(), Digit.split(digit.getInternal()).toArray());
            start= random.nextInt(rdMax);
        }
        System.out.println("itérations : "+i);

    }


}