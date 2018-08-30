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
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import tm.android.chronos.R;
import tm.android.chronos.core.Units;
import tm.android.chronos.localisation.Point;
import tm.android.chronos.uicomponent.BaseUI;

/**
 *
 */
public class PointDialog extends DialogFragment {
    public enum MODE {CREATE, EDIT, VIEW}
    private MODE mode;
    private Point point;
    private OnDialogClickListener dialogClickListener;
    private EditText edt_name;
    private EditText edt_latitude;
    private EditText edt_longitude;
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

        // inflate the views
        TableLayout point_table = (TableLayout) inflater.inflate(R.layout.point_dialog, null);

        //
        edt_name = point_table.findViewById(R.id.edt_name);
        edt_name.setText(point.getName());

        edt_latitude = point_table.findViewById(R.id.edt_latitude);
        edt_latitude.setText(point.getLatitude() == Point.NO_VALUE ? "" : String.valueOf(point.getLatitude()));

        edt_longitude = point_table.findViewById(R.id.edt_longitude);
        edt_longitude.setText(point.getLongitude() == Point.NO_VALUE ? "" : String.valueOf(point.getLongitude()));

        edt_desc = point_table.findViewById(R.id.edt_desc);
        edt_desc.setText(point.getDescription());

        builder.setView(point_table);

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

    public void setPoint(Point point) {
        this.point = point;
    }

    public void setMode(MODE mode) {
        this.mode = mode;
    }

    public void setDialogClickListener(OnDialogClickListener dialogClickListener) {
        this.dialogClickListener = dialogClickListener;
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
            if (isInvalide(edt_latitude,Units.getLocalizedText(R.string.latitude)))
                return;
            if (isInvalide(edt_longitude,Units.getLocalizedText(R.string.longitude)))
                return;

            point.setName(edt_name.getText().toString());
            point.setLatitude(Double.valueOf(edt_latitude.getText().toString()));
            point.setLongitude(Double.valueOf(edt_longitude.getText().toString()));
            point.setDescription(edt_desc.getText().toString());

            if (dialogClickListener != null)
                dialogClickListener.onDialogPositiveClick(PointDialog.this);

            dismiss();
        }
    }
}