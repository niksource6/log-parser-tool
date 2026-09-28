package com.logtool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class LogFileParsingTest {
    @Test
    public void readsInfoAndWarnFile() throws Exception {
        List<LogEntity> logs = readInRange("logs/info-warn.log",
                LocalDateTime.of(2026, 9, 28, 10, 20, 15),
                LocalDateTime.of(2026, 9, 28, 10, 20, 21));
        assertEquals(2, logs.size());
        assertEquals("UserController", logs.get(0).getInterfaceName());
        assertEquals(Long.valueOf(500), logs.get(1).getRequestCostMs());
    }

    @Test
    public void keepsBothTimeBoundaries() throws Exception {
        List<LogEntity> logs = readInRange("logs/errors-range.log",
                LocalDateTime.of(2026, 9, 28, 10, 0),
                LocalDateTime.of(2026, 9, 28, 10, 5));
        assertEquals(2, logs.size());
        assertEquals(LocalDateTime.of(2026, 9, 28, 10, 0), logs.get(0).getLogTime());
        assertEquals(LocalDateTime.of(2026, 9, 28, 10, 5), logs.get(1).getLogTime());
    }

    @Test
    public void skipsMalformedFileLines() throws Exception {
        List<LogEntity> logs = readInRange("logs/invalid.log",
                LocalDateTime.of(2026, 9, 28, 0, 0),
                LocalDateTime.of(2026, 9, 28, 23, 59, 59));
        assertEquals(1, logs.size());
        assertTrue(logs.get(0).getExceptionMessage().contains("database connection"));
    }

    private List<LogEntity> readInRange(String resource, LocalDateTime start, LocalDateTime end)
            throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resource);
        if (stream == null) {
            throw new IOException("Missing test resource: " + resource);
        }
        List<LogEntity> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Optional<LogEntity> parsed = RegexUtil.parse(line);
                if (parsed.isPresent() && !parsed.get().getLogTime().isBefore(start)
                        && !parsed.get().getLogTime().isAfter(end)) {
                    result.add(parsed.get());
                }
            }
        }
        return result;
    }
}
