/*
 * TimeSelector
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.uicomponent;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import tm.android.chronos.util.DateTime;
import tm.android.chronos.util.geom.Point;

import static tm.android.chronos.util.geom.Point.SYMMETRY_TYPE.X_AXIS;


/**
 * Created by thmarg on 14/02/15.
 */
public class TimeSelector extends Watch {

    private final static double EPSILON = 0.000001;
    private double rotMinute;
    private double rotHour;
    private long[] currentTime;
    private boolean pmIndicator;// true->PM false->AM
    private double rot;

    public TimeSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        currentTime = new long[5];
    }

    @Override
    protected void init(int i1, int i2) {
        super.init(i1, i2);
        DateTime dateTime = new DateTime();
        int hour = dateTime.get(DateTime.HOURS);
        pmIndicator = hour >= 12;
        if (hour >= 12)
            hour -= 12;

        int minutes = dateTime.get(DateTime.MINUTES);
        updateNeedlesPosition(hour, minutes);
    }

    private void updateDigits() {

        currentTime[1] = (long) (((5 * Math.PI / 2 - baseLineHour.getB().getAngle()) % (2 * Math.PI)) * 6 / Math.PI);
        currentTime[2] = Math.round(((5 * Math.PI / 2 - baseLineMinute.getB().getAngle()) % (2 * Math.PI)) * 30 / Math.PI);
        if (currentTime[2] == 60) {
            if (rot > 0) {
                currentTime[1]--;
                currentTime[2] = 59;
            } else {
                currentTime[1]++;
                currentTime[2] = 0;

            }
        }
        if (pmIndicator)
            currentTime[1] += 12;
        if (currentTime[1] == 24)
            currentTime[1] = 12;
    }

    private void updateNeedlesPosition(int hour, int minute) {
        rotMinute = (2 * Math.PI - minute * Math.PI / 30) % (2 * Math.PI);
        rotHour = (2 * Math.PI - hour * Math.PI / 6) % (2 * Math.PI) - minute * Math.PI / 360;

    }

    private void drawDigits(Canvas canvas, long hours, long minutes) {
        String t = hours + ":" + (minutes < 10 ? "0" : "") + minutes;
        BaseUI.getPaintWhiteRigthDigitSize().getTextBounds(t, 0, t.length(), rect);
        Point position = new Point(rect.right / 2, -centerX / 3, Point.TYPE.XY);
        float[] pos = position.getChangedCoordinates(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).asCanvasPoint();
        canvas.drawText(t, pos[0], pos[1], BaseUI.getPaintWhiteRigthDigitSize());
        canvas.drawText((pmIndicator ? "pm" : "am"), pos[0], pos[1] + 50, BaseUI.getPaintNormalTextWhiteRigth());
    }

    @Override
    protected void drawAll(Canvas canvas) {
        super.drawAll(canvas);
        drawNeedles(baseLineMinute, canvas, rotMinute);
        drawNeedles(baseLineHour, canvas, rotHour);
        updateDigits();
        drawDigits(canvas, currentTime[1], currentTime[2]);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                previousPoint = new Point(event.getX(), event.getY(), Point.TYPE.XY).getSymmetrical(X_AXIS, Point.ZERO).getChangedCoordinates(localCoordInScreenCoord);
                return true;
            case MotionEvent.ACTION_UP:
                previousPoint = null;
                return true;
            case MotionEvent.ACTION_MOVE:
                Point currentPoint = new Point(event.getX(), event.getY(), Point.TYPE.XY).getSymmetrical(X_AXIS, Point.ZERO).getChangedCoordinates(localCoordInScreenCoord);
                if (Math.abs(currentPoint.getAngle() - previousPoint.getAngle()) >= 1) {
                    previousPoint = currentPoint;
                    return true;
                }
                if (Math.abs(currentPoint.getAngle() - previousPoint.getAngle()) < 0.001)
                    return true;
                double rot = currentPoint.getAngle() - previousPoint.getAngle();
                if (currentPoint.getRadius() >= radius) {
                    rotMinute = rot;
                    rotHour = rotMinute / 12;
                } else {
                    rotHour = rot;
                    rotMinute = rotHour * 12;
                }
                if (Math.abs(baseLineHour.getB().getAngle() - Math.PI / 2) < 0.1 && Math.abs((baseLineHour.getB().getAngle() + rotHour) % (2 * Math.PI) - Math.PI / 2) < 0.1)
                    if ((baseLineHour.getB().getAngle() > Math.PI / 2 && (baseLineHour.getB().getAngle() + rotHour) % (2 * Math.PI) <= Math.PI / 2) ||
                            baseLineHour.getB().getAngle() <= Math.PI / 2 && (baseLineHour.getB().getAngle() + rotHour) % (2 * Math.PI) > Math.PI / 2)
                        pmIndicator = !pmIndicator;
                updateDigits();
                Canvas canvas = getHolder().lockCanvas();
                drawAll(canvas);
                getHolder().unlockCanvasAndPost(canvas);

                previousPoint = currentPoint;
                return true;
        }
        return super.onTouchEvent(event);
    }
}
