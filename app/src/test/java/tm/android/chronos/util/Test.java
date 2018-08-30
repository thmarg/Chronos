/*
 * ${NAME}
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.util;

import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.runner.RunWith;
import tm.android.chronos.util.geom.Point;
import tm.android.chronos.util.geom.Segment;


/**
 * Created by thmarg on 19/01/15.
 *
 */
@RunWith(JUnit38ClassRunner.class)
public class Test {

    @org.junit.Test
    public void PointTest() {
        Point p = new Point(20, 20, Point.TYPE.XY);
        System.out.println(p);
        p.setTypeForToString(Point.TYPE.POLAR_DEGREE);
        System.out.println(p);
        p.setTypeForToString(Point.TYPE.POLAR_RADIANT);
        System.out.println(p);

        p.rotate(new Point(0, 0, Point.TYPE.XY), Point.PIs4);
        p.setTypeForToString(Point.TYPE.POLAR_DEGREE);
        System.out.println(p);
        p.setTypeForToString(Point.TYPE.XY);
        System.out.println(p);

        Segment segment = new Segment(new Point(-1, -1, Point.TYPE.XY), new Point(1, 1, Point.TYPE.XY));
        System.out.println(segment);
        segment.rotate(new Point(0, 0, Point.TYPE.XY), 2 * Point.PIs4);
        System.out.println(segment);

    }
}
