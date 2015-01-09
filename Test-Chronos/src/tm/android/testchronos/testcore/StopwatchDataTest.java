/*
 *   $NAME
 *  *
 *  *  Copyright (c) 2014 Thierry Margenstern under MIT license
 *  *  http://opensource.org/licenses/MIT
 *
 */

package tm.android.testchronos.testcore;

import org.testng.annotations.Test;
import tm.android.chronos.core.StopwatchData;
import tm.android.chronos.core.StopwatchDataRow;
import tm.android.chronos.core.Units;

import java.util.ArrayDeque;
import java.util.Date;

import static org.testng.Assert.*;

public class StopwatchDataTest {


    @Test
    public void addgetRowTest(){
        long now = System.currentTimeMillis();
        StopwatchData stopwatchData = new StopwatchData(Units.CHRONO_TYPE.LAPS,"Chrono-1",now);
        stopwatchData.add(12000);
        stopwatchData.add(25497);
        stopwatchData.add(23590);
        stopwatchData.add(200687);



        assertEquals(stopwatchData.getName(), "Chrono-1");
        assertEquals(stopwatchData.getCreationDate(),now);

//        ArrayDeque<StopwatchDataRow> timeList= stopwatchData.getTimeList();
//        assertEquals("00:12:000", timeList.pollFirst().getTimeToString().trim());
//        assertEquals("00:25:497",timeList.pollFirst().getTimeToString().trim());
//        assertEquals("00:23:590",timeList.pollFirst().getTimeToString().trim());
//        assertEquals("03:20:687",timeList.pollFirst().getTimeToString().trim());

    }

}