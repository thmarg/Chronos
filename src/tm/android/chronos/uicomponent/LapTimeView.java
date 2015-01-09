/*
 * LapTimeView
 *
 *   Copyright (c) 2014 Thierry Margenstern under MIT license
 *   http://opensource.org/licenses/MIT
 */

package tm.android.chronos.uicomponent;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import tm.android.chronos.core.Digit;
import tm.android.chronos.core.StopwatchData;
import tm.android.chronos.core.StopwatchDataRow;
import tm.android.chronos.core.Units;

/**
 * View used to display lap times attached to a stopwatch, as child in an ExpandableListView.
 * Click time is the time a user made the action to time a lap
 * real time is the real time for the lap
 * distance is the distance if any
 * speed is the computed speed if possible.
 * This view has no input field.
 */
public class LapTimeView extends LinearLayout {

    private StopwatchData stopwatchData;
    private StopwatchDataRow stopwatchDataRow;

    private TextView txtView_clickTime;
    private TextView txtView_realTime;
    private TextView txtView_Distance;
    private TextView txtView_DistanceUnit;
    private TextView txtView_Speed;
    private TextView txtView_SpeedUnit;

    public LapTimeView(Context context) {
        super(context);
    }

    public LapTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(StopwatchData stopwatchData, int currentRow) {
        this.stopwatchData = stopwatchData;
        stopwatchDataRow = stopwatchData.getStopwatchDataRow(currentRow);

        if (stopwatchDataRow==null)
            return;

        setOrientation(HORIZONTAL);
        setGravity(Gravity.RIGHT);

        setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));

        if (stopwatchDataRow.getLength()>0) {
            LayoutParams layoutParams2 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            txtView_Speed = new TextView(getContext());
            txtView_Speed.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
            txtView_Speed.setTextAlignment(TEXT_ALIGNMENT_TEXT_END);
            txtView_Speed.setLayoutParams(layoutParams2);
            double speed = Units.getSpeed(stopwatchData.getgLength(), stopwatchData.getLengthUnit(), stopwatchDataRow.getDiffTime(), stopwatchData.getSpeedUnit());
            txtView_Speed.setText(String.format("%1$.2f", speed));
            addView(txtView_Speed);
            //
            LayoutParams layoutParams3 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            txtView_SpeedUnit = new TextView(getContext());
            txtView_SpeedUnit.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
            //txtView_SpeedUnit.setTextAlignment(TEXT_ALIGNMENT_TEXT_END);
            txtView_SpeedUnit.setLayoutParams(layoutParams3);
            txtView_SpeedUnit.setText(stopwatchData.getSpeedUnit().toString());
            addView(txtView_SpeedUnit);
        }
        //
        LayoutParams layoutParams1 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        txtView_realTime = new TextView(getContext());
        txtView_realTime.setTextAppearance(getContext(),android.R.style.TextAppearance_Medium);
        //txtView_realTime.setTextAlignment(TEXT_ALIGNMENT_TEXT_END);
        txtView_realTime.setLayoutParams(layoutParams1);
        txtView_realTime.setText(Digit.split(stopwatchDataRow.getDiffTime()).toString());
        addView(txtView_realTime);
        //
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin= (int)(getResources().getDisplayMetrics().density*10);
        txtView_clickTime = new TextView(getContext());
        txtView_clickTime.setTextAppearance(getContext(),android.R.style.TextAppearance_Medium);
        //txtView_clickTime.setTextAlignment(TEXT_ALIGNMENT_TEXT_END);
        txtView_clickTime.setLayoutParams(layoutParams);
        txtView_clickTime.setText(Digit.split(stopwatchDataRow.getClickTime()).toString());
        addView(txtView_clickTime);
        //

        //



    }
}
