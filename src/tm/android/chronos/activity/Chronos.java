/*
 * $SRC
 *
 *   Copyright (c) 2014 Thierry Margenstern under MIT license
 *   http://opensource.org/licenses/MIT
 */

package tm.android.chronos.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import tm.android.chronos.R;
import tm.android.chronos.core.Stopwatch;
import tm.android.chronos.core.Units;
import tm.android.chronos.sql.DbBase;

/**
 * Entry point of the Application
 */
public class Chronos extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chronos);
        // don't forget this for i18n support from some component (>50%).
        Units.setResources(getResources());
    }


    @SuppressWarnings("unchecked")
    public void onClick(View view) {
        if (view.getId()==R.id.btn_StopwatchAcitvity) {
            try {
                Class<ChronometerActivity<Stopwatch>> cl = (Class<ChronometerActivity<Stopwatch>>) Class.forName("tm.android.chronos.activity.ChronometerActivity");
                Intent intent = new Intent(getBaseContext(), cl);
                startActivity(intent);
            } catch (ClassNotFoundException e) {
                Toast.makeText(this, "Unable to instantiate ChronometerActivity", Toast.LENGTH_LONG).show();
            }
        } else if (view.getId()==R.id.btn_timerActivity){
            Intent intent = new Intent(getBaseContext(),ActivityTimer.class);
            startActivity(intent);
        } else {
            Dialog dialog = new Dialog(this);
            TextView textView = new TextView(this);
            textView.setText("Not yet implemented !\nComing soon.");
            textView.setTextAppearance(getBaseContext(),android.R.style.TextAppearance_Large);
            dialog.setContentView(textView);
            dialog.setTitle("Message-oup!!!");
            dialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DbBase.closeDb();
    }
}





