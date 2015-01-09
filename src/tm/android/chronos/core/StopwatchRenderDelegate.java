/*
 *   $NAME
 *  *
 *  *  Copyright (c) 2014 Thierry Margenstern under MIT license
 *  *  http://opensource.org/licenses/MIT
 *
 */

package tm.android.chronos.core;

/**
 * Intended to be implemented to render something
 */
public interface StopwatchRenderDelegate<T> {
    void doDrawRun(T t);
    void doDrawNoRun(T t, boolean update);
    void doFinal();
}
