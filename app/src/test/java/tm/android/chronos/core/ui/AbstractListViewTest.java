package tm.android.chronos.core.ui;

import android.content.Context;
import android.view.SurfaceHolder;
import org.junit.Test;

public class AbstractListViewTest {
    @Test
    public void add() {
    }

    @Test
    public void remove() {
    }

    @Test
    public void select() {
    }

    class ListView extends AbstractListView {
        public ListView(Context context) {
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
        public boolean hasNonUIAction() {
            return false;
        }

        @Override
        public void doNonUIAction() {

        }


        @Override
        public void renderOnCache() {

        }

        @Override
        public float getComputedHeight() {
            return 0;
        }

        @Override
        public <T> T getData() {
            return null;
        }
    }
}