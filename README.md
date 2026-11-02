# Placement NIT — Placement Information Management & Notification System

A full-stack placement portal for NIT: coordinators publish company drives, students see only what they are eligible for, and everyone gets in-app + email notifications before deadlines. Built for the **CS1207 Object Oriented Programming Lab** (Group 17).

- **Frontend:** React 19 + Vite 6 + Tailwind CSS 4
- **Backend:** Java 21 LTS + Spring Boot 3.5 + **JdbcTemplate** (hand-written SQL/DAO layer)
- **Database:** Local **MySQL 8** (`placement_nit`, user `root`) — database, tables and demo data are created automatically on first run

---

## Quick start (no setup needed)

The backend needs a local **MySQL 8** server running on `localhost:3306` (user `root`, password `manoj@123` by default). Nothing to create manually — the `placement_nit` database, all tables and the demo data are set up automatically on first start.

> 🚀 **Full launch steps:** see [`LAUNCH.md`](LAUNCH.md). **Database details:** see [`database/README.md`](database/README.md).

```bash
# 1. Backend (terminal 1) — easiest way; survives closing the terminal
cd server
./run.sh

#    or run it the manual way:
#    JAVA_HOME="/path/to/jdk-21" ./mvnw spring-boot:run -Dspring-boot.run.profiles=demo

# 2. Frontend (terminal 2)
cd client
npm install
npm run dev -- --host 127.0.0.1 --port 5173
```

Open <http://127.0.0.1:5173>.

- `server/run.sh` starts the backend detached (logs at `/tmp/placement-nit.log`), frees port 8080 from stale runs and waits until it responds.
- `server/stop.sh` stops it.
- Use the bundled **JDK 21** (`../.tools/jdk21`) — newer JDKs print harmless but noisy Maven warnings.

### Demo accounts

| Role | Email | Password |
| --- | --- | --- |
| Student | `student@nit.edu` | `Student@123` |
| Coordinator | `coordinator@nit.edu` | `Coord@123` |
| Admin | `admin@nit.edu` | `Admin@123` |

Extra seeded students: `chakradhar@nit.edu`, `raghu@nit.edu`, `ramcharan@nit.edu`, `sita@nit.edu` (all `Student@123`).

Demo data lives in the `placement_nit` MySQL database (created automatically next to your other databases). Drop all its tables (or the whole database) and restart the backend to re-seed from scratch.

### JDK note

Use **Java 21**. If your machine has a newer JDK as default, always export `JAVA_HOME` to a JDK 21 installation before running Maven.

---

## MySQL database

The application uses a local MySQL 8 server:

- **Host/port:** `localhost:3306`, **user:** `root`, **password:** `manoj@123`
- **Database:** `placement_nit` — created automatically on first connect (`createDatabaseIfNotExist=true` in the JDBC URL)
- **Schema:** the numbered SQL pipeline in **`database/`** is the single source of truth — `DatabaseInitializer` applies `01-schema.sql` on first start, `02-seed.sql` when the `USERS` table is empty, then `03-frontend-alignment.sql`; your data survives restarts because scripts only run when the schema/data is missing

To point the backend at a different MySQL instance, set `DB_URL`, `DB_USER` and `DB_PASSWORD` (see `server/.env.example`). Integration tests use a separate `placement_nit_test` database, so running tests never touches the real data.

### Environment variables

| Variable | Default | Purpose |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | HTTP port |
| `DB_URL` | `jdbc:mysql://localhost:3306/placement_nit?createDatabaseIfNotExist=true…` | Local MySQL database |
| `DB_USER` / `DB_PASSWORD` | `root` / `manoj@123` | MySQL credentials |
| `JWT_SECRET` | dev-only secret | HS256 signing key — **change in production** (32+ chars) |
| `MAIL_ENABLED` | `false` | Enable real email delivery |
| `MAIL_HOST`, `MAIL_PORT` | `smtp.gmail.com`, `587` | SMTP server |
| `MAIL_USER`, `MAIL_PASSWORD` | empty | SMTP credentials (use an app password) |
| `MAIL_FROM` | `no-reply@placement.nit.edu` | Sender address |
| `COLLEGE_EMAIL_DOMAIN` | `nit.edu` | Domain allowed to register |
| `CORS_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | Allowed frontend origins |
| `REMINDER_CRON` | `0 0 8 * * *` | Daily deadline-reminder schedule |

Frontend configuration: copy `client/.env.example` to `client/.env` and set `VITE_API_URL` if the API is not on `http://localhost:8080`.

---

## Features

**Student**
- Register/login with college email; JWT session
- Profile (branch, batch, CGPA, backlogs) drives live eligibility
- Dashboard with search, branch/CTC/eligibility filters and applied/bookmarked tabs
- Company detail: eligibility, rounds, dates, venue, test link, JD download
- Apply / bookmark / withdraw bookmark; deadline list sorted by urgency
- My results (shortlisted / selected / not shortlisted) and notification inbox with read toggles

**Coordinator**
- Create, edit, archive/restore and delete announcements
- Set eligibility criteria (branches, minimum CGPA, max backlogs, batch)
- Upload JD (PDF/DOC, max 5 MB) and update schedule/test link
- View applicants and publish shortlist/selection results (students are notified)

**Admin**
- System stats dashboard
- Create users with a temporary password; change roles
- Block/unblock, delete accounts, reset passwords
- View a user's stored BCrypt password hash (admin-only `GET /api/admin/users/{id}/password-hash`)
- Self-protection guards (cannot block/delete/demote own admin account)

**Notifications**
- Abstract `Notification` with `InAppNotification` + `EmailNotification` channels
- In-app notifications persisted in MySQL
- `@Scheduled` deadline reminders; email delivery is optional and each attempt is audited as a `channel = 'EMAIL'` row in `NOTIFICATIONS`

