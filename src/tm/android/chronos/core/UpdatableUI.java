/*
 * ${NAME}
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.core;

/**
 * Created by thmarg on 22/01/15.
 */
public interface UpdatableUI {
	/**
	 * *
	 * @return true if some info other than time have changed
	 */
	boolean mustUpdateUI();
}
