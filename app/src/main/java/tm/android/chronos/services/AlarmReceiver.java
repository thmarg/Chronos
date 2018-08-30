package tm.android.chronos.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import tm.android.chronos.activity.Chronos;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager.WakeLock wakeLock = null;
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "CHRONOS-ALARM");
            wakeLock.acquire(500);
        }
        // we continue
        String[] toks;
        if (intent.getDataString() != null && (toks = intent.getDataString().split("@")).length == 2) {
            Log.i(Chronos.name + "-AlarmReceiver", "CALLED !!! with alarm id " + toks[1]);
            AlarmRunner alarmRunner = new AlarmRunner(context, Long.valueOf(toks[1]));
            alarmRunner.run();
        } else {
            Log.i(Chronos.name + "-AlarmReceiver", "CALLED but without the expected Data String, AlarmRunner not called");
        }
        if (wakeLock != null && wakeLock.isHeld())
            wakeLock.release();
    }
}
