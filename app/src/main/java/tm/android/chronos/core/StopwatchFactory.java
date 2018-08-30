/*
 * StopwatchFactory
 *
 * Copyright (c) 2014 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */
package tm.android.chronos.core;

/**
 * Factory to create object in parametrized class param T where a T must be created.
 * but due to type erasure at runtime this is not possible. (can't write T t = new T())<br>
 */
public class StopwatchFactory {

    @SuppressWarnings("unchecked")
    public static <T extends Stopwatch> T create() {
        T ret;
        ret = (T) (new StopwatchImpl());
        return ret;
    }
}