---

## Architecture

```
oops project/
├── client/                       React + Vite frontend
│   └── src/
│       ├── api/                  fetch client (JWT) + API→view mappers
│       ├── context/              SessionContext (login/register/refresh)
│       ├── pages/                student / coordinator / admin pages
│       ├── components/           Badge, PageHeader, ConfirmModal, …
│       └── data/                 branch options (UI constants)
└── server/                       Spring Boot backend
    └── src/main/java/edu/nit/placement/
        ├── model/                Domain: User → Student/Coordinator/Admin,
        │                         Notification → InApp/Email, Announcement,
        │                         EligibilityCriteria, Round, enums
        ├── dao/                  BaseDAO → UserDAO, AnnouncementDAO,
        │                         ApplicationDAO, ResultDAO, NotificationDAO,
        │                         EmailLogDAO (hand-written SQL over MySQL)
        ├── service/              Auth, Announcement, Notification, Reminder,
        │                         Admin, Seed (use cases)
        ├── controller/           REST controllers
        ├── security/             JWT filter/service, SecurityConfig
        ├── exception/            ApiException + global handler
        └── config/               DatabaseInitializer (schema + seed, scheduling, beans)
```

### Where the OOP concepts live

| Concept | Implementation |
| --- | --- |
| **Encapsulation** | Private fields + getters/setters; `passwordHash` is never serialized (`@JsonIgnore`) |
| **Abstraction** | Abstract `User` (`dashboardKey()`), abstract `Notification` (`send()`, `channel()`), abstract `BaseDAO` |
| **Inheritance** | `Student`/`Coordinator`/`Admin` extend `User`; `InAppNotification`/`EmailNotification` extend `Notification`; concrete DAOs extend `BaseDAO` |
| **Polymorphism** | Controllers/services hold `User`/`Notification`/`BaseDAO` references; behaviour resolves to the subclass at runtime |
| **Composition** | `Announcement` owns `EligibilityCriteria` and `List<Round>` |
| **Exception handling** | Domain/API errors via `ApiException` + `@RestControllerAdvice` |
| **Generics / collections** | DAO row mappers, `List`, `Map`, `Set` for branches/results |

### Database schema (9 tables)

The MySQL database `placement_nit` — DDL in **`database/01-schema.sql`** (single source of truth, mapped into the build): `users` (accounts only: name, email, BCrypt hash, role, status, coordinator department), `student_profiles` (student-specific fields, `ON DELETE CASCADE` from `users`), `announcements` (drive info; uploaded JDs live on disk, paths in `jd_file_path`), `eligibility_criteria`, `eligibility_branches`, `rounds`, `applications`, `results`, `notifications` (both the in-app inbox and the email audit trail, via `channel`).

> Demo data (8 pre-defined users + 6 drives + applications + results + notifications) comes from `database/02-seed.sql`; `03-frontend-alignment.sql` aligns any existing database with the frontend contract.

---

## API overview

| Method & path | Role | Purpose |
| --- | --- | --- |
| `POST /api/auth/register` | public | Student registration |
| `POST /api/auth/login` | public | JWT login |
| `GET /api/me` | any | Current account |
| `PUT /api/me/profile`, `/password` | any | Update profile / password |
| `GET /api/me/unread-count` | student | Notification badge |
| `GET /api/student/dashboard` | student | Eligible drives + flags |
| `GET /api/student/announcements/{id}` | student | Drive detail |
| `POST /api/student/announcements/{id}/apply` | student | Apply |
| `POST/DELETE /api/student/announcements/{id}/bookmark` | student | Bookmark toggle |
| `GET /api/student/{applications,bookmarks,deadlines,results}` | student | Lists |
| `GET/POST/PUT/DELETE /api/announcements…` | coordinator/admin | CRUD |
| `PATCH /api/announcements/{id}/archive`, `/schedule` | coordinator/admin | Archive / schedule |
| `POST /api/announcements/{id}/jd`, `GET …/jd` | upload / download | JD file |
| `GET /api/announcements/{id}/applicants`, `/results` | coordinator/admin | Applicants, results |
| `POST /api/announcements/{id}/results` | coordinator/admin | Publish shortlist/selection |
| `GET /api/notifications`, `PATCH …/read-all`, `PATCH …/{id}/read` | student | Inbox |
| `GET /api/admin/stats`, `/users`, `/users/{id}`, `/users/{id}/password-hash` | admin | Stats, user management, stored-hash view |
| `POST /api/admin/users`, `PATCH …/role`, `PATCH …/status`, `/reset-password`, `DELETE …/{id}` | admin | Create / change role / block / reset / delete |

Protected routes use `Authorization: Bearer <jwt>`. Role rules are enforced in `SecurityConfig` and checked again in services.

---

## Tests

```bash
cd server
./mvnw test
```

18 tests run against the MySQL test database (`placement_nit_test`, separate from the real one):

- `EligibilityCriteriaTest` — branch/CGPA/backlog/batch rules
- `JwtServiceTest` — token issue/validate
- `ApiIntegrationTest` — login, dashboard, apply, deadlines, notifications, role guards, admin stats, coordinator create, registration

Build the frontend:

```bash
cd client
npm run build
```

---

## Team (Group 17)

| Member | Work |
| --- | --- |
| Manoj (Team Lead) | Backend skeleton, MySQL schema, JWT authentication, integration |
| Chakradhar | Domain model, DAO layer, announcements |
| Raghu | Notifications, email channel, reminders |
| Ram Charan | Admin module, frontend wiring |

Course: **CS1207 Object Oriented Programming Lab**
