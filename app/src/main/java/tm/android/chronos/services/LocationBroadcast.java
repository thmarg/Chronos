package tm.android.chronos.services;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import tm.android.chronos.activity.Chronos;

import java.util.List;

public class LocationBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Chronos.Logi("LocationBroadcast","onReceive");
        Chronos.Logi("LocationBroadcast","Context: "+String.valueOf(context) + ", intent: "+String.valueOf(intent));
        //Location location = intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
        //Chronos.Logi("LocationBroadcast","Location: "+location.toString());
    }
}
