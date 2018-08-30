package tm.android.chronos.core.ui;


import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.SurfaceHolder;
import tm.android.chronos.core.Units;
import tm.android.chronos.util.geom.Point;

import java.util.Vector;

/**
 * This class implement UIRenderer, providing support for expand, select and updateType.
 * Extends this class to provide a gui component that will render (draw) itself on a provided canvas.
 * Implement only the method Point renderOnCanvas(Canvas,Point)
 * Living component will have to be register into a RendererWorker witch as a mainUI .
 */
public abstract class AbstractUI implements UIRenderer {
    protected final Vector<Units.UPDATE_TYPE> updateTypeList;
    protected Canvas cachedCanvas = null;
    protected RectF rect;
    protected Point location;
    protected boolean sizeChanged = false;
    private boolean hasNonUIAction = false;
    private boolean expanded = false;
    private boolean selected = false;


    protected AbstractUI(Canvas canvas) {
        if (canvas != null)
            cachedCanvas = canvas;

        updateTypeList = new Vector<>(5);
        rect = new RectF();


    }

//    protected AbstractUI() {
//        updateTypeList = new Vector<>(5);
//    }

    public abstract void renderOnScreen(Canvas canvas);


    @Override
    public boolean mustUpdateUI() {
        synchronized (updateTypeList) {
            return updateTypeList.size() > 0;
        }
    }

    @Override
    public synchronized Vector<Units.UPDATE_TYPE> getUpdateTypes() {
        return updateTypeList;
    }

    @Override
    public void addUpdateType(Units.UPDATE_TYPE updateType) {
        synchronized (updateTypeList) {updateTypeList.add(updateType);}
    }

    @Override
    public void clearUpdateType() {
        synchronized (updateTypeList) {updateTypeList.clear();}
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
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(boolean select) {
        selected = select;
    }

    @Override
    public void resetSizeChangedStatus() {
        sizeChanged = false;
    }

    @Override
    public boolean hasSizeChanged() {
        return sizeChanged;
    }

    public abstract boolean drawMe();


    @Override
    public SurfaceHolder getHolder() {
        return null;
    }

    @Override
    public Point getLocation() {
        return location;
    }

    @Override
    public void setLocation(Point point) {
        location = point;
    }

    @Override
    public Canvas getCachedCanvas() {
        return null;
    }

    @Override
    public void setSizeChanged() {
        sizeChanged = true;
    }

    @Override
    public boolean hasNonUIAction() {
        return hasNonUIAction;
    }

    @Override
    public void setHasNonUIAction(boolean hasNonUIAction) {
        this.hasNonUIAction = hasNonUIAction;
    }
}
