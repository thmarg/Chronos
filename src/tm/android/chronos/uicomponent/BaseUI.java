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
	private static Paint paintWhiteRigthDigitSize;




	static {
		float density =  Resources.getSystem().getDisplayMetrics().density;
		float chronotextSize = density * CHRONO_TEXT_SIZE_IN_DP;// used by painter
		//

		paintWhiteRigthDigitSize = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintWhiteRigthDigitSize.setTextSize(chronotextSize);
		paintWhiteRigthDigitSize.setTextAlign(Paint.Align.RIGHT);
		paintWhiteRigthDigitSize.setColor(Color.WHITE);
	}

	public static Paint getPaintWhiteRigthDigitSize() {
		return paintWhiteRigthDigitSize;
	}
}
