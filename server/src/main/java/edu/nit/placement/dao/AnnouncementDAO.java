package edu.nit.placement.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import edu.nit.placement.model.announcement.Announcement;

/**
 * ANNOUNCEMENTS table (database/01-schema.sql layout): ctc_lpa, jd_file_path /
 * jd_original_name instead of the old combined JD columns. SQL aliases keep the
 * Java model unchanged.
 */
@Repository
public class AnnouncementDAO extends BaseDAO {

    private static final String COLUMNS =
            "ID, COMPANY_NAME, ROLE_TITLE, DESCRIPTION AS DETAILS, CTC_LPA AS CTC, LOCATION, LAST_DATE, TEST_DATE, "
                    + "INTERVIEW_DATE, VENUE, TEST_LINK, JD_ORIGINAL_NAME AS JD_FILE_NAME, "
                    + "IS_ARCHIVED, CREATED_BY, CREATED_AT";

    private static final RowMapper<Announcement> MAPPER = (rs, rowNum) -> mapRow(rs);

    private static Announcement mapRow(ResultSet rs) throws SQLException {
        Announcement a = new Announcement();
        a.setId(rs.getLong("ID"));
        a.setCompanyName(rs.getString("COMPANY_NAME"));
        a.setRoleTitle(rs.getString("ROLE_TITLE"));
        a.setDetails(rs.getString("DETAILS"));
        a.setCtc(rs.getBigDecimal("CTC"));
        a.setLocation(rs.getString("LOCATION"));
        a.setLastDate(toLocalDate(rs.getDate("LAST_DATE")));
        a.setTestDate(toLocalDate(rs.getDate("TEST_DATE")));
        a.setInterviewDate(toLocalDate(rs.getDate("INTERVIEW_DATE")));
        a.setVenue(rs.getString("VENUE"));
        a.setTestLink(rs.getString("TEST_LINK"));
        a.setJdFileName(rs.getString("JD_FILE_NAME"));
        a.setJdMime(null); // mime type is not stored in the new schema
        a.setArchived(rs.getInt("IS_ARCHIVED") == 1);
        a.setCreatedBy(rs.getLong("CREATED_BY"));
        return a;
    }

    public AnnouncementDAO(JdbcTemplate jdbc) {
        super(jdbc);
    }

    public Long insert(Announcement a) {
        Long id = insertReturningId(
                "INSERT INTO ANNOUNCEMENTS (COMPANY_NAME, ROLE_TITLE, DESCRIPTION, CTC_LPA, LOCATION, LAST_DATE, "
                        + "TEST_DATE, INTERVIEW_DATE, VENUE, TEST_LINK, IS_ARCHIVED, CREATED_BY) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                a.getCompanyName(), a.getRoleTitle(), a.getDetails(), a.getCtc(), a.getLocation(),
                sqlDate(a.getLastDate()), sqlDate(a.getTestDate()), sqlDate(a.getInterviewDate()),
                a.getVenue(), a.getTestLink(), a.isArchived() ? 1 : 0, a.getCreatedBy());
        a.setId(id);
        saveEligibility(id, a.getEligibility());
        replaceRounds(id, a.getRounds());
        return id;
    }

    public void update(Announcement a) {
        jdbc.update(
                "UPDATE ANNOUNCEMENTS SET COMPANY_NAME = ?, ROLE_TITLE = ?, DESCRIPTION = ?, CTC_LPA = ?, LOCATION = ?, "
                        + "LAST_DATE = ?, TEST_DATE = ?, INTERVIEW_DATE = ?, VENUE = ?, TEST_LINK = ?, "
                        + "IS_ARCHIVED = ? WHERE ID = ?",
                a.getCompanyName(), a.getRoleTitle(), a.getDetails(), a.getCtc(), a.getLocation(),
                sqlDate(a.getLastDate()), sqlDate(a.getTestDate()), sqlDate(a.getInterviewDate()),
                a.getVenue(), a.getTestLink(), a.isArchived() ? 1 : 0, a.getId());
        saveEligibility(a.getId(), a.getEligibility());
        replaceRounds(a.getId(), a.getRounds());
    }

    /** Records an uploaded JD file (bytes are stored on disk by the service layer). */
    public void updateJd(Long id, String originalName, String path) {
        jdbc.update("UPDATE ANNOUNCEMENTS SET JD_FILE_PATH = ?, JD_ORIGINAL_NAME = ? WHERE ID = ?",
                path, originalName, id);
    }

