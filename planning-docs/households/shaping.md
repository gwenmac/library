# Households – Shaping Doc

## Problem

The library app currently scopes all data (books, authors, series) per individual user. In practice, many users share a physical book collection with a partner or family. There is no way to share a library — each person has a completely separate copy of everything. This leads to duplicate data entry and doesn't reflect how books are actually owned in a household.

---

## Appetite

Small batch – 2–3 days of work across backend and frontend.

---

## Solution

### Concept

A **Household** is a named group that owns one shared collection of books, authors, and series. Every user belongs to exactly one household. All members of a household see the same library and can add, edit, or delete any book, author, or series in it.

**Ratings remain per-user** — each person can rate/review a book independently, even though the book record itself is shared.

**Gauges remain per-user** — reading progress and goals are personal.

An **admin** creates households and assigns users to them.

### Core Objects

| Object | Purpose |
|--------|---------|
| `Household` | Named group that owns books, authors, series |
| `User.household` | FK linking each user to their household |

### How It Works

1. Admin creates a household (e.g., "The MacDonalds").
2. Admin assigns users to a household.
3. All books, authors, and series owned by those users become household-scoped.
4. Any member of the household can add/edit/delete books, authors, series.
5. Ratings on books remain tied to the individual user who rated.
6. Gauges remain fully personal (scoped to user, not household).

---

## Database Design

### `households` table

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT, PK, AUTO_INCREMENT | |
| `name` | VARCHAR(100), NOT NULL | Display name for the household |
| `created_at` | DATETIME | |

### Changes to `users` table

Add `household_id BIGINT NOT NULL` foreign key → `households.id`.

Every user must belong to a household. When a user is created without an explicit household, a default one is created for them (or the admin assigns one).

### Changes to owned tables

Replace `user_id` with `household_id` on:

- `books`
- `authors`
- `series`

These are now scoped to the household, not the individual user.

### Tables that stay per-user

- `gauges` – remain scoped by `user_id`
- `book_ratings` (if/when added) – scoped by `user_id`

### Unique constraints

- `authors`: UNIQUE(`household_id`, `name`) — no duplicate authors within a household
- `series`: UNIQUE(`household_id`, `name`) — no duplicate series within a household

---

## Backend (Spring Boot)

### Entity Changes

- **New entity**: `Household` with `id`, `name`, `createdAt`
- **User**: add `@ManyToOne Household household`
- **Book, Author, Series**: change `@ManyToOne User user` → `@ManyToOne Household household`
- **Gauge**: remains `@ManyToOne User user` (no change)

### Data Isolation Changes

- Book/Author/Series repositories: queries change from `findAllByUserId` → `findAllByHouseholdId`
- Controllers: get household from `CurrentUser.get().getHousehold().getId()` instead of `CurrentUser.id()`
- Gauge controllers: no change (still user-scoped)

### Admin Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/admin/households` | Create a household |
| GET | `/admin/households` | List all households |
| PUT | `/admin/households/{id}` | Update household name |
| PUT | `/admin/users/{id}/household` | Assign user to a household |

---

## Frontend (Vue)

### Changes to Admin Page

- Add "Households" section to admin: create/rename/list households.
- User management: show which household a user belongs to, allow reassignment.

### Changes to Existing Pages

- No functional changes for non-admin users — the library works the same, but data is now shared with household members.
- Nav bar could optionally show household name.

---

## Rabbit Holes

- **Don't** build user-to-user permissions within a household. All members have equal read/write access to the shared collection.
- **Don't** build a "leave household" flow. Admin handles all assignment.
- **Don't** build household invitations or self-service join. Admin assigns directly.
- **Don't** handle multi-household membership. One user = one household, always.
- **Don't** build per-book ratings in this batch — just note that the model supports it for the future.

---

## No-Gos

- No user-facing household management (admin only).
- No sharing between households.
- No per-item permissions within a household.
- No splitting/forking a household's collection.

---

## Cut of Work

1. **Backend – Household entity**: Create Household entity, repository, add `household_id` FK to User.
2. **Backend – Data model migration**: Change Book/Author/Series from `user_id` → `household_id`, update repositories and controllers.
3. **Backend – Admin endpoints**: CRUD for households, user-to-household assignment.
4. **Frontend – Admin UI**: Household management section, user assignment controls.
5. **Database – Schema update**: Update `boot.sql` with households table, FK changes, updated constraints. Update `data.sql` with seed household.
