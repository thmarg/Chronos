package tm.android.chronos.uicomponent.event;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import tm.android.chronos.audio.CommonMediaPlayer;
import tm.android.chronos.core.Stopwatch;
import tm.android.chronos.core.Units;
import tm.android.chronos.core.ui.AbstractListView;
import tm.android.chronos.core.ui.AbstractListViewController;
import tm.android.chronos.core.ui.UIRenderer;
import tm.android.chronos.dialogs.ChronographeDialog;
import tm.android.chronos.dialogs.OnDialogClickListener;
import tm.android.chronos.services.LocationService;
import tm.android.chronos.uicomponent.ChronoListView;
import tm.android.chronos.uicomponent.StopWatchUI2;
import tm.android.chronos.util.FilesUtils;
import tm.android.chronos.util.Randomizer;

import java.io.File;
import java.util.List;

import static tm.android.chronos.core.Units.UPDATE_TYPE.*;

public class ChronoListViewController extends AbstractListViewController {
    private long now = 0;
    private ChronoListView listView;

    public ChronoListViewController(ChronoListView view) {
        this.listView = view;
    }


    @Override
    protected void startOnTouch(View view, MotionEvent event) {
        now = System.currentTimeMillis();
    }

    @Override
    protected void onTouchNormalClick(AbstractListView view, MotionEvent event, UIRenderer touchedItem) {
        Stopwatch touchedStopwatch = touchedItem.getData();
        switch (((ChronoListView) view).getUserAction(event.getX())) {
            case START_STOP_RESET:
                if (touchedStopwatch.isWaitingStart()) {
                    touchedStopwatch.start(now);
                } else if (touchedStopwatch.isRunning()) {
                    touchedStopwatch.stopTime(now);
                    if (touchedStopwatch.getStopwatchData().getChronoType() == Units.CHRONO_TYPE.SEGMENTS &&
                            touchedStopwatch.getStopwatchData().isUseGps()) {
                        LocationService locationService = LocationService.getSelf();
                        if (locationService != null)
                            locationService.stop();
                    }

                    if (touchedStopwatch.getStopwatchData().hasDataRow()) {
                        touchedItem.addUpdateType(UPDATE_DETAILS);
                    }
                } else if (touchedStopwatch.isStopped()) {
                    touchedStopwatch.reset();
                    touchedItem.setExpanded(false);
                    touchedItem.addUpdateType(UPDATE_DETAILS);
                }
                if (StopWatchUI2.mustShowStartDateTime())
                    touchedItem.addUpdateType(UPDATE_HEAD_LINE1); // needed to display start ftime/time if needed
                touchedItem.addUpdateType(UPDATE_HEAD_DIGIT);
                break;
            case LAP_TIME:
                if (touchedStopwatch.isRunning()) {
                    if (touchedStopwatch.getStopwatchData().getChronoType() == Units.CHRONO_TYPE.SEGMENTS && touchedStopwatch.getStopwatchData().isLastSegment())
                        touchedStopwatch.stopTime(now);
                    else
                        touchedStopwatch.lapTime(now);
                    touchedItem.setExpanded(true);
                    touchedItem.addUpdateType(UPDATE_DETAILS);
                }

                break;
            case PARAM:
                if (touchedStopwatch.isRunning())
                    break;
                ChronographeDialog dialog = new ChronographeDialog();
                FragmentManager fm = ((Activity) listView.getContext()).getFragmentManager();
                dialog.setStopwatch(touchedStopwatch);
                dialog.setDialogClickListener(new DialogController(touchedStopwatch, touchedItem));
                dialog.show(fm, "Dialog-Chronographe");
                break;
            case SHOW_HIDE:
                if (touchedStopwatch.getStopwatchData().hasDataRow()) {
                    touchedItem.setExpanded(!touchedItem.isExpanded());
                    touchedItem.addUpdateType(UPDATE_DETAILS);
                }
                break;
        }

    }

    @Override
    protected void onTouchLongClick(AbstractListView view, MotionEvent event, UIRenderer touchedItem) {
        view.select(touchedItem);
    }


    @Override
    protected void endOnTouch(AbstractListView view, MotionEvent event) {

    }


    /*
        Controller for the dialog to manage a stopwatch's parameters.
     */
    class DialogController implements OnDialogClickListener {
        private Stopwatch touchedStopwatch;
        private UIRenderer renderer;

