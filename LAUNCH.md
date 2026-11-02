# How to Launch Placement NIT

Three things in order: **MySQL → backend → frontend**, then open the browser.

---

## 1. Start MySQL (once per Mac boot)

```bash
brew services start mysql    # or check with: brew services list
mysql -u root -p'manoj@123' -e "SELECT 1;"    # quick credential check
```

## 2. Start the backend

```bash
cd "/Users/manoj/Downloads/oops project/server"
./run.sh
```

- Creates `placement_nit` + all tables + demo data automatically (only when empty), by applying the `database/01-schema.sql` → `02-seed.sql` → `03-frontend-alignment.sql` pipeline.
- Runs **detached** — closing the terminal will not kill it.
- Logs: `tail -f /tmp/placement-nit.log`
- Ready when it prints: `✅ Backend is up on http://localhost:8080`
- Stop it later with:

```bash
cd server && ./stop.sh
```

Quick check without the browser:

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"student@nit.edu","password":"Student@123"}'
# → JSON containing a "token"
```

## 3. Start the frontend (second terminal)

```bash
cd "/Users/manoj/Downloads/oops project/client"
npm install        # first time only (skip if node_modules exists)
npm run dev -- --host 127.0.0.1 --port 5173
```

## 4. Open the app

<http://127.0.0.1:5173>

| Role | Email | Password |
| --- | --- | --- |
| Student | `student@nit.edu` | `Student@123` |
| Coordinator | `coordinator@nit.edu` | `Coord@123` |
| Admin | `admin@nit.edu` | `Admin@123` |

More seeded students: `chakradhar@nit.edu`, `raghu@nit.edu`, `ramcharan@nit.edu`, `ananya@nit.edu`, `rahul@nit.edu` (all `Student@123`).

---

## Run tests

```bash
cd server
JAVA_HOME="$(cd .. && pwd)/.tools/jdk21/Contents/Home" ./mvnw test
```

Uses the separate `placement_nit_test` database — your real data is never touched.

## Troubleshooting

| Problem | Fix |
| --- | --- |
| Frontend: "Cannot reach the server" | Backend not running → `cd server && ./run.sh` |
| `Port 8080 was already in use` | `./stop.sh`, then `./run.sh` (it also clears the port itself) |
| Wrong password shown for MySQL | It is `manoj@123` — see `database/README.md` |
| Want the original demo data back | `mysql -u root -p'manoj@123' -e "DROP DATABASE placement_nit;"` → `./run.sh` (re-seeds from scratch) |
| Backend died | `tail -50 /tmp/placement-nit.log` for its last words, then `./run.sh` |

**Shutdown order:** Ctrl+C the frontend terminal (or close it), then `./stop.sh`.
MySQL can keep running; it's fine.
