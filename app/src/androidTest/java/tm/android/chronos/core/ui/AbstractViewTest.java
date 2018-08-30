package tm.android.chronos.core.ui;


import android.content.Context;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import tm.android.chronos.core.Units;

import static org.junit.Assert.*;

@RunWith(android.support.test.runner.AndroidJUnit4.class)
public class AbstractViewTest {
    Context appContext = android.support.test.InstrumentationRegistry.getTargetContext();
    MyAbstractView view = new MyAbstractView(appContext);

    @Test
    public void testgetUpdateTypes() {
        assertNotNull(view.getUpdateTypes());
        assertTrue(view.getUpdateTypes().isEmpty());
    }

    @Test
    public void addUpdateType() {
        view.addUpdateType(Units.UPDATE_TYPE.PAINT_ALL);
        assertTrue(view.getUpdateTypes().size() == 1);
        assertTrue(view.getUpdateTypes().get(0) == Units.UPDATE_TYPE.ADD_NEW);
        view.addUpdateType(Units.UPDATE_TYPE.SELECT);
        assertTrue(view.getUpdateTypes().size() == 2);
        Units.UPDATE_TYPE[] tab = new Units.UPDATE_TYPE[2];
        Assert.assertArrayEquals(view.getUpdateTypes().toArray(tab), new Units.UPDATE_TYPE[]{Units.UPDATE_TYPE.PAINT_ALL, Units.UPDATE_TYPE.SELECT});
    }

    @Test
    public void clearUpdateType() {
        view.clearUpdateType();
        assertTrue(view.getUpdateTypes().isEmpty());
    }


    @Test
    public void Expanded() {
        view.setExpanded(true);
        assertTrue(view.isExpanded());
        view.setExpanded(false);
        assertFalse(view.isExpanded());
    }

    @Test
    public void hasNonUIAction() {
        assertTrue(view.hasNonUIAction());
    }

    @Test
    public void doNonUIAction() {
        view.doNonUIAction();
        Assert.assertEquals(view.action, "DONE");
    }

    @Test
    public void getData() {
        Assert.assertEquals(view.getData(), "Data");
    }
}