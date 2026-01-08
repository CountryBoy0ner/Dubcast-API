# Criterion: API Documentation

## Architecture Decision Record

### Status
**Status:** Accepted  
**Date:** 2026-01-07

### Context
Dubcast exposes a public + authenticated REST API (and WebSocket endpoints).  
The diploma rubric requires a **complete, structured API documentation set** that is:

- easy to navigate (clear structure + naming),
- technically accurate and aligned with the implementation,
- available in an **interactive** form (Swagger UI),
- shipped as a **version-controlled** machine-readable specification (OpenAPI JSON/YAML),
- includes **Getting Started** + at least one **integration tutorial**,
- documents **auth/roles**, **errors**, and advanced usage guidance (e.g., idempotency, versioning policy),
- includes an explicit **documentation strategy** (tools, conventions, limitations).

### Decision
1. Use **springdoc-openapi** to auto-generate an OpenAPI 3.x contract from Spring MVC controllers and publish:
    - Swagger UI: `/swagger-ui/index.html`
    - OpenAPI JSON: `/v3/api-docs`
    - OpenAPI YAML: `/v3/api-docs.yaml`

2. Keep the documentation as a structured, version-controlled Markdown repository under `docs/`, including:
    - high-level API architecture overview,
    - Getting Started guide,
    - runnable cURL examples + step-by-step integration tutorial,
    - reference docs: error handling, versioning policy, changelog,
    - stored exported schemas: `docs/schemas/openapi.json` and `docs/schemas/openapi.yaml`.

### Alternatives Considered

| Alternative | Pros | Cons | Why Not Chosen |
|---|---|---|---|
| Hand-written Markdown only | Fast | Drifts from code, not machine-readable | Fails rubric “complete specification” requirement |
| Springfox Swagger (legacy) | Familiar to some | Legacy/compat issues with modern Spring Boot | springdoc is the current standard |
| API-first (write OpenAPI first, generate stubs) | Strong contract discipline | Higher overhead for a diploma project | Current approach meets rubric with less complexity |

### Consequences
**Positive:**
- Reviewers can explore and test endpoints immediately in Swagger UI.
- OpenAPI schema can be used for client generation, validation, Postman import, etc.
- JWT bearer scheme and role rules are visible in a single, consistent contract.

**Negative:**
- Documentation quality depends on controller annotations/DTO naming.
- Exported `openapi.json/yaml` must be regenerated when API changes (otherwise may drift).

**Neutral:**
- Current REST base path is `/api/**` without a `/api/v1` URL prefix; version is communicated via OpenAPI `info.version = v1` and docs.

---

## Implementation Details

### Dependency (Springdoc)
```xml
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.8.14</version>
</dependency>
```

### OpenAPI config (JWT Bearer)
```java
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI dubcastOpenApi() {
    final String securitySchemeName = "bearerAuth";

    return new OpenAPI()
        .info(new Info()
            .title("Dubcast Radio API")
            .version("v1")
            .description("Dubcast Internet Radio API. Contains public, user, and admin endpoints."))
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
        .components(new Components()
            .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")));
  }
}
```

### Published documentation endpoints
- Swagger UI: `/swagger-ui/index.html`
- OpenAPI JSON: `/v3/api-docs`
- OpenAPI YAML: `/v3/api-docs.yaml`

> Base REST prefix: `/api` (no `/api/v1` prefix yet).  
> In local Docker setup the base URL is typically: `http://localhost:8089/api`.

### Repository documentation structure
```
docs/
├── schemas/
│   ├── openapi.json
│   └── openapi.yaml
├── architecture/
│   └── api-architecture-overview.md
├── guides/
│   ├── getting-started.md
│   └── integration-tutorial.md
├── examples/
│   └── curl-examples.md
└── reference/
    ├── documentation-strategy.md
    ├── error-handling.md
    ├── api-versioning.md
    └── changelog.md
```

### How OpenAPI files are exported
Run the app (locally or via Docker), then export:

