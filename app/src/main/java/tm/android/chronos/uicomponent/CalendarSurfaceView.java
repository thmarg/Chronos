/*
 * ${NAME}
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
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import tm.android.chronos.core.CalendarImpl;
import tm.android.chronos.core.SurfaceViewRenderer;
import tm.android.chronos.core.ui.RendererWorker;
import tm.android.chronos.util.DateTime;


/**
 * Created by thmarg on 10/02/15.
 */
public class CalendarSurfaceView extends SurfaceView implements SurfaceViewRenderer, SurfaceHolder.Callback {
    private final static long YEAR = 31536000000L;
    CalendarImpl calendar;
    private RendererWorker worker;
    private Rect rect;
    private int colWidth;
    private Paint paintNormalTextRightWhite;
    private Paint paintNormalTextRightLightGray;
    private int vPosStartDaySearch;
    private DateTime.Day[] currentDaysOnScreen;
    private DateTime.Day selectedDay;
    private DateTime.Day pickedDay;
    private DateTime selDay;

    public CalendarSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //worker = new RendererWorker(this);
        calendar = new CalendarImpl();
        //calendar.setMaxDate(20 * YEAR);

        // worker.register(calendar);
        colWidth = (BaseUI.SCREENWIDTH - 2 * BaseUI.SPACING) / 7;
        //
        setBackgroundColor(Color.TRANSPARENT);
        rect = new Rect();
        //
        paintNormalTextRightWhite = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintNormalTextRightWhite.setTextSize(BaseUI.getNormalTextSize());
        paintNormalTextRightWhite.setTextAlign(Paint.Align.RIGHT);
        paintNormalTextRightWhite.setColor(Color.WHITE);
        //
        paintNormalTextRightLightGray = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintNormalTextRightLightGray.setTextSize(BaseUI.getNormalTextSize());
        paintNormalTextRightLightGray.setTextAlign(Paint.Align.RIGHT);
        paintNormalTextRightLightGray.setColor(Color.GRAY);
        //
        getHolder().addCallback(this);
        setOnTouchListener(new TouchListener());

        currentDaysOnScreen = calendar.getDaysToDisplay(null);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//					Canvas canvas = holder.lockCanvas();
//				renderOnScreen(canvas);
//				holder.unlockCanvasAndPost(canvas);
    }

