package tm.android.chronos.uicomponent;


import android.content.Context;
import android.content.SharedPreferences;
import android.view.SurfaceHolder;
import tm.android.chronos.core.Stopwatch;
import tm.android.chronos.core.StopwatchDataRow;
import tm.android.chronos.core.StopwatchFactory;
import tm.android.chronos.core.Units;
import tm.android.chronos.core.Units.ZONE_ACTION;
import tm.android.chronos.core.ui.AbstractListView;
import tm.android.chronos.core.ui.UIRenderer;
import tm.android.chronos.preference.PreferenceCst;
import tm.android.chronos.sql.DbConstant;
import tm.android.chronos.sql.DbLiveObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static tm.android.chronos.core.Units.UPDATE_TYPE.*;
import static tm.android.chronos.core.Units.ZONE_ACTION.*;

public class ChronoListView extends AbstractListView {

    public ChronoListView(Context context) {
        super(context);
    }

    @Override
    protected void innerSurfaceCreated(SurfaceHolder surfaceHolder) {
        if (items.isEmpty()) {
            DbLiveObject<Stopwatch> dbLiveObject = new DbLiveObject<>(getContext());
            List<Stopwatch> stopwatchList = dbLiveObject.getRunningLiveObjects(DbConstant.RUNNING_STOPWATCHES_TABLE_NAME);
            if (stopwatchList != null && stopwatchList.size() > 0) {
                for (Stopwatch stopwatch : stopwatchList) {
                    // rebuild link : stopwatchDataRow.head must point to it's  stopwatchData
                    if (stopwatch.getStopwatchData().hasDataRow())
                        for (StopwatchDataRow row : stopwatch.getStopwatchData().getTimeList())
                            row.setHead(stopwatch.getStopwatchData());
                    // add this stopwatch for being render onto the ui
                    StopWatchUI2 stopWatchUI2 = new StopWatchUI2(stopwatch, cachedCanvas);
                    add(stopWatchUI2);
                }
                // delete from db
                dbLiveObject.clearTableAndClose(DbConstant.RUNNING_STOPWATCHES_TABLE_NAME);
            }
            dbLiveObject.close();
        }
        updateAll();

    }

    @Override
    protected void innerSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    protected void innerSurfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public boolean hasNonUIAction() {
        return false;
    }

    @Override
    public void doNonUIAction() {

    }

    @Override
    public void renderOnCache() {
        clearUpdateType(); // nothing to paint, all is done by the stopwatches
    }


    @Override
    public float getComputedHeight() {
        return 0;
    }

    @Override
    public <T> T getData() {
        return null;
    }

    public ZONE_ACTION getUserAction(float x) {
        if (x > BaseUI.lapTimeHorizontalLimit && x < getWidth())
            return LAP_TIME;
        if (x < BaseUI.lapTimeHorizontalLimit && x > BaseUI.ssrHorizontalLimit)
            return START_STOP_RESET;
        if (x < BaseUI.ssrHorizontalLimit && x > BaseUI.paramHorizontalLimit)
            return PARAM;
        return SHOW_HIDE;

    }

    public void addNewStopwatch() {
        Stopwatch stopwatch = StopwatchFactory.create();
        stopwatch.setName("Chrono - " + stopwatch.hashCode());
        StopWatchUI2 stopWatchUI2 = new StopWatchUI2(stopwatch, cachedCanvas);
        add(stopWatchUI2);
    }

    public boolean startStopwatches(long now) {
        boolean hasStartAtLeastOne = false;
        // start all that are wainting to start if none is selected or only the selected.
        List<UIRenderer> waiters = new ArrayList<>(5);
        for (UIRenderer renderer : items) {
            Stopwatch stopwatch = renderer.getData();
            if (stopwatch.isWaitingStart())
                waiters.add(renderer);
        }

        for (UIRenderer renderer : waiters) {
            if (selectedItems.isEmpty() || selectedItems.contains(renderer)) {
                Stopwatch stopwatch = renderer.getData();
                stopwatch.start(now);
                if (StopWatchUI2.mustShowStartDateTime())
                    renderer.addUpdateType(UPDATE_HEAD_LINE1); // needed to display start ftime/time if needed
                renderer.addUpdateType(UPDATE_HEAD_DIGIT);
                hasStartAtLeastOne = true;
            }
        }
        return hasStartAtLeastOne;
    }

