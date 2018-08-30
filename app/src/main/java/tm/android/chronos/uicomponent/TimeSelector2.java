/*
 * Watch
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.uicomponent;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import tm.android.chronos.activity.Chronos;
import tm.android.chronos.util.geom.Point;
import tm.android.chronos.util.geom.Segment;

import java.util.Calendar;

import static tm.android.chronos.util.geom.Point.SYMMETRY_TYPE.X_AXIS;


/**
 * Analogique watch to select a time
 */
public class TimeSelector2 extends View  {

    private float centerX;
    private float radius;
    private Segment baseLineHour;
    private Segment baseLineMinute;
    protected Paint paint;
    private Paint paintNeedles;
    private Point screenCoordInLocalCoord;
    private Point localCoordInScreenCoord;
    private Point localCenter;
    private Point previousPoint;
    Rect rect = new Rect();
    private float centerY;
    private Segment clockRepereSegment;

    private double rotMinute;
    private double rotHour;
    private long[] currentTime;
    private boolean pmIndicator;// true->PM false->AM
    private double rot;


    public TimeSelector2(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintNeedles = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintNeedles.setColor(Color.RED);
        //getHolder().addCallback(this);
        localCenter = new Point(0, 0, Point.TYPE.XY);
        currentTime = new long[5];
        init(BaseUI.SCREENWIDTH, BaseUI.SCREENWIDTH-100);
    }

    private void init(int width, int height) {
        Log.i(Chronos.name+"-TimeSelector2","init");
        centerX = width / 2;

        centerY = height/2;

        screenCoordInLocalCoord = new Point(-centerX, centerY, Point.TYPE.XY);
        localCoordInScreenCoord = new Point(centerX, -centerY, Point.TYPE.XY);
        radius = centerX - 100;

        Point E = new Point();
        E.setRadius(0.85 * radius);
        E.setAngle(0);
        Point F = new Point();
        F.setRadius(0.95 * radius);
        F.setAngle(0);
        clockRepereSegment = new Segment(E, F);

        Point A = new Point();
        A.setRadius(0.1 * radius);
        A.setAngle(3 * Math.PI / 2);
        Point B = new Point();
        B.setRadius(0.6 * radius);
        B.setAngle(Math.PI / 2);
        baseLineHour = new Segment(A, B);

        Point C = new Point();
        C.setX(0.1 * radius);
        C.setAngle(3 * Math.PI / 2);
        Point D = new Point();
        D.setRadius(0.82 * radius);
        D.setAngle(Math.PI / 2);

        baseLineMinute = new Segment(C, D);

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        pmIndicator = hour >= 12;
        if (hour >= 12)
            hour -= 12;

        int minutes = calendar.get(Calendar.MINUTE);
        updateNeedlesPosition(hour, minutes);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawAll(canvas);
    }

    private void drawAll(Canvas canvas) {
        if (canvas == null)
            return;
        rect = canvas.getClipBounds();
        paint.setColor(Color.BLACK);
        canvas.drawRect(rect, paint);
        drawBackground(canvas);

        drawNeedles(baseLineMinute, canvas, rotMinute);
        drawNeedles(baseLineHour, canvas, rotHour);
        updateDigits();
        drawDigits(canvas, currentTime[1], currentTime[2]);
    }

    private void drawDigits(Canvas canvas, long hours, long minutes) {
        String t = hours + ":" + (minutes < 10 ? "0" : "") + minutes;
        BaseUI.getPaintWhiteRigthDigitSize().getTextBounds(t, 0, t.length(), rect);
        Point position = new Point(rect.right / 2, -centerX / 3, Point.TYPE.XY);
        float[] pos = position.getChangedCoordinates(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).asCanvasPoint();
        canvas.drawText(t, pos[0], pos[1], BaseUI.getPaintWhiteRigthDigitSize());
        canvas.drawText((pmIndicator ? "pm" : "am"), pos[0], pos[1] + 50, BaseUI.getPaintNormalTextWhiteRigth());
    }

//    protected void drawCenterPoint(Canvas canvas) {
//        paint.setColor(Color.BLACK);
//        float x = (float) localCenter.getChangedCoordinates(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).getX();
//        float y = (float) localCenter.getChangedCoordinates(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).getY();
//        canvas.drawCircle(x, y, 3, paint);
//    }

    private void drawBackground(Canvas canvas) {
        // All computation are made in local coordinate (based on 0,0) and computed to screen coordinate for use in canvas.drawSomething
        paint.setColor(Color.BLUE);
        canvas.drawCircle(centerX, centerY, radius, paint);
        paint.setColor(Color.DKGRAY);
        canvas.drawCircle(centerX, centerY, radius - 5, paint);

        paint.setColor(Color.YELLOW);
        for (int i = 0; i < 12; i += 1) {
            canvas.drawLines(clockRepereSegment.getChangedCoordinate(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).asCanvasLine(), paint);
            for (int j = 1; j < 5; j++) {
                canvas.drawLines(clockRepereSegment.getTranslatedParallel(j).getChangedCoordinate(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).asCanvasLine(), paint);
                canvas.drawLines(clockRepereSegment.getTranslatedParallel(-j).getChangedCoordinate(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).asCanvasLine(), paint);
            }
            clockRepereSegment.rotate(localCenter, Math.PI / 6);
        }
    }

    private void drawNeedles(Segment segment, Canvas canvas, double rotationAngle) {
        segment.rotate(localCenter, rotationAngle);

        for (int j = 0; j < 3; j++) {
            canvas.drawLines(segment.getTranslatedParallel(j).getChangedCoordinate(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).asCanvasLine(), paintNeedles);
            canvas.drawLines(segment.getTranslatedParallel(-j).getChangedCoordinate(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).asCanvasLine(), paintNeedles);
        }
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


    @Override
    public boolean performClick() {
        super.performClick();
        return false;
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
                rot = currentPoint.getAngle() - previousPoint.getAngle();
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

                previousPoint = currentPoint;
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

}
