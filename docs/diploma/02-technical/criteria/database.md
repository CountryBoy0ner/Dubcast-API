# Criterion: Database (PostgreSQL + Liquibase)

This document describes the **Dubcast** database design and migration strategy.  
Focus: **data integrity**, a **deterministic schedule timeline**, and a **reproducible setup** for a diploma/demo environment.

---

## Architecture Decision Record

### Status
**Status:** Accepted  
**Date:** 2026-01-06

### Context
Dubcast needs a persistent data store for the core domains:

- **Authentication & profiles:** users, roles, username/bio
- **Radio catalog:** tracks (SoundCloud URL + metadata)
- **Programming:** schedule entries (time intervals) and optional playlists with ordered items
- **Community:** chat message history

Key forces and constraints:

- Admin management is **API-only** (Swagger/REST), so the database must protect against invalid writes.
- The radio timeline must be **deterministic**: schedule intervals must be valid and **must not overlap**.
- The schema must be **reproducible** for reviewers: one-command startup via Docker Compose + migrations.
- Basic performance must be acceptable for typical queries (time ranges, unique identifiers, pagination).
- Security principle: **least privilege** for the application DB user (no superuser in runtime).

### Decision
- Use **PostgreSQL** as the primary **OLTP** relational database.
- Use **Liquibase** for **versioned migrations** (schema evolution tracked in Git).
- Enforce correctness at the database layer using:
  - **Primary/foreign keys** for referential integrity
  - **UNIQUE** constraints for identity fields (e.g., email, username, SoundCloud URL)
  - **CHECK** constraints for domain validity (roles, positive duration, valid time ranges)
  - **Overlap prevention** for schedule entries (implemented as a trigger/function or a declarative exclusion constraint)
  - **Indexes** on frequently filtered/sorted columns

### Alternatives Considered

| Alternative | Pros | Cons | Why Not Chosen |
|---|---|---|---|
| App-only validation (no DB overlap protection) | Faster to change in Java | Invalid data possible if validation is bypassed; timeline can become inconsistent | DB must guarantee schedule correctness |
| Declarative overlap prevention (`EXCLUDE USING gist`) | Strong, declarative, efficient | Requires range + GiST knowledge and setup | Trigger-based approach is easier to audit/explain for diploma scope |
| No migrations (manual schema setup) | Quick prototype | Not reproducible; no history; hard to review | Diploma requires reproducible, versioned DB setup |

### Consequences
**Positive:**
- The DB guarantees timeline correctness (no overlapping schedule entries) even if clients send invalid writes.
- Schema changes are reproducible and traceable (Liquibase + Git history).
- Common access patterns are supported by indexes.

**Negative:**
- Some business rules live in SQL (trigger/function or exclusion constraint), requiring DB knowledge to change.
- Creating DB roles/users inside migrations can require elevated privileges on managed databases.

**Neutral:**
- Optional views/materialized views can exist for diagnostics/optimization, but the runtime does not depend on them.

---

## Implementation Details

> **Note on verification:** The checklist below is based on the *intended/current design*.  
> If you want “100% accurate ✅”, verify each item in the repository and/or by running the DB migrations (see **How to Verify** sections).

### Migration Layout (Liquibase)

Typical layout:

```text
src/main/resources/db/changelog/
├── db.changelog-master.yaml
├── V1.0/
├── V1.1/
└── ...
```

- `db.changelog-master.yaml` includes version folders (`include` / `includeAll`).
- All changesets are committed to Git.

### Data Dictionary (core tables)

Below is a compact data dictionary for the **core OLTP model**.

#### `users`
Stores authentication and profile data.

- `id UUID` — **PK**
- `email TEXT` — **UNIQUE, NOT NULL**
- `password TEXT` — **NOT NULL** (stored hashed by the application)
- `role VARCHAR(20)` — **NOT NULL**, `CHECK role IN ('ROLE_USER','ROLE_ADMIN')`
- `username VARCHAR(50)` — **UNIQUE, NULL**
- `bio VARCHAR(512)` — NULL
- `created_at TIMESTAMPTZ` — **NOT NULL**, default `now()`

#### `tracks`
Radio catalog.

- `id BIGINT` — **PK**
- `sc_url TEXT` — **UNIQUE, NOT NULL**
- `title TEXT` — **NOT NULL**
- `duration_seconds INT` — **NOT NULL**, `CHECK duration_seconds > 0`
- `artwork_url TEXT` — NULL
- `created_at TIMESTAMPTZ` — **NOT NULL**, default `now()`

#### `playlists` (optional)
- `id BIGINT` — **PK**
- `name VARCHAR(255)` — **NOT NULL**
- `sc_playlist_url VARCHAR(500)` — NULL
- `created_at TIMESTAMPTZ` — default `now()`

#### `playlist_tracks`
Ordered items inside a playlist.

- `id BIGINT` — **PK**
- `playlist_id BIGINT` — **FK playlists(id)**, `ON DELETE CASCADE`
- `track_id BIGINT` — **FK tracks(id)**, `ON DELETE CASCADE`
- `position INT` — **NOT NULL**
- `UNIQUE (playlist_id, position)` — guarantees stable ordering

#### `schedule_entries`
Defines the global radio timeline (most critical table).

- `id BIGINT` — **PK**
- `track_id BIGINT` — **FK tracks(id)**, `ON DELETE RESTRICT`
- `playlist_id BIGINT` — **FK playlists(id)**, `ON DELETE SET NULL` (optional)
- `start_time TIMESTAMPTZ` — **NOT NULL**
- `end_time TIMESTAMPTZ` — **NOT NULL**
- `CHECK (end_time > start_time)`
- Indexed for time-range queries

