package tm.android.chronos.uicomponent;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;
import tm.android.chronos.R;
import tm.android.chronos.activity.Chronos;
import tm.android.chronos.core.Alarm;
import tm.android.chronos.core.AlarmData;
import tm.android.chronos.core.Digit;
import tm.android.chronos.core.Units;
import tm.android.chronos.core.ui.AbstractListView;
import tm.android.chronos.core.ui.UIRenderer;
import tm.android.chronos.dialogs.AlarmDialog;
import tm.android.chronos.dialogs.OnDialogClickListener;
import tm.android.chronos.services.AlarmServices;
import tm.android.chronos.sql.DbConstant;
import tm.android.chronos.sql.DbLiveObject;

import java.util.ArrayList;
import java.util.List;

public class AlarmListView extends AbstractListView implements OnDialogClickListener {
    private AlarmDialog.MODE currentMode;
    private UIRenderer currentAlarmUIInEditMode;

    public AlarmListView(Context context) {
        super(context);
        Digit.setInitilaDigitFormat(Units.DIGIT_FORMAT.NO_MS_SHORT);
    }


    @Override
    protected void innerSurfaceCreated(SurfaceHolder surfaceHolder) {
        if (items.isEmpty()) {
            // check database, despite name, all alarm on the UI, runing or not are stored
            DbLiveObject<Alarm> dbLiveObject = new DbLiveObject<>(getContext());
            List<Alarm> lst = dbLiveObject.getRunningLiveObjectsWithId(DbConstant.RUNNING_ALARMS_TABLE_NAME);
            if (!lst.isEmpty()) {
                for (Alarm alarm : lst) {
                    if (alarm.isRunning() && !alarm.isPassed()) {
                        AlarmServices.updateAlarm(getContext(), alarm);

                    }
                    addAlarm(alarm);
                }
            }
            dbLiveObject.close();
        }
    }

    @Override
    protected void innerSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    protected void innerSurfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void renderOnCache() {
        clearUpdateType();
    }

    @Override
    public <T> T getData() {
        return null;
    }

    @Override
    public void doNonUIAction() {

    }

    @Override
    public float getComputedHeight() {
        return 0;
    }


    private void addAlarm(Alarm alarm) {
        add(new AlarmUI(alarm, cachedCanvas));
    }

