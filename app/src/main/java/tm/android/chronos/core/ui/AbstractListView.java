package tm.android.chronos.core.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.widget.Toast;
import tm.android.chronos.core.Units;
import tm.android.chronos.uicomponent.BaseUI;
import tm.android.chronos.util.geom.Point;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import static tm.android.chronos.core.ui.AbstractListView.SELECT_MODE.SINGLE_SELECT;


/**
 * A list view rendered on a single surface (canvas).
 * This class provide the surface and the following functionality :
 * Add/remove/update an element.
 * Select/deselect
 * Scroll
 * Added elements to the list draw them self on the provided canvas starting at the given position.
 */
public abstract class AbstractListView extends AbstractView {
    protected final Vector<UIRenderer> selectedItems;
    public final Vector<UIRenderer> items;
    int scrollOffset = 0; // the position from the top (0) of rectangle send to the screen.
    int lastY = 0; // vertical position of the first available line after the last item;
    private SELECT_MODE selectionMode = SINGLE_SELECT;
    public AbstractListView(Context context) {
        super(context, null);
        selectedItems =  new Vector<>(5);
        items = new Vector<>(10);
    }

    @Override
    public void renderOnCanvas(Canvas canvas) {
    }

    protected void add(UIRenderer item) {
        if (updateCachedCanvasLength((int) item.getComputedHeight())) {
            if (!items.isEmpty()) {
                UIRenderer last = items.lastElement();
                lastY = (int) last.getLocation().add(0, last.getComputedHeight()).getY();
            }
            item.setLocation(new Point(0, lastY, Point.TYPE.XY));
            addUpdateType(Units.UPDATE_TYPE.ADD_NEW);
            item.addUpdateType(Units.UPDATE_TYPE.PAINT_ALL);
            items.add(item);
            lastY += item.getComputedHeight();
            rendererWorker.register(item);
        }
    }


    protected void remove(List<UIRenderer> itemsToRemove) {
        if (itemsToRemove != null && itemsToRemove.size() > 0) {

            int loss = 0;
            // remove each selected from the renderer and compute the height lost.
            for (UIRenderer item : itemsToRemove) {
                loss += item.getComputedHeight();
                rendererWorker.remove(item);
                items.remove(item);
                selectedItems.remove(item);
            }
            itemsToRemove.clear();
            // possible optimisation : find the position of the first removed item and start rebuild from this position

            // update the cached canvas
            lastY -= loss;
            if (lastY > BaseUI.SCREENHEIGHT && bufferBitmap.getHeight() > lastY)
                changeBufferBitmapHeight(lastY + 20); // bufferBitmap.getHeight() - loss


            updateAll();
        }
    }


//    public void remove() {
//        if (items.size() > 0) {
//            if (selectedItems.isEmpty())  // remove only the last element if none is selected.
//                selectedItems.add(items.lastElement());
//
//            int loss = 0;
//            // remove each selected from the renderer and compute the height lost.
//            for (UIRenderer item : selectedItems) {
//                loss += item.getComputedHeight();
//                rendererWorker.getRenderableList().remove(item);
//                items.remove(item);
//            }
//            selectedItems.clear();
//            // possible optimisation : find the position of the first removed item and start rebuild from this position
//
//            // update the cached canvas
//            lastY -= loss;
//            if (lastY > BaseUI.SCREENHEIGHT && bufferBitmap.getHeight() > lastY)
//                changeBufferBitmapHeight(lastY + 20); // bufferBitmap.getHeight() - loss
//
//
//            cachedCanvas.drawRect(0, 0, cachedCanvas.getWidth(), cachedCanvas.getHeight(), getBackgroundPaint());
//            lastY = 0;
//            for (UIRenderer item : items) { // repaint all remaining item
//                item.setLocation(new Point(0, lastY, Point.ALARM_TYPE.XY));
//                item.addUpdateType(Units.UPDATE_CHRONO_TYPE.PAINT_ALL);
//                lastY += item.getComputedHeight();
//            }
//            if (items.isEmpty())
//                addUpdateType(Units.UPDATE_CHRONO_TYPE.PAINT_ALL); // juste to render the background
//        }
//    }

