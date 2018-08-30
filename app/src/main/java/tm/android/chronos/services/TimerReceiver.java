package tm.android.chronos.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import tm.android.chronos.activity.Chronos;

/**
 * Simple but seems the only way to be called systematically when passed to the pendingIntend when setting an alarm.
 */
public class TimerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(Chronos.name + "-TimerReceiver", "CALLED");
        PowerManager.WakeLock wakeLock = null;
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "CHRONOS-ALARM");
            wakeLock.acquire(500);
        }
        TimerRunner timerRunner = new TimerRunner(context);
        timerRunner.run();
        if (wakeLock != null && wakeLock.isHeld())
            wakeLock.release();
    }
}
