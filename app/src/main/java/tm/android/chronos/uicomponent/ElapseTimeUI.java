/*
 * ElapseTimeUI
 *
 * Copyright (c) 2018 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */
package tm.android.chronos.uicomponent;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import tm.android.chronos.R;
import tm.android.chronos.core.Digit;
import tm.android.chronos.core.Stopwatch;
import tm.android.chronos.core.StopwatchFactory;
import tm.android.chronos.core.Units;
import tm.android.chronos.core.ui.AbstractUI;
import tm.android.chronos.core.ui.UIRenderer;
import tm.android.chronos.util.geom.Point;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Graphical User Interface showing timer parameters and the time alapsed since the end of the timer.
 */
public class ElapseTimeUI extends AbstractUI {
    private final Stopwatch stopwatch;
    private final Paint paintFix;
    private final Paint paintDigit;
    private int dxForSinceTimeDigit = 0;
    private final Params params;

    ElapseTimeUI(Canvas canvas, Params params) {
        super(canvas);
        this.params = params;
        stopwatch = StopwatchFactory.create(); // type SIMPLE by default
        stopwatch.start(params.getTimerEndTime());

        paintFix = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintFix.setTextSize(BaseUI.normalTextSize);
        paintFix.setColor(Color.BLUE);

        paintDigit = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintDigit.setTextSize(BaseUI.normalTextSize);
        paintDigit.setTextAlign(Paint.Align.LEFT);
        paintDigit.setColor(Color.RED);
        Digit.setInitilaDigitFormat(Units.DIGIT_FORMAT.NO_MS_SHORT);
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
                        renderAll(canvas);
                        break;
                    case UPDATE_HEAD_DIGIT:
                        renderSinceTime(canvas);
                        break;
                }
            }
        }
        clearUpdateType();
        if (stopwatch.isRunning())
            addUpdateType(Units.UPDATE_TYPE.UPDATE_HEAD_DIGIT);


    }

    private void renderAll(Canvas canvas) {
        Log.i("ElapseTimeUI", "renderAll");
        paintFix.setColor(Color.BLUE);
        Point point = location.add(BaseUI.SPACING, 0);
        canvas.drawText(Units.getLocalizedText(R.string.timer_elapseTime_L1), point.getXasFloat(), point.getYasFloat(), paintFix);
        point.addY(BaseUI.normalTextBlocHeight);
        Date d = new Date(params.getTimerStartTime());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("[dd-LL-yyyy] HH:mm:ss");
        canvas.drawText(sdf.format(d), point.getXasFloat(), point.getYasFloat(), paintFix);
        point.addY(BaseUI.normalTextBlocHeight);
        canvas.drawText(Units.getLocalizedText(R.string.timer_elapseTime_L2) + (params.getTimerDuration() / 60000) + " minute" + (params.getTimerDuration() > 60000 ? "s" : ""), point.getXasFloat(), point.getYasFloat(), paintFix);
        renderSinceText(canvas);
        renderSinceTime(canvas);
    }

    private void renderSinceText(Canvas canvas) {
        Rect r = new Rect();
        String txt = Units.getLocalizedText(R.string.timer_elapseTime_L3);
        paintFix.getTextBounds(txt, 0, txt.length(), r);
        dxForSinceTimeDigit = r.width() + 3 * BaseUI.SPACING;
        Point point = location.add(BaseUI.SPACING, 3 * BaseUI.normalTextBlocHeight);
        canvas.drawText(txt, point.getXasFloat(), point.getYasFloat(), paintFix);
    }

    private void renderSinceTime(Canvas canvas) {
        Point point = location.add(dxForSinceTimeDigit, 3 * BaseUI.normalTextBlocHeight);
        rect.set(point.getXasFloat(), point.getYasFloat(), BaseUI.SCREENWIDTH, point.getYasFloat() - BaseUI.normalTextBlocHeight);
        canvas.drawRect(rect, BaseUI.paintNoText);
        String time = stopwatch.getTime().toString().trim() + (stopwatch.getTime().getInternal() > 60000 ? " (m:s)" : " s");
        canvas.drawText(time, point.getXasFloat(), point.getYasFloat(), paintDigit);
    }


    @Override
    public void renderOnCache() {

    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) stopwatch;
    }

    @Override
    public void doNonUIAction() {

    }

    @Override
    public float getComputedHeight() {
        return 0;
    }

    @Override
    public void sizeChangedFor(UIRenderer renderer) {

    }
}
