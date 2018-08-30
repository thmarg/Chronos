package tm.android.chronos.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReminderTest {

    @Test
    public void getDate() {
        long date = 56231010101L;
        Reminder reminder = new Reminder(date,null);
        assertEquals(reminder.getDate(),date);
    }
}