package com.logtool;

import java.time.LocalDateTime;

public final class LogEntity {
    private final LocalDateTime logTime;
    private final String level;
    private final String interfaceName;
    private final Long requestCostMs;
    private final String exceptionMessage;

    public LogEntity(LocalDateTime logTime, String level, String interfaceName,
                     Long requestCostMs, String exceptionMessage) {
        this.logTime = logTime;
        this.level = level;
        this.interfaceName = interfaceName;
        this.requestCostMs = requestCostMs;
        this.exceptionMessage = exceptionMessage;
    }

    public LocalDateTime getLogTime() { return logTime; }
    public String getLevel() { return level; }
    public String getInterfaceName() { return interfaceName; }
    public Long getRequestCostMs() { return requestCostMs; }
    public String getExceptionMessage() { return exceptionMessage; }
}
