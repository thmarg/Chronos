/*
 * ${NAME}
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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import tm.android.chronos.R;
import tm.android.chronos.core.Stopwatch;
import tm.android.chronos.core.Units;
import tm.android.chronos.core.Units.CHRONO_TYPE;
import tm.android.chronos.core.Units.LENGTH_UNIT;
import tm.android.chronos.core.Units.SPEED_UNIT;

/**
 *
 */
public class ChronographeDialog<T extends Stopwatch> extends DialogFragment {
    private T stopwatch;
    private OnDialogClickListener dialogClickListener;
    private String name;
    private CHRONO_TYPE type;
    private LENGTH_UNIT lengthUnit;
    private SPEED_UNIT speedUnit;
    private double distance;
    private Spinner unitDistance;
    private Spinner unitSpeed;
    private EditText editTextDistance;
    private View view_distance_speed;
    private LinearLayout backgroundLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, 0);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // inflate the views
        @SuppressLint("null")
        View view_name_type = inflater.inflate(R.layout.param_name_type, null);
        view_distance_speed = inflater.inflate(R.layout.param_distances_speed, null);

        // view_name_type is always présent.

        // view_distance_speed is optionnel only for types LAPS and SEGMENT
        // but we construct the two views anyway to add or remove the optional one, when notably the chrono_type spinner selection change.

        // spinner for distance unit
        unitDistance = (Spinner) view_distance_speed.findViewById(R.id.spin_units_distance);
        ArrayAdapter<LENGTH_UNIT> arrayAdapter = new ArrayAdapter<LENGTH_UNIT>(getActivity(), R.layout.layoutforspinner, R.id.txt_view_item, Units.getUnitLenghtList());
        unitDistance.setAdapter(arrayAdapter);

        // spinner for speed units
        unitSpeed = (Spinner) view_distance_speed.findViewById(R.id.spin_speed);
        ArrayAdapter<SPEED_UNIT> arrayAdapter1 = new ArrayAdapter<SPEED_UNIT>(getActivity(), R.layout.layoutforspinner, R.id.txt_view_item, Units.getSpeedUnitList());
        unitSpeed.setAdapter(arrayAdapter1);

        // spinner for chrono type
        Spinner chronoType = (Spinner) view_name_type.findViewById(R.id.spin_type);
        ArrayAdapter<CHRONO_TYPE> arrayAdapter2 = new ArrayAdapter<CHRONO_TYPE>(getActivity(), R.layout.layoutforspinner, R.id.txt_view_item, Units.getModeList());
        chronoType.setAdapter(arrayAdapter2);
        chronoType.setOnItemSelectedListener(new OnTypeChanged());

        editTextDistance = (EditText) view_distance_speed.findViewById(R.id.edt_txt_distance);

        if (stopwatch != null) {
            // Stopwatch name
            ((TextView) view_name_type.findViewById(R.id.edt_txt_name)).setText(stopwatch.getName());
            // Type
            if (this.stopwatch.getStopwatchData().getChronoType() != null)
                chronoType.setSelection(arrayAdapter2.getPosition(stopwatch.getStopwatchData().getChronoType()));
            //
            if (stopwatch.getStopwatchData().getChronoType() == CHRONO_TYPE.LAPS || stopwatch.getStopwatchData().getChronoType() == CHRONO_TYPE.SEGMENTS)
                updateLapInfoFromStopwatch();

        }
        // build the layout that supports the views
        backgroundLayout = new LinearLayout(getActivity());
        backgroundLayout.setOrientation(LinearLayout.VERTICAL);
        backgroundLayout.addView(view_name_type);
        if (stopwatch.getStopwatchData().getChronoType() == CHRONO_TYPE.LAPS || stopwatch.getStopwatchData().getChronoType() == CHRONO_TYPE.SEGMENTS)
            backgroundLayout.addView(view_distance_speed);
        // add it to the builder
        builder.setView(backgroundLayout);

        builder.setTitle(Units.getLocalizedText("title_stopwatch_params", null));

        builder.setPositiveButton(Units.getLocalizedText("validate", null), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                name = ((TextView) getDialog().findViewById(R.id.edt_txt_name)).getText().toString().trim();

                type = (CHRONO_TYPE) ((Spinner) getDialog().findViewById(R.id.spin_type)).getSelectedItem();

                if (type == CHRONO_TYPE.LAPS || type == CHRONO_TYPE.SEGMENTS) {
                    lengthUnit = (LENGTH_UNIT) ((Spinner) getDialog().findViewById(R.id.spin_units_distance)).getSelectedItem();

                    speedUnit = (SPEED_UNIT) ((Spinner) getDialog().findViewById(R.id.spin_speed)).getSelectedItem();

                    String text = ((EditText) getDialog().findViewById(R.id.edt_txt_distance)).getText().toString();
                    if (!text.equals(""))
                        distance = Double.valueOf(text);
                }

                if (dialogClickListener != null)
                    dialogClickListener.onDialogPositiveClick(ChronographeDialog.this);

            }
        });

        builder.setNegativeButton(Units.getLocalizedText("cancel", null), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogClickListener != null)
                    dialogClickListener.onDialogNegativeClick(ChronographeDialog.this);

            }
        });


        return builder.create();
    }

    @SuppressWarnings("unckecked")
    private void updateLapInfoFromStopwatch() {
        if (stopwatch.getStopwatchData().getLapDistance() > 0)
            editTextDistance.setText(String.valueOf(stopwatch.getStopwatchData().getLapDistance()));

        // init value
        if (stopwatch.getStopwatchData().getLengthUnit() != null) {
            ArrayAdapter<LENGTH_UNIT> arrayAdapter = (ArrayAdapter<LENGTH_UNIT>) unitDistance.getAdapter();
            unitDistance.setSelection(arrayAdapter.getPosition(stopwatch.getStopwatchData().getLengthUnit()));
        }
        // init value
        if (stopwatch.getStopwatchData().getSpeedUnit() != null) {
            ArrayAdapter<SPEED_UNIT> arrayAdapter1 = (ArrayAdapter<SPEED_UNIT>) unitSpeed.getAdapter();
            unitSpeed.setSelection(arrayAdapter1.getPosition(stopwatch.getStopwatchData().getSpeedUnit()));
        }

    }

    public T getStopwatch() {
        return stopwatch;
    }

    public void setStopwatch(T stopwatch) {
        this.stopwatch = stopwatch;

    }

    public void setDialogClickListener(OnDialogClickListener dialogClickListener) {
        this.dialogClickListener = dialogClickListener;
    }

    public double getDistance() {
        return distance;
    }

    public LENGTH_UNIT getLengthUnit() {
        return lengthUnit;
    }

    public String getName() {
        return name;
    }

    public SPEED_UNIT getSpeedUnit() {
        return speedUnit;
    }

    public CHRONO_TYPE getType() {
        return type;
    }


    private class OnTypeChanged implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            CHRONO_TYPE selected = (CHRONO_TYPE) adapterView.getSelectedItem();
            if (selected == CHRONO_TYPE.LAPS || selected == CHRONO_TYPE.SEGMENTS) {
                backgroundLayout.removeView(view_distance_speed);
                backgroundLayout.addView(view_distance_speed);
                if (stopwatch != null)
                    updateLapInfoFromStopwatch();
            } else {
                backgroundLayout.removeView(view_distance_speed);
            }


        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            //
        }
    }
}