package edu.nit.placement.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import edu.nit.placement.model.notification.InAppNotification;

/**
 * NOTIFICATIONS table (database/01-schema.sql layout). Both the in-app inbox
 * and outgoing email attempts live here: channel='IN_APP' rows are the inbox,
 * channel='EMAIL' rows are the email audit trail (there is no EMAIL_LOG table).
 */
@Repository
public class NotificationDAO extends BaseDAO {

    public NotificationDAO(JdbcTemplate jdbc) {
        super(jdbc);
    }

    /** Inserts one in-app inbox row; returns the generated id. */
    public Long insert(InAppNotification notification) {
        Long id = insertReturningId(
                "INSERT INTO NOTIFICATIONS (USER_ID, ANNOUNCEMENT_ID, CHANNEL, TYPE, TITLE, MESSAGE, "
                        + "IS_READ, STATUS, SENT_AT, DEDUPE_KEY) "
                        + "VALUES (?, NULL, 'IN_APP', ?, ?, ?, 0, 'SENT', CURRENT_TIMESTAMP, ?)",
                notification.getUserId(), storedType(notification.getType()),
                truncate(notification.getMessage(), 200), truncate(notification.getMessage(), 1000),
                "inapp:" + UUID.randomUUID());
        notification.setId(id);
        return id;
    }

    /**
     * Records the intent to send an email (channel='EMAIL', status PENDING) and
     * returns the row id so the service layer can mark the outcome.
     */
    public Long insertEmailAttempt(Long userId, String emailAddress, String subject, String type, String message) {
        return insertReturningId(
                "INSERT INTO NOTIFICATIONS (USER_ID, ANNOUNCEMENT_ID, CHANNEL, TYPE, TITLE, MESSAGE, "
                        + "IS_READ, STATUS, DEDUPE_KEY) "
                        + "VALUES (?, NULL, 'EMAIL', ?, ?, ?, 0, 'PENDING', ?)",
                userId, storedType(type), truncate(subject, 200), truncate(message, 1000),
                "email:" + UUID.randomUUID());
    }

    /** The NOTIFICATIONS.TYPE enum — values outside it map onto UPDATE. */
    private String storedType(String type) {
        return switch (type == null ? "" : type) {
            case "ANNOUNCEMENT", "REMINDER", "RESULT", "APPLICATION", "ACCOUNT" -> type;
            default -> "UPDATE";
        };
    }

    private String truncate(String value, int limit) {
        if (value == null || value.length() <= limit) {
            return value;
        }
        return value.substring(0, limit);
    }

    public List<InAppNotification> listByUser(Long userId) {
        return jdbc.query(
                "SELECT ID, USER_ID, MESSAGE, TYPE, IS_READ, CREATED_AT FROM NOTIFICATIONS "
                        + "WHERE USER_ID = ? AND CHANNEL = 'IN_APP' ORDER BY CREATED_AT DESC, ID DESC",
                (rs, rowNum) -> {
                    InAppNotification n = new InAppNotification();
                    n.setId(rs.getLong("ID"));
                    n.setUserId(rs.getLong("USER_ID"));
                    n.setMessage(rs.getString("MESSAGE"));
                    n.setType(rs.getString("TYPE"));
                    n.setRead(rs.getInt("IS_READ") == 1);
                    java.sql.Timestamp ts = rs.getTimestamp("CREATED_AT");
                    n.setCreatedAt(ts == null ? null : ts.toLocalDateTime());
                    return n;
                }, userId);
    }

    public void markAllRead(Long userId) {
        jdbc.update("UPDATE NOTIFICATIONS SET IS_READ = 1 WHERE USER_ID = ? AND IS_READ = 0 "
                + "AND CHANNEL = 'IN_APP'", userId);
    }

    public void setRead(Long userId, Long id, boolean read) {
        jdbc.update("UPDATE NOTIFICATIONS SET IS_READ = ? WHERE ID = ? AND USER_ID = ?",
                read ? 1 : 0, id, userId);
    }

    public long unreadCount(Long userId) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM NOTIFICATIONS WHERE USER_ID = ? AND IS_READ = 0 AND CHANNEL = 'IN_APP'",
                Long.class, userId);
        return count == null ? 0 : count;
    }

    public long countUnreadAll() {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM NOTIFICATIONS WHERE IS_READ = 0 AND CHANNEL = 'IN_APP'", Long.class);
        return count == null ? 0 : count;
    }

    public boolean existsForUser(Long userId, String message) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM NOTIFICATIONS WHERE USER_ID = ? AND MESSAGE = ? AND CHANNEL = 'IN_APP'",
                Integer.class, userId, message);
        return count != null && count > 0;
    }

    public void deleteAllForUser(Long userId) {
        jdbc.update("DELETE FROM NOTIFICATIONS WHERE USER_ID = ?", userId);
    }
}