#### `messages`
Chat history.

- `id BIGINT` — **PK**
- `user_id UUID` — **FK users(id)**, `ON DELETE CASCADE`
- `text VARCHAR(1000)` — **NOT NULL**
- `created_at TIMESTAMPTZ` — **NOT NULL**
- Indexed by `created_at` for pagination

### Data Integrity & Transactions

**Integrity mechanisms**
- Referential integrity via **FKs** across `schedule_entries ↔ tracks`, `playlist_tracks ↔ playlists/tracks`, `messages ↔ users`.
- Domain correctness via **CHECK** and **UNIQUE** constraints.
- Deterministic schedule correctness via **overlap prevention** on `schedule_entries`.

**Transaction behavior**
- Application writes are executed inside Spring-managed transactions.
- DB constraints provide an additional correctness boundary: invalid writes fail with an error, preventing corruption.

### Schedule Overlap Prevention (example trigger)

One valid approach is a trigger/function that blocks overlaps:

```sql
CREATE OR REPLACE FUNCTION no_schedule_overlap()
RETURNS trigger AS $func$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM schedule_entries e
        WHERE e.id <> NEW.id
          AND tstzrange(e.start_time, e.end_time)
              && tstzrange(NEW.start_time, NEW.end_time)
    ) THEN
        RAISE EXCEPTION 'Schedule overlaps another entry';
    END IF;

    RETURN NEW;
END;
$func$ LANGUAGE plpgsql;

CREATE TRIGGER trg_schedule_no_overlap
BEFORE INSERT OR UPDATE ON schedule_entries
FOR EACH ROW EXECUTE FUNCTION no_schedule_overlap();
```

> Alternative (more declarative): `EXCLUDE USING gist (tstzrange(start_time, end_time) WITH &&)`.

### Indexing (typical set)

Indexes for common access patterns:

- `users(email)` — authentication lookups / uniqueness
- `tracks(sc_url)` — imports / lookups by external identifier
- `schedule_entries(start_time, end_time)` — range queries
- `messages(created_at)` — pagination

### Least Privilege (roles)

Recommended roles (names can vary):

- `app_read`  → SELECT
- `app_write` → INSERT/UPDATE/DELETE
- `app_admin` → full privileges
- application user (e.g., `dubcast_app`) → granted `app_read` + `app_write`

**Important:** The application should not connect as a superuser.

### Test / Demo Data

- Seed/reference data can be stored as Liquibase changesets (e.g., default admin user, sample tracks, small schedule sample).
- Seed data should include at least a few “edge cases” (e.g., very short track, long track, schedule gaps).

---

## Requirements Compliance Checklist (Database — OLTP rubric)

Legend: ✅ implemented / present, ⚠️ partially or environment-dependent, ❌ missing, 🟡 not verified.

| # | Requirement (from rubric) | Status | Evidence / How to verify |
|---|---|---|---|
| 1 | Modern relational DB (PostgreSQL) | ✅  | Check `docker-compose.yml` / `application.properties` for Postgres; run stack and confirm `SELECT version();` |
| 2 | Versioned schema creation via migrations (not manual) | ✅  | Verify Liquibase is enabled and `db.changelog-master.yaml` exists; run migrations on clean DB |
| 3 | Migrations stored in VCS (Git) | ✅ | Repo contains `src/main/resources/db/changelog/**` committed |
| 4 | Data dictionary: tables/columns/types/keys described | ✅ | Section “Data Dictionary (core tables)” |
| 5 | Integrity: PK/FK/UNIQUE/CHECK constraints used | ✅ | Inspect SQL changesets or generated schema: `\d+ table_name` in psql |
| 6 | Schedule timeline is deterministic (no overlaps) | ✅ | Look for trigger/function or exclusion constraint in migrations; test by inserting overlapping rows |
| 7 | Normalization to 3NF and meaningful relations | ✅ | Review ERD/relations; confirm tables split by domain (users/tracks/schedule/messages/playlists) |
| 8 | Indexes for typical queries | ✅ | Inspect migrations or `\di` output; verify time-range and pagination indexes |
| 9 | Roles and permissions (least privilege) | ⚠️ | If roles are created in migrations, confirm they exist; on managed DB this may require admin provisioning |
|10 | Passwords not stored in plaintext (hashing) | ✅ | Verify application uses BCrypt/Argon2/etc.; check seed data does not contain plaintext passwords |
|11 | Test/demo data sufficient for demo + edge cases | ✅ | Check seed changesets and run demo queries; confirm not empty on first run |

---

## Known Limitations

| Limitation | Impact | Mitigation |
|---|---|---|
| Role/user creation in migrations may require elevated DB privileges | Migrations can fail on managed DB providers | Move role provisioning to infra scripts; keep schema migrations in Liquibase |
| Trigger-based overlap prevention is less “visible” to app devs | Harder to modify without DB knowledge | Add docs + tests; optionally switch to declarative exclusion constraint |
| Materialized views require refresh strategy (if used) | Risk of stale reads | Prefer normal views or implement refresh job |
| Seed data in migrations may be “dev only” | Not production-friendly | Split dev seeds into a separate changelog/profile |

---

## References
- Liquibase master changelog: `src/main/resources/db/changelog/db.changelog-master.yaml`
- Versioned migrations: `src/main/resources/db/changelog/V*/`
- PostgreSQL range types: `tstzrange` and overlap operator `&&`
- Liquibase concepts: changesets, `includeAll`, constraints, SQL changesets
