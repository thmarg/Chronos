/*
 * OnDialogClickListener
 *
 *   Copyright (c) 2014 Thierry Margenstern under MIT license
 *   http://opensource.org/licenses/MIT
 */

package tm.android.chronos.dialogs;

import android.app.DialogFragment;

/**
 * Interface to get Ok or cancel event on ChronographeDialog
 */
public interface OnDialogClickListener {
    public void onDialogPositiveClick(DialogFragment dialog);
    public void onDialogNegativeClick(DialogFragment dialog);

}
