# Households – Design Doc

## Overview

This document details the implementation plan for the household feature described in `shaping.md`. It specifies exact file paths, class structures, SQL, API contracts, and Vue component changes needed to move book/author/series ownership from individual users to households while keeping gauges per-user.

---

## 1. Database

### 1.1 New table: `households` (add to `library-docker/mysql/boot.sql` before `users`)

```sql
CREATE TABLE households (
    id         BIGINT NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id)
);
```

### 1.2 Modify `users` table

Add `household_id` column with a foreign key to `households`:

```sql
CREATE TABLE users (
    id            BIGINT NOT NULL AUTO_INCREMENT,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    role          ENUM('admin', 'user') NOT NULL DEFAULT 'user',
    household_id  BIGINT NOT NULL,
    created_at    DATETIME NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (household_id) REFERENCES households(id)
);
```

### 1.3 Change `user_id` → `household_id` on owned tables

**`authors`**:
```sql
CREATE TABLE authors (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    name         VARCHAR(255) NOT NULL,
    household_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY (household_id, name),
    FOREIGN KEY (household_id) REFERENCES households(id)
);
```

**`series`**:
```sql
CREATE TABLE series (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    name         VARCHAR(255) NOT NULL,
    status       VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED',
    household_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY (household_id, name),
    FOREIGN KEY (household_id) REFERENCES households(id)
);
```

**`books`**:
```sql
CREATE TABLE books (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    page_count   INT,
    year         INT,
    sort_title   VARCHAR(255),
    series_id    BIGINT,
    series_order INT,
    edition_id   BIGINT,
    household_id BIGINT NOT NULL,
    created_at   DATETIME,
    updated_at   DATETIME,
    PRIMARY KEY (id),
    FOREIGN KEY (series_id)    REFERENCES series(id),
    FOREIGN KEY (edition_id)   REFERENCES editions(id),
    FOREIGN KEY (household_id) REFERENCES households(id)
);
```

### 1.4 Tables that stay per-user (no change)

- `gauges` — remains `user_id BIGINT NOT NULL`
- `gauge_entries` — unchanged (FK to gauges)

### 1.5 Tables that remain global (no change)

- `genres`, `languages`, `editions`, `statuses`

### 1.6 Seed data (`library-docker/mysql/data.sql`)

```sql
-- Seed household
INSERT INTO households (id, name, created_at) VALUES
    (1, 'Default Household', NOW());

-- Admin user assigned to household 1
INSERT INTO users (id, email, password_hash, display_name, role, household_id, created_at) VALUES
    (1, 'orangutan@library.local', '$2a$10$85gbUAisk9lqREfG1Pjk7.lihrUKrQ.03wCfua3FV0WL3CLTcbn7u', 'Orangutan', 'admin', 1, NOW());
```

All other data.sql inserts that currently reference `user_id` change to `household_id` with value `1`.

---

## 2. Backend – Entities

### 2.1 New entity: `Household`

**File**: `lib/src/main/java/library/entities/Household.java`

```java
package library.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "households")
public class Household {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
```

### 2.2 Modify `User` entity

**File**: `lib/src/main/java/library/entities/User.java`

Add:
```java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "household_id", nullable = false)
private Household household;
```

### 2.3 Modify `Book` entity

**File**: `lib/src/main/java/library/entities/Book.java`

Replace:
```java
@JsonIgnore
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;
```

With:
```java
@JsonIgnore
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "household_id", nullable = false)
private Household household;
```

### 2.4 Modify `Author` entity

**File**: `lib/src/main/java/library/entities/Author.java`

Replace:
```java
@JsonIgnore
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;
```

With:
```java
@JsonIgnore
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "household_id", nullable = false)
private Household household;
```

### 2.5 Modify `Series` entity

**File**: `lib/src/main/java/library/entities/Series.java`

Replace:
```java
@JsonIgnore
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;
```

With:
```java
@JsonIgnore
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "household_id", nullable = false)
private Household household;
```

### 2.6 `Gauge` entity — no change

