/*
 * TimeSelectionView
 *
 * Copyright (c) 2018 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.uicomponent;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import tm.android.chronos.core.ClockTimer;
import tm.android.chronos.core.Digit;
import tm.android.chronos.core.Units;
import tm.android.chronos.core.ui.AbstractUI;
import tm.android.chronos.core.ui.UIRenderer;
import tm.android.chronos.util.geom.Point;
import tm.android.chronos.util.geom.Segment;

import static tm.android.chronos.core.Units.UPDATE_TYPE.PAINT_ALL;
import static tm.android.chronos.core.Units.UPDATE_TYPE.UPDATE_HEAD_DIGIT;
import static tm.android.chronos.util.geom.Point.SYMMETRY_TYPE.X_AXIS;


/**
 * Analogique watch to select a time or a duration.
 */
public class TimeSelectionView extends AbstractUI {
    private final static double EPSILON = 0.000001;
    public Point localCoordInScreenCoord;
    //private double rotHour;
    public double rotMinute;
    protected Paint paint;
    //Point previousPoint;
    private Rect rect = new Rect();
    private float centerX;
    private float radius;
    //private Segment baseLineHour;
    private Segment baseLineMinute;
    private Segment baseLineSeconds;
    private Segment baselineZeroSegment;
    private Paint paintNeedles;
    private Point screenCoordInLocalCoord;
    private Point localCenter;
    private float centerY;
    private Segment clockRepereSegment;
    private double rotSecond;
    private long[] currentTime;
    //private boolean pmIndicator;
    //private double rot;
    private float radiusNeedles;
    //private String prefix = PreferenceCst.PREFIX_TIMER;


    private ClockTimer clockTimer;

    TimeSelectionView() {
        super(null);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintNeedles = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintNeedles.setColor(Color.RED);
        localCenter = new Point(0, 0, Point.TYPE.XY);
        currentTime = new long[5];
        init(BaseUI.SCREENWIDTH, BaseUI.SCREENHEIGHT);
        clockTimer = new ClockTimer();
    }

    protected void init(int i1, int i2) {

        centerX = i1 / 2;
        if (i1 > i2)
            centerX = i2 / 2;

        centerY = centerX;

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

//        Point A = new Point();
//        A.setRadius(0.1 * radius);
//        A.setAngle(3 * Math.PI / 2);
//        Point B = new Point();
//        B.setRadius(0.6 * radius);
//        B.setAngle(Math.PI / 2);
//        baseLineHour = new Segment(A, B);

        Point C = new Point();
        C.setRadius(0.1 * radius);
        C.setAngle(3 * Math.PI / 2);
        Point D = new Point();
        D.setRadius(0.82 * radius);
        D.setAngle(Math.PI / 2);

        baseLineMinute = new Segment(C, D);
        radiusNeedles = 0.82f * radius;

        baselineZeroSegment = baseLineMinute.getTranslatedParallel(0);
        baseLineSeconds = baseLineMinute.getTranslatedParallel(0);
        baseLineSeconds.rotate(Point.ZERO, -EPSILON);
        baseLineMinute.rotate(Point.ZERO, -EPSILON);
    }

    @Override
    public void renderOnScreen(Canvas canvas) {
    }

    @Override
    public boolean drawMe() {
        return true;
    }

    @Override
    public void renderOnCanvas(Canvas canvas) {
        synchronized (updateTypeList) {
            for (Units.UPDATE_TYPE updateType : updateTypeList) {
                switch (updateType) {
                    case PAINT_ALL:
                        drawAll(canvas);
                        break;
                    case UPDATE_HEAD_DIGIT:
                        eraseNeedles(canvas);
                        if (clockTimer.isRunning()) {
                            currentTime = clockTimer.getTime().toArray();
                            updateNeedlesPosition(currentTime[2], currentTime[3]);
                            drawNeedles(baseLineSeconds, canvas, rotSecond);
                        }
                        drawNeedles(baseLineMinute, canvas, rotMinute);

                        updateDigits();
                        drawDigits(canvas, currentTime[2], currentTime[3]);

                }
            }
        }

        clearUpdateType();
        if (clockTimer.isRunning())
                addUpdateType(UPDATE_HEAD_DIGIT);

    }