```bash
curl -s http://localhost:8089/v3/api-docs > docs/schemas/openapi.json
curl -s http://localhost:8089/v3/api-docs.yaml > docs/schemas/openapi.yaml
```

Windows PowerShell note: use `curl.exe` if `curl` is an alias.

### OpenAPI validation (rubric requirement)
At least one validation method must be used and documented. Recommended commands:

```bash
npx @redocly/cli lint docs/schemas/openapi.yaml
# or
npx @stoplight/spectral-cli lint docs/schemas/openapi.yaml
```

---

## Requirements Compliance Checklist (Diploma Rubric)

### Minimum requirements (passing grade)

| # | Requirement | Status | Evidence/Notes |
|---|---|---|---|
| 1 | Structured, well-organized API documentation set (endpoints, params, req/res formats, auth, errors) | ✅ | `docs/` structure + Swagger UI contract |
| 2 | Complete API specification in industry standard (OpenAPI), validated, version-controlled YAML/JSON | ✅ | `docs/schemas/openapi.yaml` + `docs/schemas/openapi.json` + validation command(s) in `docs/reference/documentation-strategy.md` |
| 3 | Each endpoint documented with meaningful examples: requests, responses, status codes, errors, data models; includes typical + edge cases | ✅ | Swagger UI provides per-endpoint schemas/status codes; curated runnable examples in `docs/examples/curl-examples.md` + edge-case notes in `docs/reference/error-handling.md` |
| 4 | ≥1 Getting Started guide + ≥1 step-by-step integration tutorial | ✅ | `docs/guides/getting-started.md`, `docs/guides/integration-tutorial.md` |
| 5 | High-level architecture overview (context diagram, request flow, modules/resources) | ✅ | `docs/architecture/api-architecture-overview.md` |
| 6 | Published / developer-accessible format (Swagger UI and/or structured Markdown repo) | ✅ | Swagger UI `/swagger-ui/index.html` + `docs/` repository |
| 7 | Written report describes documentation strategy: tools, standards, naming, versioning, formatting, known gaps | ✅ | `docs/reference/documentation-strategy.md` + Known Limitations below |
| 8 | Consistent formatting and naming conventions + clear folder structure for maintainability | ✅ | `docs/{reference,guides,examples,schemas,architecture}` |

### Maximum requirements (highest grade)

| # | Requirement | Status | Evidence/Notes |
|---|---|---|---|
| 1 | Comprehensive system: conceptual guides, onboarding flows, release notes, DX design | ✅ | Guides + changelog + “documentation strategy”; optional landing/TOC recommended |
| 2 | Advanced topics described (pagination, rate limits, auth deep dive, error taxonomy, idempotency, versioning) | ✅/⚠️ | `error-handling.md`, `api-versioning.md`, idempotency section; if rate limits/pagination not implemented, document as “N/A” explicitly |
| 3 | Detailed diagrams (sequence/component/dependency maps/request lifecycle) | ⚠️ | Context + flow present; optional: add one sequence diagram for Login → Radio → WebSocket analytics |
| 4 | Advanced tools/methods (Spectral/Redocly lint, contract testing, mock server, etc.) | ⚠️/✅ | If lint step added to CI: ✅; otherwise keep as manual validation command |

---

## Known Limitations

| Limitation | Impact | Potential Solution |
|---|---|---|
| No `/api/v1` prefix in URLs | Version not visible in URL | Introduce `/api/v2/**` on breaking changes; keep v1 routes during migration |
| Exported OpenAPI files can drift from code | Repo schema may become outdated | Add CI job to re-export + lint OpenAPI and fail on diffs |
| External dependencies affect parser endpoints | Parser can fail if SoundCloud changes/blocks requests | Add retries, timeouts, clearer error codes; cache resolved metadata |

---

## References
- Swagger UI: `/swagger-ui/index.html`
- OpenAPI JSON/YAML: `/v3/api-docs`, `/v3/api-docs.yaml`
- Springdoc dependency: `springdoc-openapi-starter-webmvc-ui`
- Docs folder: `docs/` (schemas, guides, examples, reference, architecture)
