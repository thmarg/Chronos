/*
 * DelayedActionListener
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;

/**
 * Interface to implements to be passed in a DelayedRunner
 */
public interface DelayedActionListener {
    void onDelayedActionBefore(Object... objects);
    void onDelayedAction(Object... objects);
    void onDelayedActionAfter(Object ... objects);

}
