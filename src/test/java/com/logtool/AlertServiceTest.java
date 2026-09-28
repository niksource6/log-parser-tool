package com.logtool;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Test;

public class AlertServiceTest {
    @Test
    public void alertsOnlyAboveThreshold() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(bytes, true, "UTF-8")) {
            assertFalse(AlertService.alertIfNeeded(2, 2, output));
            assertEquals("", bytes.toString("UTF-8"));
            assertTrue(AlertService.alertIfNeeded(3, 2, output));
            assertTrue(bytes.toString("UTF-8").contains("ERROR count 3 exceeds threshold 2"));
        }
    }
}
