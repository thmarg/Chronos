/*
 * AudioNotificationPreferenceActivity
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.preference;


import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.widget.Toast;
import tm.android.chronos.R;
import tm.android.chronos.core.Units;

/**
 * Entry point for preferences
 * The activity launch  a fragment which class name is retrieve from the intent bundle.
 * The prefix needed to get and store preferences is also retrieve from the intent bundle.
 */
public class PreferencesActivity extends PreferenceActivity {
    private PreferenceFragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The fragment to use is passed into the extra properties of the intent.
        String className = getIntent().getStringExtra(PreferenceCst.PREF_FRAGMENT_CLASS_NAME);
        if (!isValidFragment(className)) {
            Toast.makeText(getBaseContext(), R.string.bad_fragment,Toast.LENGTH_LONG).show();
            finish();
        }

        try {
            fragment = (PreferenceFragment) Class.forName(className).newInstance();
            getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
        } catch (Exception e) {
            Toast.makeText(this, Units.getLocalizedTexts("pref_access_failed_fragment", "error_report"), Toast.LENGTH_LONG).show();
            finish();
        }


        // pref keys are prefixed, the prefix is passed to the intent into the extra properties.
        // this is retrieve from the intent bundle
        String prefix = getIntent().getStringExtra(PreferenceCst.PREFIX_BUNDLE_KEY);
        if (prefix == null || prefix.trim().equals("")) {
            Toast.makeText(this, Units.getLocalizedTexts("pref_access_failed_prefix", "error_report"), Toast.LENGTH_LONG).show();
            finish();
        }

        // finally title must also be updated and retrieved from the intent bundle.
        String title_key = getIntent().getStringExtra(PreferenceCst.PREF_TITLE);
        setTitle(Units.getLocalizedText(title_key));
        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setIcon(android.R.drawable.ic_menu_manage);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return fragmentName.equals(AudioNotificationPreferenceFragment.class.getName()) || fragmentName.equals(StopwatchPreferenceFragment.class.getName());
    }

    // for being able in a class extending Preference to launch activity by intent for result, to select file or else, and receive onResult.
    PreferenceFragment getFragment(){
        return fragment;
    }
}
