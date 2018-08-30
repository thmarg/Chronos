/*
 * ChronographeDialog
 *
 * Copyright (c) 2014 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.*;
import android.widget.*;
import tm.android.chronos.R;
import tm.android.chronos.activity.Chronos;
import tm.android.chronos.audio.AudioProperties;
import tm.android.chronos.core.*;
import tm.android.chronos.preference.AudioNotificationPreference;
import tm.android.chronos.preference.AudioNotificationPreferenceFragment;
import tm.android.chronos.preference.PreferenceCst;
import tm.android.chronos.preference.PreferencesActivity;
import tm.android.chronos.uicomponent.BaseUI;
import tm.android.chronos.util.Couple;
import tm.android.chronos.util.Permissions;

import java.text.ParseException;
import java.util.Hashtable;

/**
 *
 */
public class AlarmDialog extends DialogFragment {

    private OnDialogClickListener dialogClickListener;
    private static Object self;
    private Alarm alarm; //
    private String oldDateValue = "";
    private String oldTimeValue = "";

    public enum MODE {CREATE, EDIT, VIEW}

    private MODE mode;
    private boolean typeChanged = false;
    private Hashtable<Integer, Couple<Integer, DaysOfWeek.DAYS>> mapper;
    private Hashtable<Integer, Integer> mapper2;
    private TableLayout table_date_time;
    private ClickListener clickListener;
    private Hashtable<Integer, AudioProperties> repeatedAudioPref;
    private View specLayout = null; // layout for each alarm type except alarm type ONCE
    private ScrollView alarm_repeat_loop_spec_time;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, 0);
        repeatedAudioPref = new Hashtable<>(5);
        AlarmDialog.self = this;
    }

    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // inflate the views
        final LinearLayout view_dateTime = (LinearLayout) inflater.inflate(R.layout.alarmdatetime, null);
        final ScrollView alarm_repeat = (ScrollView) inflater.inflate(R.layout.alarmrepeat, null);
        final LinearLayout alarm_repeat_loop = (LinearLayout) inflater.inflate(R.layout.alarmrepeatloop, null);
        alarm_repeat_loop_spec_time = (ScrollView) inflater.inflate(R.layout.alarmrepeatloopspectime, null);

        builder.setView(view_dateTime);
        builder.setTitle(Units.getLocalizedText("alarm_title_settings", null));

        if (mode == MODE.CREATE) {
            alarm = new Alarm();
            alarm.setEndTime(System.currentTimeMillis() + 60000);
            AudioProperties audioProperties = new AudioProperties();
            audioProperties.loadFromPref(PreferenceCst.PREFIX_ALARM, getActivity());
            alarm.getAlarmData().setAudioProperties(audioProperties);
        } // no else, in EDIT mode

        ((EditText) view_dateTime.findViewById(R.id.edt_alarm_name)).setText(alarm.getName());
        ((EditText) view_dateTime.findViewById(R.id.edt_alarm_desc)).setText(alarm.getAlarmData().getDescription());


        ((EditText) view_dateTime.findViewById(R.id.edt_date)).setText(Chronos.fdate.format(alarm.getEndTime()));
        oldDateValue = ((EditText) view_dateTime.findViewById(R.id.edt_date)).getText().toString();
        ((EditText) view_dateTime.findViewById(R.id.edt_time)).setText(Chronos.ftime.format(alarm.getEndTime()));
        oldTimeValue = ((EditText) view_dateTime.findViewById(R.id.edt_time)).getText().toString();

        clickListener = new ClickListener();
        view_dateTime.findViewById(R.id.txv_audioPref).setOnClickListener(clickListener);

        mapper = new Hashtable<>(14);
        mapper2 = new Hashtable<>(7);


        final Spinner spn_type = view_dateTime.findViewById(R.id.spn_type);
        ArrayAdapter<Couple<AlarmData.ALARM_TYPE, String>> spinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        spinnerAdapter.addAll(AlarmData.getAlarmTypeForSpinner());
        spn_type.setAdapter(spinnerAdapter);

        spn_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Couple<AlarmData.ALARM_TYPE, String> value = (Couple<AlarmData.ALARM_TYPE, String>) spn_type.getSelectedItem();
                typeChanged = alarm.getAlarmData().getType() != value.getKey();

                // fixed part
                getDialog().findViewById(R.id.txv_audioPref).setVisibility(value.getKey() == AlarmData.ALARM_TYPE.ONCE ? View.VISIBLE : View.GONE);
                ((EditText) getDialog().findViewById(R.id.edt_date)).setText(value.getKey() == AlarmData.ALARM_TYPE.ONCE ? oldDateValue : "");
                ((TextView) getDialog().findViewById(R.id.edt_time)).setText((value.getKey() == AlarmData.ALARM_TYPE.REPEATED || value.getKey() == AlarmData.ALARM_TYPE.REPEATED_LOOP_SPEC_TIME) ? "" : oldTimeValue);
                getDialog().findViewById(R.id.edt_date).setEnabled(value.getKey() == AlarmData.ALARM_TYPE.ONCE);
                getDialog().findViewById(R.id.edt_time).setEnabled(value.getKey() == AlarmData.ALARM_TYPE.ONCE || value.getKey() == AlarmData.ALARM_TYPE.REPEATED_LOOP);
                alarm.getAlarmData().setType(value.getKey());

                if (specLayout != null)
                    view_dateTime.removeView(specLayout);

                switch (value.getKey()) {
                    case ONCE:
                        specLayout = null;
                        break;
                    case REPEATED_LOOP:
                        view_dateTime.addView(alarm_repeat_loop);
                        specLayout = alarm_repeat_loop;
                        break;
                    case REPEATED_LOOP_SPEC_TIME:
                        view_dateTime.addView(alarm_repeat_loop_spec_time);
                        specLayout = alarm_repeat_loop_spec_time;
                        break;
                    case REPEATED:
                        view_dateTime.addView(alarm_repeat);
                        specLayout = alarm_repeat;
                        table_date_time = view_dateTime.findViewById(R.id.table_date_time);
                        view_dateTime.findViewById(R.id.img_btn_plus).setOnClickListener(clickListener);
                        view_dateTime.findViewById(R.id.img_btn_moins).setOnClickListener(clickListener);
                        if (!alarm.getAlarmData().getRepeatedSpecDays().isEmpty()) {
                            for (Long dateTime : alarm.getAlarmData().getRepeatedSpecDays().keySet()) {
                                repeatedAudioPref.put(table_date_time.getChildCount(), alarm.getAlarmData().getRepeatedSpecDays().get(dateTime));
                                table_date_time.addView(getDateTimeTableRow(dateTime));
                            }
                        }
                        if (table_date_time.getChildCount() == 0) // add a first row if none
                            table_date_time.addView(getDateTimeTableRow(-1L));

                        break;
                }
