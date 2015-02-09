/*
 * StopwatchUIImpl
 *
 * Copyright (c) 2014 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 *
 */
package tm.android.chronos.uicomponent;

import tm.android.chronos.core.Units.UPDATE_TYPE;

import java.io.Serializable;
import java.util.Vector;


/**
 * StopwatchUI implementation @see{StopwatchUI}
 */
public class StopwatchUIImpl implements StopwatchUI, Serializable{

	private boolean expanded;
	private int detailsHeight;
	private int headHeight;
	private boolean selected;
	private   Vector<UPDATE_TYPE> updateTypeList;

	public StopwatchUIImpl() {
		headHeight = BaseChronographe.fullHeight;
		updateTypeList = new Vector<>(5);
		expanded = false;
	}


	public boolean mustUpdateUI() {
		return updateTypeList.size() > 0;
	}


	@Override
	public boolean isExpanded() {
		return expanded;
	}

	@Override
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	@Override
	public int getDetailsHeight() {
		return detailsHeight;
	}

	@Override
	public void setDetailsHeight(int height) {
		detailsHeight = height;
	}

	@Override
	public int getHeadHeight() {
		return headHeight;
	}

	@Override
	public void setHeadHeight(int height) {
		headHeight = height;
	}

	@Override
	public UPDATE_TYPE[] getUpdateTypes() {
		if (updateTypeList.size() == 0)
			return null;
		return updateTypeList.toArray(new UPDATE_TYPE[]{null});
	}

	@Override
	public void addUpdateType(UPDATE_TYPE updateType) {
		if (updateTypeList.contains(updateType))
			return;
		updateTypeList.add(updateType);
	}

	@Override
	public void clearUpdateType() {
		updateTypeList.removeAllElements();
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void setSelected(boolean select) {
		selected = select;
	}

	@Override
	public void onDelayedAction(Object... obj) {
		synchronized (this) {
			addUpdateType(selected?UPDATE_TYPE.DESELECT:UPDATE_TYPE.SELECT);
			StopwatchUI stopwatchUI = (StopwatchUI) obj[0];
			if (stopwatchUI != null && stopwatchUI != this)
				stopwatchUI.addUpdateType(UPDATE_TYPE.DESELECT);
		}
	}
}
