# Criterion: CI/CD Pipeline (GitHub Actions + GHCR)

This document describes the **Dubcast CI/CD** pipeline implemented with **GitHub Actions** and publication of a **Docker image** to **GHCR** (GitHub Container Registry).

The pipeline is designed to satisfy the diploma rubric goals: **automation**, **quality gates**, **artifacts for reviewers**, **basic security scanning**, and **reproducible delivery**.

---

## Architecture Decision Record

### Status
**Status:** Accepted  
**Date:** 2026-01-07

### Context (problem)
The project must demonstrate modern engineering culture:

- Every Pull Request must be validated automatically (**CI**).
- Code style must be enforced consistently (no “it works on my machine”).
- Unit tests and coverage gate must prevent regressions.
- Reviewers must have evidence (reports/artifacts).
- The delivered application must be reproducible and runnable by others (**Docker image**).
- Secrets must not be stored in the repository.
- Minimal static security analysis must exist.

Constraints:
- Single-developer diploma scope → pipeline must remain simple and stable.
- Backend is **Java 17 + Maven** and uses **Playwright** at runtime for SoundCloud playlist parsing.
- No mandatory external infra (Kubernetes/cloud) is required by the rubric.

### Decision (what we chose)
Implement a GitHub Actions workflow with two logical parts:

1) **CI (build validation)** — runs on every PR and on pushes to main branches:
- Formatting gate (**Spotless**)
- Build + unit tests + coverage gate (**JaCoCo**) via Maven lifecycle
- Upload evidence artifacts (test/coverage reports, optional build output)
- Static security scan (**Trivy FS**)

2) **CD (delivery)** — runs only after CI succeeds, on push to `main` (and optionally manual trigger):
- Build and **push Docker image** to GHCR (reproducible delivery artifact)
- Scan the **final image** (**Trivy image scan**) to validate shipped layers

> Local builds and CI use **Maven Wrapper** (`./mvnw`) for reproducibility (same Maven version on any machine).  
> If `mvn` is not available on a developer machine, using `./mvnw clean verify` is expected and correct.

### Alternatives Considered
| Alternative | Pros | Cons | Why Not Chosen |
|---|---|---|---|
| “Build only” without formatting/tests | Fast | No quality assurance; violates rubric | Must show CI quality gates |
| Build and publish Docker image for every PR | Early container validation | Slower; registry noise | Publishing only on main is cleaner |
| Only one security scan | Simpler | Misses either repo manifests or final image layers | Use FS scan + image scan |
| Add full auto-deploy to a server | Full DevOps | Needs infra/secrets/ops | Out of scope; publishing image is enough for diploma |

### Consequences
**Positive**
- PRs cannot be merged with broken tests or failing style.
- Coverage gate enforces minimum test quality.
- Reviewers can download reports and verify results.
- Docker image in GHCR allows simple “run anywhere” delivery.

**Negative**
- Security scanners may fail due to upstream CVEs (even if not exploited in this demo).
- Playwright runtime makes the image larger and slightly slower to build/pull.

**Neutral**
- CD publishes an artifact (image) but does not auto-deploy; deployment is a documented manual step.

---

## CI/CD Pipeline Diagram

```text
Pull Request / Push
        │
        ▼
  CI: Code check + build + tests + reports + security
        │
        ├─ Format gate (Spotless)
        ├─ Build (Maven)
        ├─ Unit tests (Surefire)
        ├─ Coverage gate (JaCoCo check)
        ├─ Upload artifacts (reports / optional JAR)
        └─ Trivy FS scan (repo/deps/config)
        │
        ▼
CD (only on main, after CI success)
        │
        ├─ Build Docker image (Buildx)
        ├─ Push image to GHCR (tags: sha + latest)
        └─ Trivy image scan (final shipped artifact)
```

---

## Implementation Details

### Tooling
- **CI/CD system:** GitHub Actions
- **Registry:** GHCR (GitHub Container Registry)
- **Build system:** Maven (via **Maven Wrapper**: `./mvnw`)
- **Quality gates:** Spotless formatting + JaCoCo coverage check
- **Security scanning:** Trivy (filesystem scan + image scan)
- **Container build:** Docker Buildx (with cache)

Workflow file lives under:
- `.github/workflows/` (for example `backend-ci.yml` / `ci.yml`)

