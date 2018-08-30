package tm.android.chronos.core.ui;

import android.graphics.Canvas;
import android.view.SurfaceHolder;
import tm.android.chronos.core.Units;
import tm.android.chronos.util.geom.Point;

import java.util.List;

public interface UIRenderer {

    void renderOnCanvas(Canvas canvas);

    void renderOnCache();

    Canvas getCachedCanvas();

    void renderOnScreen(Canvas canvas);

    boolean mustUpdateUI();

    List<Units.UPDATE_TYPE> getUpdateTypes();

    void addUpdateType(Units.UPDATE_TYPE updateType);

    void clearUpdateType();

    boolean isSelected();

    void setSelected(boolean select);

    /**
     * If any, return the data that is rendered by the implementer of this interface
     *
     * @param <T> generic Type
     * @return object of the desired type !
     * <code>For instance : <T> T getData(){ return (T) new Dates[]{date1,date2}}\br
     * Somewhere else : <br>
     * Dates[] dates = myRenderer.getData()
     * </code>
     */
    <T> T getData();

    /**
     * Expand state
     *
     * @return boolean, true if details are expanded on the ui, false if they are collapsed.
     */
    boolean isExpanded();

    /**
     * Update expand state
     *
     * @param expanded true or false
     */
    void setExpanded(boolean expanded);


    /**
     * Say if some "background" action are to be performed.
     *
     * @return boolean
     */
    boolean hasNonUIAction();

    /**
     * Caller for some "background" action to be performed, if any.
     */
    void doNonUIAction();


    void setHasNonUIAction(boolean hasNonUIAction);

    /**
     * Say if rendering is needed
     *
     * @return true if rendering is needed, false otherwise
     */
    boolean drawMe();

    /**
     * When implementer extends {@link android.view.SurfaceView}, return it's {@link SurfaceHolder}
     *
     * @return SurfaceHolder
     */
    SurfaceHolder getHolder();

    /**
     * Must return the real height of this GUI when it will be drawn !
     *
     * @return float : real height
     */
    float getComputedHeight();

    /**
     * Return the location (top left corner) of this renderer
     *
     * @return Point
     */
    Point getLocation();

    /**
     * Set "top left corner" to start from for rendering.
     *
     * @param point : top left corner
     */
    void setLocation(Point point);

    /**
     * Say if size changed. Used by {@link RendererWorker} to notify mainGUI when a "child" change it's height.
     *
     * @return boolean
     */
    boolean hasSizeChanged();

    /**
     * When hasSizeChanged is true for some "child" of a mainGUI, {@link RendererWorker}  will be called this method
     * on mainGUI with the first renderer where hasSizeChanged equals to true.
     *
     * @param renderer a {@link UIRenderer}
     */
    void sizeChangedFor(UIRenderer renderer);

    void setSizeChanged();

    void resetSizeChangedStatus();

}
