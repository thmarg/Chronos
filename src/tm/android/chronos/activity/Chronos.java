/*
 * Chronos
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
import tm.android.chronos.R;

/**
 * Entry point of the Application
 */
public class Chronos extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chronos);

        
    }



    public void onClick(View view) {
            if (view.getId()==R.id.btn_StopwatchAcitvity){
                Intent intent = new Intent(getBaseContext(),ChronometerActivity.class);
                startActivity(intent);
            } else {
                Dialog dialog = new Dialog(this);
                TextView textView = new TextView(this);
                textView.setText("Not yet implemented !\nComing soon.");
                textView.setTextAppearance(getBaseContext(),android.R.style.TextAppearance_Large);
                dialog.setContentView(textView);
                //dialog.setContentView(R.layout.layoutforspinner);
                dialog.setTitle("Message-oup!!!");
                dialog.show();
            }
    }
    
    
    
}




    
