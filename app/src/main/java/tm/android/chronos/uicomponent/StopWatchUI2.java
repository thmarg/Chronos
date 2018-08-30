package tm.android.chronos.uicomponent;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import tm.android.chronos.R;
import tm.android.chronos.core.Stopwatch;
import tm.android.chronos.core.StopwatchDataRow;
import tm.android.chronos.core.Units;
import tm.android.chronos.core.ui.AbstractUI;
import tm.android.chronos.core.ui.UIRenderer;
import tm.android.chronos.util.geom.Point;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import static tm.android.chronos.uicomponent.StopWatchUI2.VERTICAL_POSITIONS.*;


public class StopWatchUI2 extends AbstractUI {

     // other text very small
    //private final static int CHRONO_TEXT_SIZE_IN_DP = 30;//  good base size for the chrono digits
    private final static int colorStopped = Color.parseColor("#f16b36");

    private final static int colorBottomGroup = Color.parseColor("#2982cd");
    private final static int colorSepHeadDetail = Color.parseColor("#8e9da0");
    private final static int colorRunning = Color.parseColor("#049d2b");
    private final static int colorSelected = Color.parseColor("#ec4316");
    private static final Paint paintLine;
    protected RectF rect;
    //float chronotextSize = BaseUI.density * CHRONO_TEXT_SIZE_IN_DP;// used by painter
    private Stopwatch stopwatch;
    private float headHeight;
    private float detailsHeight;
    private final static Paint paintBackground;
    private HashMap<VERTICAL_POSITIONS, Float> verticalPositions;
    private static boolean showStartTime = false;
    private static boolean showStartDate = false;
    @SuppressLint("SimpleDateFormat")
    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("[dd:MM:yyyy]");
    @SuppressLint("SimpleDateFormat")
    private final static SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss:SSS");

    protected enum VERTICAL_POSITIONS {Line1, Line2, Line3, Digit, Hline, Data}

    static {
        paintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBackground.setColor(Color.BLACK);

        paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLine.setColor(Color.BLUE);
    }

