package tm.android.chronos.audio;

import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import tm.android.chronos.core.DelayedActionListener;

public class VibratorVariator implements DelayedActionListener {
    private Vibrator vibrator;

    @Override

    public void onDelayedActionBefore(Object... objects) {
        if (objects.length != 4 && !(objects[0] instanceof Vibrator) && !(objects[1] instanceof long[])
                && !(objects[2] instanceof int[]) && !(objects[3] instanceof Integer))
                return;

        vibrator = (Vibrator)objects[0];
        long[] pattern  = (long[])objects[1];
        int[] amplitude = (int[])objects[2];
        int repeat = (int)objects[3];
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
            if (vibrator.hasAmplitudeControl())
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitude, repeat));
            else
                vibrator.vibrate(VibrationEffect.createWaveform(pattern,repeat));
        } else
            vibrator.vibrate(pattern,repeat);
    }

    @Override
    public void onDelayedAction(Object... objects) {
        // nothing to do
    }

    @Override
    public void onDelayedActionAfter(Object... objects) {
        if (vibrator != null)
            vibrator.cancel();
    }
}
