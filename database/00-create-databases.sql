-- Placement NIT — local database bootstrap
-- Run once as a MySQL administrator:
--   mysql -u root < database/00-create-databases.sql
--
-- The password below is for local development only. Change it before sharing
-- the project or deploying it anywhere.

CREATE DATABASE IF NOT EXISTS placement_nit
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE DATABASE IF NOT EXISTS placement_nit_test
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'placement_app'@'localhost'
  IDENTIFIED BY 'PnitApp2026Dev';

ALTER USER 'placement_app'@'localhost'
  IDENTIFIED BY 'PnitApp2026Dev';

GRANT ALL PRIVILEGES ON placement_nit.* TO 'placement_app'@'localhost';
GRANT ALL PRIVILEGES ON placement_nit_test.* TO 'placement_app'@'localhost';
FLUSH PRIVILEGES;

SELECT 'placement_nit and placement_nit_test are ready.' AS status;