    public Optional<JdFile> findJd(Long id) {
        List<JdFile> files = jdbc.query(
                "SELECT JD_FILE_PATH, JD_ORIGINAL_NAME FROM ANNOUNCEMENTS WHERE ID = ?",
                (rs, rowNum) -> new JdFile(rs.getString("JD_ORIGINAL_NAME"), rs.getString("JD_FILE_PATH")),
                id);
        return files.stream().findFirst();
    }

    public void saveEligibility(Long announcementId, edu.nit.placement.model.announcement.EligibilityCriteria criteria) {
        jdbc.update("DELETE FROM ELIGIBILITY_CRITERIA WHERE ANNOUNCEMENT_ID = ?", announcementId);
        jdbc.update("DELETE FROM ELIGIBILITY_BRANCHES WHERE ANNOUNCEMENT_ID = ?", announcementId);
        if (criteria == null) {
            return;
        }
        jdbc.update("INSERT INTO ELIGIBILITY_CRITERIA (ANNOUNCEMENT_ID, MIN_CGPA, MAX_BACKLOGS, BATCH) "
                + "VALUES (?, ?, ?, ?)",
                announcementId, criteria.getMinCgpa(), criteria.getMaxBacklogs(), criteria.getBatch());
        if (criteria.getBranches() != null) {
            for (String branch : criteria.getBranches()) {
                jdbc.update("INSERT INTO ELIGIBILITY_BRANCHES (ANNOUNCEMENT_ID, BRANCH) VALUES (?, ?)",
                        announcementId, branch);
            }
        }
    }

    public void replaceRounds(Long announcementId, List<edu.nit.placement.model.announcement.Round> rounds) {
        jdbc.update("DELETE FROM ROUNDS WHERE ANNOUNCEMENT_ID = ?", announcementId);
        if (rounds == null) {
            return;
        }
        int seq = 1;
        for (edu.nit.placement.model.announcement.Round round : rounds) {
            jdbc.update("INSERT INTO ROUNDS (ANNOUNCEMENT_ID, ROUND_NO, NAME, TYPE, SCHEDULED_AT) "
                    + "VALUES (?, ?, ?, ?, ?)",
                    announcementId, seq++, round.getName(), roundType(round.getName()),
                    round.getDate() == null ? null : java.sql.Timestamp.valueOf(round.getDate().atStartOfDay()));
        }
    }

    /** The ROUNDS.TYPE enum — inferred from the round name when not tracked by the model. */
    private String roundType(String name) {
        String lower = name == null ? "" : name.toLowerCase();
        if (lower.contains("interview")) {
            return "INTERVIEW";
        }
        if (lower.contains("test") || lower.contains("assessment") || lower.contains("exam")
                || lower.contains("aptitude")) {
            return "TEST";
        }
        return "OTHER";
    }

    public Optional<Announcement> findById(Long id) {
        List<Announcement> list = jdbc.query(
                "SELECT " + COLUMNS + " FROM ANNOUNCEMENTS WHERE ID = ?", MAPPER, id);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(loadRelations(list.get(0)));
    }

    public List<Announcement> search(String query, boolean includeArchived) {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM ANNOUNCEMENTS WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (!includeArchived) {
            sql.append(" AND IS_ARCHIVED = 0");
        }
        if (query != null && !query.isBlank()) {
            sql.append(" AND (LOWER(COMPANY_NAME) LIKE ? OR LOWER(ROLE_TITLE) LIKE ? OR LOWER(LOCATION) LIKE ?)");
            args.add(like(query));
            args.add(like(query));
            args.add(like(query));
        }
        sql.append(" ORDER BY LAST_DATE ASC, ID DESC");
        List<Announcement> list = jdbc.query(sql.toString(), MAPPER, args.toArray());
        list.forEach(this::loadRelations);
        return list;
    }

