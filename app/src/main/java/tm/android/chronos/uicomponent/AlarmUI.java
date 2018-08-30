/*
 * ${NAME}
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.uicomponent;

import android.graphics.*;
import tm.android.chronos.R;
import tm.android.chronos.activity.Chronos;
import tm.android.chronos.core.Alarm;
import tm.android.chronos.core.AlarmData;
import tm.android.chronos.core.DaysOfWeek;
import tm.android.chronos.core.Units;
import tm.android.chronos.core.ui.AbstractUI;
import tm.android.chronos.core.ui.UIRenderer;
import tm.android.chronos.util.geom.Point;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static tm.android.chronos.uicomponent.AlarmUI.VERTICAL_POSITIONS.*;


/**
 * Created by thmarg on Mai 2018.
 */
public class AlarmUI extends AbstractUI {
    private final static int colorSelected = Color.parseColor("#ec4316");
    private Alarm alarm;
    private final static Paint paintDigit;
    private final static Paint paintLine;
    private HashMap<VERTICAL_POSITIONS, Float> verticalPositions;
    protected enum VERTICAL_POSITIONS {Line1, Line2, Line3, Line4, Line5, Hline}
    private float last_vertical_size;

    static {
        paintDigit = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintDigit.setTextSize(BaseUI.normalTextSize);
        paintDigit.setTextAlign(Paint.Align.RIGHT);

        paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLine.setColor(Color.BLUE);
    }

    AlarmUI(Alarm alarm, Canvas cachedCanvas) {
        super(cachedCanvas);
        this.alarm = alarm;
        verticalPositions = new HashMap<>(5);
        computeVerticalPositions(verticalPositions, alarm);
    }

    private static void computeVerticalPositions(HashMap<VERTICAL_POSITIONS, Float> verticalPositions, Alarm alarm) {
        verticalPositions.put(Line1, 0.0f + BaseUI.SPACING);
        verticalPositions.put(Line2, verticalPositions.get(Line1) + BaseUI.normalTextBlocHeight);
        verticalPositions.put(Line3, verticalPositions.get(Line2) + BaseUI.normalTextBlocHeight);
        verticalPositions.put(Line4, verticalPositions.get(Line3) + BaseUI.normalTextBlocHeight);
        if (alarm.getAlarmData().getType() == AlarmData.ALARM_TYPE.REPEATED_LOOP || alarm.getAlarmData().getType() == AlarmData.ALARM_TYPE.REPEATED_LOOP_SPEC_TIME) {
            verticalPositions.put(Line5, verticalPositions.get(Line4) + BaseUI.normalTextBlocHeight);
            verticalPositions.put(Hline, verticalPositions.get(Line5) + 3 * BaseUI.normalTextBlocHeight / 2);
        } else {
            verticalPositions.put(Hline, verticalPositions.get(Line4) + 3 * BaseUI.normalTextBlocHeight / 2);
        }
    }


    @Override
    public void renderOnScreen(Canvas canvas) {
        // unused
    }

    @Override
    public boolean drawMe() {
        return true;
    }

    @Override
    public void renderOnCanvas(Canvas canvas) {
        for (Units.UPDATE_TYPE updateType : getUpdateTypes()) {
            switch (updateType) {
                case PAINT_ALL:
                    renderOnCachePaintAll(canvas);
                    break;
                case UPDATE_ALARM_TYPE:
                    computeVerticalPositions(verticalPositions,alarm);
                    float current_vertical_size = verticalPositions.get(Hline)+BaseUI.normalTextBlocHeight/2;
                    erase(canvas,location, (int)Math.max(current_vertical_size,last_vertical_size),rect);
                    renderOnCachePaintAll(canvas);
                    setSizeChanged();
                case UPDATE_HEAD_DIGIT:
                    renderOnCachePaintDigit(canvas);
                    break;
                case SELECT:
                    renderOnCacheSelect(canvas, location.add(0, verticalPositions.get(Hline)), BaseUI.normalTextBlocHeight / 2, rect);
                    break;
                case DESELECT:
                    renderOnCacheHorizontalLine(canvas, location.add(0, verticalPositions.get(Hline)), BaseUI.normalTextBlocHeight / 2, Color.BLUE, rect);
                    break;
            }
        }
        clearUpdateType();
        if (alarm.isRunning())
            addUpdateType(Units.UPDATE_TYPE.UPDATE_HEAD_DIGIT);
        last_vertical_size = verticalPositions.get(Hline)+BaseUI.normalTextBlocHeight/2;
    }