### What is automated and why
| Step | Why it exists |
|---|---|
| Spotless check | Prevents style drift; fail fast before tests |
| Build + tests | Ensures code compiles and business logic is validated |
| JaCoCo gate | Forces a minimum test coverage level for service/business logic |
| Artifacts upload | Reviewer-friendly evidence and easier debugging of CI failures |
| Trivy FS scan | Finds vulnerable dependencies/configs early (PR stage) |
| Docker build + push | Produces reproducible artifact that others can run |
| Trivy image scan | Validates what will actually run (OS packages + layers) |

### Environments (Dev/Test/Prod)
Even without “real” staging infrastructure, the pipeline uses clear environments:

- **Dev**: local development (IDE + Docker Compose). Developers run:
  - `./mvnw clean verify` locally
  - `docker compose up -d` for local run
- **Test**: GitHub Actions runners (ephemeral). This is where CI runs:
  - formatting + unit tests + coverage gate + scans
- **Prod (delivery)**: published Docker image in GHCR (immutable artifact):
  - can be run on any server/VM/container host (or Azure/AWS/GCP later)
  - deployment is “pull image + run docker compose / docker run”

### Artifacts (what is produced)
Typical artifacts provided to reviewers (depending on workflow settings):

- **Test reports**: `target/surefire-reports/`
- **Coverage report**: `target/site/jacoco/` (HTML)
- **Build output** (optional): JAR from `target/*.jar`
- **Published delivery artifact**: Docker image in GHCR (`ghcr.io/<owner>/<repo>:latest` and `:<sha>`)

### Why two Trivy scans
- **FS scan** checks repository contents (dependency manifests, configs, IaC files).
- **Image scan** checks the final container image layers (OS packages + app layers).

This gives coverage of both “source inputs” and “shipped artifact”.

---

## Requirements Compliance Checklist (Diploma Rubric)

> Legend: ✅ implemented, ⚠️ partially, ❌ missing

### Documentation requirements
| # | Requirement | Status | Evidence / Notes |
|---:|---|:---:|---|
| 1 | CI/CD diagram and explanation of stages | ✅ | Diagram in this document + stage breakdown |
| 2 | Tool described (GitHub Actions) | ✅ | “Tooling” section |
| 3 | What is automated and why | ✅ | “What is automated and why” section |
| 4 | List of artifacts (images/reports/build output) | ✅ | “Artifacts” section |
| 5 | Environments described (Dev/Test/Prod) | ✅ | “Environments” section |

### CI/CD architecture requirements
| # | Requirement | Status | Evidence / Notes |
|---:|---|:---:|---|
| 1 | CI has ≥ 3 stages: code check → build → tests | ✅ | Spotless → Maven build → unit tests |
| 2 | CD exists (at least one type) | ✅ | Docker image published to GHCR (delivery artifact) |
| 3 | Runs automatically on push + PR + optional manual | ✅ | `on: push`, `on: pull_request`, optional `workflow_dispatch` |
| 4 | Dependency caching | ✅ | Maven cache via `actions/setup-java` (cache: maven) |
| 5 | Pipeline fails on build/tests errors | ✅ | GitHub Actions step failure stops job |
| 6 | Reports/visibility of results | ✅ | Uploaded surefire + JaCoCo artifacts |
| 7 | Secrets stored securely | ✅ | Uses GitHub Secrets / `GITHUB_TOKEN` for registry auth |
| 8 | Static security analysis present | ✅ | Trivy FS scan; plus image scan as extra |

---

## Operational Notes (how to run the delivered artifact)

### Pull and run
Example (Docker):
```bash
docker pull ghcr.io/<owner>/<repo>:latest
docker run --rm -p 8089:8080 ghcr.io/<owner>/<repo>:latest
```

### Typical health verification (recommended)
If you expose Actuator health:
```bash
curl -f http://localhost:8089/actuator/health
```

---

## Known Limitations / Possible Improvements

| Limitation | Impact | Improvement (optional) |
|---|---|---|
| No automatic deploy to a public server | Reviewer must run the image manually | Add optional deploy job via SSH (docker compose pull + up) |
| Reports may not upload if job fails early | Harder to debug failing PRs | Upload reports with `if: always()` |
| PR does not validate Docker build (if CD only on main) | Container issues appear only after merge | Add PR job “build image (no push)” |
| Security scan thresholds may be too strict | Pipeline can fail due to upstream CVEs | Use allowlist / ignore-unfixed policy with justification |

---

## References
- Workflow: `.github/workflows/*.yml` (CI/CD pipeline definition)
- Maven config: `pom.xml` (Spotless + JaCoCo)
- Docker build: `Dockerfile`
- Registry: GHCR image tags `latest` and commit `sha`
