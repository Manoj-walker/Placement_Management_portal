package edu.nit.placement.dao;

import edu.nit.placement.model.user.AccountStatus;
import edu.nit.placement.model.user.Admin;
import edu.nit.placement.model.user.Coordinator;
import edu.nit.placement.model.user.Role;
import edu.nit.placement.model.user.Student;
import edu.nit.placement.model.user.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Maps one USERS row to the correct User subclass (single-table storage:
 * student and coordinator columns live side by side with the shared ones).
 */
public final class UserFactory {

    private UserFactory() {
    }

    public static User fromRow(ResultSet rs) throws SQLException {
        String role = rs.getString("ROLE");
        User user = switch (role) {
            case "STUDENT" -> new Student();
            case "COORDINATOR" -> new Coordinator();
            case "ADMIN" -> new Admin();
            default -> throw new IllegalStateException("Unknown role " + role);
        };
        user.setId(rs.getLong("ID"));
        user.setName(rs.getString("NAME"));
        user.setEmail(rs.getString("EMAIL"));
        user.setPasswordHash(rs.getString("PASSWORD_HASH"));
        String status = rs.getString("STATUS");
        user.setStatus(AccountStatus.from(status == null ? "ACTIVE" : status));
        java.sql.Timestamp createdAt = rs.getTimestamp("CREATED_AT");
        user.setCreatedAt(createdAt == null ? null : LocalDateTime.ofInstant(
                createdAt.toInstant(), java.time.ZoneId.systemDefault()));
        if (user instanceof Student student) {
            student.setRollNo(rs.getString("ROLL_NO"));
            student.setBranch(rs.getString("BRANCH"));
            student.setBatch(rs.getString("BATCH"));
            student.setCgpa(rs.getBigDecimal("CGPA"));
            student.setBacklogs(rs.getInt("BACKLOGS"));
        }
        if (user instanceof Coordinator coordinator) {
            coordinator.setDepartment(rs.getString("DEPARTMENT"));
        }
        return user;
    }
}
