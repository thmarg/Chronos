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


/**
 * This "static" class prepare common data for dimension and paint
 */
public class BaseUI {
	private  final static int CHRONO_TEXT_SIZE_IN_DP = 30;
	protected  final static int TEXT_NORMAL_SIZE_IN_DP =20; //  other texte normal
	private static Paint paintWhiteRigthDigitSize;
	public   final static int SPACING = (int)(4*Resources.getSystem().getDisplayMetrics().density);

	protected static int normalTextBlocHeight;
	protected static int digitTextBlocHeight;
	//
	protected static int digitTextBaseLineVerticalOffset;
	protected static int normalTextBaseLineVerticalOffset;
	//
	protected static Paint paintNormalTextWhiteRigth;



	static {
		float density =  Resources.getSystem().getDisplayMetrics().density;
		float chronotextSize = density * CHRONO_TEXT_SIZE_IN_DP;// used by painter
		float normalTextSize = density* TEXT_NORMAL_SIZE_IN_DP;

		paintWhiteRigthDigitSize = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintWhiteRigthDigitSize.setTextSize(chronotextSize);
		paintWhiteRigthDigitSize.setTextAlign(Paint.Align.RIGHT);
		paintWhiteRigthDigitSize.setColor(Color.WHITE);
		//
		paintNormalTextWhiteRigth = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintNormalTextWhiteRigth.setTextSize(normalTextSize);
		paintNormalTextWhiteRigth.setTextAlign(Paint.Align.RIGHT);
		paintNormalTextWhiteRigth.setColor(Color.WHITE);

		digitTextBlocHeight = paintWhiteRigthDigitSize.getFontMetricsInt().bottom- paintWhiteRigthDigitSize.getFontMetricsInt().top;
		digitTextBaseLineVerticalOffset = -paintWhiteRigthDigitSize.getFontMetricsInt().top;


		normalTextBlocHeight = paintNormalTextWhiteRigth.getFontMetricsInt().bottom- paintNormalTextWhiteRigth.getFontMetricsInt().top;
		normalTextBaseLineVerticalOffset=-paintNormalTextWhiteRigth.getFontMetricsInt().top;
	}

	public static Paint getPaintWhiteRigthDigitSize() {
		return paintWhiteRigthDigitSize;
	}



	public static int getDigitTextBaseLineVerticalOffset() {
		return digitTextBaseLineVerticalOffset;
	}

	public static int getDigitTextBlocHeight() {
		return digitTextBlocHeight;
	}

	public static int getNormalTextBaseLineVerticalOffset() {
		return normalTextBaseLineVerticalOffset;
	}



	public static Paint getPaintNormalTextWhiteRigth() {
		return paintNormalTextWhiteRigth;
	}
}
