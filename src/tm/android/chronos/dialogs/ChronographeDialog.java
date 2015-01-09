/*
 * ChronographeDialog
 *
 *   Copyright (c) 2014 Thierry Margenstern under MIT license
 *   http://opensource.org/licenses/MIT
 */

package tm.android.chronos.dialogs;

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
import tm.android.chronos.core.Units;

/**
 *
 */
public class ChronographeDialog extends DialogFragment {
    private OnDialogClickListener onDialogClickListener;
    private String initialChronoName;
    private Units.CHRONO_TYPE chronoType;
    private Units.LENGTH_UNIT lengthUnit;
    private Units.SPEED_UNIT speedUnit;
    private double distance;
    //private long chronoTime;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Stopwatch name
        View view =inflater.inflate(R.layout.dialogstopwatchparam, null);
        ((TextView)view.findViewById(R.id.edt_txt_name)).setText(initialChronoName);

        // spinner for distance unit
        Spinner unitDistance = (Spinner) view.findViewById(R.id.spin_units_distance);
        ArrayAdapter<Units.LENGTH_UNIT> arrayAdapter = new ArrayAdapter<Units.LENGTH_UNIT>(getActivity(),R.layout.layoutforspinner,R.id.txt_view_item,Units.getUnitLenghtList());
        unitDistance.setAdapter(arrayAdapter);
        // init value
        if (lengthUnit != null)
            unitDistance.setSelection(arrayAdapter.getPosition(lengthUnit));

        // spinner for speed units
        Spinner unitSpeed = (Spinner)view.findViewById(R.id.spin_speed);
        ArrayAdapter<Units.SPEED_UNIT> arrayAdapter1 = new ArrayAdapter<Units.SPEED_UNIT>(getActivity(),R.layout.layoutforspinner,R.id.txt_view_item,Units.getSpeedUnitList());
        unitSpeed.setAdapter(arrayAdapter1);
        // init value
        if (speedUnit != null)
            unitSpeed.setSelection(arrayAdapter1.getPosition(speedUnit));

        // spinner for chrono type
        Spinner chronoType = (Spinner)view.findViewById(R.id.spin_type);
        ArrayAdapter<Units.CHRONO_TYPE> arrayAdapter2 = new ArrayAdapter<Units.CHRONO_TYPE>(getActivity(),R.layout.layoutforspinner,R.id.txt_view_item,Units.getModeList());
        chronoType.setAdapter(arrayAdapter2);
        // init value
        if (this.chronoType != null)
            chronoType.setSelection(arrayAdapter2.getPosition(this.chronoType));


        if (distance >=0)
            ((EditText)view.findViewById(R.id.edt_txt_distance)).setText(String.valueOf(distance));




        builder.setView(view);

        builder.setTitle("Paramètres du Chronomètre");

        builder.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (onDialogClickListener!=null)
                    onDialogClickListener.onDialogPositiveClick(ChronographeDialog.this);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (onDialogClickListener!=null)
                    onDialogClickListener.onDialogNegativeClick(ChronographeDialog.this);
            }
        });



        return builder.create();
    }


    public void setChronoName(String name){
        initialChronoName=name;
    }

    public String getChronoName(){
        return ((TextView)getDialog().findViewById(R.id.edt_txt_name)).getText().toString();
    }




    public void setOnDialogClickListener(OnDialogClickListener onDialogClickListener) {
        this.onDialogClickListener = onDialogClickListener;
    }

    public Units.CHRONO_TYPE getChronoType() {
        chronoType = (Units.CHRONO_TYPE) ((Spinner)getDialog().findViewById(R.id.spin_type)).getSelectedItem() ;
        return chronoType;
    }

    public void setChronoType(Units.CHRONO_TYPE chronoType) {
        this.chronoType = chronoType;
    }

    public Units.LENGTH_UNIT getLengthUnit() {
        lengthUnit = (Units.LENGTH_UNIT)((Spinner)getDialog().findViewById(R.id.spin_units_distance)).getSelectedItem();
        return lengthUnit;
    }

    public void setLengthUnit(Units.LENGTH_UNIT lengthUnit) {
        this.lengthUnit = lengthUnit;
    }

    public Units.SPEED_UNIT getSpeedUnit() {
        speedUnit = (Units.SPEED_UNIT)((Spinner)getDialog().findViewById(R.id.spin_speed)).getSelectedItem();
        return speedUnit;
    }

    public void setSpeedUnit(Units.SPEED_UNIT speedUnit) {
        this.speedUnit = speedUnit;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return  Double.parseDouble(((EditText) getDialog().findViewById(R.id.edt_txt_distance)).getText().toString());
    }

//    public void setChronoTime(long chronoTime) {
//        this.chronoTime = chronoTime;
//    }
}