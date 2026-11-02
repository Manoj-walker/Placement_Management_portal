package edu.nit.placement.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Email audit trail. The old EMAIL_LOG table no longer exists — email attempts
 * are rows in NOTIFICATIONS with CHANNEL = 'EMAIL' (status, error_message and
 * sent_at are columns of that table).
 */
@Repository
public class EmailLogDAO extends BaseDAO {

    public EmailLogDAO(JdbcTemplate jdbc) {
        super(jdbc);
    }

    /** Records the final state of an email attempt (also stamps sent_at). */
    public void finish(Long rowId, String status, String errorMessage) {
        jdbc.update(
                "UPDATE NOTIFICATIONS SET STATUS = ?, ERROR_MESSAGE = ?, "
                        + "SENT_AT = IF('SENT' = ?, CURRENT_TIMESTAMP, SENT_AT) WHERE ID = ?",
                status, errorMessage == null ? null
                        : errorMessage.substring(0, Math.min(errorMessage.length(), 500)),
                status, rowId);
    }

    public long countSent() {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM NOTIFICATIONS WHERE CHANNEL = 'EMAIL' AND STATUS = 'SENT'", Long.class);
        return count == null ? 0 : count;
    }
}
