package edu.nit.placement.dao;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * RESULTS table (database/01-schema.sql layout): student_id + result columns,
 * optionally linked to a ROUNDS row. Aliased to the model's names.
 */
@Repository
public class ResultDAO extends BaseDAO {

    public ResultDAO(JdbcTemplate jdbc) {
        super(jdbc);
    }

    /** Replaces every result of the given type for an announcement. */
    public void replace(Long announcementId, String resultType, List<Long> userIds) {
        jdbc.update("DELETE FROM RESULTS WHERE ANNOUNCEMENT_ID = ? AND RESULT = ?",
                announcementId, resultType);
        if (userIds == null) {
            return;
        }
        for (Long userId : userIds) {
            jdbc.update(
                    "INSERT INTO RESULTS (ANNOUNCEMENT_ID, STUDENT_ID, ROUND_ID, RESULT) VALUES (?, ?, NULL, ?)",
                    announcementId, userId, resultType);
        }
    }

    public List<Long> userIds(Long announcementId, String resultType) {
        return jdbc.queryForList(
                "SELECT STUDENT_ID FROM RESULTS WHERE ANNOUNCEMENT_ID = ? AND RESULT = ?",
                Long.class, announcementId, resultType);
    }

    public List<ResultEntry> findByAnnouncement(Long announcementId) {
        return jdbc.query(
                "SELECT STUDENT_ID AS USER_ID, RESULT AS RESULT_TYPE FROM RESULTS "
                        + "WHERE ANNOUNCEMENT_ID = ? ORDER BY RESULT, STUDENT_ID",
                (rs, rowNum) -> new ResultEntry(rs.getLong("USER_ID"), rs.getString("RESULT_TYPE")),
                announcementId);
    }

    public record ResultEntry(Long userId, String resultType) {
    }
}
