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
 * <p>Extends a SurfaceVew and implement this interface so that a thread can loop around renderOnCanvas() and renderOnScreen(Canvas canvas)
 * at interval of your choice.
 * renderOnCanvas allow to render for example a bitmap in memory (or many bitmaps) and after transfer parts that fill fit the screen width end height  on screen.
 * It allow an easy way to do and control custom scrolling on a surface view or to collapse or extend parts of a list</p>
 * <p>RendererWorker is a thread instantiated with a surfaceview to render. Each business object that will have to be diplayed on the surface view
 * have to be registered in the RendererWorker. Such business object are stored in a list. RenderOnCache and RenderOnScreen doesn't pass this list as argument
 * because it is really thread unsafe. It is event unsafe to synchronize the getter method of this list. The only way to work on the list in renderOnCanvas or
 * in renderOnScreen(if needed) on thread safe manner is like this :</p>
 * <p><code>synchronized (rendererWorker.getRenderablekList()) {<br>
 * for (T renderable : rendererWorker.getRenderablekList()) {<br>
 * // do your rendering here (not directly it can be a mess ! use methods)<br>
 * }<br>
 * }<br>
 * </code></p>
 * <p>For a full code example see RendererWorker</p>
 */
public interface SurfaceViewRenderer {
    SurfaceHolder getHolder();

    void renderOnCache();

    void renderOnScreen(Canvas canvas);
}
