/*
 * ChronographeDialog
 *
 *   Copyright (c) 2014 Thierry Margenstern under MIT license
 *   http://opensource.org/licenses/MIT
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import tm.android.chronos.R;
import tm.android.chronos.core.Stopwatch;
import tm.android.chronos.core.Units.*;
import tm.android.chronos.core.Units;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME,0);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("null")
        View view =inflater.inflate(R.layout.dialogstopwatchparam, null);

        // spinner for distance unit
        Spinner unitDistance = (Spinner) view.findViewById(R.id.spin_units_distance);
        ArrayAdapter<LENGTH_UNIT> arrayAdapter = new ArrayAdapter<LENGTH_UNIT>(getActivity(),R.layout.layoutforspinner,R.id.txt_view_item,Units.getUnitLenghtList());
        unitDistance.setAdapter(arrayAdapter);

        // spinner for speed units
        Spinner unitSpeed = (Spinner)view.findViewById(R.id.spin_speed);
        ArrayAdapter<SPEED_UNIT> arrayAdapter1 = new ArrayAdapter<SPEED_UNIT>(getActivity(),R.layout.layoutforspinner,R.id.txt_view_item,Units.getSpeedUnitList());
        unitSpeed.setAdapter(arrayAdapter1);

        // spinner for chrono type
        Spinner chronoType = (Spinner)view.findViewById(R.id.spin_type);
        ArrayAdapter<CHRONO_TYPE> arrayAdapter2 = new ArrayAdapter<CHRONO_TYPE>(getActivity(),R.layout.layoutforspinner,R.id.txt_view_item,Units.getModeList());
        chronoType.setAdapter(arrayAdapter2);


        if (stopwatch !=null) {
            if (stopwatch.getStopwatchData().getgLength() >= 0)
                ((EditText) view.findViewById(R.id.edt_txt_distance)).setText(String.valueOf(stopwatch.getStopwatchData().getgLength()));


            // init value
            if (stopwatch.getStopwatchData().getLengthUnit() != null)
                unitDistance.setSelection(arrayAdapter.getPosition(stopwatch.getStopwatchData().getLengthUnit()));
            // init value
            if (stopwatch.getStopwatchData().getSpeedUnit() != null)
                unitSpeed.setSelection(arrayAdapter1.getPosition(stopwatch.getStopwatchData().getSpeedUnit()));
            // init value
            if (this.stopwatch.getStopwatchData().getChronoType() != null)
                chronoType.setSelection(arrayAdapter2.getPosition(stopwatch.getStopwatchData().getChronoType()));


            // Stopwatch name

            ((TextView) view.findViewById(R.id.edt_txt_name)).setText(stopwatch.getName());

        }

        builder.setView(view);

        builder.setTitle("Paramètres du Chronomètre");

        builder.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                name = ((TextView) getDialog().findViewById(R.id.edt_txt_name)).getText().toString().trim();

                type = (CHRONO_TYPE)((Spinner)getDialog().findViewById(R.id.spin_type)).getSelectedItem();


                lengthUnit = (LENGTH_UNIT)((Spinner)getDialog().findViewById(R.id.spin_units_distance)).getSelectedItem();

                speedUnit = (SPEED_UNIT)((Spinner)getDialog().findViewById(R.id.spin_speed)).getSelectedItem();


                distance = Double.valueOf(((EditText) getDialog().findViewById(R.id.edt_txt_distance)).getText().toString());

                if (dialogClickListener !=null)
                    dialogClickListener.onDialogPositiveClick(ChronographeDialog.this);

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogClickListener !=null)
                    dialogClickListener.onDialogNegativeClick(ChronographeDialog.this);

            }
        });



        return builder.create();
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
}