package tm.android.chronos.uicomponent.event;

import android.view.MotionEvent;
import android.view.View;
import tm.android.chronos.core.Alarm;
import tm.android.chronos.core.ui.AbstractListView;
import tm.android.chronos.core.ui.AbstractListViewController;
import tm.android.chronos.core.ui.UIRenderer;
import tm.android.chronos.uicomponent.AlarmListView;

public class AlarmListViewController extends AbstractListViewController {
    @Override
    protected void startOnTouch(View view, MotionEvent event) {

    }

    @Override
    protected void onTouchNormalClick(AbstractListView view, MotionEvent event, UIRenderer touchedItem) {
        Alarm alarm = touchedItem.getData();
        if (alarm.isRunning())
            ((AlarmListView) view).viewAlarm(touchedItem);
        else
            ((AlarmListView) view).editAlarm(touchedItem);

    }

    @Override
    protected void onTouchLongClick(AbstractListView view, MotionEvent event, UIRenderer touchedItem) {
        view.select(touchedItem);
    }

    @Override
    protected void endOnTouch(AbstractListView view, MotionEvent event) {

    }
}
