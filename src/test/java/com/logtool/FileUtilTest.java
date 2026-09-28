package com.logtool;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FileUtilTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void readsUtf8OneLineAtATime() throws Exception {
        Path path = folder.newFile("sample.log").toPath();
        Files.write(path, "first\nsecond\n".getBytes(StandardCharsets.UTF_8));
        try (BufferedReader reader = FileUtil.openReader(path)) {
            assertEquals("first", reader.readLine());
            assertEquals("second", reader.readLine());
            assertNull(reader.readLine());
        }
    }

    @Test(expected = NoSuchFileException.class)
    public void reportsMissingFile() throws Exception {
        FileUtil.openReader(folder.getRoot().toPath().resolve("missing.log"));
    }
}
