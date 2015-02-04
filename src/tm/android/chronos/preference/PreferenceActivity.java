/*
 * AudioNotificationPreferenceActivity
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.preference;


import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.widget.Toast;
import tm.android.chronos.core.Units;

/**
 * Entry point for preferences<br>
 * The activity launch  a fragment which class name is retrieve from the intent bundle.
 * The prefix needed to get and store preferences is also retrieve from the intent bundle.
 */
public class PreferenceActivity extends android.preference.PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// The fragment to use is passed into the extra properties of the intent.
		String className = getIntent().getExtras().getString(PreferenceCst.PREF_FRAGMENT_CLASS_NAME);
		try {
			PreferenceFragment fragment = (PreferenceFragment) Class.forName(className).newInstance();
			getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
		} catch (Exception e){
			Toast.makeText(this,Units.getLocalizedTexts("pref_access_failed_fragment", "error_report"), Toast.LENGTH_LONG).show();
			finish();
		}

		// pref keys are prefixed, the prefix is passed to the intent into the extra properties.
		// this is retrieve from the intent bundle
		String prefix = getIntent().getStringExtra(PreferenceCst.PREFIX_BUNDLE_KEY);
		if (prefix==null || prefix.trim().equals("")){
			Toast.makeText(this,Units.getLocalizedTexts("pref_access_failed_prefix", "error_report"), Toast.LENGTH_LONG).show();
			finish();
		}
	}

}
