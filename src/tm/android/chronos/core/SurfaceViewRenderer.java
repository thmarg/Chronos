/*
 * SurfaceViewRenderer
 *
 * Copyright (c) 2014 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;

import android.graphics.Canvas;
import android.view.SurfaceHolder;


/**
 *  Implement this so that a thread can render on a surface view and with
 *  a cache system which make easy to do and control scrolling.
 */
public interface SurfaceViewRenderer {
    SurfaceHolder getHolder();
    void renderOnCache();
    void renderOnScreen(Canvas canvas);
}
