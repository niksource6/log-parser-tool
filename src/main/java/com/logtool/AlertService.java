package com.logtool;

import java.io.PrintStream;

public final class AlertService {
    private AlertService() { }

    public static boolean alertIfNeeded(long errorCount, long threshold, PrintStream output) {
        if (errorCount > threshold) {
            output.printf("ALERT: ERROR count %d exceeds threshold %d.%n", errorCount, threshold);
            return true;
        }
        return false;
    }
}
