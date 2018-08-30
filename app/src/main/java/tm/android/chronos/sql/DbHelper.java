/*
 * DbHelper
 *
 * Copyright (c) 2015-2018 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;
import tm.android.chronos.activity.Chronos;


/**
 * DB access for stopwatches. db version :1 (17/06/2018) build 0.5.4
 *
 */
public class DbHelper extends SQLiteOpenHelper {
    private Context context;
    DbHelper(Context context) {
        super(context, "Chronos.db", null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            for (String script : DbConstant.getCreationScripts()) {
                sqLiteDatabase.execSQL(script);
            }
        } catch (Exception e) {
            Log.e(Chronos.name+"-DbHelper","Error while creating database !!! ",e);
            Toast.makeText(context,"Fuck Error while creating database !!!",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
//       Chronos.Logi("-DbHelper","Receive onUpgrade, oldversion: "+ oldVersion+", new version is: "+newVersion);
//        String updateQuery;
//        while (oldVersion<newVersion) {
//            switch (oldVersion) {
//                case 1: // update 1 to 2
//                    sqLiteDatabase.execSQL(DbConstant.create_table_stopwatche_with_id);
//                    break;
//                case 2: // update 2 to 3
//                   // sqLiteDatabase.execSQL(updateQuery);
//                    break;
//            }
//            oldVersion++;
//        }


    }

    public Context getContext(){
        return context;
    }


}