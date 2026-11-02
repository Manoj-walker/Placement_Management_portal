-- Placement NIT — demo data
-- Apply with:
--   mysql -u placement_app -p placement_nit < database/02-seed.sql
--
-- Demo accounts, shown on the frontend login screen:
--   student@nit.edu / Student@123
--   coordinator@nit.edu / Coord@123
--   admin@nit.edu / Admin@123
-- Stored values are BCrypt hashes ($2a$, cost 10).

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE notifications;
TRUNCATE TABLE results;
TRUNCATE TABLE applications;
TRUNCATE TABLE rounds;
TRUNCATE TABLE eligibility_branches;
TRUNCATE TABLE eligibility_criteria;
TRUNCATE TABLE announcements;
TRUNCATE TABLE student_profiles;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------------
-- Users (demo credentials shown on the frontend login screen)
-- ---------------------------------------------------------------------------
INSERT INTO users (id, name, email, password_hash, role, status) VALUES
  (1, 'Manoj', 'student@nit.edu', '$2a$10$Oxp0EjCpNo0n9wCaZ7t69OW/2nfGN4uyluFxMy922cpjxUlmhTvKu', 'STUDENT', 'ACTIVE'),
  (2, 'Chakradhar', 'chakradhar@nit.edu', '$2a$10$Oxp0EjCpNo0n9wCaZ7t69OW/2nfGN4uyluFxMy922cpjxUlmhTvKu', 'STUDENT', 'ACTIVE'),
  (3, 'Raghu', 'raghu@nit.edu', '$2a$10$Oxp0EjCpNo0n9wCaZ7t69OW/2nfGN4uyluFxMy922cpjxUlmhTvKu', 'STUDENT', 'ACTIVE'),
  (4, 'Ram Charan', 'ramcharan@nit.edu', '$2a$10$Oxp0EjCpNo0n9wCaZ7t69OW/2nfGN4uyluFxMy922cpjxUlmhTvKu', 'STUDENT', 'ACTIVE'),
  (5, 'Ananya', 'ananya@nit.edu', '$2a$10$Oxp0EjCpNo0n9wCaZ7t69OW/2nfGN4uyluFxMy922cpjxUlmhTvKu', 'STUDENT', 'ACTIVE'),
  (6, 'Rahul', 'rahul@nit.edu', '$2a$10$Oxp0EjCpNo0n9wCaZ7t69OW/2nfGN4uyluFxMy922cpjxUlmhTvKu', 'STUDENT', 'ACTIVE'),
  (7, 'Dr. Placement Cell', 'coordinator@nit.edu', '$2a$10$M6QSkopwDvBfnv1Y/K3kO.C1naQI06Ermm0a6QV7IWNM3Z0AeEezK', 'COORDINATOR', 'ACTIVE'),
  (8, 'System Administrator', 'admin@nit.edu', '$2a$10$6POjdntnk8reCCFkO0Ikie7k.yuQWiZxANzwa0mzXUkmghuzV4vmq', 'ADMIN', 'ACTIVE');

INSERT INTO student_profiles (user_id, roll_no, branch, batch, cgpa, backlogs) VALUES
  (1, '25CSB0A53', 'CSE', '2026', 8.70, 0),
  (2, '25CSB0A76', 'CSE', '2026', 8.20, 0),
  (3, '25CSB0A80', 'ECE', '2026', 7.40, 1),
  (4, '25CSB0A46', 'CSE', '2026', 8.90, 0),
  (5, '25CSB0A61', 'IT', '2026', 8.10, 0),
  (6, '25CSB0A92', 'MECH', '2026', 6.80, 2);

-- ---------------------------------------------------------------------------
-- Announcements
-- ---------------------------------------------------------------------------
INSERT INTO announcements
  (id, company_name, role_title, description, ctc_lpa, location, jd_file_path, jd_original_name, last_date, test_date, interview_date, venue, test_link, is_archived, created_by, created_at, updated_at)
