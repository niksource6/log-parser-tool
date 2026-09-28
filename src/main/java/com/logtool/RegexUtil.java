package com.logtool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegexUtil {
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss").withResolverStyle(ResolverStyle.STRICT);

    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\s+(INFO|WARN|ERROR)\\s+(\\S+)\\s+-\\s+(.*)$");
    private static final Pattern COST_PATTERN = Pattern.compile("\\bcost\\s*:\\s*(\\d+)\\s*ms\\b",
            Pattern.CASE_INSENSITIVE);

    private RegexUtil() { }

    public static Optional<LogEntity> parse(String line) {
        Matcher match = LOG_PATTERN.matcher(line);
        if (!match.matches()) {
            return Optional.empty();
        }
        try {
            LocalDateTime time = LocalDateTime.parse(match.group(1), TIME_FORMAT);
            String message = match.group(4);
            Matcher costMatch = COST_PATTERN.matcher(message);
            Long cost = costMatch.find() ? Long.valueOf(costMatch.group(1)) : null;
            String exception = "ERROR".equals(match.group(2)) ? message : null;
            return Optional.of(new LogEntity(time, match.group(2), match.group(3), cost, exception));
        } catch (DateTimeParseException | NumberFormatException e) {
            return Optional.empty();
        }
    }
}
