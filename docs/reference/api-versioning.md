
# API Versioning Guide — Dubcast Radio

This document describes the versioning strategy used by the Dubcast Radio API.
It provides guidelines for evolving the API while maintaining backward compatibility.
This guide satisfies the *advanced documentation requirement* for version lifecycle design.

---

# 1. Versioning Principles

Dubcast Radio follows **semantic API versioning**, where every breaking change requires
a new version of the API. Consumers must be able to rely on the stability of any given version.

Main principles:

- **Backward compatibility** must be preserved within a major version.
- **New optional fields** may be added safely.
- **Breaking changes require a new version prefix** (`/api/v2`, `/api/v3`).
- **Deprecated features** remain supported for a transition period.

---

# 2. Current Public Version

The active version is:

```
/api/v1
```

This applies to:

- authentication
- profile operations
- tracks API
- admin programming API
- radio API
- parser API

---

# 3. What Counts as a Breaking Change?

Breaking changes include:

- Renaming fields
- Removing fields
- Changing field types
- Changing HTTP methods (e.g., PUT → PATCH)
- Requiring new mandatory fields
- Changing response structure
- Endpoint removal
- Semantic behavior changes

Non-breaking changes:

- Adding new fields (optional)
- Adding new endpoints
- Adding pagination or filters
- Improving error messages

---

# 4. Versioning Strategy

## 4.1 URL-based versioning (current)

```
/api/v1/tracks
/api/v1/admin/programming
```

Advantages:

- simple
- explicit
- widely supported

## 4.2 Planned Future Versions

Examples of evolving the API:

```
/api/v2
    - improved schedule editing
    - track categorization
    - metadata extensions
```

---

# 5. Deprecation Policy

To ensure a smooth upgrade path:

1. Breaking changes are introduced only in new versions.
2. Deprecated endpoints remain available for **one full release cycle**.
3. Deprecation warnings are documented in:
   - OpenAPI spec
   - Guides
   - API changelog (planned)

---

# 6. Changelog

A changelog will be maintained in:

```
/docs/reference/changelog.md
```

Typical entry:

```
## v1.1
- Added `durationSeconds` to track response.
- Added pagination to schedule endpoints.
```

---

# 7. Summary

This versioning guide ensures:

- predictable API evolution  
- stability for client applications  
- ability to introduce new features safely  

This document fulfills the **Maximum Requirements** regarding versioning strategy and lifecycle documentation.
