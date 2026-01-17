# Permission Naming Convention

## Overview

This document defines the standard used to name and manage permissions within the StockApp API.

The permission model is designed to be:

* Compact (JWT-friendly)
* Deterministic
* Easy to read and maintain
* Scalable as new domains and features are introduced

Permissions are treated as **atomic authorities** evaluated by the authorization layer via method-level security annotations.

---

## Naming Pattern

All permissions follow the pattern:

```
<DOMAIN>:<ACTION>
```

Where:

* `DOMAIN` represents a functional area of the system
* `ACTION` represents an allowed operation within that domain
* Permissions are serialized as a map where the key is the `DOMAIN` and the value is a list of `ACTION` codes.
All permission identifiers:

* Are uppercase
* Use fixed, documented abbreviations
* Are immutable once introduced

---

## Domains

Domains represent high-level modules or bounded contexts of the application.

| Domain     | Code | Description                         |
| ---------- |------| ----------------------------------- |
| User       | USER | User and account management         |
| Group      | GRP  | User groups and permission grouping |
| Permission | PERM | Permission administration           |
| Item       | ITEM | Stock items                         |
| Item Model | IMOD | Item model definitions              |
| Category   | ICAT | Item categorization                 |
| Inventory  | INV  | Inventory locations                 |
| Box        | BOX  | Physical or logical containers      |
| Statistics | STAT | Aggregated stock data and metrics   |

> New domain codes must be documented in this table before use.

---

## Actions

Actions are represented using short, two-letter codes to reduce token size.

| Action   | Code | Description                               |
|----------|------|-------------------------------------------|
| Wildcard | *    | Authorizes any request to a domain        |
| Create   | CR   | Create a new resource                     |
| Read     | RD   | Read a single resource                    |
| Update   | UP   | Update an existing resource               |
| Delete   | DL   | Delete a resource                         |
| List     | LS   | List or search resources                  |
| Assign   | AS   | Assign or link entities                   |
| Revoke   | RV   | Remove a previously assigned relationship |
| Execute  | EX   | Execute a non-CRUD operation              |

> Action codes are shared across all domains and must remain consistent.

---

## Examples

| Permission | Meaning                                               |
|------------|-------------------------------------------------------|
| BOX:*      | All permissions will be granted within the Box domain |
| ITEM:CR    | Create an item                                        |
| ITEM:RD    | Read item details                                     |
| ITEM:UP    | Update an item                                        |
| ITEM:DL    | Delete an item                                        |
| MODEL:LS   | List item models                                      |
| USER:AS    | Assign groups or permissions to a user                |
| GRP:CR     | Create a group                                        |
| STAT:RD    | Access statistics                                     |

---

## Usage in Authorization

Permissions are evaluated exclusively by the security layer using a custom authorization annotation.

### Standard Authorization Pattern

```java
@RequiresPermission("ITEM:CR")
public Item createItem(...) {
    ...
}
```

### ROOT User Behavior

The ROOT user is identified by the presence of the authority:

```
ROLE_ROOT
```

Authorization rules are evaluated as:

```
ROLE_ROOT OR <REQUIRED_PERMISSION>
```

As a result:

* ROOT automatically bypasses all permission checks
* Controllers do not contain ROOT-specific logic
* Permission rules remain explicit and auditable

ROOT bypass behavior is implemented centrally and **must not** be reimplemented at the controller or service level.

---

## JWT Representation

Permissions are serialized into JWT tokens as a Nested JSON Object to optimize space:

```json
{
  "sub": "user-public-uuid",
  "role": "USER",
  "perm": {
    "ITEM": ["RD", "CR", "DL", "CR"], 
    "BOX": ["RD", "LS"]
  }
}
```

For ROOT users:

```json
{
  "sub": "root-public-uuid",
  "role": "ROOT",
  "perm": []
}
```
## Denied by Default

Access is denied if any annotation is found. Public methods must be annotated with a public annotation, otherwise, access will be denied:
```java
@PublicAccess
public List<Item> itemList(...) {
    ...
}
```

* **Strict Enforcement:** If a method or class lacks both `@RequiresPermission` and `@PublicAccess`, the `PermissionAuthorizationManager` will return a `Denied` decision. **Security is opt-out, not opt-in**.

## Architectural Notes

* Permissions are evaluated without database access
* Authorization is fully stateless and JWT-driven
* Domain entities are never exposed as security principals
* Controllers consume a custom `AuthenticatedUser` principal

This model aligns with IAM-style systems and supports future extensions such as service-to-service authentication.

---

## Evolution Guidelines

* Permissions are additive; existing permissions must never be renamed or removed
* Deprecated permissions should be documented but kept for backward compatibility
* Any change to this convention must be documented and justified via an ADR

---

## Related Documentation

* `requirements.md` – Authorization requirements
* `decisions.md` – Architectural decisions related to security
* `ADR-008` -  Permission Control System Implementation
* `ADR-009` - Structured Permission Schema in JWT (Nested Map)
* `ADR-010` - Support for Wildcard Permissions