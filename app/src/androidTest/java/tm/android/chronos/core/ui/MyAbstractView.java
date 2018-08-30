package tm.android.chronos.core.ui;


import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class MyAbstractView extends AbstractView {
    public String action;

    public MyAbstractView(Context context) {
        super(context);
    }

    @Override
    protected void innerSurfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    protected void innerSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    protected void innerSurfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void renderOnCanvas(Canvas canvas) {

    }

    @Override
    public void renderOnCache() {

    }

    @Override
    public float getComputedHeight() {
        return 0;
    }

    @Override
    public boolean hasNonUIAction() {
        return true;
    }

    @Override
    public void doNonUIAction() {
        action = "DONE";
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) "Data";
    }

    @Override
    public void sizeChangedFor(UIRenderer renderer) {

    }
}
