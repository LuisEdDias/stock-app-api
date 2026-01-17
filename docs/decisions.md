# Architectural Decisions

## ADR-001: Introduction of a ROOT User

### Context

The system requires a secure and deterministic way to manage permissions and administrative access. Relying solely on dynamic role or group assignment creates a chicken-and-egg problem during initial system setup.

### Decision

A single ROOT user will be introduced as a bootstrap authority.

* The ROOT user has unrestricted access.
* The ROOT user exists solely to initialize and recover administrative control.

### Consequences
#### Positive

* Simplifies initial system bootstrap.
* Prevents accidental lockout of administrative access.

#### Trade-offs

* Requires strict safeguards to avoid misuse.

---

## ADR-002: Separation Between Roles, Groups, and Permissions

### Context

Traditional role-based models are insufficient for fine-grained access control and scalability.

### Decision

The authorization model will be composed of:

1. Roles: High-level identity classification (e.g., ROOT, ADMIN, USER).
2. Groups: Logical collections of permissions.
3. Permissions: Atomic actions enforced by the system.

* **Inheritance:** Users inherit permissions cumulatively from all groups they belong to.

---

## ADR-003: ROOT User Bootstrap via Environment Configuration

### Context

Avoiding hardcoded credentials while ensuring system recoverability.

### Decision

ROOT credentials will be supplied exclusively via environment variables on first startup.

* If a ROOT user already exists, bootstrap is skipped.
* If no credentials are provided, startup fails.

### Consequences

* Enforces secure operational practices.
* Requires deployment environments to be properly configured.

---

## ADR-004: Immutable ROOT Role

### Context

Allowing modification of ROOT privileges would undermine system security.

### Decision

* ROOT role assignment is internal and non-modifiable.
* APIs shall explicitly block attempts to create, delete, or update ROOT users.

---

## ADR-005: ROOT User Recovery and Bootstrap Strategy

### Context

The system must guarantee administrative access under failure scenarios such as server restarts, database loss, or deployment to new environments. Relying exclusively on persisted database records for the ROOT user creates a single point of failure and may lead to permanent administrative lockout.

At the same time, storing ROOT credentials in source code, versioned configuration files, or database seeds introduces severe security risks.

### Decision

The ROOT user shall follow a **bootstrap and recovery strategy** based on external configuration:

* The ROOT user is persisted in the database for auditability and traceability.
* ROOT credentials are never stored in source code or versioned configuration files.
* On application startup:

    * If a ROOT user exists, no action is taken.
    * If a ROOT user does not exist:

        * Credentials are read from secure environment variables.
        * A ROOT user is automatically created.
        * If required environment variables are missing, application startup fails.

The ROOT user:

* Is unique (single instance).
* Cannot be created, deleted, or modified via API endpoints.
* Bypasses group and permission checks by design.

### Consequences
#### Positive

* Ensures recoverability after catastrophic database loss.
* Prevents permanent administrative lockout.
* Enforces secure operational practices via external secret management.

#### Trade-offs

* Requires deployment environments to be correctly configured before startup.

This approach balances security, operational resilience, and auditability while maintaining a clear separation between bootstrap authority and regular authorization logic.

---

## ADR-006: Use UUID as External User Identifier

### Context

The system exposes a REST API secured by JWT-based authentication.
Users are persisted in a relational database and are referenced internally by a database-generated primary key.

As part of the security and architectural refactoring, the application must define how users are identified:

* internally (database, relationships, performance)
* externally (API, JWT tokens, logs, integrations)

In particular, the JWT `sub` (subject) claim must uniquely and safely identify the authenticated user without exposing sensitive or implementation-specific details.

### Decision

The system will use **two distinct identifiers for users**:

1. **Internal Identifier**
* Type: `Long`
* Purpose: database primary key, internal relationships, joins, and performance
* Visibility: **internal only**, never exposed outside the backend

2. **External Identifier**
* Type: `UUID`
  * Purpose:
  * JWT `sub` claim
  * Public API paths
  * Logs and audit trails
  * Future integrations and distributed scenarios
* Visibility: exposed to clients and external systems

The JWT `sub` claim will contain the user’s **UUID external identifier**, not the database ID.

---

## ADR-007: Use a Custom Authenticated Principal Instead of JPA Entities

### Context

The application uses JWT-based authentication with Spring Security. Initially, authenticated users were resolved from the database and exposed directly as JPA entities (`User`) via the Spring Security context. Controllers relied on `@AuthenticationPrincipal User` mainly for logging and contextual information.

### Decision

The application will use a lightweight, immutable object (e.g. `AuthenticatedUser`) instead of exposing JPA entities through Spring Security.

The custom principal will:

* Contain only identity-related data (publicId, username, role, flags)
* Be constructed during JWT conversion
* Be used consistently in controllers via `@AuthenticationPrincipal`

### Consequences
#### Positive

* Decouples authentication from persistence
* Allows authentication without database access
* Prevents leaking JPA entities into the web and security layers
* Improves testability and architectural clarity

#### Trade-offs

* Requires minor refactoring of controllers and tests
* Introduces an additional abstraction layer

This decision enables future extensions such as service-to-service authentication, client credentials, and richer authorization models without impacting domain entities.

