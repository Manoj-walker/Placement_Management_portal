package edu.nit.placement.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * Shared JDBC plumbing for all DAOs. Every SQL statement in this project is
 * hand-written; Spring is only used to manage connections and bind parameters.
 */
public abstract class BaseDAO {

    protected final JdbcTemplate jdbc;

    protected BaseDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Runs an INSERT and returns the generated ID column. */
    protected Long insertReturningId(String sql, Object... args) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    protected static LocalDate toLocalDate(java.sql.Date date) {
        return date == null ? null : date.toLocalDate();
    }

    protected static LocalDateTime toLocalDateTime(java.sql.Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }

    protected static java.sql.Date sqlDate(LocalDate date) {
        return date == null ? null : java.sql.Date.valueOf(date);
    }

    protected static String like(String value) {
        return "%" + (value == null ? "" : value.trim().toLowerCase()) + "%";
    }
}