VALUES
  (1, 'Infosys', 'Systems Engineer',
   'Infosys is hiring final-year students for the Systems Engineer role. Selected candidates will work on enterprise Java and cloud projects after a structured training programme.',
   6.50, 'Bangalore', NULL, NULL, '2026-11-15 23:59:59', '2026-11-18', '2026-11-22', 'Placement Cell, Block A', 'https://example.com/infosys-test', FALSE, 7, '2026-10-18 10:00:00', '2026-10-18 10:00:00'),
  (2, 'TCS', 'Digital Developer',
   'TCS Digital hiring for students interested in full-stack development, microservices, and cloud-native applications. Online aptitude test followed by technical and HR interviews.',
   9.00, 'Hyderabad', NULL, NULL, '2026-11-20 23:59:59', '2026-11-23', '2026-11-27', 'TCS Office, Hyderabad', 'https://example.com/tcs-test', FALSE, 7, '2026-10-20 09:30:00', '2026-10-20 09:30:00'),
  (3, 'Wipro', 'Project Engineer',
   'Wipro is hiring Project Engineers across branches. The role includes application support, testing, and development. One backlog is allowed.',
   5.50, 'Pune', NULL, NULL, '2026-11-10 23:59:59', '2026-11-12', '2026-11-16', 'Placement Cell, Block B', 'https://example.com/wipro-test', FALSE, 7, '2026-10-22 14:15:00', '2026-10-22 14:15:00'),
  (4, 'Accenture', 'Associate Software Engineer',
   'Accenture is hiring Associate Software Engineers for Java, React, and cloud engineering roles. Cognitive and coding assessment followed by a technical interview.',
   7.20, 'Chennai', NULL, NULL, '2026-12-01 23:59:59', '2026-12-03', '2026-12-07', 'Accenture, Chennai', 'https://example.com/accenture-test', FALSE, 7, '2026-10-25 11:00:00', '2026-10-25 11:00:00'),
  (5, 'Deloitte', 'Analyst',
   'Deloitte Analyst role for students interested in consulting, data analysis, and enterprise technology. This drive is now closed.',
   8.50, 'Mumbai', NULL, NULL, '2026-10-30 23:59:59', '2026-11-02', '2026-11-05', 'Deloitte, Mumbai', 'https://example.com/deloitte-test', FALSE, 7, '2026-10-01 09:00:00', '2026-10-01 09:00:00'),
  (6, 'Amazon', 'SDE Intern',
   'Amazon is hiring SDE interns for a six-month internship with a pre-placement offer opportunity. Strong data structures and problem-solving skills required.',
   12.00, 'Remote', NULL, NULL, '2026-12-10 23:59:59', '2026-12-12', '2026-12-15', 'Amazon, Bangalore', 'https://example.com/amazon-test', FALSE, 7, '2026-10-28 16:45:00', '2026-10-28 16:45:00');

-- ---------------------------------------------------------------------------
-- Eligibility criteria (frontend batches are plain years, defaults to 2026)
-- ---------------------------------------------------------------------------
INSERT INTO eligibility_criteria (announcement_id, min_cgpa, max_backlogs, batch) VALUES
  (1, 7.00, 0, '2026'),
  (2, 7.50, 0, '2026'),
  (3, 6.50, 1, '2026'),
  (4, 7.00, 0, '2026'),
  (5, 7.00, 0, '2026'),
  (6, 8.00, 0, '2026');

INSERT INTO eligibility_branches (announcement_id, branch) VALUES
  (1, 'CSE'), (1, 'ECE'),
  (2, 'CSE'), (2, 'IT'),
  (4, 'CSE'), (4, 'IT'), (4, 'ECE'),
  (6, 'CSE');
-- Announcements 3 and 5 allow all branches, so they have no branch rows.