        DialogController(Stopwatch touchedStopwatch, UIRenderer renderer) {
            this.touchedStopwatch = touchedStopwatch;
            this.renderer = renderer;
        }

        @Override
        public void onDialogPositiveClick(DialogFragment dialog) {
            // Name
            touchedStopwatch.reset();
            renderer.addUpdateType(UPDATE_CHRONO_TYPE);
            renderer.addUpdateType(PAINT_ALL);

//            ChronographeDialog cdialog = (ChronographeDialog) dialog;
//            if (cdialog.getName() != null && !cdialog.getName().equals("") && !touchedStopwatch.getName().equals(cdialog.getName())) {
//                touchedStopwatch.setName(cdialog.getName());
//                renderer.addUpdateType(UPDATE_HEAD_LINE1);
//            }
//
//            // chrono type
//            if (cdialog.getType() != null) {
//                Units.CHRONO_TYPE ctype = touchedStopwatch.getStopwatchData().getChronoType();
//                if (ctype != cdialog.getType()) {
//                    touchedStopwatch.getStopwatchData().setChronoType(cdialog.getType());
//                    renderer.addUpdateType(UPDATE_CHRONO_TYPE);
//                    renderer.setSizeChanged();
//                }
//
//            }



//            if (cdialog.getType() == Units.CHRONO_TYPE.LAPS || cdialog.getType() == Units.CHRONO_TYPE.SEGMENTS) {
//
//                if (cdialog.getDistance() >= 0) {
//                    if (touchedStopwatch.getStopwatchData().getLapDistance() != cdialog.getDistance()) {
//                        renderer.addUpdateType(UPDATE_HEAD_LINE2);
//                        renderer.addUpdateType(UPDATE_HEAD_LINE3);
//                    }
//                    touchedStopwatch.getStopwatchData().setLapDistance(cdialog.getDistance());
//                }
//
//                if (cdialog.getLengthUnit() != null) {
//                    if (touchedStopwatch.getStopwatchData().getLengthUnit() != cdialog.getLengthUnit()) {
//                        renderer.addUpdateType(UPDATE_HEAD_LINE3);
//                    }
//                    touchedStopwatch.getStopwatchData().setLengthUnit(cdialog.getLengthUnit());
//                }
//
//
//                if (cdialog.getSpeedUnit() != null) {
//                    if (touchedStopwatch.getStopwatchData().getGlobalDistance() > 0 && touchedStopwatch.getStopwatchData().getGlobalTime() > 0 && touchedStopwatch.getStopwatchData().getSpeedUnit() != cdialog.getSpeedUnit()) {
//                        renderer.addUpdateType(UPDATE_HEAD_LINE2);
//                    }
//                    touchedStopwatch.getStopwatchData().setSpeedUnit(cdialog.getSpeedUnit());
//                }
//            }

            if (touchedStopwatch.getStopwatchData().getChronoType() == Units.CHRONO_TYPE.SEGMENTS) {
                if (touchedStopwatch.getStopwatchData().isUseGps()) { // start the gps here
                    Intent intent = new Intent(dialog.getActivity(),LocationService.class);
                    intent.putExtra("trackId",touchedStopwatch.getStopwatchData().getTrack().getId());
                    intent.putExtra("dsp_type",touchedStopwatch.getStopwatchData().getDisplacementType());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        dialog.getActivity().startForegroundService(intent);
                    else
                        dialog.getActivity().startService(intent);
                }
                if (touchedStopwatch.getStopwatchData().isRandomMusic()) { // start music
                    CommonMediaPlayer.build(dialog.getActivity());
                    List<String> list = FilesUtils.getFiles(new File(touchedStopwatch.getStopwatchData().getRandomMusicPath()),FilesUtils.getAudioFilesExtensions());
                    if (list.isEmpty()) {
                        Toast.makeText(dialog.getActivity(),"Le répertoire selectionné ne contient aucun fichier audio",Toast.LENGTH_LONG).show();
                    } else {
                        Randomizer<String> songs = new Randomizer<>();
                        songs.load(list);
                        CommonMediaPlayer.Instance().setFixedVolumeLevel(0.2f);
                        CommonMediaPlayer.Instance().startRandom(songs);
                    }
                }
            }
        }

        @Override
        public void onDialogNegativeClick(DialogFragment dialog) {
            // nothing to do
        }
    }

}
