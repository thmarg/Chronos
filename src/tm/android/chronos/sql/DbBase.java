/*
 * DbBase
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.sql;


import android.content.Context;
import tm.android.chronos.core.Units;
import tm.android.chronos.util.ErrorMessage;

import java.lang.reflect.Method;


/**
 * Base object to extends.<br>
 * Common constants and methods
 */
public class DbBase {
	protected final static String[] COUNT_ETOILE = new String[]{"COUNT(*)"};
	protected final  static String[] ETOILE = new String[]{"*"};
	protected   static DbHelper dbHelper;
	protected boolean error;
	protected ErrorMessage errorMessage;
	private final static Class[] NULL_ARG = new Class[0];


	protected DbBase(){
		try {
			errorMessage = new ErrorMessage();
			Class<?> actThreadClass = Class.forName("android.app.ActivityThread");
			Method method = actThreadClass.getDeclaredMethod("currentActivityThread",NULL_ARG);
			method.setAccessible(true);
			Object actThread = method.invoke(actThreadClass, NULL_ARG);
			method = actThreadClass.getDeclaredMethod("getSystemContext",NULL_ARG);
			method.setAccessible(true);
			Object context = method.invoke(actThread,NULL_ARG);
			dbHelper= new DbHelper(((Context)context).createPackageContext("tm.android.chronos",Context.CONTEXT_INCLUDE_CODE));
		} catch (Exception e){
			setOnError(e, Units.getLocalizedText("db_init_failed","error_report"));
		}
	}

	public boolean hasError() {
		return error;
	}

	public ErrorMessage getErrorMessage() {
		error= false;
		return errorMessage;
	}

	protected void setOnError(Exception e, String localizedMessage){
		error=true;
		errorMessage.exception=e;
		errorMessage.localiszedMessage=localizedMessage;
	}
	public static void closeDb(){
		if (dbHelper !=null)
			dbHelper.close();

	}
}