-- ---------------------------------------------------------------------------
-- Rounds
-- ---------------------------------------------------------------------------
INSERT INTO rounds (id, announcement_id, round_no, name, type, scheduled_at, venue, test_link) VALUES
  (1, 1, 1, 'Online Test', 'TEST', '2026-11-18 10:00:00', 'Online', 'https://example.com/infosys-test'),
  (2, 1, 2, 'Technical Interview', 'INTERVIEW', '2026-11-22 10:00:00', 'Placement Cell, Block A', NULL),
  (3, 1, 3, 'HR Interview', 'INTERVIEW', '2026-11-25 10:00:00', 'Placement Cell, Block A', NULL),
  (4, 2, 1, 'Aptitude Test', 'TEST', '2026-11-23 09:00:00', 'Online', 'https://example.com/tcs-test'),
  (5, 2, 2, 'Technical Interview', 'INTERVIEW', '2026-11-27 10:00:00', 'TCS Office, Hyderabad', NULL),
  (6, 3, 1, 'Online Assessment', 'TEST', '2026-11-12 11:00:00', 'Online', 'https://example.com/wipro-test'),
  (7, 3, 2, 'Technical Interview', 'INTERVIEW', '2026-11-16 10:00:00', 'Placement Cell, Block B', NULL),
  (8, 4, 1, 'Cognitive Assessment', 'TEST', '2026-12-03 10:00:00', 'Online', 'https://example.com/accenture-test'),
  (9, 4, 2, 'Technical Interview', 'INTERVIEW', '2026-12-07 10:00:00', 'Accenture, Chennai', NULL),
  (10, 5, 1, 'Online Test', 'TEST', '2026-11-02 10:00:00', 'Online', 'https://example.com/deloitte-test'),
  (11, 5, 2, 'Interview', 'INTERVIEW', '2026-11-05 10:00:00', 'Deloitte, Mumbai', NULL),
  (12, 6, 1, 'Online Test', 'TEST', '2026-12-12 10:00:00', 'Online', 'https://example.com/amazon-test'),
  (13, 6, 2, 'Technical Interviews', 'INTERVIEW', '2026-12-15 10:00:00', 'Amazon, Bangalore', NULL);

-- ---------------------------------------------------------------------------
-- Applications
-- ---------------------------------------------------------------------------
INSERT INTO applications (id, student_id, announcement_id, status, applied_at, created_at, updated_at) VALUES
  (1, 1, 1, 'APPLIED', '2026-10-20 12:00:00', '2026-10-20 12:00:00', '2026-10-20 12:00:00'),
  (2, 1, 2, 'BOOKMARKED', NULL, '2026-10-21 09:00:00', '2026-10-21 09:00:00'),
  (3, 1, 6, 'APPLIED', '2026-10-29 18:30:00', '2026-10-29 18:30:00', '2026-10-29 18:30:00'),
  (4, 2, 1, 'APPLIED', '2026-10-21 15:00:00', '2026-10-21 15:00:00', '2026-10-21 15:00:00'),
  (5, 2, 3, 'APPLIED', '2026-10-23 10:00:00', '2026-10-23 10:00:00', '2026-10-23 10:00:00'),
  (6, 3, 3, 'APPLIED', '2026-10-24 11:30:00', '2026-10-24 11:30:00', '2026-10-24 11:30:00'),
  (7, 3, 5, 'APPLIED', '2026-10-02 10:00:00', '2026-10-02 10:00:00', '2026-10-02 10:00:00'),
  (8, 4, 1, 'APPLIED', '2026-10-20 08:45:00', '2026-10-20 08:45:00', '2026-10-20 08:45:00'),
  (9, 4, 2, 'APPLIED', '2026-10-22 13:00:00', '2026-10-22 13:00:00', '2026-10-22 13:00:00'),
  (10, 4, 6, 'BOOKMARKED', NULL, '2026-10-30 19:00:00', '2026-10-30 19:00:00'),
  (11, 5, 4, 'APPLIED', '2026-10-26 09:15:00', '2026-10-26 09:15:00', '2026-10-26 09:15:00'),
  (12, 5, 2, 'APPLIED', '2026-10-23 17:20:00', '2026-10-23 17:20:00', '2026-10-23 17:20:00'),
  (13, 6, 3, 'BOOKMARKED', NULL, '2026-10-24 12:00:00', '2026-10-24 12:00:00');

-- ---------------------------------------------------------------------------
-- Results
-- ---------------------------------------------------------------------------
INSERT INTO results (id, announcement_id, student_id, round_id, result, published_at) VALUES
  (1, 1, 1, 1, 'SHORTLISTED', '2026-10-31 10:00:00'),
  (2, 1, 4, 1, 'SHORTLISTED', '2026-10-31 10:00:00'),
  (3, 1, 2, 1, 'REJECTED', '2026-10-31 10:00:00');

-- Keep application status in sync with the seeded results.
UPDATE applications SET status = 'SHORTLISTED' WHERE announcement_id = 1 AND student_id IN (1, 4);
UPDATE applications SET status = 'REJECTED' WHERE announcement_id = 1 AND student_id = 2;

-- ---------------------------------------------------------------------------
-- Notifications
-- ---------------------------------------------------------------------------
INSERT INTO notifications
  (id, user_id, announcement_id, channel, type, title, message, is_read, status, dedupe_key, created_at, sent_at)
