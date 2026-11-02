# Placement NIT — Database

Everything the app needs from MySQL lives in **this folder**. Four numbered SQL files,
applied strictly in order:

| File | What it does | Applied when |
| --- | --- | --- |
| `00-create-databases.sql` | Creates the `placement_nit` + `placement_nit_test` databases (and, optionally, a dedicated `placement_app` MySQL user — unused; the backend connects as `root`) | Manually, once per machine |
| `01-schema.sql` | Full DDL — all 9 tables, FKs and checks | Automatically at boot if the schema is missing |
| `02-seed.sql` | Demo data: 8 users, 6 drives, eligibility, rounds, 13 applications, 3 results, 13 notifications | Automatically at boot if `users` is empty |
| `03-frontend-alignment.sql` | Idempotent migration aligning an existing DB with the frontend contract | Automatically after seeding |

The Spring `DatabaseInitializer` applies `01` → (`02` → `03`) on startup; each script only
runs when its precondition is missing, so **your data survives restarts**.

## Connection

| Setting | Value |
| --- | --- |
| Server | `localhost:3306` (local MySQL 8) |
| Backend login | `root` / `manoj@123` (see `server/.env.example` for overrides) |
| App database | `placement_nit` — created automatically on first connect |
| Test database | `placement_nit_test` — only used by `./mvnw test`, separate from real data |

## The 9 tables (`01-schema.sql`)

| Table | Stores |
| --- | --- |
| `users` | Accounts only: name, email, BCrypt `password_hash`, role, status, coordinator department |
| `student_profiles` | Student fields (roll no, branch, batch, CGPA, backlogs) keyed by `user_id`, cascade-deleted |
| `announcements` | Company drives: dates, venue, test link, CTC (`ctc_lpa`); uploaded JDs live **on disk**, recorded in `jd_file_path` / `jd_original_name` |
| `eligibility_criteria` | Per-drive rules: min CGPA, max backlogs, batch |
| `eligibility_branches` | One row per eligible branch |
| `rounds` | Drive rounds: `round_no`, name, type (TEST/INTERVIEW/OTHER), scheduled_at |
| `applications` | One row per (student, drive): BOOKMARKED / APPLIED / SHORTLISTED / SELECTED / REJECTED |
| `results` | Shortlists & selections, optionally linked to a round (`round_id`) |
| `notifications` | In-app inbox **and** email audit trail (`channel` = IN_APP or EMAIL, `status`, `dedupe_key`) |

## Demo accounts (`02-seed.sql`)

| ID | Name | Email | Role | Password |
| --- | --- | --- | --- | --- |
| 1 | Manoj | `student@nit.edu` | STUDENT | `Student@123` |
| 2 | Chakradhar | `chakradhar@nit.edu` | STUDENT | `Student@123` |
| 3 | Raghu | `raghu@nit.edu` | STUDENT | `Student@123` |
| 4 | Ram Charan | `ramcharan@nit.edu` | STUDENT | `Student@123` |
| 5 | Ananya | `ananya@nit.edu` | STUDENT | `Student@123` |
| 6 | Rahul | `rahul@nit.edu` | STUDENT | `Student@123` |
| 7 | Dr. Placement Cell | `coordinator@nit.edu` | COORDINATOR | `Coord@123` |
| 8 | System Administrator | `admin@nit.edu` | ADMIN | `Admin@123` |

Only BCrypt hashes are stored.

## Everyday SQL

```bash
# Peek at the data
mysql -u root -p'manoj@123' -e "SELECT ID, NAME, EMAIL, ROLE, STATUS FROM placement_nit.users;"
mysql -u root -p'manoj@123' -e "SELECT ID, COMPANY_NAME, CTC_LPA, LAST_DATE FROM placement_nit.announcements;"

# Reset everything to the fresh demo state (loses accounts/drives you created)
mysql -u root -p'manoj@123' -e "DROP DATABASE placement_nit;"
cd ../server && ./run.sh        # re-applies 01 → 02 → 03 from scratch

# Manual application of the pipeline (as this folder's headers describe)
mysql -u root -p'manoj@123' < database/00-create-databases.sql
mysql -u root -p'manoj@123' placement_nit       < database/01-schema.sql
mysql -u root -p'manoj@123' placement_nit       < database/02-seed.sql
mysql -u root -p'manoj@123' placement_nit       < database/03-frontend-alignment.sql

# Back up / restore
mysqldump -u root -p'manoj@123' placement_nit > backup.sql
mysql  -u root -p'manoj@123' placement_nit < backup.sql
```

## Notes

- Uploaded job descriptions are stored on disk (`server/uploads/` by default, see
  `app.jd.storage-dir`), not in the database — the drive's `jd_file_path` points to the file.
- Never delete the system databases MySQL ships with (`mysql`, `information_schema`,
  `performance_schema`, `sys`) — they are required for MySQL to work.
