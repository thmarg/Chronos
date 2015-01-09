/*
 *   ChronometerActivity
 *
 *    Copyright (c) 2014 Thierry Margenstern under MIT license
 *    http://opensource.org/licenses/MIT
 *
 */

package tm.android.chronos.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ExpandableListView;
import tm.android.chronos.R;
import tm.android.chronos.core.Units;
import tm.android.chronos.uicomponent.ExpandableAdapter;
import tm.android.chronos.uicomponent.IntermediateTimeListener;

/**
 *
 */
public class ChronometerActivity extends Activity  {

    private ExpandableListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chronolayout);
        listView = (ExpandableListView)findViewById(R.id.expandableListView);

        ExpandableAdapter expandableAdapter = new ExpandableAdapter(this);

        listView.setAdapter(expandableAdapter);
        expandableAdapter.setHisExpandableListView(listView);

        Units.setResources(getResources());

    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
