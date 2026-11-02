-- Placement NIT — MySQL 8.4 schema
-- Apply with:
--   mysql -u placement_app -p placement_nit < database/01-schema.sql
--   mysql -u placement_app -p placement_nit_test < database/01-schema.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS results;
DROP TABLE IF EXISTS applications;
DROP TABLE IF EXISTS rounds;
DROP TABLE IF EXISTS eligibility_branches;
DROP TABLE IF EXISTS eligibility_criteria;
DROP TABLE IF EXISTS announcements;
DROP TABLE IF EXISTS student_profiles;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE users (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL,
  email VARCHAR(190) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  role ENUM('STUDENT', 'COORDINATOR', 'ADMIN') NOT NULL,
  status ENUM('ACTIVE', 'BLOCKED') NOT NULL DEFAULT 'ACTIVE',
  department VARCHAR(120) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email),
  KEY idx_users_role_status (role, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE student_profiles (
  user_id BIGINT UNSIGNED NOT NULL,
  roll_no VARCHAR(40) NOT NULL,
  branch VARCHAR(80) NOT NULL,
  batch VARCHAR(20) NOT NULL,
  cgpa DECIMAL(4,2) NOT NULL,
  backlogs INT NOT NULL DEFAULT 0,
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_student_roll_no (roll_no),
  KEY idx_student_branch_batch (branch, batch),
  CONSTRAINT fk_student_profile_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT chk_student_cgpa CHECK (cgpa >= 0 AND cgpa <= 10),
  CONSTRAINT chk_student_backlogs CHECK (backlogs >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE announcements (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  company_name VARCHAR(160) NOT NULL,
  role_title VARCHAR(160) NOT NULL,
  description TEXT NOT NULL,
  ctc_lpa DECIMAL(8,2) NOT NULL,
  location VARCHAR(160) NOT NULL,
  jd_file_path VARCHAR(255) NULL,
  jd_original_name VARCHAR(255) NULL,
  last_date DATETIME NOT NULL,
  test_date DATE NULL,
  interview_date DATE NULL,
  venue VARCHAR(160) NULL,
  test_link VARCHAR(500) NULL,
  is_archived BOOLEAN NOT NULL DEFAULT FALSE,
  created_by BIGINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_announcements_last_date (last_date),
  KEY idx_announcements_company (company_name),
  KEY idx_announcements_archived (is_archived),
  KEY idx_announcements_created_by (created_by),
  CONSTRAINT fk_announcement_creator
    FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
  CONSTRAINT chk_announcement_ctc CHECK (ctc_lpa >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE eligibility_criteria (
  announcement_id BIGINT UNSIGNED NOT NULL,
  min_cgpa DECIMAL(4,2) NOT NULL DEFAULT 0.00,
  max_backlogs INT NOT NULL DEFAULT 0,
  batch VARCHAR(20) NULL,
  PRIMARY KEY (announcement_id),
  CONSTRAINT fk_eligibility_announcement
    FOREIGN KEY (announcement_id) REFERENCES announcements (id) ON DELETE CASCADE,
  CONSTRAINT chk_eligibility_cgpa CHECK (min_cgpa >= 0 AND min_cgpa <= 10),
  CONSTRAINT chk_eligibility_backlogs CHECK (max_backlogs >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE eligibility_branches (
  announcement_id BIGINT UNSIGNED NOT NULL,
  branch VARCHAR(80) NOT NULL,
  PRIMARY KEY (announcement_id, branch),
  CONSTRAINT fk_eligibility_branch_announcement
    FOREIGN KEY (announcement_id) REFERENCES announcements (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE rounds (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  announcement_id BIGINT UNSIGNED NOT NULL,
  round_no INT NOT NULL,
  name VARCHAR(120) NOT NULL,
  type ENUM('TEST', 'INTERVIEW', 'OTHER') NOT NULL,
  scheduled_at DATETIME NULL,
  venue VARCHAR(160) NULL,
  test_link VARCHAR(500) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_round_number (announcement_id, round_no),
  KEY idx_rounds_scheduled_at (scheduled_at),
  CONSTRAINT fk_round_announcement
    FOREIGN KEY (announcement_id) REFERENCES announcements (id) ON DELETE CASCADE,
  CONSTRAINT chk_round_no CHECK (round_no > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE applications (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  student_id BIGINT UNSIGNED NOT NULL,
  announcement_id BIGINT UNSIGNED NOT NULL,
  status ENUM('BOOKMARKED', 'APPLIED', 'SHORTLISTED', 'SELECTED', 'REJECTED') NOT NULL,
  applied_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_application_student_announcement (student_id, announcement_id),
  KEY idx_applications_announcement_status (announcement_id, status),
  KEY idx_applications_student (student_id),
  CONSTRAINT fk_application_student
    FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_application_announcement
    FOREIGN KEY (announcement_id) REFERENCES announcements (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE results (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  announcement_id BIGINT UNSIGNED NOT NULL,
  student_id BIGINT UNSIGNED NOT NULL,
  round_id BIGINT UNSIGNED NULL,
  result ENUM('SHORTLISTED', 'SELECTED', 'REJECTED') NOT NULL,
  published_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_result_announcement_student_round (announcement_id, student_id, round_id, result),
  KEY idx_results_student (student_id),
  KEY idx_results_announcement (announcement_id),
  CONSTRAINT fk_result_announcement
    FOREIGN KEY (announcement_id) REFERENCES announcements (id) ON DELETE CASCADE,
  CONSTRAINT fk_result_student
    FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_result_round
    FOREIGN KEY (round_id) REFERENCES rounds (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE notifications (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  announcement_id BIGINT UNSIGNED NULL,
  channel ENUM('IN_APP', 'EMAIL') NOT NULL,
  type ENUM('ANNOUNCEMENT', 'REMINDER', 'RESULT', 'UPDATE', 'APPLICATION', 'ACCOUNT')
    NOT NULL DEFAULT 'UPDATE',
  title VARCHAR(200) NOT NULL,
  message VARCHAR(1000) NOT NULL,
  is_read BOOLEAN NOT NULL DEFAULT FALSE,
  status ENUM('PENDING', 'SENT', 'FAILED') NOT NULL DEFAULT 'PENDING',
  dedupe_key VARCHAR(190) NOT NULL,
  error_message VARCHAR(500) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  sent_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_notifications_dedupe (dedupe_key),
  KEY idx_notifications_user_read (user_id, is_read, created_at),
  KEY idx_notifications_status (status),
  KEY idx_notifications_announcement (announcement_id),
  CONSTRAINT fk_notification_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_notification_announcement
    FOREIGN KEY (announcement_id) REFERENCES announcements (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
