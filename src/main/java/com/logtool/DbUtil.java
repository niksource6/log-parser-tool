package com.logtool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public final class DbUtil {
    private static final String INSERT_SQL = "INSERT INTO log_entries "
            + "(log_time, level, interface_name, request_cost_ms, exception_message) "
            + "VALUES (?, ?, ?, ?, ?)";
    private static final String COUNT_SQL = "SELECT COUNT(*) FROM log_entries "
            + "WHERE level = 'ERROR' AND log_time BETWEEN ? AND ?";

    private DbUtil() { }

    public static Connection openConnection() throws SQLException {
        String url = System.getenv("LOGTOOL_DB_URL");
        String user = System.getenv("LOGTOOL_DB_USER");
        if (url == null || url.isEmpty() || user == null || user.isEmpty()) {
            throw new SQLException("Set LOGTOOL_DB_URL and LOGTOOL_DB_USER before running.");
        }
        Connection connection = DriverManager.getConnection(url, user,
                System.getenv().getOrDefault("LOGTOOL_DB_PASSWORD", ""));
        try {
            connection.setAutoCommit(false);
            return connection;
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
    }

    public static void insertBatch(Connection connection, List<LogEntity> logs) throws SQLException {
        if (logs.isEmpty()) {
            return;
        }
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            for (LogEntity log : logs) {
                statement.setTimestamp(1, Timestamp.valueOf(log.getLogTime()));
                statement.setString(2, log.getLevel());
                statement.setString(3, log.getInterfaceName());
                if (log.getRequestCostMs() == null) {
                    statement.setNull(4, java.sql.Types.BIGINT);
                } else {
                    statement.setLong(4, log.getRequestCostMs());
                }
                statement.setString(5, log.getExceptionMessage());
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw e;
        }
    }

    public static long countErrors(Connection connection, LocalDateTime start,
                                   LocalDateTime end) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(COUNT_SQL)) {
            statement.setTimestamp(1, Timestamp.valueOf(start));
            statement.setTimestamp(2, Timestamp.valueOf(end));
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getLong(1);
            }
        }
    }
}