    private void renderOnCachePaintAll(Canvas canvas) {
        renderOnCachePaintHead(canvas);
        if (alarm.isRunning())
            renderOnCachePaintDigit(canvas);
        if (alarm.getAlarmData().getType() == AlarmData.ALARM_TYPE.REPEATED_LOOP || alarm.getAlarmData().getType() == AlarmData.ALARM_TYPE.REPEATED_LOOP_SPEC_TIME) {
            renderOnCacheWeekDays(canvas, location.add(0, verticalPositions.get(Line5)), alarm, rect);
        }
        if (isSelected())
            renderOnCacheSelect(canvas, location.add(0, verticalPositions.get(Hline)), BaseUI.normalTextBlocHeight / 2, rect);
        else
            renderOnCacheHorizontalLine(canvas, location.add(0, verticalPositions.get(Hline)), BaseUI.normalTextBlocHeight / 2, Color.BLUE, rect);
    }

    private void renderOnCachePaintHead(Canvas canvas) {
        Point point = location.add(BaseUI.SPACING, 0);
        //erase(canvas, point, 2 * BaseUI.normalTextBlocHeight, rect);

        point.addY(BaseUI.normalTextBlocHeight);
        canvas.drawText(Units.getLocalizedText(R.string.alarm)+" " + Units.getLocalizedText(alarm.getAlarmData().getType().name()), point.getXasFloat(), point.getYasFloat(), BaseUI.paintNormalTextWhiteLeft);
        point.addY(BaseUI.normalTextBlocHeight);
        canvas.drawText(Units.getLocalizedText(R.string.title)+ ": " + alarm.getName(), point.getXasFloat(), point.getYasFloat(), BaseUI.paintNormalTextWhiteLeft);
        erase(canvas, point, BaseUI.normalTextBlocHeight, rect);
        point.addY(BaseUI.normalTextBlocHeight);
        if (alarm.isRunning()) {
            if (!alarm.isPassed()) {
                String txt = Units.getLocalizedTextWithParams("le_a",Chronos.fdate.format(alarm.getEndTime()),Chronos.ftime.format(alarm.getEndTime()));
                canvas.drawText(Units.getLocalizedText(R.string.planned) + ": " + txt, point.getXasFloat(), point.getYasFloat(), BaseUI.paintNormalTextWhiteLeft);
                erase(canvas, point, 2 * BaseUI.normalTextBlocHeight, rect);
                point.addY(BaseUI.normalTextBlocHeight);
                canvas.drawText( Units.getLocalizedText(R.string.remaining_time) + ": ", point.getXasFloat(), point.getYasFloat(), BaseUI.paintNormalTextWhiteLeft);
            } else {
                String txt = Units.getLocalizedTextWithParams("le_a",Chronos.fdate.format(alarm.getEndTime()),Chronos.ftime.format(alarm.getEndTime()));
                canvas.drawText(Units.getLocalizedText(R.string.finished)+": " + txt, point.getXasFloat(), point.getYasFloat(), BaseUI.paintNormalTextWhiteLeft);
                erase(canvas, point, 2 * BaseUI.normalTextBlocHeight, rect);
                point.addY(BaseUI.normalTextBlocHeight);
                canvas.drawText(Units.getLocalizedText(R.string.elapsed_time)+": ", point.getXasFloat(), point.getYasFloat(), BaseUI.paintNormalTextWhiteLeft);
            }
        } else {
            canvas.drawText(Units.getLocalizedText(R.string.not_planned), point.getXasFloat(), point.getYasFloat(), BaseUI.paintNormalTextWhiteLeft);
            erase(canvas, point, 2 * BaseUI.normalTextBlocHeight, rect);
            point.addY(BaseUI.normalTextBlocHeight);
            canvas.drawText(Units.getLocalizedText(R.string.stopped), point.getXasFloat(), point.getYasFloat(), BaseUI.paintNormalTextWhiteLeft);
        }

    }

