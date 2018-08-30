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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import tm.android.chronos.R;
import tm.android.chronos.core.Stopwatch;
import tm.android.chronos.core.Track;
import tm.android.chronos.core.Units;
import tm.android.chronos.core.Units.CHRONO_TYPE;
import tm.android.chronos.core.Units.LENGTH_UNIT;
import tm.android.chronos.core.Units.SPEED_UNIT;
import tm.android.chronos.localisation.SpeedCategory;
import tm.android.chronos.sql.TrackFactory;
import tm.android.chronos.uicomponent.BaseUI;
import tm.android.chronos.util.Couple;
import tm.android.chronos.util.FilesUtils;
import tm.android.chronos.util.PathFinder;

import java.io.File;

/**
 *
 */
@SuppressWarnings("unchecked")
public class ChronographeDialog extends DialogFragment {
    private Stopwatch stopwatch;
    private OnDialogClickListener dialogClickListener;
    private Spinner unitDistance;
    private Spinner unitSpeed;
    private EditText edt_txt_name;
    private Spinner chronoType;
    private EditText editTextDistance;
    private View view_distance_speed;
    private View view_segment;
    private View view_name_type;
    private LinearLayout backgroundLayout;
    private Spinner spn_track;
    private Switch swt_use_gps;
    private Spinner spn_dsp_type;
    private Switch swt_rdm_music;
    private TextView txv_path;
    private Button btn_select;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, 0);
    }

    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // inflate the views
        view_name_type = inflater.inflate(R.layout.param_name_type, null);
        view_distance_speed = inflater.inflate(R.layout.param_distances_speed, null);
        view_segment = inflater.inflate(R.layout.param_segments, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, BaseUI.getPixels(20), 0, 0);
        view_segment.setLayoutParams(lp);

        // view_name_type is always présent.
        edt_txt_name = view_name_type.findViewById(R.id.edt_txt_name);

        // view_distance_speed is optionnel only for types LAPS and SEGMENT
        // but we construct the two views anyway to add or remove the optional one, when notably the chrono_type spinner selection change.

        // spinner for distance unit
        unitDistance = view_distance_speed.findViewById(R.id.spin_units_distance);
        ArrayAdapter<LENGTH_UNIT> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.layoutforspinner, R.id.txt_view_item, Units.getUnitLenghtList());
        unitDistance.setAdapter(arrayAdapter);

        // spinner for speed units
        unitSpeed = view_distance_speed.findViewById(R.id.spin_speed);
        ArrayAdapter<SPEED_UNIT> arrayAdapter1 = new ArrayAdapter<>(getActivity(), R.layout.layoutforspinner, R.id.txt_view_item, Units.getSpeedUnitList());
        unitSpeed.setAdapter(arrayAdapter1);

        // spinner for chrono type
        chronoType = view_name_type.findViewById(R.id.spin_type);
        ArrayAdapter<CHRONO_TYPE> chronoTypeArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.layoutforspinner, R.id.txt_view_item, Units.getModeList());
        chronoType.setAdapter(chronoTypeArrayAdapter);
        chronoType.setOnItemSelectedListener(new OnTypeChanged());
        chronoType.setSelection(chronoTypeArrayAdapter.getPosition(stopwatch.getStopwatchData().getChronoType()));

        ArrayAdapter<Track> trackArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        TrackFactory factory = new TrackFactory(getActivity());
        trackArrayAdapter.addAll(factory.getTracks());
        spn_track = view_segment.findViewById(R.id.spn_tracks);
        spn_track.setAdapter(trackArrayAdapter);
        ClickListener clickListener = new ClickListener();
        view_segment.findViewById(R.id.txv_details).setOnClickListener(clickListener);
        swt_use_gps = view_segment.findViewById(R.id.swt_use_gps);
        spn_dsp_type = view_segment.findViewById(R.id.spn_dep_type);
        final ArrayAdapter<Couple<Integer, String>> dsptypeArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        dsptypeArrayAdapter.addAll(SpeedCategory.getSpeedCategories());
        spn_dsp_type.setAdapter(dsptypeArrayAdapter);
        spn_dsp_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
              stopwatch.getStopwatchData().setDisplacementType(dsptypeArrayAdapter.getItem(position).getKey());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spn_dsp_type.setSelection(stopwatch.getStopwatchData().getDisplacementType());

        swt_rdm_music = view_segment.findViewById(R.id.swt_rdm_music);
        swt_rdm_music.setOnClickListener(clickListener);
        txv_path = view_segment.findViewById(R.id.txv_path);
        btn_select = view_segment.findViewById(R.id.btn_select);
        btn_select.setOnClickListener(clickListener);


        editTextDistance = view_distance_speed.findViewById(R.id.edt_txt_distance);

        if (stopwatch != null) {
            // Stopwatch name
            edt_txt_name.setText(stopwatch.getName());
            // Type
            if (this.stopwatch.getStopwatchData().getChronoType() != null)
                chronoType.setSelection(chronoTypeArrayAdapter.getPosition(stopwatch.getStopwatchData().getChronoType()));
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
        if (stopwatch.getStopwatchData().getChronoType() == CHRONO_TYPE.SEGMENTS) {
            backgroundLayout.addView(view_segment);
            //view_segment_btn_add.callOnClick();
        }
        // add it to the builder
        builder.setView(backgroundLayout);

        builder.setTitle(Units.getLocalizedText("title_stopwatch_params", null));


        // Button ok/cancel // real listener is set in onResume method
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
            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            ChronographeDialog.DialogClick dialogClick = new ChronographeDialog.DialogClick();
            positiveButton.setOnClickListener(dialogClick);
            Window window = alertDialog.getWindow();
            if (window != null)
                window.setLayout(BaseUI.SCREENWIDTH, WindowManager.LayoutParams.WRAP_CONTENT);
        }

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

        if (stopwatch.getStopwatchData().getChronoType() == CHRONO_TYPE.SEGMENTS) {
            swt_use_gps.setChecked(stopwatch.getStopwatchData().isUseGps());
            swt_rdm_music.setChecked(stopwatch.getStopwatchData().isRandomMusic());
            btn_select.setEnabled(swt_rdm_music.isChecked());
            txv_path.setText(swt_rdm_music.isChecked() ? stopwatch.getStopwatchData().getRandomMusicPath():"");

        }

    }


    public void setStopwatch(Stopwatch stopwatch) {
        this.stopwatch = stopwatch;

    }

    public void setDialogClickListener(OnDialogClickListener dialogClickListener) {
        this.dialogClickListener = dialogClickListener;
    }

