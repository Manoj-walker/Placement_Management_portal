package edu.nit.placement.config;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

/**
 * Applies the numbered SQL pipeline from the project's database/ folder
 * (mapped onto the classpath as db/):
 *
 *   db/01-schema.sql            — DDL, only when ANNOUNCEMENTS is missing
 *   db/02-seed.sql              — demo data, only when USERS is empty
 *   db/03-frontend-alignment.sql— idempotent alignment migration (runs after seed)
 *
 * Every statement of a script runs on one connection so SET FOREIGN_KEY_CHECKS
 * and SET NAMES hold. The database itself is created by the JDBC driver via
 * createDatabaseIfNotExist=true (00-create-databases.sql stays a manual step).
 */
@Component
public class DatabaseInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final JdbcTemplate jdbc;
    private final DataSource dataSource;

    public DatabaseInitializer(JdbcTemplate jdbc, DataSource dataSource) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!tableExists("announcements")) {
            log.info("No schema — applying db/01-schema.sql");
            runScript("db/01-schema.sql");
        } else {
            log.info("Placement NIT schema already present");
        }
        if (userCount() == 0) {
            log.info("Empty users — applying db/02-seed.sql and db/03-frontend-alignment.sql");
            runScript("db/02-seed.sql");
            runScript("db/03-frontend-alignment.sql");
        }
    }

    private int userCount() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        return count == null ? 0 : count;
    }

    private boolean tableExists(String table) throws Exception {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                Integer.class, table);
        return count != null && count > 0;
    }

    private void runScript(String path) throws Exception {
        String script = StreamUtils.copyToString(
                new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8);
        List<String> statements = splitStatements(script);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            try (Statement statement = connection.createStatement()) {
                for (String sql : statements) {
                    statement.execute(sql);
                }
            }
        }
    }

    /** Splits on semicolons, ignoring blank lines and SQL comments. */
    private List<String> splitStatements(String script) {
        StringBuilder cleaned = new StringBuilder();
        for (String line : script.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }
            cleaned.append(line).append('\n');
        }
        List<String> statements = new ArrayList<>();
        for (String part : cleaned.toString().split(";")) {
            String statement = part.trim();
            if (!statement.isEmpty()) {
                statements.add(statement);
            }
        }
        return statements;
    }
}