    private void renderOnCachePaintDigit(Canvas canvas) {
        Point point = location.add(70 * BaseUI.SPACING, 3 * BaseUI.normalTextBlocHeight);
        erase(canvas, point, BaseUI.digitTextBlocHeight, rect);
        int digitColor = (alarm.isRunning() && alarm.getMode() == Alarm.MODE.TIMER) ? Color.GREEN : Color.RED;
        paintDigit.setColor(digitColor);
        point = location.add(BaseUI.SCREENWIDTH - 2 * BaseUI.SPACING, verticalPositions.get(Line4)+BaseUI.normalTextBlocHeight);
        canvas.drawText(alarm.getTime().toString().trim(), point.getXasFloat(), point.getYasFloat(), paintDigit);
    }

    private static void renderOnCacheWeekDays(Canvas canvas, Point point, Alarm alarm, RectF rect) {
        erase(canvas, point, BaseUI.normalTextBlocHeight+BaseUI.SPACING, rect);
        point.addY(BaseUI.normalTextBlocHeight);
        if (alarm.isRunning() && !alarm.isPassed()) {
            Rect rect1 = new Rect();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(alarm.getFirstEndTime()));
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            int color;
            for (int i : DaysOfWeek.getWeekDays()) {
                if (alarm.getAlarmData().getDaysOfWeek().isDayEnable(i)) {
                    if (day == i)
                        color = Color.GREEN;
                    else
                        color = Color.MAGENTA;
                } else {
                    color = Color.GRAY;
                }
                BaseUI.paintNormalTextWhiteLeft.setColor(color);
                String text = Units.getLocalizedText("day" + i);
                canvas.drawText(text, point.getXasFloat(), point.getYasFloat(), BaseUI.paintNormalTextWhiteLeft);
                BaseUI.paintNormalTextWhiteLeft.getTextBounds(text,0,text.length(),rect1);
                point.addX(2*BaseUI.SPACING+rect1.right-rect1.left);
            }
            BaseUI.paintNormalTextWhiteLeft.setColor(Color.WHITE);
        } else {
            canvas.drawText(alarm.getAlarmData().getDaysOfWeek().getSelectedDaysAsString(), point.getXasFloat(), point.getYasFloat(), BaseUI.paintNormalTextWhiteLeft);
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
        canvas.drawRect(rect, BaseUI.paintNoText);
    }


    private static void renderOnCacheHorizontalLine(Canvas canvas, Point point, int height, int color, RectF rect) {
        // sep line point.getYasFloat() is the top of the line.
        paintLine.setColor(color);
        rect.set(point.getXasFloat(), point.getYasFloat(), canvas.getWidth(), point.getYasFloat() + height);
        canvas.drawRect(rect, paintLine);
    }

    private static void renderOnCacheSelect(Canvas canvas, Point point, int height, RectF rect) {
        renderOnCacheHorizontalLine(canvas, point, height, colorSelected, rect);
        canvas.drawText(Units.getLocalizedText(R.string.selected), BaseUI.SPACING, point.getYasFloat() + height - BaseUI.SPACING, BaseUI.paintVerySmallTextWhiteLeft);
    }

    @Override
    public void renderOnCache() {
        //unused
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) alarm;
    }

    @Override
    public void doNonUIAction() {

    }

    @Override
    public float getComputedHeight() {

        return verticalPositions.get(Hline) + BaseUI.normalTextBlocHeight / 2;
    }

    @Override
    public void sizeChangedFor(UIRenderer renderer) {

    }
}
