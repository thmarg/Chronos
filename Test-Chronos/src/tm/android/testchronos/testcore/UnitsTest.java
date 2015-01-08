/*
 *   UnitsTest
 *
 *   Copyright (c) 2014 Thierry Margenstern under MIT license
 *   http://opensource.org/licenses/MIT
 */

package tm.android.testchronos.testcore;

import org.testng.annotations.Test;
import tm.android.chronos.core.Units;
import tm.android.chronos.core.Units.*;

import java.util.Random;



import static org.testng.Assert.*;

public class UnitsTest {
double precision = 0.000000000001;
    @Test
    public void testGetLength() throws Exception {
        double converted=0;
        Random random = new Random(System.currentTimeMillis());
        double val=0;
        for (int i=0; i<1000000;i++)
            val = random.nextDouble();
            for(LENGTH_UNIT origin : Units.getUnitLenghtList())
                for (LENGTH_UNIT destination :Units.getUnitLenghtList())
                    if (origin!=destination){
                        converted = Units.getLength(val,origin,destination);
                        assertTrue(Math.abs(val - Units.getLength(converted, destination, origin)) < precision);
                    }
    }

    @Test
    public void testGetSpeed() throws Exception {
        double converted=0;
        Random random = new Random(System.currentTimeMillis());
        double length=0;
        int time = 0;
        for (int i=0; i<1000000;i++){
            length = random.nextInt(1000)+1;
            time = random.nextInt(60000)+1;
            for(LENGTH_UNIT origin : Units.getUnitLenghtList())
                    for (SPEED_UNIT speedUnit : SPEED_UNIT.values()) {
                        converted = Units.getSpeed(length, origin, time, speedUnit);
                        double reconv =  Units.getLength(converted*(time/speedUnit.getTimeUnit().getValue()),speedUnit.getLengthUnit(),origin);
                        assertTrue(Math.abs(reconv-length)<precision);
                    }
        }
    }
}