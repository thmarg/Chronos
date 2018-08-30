/*
 * Chronos
 *
 *   Copyright (c) 2014-2018 Thierry Margenstern under MIT license
 *   http://opensource.org/licenses/MIT
 */

package tm.android.chronos.activity;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import tm.android.chronos.BuildConfig;
import tm.android.chronos.R;
import tm.android.chronos.audio.CommonMediaPlayer;
import tm.android.chronos.core.Units;
import tm.android.chronos.sql.DbBase;
import tm.android.chronos.util.Couple;
import tm.android.chronos.util.Permissions;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Entry point of the Application
 */
public class Chronos extends AppCompatActivity {
    public final static String name = "CHRONOS";
    @SuppressLint("SimpleDateFormat")
    public final static SimpleDateFormat ftime = new SimpleDateFormat("HH:mm:ss");
    @SuppressLint("SimpleDateFormat")
    public final static SimpleDateFormat fdate = new SimpleDateFormat("dd/MM/yyyy");
    public final static String DIRECT_CALL = "DIRECT_CALL";
    private static boolean read_write_ext_storage = false;
    private Dialog permissionDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chronos);
        Toolbar toolbar = findViewById(R.id.chronos_toolbar);
        setSupportActionBar(toolbar);
        // don't forget this for i18n support from component without android context.
        if (Units.hasNoResources())
            Units.setResources(getResources());


        String directCall = getIntent().getStringExtra(DIRECT_CALL);
        if (directCall != null && !directCall.equals("NONE")) {
            if (directCall.equals(TimerActivity.class.getName())) {
                onClick(findViewById(R.id.btn_timerActivity));
            } else if (directCall.equals(AlarmResultActivity.class.getName())) {
                Intent intent = new Intent(getBaseContext(), AlarmResultActivity.class);
                intent.putExtra("AlarmId", getIntent().getLongExtra("AlarmId", -1));
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }

        } else {
            checkPermission();
            if (read_write_ext_storage)
                CommonMediaPlayer.build(this);

            checkZenModeAccess();
        }

    }


    private void checkZenModeAccess() {
        Couple<Boolean, Integer> result = Permissions.Instance().checkZenModeAccess(this);
        if (!result.getKey()) { // acces not granted, present system setting to user.
            // no access to do not disturb settings, request !
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivityForResult(intent, 0);
            } // bellow version M it is transparently granted.
        }
    }

    private void checkPermission() {
        if (!Permissions.Instance().hasReadWriteExternalStorage(this)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissionDialog = new Dialog(this);
                permissionDialog.setTitle(R.string.permission_title);
                LayoutInflater inflater = getLayoutInflater();
                @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.permissionlayout, null);
                TextView textView = view.findViewById(R.id.txv_message);
                textView.setText(R.string.permission_text);
                OnClickListener onClickListener = new OnClickListener();
                Button btn_ok = view.findViewById(R.id.btn_ok);
                btn_ok.setOnClickListener(onClickListener);
                Button btn_cancel = view.findViewById(R.id.btn_cancel);
                btn_cancel.setOnClickListener(onClickListener);
                permissionDialog.setContentView(view);
                permissionDialog.show();
            } else { // no need to justify !?
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        read_write_ext_storage = (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            String version = BuildConfig.DEBUG ? "Debug " : "Release ";
            String updateTime;
            try {
                version += getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                updateTime = new Date(getPackageManager().getPackageInfo(getPackageName(), 0).lastUpdateTime).toString();
            } catch (PackageManager.NameNotFoundException e) {
                version += getResources().getString(R.string.not_found);
                updateTime = "None";
            }
            version = getResources().getString(R.string.chronos_version) + version + ", ftime: " + updateTime;
            Toast.makeText(getBaseContext(), version, Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @SuppressWarnings({"unchecked", "WeakerAccess"})
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn_StopwatchAcitvity:
                Intent intent = new Intent(getBaseContext(), ChronometerActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_timerActivity:
                Intent intent3 = new Intent(getBaseContext(), TimerActivity.class);
                startActivity(intent3);
                break;
            case R.id.btn_alarmActivity:
                Intent intent1 = new Intent(this, AlarmActivity.class);
                startActivity(intent1);
                break;
            case R.id.btn_metronomeActivity:
//                Intent intent2 = new Intent(this, MetronomeActivity.class);
//                startActivity(intent2);
//                break;
            default:
                Dialog dialog = new Dialog(this);
                TextView textView = new TextView(this);
                textView.setText("Not yet implemented !\nComing soon.");
                //textView.setTextAppearance(android.R.style.TextAppearance_Large);
                dialog.setContentView(textView);
                dialog.setTitle("Message-oup!!!");
                dialog.show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DbBase.closeDb();
        //CommonMediaPlayer.Instance().releasePlayer();
    }


    private class OnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_cancel:
                    permissionDialog.cancel();
                    break;
                case R.id.btn_ok:
                    ActivityCompat.requestPermissions(Chronos.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    permissionDialog.cancel();
                    break;
            }
        }
    }

    public static void Logi(String tag, String message) {
        String fullmessage = tag + " - " + message;
        Log.d(name, fullmessage);
    }

    public static void Logdebug(String tag, String message, Throwable e) {
        if (BuildConfig.DEBUG) {
            String fullmessage = tag + " - " + message;
            Log.d(name, fullmessage, e);
        }
    }

    public static void Logdebug(String tag, String message) {
        if (BuildConfig.DEBUG) {
            String fullmessage = tag + " - " + message;
            Log.d(name, fullmessage);
        }
    }
}





