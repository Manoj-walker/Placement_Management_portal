package edu.nit.placement.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import edu.nit.placement.model.user.AccountStatus;
import edu.nit.placement.model.user.Admin;
import edu.nit.placement.model.user.Coordinator;
import edu.nit.placement.model.user.Role;
import edu.nit.placement.model.user.Student;
import edu.nit.placement.model.user.User;
import edu.nit.placement.dao.UserFactory;

/**
 * USERS + STUDENT_PROFILES tables (split storage, per database/01-schema.sql):
 * shared account fields live in USERS, student-only fields in STUDENT_PROFILES.
 * The read query LEFT JOINs the profile table so role-specific columns keep
 * their model names.
 */
@Repository
public class UserDAO extends BaseDAO {

    private static final String SELECT =
            "SELECT u.ID, u.NAME, u.EMAIL, u.PASSWORD_HASH, u.ROLE, u.STATUS, "
                    + "sp.ROLL_NO, sp.BRANCH, sp.BATCH, sp.CGPA, sp.BACKLOGS, "
                    + "u.DEPARTMENT, u.CREATED_AT FROM USERS u "
                    + "LEFT JOIN STUDENT_PROFILES sp ON sp.USER_ID = u.ID";

    private static final RowMapper<User> USER_MAPPER = (rs, rowNum) -> UserFactory.fromRow(rs);

    public UserDAO(JdbcTemplate jdbc) {
        super(jdbc);
    }

    public Optional<User> findByEmail(String email) {
        List<User> users = jdbc.query(
                SELECT + " WHERE u.EMAIL = ?",
                USER_MAPPER, email == null ? null : email.trim().toLowerCase());
        return users.stream().findFirst();
    }

    public Optional<User> findById(Long id) {
        List<User> users = jdbc.query(
                SELECT + " WHERE u.ID = ?", USER_MAPPER, id);
        return users.stream().findFirst();
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM USERS WHERE EMAIL = ?", Integer.class,
                email == null ? null : email.trim().toLowerCase());
        return count != null && count > 0;
    }

    public Long insert(User user) {
        Long id = insertReturningId(
                "INSERT INTO USERS (NAME, EMAIL, PASSWORD_HASH, ROLE, STATUS, DEPARTMENT) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                user.getName(), user.getEmail(), user.getPasswordHash(),
                user.getRole().name(), user.getStatus().name(),
                user instanceof Coordinator c ? c.getDepartment() : null);
        user.setId(id);
        if (user instanceof Student s) {
            insertProfile(id, s);
        }
        return id;
    }

    private void insertProfile(Long userId, Student s) {
        jdbc.update(
                "INSERT INTO STUDENT_PROFILES (USER_ID, ROLL_NO, BRANCH, BATCH, CGPA, BACKLOGS) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                userId, s.getRollNo(), s.getBranch(), s.getBatch(), s.getCgpa(), s.getBacklogs());
    }

    public void updateProfile(Student student) {
        jdbc.update("UPDATE USERS SET NAME = ? WHERE ID = ?", student.getName(), student.getId());
        jdbc.update(
                "UPDATE STUDENT_PROFILES SET ROLL_NO = ?, BRANCH = ?, BATCH = ?, CGPA = ?, BACKLOGS = ? "
                        + "WHERE USER_ID = ?",
                student.getRollNo(), student.getBranch(), student.getBatch(),
                student.getCgpa(), student.getBacklogs(), student.getId());
    }

    public void updateBasicProfile(User user) {
        jdbc.update("UPDATE USERS SET NAME = ? WHERE ID = ?", user.getName(), user.getId());
        if (user instanceof Coordinator c) {
            jdbc.update("UPDATE USERS SET DEPARTMENT = ? WHERE ID = ?", c.getDepartment(), c.getId());
        }
    }

    public void updateRole(Long id, Role role) {
        jdbc.update("UPDATE USERS SET ROLE = ? WHERE ID = ?", role.name(), id);
    }

    public void updateStatus(Long id, AccountStatus status) {
        jdbc.update("UPDATE USERS SET STATUS = ? WHERE ID = ?", status.name(), id);
    }

    public void updatePassword(Long id, String passwordHash) {
        jdbc.update("UPDATE USERS SET PASSWORD_HASH = ? WHERE ID = ?", passwordHash, id);
    }

    public boolean delete(Long id) {
        // student_profiles (and notifications, applications, …) cascade on user delete
        return jdbc.update("DELETE FROM USERS WHERE ID = ?", id) > 0;
    }

    public List<User> findAll(String roleFilter, String search) {
        StringBuilder sql = new StringBuilder(SELECT + " WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (roleFilter != null && !roleFilter.isBlank()) {
            sql.append(" AND u.ROLE = ?");
            args.add(roleFilter.trim().toUpperCase());
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(u.NAME) LIKE ? OR LOWER(u.EMAIL) LIKE ?)");
            args.add(like(search));
            args.add(like(search));
        }
        sql.append(" ORDER BY u.CREATED_AT DESC, u.ID DESC");
        return jdbc.query(sql.toString(), USER_MAPPER, args.toArray());
    }

    public List<Student> findAllStudents() {
        return jdbc.query(SELECT + " WHERE u.ROLE = 'STUDENT' AND u.STATUS = 'ACTIVE'",
                (rs, rowNum) -> (Student) UserFactory.fromRow(rs));
    }

    public List<Student> findStudentsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", ids.stream().map(x -> "?").toList());
        return jdbc.query(SELECT + " WHERE u.ROLE = 'STUDENT' AND u.ID IN (" + placeholders + ")",
                (rs, rowNum) -> (Student) UserFactory.fromRow(rs), ids.toArray());
    }

    public long countAll() {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM USERS", Long.class);
        return count == null ? 0 : count;
    }

    public long countByRole(Role role) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM USERS WHERE ROLE = ?", Long.class, role.name());
        return count == null ? 0 : count;
    }

    public long countByStatus(AccountStatus status) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM USERS WHERE STATUS = ?", Long.class, status.name());
        return count == null ? 0 : count;
    }

    /** Creates the correct subclass for a role — used by admin user creation. */
    public static User newUserFor(Role role) {
        return switch (role) {
            case STUDENT -> new Student();
            case COORDINATOR -> new Coordinator();
            case ADMIN -> new Admin();
        };
    }
}
