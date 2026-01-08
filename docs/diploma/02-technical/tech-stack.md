# Technology Stack

This document lists the technologies used to build **Dubcast** and briefly explains why each choice was made.

## Stack Overview

| Layer | Technology | Version | Justification |
|-------|------------|---------|---------------|
| **Frontend** | Angular (SPA) | 21.0.x | Modern TypeScript SPA for interactive UI (radio player, chat, profile). |
| **UI / Styling** | PrimeNG + PrimeIcons + SCSS | PrimeNG 21.0.2, PrimeIcons 7.0.0 | Ready UI components + icons with lightweight custom styling; fits diploma scope and speeds up UI delivery. |
| **Backend** | Java | 17 | LTS runtime with strong tooling and ecosystem for backend development. |
| **Backend Framework** | Spring Boot | 3.5.6 | Rapid development for REST APIs, security, validation, and OpenAPI/Swagger support. |
| **Database** | PostgreSQL | 16 (Docker image: 16-alpine) | Reliable relational DB for structured entities (users, tracks, playlists, schedule, chat) and strong constraints. |
| **ORM / Data Access** | Spring Data JPA (Hibernate) | via Spring Boot 3.5.6 | Standard persistence approach in Spring ecosystem; reduces boilerplate for CRUD and supports validations/transactions. |
| **DB Migrations** | Liquibase | via Spring Boot 3.5.6 | Versioned schema changes and reproducible DB setup in Docker. |
| **API Documentation** | springdoc OpenAPI + Swagger UI | 2.8.14 | Auto-generated interactive API docs; used for testing and admin CRUD (API-only). |
| **Authentication** | Spring Security + JWT (jjwt) | jjwt 0.11.5 (Security via Spring Boot 3.5.6) | Standard RBAC security for USER/ADMIN endpoints with stateless auth. |
| **Testing** | JUnit 5 + Mockito + Playwright | JUnit 5.11.0, Mockito 5.20.0, Playwright 1.49.0 (Java) / 1.57.0 (UI) | Unit tests for service layer, plus browser-driven checks for UI/API flows. |
| **Code Quality** | Spotless + JaCoCo | Spotless 2.43.0, JaCoCo 0.8.12 | Enforces consistent formatting and coverage gate for service layer. |
| **Containerization / Deployment** | Docker + Docker Compose | - | One-command reproducible environment for reviewers (DB + backend + configs). |
| **CI/CD** | GitHub Actions + GHCR + Trivy | Trivy action 0.28.0 | Automated format/tests, artifact uploads, container build & publish, and vulnerability scanning (repo + image). |

## Key Technology Decisions

### Decision 1: Spring Boot (Java 17) for backend

**Context:** The project needs a secure REST backend with scheduling logic, validation, and easy API documentation for admin operations.

**Decision:** Use **Spring Boot** with **Java 17**.

**Rationale:**
- Fast development with mature ecosystem (Spring Web, Security, Validation).
- Built-in support for OpenAPI/Swagger UI for API-first admin operations.
- Strong testing support (Spring Boot Test, JUnit) and easy CI integration.

**Trade-offs:**
- Pros: well-documented, stable, scalable architecture patterns.
- Cons: more configuration and “framework weight” compared to minimal Node/Express setups.

---

### Decision 2: Angular SPA for the listener UI

**Context:** The listener UI needs interactive features (playback screen, chat, profile) and frequent updates (“now playing”, online counter).

**Decision:** Use **Angular** as a **single-page application (SPA)**.

**Rationale:**
- TypeScript-first development and structure (modules/components).
- Good fit for dynamic UI updates and real-time features.
- Easy to build and serve as static assets for deployment.

**Trade-offs:**
- Pros: strong structure and tooling.
- Cons: heavier bundle/runtime than server-rendered templates.

---

### Decision 3: PostgreSQL + DB-level constraints for schedule correctness

**Context:** Scheduling mistakes (overlaps/invalid ranges) must be prevented even when schedule is managed manually via API.

**Decision:** Store scheduling data in **PostgreSQL** and enforce correctness using DB constraints/overlap protection.

**Rationale:**
- Data integrity is guaranteed at the persistence layer.
- Prevents invalid intervals regardless of client or admin mistakes.
- Works well with Liquibase migrations.

**Trade-offs:**
- Pros: strong consistency and safety.
- Cons: some logic lives in DB (harder to change than pure application logic).

---

### Decision 4: Docker Compose for reproducible demo environment

**Context:** Reviewers should be able to run the system without manual DB installation/configuration.

**Decision:** Use **Docker + Docker Compose** to run backend + PostgreSQL.

**Rationale:**
- Reproducible setup (same versions, same configuration).
- Simple start/stop commands.
- Matches “deployment-like” environment for demos.

**Trade-offs:**
- Pros: predictable environment.
- Cons: requires Docker installed; slightly higher startup overhead than local run.

---

### Decision 5: GitHub Actions + Trivy for CI quality gates

**Context:** Diploma criteria require engineering culture: automated checks, reproducible builds, and security scanning.

**Decision:** Implement CI with **GitHub Actions** and security scanning with **Trivy**.

**Rationale:**
- CI runs formatting checks, builds, tests, and uploads artifacts.
- Container image is published to **GHCR** for reproducible deployment.
- Trivy detects HIGH/CRITICAL (repo) and CRITICAL (image) vulnerabilities (depending on configured severity).

**Trade-offs:**
- Pros: automation reduces regressions and manual work.
- Cons: pipeline maintenance; scans can fail builds when vulnerabilities are found.

## Development Tools

| Tool | Purpose | Notes |
|------|---------|-------|
| **IntelliJ IDEA / VS Code** | Development IDEs | IntelliJ typically for backend; VS Code often used for frontend. |
| **Git + GitHub** | Version control | Branches: `main` and `develop`; PR checks on changes. |
| **Maven** | Backend build/deps | Used in CI with caching. |
| **npm** | Frontend build/deps | Used for Angular project dependencies. |
| **Spotless** | Backend formatting gate | Enforced in CI (`spotless:check`). |
| **JUnit 5 + Spring Boot Test** | Backend tests | Run in CI (`mvn clean verify`). |
| **Playwright** | E2E tests (UI) | Used to validate key UI flows. |
| **Swagger UI / OpenAPI** | API documentation/testing | Primary interface for admin (API-only management). |

## External Services & APIs

| Service | Purpose | Pricing Model |
|---------|---------|---------------|
| **SoundCloud (pages / oEmbed)** | Metadata source for track/playlist import flows | External service (public endpoints; may change over time). |
