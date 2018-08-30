/*
 * TimerView : a list view with to items: TimeSelectionView and ElapsedTimeUI
 *
 * Copyright (c) 2018 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.uicomponent;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;
import tm.android.chronos.activity.Chronos;
import tm.android.chronos.core.Clock;
import tm.android.chronos.core.ClockTimer;
import tm.android.chronos.core.Stopwatch;
import tm.android.chronos.core.Units;
import tm.android.chronos.core.ui.AbstractListView;
import tm.android.chronos.sql.DbConstant;
import tm.android.chronos.sql.DbLiveObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A graphical Analogue watch for a Timer from 1 to  60 minutes, for setting the time and see visually the  count down.
 * Preferences allow to choose the sound at the end of the timer.
 */
public class TimerView extends AbstractListView {

    private TimeSelectionView timeSelectionView;
    private ElapseTimeUI elapseTimeUI;
    private final static String logname= Chronos.name+"-TimerView";

    public TimerView(Context context) {
        super(context);
    }

    @Override
    protected void innerSurfaceCreated(SurfaceHolder surfaceHolder) {
        Log.i(logname, "TimerView innerSurfaceCreated@" + hashCode());
        // we can reenter here.
        if (items.isEmpty()) {
            timeSelectionView = new TimeSelectionView();
            add(timeSelectionView);
        }

        ClockTimer clockTimer = getClockTimer();
        if (clockTimer != null) {// not null if exist and running
            timeSelectionView.setClockTimer(clockTimer);
        }

        if (elapseTimeUI != null && !items.contains(elapseTimeUI))
            add(elapseTimeUI);

        if (timeSelectionView.getUpdateTypes().isEmpty())
            timeSelectionView.addUpdateType(Units.UPDATE_TYPE.PAINT_ALL);
        if (elapseTimeUI != null)
            elapseTimeUI.addUpdateType(Units.UPDATE_TYPE.PAINT_ALL);

    }

    private ClockTimer getClockTimer() {
        ClockTimer clockTimer = null;
        DbLiveObject<Clock> dbLiveObject = new DbLiveObject<>(getContext());
        List<Clock> clockList = dbLiveObject.getRunningLiveObjects(DbConstant.RUNNING_TIMER_TABLE_NAME);
        if (clockList != null && clockList.size() > 0)
            clockTimer = (ClockTimer) clockList.get(0);
        dbLiveObject.close();
        return clockTimer;
    }

    @Override
    protected void innerSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(logname, "TimerView innerSurfaceChanged " + hashCode());
    }

    @Override
    protected void innerSurfaceDestroyed(SurfaceHolder holder) {
        ClockTimer clockTimer = getTimeSelectionView().getData();
        if (clockTimer.isRunning()) { // second test to prevent register when destroying the previous view (dead time reach)
            DbLiveObject<Clock> dbLiveObject = new DbLiveObject<>(getContext());
            List<Clock> timer = new ArrayList<>(1);
            timer.add(clockTimer);
            dbLiveObject.storeLiveObjects(timer, DbConstant.RUNNING_TIMER_TABLE_NAME);
            dbLiveObject.close();
            if (dbLiveObject.hasError()) {
                Toast.makeText(getContext(), dbLiveObject.getErrorMessage().localiszedMessage, Toast.LENGTH_LONG).show();
                return;
            }
        }
        Log.i(logname, "TimerView innerSurfaceDestroyed " + hashCode());
    }


    @Override
    public void renderOnCache() {
        clearUpdateType();
    }

    @Override
    public <T> T getData() {
        return null;
    }

    @Override
    public boolean hasNonUIAction() {
        return false;
    }

    @Override
    public void doNonUIAction() {

    }

    @Override
    public float getComputedHeight() {
        return 0;
    }

    public TimeSelectionView getTimeSelectionView() {
        return timeSelectionView;
    }

    public boolean hasRunningTimer() {
        return ((ClockTimer) timeSelectionView.getData()).isRunning();
    }

    public boolean isDurationSet() {
        return timeSelectionView.getTimeFromScreen().getInternal() > 0;
    }

    public boolean hasRunningElapseTimeUI() {
        return elapseTimeUI != null && ((Stopwatch) elapseTimeUI.getData()).isRunning();
    }



    public void resetElapseTimeUI() {
        if (elapseTimeUI != null) {
//            ((Stopwatch) elapseTimeUI.getData()).stopTime(System.currentTimeMillis());
            rendererWorker.remove(elapseTimeUI);
            items.remove(elapseTimeUI);
            elapseTimeUI = null;
            timeSelectionView.addUpdateType(Units.UPDATE_TYPE.PAINT_ALL);
        }
    }



    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void timerEnd() {
       // Log.i(logname, "timerEnd @" + hashCode());
        if (timeSelectionView != null) {
         //   Log.i(logname, "timerEnd, timeSelectionView NOT null @" + hashCode());
            ClockTimer clockTimer = timeSelectionView.getData();
            elapseTimeUI = new ElapseTimeUI(cachedCanvas, new Params(clockTimer.getDuration(), clockTimer.getStartTime()));
            add(elapseTimeUI);
            timeSelectionView.resetTimer();
        } else {
           // Log.ilogname, "timerEnd, timeSelectionView is NULL @" + hashCode());
            ClockTimer clockTimer = getClockTimer(); // from db
            elapseTimeUI = new ElapseTimeUI(cachedCanvas, new Params(clockTimer.getDuration(), clockTimer.getStartTime()));
        }

        new DbLiveObject<>(getContext()).clearTableAndClose(DbConstant.RUNNING_TIMER_TABLE_NAME);
    }


}
