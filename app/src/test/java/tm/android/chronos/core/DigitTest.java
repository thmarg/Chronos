package tm.android.chronos.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DigitTest {

    @SuppressWarnings("NumericOverflow")
    @Test
    public void getStringTime() {
        long n = 365*24*60*60*1000;
        for (long i =0 ; i <= n; i++) {
            assertEquals(i, Digit.getTimeFromString(Digit.split(i).toString().trim()));
        }
    }
}