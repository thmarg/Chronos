/*
 *  ClockWorker
 *
 *  Copyright (c) 2014 Thierry Margenstern under MIT license
 *  http://opensource.org/licenses/MIT
 */
package tm.android.chronos.core;

import android.graphics.Canvas;
import java.util.Vector;

/**
 * The thread that loop "forever" and send all the registered stopwatches to
 * the UI to be rendered and driven by input event on screen.
 */
public class ClockWorker<T extends Clock> extends Thread {
    private final Vector<T> clockList;
    private boolean run = true;
    private boolean stopDisplay=false;
    private SurfaceViewRenderer<T> surfaceViewRenderer;

    public ClockWorker(SurfaceViewRenderer<T> surfaceViewRenderer) {
        this.surfaceViewRenderer = surfaceViewRenderer;
        clockList = new Vector<T>(5);
    }


    public void finalStop() {
        run = false;

        try {
            interrupt();
            join(100);
        } catch (InterruptedException e) {
            // nothing, leave anyway.
        }
    }


    @Override
    public void run() {
        try {
            sleep(100);
        } catch (InterruptedException e) {
            // nothing, leave anyway.
        }
        Canvas canvas = null;
        while (run) {
            if (stopDisplay || noneChronoIsRunning()){
                try {
                    sleep(400);
                    continue;
                }catch (InterruptedException e){
                    //
                }
            }
            synchronized (surfaceViewRenderer.getHolder()) {
                try {
                    surfaceViewRenderer.renderOnCache(clockList);
                    canvas = surfaceViewRenderer.getHolder().lockCanvas();
                    surfaceViewRenderer.renderOnScreen(canvas);
                    sleep(40);
                } catch (InterruptedException e) {
                    //
                } finally {
                    if (canvas != null)
                        surfaceViewRenderer.getHolder().unlockCanvasAndPost(canvas);

                }
            }
        }
    }

    public boolean register(T clock) {
        return clockList.add(clock);
    }

    public boolean remove(T clock) {
        return clockList.remove(clock);
    }

    public T get(int i) {
        return clockList.get(i);

    }


    public boolean isDisplayStopped(){
        return  stopDisplay;

    }

    public void setStopDisplay(boolean stopDisplay) {
        this.stopDisplay = stopDisplay;
    }

    public boolean noneChronoIsRunning(){
        for (T t:clockList)
            if (t.isRunning())
                return false;
        return true;

    }

    public  Vector<T> getClockList() {
        return clockList;
    }

    public int getClocksCount(){
        return clockList.size();
    }

}