//	@Override
//	protected void onDraw(Canvas canvas) {
//		super.onDraw(canvas);
//		renderOnScreen(canvas);
//	}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        worker.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void renderOnCache() {

    }

    @Override
    public void renderOnScreen(Canvas canvas) {

        rect = canvas.getClipBounds();
        canvas.drawRect(rect, BaseUI.getPaintNoText());

        String month = calendar.getDayToShow().getDisplayName(DateTime.MONTH);
        //month = month.substring(0,1).toUpperCase()+month.substring(1);
        int year = calendar.getDayToShow().get(DateTime.YEAR);

        int n = BaseUI.SCREENWIDTH / 2;
        int q = n / 2;
        int y = BaseUI.normalTextBaseLineVerticalOffset + BaseUI.SPACING;

        canvas.drawText("< " + month + " >", q, y, BaseUI.getPaintNormalTextWhiteCenter());
        canvas.drawText("< " + year + " >", n + q, y, BaseUI.getPaintNormalTextWhiteCenter());
        //
        y = BaseUI.normalTextBlocHeight + 3 * BaseUI.SPACING;
        rect.set(BaseUI.SPACING, y, BaseUI.SCREENWIDTH - BaseUI.SPACING, y + 3);
        canvas.drawRect(rect, BaseUI.paintNormalTextWhiteRigth);
        //
        vPosStartDaySearch = y + 2 * BaseUI.SPACING;
        y += 3 + 2 * BaseUI.SPACING + BaseUI.normalTextBaseLineVerticalOffset;

        int x = colWidth;
        canvas.drawText("L", x, y, BaseUI.getPaintNormalTextWhiteRigth());
        x += colWidth;
        canvas.drawText("M", x, y, BaseUI.getPaintNormalTextWhiteRigth());
        x += colWidth;
        canvas.drawText("M", x, y, BaseUI.getPaintNormalTextWhiteRigth());
        x += colWidth;
        canvas.drawText("J", x, y, BaseUI.getPaintNormalTextWhiteRigth());
        x += colWidth;
        canvas.drawText("V", x, y, BaseUI.getPaintNormalTextWhiteRigth());
        x += colWidth;
        canvas.drawText("S", x, y, BaseUI.getPaintNormalTextWhiteRigth());
        x += colWidth;
        canvas.drawText("D", x, y, BaseUI.getPaintNormalTextWhiteRigth());

        y += BaseUI.SPACING;
        rect.top = y;
        rect.bottom = y + 1;
        canvas.drawRect(rect, BaseUI.paintNormalTextWhiteRigth);
        y += 4 * BaseUI.SPACING + BaseUI.normalTextBaseLineVerticalOffset;


        x = colWidth;
        int i = 0;
        for (DateTime.Day day : currentDaysOnScreen) {

            if (day.isInCurrentMonth()) {
                if (day.isSelected()) {
                    y -= BaseUI.normalTextBaseLineVerticalOffset;
                    rect.set(x - BaseUI.normalTextDigitWidth - 3, y - 3, x + 3, y + BaseUI.normalTextBlocHeight + 3);
                    BaseUI.getPaintNoText().setColor(Color.RED);
                    canvas.drawRect(rect, BaseUI.getPaintNoText());
                    BaseUI.getPaintNoText().setColor(Color.BLACK);
                    rect.set(x - BaseUI.normalTextDigitWidth, y, x, y + BaseUI.normalTextBlocHeight);
                    canvas.drawRect(rect, BaseUI.getPaintNoText());
                    y += BaseUI.normalTextBaseLineVerticalOffset;

                }
                if (day.isToday()) {
                    y -= BaseUI.normalTextBaseLineVerticalOffset;
                    rect.set(x - BaseUI.normalTextDigitWidth, y, x, y + BaseUI.normalTextBlocHeight);
                    paintNormalTextRightLightGray.setColor(Color.BLUE);
                    canvas.drawRect(rect, paintNormalTextRightLightGray);
                    paintNormalTextRightLightGray.setColor(Color.GRAY);
                    y += BaseUI.normalTextBaseLineVerticalOffset;
                }
                canvas.drawText("" + day.getDayNum(), x, y, paintNormalTextRightWhite);
            } else {
                canvas.drawText("" + day.getDayNum(), x, y, paintNormalTextRightLightGray);
            }
            x += colWidth;
            if ((i + 1) % 7 == 0) {
                x = colWidth;
                y = y + (BaseUI.normalTextBlocHeight + 4 * BaseUI.SPACING);
            }
            i++;
        }


    }

    private ACTION getAction(float X, float Y) {
        if (Y < vPosStartDaySearch) {
            if (X < BaseUI.SCREENWIDTH / 2) {
                if (X < BaseUI.SCREENWIDTH / 4)
                    return ACTION.M_MOINS;
                else
                    return ACTION.M_PLUS;
            } else {
                if (X > BaseUI.SCREENWIDTH / 2 + BaseUI.SCREENWIDTH / 4)
                    return ACTION.Y_PLUS;
                else
                    return ACTION.Y_MOINS;
            }
        } else {
            int vStep = BaseUI.normalTextBlocHeight + 4 * BaseUI.SPACING;
            if (Y < vPosStartDaySearch && Y > vPosStartDaySearch + 6 * vStep)
                return ACTION.NONE;
            int j = (int) (Y - vPosStartDaySearch) / vStep - 1;
            int i = (int) (X / colWidth);
            int n = i + 7 * j;
            if (n >= 0 && n < currentDaysOnScreen.length) {
                pickedDay = currentDaysOnScreen[n];
                return ACTION.PICK_A_DAY;
            } else
                return ACTION.NONE;
        }

    }


    private enum ACTION {M_PLUS, M_MOINS, Y_PLUS, Y_MOINS, NONE, PICK_A_DAY}

    private class TouchListener implements OnTouchListener {
        private final static int WAIT = 0;
        private final static int DOWN = 1;
        private final static int UP = 2;
        private int status = WAIT;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (status == WAIT)
                        status = DOWN;
                    break;
                case MotionEvent.ACTION_MOVE:
                    status = WAIT;
                    break;
                case MotionEvent.ACTION_UP:
                    if (status == DOWN) {
                        // yeh ! onClick
                        float X = getX();
                        float Y = getY();
                        ACTION action = getAction(event.getX(), event.getY());
                        switch (action) {
                            case NONE:
                                break;
                            case M_MOINS:
                                calendar.subMonth();
                                currentDaysOnScreen = calendar.getDaysToDisplay(selDay);
                                break;
                            case M_PLUS:
                                calendar.addMonth();
                                currentDaysOnScreen = calendar.getDaysToDisplay(selDay);
                                break;
                            case Y_MOINS:
                                calendar.subYear();
                                currentDaysOnScreen = calendar.getDaysToDisplay(selDay);
                                break;
                            case Y_PLUS:
                                calendar.addYear();
                                currentDaysOnScreen = calendar.getDaysToDisplay(selDay);
                                break;
                            case PICK_A_DAY:
                                if (pickedDay.isInCurrentMonth()) {
                                    if (calendar.isSameMonthYear() && pickedDay.getDayNum() < calendar.getDayToShow().get(DateTime.DAY_OF_MONTH))
                                        break;
                                    // select and deselect if any the previous on selected.
                                    if (selectedDay != null) {
                                        selectedDay.setSelected(false);
                                    }
                                    pickedDay.setSelected(true);
                                    selectedDay = pickedDay;
                                    selDay = calendar.getDayToShow().cloneIt();
                                    selDay.setField(DateTime.DAY_OF_MONTH, pickedDay.getDayNum());
                                    currentDaysOnScreen = calendar.getDaysToDisplay(selDay);
                                }
                                break;
                        }
//                        if (action != ACTION.NONE)
//                            calendar.getUIRenderer().addUpdateType(Units.UPDATE_CHRONO_TYPE.ADD_NEW);
                        break;
                    }
            }
            performClick();
            return true;
        }
    }

}
