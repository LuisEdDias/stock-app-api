# Permission Naming Convention

## Overview

This document defines the standard used to name and manage permissions within the StockApp API.

The permission model is designed to be:

* Compact (JWT-friendly)
* Deterministic
* Easy to read and maintain
* Scalable as new domains and features are introduced

Permissions are treated as **atomic authorities** evaluated by the authorization layer.

---

## Naming Pattern

All permissions follow the pattern:

```
<DOMAIN>:<ACTION>
```

Where:

* `DOMAIN` represents a functional area of the system
* `ACTION` represents an allowed operation within that domain

All permission identifiers:

* Are uppercase
* Use fixed, documented abbreviations
* Are immutable once introduced

---

## Domains

Domains represent high-level modules or bounded contexts of the application.

| Domain     | Code  | Description                         |
| ---------- | ----- | ----------------------------------- |
| User       | USER  | User and account management         |
| Group      | GRP   | User groups and permission grouping |
| Permission | PERM  | Permission administration           |
| Item       | ITEM  | Stock items                         |
| Item Model | MODEL | Item model definitions              |
| Category   | CAT   | Item categorization                 |
| Inventory  | INV   | Inventory locations                 |
| Box        | BOX   | Physical or logical containers      |
| Statistics | STAT  | Aggregated stock data and metrics   |

> New domain codes must be documented in this table before use.

---

## Actions

Actions are represented using short, two-letter codes to reduce token size.

| Action  | Code | Description                               |
| ------- | ---- | ----------------------------------------- |
| Create  | CR   | Create a new resource                     |
| Read    | RD   | Read a single resource                    |
| Update  | UP   | Update an existing resource               |
| Delete  | DL   | Delete a resource                         |
| List    | LS   | List or search resources                  |
| Assign  | AS   | Assign or link entities                   |
| Revoke  | RV   | Remove a previously assigned relationship |
| Execute | EX   | Execute a non-CRUD operation              |

> Action codes are shared across all domains and must remain consistent.

---

## Examples

| Permission | Meaning                                |
| ---------- | -------------------------------------- |
| ITEM:CR    | Create an item                         |
| ITEM:RD    | Read item details                      |
| ITEM:UP    | Update an item                         |
| ITEM:DL    | Delete an item                         |
| MODEL:LS   | List item models                       |
| USER:AS    | Assign groups or permissions to a user |
| GRP:CR     | Create a group                         |
| STAT:RD    | Access statistics                      |

---

## Usage in Authorization

Permissions are evaluated directly by the security layer.

Example with Spring Security:

```java
@PreAuthorize("hasAuthority('ITEM:CR')")
public Item createItem(...) {
    ...
}
```

The ROOT user bypasses permission checks by design and is not governed by this convention.

---

## JWT Representation

Permissions are serialized into JWT tokens as a flat list of strings:

```json
{
  "sub": "user-id",
  "perm": ["ITEM:CR", "ITEM:RD", "INV:LS"]
}
```

This compact representation minimizes token size while preserving clarity and flexibility.

---

## Evolution Guidelines

* Permissions are additive; existing permissions must never be renamed or removed.
* Deprecated permissions should be documented but kept for backward compatibility.
* Any change to this convention must be documented and justified via an ADR.

---

## Related Documentation

* `requirements.md` – Authorization requirements
* `decisions.md` – Architectural decisions related to security
