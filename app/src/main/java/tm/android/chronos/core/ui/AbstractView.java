/*
 * AbstractView
 *
 * Copyright (c) 2018 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */
package tm.android.chronos.core.ui;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import tm.android.chronos.core.Units;
import tm.android.chronos.util.geom.Point;

import java.util.Vector;

/**
 * This class implements UIRenderer as a surfaceview and use a cachedCanvas (linked to a bitmap) to render it's view.
 */
public abstract class AbstractView extends SurfaceView implements UIRenderer, SurfaceHolder.Callback {
    private static final Paint paintBackgroundTransparent;

    static {
        paintBackgroundTransparent = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBackgroundTransparent.setColor(Color.TRANSPARENT);
    }

    private final Vector<Units.UPDATE_TYPE> updateTypeList;
    protected Canvas cachedCanvas = null;
    RectF rect;
    Bitmap bufferBitmap;
    Rect viewPort;
    Rect scrollView;
    protected RendererWorker rendererWorker;
    private Point location;
    private boolean sizeChanged = false;
    private boolean hasNonUIAction = false;
    private Paint paintBackground;
    private boolean viewPortHeighSet = false;
    private boolean expanded = false;
    private boolean drawMe = false;

    AbstractView(Context context, Canvas canvas) {
        super(context);

        if (canvas != null)
            cachedCanvas = canvas;

        updateTypeList = new Vector<>(5);
        rect = new RectF();
        getHolder().addCallback(this);


    }

    AbstractView(Context context) {
        this(context, null);
    }


    @Override
    public void renderOnScreen(Canvas canvas) {
        if (canvas == null)
            return;
        canvas.drawRect(viewPort, getBackgroundPaint());
        canvas.drawBitmap(bufferBitmap, scrollView, viewPort, null);
        // Log.i("AbstractView","RenderOnScreen, canvas: " + canvas.getClipBounds().toShortString());
    }


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
        synchronized (updateTypeList) {
            updateTypeList.add(updateType);
        }
        //Log.i("AbstractView","add "+updateType.name() + " From : " + this.getClass().getCanonicalName());
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


    public void setPaintBackgroundColor(int color) {
        paintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBackground.setColor(color);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.i("AbstractVieUI", "surfaceCreated");
        if (cachedCanvas == null) {
            bufferBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            cachedCanvas = new Canvas(bufferBitmap);
            viewPort = new Rect(0, 0, getWidth(), getHeight());
            scrollView = new Rect(0, 0, getWidth(), getHeight());
        }
        drawMe = true;
        innerSurfaceCreated(surfaceHolder);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("AbstractUI", "surfaceChanged");
        if (!viewPortHeighSet) {// set view port height once.
            viewPort.bottom = height;
            scrollView.bottom = height;
            viewPortHeighSet = true;
        }
        innerSurfaceChanged(holder, format, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i("AbstractUI", "surfaceDestroyed");
        innerSurfaceDestroyed(holder);
        drawMe = false;
    }

    @Override
    public boolean drawMe() {
        return drawMe;
    }

    Paint getBackgroundPaint() {
        return (paintBackground == null ? paintBackgroundTransparent : paintBackground);
    }

    public void setRendererWorker(RendererWorker rendererWorker) {
        this.rendererWorker = rendererWorker;
    }


    @Override
    public void resetSizeChangedStatus() {
        sizeChanged = false;
    }

    @Override
    public boolean hasSizeChanged() {
        return sizeChanged;
    }

    @Override
    public Canvas getCachedCanvas() {
        return cachedCanvas;
    }

    @Override
    public Point getLocation() {
        return location;
    }

    @Override
    public void setLocation(Point location) {
        this.location = location;
    }

    protected abstract void innerSurfaceCreated(SurfaceHolder surfaceHolder);

    protected abstract void innerSurfaceChanged(SurfaceHolder holder, int format, int width, int height);

    protected abstract void innerSurfaceDestroyed(SurfaceHolder holder);


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
