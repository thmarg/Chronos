/*
 * ${NAME}
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.uicomponent;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;


/**
 * This "static" class prepare common data for dimension and paint
 */
public class BaseUI {
    public final static int SCREENWIDTH;
    public final static int SCREENHEIGHT;
    private final static float density = Resources.getSystem().getDisplayMetrics().density;
    public final static int SPACING = (int) (4 * density);
    private final static int TEXT_NORMAL_SIZE_IN_DP = 20; //  other texte normal
    private final static int TEXT_SMALL_SIZE_IN_DP = 16; // other text small
    private final static int TEXT_VERY_SMALL_SIZE_IN_DP = 12; // very small
    private final static float verySmallTextSize = BaseUI.density * BaseUI.TEXT_VERY_SMALL_SIZE_IN_DP;
    final static int groupBottomLineHeight = 4 * SPACING; //
    final static int normalTextBlocHeight;
    final static float normalTextSize = density * TEXT_NORMAL_SIZE_IN_DP;
    final static int normalTextDigitWidth;
    final static int digitTextBlocHeight;
    final static int smallTextBaseLineVerticalOffset;
    final static int smallTextBlocHeight;
    //
    final static int digitTextBaseLineVerticalOffset;
    final static int normalTextBaseLineVerticalOffset;
    //
    final static Paint paintNormalTextWhiteRigth;
    final static Paint paintNormalTextWhiteLeft;
    private final static Paint paintNormalTextWhiteCenter;
    final static Paint paintNoText;
    final static Paint paintWhiteRigthDigitSize;
    final static Paint paintSmallTextWhiteRigth;
    final static Paint paintDigit;
    final static Paint paintVerySmallTextWhiteLeft;
    private final static int digitMaxLength;
    final static int lapTimeHorizontalLimit;
    final static int ssrHorizontalLimit; // startSelectedStopwatch stopSelectedStopwatch resetSelectedStopwatch
    final static int paramHorizontalLimit;
    private final static int CHRONO_TEXT_SIZE_IN_DP = 30;

    static {
        SCREENWIDTH = Resources.getSystem().getDisplayMetrics().widthPixels;
        SCREENHEIGHT = Resources.getSystem().getDisplayMetrics().heightPixels;
        float chronotextSize = density * CHRONO_TEXT_SIZE_IN_DP;// used by painter
        float smallTextSize = density * TEXT_SMALL_SIZE_IN_DP;
        Rect r = new Rect();


        paintDigit = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintDigit.setTextSize(chronotextSize);
        paintDigit.setTextAlign(Paint.Align.RIGHT);
        paintDigit.setColor(Color.WHITE);

        paintWhiteRigthDigitSize = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintWhiteRigthDigitSize.setTextSize(chronotextSize);
        paintWhiteRigthDigitSize.setTextAlign(Paint.Align.RIGHT);
        paintWhiteRigthDigitSize.setColor(Color.WHITE);

        paintNormalTextWhiteLeft = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintNormalTextWhiteLeft.setTextSize(normalTextSize);
        paintNormalTextWhiteLeft.setTextAlign(Paint.Align.LEFT);
        paintNormalTextWhiteLeft.setColor(Color.WHITE);

        paintSmallTextWhiteRigth = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSmallTextWhiteRigth.setTextSize(smallTextSize);
        paintSmallTextWhiteRigth.setTextAlign(Paint.Align.RIGHT);
        paintSmallTextWhiteRigth.setColor(Color.WHITE);
        //
        paintNormalTextWhiteRigth = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintNormalTextWhiteRigth.setTextSize(normalTextSize);
        paintNormalTextWhiteRigth.setTextAlign(Paint.Align.RIGHT);
        paintNormalTextWhiteRigth.setColor(Color.WHITE);
        //
        paintNormalTextWhiteCenter = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintNormalTextWhiteCenter.setTextSize(normalTextSize);
        paintNormalTextWhiteCenter.setTextAlign(Paint.Align.CENTER);
        paintNormalTextWhiteCenter.setColor(Color.WHITE);
        //
        paintNoText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintNoText.setColor(Color.BLACK);
        //
        paintVerySmallTextWhiteLeft = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintVerySmallTextWhiteLeft.setTextSize(BaseUI.verySmallTextSize);
        paintVerySmallTextWhiteLeft.setTextAlign(Paint.Align.LEFT);
        paintVerySmallTextWhiteLeft.setColor(Color.WHITE);
        paintVerySmallTextWhiteLeft.setFakeBoldText(true);
        paintVerySmallTextWhiteLeft.setUnderlineText(true);
        //

        digitTextBlocHeight = paintWhiteRigthDigitSize.getFontMetricsInt().bottom - paintWhiteRigthDigitSize.getFontMetricsInt().top;
        digitTextBaseLineVerticalOffset = -paintWhiteRigthDigitSize.getFontMetricsInt().top;


        normalTextBlocHeight = paintNormalTextWhiteRigth.getFontMetricsInt().bottom - paintNormalTextWhiteRigth.getFontMetricsInt().top;
        normalTextBaseLineVerticalOffset = -paintNormalTextWhiteRigth.getFontMetricsInt().top;
        Rect rect = new Rect();
        paintNormalTextWhiteRigth.getTextBounds("25", 0, 2, rect);
        normalTextDigitWidth = SPACING + rect.right - rect.left;
        smallTextBlocHeight = paintSmallTextWhiteRigth.getFontMetricsInt().bottom - paintSmallTextWhiteRigth.getFontMetricsInt().top;
        smallTextBaseLineVerticalOffset = -paintSmallTextWhiteRigth.getFontMetricsInt().top;

        paintDigit.getTextBounds("000:00:00:00:000", 0, 16, r);
        digitMaxLength = r.width() + 3 * SPACING;

        lapTimeHorizontalLimit = SCREENWIDTH - digitMaxLength / 3;
        ssrHorizontalLimit = SCREENWIDTH - digitMaxLength * 3 / 4;
        paramHorizontalLimit = (SCREENWIDTH - ssrHorizontalLimit) / 2;
    }

    public BaseUI() {
    }

    static Paint getPaintWhiteRigthDigitSize() {
        return paintWhiteRigthDigitSize;
    }


//    public static int getDigitTextBaseLineVerticalOffset() {
//        return digitTextBaseLineVerticalOffset;
//    }
//
//    public static int getDigitTextBlocHeight() {
//        return digitTextBlocHeight;
//    }
//
//    public static int getNormalTextBaseLineVerticalOffset() {
//        return normalTextBaseLineVerticalOffset;
//    }

    static Paint getPaintNormalTextWhiteCenter() {
        return paintNormalTextWhiteCenter;
    }

    static Paint getPaintNormalTextWhiteRigth() {
        return paintNormalTextWhiteRigth;
    }

    static Paint getPaintNoText() {
        return paintNoText;
    }

    static float getNormalTextSize() {
        return normalTextSize;
    }

    public static int getPixels(float dpSize) {
        return (int) (density * dpSize + 0.5f);
    }
}
