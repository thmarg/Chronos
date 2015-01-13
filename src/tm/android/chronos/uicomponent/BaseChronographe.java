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
import tm.android.chronos.core.Units.ZONE_ACTION;
import static tm.android.chronos.core.Units.ZONE_ACTION.*;

/**
 * The goal of this class is to provide constant to render a stopwatch on a surfaceView.<br>
 *   Calculations are made to fit any screen size.<br>
 *   The basic presentation is as follow but can be changed at any time.<br>
 *
 *   *********************************
 *   *N : Clock-1        T : by laps *
 *   *********************************
 *   *D : 600 m  Laps 4  TD : 2400 m *
 *   *               S : 23.848 km/h *
 *   *********************************
 *   *                               *
 *   * +/-          000:00:06:02:289 * Global Time
 *   *********************************
 *   * (S)23.80 01:30:754  01:30:754 * laps time
 *   *    23.80 01:30:749  03:01:503 *
 *   *    24.39 01:28:576  04:30:079 *
 *   *    23.42 01:32:210  06:02:028 *
 *   *********************************
 *
 *   N : Name
 *   T : Type
 *   D : Distance (one lap)
 *   TD : Total Distance
 *   Laps : laps number
 *   S : Speed
 */
public class BaseChronographe extends SurfaceView {

    protected  final static int CHRONO_TEXT_SIZE_IN_DP = 30;// dp , good base size for the chrono digits
    protected  final static int OTHER_TEXT_SIZE_IN_DP =20;
    protected  static int SPACING = (int)(4*Resources.getSystem().getDisplayMetrics().density);// extra space around blocs of letters.

    protected static Paint paintWhiteRight;// to paint digit right align
    protected static Paint paintWhiteLeft;// to paint digit left align
    protected static Paint paintWhileRunning; // background while running
    protected static Paint paintWileWaitToStart;// to erase before writing digit
    protected static Paint paintWhilePaused;
    protected static Paint paintWhileStoppedAndWaitReset;
    protected static Paint paintOtherTextWhiteLeft;
    protected static Paint paintLine;

    protected static int screenWidth;// pixel

    protected static int chronoDigitHeight; // pixel chronoDigitHeight plus spacing top and bottom
    protected static int otherTextCharHeight;// pixel

    protected static int allDigitWidth;
    protected static int fullHeight;


    // vertical position for drawText (top=0, bottom=fullHeight)
    protected  static int bottomLineVerticalPosition;
    protected  static int topLineVerticalPosition;
    protected  static int middleLineVerticalPosition;

    // horizontal user touch zone limit
    private static int lapTimeHorizontalLimit;
    private static int ssrHorizontalLimit; // start stop reset
    private static int paramHorizontalLimit;




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
        paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLine.setColor(Color.BLUE);

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
        Paint.FontMetricsInt fm = paintWhiteRight.getFontMetricsInt();
        int chronoDigitBaseLine = fm.bottom;
        chronoDigitHeight = fm.bottom-fm.top;
        paintWhiteRight.getTextBounds("000:00:00:00:000", 0, 16, r);
        allDigitWidth = r.right-r.left+2*SPACING;



        paintOtherTextWhiteLeft.getTextBounds("A", 0, 1, r);
        otherTextCharHeight =  paintOtherTextWhiteLeft.getFontMetricsInt().bottom-paintOtherTextWhiteLeft.getFontMetricsInt().top;
        int otherTextBaseLine =  paintOtherTextWhiteLeft.getFontMetricsInt().bottom;

        fullHeight = chronoDigitHeight+2*otherTextCharHeight+4*SPACING;// used to full erase  a stopwatch area.

        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        // limit screen width to 1024 which is enough.
        if (screenWidth>1024)
            screenWidth=1024;


        bottomLineVerticalPosition = fullHeight -chronoDigitBaseLine-SPACING;
        middleLineVerticalPosition = fullHeight -chronoDigitHeight-otherTextBaseLine-SPACING;
        topLineVerticalPosition = fullHeight-chronoDigitHeight-otherTextCharHeight -otherTextBaseLine-SPACING;


        //
        lapTimeHorizontalLimit = screenWidth-allDigitWidth/3;
        ssrHorizontalLimit = screenWidth-allDigitWidth*3/4;
        paramHorizontalLimit = (screenWidth-ssrHorizontalLimit)/2;

    }

    public static ZONE_ACTION getUserAction(float x){
        if (x>lapTimeHorizontalLimit && x<screenWidth)
            return LAP_TIME;
        if (x<lapTimeHorizontalLimit && x> ssrHorizontalLimit)
            return START_STOP_RESET;
        if (x<ssrHorizontalLimit && x > paramHorizontalLimit)
            return PARAM;
        return SHOW_HIDE;

    }

    public  static  int getScreenWidth(){
        return screenWidth;
    }

}
