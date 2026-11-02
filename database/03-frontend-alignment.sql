-- Placement NIT — one-time migration to align the existing database
-- with the React frontend API contract (frontend is the source of truth).
--
-- Apply with:
--   mysql -u placement_app -p placement_nit < database/03-frontend-alignment.sql
--
-- Safe to run more than once: column additions are guarded, and the data
-- updates (backfills, demo accounts) are idempotent.

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------------
-- 1) announcements: top-level schedule fields shown in the UI
-- ---------------------------------------------------------------------------
SET @sql = (SELECT IF(COUNT(*) = 0,
        'ALTER TABLE announcements ADD COLUMN test_date DATE NULL AFTER last_date',
        'SELECT ''skip: announcements.test_date''')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'announcements' AND COLUMN_NAME = 'test_date');
PREPARE stmt FROM @sql; EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(COUNT(*) = 0,
        'ALTER TABLE announcements ADD COLUMN interview_date DATE NULL AFTER test_date',
        'SELECT ''skip: announcements.interview_date''')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'announcements' AND COLUMN_NAME = 'interview_date');
PREPARE stmt FROM @sql; EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(COUNT(*) = 0,
        'ALTER TABLE announcements ADD COLUMN venue VARCHAR(160) NULL AFTER interview_date',
        'SELECT ''skip: announcements.venue''')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'announcements' AND COLUMN_NAME = 'venue');
PREPARE stmt FROM @sql; EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(COUNT(*) = 0,
        'ALTER TABLE announcements ADD COLUMN test_link VARCHAR(500) NULL AFTER venue',
        'SELECT ''skip: announcements.test_link''')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'announcements' AND COLUMN_NAME = 'test_link');
PREPARE stmt FROM @sql; EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Backfill schedule values from the earliest TEST round and first venue.
UPDATE announcements a
SET a.test_date = (
        SELECT DATE(MIN(r.scheduled_at)) FROM rounds r
        WHERE r.announcement_id = a.id AND r.type = 'TEST'),
    a.interview_date = (
        SELECT DATE(MIN(r.scheduled_at)) FROM rounds r
        WHERE r.announcement_id = a.id AND r.type = 'INTERVIEW'),
    a.venue = (
        SELECT r.venue FROM rounds r
        WHERE r.announcement_id = a.id AND r.venue IS NOT NULL
        ORDER BY r.round_no LIMIT 1),
    a.test_link = (
        SELECT r.test_link FROM rounds r
        WHERE r.announcement_id = a.id AND r.type = 'TEST' AND r.test_link IS NOT NULL
        ORDER BY r.round_no LIMIT 1);

-- ---------------------------------------------------------------------------
-- 2) notifications: explicit type used for the frontend icon mapping
-- ---------------------------------------------------------------------------
SET @sql = (SELECT IF(COUNT(*) = 0,
        'ALTER TABLE notifications ADD COLUMN type ENUM(''ANNOUNCEMENT'',''REMINDER'',''RESULT'',''UPDATE'',''APPLICATION'',''ACCOUNT'') NOT NULL DEFAULT ''UPDATE'' AFTER channel',
        'SELECT ''skip: notifications.type''')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'notifications' AND COLUMN_NAME = 'type');
PREPARE stmt FROM @sql; EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE notifications SET type = 'REMINDER' WHERE dedupe_key LIKE '%reminder%';
UPDATE notifications SET type = 'RESULT' WHERE dedupe_key LIKE '%result%';
UPDATE notifications SET type = 'ANNOUNCEMENT' WHERE dedupe_key LIKE '%announcement%';
UPDATE notifications SET type = 'ACCOUNT' WHERE (type = 'UPDATE' OR type IS NULL)
    AND (dedupe_key LIKE '%welcome%' OR title LIKE 'Welcome%');

-- ---------------------------------------------------------------------------
-- 3) Demo accounts expected by the frontend login screen
--    student@nit.edu / Student@123
--    coordinator@nit.edu / Coord@123
--    admin@nit.edu / Admin@123
-- ---------------------------------------------------------------------------
UPDATE users SET email = 'student@nit.edu',
                 password_hash = '$2a$10$Oxp0EjCpNo0n9wCaZ7t69OW/2nfGN4uyluFxMy922cpjxUlmhTvKu'
WHERE email = 'manoj@nit.edu';
UPDATE users SET password_hash = '$2a$10$M6QSkopwDvBfnv1Y/K3kO.C1naQI06Ermm0a6QV7IWNM3Z0AeEezK'
WHERE email = 'coordinator@nit.edu';
UPDATE users SET password_hash = '$2a$10$6POjdntnk8reCCFkO0Ikie7k.yuQWiZxANzwa0mzXUkmghuzV4vmq'
WHERE email = 'admin@nit.edu';

-- The frontend register/announcement forms use plain years (2026, 2027, 2028);
-- seeded accounts and criteria use the same values so drives are eligible.
UPDATE student_profiles SET batch = '2026' WHERE batch IS NULL OR batch <> '2026';
UPDATE eligibility_criteria SET batch = '2026' WHERE batch IS NOT NULL AND batch <> '2026';
