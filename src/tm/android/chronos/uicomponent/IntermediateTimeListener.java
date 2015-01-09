/*
 * IntermediateTimeListener
 *
 *   Copyright (c) 2014 Thierry Margenstern under MIT license
 *   http://opensource.org/licenses/MIT
 */

package tm.android.chronos.uicomponent;


/**
 * Interface to receive lap time event and report then to E
 */
public interface IntermediateTimeListener {
    void onIntermediateTimeUpdate(int id);

}
