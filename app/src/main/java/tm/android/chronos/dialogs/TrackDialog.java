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
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import tm.android.chronos.R;
import tm.android.chronos.core.Track;
import tm.android.chronos.core.TrackPart;
import tm.android.chronos.core.Units;
import tm.android.chronos.localisation.Point;
import tm.android.chronos.sql.TrackFactory;
import tm.android.chronos.uicomponent.BaseUI;

import java.util.ArrayList;
import java.util.List;

import static tm.android.chronos.dialogs.TrackDialog.ROW_TAG.*;

/**
 * A Dialog to create one Track : name, description and its parts
 */
public class TrackDialog extends DialogFragment {
    public enum MODE {CREATE, EDIT, VIEW}

    enum ROW_TAG {NAME, LATITUDE, LONGITUDE, DISTANCE, CHECKBOX}

    private MODE mode;
    private Track track;
    private OnDialogClickListener dialogClickListener;
    private TableLayout track_parts_table;
    private SelectedItemListener selectedItemListener;
    private EditText edt_name;
    private EditText edt_desc;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, 0);

    }

    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        //
        selectedItemListener = new SelectedItemListener();
        // inflate the views
        RelativeLayout track_view = (RelativeLayout) inflater.inflate(R.layout.track_dialog, null);
        track_parts_table = track_view.findViewById(R.id.track_parts_list);
        //
        edt_name = track_view.findViewById(R.id.edt_name);
        edt_name.setText(track.getName());

        edt_desc = track_view.findViewById(R.id.edt_desc);
        edt_desc.setText(track.getDescription());

        if (track.getTrackParts() != null) {
            for (TrackPart trackPart : track.getTrackParts()) {
                track_parts_table.addView(getTableRow(trackPart));
            }
        } else {
            track_parts_table.addView(getTableRow(new TrackPart("Start", TrackPart.TYPE.START)));
            track_parts_table.addView(getTableRow(new TrackPart("End", TrackPart.TYPE.END)));
        }

        ClickListener clickListener = new ClickListener();
        track_view.findViewById(R.id.img_btn_plus).setOnClickListener(clickListener);
        track_view.findViewById(R.id.img_btn_moins).setOnClickListener(clickListener);

        builder.setView(track_view);


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


    private TableRow getTableRow(TrackPart trackPart) {
        TableRow tableRow = new TableRow(getActivity());
        tableRow.setTag(trackPart);
        tableRow.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Spinner spinner_name = new Spinner(getActivity());
        AdapterSpinner adapter = new AdapterSpinner(getActivity(), android.R.layout.simple_spinner_item);
        TrackFactory trackFactory = new TrackFactory(getActivity());
        adapter.addAll(trackFactory.getPoints());
        spinner_name.setAdapter(adapter);
        spinner_name.setTag(NAME);
        spinner_name.setOnItemSelectedListener(selectedItemListener);
        for (int i = 0; i < adapter.getCount() ; i++) {
            if (adapter.getItem(i).getId()==trackPart.getLocation().getId()){
                spinner_name.setSelection(i);
                break;
            }
        }

        tableRow.addView(spinner_name);

        TextView txv_latitude = new TextView(getActivity());
        String text;
        if (trackPart.getLatitude() == Point.NO_VALUE)
            text = String.valueOf(((Point) spinner_name.getSelectedItem()).getLatitude());
        else
            text = String.valueOf(trackPart.getLocation().getLatitude());
        txv_latitude.setText(text);

        txv_latitude.setTag(LATITUDE);
        tableRow.addView(txv_latitude);

        TextView txv_longitude = new TextView(getActivity());
        if (trackPart.getLongitude() == Point.NO_VALUE)
            text = String.valueOf(((Point) spinner_name.getSelectedItem()).getLongitude());
        else
            text = String.valueOf(trackPart.getLocation().getLongitude());
        txv_longitude.setText(text);
        txv_longitude.setTag(LONGITUDE);
        tableRow.addView(txv_longitude);

        EditText edt_distance = new EditText(getActivity());
        edt_distance.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        edt_distance.setEms(4);
        if (trackPart.isEnd())
            edt_distance.setEnabled(false);
        else
            edt_distance.setText(String.valueOf(trackPart.getDistanceToNextLocation()));
        edt_distance.setTag(DISTANCE);
        tableRow.addView(edt_distance);

        if (trackPart.isCurrent()) {
            CheckBox ckb_select = new CheckBox(getActivity());
            ckb_select.setTag(CHECKBOX);
            tableRow.addView(ckb_select);
        }


        return tableRow;
    }


    @Override
    public void onResume() {
        super.onResume();

        AlertDialog alertDialog = (AlertDialog) getDialog();
        if (alertDialog != null) {
            if (mode != MODE.VIEW) { // no validation in VIEW mode
                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new DialogClick());
            }
            Window window = alertDialog.getWindow();
            if (window != null) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                    window.setLayout(BaseUI.SCREENWIDTH, WindowManager.LayoutParams.WRAP_CONTENT);
                else
                    window.setLayout(BaseUI.SCREENHEIGHT, WindowManager.LayoutParams.WRAP_CONTENT);
            }
        }


    }

    public void setTrack(Track track) {
        this.track = track;
    }

    public void setMode(MODE mode) {
        this.mode = mode;
    }

    public void setDialogClickListener(OnDialogClickListener dialogClickListener) {
        this.dialogClickListener = dialogClickListener;
    }


    /**
     * Listener for audio pref, single if alarm is once, or for each day of week or ftime.
     */
    private class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.img_btn_plus:
                    View lastView = track_parts_table.getChildAt(track_parts_table.getChildCount() - 1);
                    track_parts_table.removeView(lastView);
                    track_parts_table.addView(getTableRow(new TrackPart("new", TrackPart.TYPE.CURRENT)));
                    track_parts_table.addView(lastView);

                    break;
                case R.id.img_btn_moins:
                    for (int i = 2; i < track_parts_table.getChildCount() - 1; i++) {
                        TableRow row = (TableRow) track_parts_table.getChildAt(i);
                        CheckBox checkBox = null;
                        for (int j = 0; j < row.getChildCount(); j++)
                            if (row.getChildAt(j) instanceof CheckBox) {
                                checkBox = (CheckBox) row.getChildAt(j);
                                break;
                            }

                        if (checkBox != null && checkBox.isChecked()) {
                            track_parts_table.removeViewAt(i);
                            i--;
                        }
                    }

                    break;
                default:

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
            // all check and track updates here
            if (isInvalide(edt_name, Units.getLocalizedText(R.string.alarm_name)))
                return;
            if (isInvalide(edt_desc, Units.getLocalizedText(R.string.alarm_desc)))
                return;

            track.setName(edt_name.getText().toString());
            track.setDescription(edt_desc.getText().toString());
            List<TrackPart> trackParts = new ArrayList<>(5);
            for (int i = 1; i < track_parts_table.getChildCount(); i++) { // start at 1 to skeep the header
                TableRow row = (TableRow) track_parts_table.getChildAt(i);
                TrackPart trackPart = (TrackPart) row.getTag();

                for (int j = 0; j < row.getChildCount(); j++) {

                    switch ((ROW_TAG) row.getChildAt(j).getTag()) {
                        case NAME:
                            Spinner spinner = (Spinner) row.getChildAt(j);
                            Point point = (Point) spinner.getSelectedItem();
                            trackPart.setLocation(point);
                            break;
                        case DISTANCE:
                            if (!trackPart.isEnd()) {
                                EditText editText = (EditText)row.getChildAt(j);
                                trackPart.setDistanceToNextLocation(Float.valueOf(editText.getText().toString()));
                            }
                            break;
                    }
                }

                trackParts.add(trackPart);

            }

            track.setTrackParts(trackParts);

            if (dialogClickListener != null)
                dialogClickListener.onDialogPositiveClick(TrackDialog.this);


            dismiss();
        }
    }

    private class AdapterSpinner extends ArrayAdapter<Point> {
        AdapterSpinner(Context context, int resId) {
            super(context, resId);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(android.R.layout.simple_spinner_item, parent, false);
            }

            ((TextView) convertView).setText(getItem(position).getName());
            return convertView;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            }

            ((TextView) convertView).setText(getItem(position).getName());
            return convertView;
        }

    }

    private class SelectedItemListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Point point = (Point)parent.getSelectedItem();
            TableRow row = (TableRow) parent.getParent();
            ((TextView)row.findViewWithTag(LATITUDE)).setText(String.valueOf(point.getLatitude()));
            ((TextView)row.findViewWithTag(LONGITUDE)).setText(String.valueOf(point.getLongitude()));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}