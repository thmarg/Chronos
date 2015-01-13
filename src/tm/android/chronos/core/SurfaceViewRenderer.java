/*
 * SurfaceViewRenderer
 *
 * Copyright (c) 2014 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import java.util.Vector;

/**
 *  Implement this so that a thread can render on a surface view and with
 *  a cache system which make easy to do and control scrolling.
 */
public interface SurfaceViewRenderer<T extends Clock> {
    SurfaceHolder getHolder();
    void renderOnCache(Vector<T> clockList);
    void renderOnScreen(Canvas canvas);
}