    StopWatchUI2(Stopwatch stopwatch, Canvas canvas) {
        super(canvas);
        this.stopwatch = stopwatch;

        rect = new RectF();
        verticalPositions = new HashMap<>(5);
        headHeight = computeHeaderPositions(verticalPositions, stopwatch);

        detailsHeight = 0f;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) stopwatch;
    }

    @Override
    public boolean hasNonUIAction() {
        return false;
    }

    @Override
    public void doNonUIAction() {

    }

    @Override
    public void renderOnCanvas(Canvas canvas) {
        for (Units.UPDATE_TYPE updateType : getUpdateTypes())
            switch (updateType) {
                case PAINT_ALL:
                    renderOnCacheAll(canvas);
                    break;
                case UPDATE_CHRONO_TYPE:
                    eraseActual(); // erase the
                    headHeight = computeHeaderPositions(verticalPositions, stopwatch);
                    detailsHeight = 0;
                    renderOnCacheAll(canvas);
                    break;
                case UPDATE_HEAD_LINE1:
                    renderOnCacheHeadLine1(canvas, location, verticalPositions, stopwatch, rect, true);
                    break;
                case UPDATE_HEAD_LINE2:
                    renderOnCacheHeadLine23(canvas, stopwatch.getStopwatchData().getInfoL2(), location.add(0, verticalPositions.get(Line2)), stopwatch, rect, true);
                    break;
                case UPDATE_HEAD_LINE3:
                    renderOnCacheHeadLine23(canvas, stopwatch.getStopwatchData().getInfoL3(), location.add(0, verticalPositions.get(Line3)), stopwatch, rect, true);
                    break;
                case UPDATE_HEAD_DIGIT:
                    renderOnCacheHeadLineDigit(canvas, location, verticalPositions, stopwatch, rect);
                    break;
                case DESELECT:
                    Point loc = location.add(0, verticalPositions.get(Hline) + (isExpanded() ? detailsHeight : 0));
                    renderOnCacheHorizontalLine(canvas, loc, BaseUI.groupBottomLineHeight, colorBottomGroup, rect);
                    break;
                case SELECT:
                    loc = location.add(0, verticalPositions.get(Hline) + (isExpanded() ? detailsHeight : 0));
                    renderOnCacheSelect(canvas, loc, BaseUI.groupBottomLineHeight,rect);
                    break;
                case UPDATE_DETAILS:
                    if (stopwatch.getStopwatchData().getChronoType() == Units.CHRONO_TYPE.LAPS || stopwatch.getStopwatchData().getChronoType() == Units.CHRONO_TYPE.SEGMENTS)
                        renderOnCacheHeadLine23(canvas, stopwatch.getStopwatchData().getInfoL2(), location.add(0, verticalPositions.get(Line2)), stopwatch, rect, true);
                    renderOnCacheUpdateDetail(canvas, stopwatch);
            }
        clearUpdateType();
        if (stopwatch.isRunning())
            addUpdateType(Units.UPDATE_TYPE.UPDATE_HEAD_DIGIT);
    }

    private void eraseActual() {
        //rect.set(location.getXasFloat(), location.getYasFloat(), BaseUI.SCREENWIDTH, location.getYasFloat() + getComputedHeight());
        rect.set(location.getXasFloat(), location.getYasFloat(), BaseUI.SCREENWIDTH, location.getYasFloat() + headHeight+detailsHeight);
        cachedCanvas.drawRect(rect, paintBackground);
    }

    @Override
    public void renderOnCache() {
    }

    @Override
    public void renderOnScreen(Canvas canvas) {
    }

    @Override
    public boolean drawMe() {
        return true;
    }

    private static float computeHeaderPositions(HashMap<VERTICAL_POSITIONS, Float> verticalPositions, Stopwatch stopwatch) {
        verticalPositions.put(Line1, 0.0f + BaseUI.SPACING);
        verticalPositions.put(Line2, verticalPositions.get(Line1) + BaseUI.normalTextBlocHeight + (showStartTime ? BaseUI.normalTextBlocHeight : 0));
        verticalPositions.put(Line3, verticalPositions.get(Line2) + BaseUI.normalTextBlocHeight);
        if (stopwatch.getStopwatchData().getChronoType() == Units.CHRONO_TYPE.SIMPLE)
            verticalPositions.put(Digit, verticalPositions.get(Line2));
        else
            verticalPositions.put(Digit, verticalPositions.get(Line3) + BaseUI.normalTextBlocHeight);

        verticalPositions.put(Hline, verticalPositions.get(Digit) + BaseUI.digitTextBlocHeight);
        verticalPositions.put(Data, verticalPositions.get(Hline) + BaseUI.groupBottomLineHeight);

        return verticalPositions.get(Data);

    }

    @Override
    public float getComputedHeight() {
        headHeight = computeHeaderPositions(verticalPositions, stopwatch);
        if (stopwatch.getStopwatchData().hasDataRow()) {
            float dl = BaseUI.SPACING + BaseUI.groupBottomLineHeight;
            int size = stopwatch.getStopwatchData().getTimeList().size();
            for (int i = 0; i < size; i++)
                dl += BaseUI.smallTextBlocHeight;
            if (dl > detailsHeight)
                detailsHeight = dl;
        }
        return headHeight + (isExpanded() ? detailsHeight : 0);
    }

    private void renderOnCacheAll(Canvas canvas) {
        renderOnCacheHeadLine1(canvas, location, verticalPositions, stopwatch, rect, false);

        if (stopwatch.getStopwatchData().getChronoType() == Units.CHRONO_TYPE.LAPS || stopwatch.getStopwatchData().getChronoType() == Units.CHRONO_TYPE.SEGMENTS) {
            renderOnCacheHeadLine23(canvas, stopwatch.getStopwatchData().getInfoL2(), location.add(0, verticalPositions.get(Line2)), stopwatch, rect, false);
            renderOnCacheHeadLine23(canvas, stopwatch.getStopwatchData().getInfoL3(), location.add(0, verticalPositions.get(Line3)), stopwatch, rect, false);
        }
        renderOnCacheHeadLineDigit(canvas, location, verticalPositions, stopwatch, rect);
        if (isSelected())
            renderOnCacheSelect(canvas, location.add(0, verticalPositions.get(Hline)), BaseUI.groupBottomLineHeight,rect);
        else
            renderOnCacheHorizontalLine(canvas, location.add(0, verticalPositions.get(Hline)), BaseUI.groupBottomLineHeight, colorBottomGroup,rect);

        // render the children if any
        if (stopwatch.getStopwatchData().hasDataRow())
            renderOnCacheUpdateDetail(canvas, stopwatch);

    }

    private static void renderOnCacheHeadLineDigit(Canvas canvas, Point location, HashMap<VERTICAL_POSITIONS, Float> verticalPositions, Stopwatch stopwatch, RectF rect) {
        Point point = location.add(0, verticalPositions.get(Digit));
        // third (or second line)
        if (stopwatch.isStopped()) {
            BaseUI.paintDigit.setColor(colorStopped);
        } else if (stopwatch.isRunning()) {
            BaseUI.paintDigit.setColor(colorRunning);
        } else {
            BaseUI.paintDigit.setColor(Color.WHITE);
        }

        rect.set(point.getXasFloat() + 10 * BaseUI.SPACING, point.getYasFloat(), canvas.getWidth(), point.getYasFloat() + BaseUI.digitTextBlocHeight);
        canvas.drawRect(rect, paintBackground);
        canvas.drawText(stopwatch.getTime().toString(), canvas.getWidth() - BaseUI.SPACING, point.getYasFloat() + BaseUI.digitTextBaseLineVerticalOffset, BaseUI.paintDigit);
        point.addY(BaseUI.digitTextBlocHeight);
    }

    private static void renderOnCacheHeadLine1(Canvas canvas, Point location, HashMap<VERTICAL_POSITIONS, Float> verticalPositions, Stopwatch stopwatch, RectF rect, boolean replace) {
        Point point = location.add(0, verticalPositions.get(Line1));
        // entering each bloc that write on cachedCanvas
        // blBottomVPos is the bottom position off the previous bloc (ie the top of this one)
        // first line name+type (always)
        if (replace)
            erase(canvas, point, BaseUI.normalTextBlocHeight, rect);

        canvas.drawText(stopwatch.getName(), BaseUI.SPACING, point.getYasFloat() + BaseUI.normalTextBaseLineVerticalOffset, BaseUI.paintNormalTextWhiteLeft);
        canvas.drawText(stopwatch.getStopwatchData().getChronoType().toString(), canvas.getWidth() - BaseUI.SPACING, point.getYasFloat() + BaseUI.normalTextBaseLineVerticalOffset, BaseUI.paintNormalTextWhiteRigth);
        //Log.i("StopWatchUI","headLine1 getWidth() = " + getWidth());
        if ((showStartDate || showStartTime)) {
            point.addY(BaseUI.normalTextBlocHeight);
            erase(canvas, point, BaseUI.normalTextBlocHeight, rect);
            if (!stopwatch.isWaitingStart()) {
                String txt = "";
                if (showStartDate && showStartTime)
                    txt = Units.getLocalizedTextWithParams("started_on_at", simpleDateFormat.format(stopwatch.getStartTime()), simpleTimeFormat.format(stopwatch.getStartTime()));
                else if (showStartDate)
                    txt = Units.getLocalizedTextWithParams("started_on", simpleDateFormat.format(stopwatch.getStartTime()));
                else if (showStartTime)
                    txt = Units.getLocalizedTextWithParams("started_at", simpleTimeFormat.format(stopwatch.getStartTime()));
                canvas.drawText(txt, BaseUI.SPACING, point.getYasFloat() + BaseUI.normalTextBaseLineVerticalOffset, BaseUI.paintNormalTextWhiteLeft);
            }
        }
    }

    private static void renderOnCacheHeadLine23(Canvas canvas, String infoLine, Point point, Stopwatch stopwatch, RectF rect, boolean replace) {
        if (stopwatch.getStopwatchData().getLapDistance() > 0) {
            if (replace)
                erase(canvas, point, BaseUI.normalTextBlocHeight, rect);

            canvas.drawText(infoLine, BaseUI.SPACING, point.getYasFloat() + BaseUI.normalTextBaseLineVerticalOffset, BaseUI.paintNormalTextWhiteLeft);
        }
    }

    /**
     * erase from point to the width of the screen and on the height passed as parameter.
     *
     * @param canvas {@link Canvas}
     * @param point  {@link Point}
     * @param rect   {@link RectF}
     * @param height int
     */
    private static void erase(Canvas canvas, Point point, int height, RectF rect) {
        rect.set(point.getXasFloat(), point.getYasFloat(), canvas.getWidth(), point.getYasFloat() + height);
        canvas.drawRect(rect, paintBackground);
    }

    private static void renderOnCacheSelect(Canvas canvas, Point point, int height, RectF rect) {
        renderOnCacheHorizontalLine(canvas, point, height, colorSelected, rect);
        canvas.drawText(Units.getLocalizedText(R.string.selected), BaseUI.SPACING, point.getYasFloat() + height - BaseUI.SPACING, BaseUI.paintVerySmallTextWhiteLeft);
    }

    private static void renderOnCacheHorizontalLine(Canvas canvas, Point point, int height, int color, RectF rect) {
        // sep line point.getYasFloat() is the top of the line.
        paintLine.setColor(color);
        rect.set(point.getXasFloat(), point.getYasFloat(), canvas.getWidth(), point.getYasFloat() + height);
        canvas.drawRect(rect, paintLine);

    }

    private void renderOnCacheUpdateDetail(Canvas canvas, Stopwatch stopwatch) {
        sizeChanged = true;
        // Remove  expand sign on the GUI
        Point point = location.add(0, verticalPositions.get(Data));
        rect.set(0, point.getYasFloat() - BaseUI.groupBottomLineHeight - BaseUI.digitTextBlocHeight, 8 * BaseUI.SPACING, point.getYasFloat() - BaseUI.groupBottomLineHeight);
        canvas.drawRect(rect, paintBackground);
        if (stopwatch.getStopwatchData().hasDataRow()) { // Update expand sign on the GUI
            String sign = isExpanded() ? "-" : "+";
            canvas.drawText(sign, 8 * BaseUI.SPACING, point.getYasFloat() - BaseUI.digitTextBlocHeight + BaseUI.digitTextBaseLineVerticalOffset - BaseUI.groupBottomLineHeight, BaseUI.paintWhiteRigthDigitSize);
        }
        point.addY(-BaseUI.groupBottomLineHeight);
        // black "remove" detail including bottomLine
        float bottom = point.getYasFloat() + detailsHeight + BaseUI.groupBottomLineHeight+ BaseUI.SPACING+BaseUI.smallTextBlocHeight;
        rect.set(point.getXasFloat(), point.getYasFloat(), canvas.getWidth(), bottom);
        canvas.drawRect(rect, paintBackground);


        if (isExpanded()) {
            renderOnCacheHorizontalLine(canvas, point, BaseUI.groupBottomLineHeight, colorSepHeadDetail,rect);
            point.addY(BaseUI.groupBottomLineHeight);
            // retain position to calculate at this end the height of this detail bloc and redraw the bottom line
            float localStart = point.getYasFloat();

            for (StopwatchDataRow stopwatchDataRow : stopwatch.getStopwatchData().getTimeList()) {
                canvas.drawText(stopwatchDataRow.getLine(), canvas.getWidth() - BaseUI.SPACING, point.getYasFloat() + BaseUI.smallTextBaseLineVerticalOffset, BaseUI.paintSmallTextWhiteRigth);
                point.addY(BaseUI.smallTextBlocHeight);
            }
            point.addY(BaseUI.SPACING);

            detailsHeight = point.getYasFloat() - localStart + BaseUI.groupBottomLineHeight;
        }
        if (isSelected())
            renderOnCacheSelect(canvas, point, BaseUI.groupBottomLineHeight,rect);
        else
            renderOnCacheHorizontalLine(canvas, point, BaseUI.groupBottomLineHeight, colorBottomGroup,rect);

    }

    @Override
    public void sizeChangedFor(UIRenderer renderer) {// unused
    }


    public static void setShowStartTime(boolean showStartTime) {
        StopWatchUI2.showStartTime = showStartTime;
    }

    public static void setShowStartDate(boolean showStartDate) {
        StopWatchUI2.showStartDate = showStartDate;
    }

    public static boolean mustShowStartDateTime() {
        return showStartTime || showStartDate;
    }

}
