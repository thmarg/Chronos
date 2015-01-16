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

    protected  final static int CHRONO_TEXT_SIZE_IN_DP = 30;//  good base size for the chrono digits
    protected  final static int TEXT_NORMAL_SIZE_IN_DP =20; //  other texte normal
    protected  final static int TEXT_SMALL_SIZE_IN_DP =16; // other text small
    protected  final static int SPACING = (int)(4*Resources.getSystem().getDisplayMetrics().density);// extra space around blocs of letters.

    protected static Paint paintWhiteRigthDigitSize;// to paint digit right align
    protected static Paint paintDigit;
    protected static Paint paintDigitsWhileRunning; // digit while running
    protected static Paint paintBackgroundBlack;// to erase

    protected static Paint paintNormalTextWhiteRigth;
    protected static Paint paintNormalTextWhiteLeft;
    protected static Paint paintSmallTextWhiteRigth;
    protected static Paint paintSmallTextWhiteLeft;
    protected static Paint paintLine;

    protected static int screenWidth;// pixel

    /* max digits length of a stopwatch ie when "000:00:00:00:000"  plus h spacing included   */
    protected static int digitMaxLength;
    /* max height of a bloc head*/
    protected static int fullHeight;

    /* Line spe between two stopwatches */
    protected static int groupBottomLineHeight = 3*SPACING;

    // horizontal user touch zone limit
    private static int lapTimeHorizontalLimit;
    private static int ssrHorizontalLimit; // start stop reset
    private static int paramHorizontalLimit;

    /* Explain
     * ********************************** top
     *
     * ********************************** baseLine
     * ********************************** bottom
     * below BlocHeight is bottom -top // bloc to reset back ground with drawRect on canvas
     * baseLineVerticalOffset est baseline -top // where to write with drawText on canvas
     *
     * digit reference the font properties to render the digits of the stopwatch.
     * normal reference the font properties to render info in the head bloc of the stopwatch.
     * small reference the font properties to render details of the stopwatch (lap time ...)
     */

    protected static int normalTextBlocHeight;
    protected static int smallTextBlocHeight;
    protected static int digitTextBlocHeight;

    protected static int normalTextBaseLineVerticalOffset;
    protected static int smallTextBaseLineVerticalOffset;
    protected static int digitTextBaseLineVerticalOffset;



    protected static int colorStopped = Color.parseColor("#f16b36");
    protected static int colorReseted = Color.WHITE;
    protected static int colorBottomGroup=Color.parseColor("#2982cd");
    protected static int colorSepHeadDetail=Color.parseColor("#8e9da0");
    protected static int colorRunning = Color.parseColor("#049d2b");



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
        float normalTextSize = density* TEXT_NORMAL_SIZE_IN_DP;
        float smallTextSize = density*TEXT_SMALL_SIZE_IN_DP;
        //
        Rect r = new Rect();
        //
        paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLine.setColor(Color.BLUE);
        //

        paintNormalTextWhiteRigth = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintNormalTextWhiteRigth.setTextSize(normalTextSize);
        paintNormalTextWhiteRigth.setTextAlign(Paint.Align.RIGHT);
        paintNormalTextWhiteRigth.setColor(Color.WHITE);
        //
        paintNormalTextWhiteLeft = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintNormalTextWhiteLeft.setTextSize(normalTextSize);
        paintNormalTextWhiteLeft.setTextAlign(Paint.Align.LEFT);
        paintNormalTextWhiteLeft.setColor(Color.WHITE);
        //

        paintSmallTextWhiteRigth = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSmallTextWhiteRigth.setTextSize(smallTextSize);
        paintSmallTextWhiteRigth.setTextAlign(Paint.Align.RIGHT);
        paintSmallTextWhiteRigth.setColor(Color.WHITE);

        paintSmallTextWhiteLeft = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSmallTextWhiteLeft.setTextSize(smallTextSize);
        paintSmallTextWhiteLeft.setTextAlign(Paint.Align.LEFT);
        paintSmallTextWhiteLeft.setColor(Color.WHITE);

        paintDigit = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintDigit.setTextSize(chronotextSize);
        paintDigit.setTextAlign(Paint.Align.RIGHT);
        paintDigit.setColor(Color.WHITE);

        paintWhiteRigthDigitSize = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintWhiteRigthDigitSize.setTextSize(chronotextSize);
        paintWhiteRigthDigitSize.setTextAlign(Paint.Align.RIGHT);
        paintWhiteRigthDigitSize.setColor(Color.WHITE);


        paintDigitsWhileRunning = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintDigitsWhileRunning.setTextSize(chronotextSize);
        paintDigitsWhileRunning.setTextAlign(Paint.Align.RIGHT);
        paintDigitsWhileRunning.setColor(colorRunning);

        paintBackgroundBlack = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBackgroundBlack.setColor(Color.BLACK);
        //
        //
        digitTextBlocHeight = paintDigit.getFontMetricsInt().bottom- paintDigit.getFontMetricsInt().top;
        digitTextBaseLineVerticalOffset = -paintDigit.getFontMetricsInt().top;


        paintDigit.getTextBounds("000:00:00:00:000", 0, 16, r);
        digitMaxLength = r.width()+3*SPACING;

        normalTextBlocHeight = paintNormalTextWhiteLeft.getFontMetricsInt().bottom- paintNormalTextWhiteLeft.getFontMetricsInt().top;
        normalTextBaseLineVerticalOffset=-paintSmallTextWhiteLeft.getFontMetricsInt().top;

        smallTextBlocHeight = paintSmallTextWhiteRigth.getFontMetricsInt().bottom-paintSmallTextWhiteRigth.getFontMetricsInt().top;
        smallTextBaseLineVerticalOffset = -paintSmallTextWhiteRigth.getFontMetricsInt().top;


        fullHeight = digitTextBlocHeight+3* normalTextBlocHeight;// used as max head height to extend the bufferBitmap..

        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        // limit screen width to 1024 which is enough.
        if (screenWidth>1024)
            screenWidth=1024;

        //
        lapTimeHorizontalLimit = screenWidth- digitMaxLength /3;
        ssrHorizontalLimit = screenWidth- digitMaxLength *3/4;
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