    public boolean stopStopwatches(long now) {
        boolean hasStopAtLeastOne = false;
        // stop all that are running if none is selected or only the selected.
        List<UIRenderer> runners = new ArrayList<>(5);
        for (UIRenderer renderer : items) {
            Stopwatch stopwatch = renderer.getData();
            if (stopwatch.isRunning())
                runners.add(renderer);
        }

        for (UIRenderer renderer : runners) {
            if (selectedItems.isEmpty() || selectedItems.contains(renderer)) {
                Stopwatch stopwatch = renderer.getData();
                stopwatch.stopTime(now);
                if (StopWatchUI2.mustShowStartDateTime())
                    renderer.addUpdateType(UPDATE_HEAD_LINE1);
                renderer.addUpdateType(UPDATE_HEAD_DIGIT);
                renderer.addUpdateType(UPDATE_DETAILS);
                hasStopAtLeastOne = true;
            }
        }
        return hasStopAtLeastOne;
    }

    public void lapTimeStopwatches(long now) {
        // lap time for all that are running if none is selected or only the selected.
        List<UIRenderer> runners = new ArrayList<>(5);
        for (UIRenderer renderer : items) {
            Stopwatch stopwatch = renderer.getData();
            if (stopwatch.isRunning())
                runners.add(renderer);
        }

        for (UIRenderer renderer : runners) {
            if (selectedItems.isEmpty() || selectedItems.contains(renderer)) {
                Stopwatch stopwatch = renderer.getData();
                if (stopwatch.getStopwatchData().getChronoType() == Units.CHRONO_TYPE.SEGMENTS && stopwatch.getStopwatchData().isLastSegment())
                    stopwatch.stopTime(now);
                else
                    stopwatch.lapTime(now);
                renderer.setExpanded(true);
                renderer.addUpdateType(UPDATE_DETAILS);
            }
        }
    }


    /**
     * Return true if at least one stopwatch has been reset, false otherwise.
     *
     * @return boolean
     */
    public boolean resetStopwatches() {
        // stop all that are running if none is selected or only the selected.
        boolean hasResetAtLeastOne = false;
        List<UIRenderer> stopeds = new ArrayList<>(5);
        for (UIRenderer renderer : items) {
            Stopwatch stopwatch = renderer.getData();
            if (stopwatch.isStopped())
                stopeds.add(renderer);
        }

        for (UIRenderer renderer : stopeds) {
            if (selectedItems.isEmpty() || selectedItems.contains(renderer)) {
                Stopwatch stopwatch = renderer.getData();
                stopwatch.reset();
                renderer.setExpanded(false);
                if (StopWatchUI2.mustShowStartDateTime())
                    renderer.addUpdateType(UPDATE_HEAD_LINE1);
                renderer.addUpdateType(UPDATE_HEAD_DIGIT);
                renderer.addUpdateType(UPDATE_DETAILS);
                hasResetAtLeastOne = true;
            }
        }
        return hasResetAtLeastOne;
    }

    public boolean hasRunningStopWatch() {
        for (UIRenderer renderer : items) {
            Stopwatch stopwatch = renderer.getData();
            if (stopwatch.isRunning())
                return true;
        }
        return false;
    }

    public List<Stopwatch> getRunningStopwatch() {
        List<Stopwatch> runners = new ArrayList<>(5);
        for (UIRenderer renderer : items) {
            Stopwatch stopwatch = renderer.getData();
            if (stopwatch.isRunning())
                runners.add(stopwatch);
        }
        return runners;

    }


    public void remove() {
        SharedPreferences preferences = getContext().getSharedPreferences(PreferenceCst.PREF_STORE_NAME, 0);
        Vector<UIRenderer> toRemove  = new Vector<>(5);
        boolean allowRemoveRunning = preferences.getBoolean(PreferenceCst.PREFIX_STOPWATCHES + PreferenceCst.PREF_KEYS.STOPWATCH_ALLOW_RM_RUNNING, false);
        if (selectedItems.isEmpty()) { // no selection, only the last can be removed.
            Stopwatch stopwatch = items.lastElement().getData();
            if (!stopwatch.isRunning() || (stopwatch.isRunning() && allowRemoveRunning))
                toRemove.add(items.lastElement());

        } else { // remove only from selected
            if (!allowRemoveRunning) { // only not running if not allowed to remove running
                for (UIRenderer renderer : selectedItems)
                    if (!((Stopwatch) renderer.getData()).isRunning())
                        toRemove.add(renderer);
            } else {
                toRemove.addAll(selectedItems); // all selected  if allowed remove running
            }
        }

        super.remove(toRemove);


}
}
