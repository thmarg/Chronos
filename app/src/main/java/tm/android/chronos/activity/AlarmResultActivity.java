package tm.android.chronos.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import tm.android.chronos.R;
import tm.android.chronos.audio.CommonMediaPlayer;
import tm.android.chronos.core.Alarm;
import tm.android.chronos.core.AlarmData;
import tm.android.chronos.core.Units;
import tm.android.chronos.services.AlarmServices;
import tm.android.chronos.sql.DbConstant;
import tm.android.chronos.sql.DbLiveObject;

public class AlarmResultActivity extends Activity {
    private Spinner spn_repeat;
    private GridLayout layout_repeat;
    private Alarm alarm;
    private Runner runner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Chronos.name + "-AlarmResult..", "onCreate start");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            setShowWhenLocked(true);
        else
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.alarmresultlayout);
        long alarmId = getIntent().getLongExtra("AlarmId", -1);

        spn_repeat = findViewById(R.id.spn_repeat);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.repeat_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_repeat.setAdapter(adapter);

        OnClickListener onClickListener = new OnClickListener();
        Button btn_close = findViewById(R.id.btn_close);
        btn_close.setOnClickListener(onClickListener);

        Button btn_repeat = findViewById(R.id.btn_repeat);
        btn_repeat.setOnClickListener(onClickListener);

        Button btn_launch_repeat = findViewById(R.id.btn_launch_repeat);
        btn_launch_repeat.setOnClickListener(onClickListener);

        layout_repeat = findViewById(R.id.layout_repeat);
        layout_repeat.setVisibility(View.GONE);


        DbLiveObject<Alarm> dbLiveObject = new DbLiveObject<>(this);
        alarm = dbLiveObject.getRunningLiveObjectById(DbConstant.RUNNING_ALARMS_TABLE_NAME, alarmId);
        ((TextView) findViewById(R.id.txv_name)).setText(alarm.getName());
        ((TextView) findViewById(R.id.txv_desc)).setText(alarm.getAlarmData().getDescription());
        dbLiveObject.close();

        TextView txv_title = findViewById(R.id.txv_title);
        runner = new Runner(txv_title,alarm);
        txv_title.post(runner);
        Log.i(Chronos.name + "-AlarmResult..", "onCreate Done");
    }

    private void close(){ // for btn_close or onBackPressed
        CommonMediaPlayer.Instance().stopAll();
        alarm.resetRepeatCount();
        // check if this alarm has to be planned again
        if (alarm.getAlarmData().getType() != AlarmData.ALARM_TYPE.ONCE) {
            if (alarm.getAlarmData().getType() == AlarmData.ALARM_TYPE.REPEATED_LOOP || alarm.getAlarmData().getType() == AlarmData.ALARM_TYPE.REPEATED_LOOP_SPEC_TIME)
                alarm.updateEndTimeForRepeatedLoop();
            else
                alarm.updateEndTimeForRepeated();

            if (alarm.isRunning() && !alarm.isPassed()) {
                alarm.setFirstEndTime(alarm.getEndTime());
                AlarmServices.updateAlarm(AlarmResultActivity.this, alarm);
            }
        }
        DbLiveObject.storeAlarm(getBaseContext(),alarm);
    }


    @Override
    public void onBackPressed() {
        close();
        super.onBackPressed();

    }

    private class OnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_close:
                    close();
                    finish();
                    break;
                case R.id.btn_repeat:
                    layout_repeat.setVisibility(layout_repeat.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                    break;
                case R.id.btn_launch_repeat:
                    CommonMediaPlayer.Instance().stopAll();
                    String[] tok = ((String) spn_repeat.getSelectedItem()).split(" ");
                    alarm.addRepeatCount();
                    alarm.setEndTime(System.currentTimeMillis() + 60000 * Integer.valueOf(tok[0]));
                    alarm.restart();
                    AlarmServices.registerIntoAlarmManager(getBaseContext(), alarm);
                    DbLiveObject.storeAlarm(getBaseContext(),alarm);
                    finish();
                    break;
            }
        }

    }


    /**
     * A simple Runnable to update the current time displayed in the "title", each second.
     * Use post method on txv_title
     */
    private class Runner implements Runnable {
        private TextView textView;
        private Alarm alarm;
        private String titlePart1;
        private String titlePart2;
        private String repeatCount = "";

        Runner(TextView textView,Alarm alarm){
            this.textView = textView;
            this.alarm = alarm;
            titlePart1 = Units.getLocalizedTextWithParams("alarm_title1", Chronos.fdate.format(alarm.getFirstEndTime()), Chronos.ftime.format(alarm.getFirstEndTime())) + "\n";
            titlePart2 = Units.getLocalizedText(R.string.alarm_title2)+" ";
            if (alarm.hasBeenRepeated())
                repeatCount = Units.getLocalizedTextWithParams("alarm_repeat_count", String.valueOf(alarm.getRepeatCount()))+"\n";
        }

        @Override
        public void run() {
            String text;
            if (alarm.hasBeenRepeated())
                text = titlePart1 + repeatCount + titlePart2 + Chronos.ftime.format(System.currentTimeMillis());
            else
                text = titlePart1 + titlePart2 + Chronos.ftime.format(System.currentTimeMillis());

            textView.setText(text);

            if (runner != null)
                textView.postDelayed(runner,1000);
        }
    }

}
