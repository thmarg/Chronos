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


/**
 * Base object to extends.<br>
 * Common constants and methods
 */
public class DbBase {
    final static String[] STAR = new String[]{"*"};
    static DbHelper dbHelper;
    boolean error;
    ErrorMessage errorMessage;

    DbBase(Context context) {
        errorMessage = new ErrorMessage();
        try {
            dbHelper = new DbHelper(context.createPackageContext("tm.android.chronos", Context.CONTEXT_INCLUDE_CODE));
        } catch (Exception e) {
            setOnError(e, Units.getLocalizedText("db_init_failed", "error_report"));
        }
    }

    public static void closeDb() {
        if (dbHelper != null)
            dbHelper.close();

    }

    public boolean hasError() {
        return error;
    }

    public ErrorMessage getErrorMessage() {
        error = false;
        return errorMessage;
    }

    void setOnError(Exception e, String localizedMessage) {
        error = true;
        errorMessage.exception = e;
        errorMessage.localiszedMessage = localizedMessage;
    }
}
