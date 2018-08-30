/*
 * ${NAME}
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core.ui;

import android.graphics.Canvas;
import android.util.Log;

import java.util.Vector;


/**
 * Created by thmarg on 11/04/2018.
 */
public class RendererWorker extends Thread {
    private final Vector<UIRenderer> renderableList;
    private boolean run = true;
    private int innerSleepTime = 20;// this will be by default the delay between each rendering of the UI on screen (+ of course all the time of the computations done between each rendering if any)

    private AbstractView mainGUI;

    public RendererWorker() {
        renderableList = new Vector<>(5);
    }

    public void setMainGUI(AbstractView ui) {
        this.mainGUI = ui;
    }

    public void register(UIRenderer renderer) {
        synchronized (renderableList) {
            renderableList.add(renderer);
        }
    }


    public boolean remove(UIRenderer renderer) {
        synchronized (renderableList) {
            return renderableList.remove(renderer);
        }

    }

    @Override
    public void run() {
        try {
            sleep(100);
        } catch (InterruptedException e) {
            // nothing, leave anyway.
        }
        Canvas canvas;
        while (run) {
            if (mainGUI != null) {
                if (mainGUI.drawMe() && mustUpdateUI()) {
                 //   Log.i("RendererWorker", "drawMe: " + mainGUI.drawMe() + ", mustUpdateUI: " + mustUpdateUI() + " child count: " + renderableList.size()+ " @"+hashCode());
                    synchronized (mainGUI.getHolder()) {
                        try {
                            if (mainGUI.mustUpdateUI()) mainGUI.renderOnCache();
                            synchronized (renderableList) {
                                for (UIRenderer renderer : renderableList)
                                    if (renderer.mustUpdateUI()) {
                                        renderer.renderOnCanvas(mainGUI.getCachedCanvas());
                                        if (renderer.hasSizeChanged()) {
                                            mainGUI.sizeChangedFor(renderer);
                                            renderer.resetSizeChangedStatus();
                                        }
                                    }
                            }
                            canvas = mainGUI.getHolder().lockCanvas();
                            if (canvas != null) {
                                int hash = canvas.hashCode();
                                mainGUI.renderOnScreen(canvas);
                                if (canvas.hashCode() == hash)
                                    mainGUI.getHolder().unlockCanvasAndPost(canvas);
                            }

                        } catch (Exception e) {
                            Log.e("RendererWorker", "OUPS !!!", e);//
                        }
                    }

                } else
                    synchronized (renderableList) {
                        for (UIRenderer renderer : renderableList)
                            if (renderer.hasNonUIAction()) {
                                renderer.doNonUIAction();
                                renderer.setHasNonUIAction(false);
                            }
                    }
            } else {  // no main ui, each UIRenderer draw on it's own canvas.
                synchronized (renderableList) {
                    for (UIRenderer renderer : renderableList)
                        if (renderer.drawMe() && renderer.mustUpdateUI()) {
                            canvas = renderer.getHolder().lockCanvas();
                            if (canvas != null) {
                                renderer.renderOnCache();
                                renderer.renderOnScreen(canvas);
                                renderer.getHolder().unlockCanvasAndPost(canvas);
                            }
                        } else if (renderer.hasNonUIAction())
                            renderer.doNonUIAction();
                }
            }

            try {
                sleep(innerSleepTime); // sleep a little !
            } catch (InterruptedException e) {
                //
            }

        }
    }

    private boolean mustUpdateUI() {
        boolean ret = mainGUI.mustUpdateUI();
        synchronized (renderableList) {
            for (UIRenderer renderer : renderableList)
                ret |= renderer.mustUpdateUI();
        }
        return ret;

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

    public void setInnerSleepTime(int innerSleepTime) {
        this.innerSleepTime = innerSleepTime;
    }


}