Remains scoped to `user_id`.

---

## 3. Backend – Repositories

### 3.1 New: `HouseholdRepository`

**File**: `lib/src/main/java/library/repositories/HouseholdRepository.java`

```java
package library.repositories;

import library.entities.Household;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseholdRepository extends JpaRepository<Household, Long> {
}
```

### 3.2 Modify `BookRepository`

Replace:
```java
List<Book> findAllByUserIdOrderBySortTitleAsc(Long userId);
Optional<Book> findByIdAndUserId(Long id, Long userId);
```

With:
```java
List<Book> findAllByHouseholdIdOrderBySortTitleAsc(Long householdId);
Optional<Book> findByIdAndHouseholdId(Long id, Long householdId);
```

### 3.3 Modify `AuthorRepository`

Replace:
```java
List<Author> findAllByUserId(Long userId);
Optional<Author> findByIdAndUserId(Long id, Long userId);
```

With:
```java
List<Author> findAllByHouseholdId(Long householdId);
Optional<Author> findByIdAndHouseholdId(Long id, Long householdId);
```

### 3.4 Modify `SeriesRepository`

Replace:
```java
List<Series> findAllByUserId(Long userId);
Optional<Series> findByIdAndUserId(Long id, Long userId);
```

With:
```java
List<Series> findAllByHouseholdId(Long householdId);
Optional<Series> findByIdAndHouseholdId(Long id, Long householdId);
```

### 3.5 `GaugeRepository` — no change

Remains user-scoped.

---

## 4. Backend – Security Helper

### 4.1 Modify `CurrentUser`

**File**: `lib/src/main/java/library/security/CurrentUser.java`

Add a convenience method for household ID:

```java
package library.security;

import library.entities.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUser {

    public static User get() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static Long id() {
        return get().getId();
    }

    public static Long householdId() {
        return get().getHousehold().getId();
    }
}
```

---

## 5. Backend – Controllers

### 5.1 Modify `BookController`

All references to `CurrentUser.id()` and `user_id` change to `CurrentUser.householdId()` and `household_id`:

- `getAll()`: `bookRepository.findAllByHouseholdIdOrderBySortTitleAsc(CurrentUser.householdId())`
- `getById()`: `bookRepository.findByIdAndHouseholdId(id, CurrentUser.householdId())`
- `create()`: `book.setHousehold(CurrentUser.get().getHousehold())`
- `update()`: `bookRepository.findByIdAndHouseholdId(id, CurrentUser.householdId())`
- `updateReview()`: `bookRepository.findByIdAndHouseholdId(id, CurrentUser.householdId())`
- `delete()`: `bookRepository.findByIdAndHouseholdId(id, CurrentUser.householdId())`

### 5.2 Modify `AuthorController`

- `getAllAuthors()`: `authorRepository.findAllByHouseholdId(CurrentUser.householdId())`
- `createAuthor()`: `author.setHousehold(CurrentUser.get().getHousehold())`

### 5.3 Modify `SeriesController`

- `getAll()`: `seriesRepository.findAllByHouseholdId(CurrentUser.householdId())`
- `create()`: `series.setHousehold(CurrentUser.get().getHousehold())`

### 5.4 `GaugeController` — no change

Remains user-scoped.

### 5.5 Modify `AdminController`

Add household management and user-to-household assignment:

```java
// --- Household CRUD ---

@GetMapping("/admin/households")
public List<Household> listHouseholds() {
    return householdRepository.findAll();
}

@PostMapping("/admin/households")
@ResponseStatus(HttpStatus.CREATED)
public Household createHousehold(@RequestBody Map<String, String> body) {
    Household household = new Household();
    household.setName(body.get("name"));
    household.setCreatedAt(LocalDateTime.now());
    return householdRepository.save(household);
}

@PutMapping("/admin/households/{id}")
public Household updateHousehold(@PathVariable Long id, @RequestBody Map<String, String> body) {
    Household household = householdRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Household not found"));
    household.setName(body.get("name"));
    return householdRepository.save(household);
}

// --- Assign user to household ---

@PutMapping("/admin/users/{id}/household")
public User assignHousehold(@PathVariable Long id, @RequestBody Map<String, Long> body) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    Household household = householdRepository.findById(body.get("householdId"))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Household not found"));
    user.setHousehold(household);
    return userRepository.save(user);
}
```

