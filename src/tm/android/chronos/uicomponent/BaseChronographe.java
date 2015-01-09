/*
 *  BaseChronographe
 *
 *    Copyright (c) 2014 Thierry Margenstern under MIT license
 *    http://opensource.org/licenses/MIT
 *
 */

package tm.android.chronos.uicomponent;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * The goal of this class is to provide constant to render a stopwatch on a surfaceView.<br>
 *   Calculations are made to fit any screen size.
 */
public class BaseChronographe extends SurfaceView {

    protected  final static int CHRONO_TEXT_SIZE_IN_DP = 30;// dp , good base size for the chrono digits
    protected  final static int OTHER_TEXT_SIZE_IN_DP =20;
    protected final static int SPACING = 4;// pixel

    protected static Paint paintWhiteRight;// to paint digit right align
    protected static Paint paintWhiteLeft;// to paint digit left align
    protected static Paint paintWhileRunning; // background while running
    protected static Paint paintWileWaitToStart;// to erase before writing digit
    protected static Paint paintWhilePaused;
    protected static Paint paintWhileStoppedAndWaitReset;
    protected static Paint paintOtherTextWhiteLeft;


    protected static int screenWidth;// pixel
    protected static int leftMargin;// pixel

    protected static int chronoDigitHeight; // pixel chronoDigitHeight plus spacing top and bottom
    protected static int chrono2PointWidth; // width of symbol ":"
    protected static int chronoDigitWidth;// pixel
    protected static int otherTextCharHeight;// pixel

    protected static int prefWidth;
    protected static int prefHeight;


    // vertical position for drawText (top=0, bottom=prefHeight)
    protected  static int bottomLineVerticalPosition;
    protected  static int topLineVerticalPosition;
    protected  static int middleLineVerticalPosition;

    // boundaries for touch event detection , line position also used.
    protected static int chronoHorizontalEnd;



    public BaseChronographe(Context context) {
        super(context);
        setBackgroundColor(Color.TRANSPARENT);
        staticInit();

    }

    public BaseChronographe(Context context, AttributeSet attributeSet) {
        super(context,attributeSet);
        setBackgroundColor(Color.TRANSPARENT);
        staticInit();

    }


    protected static void staticInit(){
        float density =  Resources.getSystem().getDisplayMetrics().density;
        float chronotextSize = density * CHRONO_TEXT_SIZE_IN_DP;// used by painter
        float otherTextSize = density* OTHER_TEXT_SIZE_IN_DP;
        //
        Rect r = new Rect();
        //
        paintOtherTextWhiteLeft = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintOtherTextWhiteLeft.setTextSize(otherTextSize);
        paintOtherTextWhiteLeft.setTextAlign(Paint.Align.LEFT);
        paintOtherTextWhiteLeft.setColor(Color.WHITE);

        paintWhiteRight = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintWhiteRight.setTextSize(chronotextSize);
        paintWhiteRight.setTextAlign(Paint.Align.RIGHT);
        paintWhiteRight.setColor(Color.WHITE);


        paintWhiteLeft = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintWhiteLeft.setTextSize(chronotextSize);
        paintWhiteLeft.setTextAlign(Paint.Align.LEFT);
        paintWhiteLeft.setColor(Color.WHITE);

        paintWhileRunning = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintWhileRunning.setColor(Color.parseColor("#f3950f"));

        paintWileWaitToStart = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintWileWaitToStart.setColor(Color.BLACK);
        //
        paintWhilePaused = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintWhilePaused.setColor(Color.MAGENTA);
        //
        paintWhileStoppedAndWaitReset = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintWhileStoppedAndWaitReset.setColor(Color.parseColor("#008200"));

        paintWhiteRight.getTextBounds("0", 0, 1, r);
        chronoDigitHeight = r.bottom - r.top + (int)(3.5 * SPACING);
        chronoDigitWidth = r.right - r.left + 2 * SPACING;
        paintWhiteRight.getTextBounds(":", 0, 1, r);
        chrono2PointWidth = r.right - r.left + 2 * SPACING;
        //offsetAlignLeft = SPACING / 2;
        paintOtherTextWhiteLeft.getTextBounds("A", 0, 1, r);
        otherTextCharHeight = r.bottom-r.top+(int)(3.5* SPACING);

        prefHeight = chronoDigitHeight+2*otherTextCharHeight;

        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        prefWidth = 12 * chronoDigitWidth + 4 * chrono2PointWidth +3* chronoDigitWidth;//12 digits,  4 sep ":" and 4 extra for finger place to take intermediates times.
        leftMargin = (screenWidth - prefWidth);

        bottomLineVerticalPosition = prefHeight-SPACING;
        middleLineVerticalPosition = bottomLineVerticalPosition -chronoDigitHeight;
        topLineVerticalPosition = middleLineVerticalPosition -otherTextCharHeight;

        chronoHorizontalEnd= prefWidth-3*chronoDigitWidth;
    }


    public static int getPrefWidth() {
        return prefWidth;
    }

    public static int getLeftMargin() {
        return leftMargin;
    }

    public  static  int getScreenWidth(){
        return screenWidth;
    }

    public static int getPrefHeight() {
        return prefHeight;
    }
}