    @Override
    public void renderOnCache() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) clockTimer;
    }


    @Override
    public void doNonUIAction() {
    }

    @Override
    public float getComputedHeight() {
        return BaseUI.SCREENWIDTH;
    }

    @Override
    public void sizeChangedFor(UIRenderer renderer) {

    }

    private void drawAll(Canvas canvas) {
        if (canvas == null)
            return;
        rect = canvas.getClipBounds();
        paint.setColor(Color.BLACK);
        canvas.drawRect(rect, paint);
        drawBackground(canvas);
        drawCenterPoint(canvas);
        drawNeedles(baseLineMinute, canvas, 0.0);
        updateDigits();
        drawDigits(canvas, currentTime[2],currentTime[3]);

    }

    private void updateDigits() {
        if (!clockTimer.isRunning()) {
            currentTime[3] = 0;
            currentTime[2] = Math.round((((5 * Math.PI / 2 - baseLineMinute.getB().getAngle()) % (2 * Math.PI) + EPSILON) * 30 / Math.PI));
        }

//        currentTime[1] = (long) (((5 * Math.PI / 2 - baseLineHour.getB().getAngle()) % (2 * Math.PI)) * 6 / Math.PI);
//        currentTime[2] = Math.round(((5 * Math.PI / 2 - baseLineMinute.getB().getAngle()) % (2 * Math.PI)) * 30 / Math.PI);
//        if (currentTime[2] == 60) {
//            if (rot > 0) {
//                currentTime[1]--;
//                currentTime[2] = 59;
//            } else {
//                currentTime[1]++;
//                currentTime[2] = 0;
//
//            }
//        }
//        if (pmIndicator)
//            currentTime[1] += 12;
//        if (currentTime[1] == 24)
//            currentTime[1] = 12;
    }

    private void drawCenterPoint(Canvas canvas) {
        paint.setColor(Color.BLACK);
        float x = (float) localCenter.getChangedCoordinates(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).getX();
        float y = (float) localCenter.getChangedCoordinates(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).getY();
        canvas.drawCircle(x, y, 3, paint);
    }

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
        if (clockTimer.isRunning())
            segment = segment.getRotated(localCenter, rotationAngle);
        else
            segment.rotate(localCenter, rotationAngle);
        drawNeedles(segment, canvas);

    }

    private void drawNeedles(Segment segment, Canvas canvas) {
        canvas.drawLines(segment.getChangedCoordinate(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).asCanvasLine(), paintNeedles);
        for (int j = 1; j < 3; j++) {
            canvas.drawLines(segment.getTranslatedParallel(j).getChangedCoordinate(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).asCanvasLine(), paintNeedles);
            canvas.drawLines(segment.getTranslatedParallel(-j).getChangedCoordinate(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).asCanvasLine(), paintNeedles);
        }
    }

    private void eraseNeedles(Canvas canvas) {
        paint.setColor(Color.DKGRAY);
        canvas.drawCircle(centerX, centerY, radiusNeedles, paint);
    }

    private void updateNeedlesPosition(long minute, long secondes) {
        rotMinute = (2 * Math.PI - minute * Math.PI / 30) % (2 * Math.PI) - secondes * Math.PI / 1800;
        rotSecond = (2 * Math.PI - secondes * Math.PI / 30) % (2 * Math.PI);
    }

    private void drawDigits(Canvas canvas, long minutes, long seconds) {
        if (clockTimer.isStopped() && minutes == 60)
                minutes = 0;
        String t = minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
        BaseUI.getPaintWhiteRigthDigitSize().getTextBounds(t, 0, t.length(), rect);
        Point position = new Point(rect.right / 2, -centerX / 3, Point.TYPE.XY);
        float[] pos = position.getChangedCoordinates(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).asCanvasPoint();
        canvas.drawText(t, pos[0], pos[1], BaseUI.getPaintWhiteRigthDigitSize());
    }

    Digit getTimeFromScreen() {
        return Digit.split(60000 * currentTime[2]);
    }

    public void startTimer(long startTime) {
        clockTimer.setDuration(getTimeFromScreen().getInternal());
        clockTimer.start(startTime);
        baseLineSeconds = baselineZeroSegment.getTranslatedParallel(0);
        baseLineMinute = baselineZeroSegment.getTranslatedParallel(0);
        addUpdateType(UPDATE_HEAD_DIGIT);
    }


    public void stopTimer() {
        clockTimer.stopTime(System.currentTimeMillis());

    }

    public void resetTimer() {
        if (clockTimer.isStopped() || clockTimer.isWaitingStart()) {
            clockTimer.reset();
            baseLineMinute = baselineZeroSegment.getTranslatedParallel(0).getRotated(localCenter, -EPSILON);
            baseLineSeconds = baselineZeroSegment.getTranslatedParallel(0).getRotated(localCenter, -EPSILON);
            rotMinute = 0;
            rotSecond = 0;
            currentTime[2] = 0;
            currentTime[3] = 0;
            updateNeedlesPosition(0, 0);
            addUpdateType(PAINT_ALL);
            addUpdateType(UPDATE_HEAD_DIGIT);
        }
    }



    void setClockTimer(ClockTimer clkt) {
        clockTimer = clkt;
    }
}
