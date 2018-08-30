package tm.android.chronos.util;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import tm.android.chronos.audio.CommonMediaPlayer;

public class Permissions {

    private static Permissions instance;

    private Permissions() {
    }

    public static Permissions Instance() {
        if (instance == null)
            instance = new Permissions();
        return instance;
    }


    public boolean hasReadWriteExternalStorage(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public Couple<Boolean, Integer> checkZenModeAccess(Context context) {
        boolean access ;
        CommonMediaPlayer.build(context);
        int mode = CommonMediaPlayer.Instance().getAudioManager().getRingerMode();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                access = notificationManager.isNotificationPolicyAccessGranted();
            else access = false;
        } else {
            access = true;
        }
        return new Couple<>(access, mode);
    }

}
