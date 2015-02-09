/*
 * ${NAME}
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;


/**
 * Created by thmarg on 04/02/15.
 */
public class DbHelper  extends SQLiteOpenHelper {

	public DbHelper(Context context) {
		super(context, "Chronos.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		try {
			for (String srcipt : DbStopwatches.getCreationScripts())
			sqLiteDatabase.execSQL(srcipt);

		} catch (Exception e){
			e.printStackTrace();
		}


	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

	}


}