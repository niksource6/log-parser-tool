package com.logtool;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MainTest {
    @Test
    public void rejectsInvalidArgumentsBeforeOpeningDatabase() {
        assertEquals(1, Main.run(new String[0]));
        assertEquals(1, Main.run(new String[] {"sample.log", "2026-09-29 00:00:00",
                "2026-09-28 00:00:00", "0"}));
        assertEquals(1, Main.run(new String[] {"sample.log", "2026-09-28 00:00:00",
                "2026-09-28 23:59:59", "-1"}));
    }
}
