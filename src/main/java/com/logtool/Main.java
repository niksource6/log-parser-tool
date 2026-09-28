package com.logtool;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Main {
    private static final int BATCH_SIZE = 500;

    private Main() { }

    public static void main(String[] args) {
        if (run(args) != 0) {
            System.exit(1);
        }
    }

    static int run(String[] args) {
        if (args.length != 4) {
            System.err.println("Usage: java -jar log-parser-tool-0.1.0-jar-with-dependencies.jar "
                    + "<file.log> <start: yyyy-MM-dd HH:mm:ss> <end: yyyy-MM-dd HH:mm:ss> <threshold>");
            return 1;
        }

        LocalDateTime start;
        LocalDateTime end;
        long threshold;
        try {
            start = LocalDateTime.parse(args[1], RegexUtil.TIME_FORMAT);
            end = LocalDateTime.parse(args[2], RegexUtil.TIME_FORMAT);
            threshold = Long.parseLong(args[3]);
            if (end.isBefore(start) || threshold < 0) {
                throw new IllegalArgumentException("End must not precede start; threshold must be nonnegative.");
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            System.err.println("Invalid time or threshold: " + e.getMessage());
            return 1;
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return 1;
        }

        long parsed = 0;
        long skipped = 0;
        List<LogEntity> batch = new ArrayList<>(BATCH_SIZE);
        try (BufferedReader reader = FileUtil.openReader(Paths.get(args[0]));
             Connection connection = DbUtil.openConnection()) {
            String line;
            while ((line = reader.readLine()) != null) {
                Optional<LogEntity> log = RegexUtil.parse(line);
                if (log.isPresent()) {
                    if (log.get().getLogTime().isBefore(start) || log.get().getLogTime().isAfter(end)) {
                        skipped++;
                        continue;
                    }
                    batch.add(log.get());
                    parsed++;
                    if (batch.size() == BATCH_SIZE) {
                        DbUtil.insertBatch(connection, batch);
                        batch.clear();
                    }
                } else {
                    skipped++;
                }
            }
            DbUtil.insertBatch(connection, batch);
            long errors = DbUtil.countErrors(connection, start, end);
            System.out.printf("Imported: %d; skipped: %d; ERROR count: %d%n", parsed, skipped, errors);
            AlertService.alertIfNeeded(errors, threshold, System.out);
            return 0;
        } catch (NoSuchFileException e) {
            System.err.println("Log file not found: " + e.getFile());
        } catch (IOException e) {
            System.err.println("Could not read log file: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database operation failed: " + e.getMessage());
        }
        return 1;
    }
}
