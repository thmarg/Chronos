package tm.android.chronos.uicomponent.event;

import android.view.MotionEvent;
import android.view.View;
import tm.android.chronos.core.ClockTimer;
import tm.android.chronos.core.Stopwatch;
import tm.android.chronos.core.Units;
import tm.android.chronos.uicomponent.ElapseTimeUI;
import tm.android.chronos.uicomponent.TimeSelectionView;
import tm.android.chronos.uicomponent.TimerView;
import tm.android.chronos.util.geom.Point;

import static tm.android.chronos.util.geom.Point.SYMMETRY_TYPE.X_AXIS;

public class TimerViewController implements View.OnTouchListener {
    private Point previousPoint;


    @Override
    public boolean onTouch(View view, MotionEvent event) {

        view.performClick();
        TimerView timerView = (TimerView) view;
        TimeSelectionView timeSelectionView = (TimeSelectionView) timerView.items.get(0);
        if (((ClockTimer) timeSelectionView.getData()).isRunning())
            return true;
        if (timerView.items.size() > 1) {
            ElapseTimeUI elapseTimeUI = (ElapseTimeUI) timerView.items.get(1);
            if (((Stopwatch) elapseTimeUI.getData()).isRunning())
                return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                previousPoint = new Point(event.getX(), event.getY(), Point.TYPE.XY).getSymmetrical(X_AXIS, Point.ZERO).getChangedCoordinates(timeSelectionView.localCoordInScreenCoord);
                return true;
            case MotionEvent.ACTION_UP:
                //previousPoint = null;
                return true;
            case MotionEvent.ACTION_MOVE:
                Point currentPoint = new Point(event.getX(), event.getY(), Point.TYPE.XY).getSymmetrical(X_AXIS, Point.ZERO).getChangedCoordinates(timeSelectionView.localCoordInScreenCoord);
                double delta = currentPoint.getAngle() - previousPoint.getAngle();
                if (Math.abs(delta) > 0) {
                    timeSelectionView.rotMinute = 0.65 * delta;
                    timeSelectionView.addUpdateType(Units.UPDATE_TYPE.UPDATE_HEAD_DIGIT);

                    previousPoint = currentPoint;
                }

                return true;

        }
        return false;
    }
}