    public void select(UIRenderer item) {
        if (item != null) {
            item.setSelected(!item.isSelected());
            if (selectionMode == SINGLE_SELECT) {
                if (!selectedItems.isEmpty() && selectedItems.get(0) != item) {
                    selectedItems.get(0).setSelected(false);
                    selectedItems.get(0).addUpdateType(Units.UPDATE_TYPE.DESELECT);
                }
                selectedItems.clear();
            }


            if (item.isSelected()) {
                selectedItems.add(item);
                item.addUpdateType(Units.UPDATE_TYPE.SELECT);
            } else {
                selectedItems.remove(item);
                item.addUpdateType(Units.UPDATE_TYPE.DESELECT);
            }
        }
    }

    private boolean updateCachedCanvasLength(int height) {
        if (lastY + height > bufferBitmap.getHeight()) {
            // no, we must expand
            if (changeBufferBitmapHeight(bufferBitmap.getHeight() + 3 * height))
                return true;
            else if (changeBufferBitmapHeight(bufferBitmap.getHeight() + 2 * height))
                return true;// retry !
            else if (changeBufferBitmapHeight(bufferBitmap.getHeight() + height))
                return true; // return if failed
            else {
                Toast.makeText(getContext(), ("Can't allocate more memory to add elements on this list."), Toast.LENGTH_LONG).show();
                return false;
            }

        } else return true;
    }

    private boolean changeBufferBitmapHeight(int newHeight) {
        // if newHeight > bufferBitmap Height ----> expand to newHeight
        // if newHeight < bufferBitmap Height ----> collapse to newHeight
        try {

            Bitmap b = Bitmap.createBitmap(bufferBitmap.getWidth(), newHeight, Bitmap.Config.ARGB_8888);
            Canvas dest = new Canvas(b);
            Rect view = new Rect();
            if (newHeight > bufferBitmap.getHeight()) {
                view.set(0, 0, bufferBitmap.getWidth(), bufferBitmap.getHeight());
            } else {
                view.set(0, 0, bufferBitmap.getWidth(), newHeight);
            }

            dest.drawBitmap(bufferBitmap, view, view, null);
            bufferBitmap = null;
            bufferBitmap = b;
            cachedCanvas = new Canvas(bufferBitmap);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    UIRenderer getItemAt(float y) {
        // y is on screen, we must translate it into the cachedCanvas coordinate
        float yCached = y + scrollOffset;
        if (yCached > lastY)
            return null;
        float pos = 0;

        for (UIRenderer item : items) {
            pos += item.getComputedHeight();
            if (yCached < pos)
                return item;
        }
        return null;
    }

    public void setSelectionMode(SELECT_MODE mode) {
        selectionMode = mode;
    }

    @Override
    public void sizeChangedFor(UIRenderer renderer) {
        if (checkSize()) {
            Iterator<UIRenderer> iter = items.iterator();
            UIRenderer next;
            //noinspection StatementWithEmptyBody
            while (iter.hasNext() && iter.next() != renderer){}
            Point startLocation = renderer.getLocation().add(0, renderer.getComputedHeight());
            // remove ("paint black") all from this position.
            rect.set(0, (int) startLocation.getY(), bufferBitmap.getWidth(), bufferBitmap.getHeight());
            cachedCanvas.drawRect(rect, getBackgroundPaint());
            while (iter.hasNext()) {
                next = iter.next();
                next.setLocation(startLocation);
                next.addUpdateType(Units.UPDATE_TYPE.PAINT_ALL);
                startLocation = startLocation.add(0, next.getComputedHeight());
            }
            lastY = (int) startLocation.getY();
        }
    }

    private boolean checkSize() {
        Point startLocation = new Point();
        for (UIRenderer next : items)
            startLocation = startLocation.add(0, next.getComputedHeight());

        return updateCachedCanvasLength((int) startLocation.getY() - lastY);
    }

    public enum SELECT_MODE {SINGLE_SELECT, MULTI_SELECT}

    protected void updateAll() {
        cachedCanvas.drawRect(0, 0, cachedCanvas.getWidth(), cachedCanvas.getHeight(), getBackgroundPaint());
        lastY = 0;
        for (UIRenderer item : items) { // repaint all remaining item
            item.setLocation(new Point(0, lastY, Point.TYPE.XY));
            item.addUpdateType(Units.UPDATE_TYPE.PAINT_ALL);
            lastY += item.getComputedHeight();
        }
        if (items.isEmpty())
            addUpdateType(Units.UPDATE_TYPE.PAINT_ALL);

    }
}
