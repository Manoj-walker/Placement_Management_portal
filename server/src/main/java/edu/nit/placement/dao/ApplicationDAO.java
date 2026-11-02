package edu.nit.placement.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** APPLICATIONS table: one row per (student, announcement) with APPLIED or BOOKMARKED. */
@Repository
public class ApplicationDAO extends BaseDAO {

    public ApplicationDAO(JdbcTemplate jdbc) {
        super(jdbc);
    }

    public void setStatus(Long userId, Long announcementId, String status) {
        int updated = jdbc.update(
                "UPDATE APPLICATIONS SET STATUS = ?, APPLIED_AT = IF('APPLIED' = ?, CURRENT_TIMESTAMP, APPLIED_AT) "
                        + "WHERE STUDENT_ID = ? AND ANNOUNCEMENT_ID = ?",
                status, status, userId, announcementId);
        if (updated == 0) {
            jdbc.update("INSERT INTO APPLICATIONS (STUDENT_ID, ANNOUNCEMENT_ID, STATUS) VALUES (?, ?, ?)",
                    userId, announcementId, status);
        }
    }

    public void remove(Long userId, Long announcementId) {
        jdbc.update("DELETE FROM APPLICATIONS WHERE STUDENT_ID = ? AND ANNOUNCEMENT_ID = ?",
                userId, announcementId);
    }

    public Map<Long, String> statusesForStudent(Long userId) {
        Map<Long, String> result = new HashMap<>();
        jdbc.query("SELECT ANNOUNCEMENT_ID, STATUS FROM APPLICATIONS WHERE STUDENT_ID = ?",
                rs -> {
                    result.put(rs.getLong("ANNOUNCEMENT_ID"), rs.getString("STATUS"));
                }, userId);
        return result;
    }

    public List<Long> studentsApplied(Long announcementId) {
        return jdbc.queryForList(
                "SELECT STUDENT_ID FROM APPLICATIONS WHERE ANNOUNCEMENT_ID = ? AND STATUS = 'APPLIED'",
                Long.class, announcementId);
    }

    public boolean hasApplied(Long userId, Long announcementId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM APPLICATIONS WHERE STUDENT_ID = ? AND ANNOUNCEMENT_ID = ? "
                        + "AND STATUS = 'APPLIED'",
                Integer.class, userId, announcementId);
        return count != null && count > 0;
    }

    public long countApplied() {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM APPLICATIONS WHERE STATUS = 'APPLIED'", Long.class);
        return count == null ? 0 : count;
    }
}
