/*
 * StopwatchPreference
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.preference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import tm.android.chronos.R;
import tm.android.chronos.uicomponent.StopWatchUI2;

import static tm.android.chronos.preference.PreferenceCst.PREF_KEYS.*;


/**
 * Preferences for stopwatches<br>
 * Choices are :
 * <ul>
 * <li>Display start ftime.</li>
 * <li>Display start time</li>
 * <li>Allow remove running stopwatches</li>
 * </ul>
 * Pref are store on fly after each click on checkboxes
 */
public class StopwatchPreference extends Preference {

    private String prefix; //
    //
    private CheckBox ckb_dsp_startTime;
    private CheckBox ckb_allow_rm;
    private CheckBox ckb_dsp_startDate;
    //

    public StopwatchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        setLayoutResource(R.layout.stopwatchpref);
        LayoutInflater inflater = LayoutInflater.from(getContext());

        return inflater.inflate(R.layout.stopwatchpref, null);
    }


    @Override
    protected void onBindView(@SuppressWarnings("NonNull") View view) {
        super.onBindView(view);

        prefix = ((Activity) getContext()).getIntent().getStringExtra(PreferenceCst.PREFIX_BUNDLE_KEY);
        SharedPreferences preferences = getContext().getSharedPreferences(PreferenceCst.PREF_STORE_NAME, 0);

        ckb_dsp_startTime = view.findViewById(R.id.ckb_dsp_startTime);
        ckb_allow_rm = view.findViewById(R.id.ckb_allow_rm);
        ckb_dsp_startDate = view.findViewById(R.id.ckb_dsp_startDate);

        ckb_dsp_startTime.setChecked(preferences.getBoolean(prefix + STOPWATCH_DSP_START_TIME, false));
        ckb_allow_rm.setChecked(preferences.getBoolean(prefix + STOPWATCH_ALLOW_RM_RUNNING, false));
        ckb_dsp_startDate.setChecked(preferences.getBoolean(prefix + STOPWATCH_DSP_START_DATE, false));
        View.OnClickListener onClickListener = new ClickListener();
        ckb_dsp_startTime.setOnClickListener(onClickListener);
        ckb_dsp_startDate.setOnClickListener(onClickListener);
        ckb_allow_rm.setOnClickListener(onClickListener);

    }

//

    private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            SharedPreferences.Editor preferencesEditor = getContext().getSharedPreferences(PreferenceCst.PREF_STORE_NAME, 0).edit();
            switch (view.getId()) {
                case R.id.ckb_dsp_startDate:
                    preferencesEditor.putBoolean(prefix + STOPWATCH_DSP_START_DATE, ckb_dsp_startDate.isChecked());
                    StopWatchUI2.setShowStartDate(ckb_dsp_startDate.isChecked());
                    break;
                case R.id.ckb_dsp_startTime:
                    preferencesEditor.putBoolean(prefix + STOPWATCH_DSP_START_TIME, ckb_dsp_startTime.isChecked());
                    StopWatchUI2.setShowStartTime(ckb_dsp_startTime.isChecked());
                    break;
                case R.id.ckb_allow_rm:
                    preferencesEditor.putBoolean(prefix + STOPWATCH_ALLOW_RM_RUNNING, ckb_allow_rm.isChecked());
                    break;
            }
            preferencesEditor.apply();
        }
    }

}

