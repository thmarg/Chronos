/*
 * AudioNotificationPrefFragment
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.preference;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import tm.android.chronos.R;

/**
 * Fragment with AudioNotificationPreference
 */
public class AudioNotificationPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.audionotificationpreferences);
    }

}
