# Individual Users – Shaping Doc

## Problem

The library app is currently single-user — all books, gauges, and data live in one shared pool. To share the app with friends and family, each person needs their own account with isolated data. There is no way to log in, no concept of ownership on records, and no access control.

---

## Appetite

Small batch – 2–3 days of work across backend and frontend.

---

## Solution

### Concept

A **User** is a person with an account who owns all the data they create (books, authors, gauges, etc.). Users authenticate with email and password. An **admin** user can invite new users and manage existing accounts. Each user's data is fully isolated — they only see their own library.

The system is designed so that friend visibility (seeing what others are reading) can be layered on later without rearchitecting.

### Core Objects

| Object | Purpose |
|--------|---------|
| `User` | Account with email, hashed password, display name, role |
| `Session/Token` | JWT or session token proving the user is authenticated |

---

## Database Design

### `users` table

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT, PK, AUTO_INCREMENT | |
| `email` | VARCHAR(255), NOT NULL, UNIQUE | Login identifier |
| `password_hash` | VARCHAR(255), NOT NULL | BCrypt-hashed password |
| `display_name` | VARCHAR(100), NOT NULL | Shown in UI |
| `role` | ENUM('admin', 'user'), NOT NULL, DEFAULT 'user' | |
| `created_at` | DATETIME | |

### Changes to existing tables

Add a `user_id BIGINT NOT NULL` foreign key column to:

- `books`
- `authors`
- `gauges`
- `series`

All queries must be scoped by `user_id`. Shared lookup tables (`genres`, `languages`, `editions`) remain global since they're reference data.

---

## Backend (Spring Boot)

### Authentication

- Use **Spring Security** with a stateless JWT approach.
- `POST /auth/login` – accepts email + password, returns JWT.
- `POST /auth/register` – **admin-only** endpoint to create new users (or a one-time bootstrap for the first admin).
- JWT is sent in `Authorization: Bearer <token>` header on all subsequent requests.
- A `SecurityContext` filter extracts the current user from the token and makes it available to controllers/services.

### Entity: `User`

- Fields: `id`, `email`, `passwordHash`, `displayName`, `role`, `createdAt`

### Data Isolation

- Add `user_id` to Book, Author, Gauge, Series entities (ManyToOne → User).
- All repository queries include a `user_id` filter.
- Controllers get the current user from `SecurityContext` and pass it to service/repository calls.
- Existing data will need a migration to assign records to the first (admin) user.

### Admin Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Create a new user (admin only) |
| GET | `/admin/users` | List all users (admin only) |
| DELETE | `/admin/users/{id}` | Delete a user and their data (admin only) |

### Bootstrap

On first run (no users exist), the app should allow creating the initial admin account — either via a setup endpoint or a CLI/environment-variable seed.

---

## Frontend (Vue)

### New Pages

| Page | Path | Purpose |
|------|------|---------|
| Login | `/login` | Email + password form |
| Admin: Users | `/admin/users` | List/invite/remove users (admin only) |

### Auth Flow

- Store JWT in localStorage (or httpOnly cookie if preferred later).
- Add an Axios request interceptor to attach the token to every API call.
- Add a Vue Router navigation guard: unauthenticated users are redirected to `/login`.
- Show current user's display name in the nav bar with a logout option.

### Changes to Existing Pages

- No functional changes — all pages continue to work as before, but the data returned is automatically scoped to the logged-in user by the backend.
- Nav bar gains a user indicator and logout button.

---

## Rabbit Holes

- **Don't** build email verification or password reset for v1. This is a small trusted group — the admin can reset passwords manually if needed.
- **Don't** implement friend visibility or sharing yet. Design the data model so `user_id` scoping is clean, and social features can be added as a separate batch.
- **Don't** build user profile pages or settings beyond display name.
- **Don't** encrypt JWT tokens beyond standard signing — HTTPS handles transport security.

---

## No-Gos

- No OAuth/social login (keep it simple email+password).
- No self-registration — admin invites only.
- No friend/social features in this batch.
- No email sending (invites are communicated out-of-band by the admin).

---

## Cut of Work

1. **Backend – Auth**: User entity, BCrypt password hashing, JWT generation/validation, Spring Security filter chain, login endpoint, admin bootstrap.
2. **Backend – Data isolation**: Add `user_id` FK to existing entities, update repositories and controllers to scope by user, migration for existing data.
3. **Backend – Admin**: Register (invite) endpoint, list users, delete user.
4. **Frontend – Auth**: Login page, JWT storage, Axios interceptor, router guard, nav bar user indicator.
5. **Frontend – Admin**: Simple user management page (admin only).
