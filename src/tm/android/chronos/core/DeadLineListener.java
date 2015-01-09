/*  CountDownListener
 *
 *  Copyright (c) 2014 Thierry Margenstern under MIT license
 *  http://opensource.org/licenses/MIT
 */
package tm.android.chronos.core;

/**
 * Call back to be notified when a count down or a dead line is reach.
 */
public interface DeadLineListener {
   void deadLineReached();
}
