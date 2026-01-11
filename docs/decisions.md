# Architectural Decisions

## ADR-001: Introduction of a ROOT User

### Context

The system requires a secure and deterministic way to manage permissions and administrative access. Relying solely on dynamic role or group assignment creates a chicken-and-egg problem during initial system setup.

### Decision

A single ROOT user will be introduced as a bootstrap authority.

* The ROOT user has unrestricted access.
* The ROOT user is not governed by RBAC or group-permission rules.
* The ROOT user exists solely to initialize and recover administrative control.

### Consequences

* Simplifies initial system bootstrap.
* Prevents accidental lockout of administrative access.
* Requires strict safeguards to avoid misuse.

---

## ADR-002: Separation Between Roles, Groups, and Permissions

### Context

Traditional role-based models are insufficient for fine-grained access control and scalability.

### Decision

The authorization model will be composed of:

* Groups: Logical collections of permissions.
* Permissions: Atomic actions enforced by the system.

Users may belong to multiple groups.

### Consequences

* Enables AWS-like permission granularity.
* Increases model complexity.
* Improves long-term flexibility.

---

## ADR-003: ROOT User Bootstrap via Environment Configuration

### Context

Storing credentials in code or repositories introduces security risks.

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

### Consequences

* Strong protection against privilege escalation.
* Requires additional validation logic.

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

* Ensures recoverability after catastrophic database loss.
* Prevents permanent administrative lockout.
* Enforces secure operational practices via external secret management.
* Requires deployment environments to be correctly configured before startup.

This approach balances security, operational resilience, and auditability while maintaining a clear separation between bootstrap authority and regular authorization logic.