    public void remove() {
        if (!selectedItems.isEmpty()) {
            Alarm alarm = selectedItems.get(0).getData();
            if (!alarm.isRunning() || (alarm.isRunning() && alarm.isPassed())) {
                List<UIRenderer> toRemove = new ArrayList<>(5);
                toRemove.addAll(selectedItems); // single selection, should always have only one element.
                super.remove(toRemove);
                // and don't forget to remove from DB
                DbLiveObject<Alarm> dbLiveObject = new DbLiveObject<>(getContext());
                dbLiveObject.deleteFromTableById(DbConstant.RUNNING_ALARMS_TABLE_NAME, alarm.getId());
                dbLiveObject.close();

            } else {
                Toast.makeText(getContext(), R.string.alarm_dont_delete_running, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void addNewAlarm() {
        currentMode = AlarmDialog.MODE.CREATE;
        startAlarmDialog(null);
    }

    public void editAlarm(UIRenderer alarmUI) {
        currentAlarmUIInEditMode = alarmUI;
        currentMode = AlarmDialog.MODE.EDIT;
        Alarm alarm = alarmUI.getData();
        startAlarmDialog(alarm);
    }

    public void viewAlarm(UIRenderer alarmUI){
        currentAlarmUIInEditMode = alarmUI;
        currentMode = AlarmDialog.MODE.VIEW;
        Alarm alarm = alarmUI.getData();
        startAlarmDialog(alarm);
    }

    private void startAlarmDialog(Alarm alarm) { // as for edit Alarm, dbStorage is done in the callback method onDialogPositiveClick
        AlarmDialog alarmDialog = new AlarmDialog();
        alarmDialog.setDialogClickListener(this);
        alarmDialog.setMode(currentMode);
        alarmDialog.show(((Activity) getContext()).getFragmentManager(), "Dialog-Alarm");
        if (currentMode != AlarmDialog.MODE.CREATE)
            alarmDialog.setAlarm(alarm);
    }

    public void stopAlarm() { // don't remove from list, but remove from schedule in AlarmManager and stop moving digit.
        // act only on the one selected alarm
        if (!selectedItems.isEmpty()) {
            UIRenderer alarmUI = selectedItems.get(0);
            Alarm alarm = alarmUI.getData();
            AlarmServices.removeFromAlarmManager(getContext(), alarm.getId());
            //removeFromAlarmManager(alarm.getId());
            alarm.stopTime(System.currentTimeMillis());
            alarmUI.addUpdateType(Units.UPDATE_TYPE.PAINT_ALL);
            // and update in db !!
            DbLiveObject.storeAlarm(getContext(), alarm);

        }
    }

    public void startAlarm() {
        if (!selectedItems.isEmpty()) {
            UIRenderer alarmUI = selectedItems.get(0);
            Alarm alarm = alarmUI.getData();
            if (!alarm.isRunning() && !alarm.isPassed()) {
                alarm.restart();
                alarmUI.addUpdateType(Units.UPDATE_TYPE.PAINT_ALL);
                AlarmServices.registerIntoAlarmManager(getContext(), alarm);
                DbLiveObject.storeAlarm(getContext(), alarm);
            } else {
                Toast.makeText(getContext(), R.string.alarm_not_in_past, Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        AlarmDialog alarmDialog = (AlarmDialog) dialog;
        Alarm alarm = alarmDialog.getAlarm();

        if (currentMode == AlarmDialog.MODE.EDIT) {
            // cancel alarm
            AlarmServices.removeFromAlarmManager(getContext(), alarm.getId());
            alarm.restart();
        }
        // update endate for Repeated* Alarm
        if (alarm.getAlarmData().getType() == AlarmData.ALARM_TYPE.REPEATED_LOOP || alarm.getAlarmData().getType() == AlarmData.ALARM_TYPE.REPEATED_LOOP_SPEC_TIME) {
            alarm.updateEndTimeForRepeatedLoop();
            alarm.setFirstEndTime(alarm.getEndTime());
        } else if (alarm.getAlarmData().getType() == AlarmData.ALARM_TYPE.REPEATED) {
            alarm.updateEndTimeForRepeated();
            alarm.setFirstEndTime(alarm.getEndTime());
        }

        if (!alarm.isPassed()) {

            if (currentMode == AlarmDialog.MODE.EDIT)
                alarm.restart();
            else
                alarm.start(System.currentTimeMillis());
        }

        // update in db
        DbLiveObject dbLiveObject = DbLiveObject.storeAlarm(getContext(), alarm);
        if (dbLiveObject.hasError()) {
            Toast.makeText(getContext(), R.string.alarm_into_db_failed, Toast.LENGTH_LONG).show();
            Log.i(Chronos.name + "AlarmListView", dbLiveObject.getErrorMessage().localiszedMessage);
            Log.e(Chronos.name + "AlarmListView", "ZOB", dbLiveObject.getErrorMessage().exception);
        } else {
            if (currentMode == AlarmDialog.MODE.CREATE) {
                addAlarm(alarm);
            } else {
                if (alarmDialog.hasTypeChanged())
                    currentAlarmUIInEditMode.addUpdateType(Units.UPDATE_TYPE.UPDATE_ALARM_TYPE);
                else
                    currentAlarmUIInEditMode.addUpdateType(Units.UPDATE_TYPE.PAINT_ALL);
            }
            // submit to alarm manager
            AlarmServices.registerIntoAlarmManager(getContext(), alarm);
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

}