### 5.6 Modify `AuthController`

- `bootstrap()`: When creating the first admin, also create a default household and assign the admin to it.
- `createUser()` in AdminController: Accept optional `householdId`; if not provided, create a new household for the user.

---

## 6. Backend – Auth Changes

### 6.1 Bootstrap endpoint creates default household

In `AuthController.bootstrap()`:

```java
Household household = new Household();
household.setName(body.getOrDefault("householdName", "My Household"));
household.setCreatedAt(LocalDateTime.now());
householdRepository.save(household);

admin.setHousehold(household);
```

### 6.2 Login response includes household

The login response `user` map should include `householdId` and `householdName`:

```java
"user", Map.of(
    "id", user.getId(),
    "email", user.getEmail(),
    "displayName", user.getDisplayName(),
    "role", user.getRole().name(),
    "householdId", user.getHousehold().getId(),
    "householdName", user.getHousehold().getName()
)
```

### 6.3 Admin user creation requires household

In `AdminController.createUser()`, require `householdId` in the request body:

```java
Long householdId = Long.parseLong(body.get("householdId"));
Household household = householdRepository.findById(householdId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Household not found"));
user.setHousehold(household);
```

---

## 7. Frontend – Admin UI

### 7.1 New component: `Households.vue`

**File**: `library-vue/src/admin/Households.vue`

Features:
- List all households with member count
- Form to create a new household (name field)
- Inline edit to rename a household

### 7.2 Modify `Users.vue`

**File**: `library-vue/src/admin/Users.vue`

Changes:
- Show household name column in user table
- Add household dropdown to invite form (required)
- Add "Reassign" button that opens a dropdown to change a user's household

### 7.3 Router changes

**File**: `library-vue/src/router.js`

Add route:
```js
{ path: '/admin/households', component: () => import('./admin/Households.vue'), meta: { admin: true } }
```

### 7.4 Nav changes

**File**: `library-vue/src/App.vue`

Add "Households" link in admin nav section:
```html
<li v-if="user && user.role === 'admin'"><router-link to="/admin/households">Households</router-link></li>
```

Optionally show household name in the nav-user section.

---

## 8. API Contract Summary

### Existing endpoints (behavior changes)

| Method | Endpoint | Change |
|--------|----------|--------|
| GET | `/all` | Returns books for current user's household |
| GET | `/books/{id}` | Scoped to household |
| POST | `/books` | Assigned to household |
| PUT | `/books/{id}` | Scoped to household |
| DELETE | `/books/{id}` | Scoped to household |
| GET | `/authors/all` | Returns authors for household |
| POST | `/authors` | Assigned to household |
| GET | `/series/all` | Returns series for household |
| POST | `/series` | Assigned to household |

### New endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/households` | List all households |
| POST | `/admin/households` | Create household `{ "name": "..." }` |
| PUT | `/admin/households/{id}` | Update household `{ "name": "..." }` |
| PUT | `/admin/users/{id}/household` | Assign user `{ "householdId": 1 }` |

---

## 9. Implementation Order

1. **Database**: Rewrite `boot.sql` with `households` table first, add `household_id` to users/books/authors/series, update `data.sql` seed.
2. **Entity + Repository**: Create `Household` entity/repo, modify User/Book/Author/Series entities, update repository method signatures.
3. **CurrentUser helper**: Add `householdId()` method.
4. **Controllers**: Update BookController, AuthorController, SeriesController to use `householdId()`. Update AdminController with household CRUD and user assignment.
5. **Auth changes**: Update bootstrap to create household, update login response, update user creation to require household.
6. **Frontend admin**: Create `Households.vue`, update `Users.vue` with household column and assignment, add route.
7. **Frontend nav**: Add household link for admin, optionally show household name.