//    public double getDistance() {
//        return distance;
//    }
//
//
//    public String getName() {
//        return name;
//    }
//
//
//    public CHRONO_TYPE getType() {
//        return type;
//    }

//    public List<Couple<String, Float>> getSegments() {
//        return segments;
//    }


    private class OnTypeChanged implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            backgroundLayout.removeAllViews();
            backgroundLayout.addView(view_name_type);
            stopwatch.reset();
            CHRONO_TYPE selected = (CHRONO_TYPE) adapterView.getSelectedItem();
            stopwatch.getStopwatchData().setChronoType(selected);
            if (selected == CHRONO_TYPE.LAPS)
                backgroundLayout.addView(view_distance_speed);

            if (selected == CHRONO_TYPE.SEGMENTS)
                backgroundLayout.addView(view_segment);

            if (stopwatch != null)
                updateLapInfoFromStopwatch();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            //
        }
    }

    /*
     */
    private class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.txv_details:
                    Track track = (Track) spn_track.getSelectedItem();
                    TrackDialog trackDialog = new TrackDialog();
                    trackDialog.setTrack(track);
                    trackDialog.setMode(TrackDialog.MODE.VIEW);
                    trackDialog.show(getFragmentManager(), "Chronos-track");
                    break;
                case R.id.btn_select:
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivityForResult(intent, 12345);
                    break;
                case R.id.swt_rdm_music:
                    btn_select.setEnabled(swt_rdm_music.isChecked());
                    break;
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 12345 && data != null) {
            txv_path.setText(PathFinder.getPath(getActivity(), data.getData()));
            if (FilesUtils.getFilesCount(new File(txv_path.getText().toString()),FilesUtils.getAudioFilesExtensions()) == 0)
                Toast.makeText(getActivity(),"Le répertoire selectionné ne contient aucun fichier audio",Toast.LENGTH_LONG).show();
        }
    }



    private class DialogClick implements View.OnClickListener {
        private boolean isValide(TextView view, String name) {
            if (view.getText().toString().trim().equals("")) {
                Toast.makeText(getActivity(), name + " " + Units.getLocalizedText(R.string.is_mandatory), Toast.LENGTH_LONG).show();
                view.requestFocus();
                return false;
            }
            return true;
        }

        @Override
        public void onClick(View view) {
            stopwatch.setName(edt_txt_name.getText().toString().trim());
            stopwatch.getStopwatchData().setChronoType((CHRONO_TYPE) chronoType.getSelectedItem());

            if (stopwatch.getStopwatchData().getChronoType() == CHRONO_TYPE.LAPS) {
                stopwatch.getStopwatchData().setLengthUnit((LENGTH_UNIT) unitDistance.getSelectedItem());
                stopwatch.getStopwatchData().setSpeedUnit((SPEED_UNIT)unitSpeed.getSelectedItem());

                if (!isValide(editTextDistance, Units.getLocalizedText(R.string.distance))) {
                    editTextDistance.requestFocus();
                    return;
                }
                stopwatch.getStopwatchData().setLapDistance(Double.valueOf(editTextDistance.getText().toString()));
            }


            if (stopwatch.getStopwatchData().getChronoType() == CHRONO_TYPE.SEGMENTS) {
                stopwatch.getStopwatchData().setTrack((Track) spn_track.getSelectedItem());
                stopwatch.getStopwatchData().setUseGps(swt_use_gps.isChecked());
                Couple<Integer, String> selected = (Couple<Integer, String>) spn_dsp_type.getSelectedItem();
                stopwatch.getStopwatchData().setDisplacementType(selected.getKey());
                stopwatch.getStopwatchData().setRandomMusic(swt_rdm_music.isChecked());
                stopwatch.getStopwatchData().setRandomMusicPath((swt_rdm_music.isChecked() ? txv_path.getText().toString(): ""));

            }

            if (dialogClickListener != null)
                dialogClickListener.onDialogPositiveClick(ChronographeDialog.this);

            dismiss();
        }
    }

}