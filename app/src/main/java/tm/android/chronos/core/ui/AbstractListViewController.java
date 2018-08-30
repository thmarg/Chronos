package tm.android.chronos.core.ui;


import android.view.MotionEvent;
import android.view.View;
import tm.android.chronos.core.DelayedActionListener;
import tm.android.chronos.core.DelayedActionRunner;
import tm.android.chronos.core.Units;


@SuppressWarnings("unused")
public abstract class AbstractListViewController implements View.OnTouchListener {
    private final int WAIT = -1;
    //  private final int DOWN_DONE = 0;
    private final int MOVE = 1;
    private int status = WAIT;
    //private long now;
    private float lastFingerY = 0;
    private int longClickDelay = 500;
    private DelayedActionRunner<AbstractListViewController.LongClickListener> delayedActionRunner;

    private UIRenderer touchedItem;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        startOnTouch(view, event);
        //String tag = "Chronograph-onTouch";
        AbstractListView list;
        try {
            list = (AbstractListView) view;
        } catch (ClassCastException e) {
            view.performClick();
            return true;
        }
       // Log.i("ON_TOUCH", "Y  --> " + Math.abs(event.getY() - lastFingerY));
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchedItem = list.getItemAt(event.getY());
                lastFingerY = event.getY();
                delayedActionRunner = new DelayedActionRunner<>(DelayedActionRunner.TYPE.ONCE, new LongClickListener(), list, event, touchedItem);
                delayedActionRunner.setDelay(longClickDelay);
                delayedActionRunner.setSleepStepDelay(50);
                delayedActionRunner.setStartTime(System.currentTimeMillis());
                delayedActionRunner.start();
                // Log.i(tag, "DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getY() - lastFingerY) > 10) {
                    delayedActionRunner.safeStop();
                    onTouchScroll(list, event);
                }
                break;
            case MotionEvent.ACTION_UP:
                // Log.i(tag, "UP");
                delayedActionRunner.safeStop();
                if (status == MOVE) { // end of scroll
                    status = WAIT;
                } else {
                    //view.performClick();
                    if (touchedItem != null && touchedItem == list.getItemAt(event.getY()))
                        if (event.getEventTime() - event.getDownTime() < longClickDelay) {
                            // Log.i(tag, "normal click");
                            onTouchNormalClick(list, event, touchedItem);
                        }
                }
        }
        endOnTouch(list, event);
        return true;
    }

    protected abstract void startOnTouch(View view, MotionEvent event);

    protected abstract void onTouchNormalClick(AbstractListView view, MotionEvent event, UIRenderer touchedItem);

    protected abstract void onTouchLongClick(AbstractListView view, MotionEvent event, UIRenderer touchedItem);

    protected abstract void endOnTouch(AbstractListView view, MotionEvent event);


    private void onTouchScroll(AbstractListView view, MotionEvent event) {
        // Log.i("onTouchScroll", (event.getY() - lastFingerY) + "");
        float scrollMove = Math.round(Math.abs(event.getY() - lastFingerY) / 1.2);
        //float speed = scrollMove / (event.getEventTime() - event.getDownTime());
        //  Log.i("onTouchScroll","ScrollMove = " + scrollMove);
        //  Log.i("onTouchScroll","Speed = " + speed);

        if (event.getY() > lastFingerY) {
            if (view.scrollView.top >= scrollMove) {
                view.scrollView.top -= scrollMove;
                view.scrollView.bottom -= scrollMove;
                view.scrollOffset -= scrollMove;
            }
        } else {
            if (view.lastY - view.scrollOffset > view.viewPort.bottom) {
                view.scrollView.top += scrollMove;
                view.scrollView.bottom += scrollMove;
                view.scrollOffset += scrollMove;
            }
        }

        lastFingerY = event.getY();
        status = MOVE;
        // redraw the view
        view.addUpdateType(Units.UPDATE_TYPE.PAINT_ALL);
    }

    protected void setLongClickDelay(int delay) {
        longClickDelay = delay;
    }

    class LongClickListener implements DelayedActionListener {
        @Override
        public void onDelayedAction(Object... obj) {
            if (obj.length == 3 && obj[0] != null && obj[0] instanceof AbstractListView && obj[1] != null && obj[1] instanceof MotionEvent &&
                    obj[2] != null && obj[2] instanceof UIRenderer) {
                onTouchLongClick((AbstractListView) obj[0], (MotionEvent) obj[1], (UIRenderer) obj[2]);
            }
        }

        @Override
        public void onDelayedActionBefore(Object... objects) {
            //unused
        }

        @Override
        public void onDelayedActionAfter(Object... objects) {
            //unused
        }
    }
}


