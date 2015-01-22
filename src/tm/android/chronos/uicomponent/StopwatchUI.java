/*
 * StopwatchUI
 *
 * Copyright (c) 2014 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */
package tm.android.chronos.uicomponent;

import tm.android.chronos.core.DelayedActionListener;
import tm.android.chronos.core.Units.UPDATE_TYPE;
/**
 * This interface define methods needed to by the UI. It is essentially flags to
 * represent some action.
 */
public interface StopwatchUI extends DelayedActionListener {

    void setExpanded(boolean expanded);

    /**
     * *
     * @return a boolean, true if details are expanded on the ui, false if they are collapsed.
     */
    boolean isExpanded();



    int getDetailsHeight();

    void setDetailsHeight(int height);

    void setHeadHeight(int height);

    int getHeadHeight();

    UPDATE_TYPE[] getUpdateTypes();

    void addUpdateType(UPDATE_TYPE updateType);

    void clearUpdateType();

    void setSelected(boolean select);

    boolean isSelected();



}