---

## ADR-008: Permission Control System Implementation

### Context

ADR-002 established the conceptual separation between **Roles, Groups, and Permissions** to enable fine-grained and scalable authorization.

As the system evolved, it became necessary to define how this model is implemented in practice, ensuring that:

* Authorization is **stateless**
* Authentication and authorization rely **exclusively on JWT**
* The ROOT user has a **global permission bypass**
* Permission checks are **declarative and centralized**
* No authorization logic leaks into controllers or domain services

Without a clear implementation strategy, the system would risk inconsistent enforcement, database coupling, and scattered conditional logic.

### Decision

The permission control system is implemented according to the following rules:

1. **Permissions as Atomic Authorities**

   * Permissions follow the convention defined in `permissions.md`
   * Each permission is represented as an immutable string (`DOMAIN:ACTION`)
   * Permissions are treated as `GrantedAuthority` instances

2. **Groups as Permission Aggregators**

   * Security Groups are persisted entities
   * Groups aggregate permissions
   * Users may belong to multiple groups
   * Effective permissions are resolved **at token issuance time**

3. **JWT as the Single Source of Truth**

   * JWTs contain:
     * `sub`: user public UUID
     * `role`: high-level classification (ROOT, ADMIN, USER)
     * `perm`: list of effective permissions
  
   * No database access occurs during authentication or authorization

4. **ROOT as an Absolute Authority**

* ROOT is represented by a dedicated authority (`ROLE_ROOT`)
* ROOT bypasses all permission checks by design
* Bypass logic is centralized within the security layer

5. **Declarative Authorization via Annotation**

* Access control is enforced using a custom annotation:

```bash
@RequiresPermission("ITEM:CR")
```

* The evaluation rule is:

```bash
ROLE_ROOT OR <REQUIRED_PERMISSION>
```
* **Flow:** Auth Check -> ROOT Bypass -> Annotation Lookup -> Permission Match
* **Deny-by-Default:** Access is denied if any annotation is found
* Public methods must be annotated with a public annotation, otherwise, access will be denied:
```bash
@PublicAccess
```
* Controllers and services must not contain conditional logic related to ROOT or permissions

6. **Custom Authenticated Principal**

* The authenticated principal is a lightweight immutable object (`AuthenticatedUser`)

* JPA entities are not exposed through the security context

* Controllers access the authenticated user via `@AuthenticationPrincipal`

### Consequences
#### Positive

* Fully stateless authorization
* Authentication independent of database availability
* Clean separation between security and domain layers
* No leakage of persistence models into the web layer
* Improved testability and maintainability

#### Trade-offs

* Permission or group changes require token reissuance
* Slight increase in initial configuration complexity
* Additional abstraction layers (annotation, evaluator)

These trade-offs are acceptable given the security and architectural benefits.

### Scope

This decision applies to:

* HTTP endpoint authorization
* Method-level authorization
* Human users and system users (ROOT)
* Future extensions such as service-to-service authentication

### Related Decisions

* `ADR-002` – Separation Between Roles, Groups, and Permissions
* `ADR-007` - Use a Custom Authenticated Principal Instead of JPA Entities
* `permissions.md` – Permission Naming and Governance

---

## ADR-009: Structured Permission Schema in JWT (Nested Map)

### Context

Flat lists of strings (e.g., `["USER:READ", "USER:WRITE"]`) increase JWT size due to redundant prefixes.

### Decision

Permissions are stored in the JWT as a JSON Map: `{"DOMAIN": ["ACTION1", "ACTION2"]}`.

* Benefit: Reduces token size by up to 60% in high-granularity scenarios.
* Conversion: A `JwtAuthenticationConverter` flattens this map into `GrantedAuthority` strings (e.g., `DOMAIN:ACTION`) for internal compatibility.

---

## ADR-010: Support for Wildcard Permissions

### Context

Assigning dozens of permissions to domain admins bloats tokens and complicates maintenance.

### Decision

The system supports the wildcard (`*`) character at the action level.

* Rule: If a user possesses `DOMAIN:*`, they are authorized for any `@RequiresPermission("DOMAIN:ANY")`.
* Scalability: New actions added to a domain are automatically granted to wildcard holders.

---

## ADR-011: Hybrid Token Strategy (Access & Refresh)

### Context

Purely stateless JWTs cannot be revoked until they expire.

### Decision

1. **Access Token:** Short-lived (15 min), purely stateless JWT.

2. **Refresh Token:** Long-lived (7 days), stateful (persisted in DB).

* **Revocation:** Permission changes are reflected every 15 minutes (at refresh time). Admin can revoke access by deleting the Refresh Token from the DB.
* **Rotation:** Every refresh request issues a new Refresh Token and revokes the old one.

---

## ADR-012: Rate Limiting for Authentication Endpoints

### Context

Authentication endpoints are vulnerable to brute-force and DoS attacks.

### Decision

Implement **Bucket4j** (Token Bucket algorithm) to limit login attempts.

* **Threshold:** 10 attempts per IP, with a slow refill rate (1 per 5 min).

* **Response:** Returns `HTTP 429 Too Many Requests` when exhausted.
