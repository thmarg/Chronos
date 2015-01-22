/*
 *  ChronometerActivity
 *
 *   Copyright (c) 2014 Thierry Margenstern under MIT license
 *   http://opensource.org/licenses/MIT
 */

package tm.android.chronos.activity;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import tm.android.chronos.R;
import tm.android.chronos.core.ClockWorker;
import tm.android.chronos.core.Digit;
import tm.android.chronos.core.Stopwatch;
import tm.android.chronos.core.Units;
import tm.android.chronos.uicomponent.BaseChronographe;
import tm.android.chronos.uicomponent.Chronographe;


/**
 *
 */
public class ChronometerActivity<T extends Stopwatch> extends Activity {


	private ClockWorker<T> clockWorker;
	private Chronographe<T> chronographe;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chronolayout);

		chronographe = new Chronographe<T>(this);
		clockWorker = chronographe.getClockWorker();
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Chronographe.getScreenWidth(), ViewGroup.LayoutParams.MATCH_PARENT);
		//layoutParams.rightMargin = Chronographe2.getLeftMargin();
		chronographe.setLayoutParams(layoutParams);
		chronographe.setBackground(new ColorDrawable(Color.TRANSPARENT));
		//chronographe.setVerticalScrollBarEnabled(true);
		//chronographe.setScrollContainer(true);
		((LinearLayout) findViewById(R.id.fond)).addView(chronographe);


		//adaptation to very wide screen so that the application stay centered and no more wide than 1024 pixel which is enough See BaseChronographe.
		RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.mainlayout);
		FrameLayout.LayoutParams layoutParams1 = new FrameLayout.LayoutParams(BaseChronographe.getScreenWidth(), ViewGroup.LayoutParams.MATCH_PARENT);
		layoutParams1.gravity = Gravity.CENTER;

		relativeLayout.setLayoutParams(layoutParams1);

		// don't forget this for i18n support from some component (>50%).
		Units.setResources(getResources());
		Digit.setInitilaDigitFormat(Units.DIGIT_FORMAT.VERY_SHORT);
		LongClick longClick = new LongClick();
		// add to pauseSelectedStopwatch a stopwatch on long press otherwise the pauseSelectedStopwatch button take a lap time.
		findViewById(R.id.btn_pause).setOnLongClickListener(longClick);
		findViewById(R.id.btn_stop).setOnLongClickListener(longClick);
		findViewById(R.id.btn_reset).setOnLongClickListener(longClick);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}


	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {

		clockWorker.finalStop();
		super.onDestroy();
	}

	/*
	Management of the button at the bottom of the screen
	 */
	public void onClick(View view) {
		long now = System.currentTimeMillis();
		switch (view.getId()) {
			case R.id.img_btn_plus:
				chronographe.addNewStopwatch();
				break;
			case R.id.img_btn_moins:
				chronographe.removeLastStopwatch();
				break;
			case R.id.btn_start:
				chronographe.startSelectedStopwatch(now);
				break;
			case R.id.btn_stop:
				chronographe.stopSelectedStopwatch(now);
				break;
			case R.id.btn_pause:
				chronographe.lapTimeSelectedStopwatch(now);
				break;
			case R.id.btn_reset:
				chronographe.resetSelectedStopwatch();
				break;
		}
	}

	private class LongClick implements View.OnLongClickListener {

		@Override
		public boolean onLongClick(View view) {
			long now = System.currentTimeMillis();
			switch (view.getId()){
				case R.id.btn_pause:
					return true;
				case R.id.btn_stop:
					clockWorker.stopAllStopwatches(now);
					chronographe.updateUI();
					return true;
				case R.id.btn_reset:
					if (clockWorker.noneChronoIsRunning()) {
						clockWorker.resetAllStopwatches();
						chronographe.updateUI();
					}
			}
			return false;
		}
	}


	@Override
	public boolean dispatchKeyEvent(@SuppressWarnings("NonNull") KeyEvent event) {
		long now = System.currentTimeMillis();
		if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
			T stopwatch = chronographe.getSelectedStopwatch();
			if (stopwatch == null)
				return true;
			switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_VOLUME_DOWN:
					if (event.getAction() == KeyEvent.ACTION_DOWN)
						if (stopwatch.isWaitingStart())
							chronographe.startSelectedStopwatch(now);
						else if (stopwatch.isRunning())
							chronographe.stopSelectedStopwatch(now);
					break;
				case KeyEvent.KEYCODE_VOLUME_UP:
					if (event.getAction() == KeyEvent.ACTION_DOWN)
						if (stopwatch.isRunning())
							if (event.isLongPress())
								chronographe.pauseSelectedStopwatch();
							else
								chronographe.lapTimeSelectedStopwatch(now);
						else if (stopwatch.isStopped())
							chronographe.resetSelectedStopwatch();
					break;
				default:
			}
			return true;
		}
		return super.dispatchKeyEvent(event);
	}
}
