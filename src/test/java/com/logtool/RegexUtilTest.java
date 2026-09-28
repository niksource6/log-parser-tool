package com.logtool;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import org.junit.Test;

public class RegexUtilTest {
    @Test
    public void parsesInfoWithCost() {
        LogEntity log = RegexUtil.parse("2026-09-28 10:20:15 INFO  UserController - query user api, cost:23ms").get();
        assertEquals(LocalDateTime.of(2026, 9, 28, 10, 20, 15), log.getLogTime());
        assertEquals("INFO", log.getLevel());
        assertEquals("UserController", log.getInterfaceName());
        assertEquals(Long.valueOf(23), log.getRequestCostMs());
        assertNull(log.getExceptionMessage());
    }

    @Test
    public void parsesErrorMessageWithoutCost() {
        LogEntity log = RegexUtil.parse("2026-09-28 10:20:20 ERROR OrderController - database connection timeout").get();
        assertNull(log.getRequestCostMs());
        assertEquals("database connection timeout", log.getExceptionMessage());
    }

    @Test
    public void parsesWarnWithOptionalCost() {
        LogEntity log = RegexUtil.parse("2026-09-28 10:20:21 WARN UserController - slow query, cost:500ms").get();
        assertEquals("WARN", log.getLevel());
        assertEquals(Long.valueOf(500), log.getRequestCostMs());
        assertNull(log.getExceptionMessage());
    }

    @Test
    public void skipsInvalidLines() {
        assertFalse(RegexUtil.parse("2026-02-30 10:20:20 ERROR OrderController - invalid date").isPresent());
        assertFalse(RegexUtil.parse("not a log entry").isPresent());
        assertFalse(RegexUtil.parse("2026-09-28 10:20:20 DEBUG OrderController - debug").isPresent());
        assertFalse(RegexUtil.parse("2026-09-28 10:20:20 INFO Api - cost:999999999999999999999ms").isPresent());
    }
}