//
                if (value.getKey() == AlarmData.ALARM_TYPE.REPEATED_LOOP || value.getKey() == AlarmData.ALARM_TYPE.REPEATED_LOOP_SPEC_TIME) {
                    for (int num : mapper.keySet())
                        view_dateTime.findViewById(num).setOnClickListener(null);
                    mapper.clear();
                    mapper.put(R.id.ckb_monday, new Couple<>(R.id.txv_monday_audio, DaysOfWeek.DAYS.MONDAY));
                    mapper.put(R.id.ckb_tuesday, new Couple<>(R.id.txv_tuesday_audio, DaysOfWeek.DAYS.TUESDAY));
                    mapper.put(R.id.ckb_wednesday, new Couple<>(R.id.txv_wednesday_audio, DaysOfWeek.DAYS.WEDNESDAY));
                    mapper.put(R.id.ckb_thursday, new Couple<>(R.id.txv_thursday_audio, DaysOfWeek.DAYS.THURSDAY));
                    mapper.put(R.id.ckb_friday, new Couple<>(R.id.txv_friday_audio, DaysOfWeek.DAYS.FRIDAY));
                    mapper.put(R.id.ckb_saturday, new Couple<>(R.id.txv_saturday_audio, DaysOfWeek.DAYS.SATURDAY));
                    mapper.put(R.id.ckb_sunday, new Couple<>(R.id.txv_sunday_audio, DaysOfWeek.DAYS.SUNDAY));
                    mapper.put(R.id.txv_monday_audio, new Couple<>(-1, DaysOfWeek.DAYS.MONDAY));
                    mapper.put(R.id.txv_tuesday_audio, new Couple<>(-1, DaysOfWeek.DAYS.TUESDAY));
                    mapper.put(R.id.txv_wednesday_audio, new Couple<>(-1, DaysOfWeek.DAYS.WEDNESDAY));
                    mapper.put(R.id.txv_thursday_audio, new Couple<>(-1, DaysOfWeek.DAYS.THURSDAY));
                    mapper.put(R.id.txv_friday_audio, new Couple<>(-1, DaysOfWeek.DAYS.FRIDAY));
                    mapper.put(R.id.txv_saturday_audio, new Couple<>(-1, DaysOfWeek.DAYS.SATURDAY));
                    mapper.put(R.id.txv_sunday_audio, new Couple<>(-1, DaysOfWeek.DAYS.SUNDAY));
                    if (value.getKey() == AlarmData.ALARM_TYPE.REPEATED_LOOP_SPEC_TIME) {
                        mapper2.clear();
                        mapper2.put(R.id.ckb_monday, R.id.edt_time_monday);
                        mapper2.put(R.id.ckb_tuesday, R.id.edt_time_tuesday);
                        mapper2.put(R.id.ckb_wednesday, R.id.edt_time_wednesday);
                        mapper2.put(R.id.ckb_thursday, R.id.edt_time_thursday);
                        mapper2.put(R.id.ckb_friday, R.id.edt_time_friday);
                        mapper2.put(R.id.ckb_saturday, R.id.edt_time_saturday);
                        mapper2.put(R.id.ckb_sunday, R.id.edt_time_sunday);

                    }

                    // set a listener for all
                    // update checkbox and textview from alarm data : checked or not
                    for (int num : mapper.keySet()) {
                        View view1 = view_dateTime.findViewById(num);
                        view1.setOnClickListener(clickListener);
                        boolean enable_day = alarm.getAlarmData().getDaysOfWeek().isDayEnable(mapper.get(num).getValue().getNum());
                        if (view1 instanceof CheckBox)
                            ((CheckBox) view1).setChecked(enable_day);
                        else if (view1 instanceof TextView)
                            enableTextView((TextView) view1, enable_day);
                    }
                    if (value.getKey() == AlarmData.ALARM_TYPE.REPEATED_LOOP_SPEC_TIME) {
                        for (int num : mapper2.keySet()) {
                            EditText editText = view_dateTime.findViewById(mapper2.get(num));
                            editText.setEnabled(((CheckBox) view_dateTime.findViewById(num)).isChecked());
                            int day = mapper.get(num).getValue().getNum();
                            if (alarm.getAlarmData().getDaysOfWeek().isDayEnable(day) &&
                                    alarm.getAlarmData().getDaysOfWeek().getTime(day) != -1)
                                editText.setText(Digit.split(alarm.getAlarmData().getDaysOfWeek().getTime(day)).toString().trim());
                        }
                    }
                }

                if (mode == MODE.VIEW){
                    for (View view1 :view_dateTime.getTouchables())
                        view1.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spn_type.setSelection(AlarmData.getTypePosition(alarm.getAlarmData().getType()));


        // Button ok/cancel
        builder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNegativeButton(Units.getLocalizedText("cancel", null), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        return builder.create();
    }


    private TableRow getDateTimeTableRow(Long dateTime) {
        TableRow tableRow = new TableRow(getActivity());
        tableRow.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int margin = BaseUI.getPixels(5.0f);
        TextView txv_date = new TextView(getActivity());
        txv_date.setText(Units.getLocalizedText(R.string.alarm_date));
        txv_date.setRight(margin);
        tableRow.addView(txv_date);

        EditText edt_date = new EditText(getActivity());
        edt_date.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
        edt_date.setEms(6);
        edt_date.setRight(margin);
        if (dateTime > 0)
            edt_date.setText(Chronos.fdate.format(dateTime));
        edt_date.setId(Integer.MAX_VALUE - table_date_time.getChildCount());
        tableRow.addView(edt_date);

        TextView txv_time = new TextView(getActivity());
        txv_time.setText(Units.getLocalizedText(R.string.alarm_time));
        txv_time.setRight(margin);
        tableRow.addView(txv_time);

        EditText edt_time = new EditText(getActivity());
        edt_time.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME);
        edt_time.setEms(4);
        edt_time.setRight(margin);
        if (dateTime > 0)
            edt_time.setText(Chronos.ftime.format(dateTime));
        edt_time.setId(Integer.MAX_VALUE - 300 - table_date_time.getChildCount());
        tableRow.addView(edt_time);

        TextView txv_audio = new TextView(getActivity());
        txv_audio.setText(Units.getLocalizedText(R.string.audio));
        txv_audio.setRight(margin);
        txv_audio.setFocusable(true);
        txv_audio.setClickable(true);
        txv_audio.setOnClickListener(clickListener);
        txv_audio.setTextColor(Color.BLUE);
        txv_audio.setContentDescription(String.valueOf(table_date_time.getChildCount()) + "@" + String.valueOf(dateTime));
        tableRow.addView(txv_audio);

        return tableRow;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mode != MODE.VIEW) { // no validation in VIEW mode
            AlertDialog alertDialog = (AlertDialog) getDialog();
            if (alertDialog != null) {
                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                DialogClick dialogClick = new DialogClick();
                positiveButton.setOnClickListener(dialogClick);
                Window window = alertDialog.getWindow();
                if (window != null)
                    window.setLayout(BaseUI.SCREENWIDTH, WindowManager.LayoutParams.WRAP_CONTENT);
            }
        }

    }

    public void setDialogClickListener(OnDialogClickListener dialogClickListener) {
        this.dialogClickListener = dialogClickListener;
    }

    public void setAlarm(Alarm alarm) {
        this.alarm = alarm;
    }

    public Alarm getAlarm() {
        return alarm;
    }

    public void setMode(MODE mode) {
        this.mode = mode;
    }

    public static AlarmDialog getInstance() {
        return (AlarmDialog) self;
    }

    public boolean hasTypeChanged() {
        return typeChanged;
    }

    public Hashtable<Integer, AudioProperties> getRepeatedAudioPref() {
        return repeatedAudioPref;
    }

    private void enableTextView(TextView textView, boolean enable) {
        textView.setEnabled(enable);
//        textView.setFocusable(enable);
//        textView.setClickable(enable);
    }

    private void startAudioParam(AudioProperties audioProperties, int day) {
        if (Permissions.Instance().hasReadWriteExternalStorage(getActivity().getBaseContext())) {
            Intent intent = new Intent(getActivity().getBaseContext(), PreferencesActivity.class);
            intent.putExtra(PreferenceCst.PREFIX_BUNDLE_KEY, PreferenceCst.PREFIX_ALARM);
            intent.putExtra(PreferenceCst.PREF_FRAGMENT_CLASS_NAME, AudioNotificationPreferenceFragment.class.getName());
            intent.putExtra(PreferenceCst.PREF_TITLE, "audio_param_title");
            intent.putExtra("mode", AudioNotificationPreference.MODE.PROPERTIES.toString());
            if (audioProperties != null)
                intent.putExtra("AudioProperties", audioProperties);
            intent.putExtra("dayOfWeek", day);
            intent.putExtra("Alarm", alarm);
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), R.string.permission_storage_short, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Listener for audio pref, single if alarm is once, or for each day of week or ftime.
     */
    private class ClickListener implements View.OnClickListener {
        CheckBox checkBox;
        TextView textView;
        EditText editText;

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.txv_audioPref:
                    startAudioParam(alarm.getAlarmData().getAudioProperties(0), -1);
                    break;
                case R.id.img_btn_plus:
                    table_date_time.addView(getDateTimeTableRow(-1L));
                    break;
                case R.id.img_btn_moins:
                    if (table_date_time.getChildCount() > 1)
                        table_date_time.removeViewAt(table_date_time.getChildCount() - 1);
                    break;
                default:
                    if (alarm.getAlarmData().getType() == AlarmData.ALARM_TYPE.REPEATED_LOOP || alarm.getAlarmData().getType() == AlarmData.ALARM_TYPE.REPEATED_LOOP_SPEC_TIME) {
                        Couple<Integer, DaysOfWeek.DAYS> param = mapper.get(view.getId());
                        if (view instanceof CheckBox) {
                            checkBox = (CheckBox) view;
                            textView = getDialog().findViewById(param.getKey());
                            enableTextView(textView, checkBox.isChecked());
                            alarm.getAlarmData().getDaysOfWeek().setDay(param.getValue().getNum(), checkBox.isChecked());
                            if (alarm.getAlarmData().getType() == AlarmData.ALARM_TYPE.REPEATED_LOOP_SPEC_TIME) {
                                editText = getDialog().findViewById(mapper2.get(view.getId()));
                                enableTextView(editText, checkBox.isChecked());
                            }
                        } else if (view instanceof TextView) {
                            if (mapper.containsKey(view.getId()))
                                startAudioParam(alarm.getAlarmData().getDaysOfWeek().getAudio(param.getValue().getNum()), param.getValue().getNum());
                        }
                    } else if (alarm.getAlarmData().getType() == AlarmData.ALARM_TYPE.REPEATED && view instanceof TextView) {
                        String[] tok = view.getContentDescription().toString().split("@");
                        int id = Integer.valueOf(tok[0]);
                        long dateTime = Long.valueOf(tok[1]);
                        startAudioParam(dateTime == -1 ? null : alarm.getAlarmData().getRepeatedSpecDays().get(dateTime), id);
                        Log.i(Chronos.name + "AlarmDialog", "start audio param");
                    }
            }
        }
    }

    /**
     * Listener for ok button
     */
    private class DialogClick implements View.OnClickListener {
        private boolean isInvalide(TextView view, String name) {
            if (view.getText().toString().trim().equals("")) {
                Toast.makeText(getActivity(), name + " " + Units.getLocalizedText(R.string.is_mandatory), Toast.LENGTH_LONG).show();
                view.requestFocus();
                return true;
            }
            return false;
        }

        @Override
        public void onClick(View view) {
            EditText edt_name = getDialog().findViewById(R.id.edt_alarm_name);

            // check
            if (isInvalide(edt_name, Units.getLocalizedText(R.string.alarm_name)))
                return;

            alarm.setName(edt_name.getText().toString());

            EditText edt_description = getDialog().findViewById(R.id.edt_alarm_desc);
            if (isInvalide(edt_description, "Description"))
                return;

            alarm.getAlarmData().setDescription(edt_description.getText().toString());

            AudioProperties audioProperties = new AudioProperties();
            audioProperties.loadFromPref(PreferenceCst.PREFIX_ALARM, getActivity());
            switch (alarm.getAlarmData().getType()) {
                case ONCE:
                    EditText edt_date = getDialog().findViewById(R.id.edt_date);
                    long ldate;
                    try {
                        ldate = Chronos.fdate.parse(edt_date.getText().toString()).getTime();
                        String s = Chronos.fdate.format(System.currentTimeMillis());
                        long now = Chronos.fdate.parse(s).getTime();
                        if (ldate < now) {
                            Toast.makeText(getActivity(), R.string.bad_date_format, Toast.LENGTH_LONG).show();
                            edt_date.requestFocus();
                            return;
                        }

                    } catch (ParseException e) {
                        Toast.makeText(getActivity(), R.string.bad_date_format, Toast.LENGTH_LONG).show();
                        edt_date.requestFocus();
                        return;
                    }
                    EditText edt_time = getDialog().findViewById(R.id.edt_time);
                    if (isInvalide(edt_time, "Time"))
                        return;
                    long time = Digit.getTimeFromString(edt_time.getText().toString());
                    if (time == -1) {
                        Toast.makeText(getActivity(), R.string.bad_time_format, Toast.LENGTH_LONG).show();
                        edt_time.requestFocus();
                        return;
                    }
                    long endTime = ldate + time;
                    if (endTime <= System.currentTimeMillis()) {
                        Toast.makeText(getActivity(), R.string.alarm_not_in_past, Toast.LENGTH_LONG).show();
                        edt_date.requestFocus();
                        return;
                    }
                    alarm.setEndTime(endTime);
                    alarm.setFirstEndTime(endTime);
                    break;
                case REPEATED_LOOP:
                    edt_time = getDialog().findViewById(R.id.edt_time);
                    time = Digit.getTimeFromString(edt_time.getText().toString());
                    if (time == -1) {
                        Toast.makeText(getActivity(), R.string.bad_time_format, Toast.LENGTH_LONG).show();
                        edt_time.requestFocus();
                        return;
                    }
                    alarm.setEndTimeInDay(time);
                    alarm.updateEndTimeForRepeatedLoop();
                    alarm.setFirstEndTime(alarm.getEndTime());
                    // update for repeat_loop : if a day is set and has non audio param, set audio param to default.
                    for (int i : DaysOfWeek.getWeekDays())
                        if (alarm.getAlarmData().getDaysOfWeek().isDayEnable(i) && alarm.getAlarmData().getDaysOfWeek().getAudio(i) == null)
                            alarm.getAlarmData().getDaysOfWeek().setAudio(i, audioProperties);
                    break;
                case REPEATED_LOOP_SPEC_TIME:
                    // check and update time
                    for (int id : mapper.keySet()) {
                        View view1 = alarm_repeat_loop_spec_time.findViewById(id);
                        if (view1 instanceof CheckBox && ((CheckBox) view1).isChecked()) {
                            edt_time = alarm_repeat_loop_spec_time.findViewById(mapper2.get(id));
                            String textTime = edt_time.getText().toString();
                            time = Digit.getTimeFromString(textTime);
                            if (time == -1) {
                                Toast.makeText(getActivity(), R.string.bad_time_format, Toast.LENGTH_LONG).show();
                                edt_time.requestFocus();
                                return;
                            } else {
                                alarm.getAlarmData().getDaysOfWeek().setTime(mapper.get(id).getValue().getNum(), time);
                            }
                        }
                    }
                    // update for repeat_loop : if a day is set and has non audio param, set audio param to default.
                    for (int i : DaysOfWeek.getWeekDays())
                        if (alarm.getAlarmData().getDaysOfWeek().isDayEnable(i) && alarm.getAlarmData().getDaysOfWeek().getAudio(i) == null)
                            alarm.getAlarmData().getDaysOfWeek().setAudio(i, audioProperties);
                    break;

                case REPEATED:
                    // update from the ui
                    alarm.getAlarmData().getRepeatedSpecDays().clear();
                    for (int k = 0; k < table_date_time.getChildCount(); k++) {
                        View view1 = table_date_time.getChildAt(k);
                        EditText edtdate = view1.findViewById(Integer.MAX_VALUE -k);
                        long date;
                        try {
                            date = Chronos.fdate.parse(edtdate.getText().toString()).getTime();
                            String s = Chronos.fdate.format(System.currentTimeMillis());
                            long now = Chronos.fdate.parse(s).getTime();
                            if (date < now) {
                                Toast.makeText(getActivity(), R.string.bad_date_format, Toast.LENGTH_LONG).show();
                                edtdate.requestFocus();
                                return;
                            }
                        } catch (ParseException e) {
                            Toast.makeText(getActivity(), R.string.bad_date_format, Toast.LENGTH_LONG).show();
                            edtdate.requestFocus();
                            return;
                        }
                        EditText edtTime = view1.findViewById(Integer.MAX_VALUE - 300 -k);
                        time = Digit.getTimeFromString(edtTime.getText().toString());
                        if (time == -1) {
                            Toast.makeText(getActivity(), R.string.bad_time_format, Toast.LENGTH_LONG).show();
                            edtTime.requestFocus();
                            return;
                        }

                        if (date + time <= System.currentTimeMillis()) {
                            Toast.makeText(getActivity(), R.string.alarm_not_in_past, Toast.LENGTH_LONG).show();
                            edtdate.requestFocus();
                            return;
                        }
                        alarm.getAlarmData().getRepeatedSpecDays().put(date + time,
                                repeatedAudioPref.get(k) == null ? audioProperties : repeatedAudioPref.get(k));
                    }

                    alarm.updateEndTimeForRepeated();
                    alarm.setFirstEndTime(alarm.getEndTime());
                    break;
            }

            if (dialogClickListener != null)
                dialogClickListener.onDialogPositiveClick(AlarmDialog.this);

            dismiss();
        }
    }
}