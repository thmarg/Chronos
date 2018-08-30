/*
 * StopwatchPreference
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.*;
import tm.android.chronos.R;
import tm.android.chronos.core.Track;
import tm.android.chronos.core.Units;
import tm.android.chronos.dialogs.OnDialogClickListener;
import tm.android.chronos.dialogs.PointDialog;
import tm.android.chronos.dialogs.TrackDialog;
import tm.android.chronos.localisation.Point;
import tm.android.chronos.sql.TrackFactory;


/**
 * List of tracks<br>
 * Creation, deletion  and edition
 * tracks are stored in db
 * Tracks are used with stopwatch of type "track"
 */
public class ActivityTracks extends Activity {

    private static final String TRACKS = "tracks";
    private static final String POINTS = "points";
    private ListView track_list;
    private ListView point_list;
    private ArrayAdapter<Track> trackArrayAdapter;
    private ArrayAdapter<Point> pointArrayAdapter;
    private View selectedViewTrack = null;
    private Track selectedTrack = null;// single selection
    private Track currentTrack = null; // This is the track passed to the DialogEditor.
    private View selectedViewPoint = null;
    private Point selectedPoint = null;
    private Point currentPoint = null;
    private TabHost tabHost;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trackArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1);
        pointArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1);

        View view = getLayoutInflater().inflate(R.layout.tracks_list, null);
        track_list = view.findViewById(R.id.tracks_list);
        track_list.setAdapter(trackArrayAdapter);
        track_list.setClickable(true);
        track_list.setOnItemClickListener(new ItemClickListener());
        track_list.setOnItemLongClickListener(new ItemLongClickListener());
        track_list.setItemsCanFocus(true);

        point_list = view.findViewById(R.id.points_list);
        point_list.setAdapter(pointArrayAdapter);
        point_list.setClickable(true);
        point_list.setOnItemClickListener(new ItemClickListener());
        point_list.setOnItemLongClickListener(new ItemLongClickListener());
        point_list.setItemsCanFocus(true);

        tabHost = view.findViewById(android.R.id.tabhost);
        tabHost.setup();


        ContentFactory contentFactory = new ContentFactory();
        TabHost.TabSpec tabSpec = tabHost.newTabSpec(TRACKS);
        tabSpec.setIndicator("Tracks");
        tabSpec.setContent(contentFactory);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec(POINTS);
        tabSpec.setIndicator("Points");
        tabSpec.setContent(contentFactory);
        tabHost.addTab(tabSpec);


        tabHost.setCurrentTab(0);
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                tabChanged(tabId);
            }
        });


        ClickListener clickListener = new ClickListener();

        view.findViewById(R.id.img_btn_plus).setOnClickListener(clickListener);
        view.findViewById(R.id.img_btn_moins).setOnClickListener(clickListener);

        setContentView(view);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (tabHost != null)
            tabChanged(tabHost.getCurrentTabTag());

    }

    private void tabChanged(String tabId) {
        TrackFactory factory = new TrackFactory(this);
        if (TRACKS.equals(tabId)) {
            if (trackArrayAdapter != null && trackArrayAdapter.isEmpty()) {
                trackArrayAdapter.addAll(factory.getTracks());
            }
        } else {
            if (pointArrayAdapter != null && pointArrayAdapter.isEmpty()) {
                pointArrayAdapter.addAll(factory.getPoints());
            }
        }
    }


    private void startTrackDialog() {
        TrackDialog dialog = new TrackDialog();
        dialog.setDialogClickListener(new DialogListener());
        dialog.setTrack(currentTrack);
        dialog.show(getFragmentManager(), "Chronos-trackDialog");
    }

    private void startPointDialog() {
        PointDialog dialog = new PointDialog();
        dialog.setDialogClickListener(new DialogListener());
        dialog.setPoint(currentPoint);
        dialog.show(getFragmentManager(), "Chronos-pointDialog");
    }

    private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.img_btn_plus:
                    if (TRACKS.equals(tabHost.getCurrentTabTag())) {
                        TrackFactory factory = new TrackFactory(getBaseContext());
                        if (factory.getPoints().isEmpty()){
                            Toast.makeText(getBaseContext(),R.string.first_create_point,Toast.LENGTH_LONG).show();
                            return;
                        }
                        currentTrack = new Track(String.valueOf(System.currentTimeMillis()));
                        startTrackDialog();
                    } else {
                        currentPoint = new Point(String.valueOf(System.currentTimeMillis()));
                        startPointDialog();
                    }
                    break;
                case R.id.img_btn_moins:
                    if (TRACKS.equals(tabHost.getCurrentTabTag())) {
                        if (selectedViewTrack != null) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityTracks.this);
                            builder.setMessage(Units.getLocalizedText(R.string.track_confirm_delete));
                            builder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    TrackFactory trackFactory = new TrackFactory(getBaseContext());
                                    trackFactory.deleteTrack(selectedTrack);
                                    if (trackFactory.hasError()) {
                                        Toast.makeText(getBaseContext(), "Problem occur with DB " + trackFactory.getErrorMessage().localiszedMessage, Toast.LENGTH_LONG).show();
                                    } else {
                                        trackArrayAdapter.remove(selectedTrack);
                                        selectedViewTrack.setBackgroundColor(Color.WHITE);
                                        selectedTrack = null;
                                        selectedViewTrack = null;
                                    }
                                }
                            });
                            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            builder.show();
                        }
                    } else {
                        if (selectedViewPoint != null) {
                            final TrackFactory factory = new TrackFactory(getBaseContext());
                            if (factory.isPointInUse(selectedPoint.getId())) {
                                Toast.makeText(getBaseContext(),R.string.point_forbiden_deletion,Toast.LENGTH_LONG).show();
                                return;
                            }
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityTracks.this);
                            builder.setMessage(Units.getLocalizedText(R.string.point_confirm_delete));
                            builder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    factory.deletePoint(selectedPoint);
                                    if (factory.hasError()) {
                                        Toast.makeText(getBaseContext(), Units.getLocalizedText(R.string.database_error)+" " + factory.getErrorMessage().localiszedMessage, Toast.LENGTH_LONG).show();
                                    } else {
                                        pointArrayAdapter.remove(selectedPoint);
                                        selectedViewPoint.setBackgroundColor(Color.WHITE);
                                        selectedPoint = null;
                                        selectedViewPoint = null;
                                    }
                                }
                            });
                            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            builder.show();
                        }
                    }
                    break;
            }
        }
    }


    private class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) { // launch editor in mode edit
            if (TRACKS.equals(tabHost.getCurrentTabTag())) {
                currentTrack = trackArrayAdapter.getItem(position);
                startTrackDialog();
            } else {
                currentPoint = pointArrayAdapter.getItem(position);
                startPointDialog();
            }
        }

    }

    private class ItemLongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {// select or deselect a row
            if (TRACKS.equals(tabHost.getCurrentTabTag())) {
                if (view == selectedViewTrack) {
                    view.setBackgroundColor(Color.WHITE);
                    selectedViewTrack = null;
                    selectedTrack = null;
                } else {
                    if (selectedViewTrack != null)
                        selectedViewTrack.setBackgroundColor(Color.WHITE);

                    view.setBackgroundColor(Color.GRAY);
                    selectedViewTrack = view;
                    selectedTrack = trackArrayAdapter.getItem(position);
                }
            } else {
                if (view == selectedViewPoint) {
                    view.setBackgroundColor(Color.WHITE);
                    selectedViewPoint = null;
                    selectedPoint = null;
                } else {
                    if (selectedViewPoint != null)
                        selectedViewPoint.setBackgroundColor(Color.WHITE);

                    view.setBackgroundColor(Color.GRAY);
                    selectedViewPoint = view;
                    selectedPoint = pointArrayAdapter.getItem(position);
                }
            }
            return true;
        }
    }


    private class DialogListener implements OnDialogClickListener {
        @Override
        public void onDialogPositiveClick(DialogFragment dialog) {
            if (TRACKS.equals(tabHost.getCurrentTabTag()))
                onTrackDialogPositiveClick();
            else
                onPointDialogPositiveClick();
        }

        private void onPointDialogPositiveClick() {
            if (currentPoint != null) {
                TrackFactory factory = new TrackFactory(getBaseContext());
                if (pointArrayAdapter.getPosition(currentPoint) == -1) {
                    factory.storePoint(currentPoint);
                    if (factory.hasError()) {
                        Toast.makeText(getBaseContext(), Units.getLocalizedText(R.string.database_error)+" " + factory.getErrorMessage().localiszedMessage, Toast.LENGTH_LONG).show();
                    } else {
                        pointArrayAdapter.add(currentPoint);
                    }
                } else {
                    factory.updatePoint(currentPoint);
                    if (factory.hasError()) {
                        Toast.makeText(getBaseContext(), Units.getLocalizedText(R.string.database_error)+" " + factory.getErrorMessage().localiszedMessage, Toast.LENGTH_LONG).show();
                    } else {
                        pointArrayAdapter.notifyDataSetChanged();
                    }
                }
                currentPoint = null;
            }
        }

        private void onTrackDialogPositiveClick() {
            if (currentTrack != null) {
                TrackFactory trackFactory = new TrackFactory(getBaseContext());
                if (trackArrayAdapter.getPosition(currentTrack) == -1) {
                    trackFactory.storeTrack(currentTrack);
                    if (trackFactory.hasError()) {
                        Toast.makeText(getBaseContext(), Units.getLocalizedText(R.string.database_error)+" " + trackFactory.getErrorMessage().localiszedMessage, Toast.LENGTH_LONG).show();
                    } else {
                        trackArrayAdapter.add(currentTrack);
                    }

                } else {
                    trackFactory.updateTrack(currentTrack);
                    if (trackFactory.hasError()) {
                        Toast.makeText(getBaseContext(), Units.getLocalizedText(R.string.database_error)+" " + trackFactory.getErrorMessage().localiszedMessage, Toast.LENGTH_LONG).show();
                    } else {
                        trackArrayAdapter.notifyDataSetChanged();
                    }

                }
                currentTrack = null;
            }
        }

        @Override
        public void onDialogNegativeClick(DialogFragment dialog) {
            if (TRACKS.equals(tabHost.getCurrentTabTag()))
                currentTrack = null;
            else
                currentPoint = null;

        }
    }


    private class ContentFactory implements TabHost.TabContentFactory {
        @Override
        public View createTabContent(String tag) {
            switch (tag) {
                case TRACKS:
                    return track_list;
                case POINTS:
                    return point_list;
                default:
                    return null;

            }
        }
    }
}