VALUES
  (1, 1, NULL, 'IN_APP', 'ACCOUNT', 'Welcome to Placement NIT', 'Your student account is ready. Complete your profile and start exploring companies.', TRUE, 'SENT', 'seed:welcome:1:inapp', '2026-10-01 09:00:00', '2026-10-01 09:00:00'),
  (2, 2, NULL, 'IN_APP', 'ACCOUNT', 'Welcome to Placement NIT', 'Your student account is ready. Complete your profile and start exploring companies.', TRUE, 'SENT', 'seed:welcome:2:inapp', '2026-10-01 09:00:00', '2026-10-01 09:00:00'),
  (3, 3, NULL, 'IN_APP', 'ACCOUNT', 'Welcome to Placement NIT', 'Your student account is ready. Complete your profile and start exploring companies.', FALSE, 'SENT', 'seed:welcome:3:inapp', '2026-10-01 09:00:00', '2026-10-01 09:00:00'),
  (4, 4, NULL, 'IN_APP', 'ACCOUNT', 'Welcome to Placement NIT', 'Your student account is ready. Complete your profile and start exploring companies.', TRUE, 'SENT', 'seed:welcome:4:inapp', '2026-10-01 09:00:00', '2026-10-01 09:00:00'),
  (5, 1, 1, 'IN_APP', 'ANNOUNCEMENT', 'Infosys — Systems Engineer', 'A new eligible announcement was posted. Last date: 15 Nov 2026.', FALSE, 'SENT', 'seed:announcement:1:created:1:inapp', '2026-10-18 10:00:00', '2026-10-18 10:00:00'),
  (6, 4, 1, 'IN_APP', 'ANNOUNCEMENT', 'Infosys — Systems Engineer', 'A new eligible announcement was posted. Last date: 15 Nov 2026.', FALSE, 'SENT', 'seed:announcement:1:created:4:inapp', '2026-10-18 10:00:00', '2026-10-18 10:00:00'),
  (7, 1, 1, 'EMAIL', 'ANNOUNCEMENT', 'Infosys — Systems Engineer', 'A new eligible announcement was posted. Last date: 15 Nov 2026.', FALSE, 'SENT', 'seed:announcement:1:created:1:email', '2026-10-18 10:00:00', '2026-10-18 10:00:05'),
  (8, 2, 2, 'IN_APP', 'ANNOUNCEMENT', 'TCS — Digital Developer', 'A new eligible announcement was posted. Last date: 20 Nov 2026.', TRUE, 'SENT', 'seed:announcement:2:created:2:inapp', '2026-10-20 09:30:00', '2026-10-20 09:30:00'),
  (9, 5, 2, 'IN_APP', 'ANNOUNCEMENT', 'TCS — Digital Developer', 'A new eligible announcement was posted. Last date: 20 Nov 2026.', FALSE, 'SENT', 'seed:announcement:2:created:5:inapp', '2026-10-20 09:30:00', '2026-10-20 09:30:00'),
  (10, 3, 3, 'IN_APP', 'ANNOUNCEMENT', 'Wipro — Project Engineer', 'A new announcement allows one backlog. Last date: 10 Nov 2026.', FALSE, 'SENT', 'seed:announcement:3:created:3:inapp', '2026-10-22 14:15:00', '2026-10-22 14:15:00'),
  (11, 1, 1, 'IN_APP', 'RESULT', 'Infosys shortlist published', 'You have been shortlisted for the Online Test round. Check the rounds section.', FALSE, 'SENT', 'seed:result:1:1:inapp', '2026-10-31 10:00:00', '2026-10-31 10:00:00'),
  (12, 4, 1, 'IN_APP', 'RESULT', 'Infosys shortlist published', 'You have been shortlisted for the Online Test round. Check the rounds section.', FALSE, 'SENT', 'seed:result:2:4:inapp', '2026-10-31 10:00:00', '2026-10-31 10:00:00'),
  (13, 3, 3, 'IN_APP', 'REMINDER', 'Deadline reminder: Wipro', 'The last date for Wipro — Project Engineer is 10 Nov 2026. Apply soon.', FALSE, 'SENT', 'seed:reminder:deadline:3:3:2026-11-09', '2026-11-09 08:00:00', '2026-11-09 08:00:00');
