# 4. Retrospective

This section reflects on the Dubcast development process, lessons learned, and future improvements.

## What Went Well ✅

### Technical Successes
- Implemented a clear layered backend architecture (Controller → Service → Repository) with consistent DTO boundaries.
- Delivered real-time features (chat + live “now playing” updates) via WebSocket/STOMP with a clean topic/app separation.
- Used Liquibase migrations to make the database reproducible and version-controlled.
- Provided interactive API documentation (Swagger UI + OpenAPI export) to simplify testing and reviewing.
- Containerized the stack with Docker Compose so reviewers can run it with minimal setup.
- Added CI quality gates (format check, unit tests, coverage gate) and security scanning to keep the build reliable.

### Process Successes
- Kept scope controlled by prioritizing “core listener experience” first (radio, now playing, chat, profile).
- Used incremental delivery: build a minimal working flow → improve reliability → document and test.
- Documented decisions as ADRs to keep architecture choices explainable during review/defense.

### Personal Achievements
- Strengthened full-stack delivery skills: Spring Boot API design, Angular SPA structure, Docker/Compose runtime setup, and CI/CD automation.
- Improved ability to translate rubric requirements into concrete, verifiable deliverables.

## What Didn’t Go As Planned ⚠️

| Planned | Actual Outcome | Cause                                           | Impact |
|---------|---------------|-------------------------------------------------|--------|
| Full “production-like” deployment automation | Delivery is reproducible via container image + Compose, | time                                            | Medium |
| Broad automated test coverage (FE + BE) | Solid service-layer tests, but FE coverage remains minimal | Time constraints + prioritizing core features   | Medium |
| Strong mobile validation | Responsive layout exists, but real-device testing is limited | Limited testing time/devices  | Medium |
| Advanced observability (metrics, dashboards) | Basic health checks exist, but no monitoring stack | Out of scope for MVP                            | Low |

### Challenges Encountered
1. **WebSocket reliability across environments**
    - Problem: WS requires correct proxying/routing in dev/prod.
    - Impact: Real-time features can break if networking is misconfigured.
    - Resolution: Documented proxy setup and added health checks + clear endpoints.

2. **Keeping documentation aligned with implementation**
    - Problem: Docs can drift as endpoints and flows evolve.
    - Impact: Reviewers may see inconsistencies if docs are outdated.
    - Resolution: Centralized API docs via OpenAPI and structured docs folders to reduce drift.

## Technical Debt & Known Issues

| ID | Issue | Severity | Description | Potential Fix |
|----|-------|----------|-------------|---------------|
| TD-001 | Limited end-to-end coverage | Medium | Only smoke E2E tests exist; many user flows are not covered automatically | Add Playwright scenarios (auth flow, chat send/receive, now playing refresh) |
| TD-002 | WebSocket security hardening | Medium | Dev-friendly settings (e.g., broad WS origins) are not strict enough for public deployment | Restrict origins, add stronger auth rules for WS if needed |
| TD-003 | Documentation validation in CI | Low | OpenAPI export and docs consistency are not automatically checked on every change | Add CI step to validate OpenAPI and fail on breaking diffs |

### Code Quality Issues
- Some configuration and environment settings could be consolidated and documented more strictly.
- Test coverage is strong in core backend logic but not uniform across all modules.
- More “contract-like” tests between FE and BE would reduce integration regressions.

## Future Improvements (Backlog)

### High Priority
1. **Expand automated tests**
    - Description: Add more FE E2E scenarios and backend integration checks for critical flows.
    - Value: Higher confidence during refactors and before releases.
    - Effort: Medium

2. **Production-ready configuration profile**
    - Description: Add separate Compose/profile for production-like settings (strict CORS/WS origins, clearer env separation).
    - Value: Safer and easier deployment story.
    - Effort: Medium

### Medium Priority
3. **UX polishing and accessibility**
    - Description: Improve mobile layout details, loading states, and user-facing error messages.
    - Value: Better developer experience for reviewers and real users.

4. **Observability baseline**
    - Description: Add lightweight metrics/log structure and basic dashboards (optional).
    - Value: Easier debugging and performance insights.

### Nice to Have
5. Optional admin UI (instead of API-only management).
6. More advanced caching and performance tuning on frequently requested endpoints.
7. Release automation (automatic changelog generation / tagged releases).

## Lessons Learned

### Technical Lessons

| Lesson | Context | Application |
|--------|---------|-------------|
| Clear layering reduces complexity | Services stayed testable and controllers stayed thin | Keep strict boundaries and DTO contracts |
| Event-driven notifications improve consistency | Broadcasting after commit avoids inconsistent UI updates | Prefer events for “write → notify” flows |
| Reproducibility matters in evaluation | Docker + migrations + CI made setup predictable | Treat “run anywhere” as a first-class feature |

### Process Lessons

| Lesson | Context | Application |
|--------|---------|-------------|
| Scope control is critical | Many “nice-to-have” items compete with core delivery | Lock MVP early and iterate |
| Documentation must be maintained like code | Docs drift quickly without structure | Use OpenAPI as source of truth + structured docs |

### What Would Be Done Differently

| Area | Current Approach | What Would Change | Why |
|------|-----------------|-------------------|-----|
| Planning | Feature-first delivery | Start with rubric checklist + verification plan earlier | Less rework late in the project |
| Technology | Minimal monitoring | Add a small observability baseline earlier | Faster debugging and clearer evidence |
| Process | Docs improved later | Integrate docs validation into CI earlier | Prevent documentation drift |
| Scope | Admin is API-only | Decide earlier whether an admin UI is required | Avoid ambiguity during review |

## Personal Growth

### Skills Developed

| Skill | Before Project | After Project |
|-------|---------------|---------------|
| Spring Boot architecture & security | Intermediate | Advanced |
| Angular SPA structure + real-time integration | Intermediate | Advanced |
| Docker/Compose for reproducible environments | Beginner/Intermediate | Intermediate/Advanced |
| CI/CD with quality gates | Beginner | Intermediate |

### Key Takeaways
1. Reproducibility (Docker + migrations + CI) is as important as features in a diploma review.
2. Real-time UX requires careful environment/config handling (especially networking/proxying).
3. Architecture decisions are easiest to defend when they’re written down and tied to requirements.

---

*Retrospective completed: 2026-01-07*
