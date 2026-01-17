# Requirements

## 1. Purpose

This document defines the functional and non-functional requirements of the Stock Management API. specifies the Identity and Access Management (IAM) model, the sovereign ROOT bootstrap process, and the stateless authorization engine.

## 2. Scope

The API provides authentication, authorization, and stock management. Access control is enforced through a hierarchical model of Roles, Groups, and Permissions, optimized for stateless execution and high granularity.

## 3. Actors

* **ROOT user**: System bootstrap authority with unrestricted access.
* **Regular user**: Performs authorized operational actions.

## 4. Authentication Requirements

* Dual-Token System: The system shall implement a short-lived Access Token (JWT) and a long-lived Refresh Token.
* Refresh Token Persistence: Refresh tokens shall be persisted to allow for manual revocation and session management.
* Token Rotation: The system shall implement Refresh Token Rotation, where a new refresh token is issued upon every renewal.
* Reuse Detection: The system shall detect and block refresh token reuse, invalidating all active sessions for the affected user upon detection.
* Multi-Factor Authentication (2FA): The system shall support TOTP-based 2FA as a requirement for specific roles or users.

## 5. Authorization Requirements

* Deny-by-Default: The system shall deny access to any protected resource if no explicit permission is granted.
* Hierarchical Permissions: Permissions shall follow the DOMAIN:ACTION naming convention.
* Structured JWT Claims: Permissions shall be stored in the JWT as a Nested Map (Map<String, List<String>>) to reduce token size and improve organization.
* Wildcard Support: The system shall support the wildcard character (*) at the action level. A DOMAIN:* permission shall grant access to all actions within that specific domain.
* Inheritance: Users shall inherit permissions cumulatively from all groups to which they are assigned.

## 6. ROOT User Requirements

* The system shall guarantee the existence of exactly one ROOT user.
* The ROOT user shall have unrestricted access to all system resources.
* The ROOT user shall bypass the permission and group model.
* The ROOT user shall not belong to any group.
* The ROOT user role shall not be assignable through public APIs.
* The ROOT user shall not be deletable or demotable.

## 7. ROOT Bootstrap Process

* On application startup, the system shall verify whether a ROOT user exists.
* If no ROOT user exists, the system shall automatically create one.
* ROOT credentials shall be provided exclusively via environment variables.
* Hardcoded ROOT credentials shall be prohibited.
* The bootstrap process shall be idempotent.
* The system shall fail to start if ROOT credentials are not properly configured on first startup.

## 8. Security & Resilience

* Sensitive credentials shall never be committed to source control.
* The system shall prevent privilege escalation through API misuse.
* Authorization rules shall be enforced at service or security-filter level.
* The authorization model shall be extensible without breaking existing APIs.
* The authentication endpoints shall implement Rate Limiting (Token Bucket algorithm) per IP address to prevent brute-force attacks.
* The authorization engine shall rely solely on JWT claims for permission checks, avoiding database lookups during the request lifecycle.
* The system accepts a maximum 15-minute window (Access Token TTL) for permission changes to propagate without forced revocation.

## 9. User Identification Requirements

* The system shall maintain a clear separation between internal and external user identifiers.
* The system shall assign an internal identifier used exclusively for persistence and relational integrity.
* The system shall assign an immutable external identifier of type UUID to each user.
* The external user identifier shall be used in all public-facing contexts, including:
  * API endpoints
  * JWT sub (subject) claim
  * Audit logs and security events
* The internal database identifier shall never be exposed through the API or included in JWT tokens.
* The system shall resolve authenticated users using the external identifier provided in the JWT.
* Logs and error messages shall mask sensitive data (e.g., partial email masking).