    /**
     * Announcements for the student dashboard, with the student's own
     * application/bookmark status attached.
     */
    public List<Announcement> listForStudent(Long studentId, String query) {
        StringBuilder sql = new StringBuilder(
                "SELECT a.ID, a.COMPANY_NAME, a.ROLE_TITLE, a.DESCRIPTION AS DETAILS, a.CTC_LPA AS CTC, a.LOCATION, "
                        + "a.LAST_DATE, a.TEST_DATE, a.INTERVIEW_DATE, a.VENUE, a.TEST_LINK, "
                        + "a.JD_ORIGINAL_NAME AS JD_FILE_NAME, a.IS_ARCHIVED, a.CREATED_BY, a.CREATED_AT, "
                        + "ap.STATUS AS APP_STATUS, "
                        + "(SELECT COUNT(*) FROM RESULTS r WHERE r.ANNOUNCEMENT_ID = a.ID) AS RESULT_COUNT "
                        + "FROM ANNOUNCEMENTS a "
                        + "LEFT JOIN APPLICATIONS ap ON ap.ANNOUNCEMENT_ID = a.ID AND ap.STUDENT_ID = ? "
                        + "WHERE a.IS_ARCHIVED = 0");
        List<Object> args = new ArrayList<>();
        args.add(studentId);
        if (query != null && !query.isBlank()) {
            sql.append(" AND (LOWER(a.COMPANY_NAME) LIKE ? OR LOWER(a.ROLE_TITLE) LIKE ?)");
            args.add(like(query));
            args.add(like(query));
        }
        sql.append(" ORDER BY a.LAST_DATE ASC, a.ID DESC");
        List<Announcement> list = jdbc.query(sql.toString(), (rs, rowNum) -> {
            Announcement a = mapRow(rs);
            String appStatus = rs.getString("APP_STATUS");
            a.setApplied("APPLIED".equalsIgnoreCase(appStatus));
            a.setBookmarked("BOOKMARKED".equalsIgnoreCase(appStatus));
            long resultCount = rs.getLong("RESULT_COUNT");
            a.setResultPosted(resultCount > 0);
            return a;
        }, args.toArray());
        list.forEach(this::loadRelations);
        return list;
    }

    public List<Announcement> findByDeadline(LocalDate date) {
        List<Announcement> list = jdbc.query(
                "SELECT " + COLUMNS + " FROM ANNOUNCEMENTS WHERE IS_ARCHIVED = 0 AND LAST_DATE = ?",
                MAPPER, sqlDate(date));
        list.forEach(this::loadRelations);
        return list;
    }

    private Announcement loadRelations(Announcement a) {
        List<edu.nit.placement.model.announcement.EligibilityCriteria> criteria = jdbc.query(
                "SELECT MIN_CGPA, MAX_BACKLOGS, BATCH FROM ELIGIBILITY_CRITERIA WHERE ANNOUNCEMENT_ID = ?",
                (rs, rowNum) -> {
                    edu.nit.placement.model.announcement.EligibilityCriteria ec = new edu.nit.placement.model.announcement.EligibilityCriteria();
                    ec.setMinCgpa(rs.getBigDecimal("MIN_CGPA"));
                    ec.setMaxBacklogs(rs.getInt("MAX_BACKLOGS"));
                    ec.setBatch(rs.getString("BATCH"));
                    return ec;
                }, a.getId());
        edu.nit.placement.model.announcement.EligibilityCriteria ec =
                criteria.isEmpty() ? new edu.nit.placement.model.announcement.EligibilityCriteria() : criteria.get(0);
        List<String> branches = jdbc.queryForList(
                "SELECT BRANCH FROM ELIGIBILITY_BRANCHES WHERE ANNOUNCEMENT_ID = ?", String.class, a.getId());
        ec.setBranches(new java.util.LinkedHashSet<>(branches));
        a.setEligibility(ec);

        List<edu.nit.placement.model.announcement.Round> rounds = jdbc.query(
                "SELECT ID, NAME, ROUND_NO, SCHEDULED_AT FROM ROUNDS WHERE ANNOUNCEMENT_ID = ? "
                        + "ORDER BY ROUND_NO, ID",
                (rs, rowNum) -> {
                    edu.nit.placement.model.announcement.Round r = new edu.nit.placement.model.announcement.Round();
                    r.setId(rs.getLong("ID"));
                    r.setName(rs.getString("NAME"));
                    r.setSequence(rs.getInt("ROUND_NO"));
                    java.sql.Timestamp scheduled = rs.getTimestamp("SCHEDULED_AT");
                    r.setDate(scheduled == null ? null : scheduled.toLocalDateTime().toLocalDate());
                    return r;
                }, a.getId());
        a.getRounds().clear();
        a.getRounds().addAll(rounds);
        return a;
    }

    public void setArchived(Long id, boolean archived) {
        jdbc.update("UPDATE ANNOUNCEMENTS SET IS_ARCHIVED = ? WHERE ID = ?", archived ? 1 : 0, id);
    }

    public boolean delete(Long id) {
        return jdbc.update("DELETE FROM ANNOUNCEMENTS WHERE ID = ?", id) > 0;
    }

    public long countActive() {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM ANNOUNCEMENTS WHERE IS_ARCHIVED = 0", Long.class);
        return count == null ? 0 : count;
    }

    public long countAll() {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM ANNOUNCEMENTS", Long.class);
        return count == null ? 0 : count;
    }

    /** Holder for an uploaded JD file (stored on disk, path recorded in JD_FILE_PATH). */
    public record JdFile(String fileName, String path) {
    }
}
