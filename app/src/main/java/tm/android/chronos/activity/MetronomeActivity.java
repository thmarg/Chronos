package tm.android.chronos.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

public class MetronomeActivity extends Activity {
    private final static String logname = Chronos.name+"-ActivityTest";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(logname, " onCreate " + hashCode());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(logname, " onDestroy " + hashCode());
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


}
