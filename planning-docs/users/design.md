# Individual Users – Design Doc

## Overview

This document details the implementation plan for the user system described in `shaping.md`. It specifies exact file paths, class structures, SQL, API contracts, and Vue component designs, following the patterns already established in this project.

---

## 1. Database

### 1.1 Schema additions (append to `library-docker/mysql/boot.sql`)

```sql
-- Users

CREATE TABLE users (
    id            BIGINT NOT NULL AUTO_INCREMENT,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    role          ENUM('admin', 'user') NOT NULL DEFAULT 'user',
    created_at    DATETIME NOT NULL,
    PRIMARY KEY (id)
);
```

### 1.2 Add `user_id` to existing table definitions in `boot.sql`

Add a `user_id BIGINT NOT NULL` column with a foreign key to `users(id)` directly in the CREATE TABLE statements for:

- `books` — `user_id BIGINT NOT NULL, FOREIGN KEY (user_id) REFERENCES users(id)`
- `authors` — same
- `series` — same
- `gauges` — same

The `users` table must be created before these tables in `boot.sql` so the FK reference resolves.

### 1.3 Tables that remain global (no `user_id`)

- `genres` — shared reference data
- `languages` — shared reference data
- `editions` — shared reference data
- `statuses` — shared reference data

### 1.4 Unique constraint changes

Authors, series, and gauge names become unique per-user rather than globally unique. In the CREATE TABLE definitions, replace `UNIQUE` on `name` with a composite unique key:

```sql
UNIQUE KEY (user_id, name)
```

### 1.5 Seed data (`data.sql`)

`data.sql` must seed an admin user first, then reference that user's ID when inserting books, authors, series, and gauges. Example:

```sql
INSERT INTO users (email, password_hash, display_name, role, created_at)
VALUES ('admin@example.com', '<bcrypt hash>', 'Admin', 'admin', NOW());

-- Use user_id = 1 (the admin) for all seeded data
INSERT INTO books (title, user_id, ...) VALUES ('...', 1, ...);
```

---

## 2. Backend – Dependencies

### 2.1 New Gradle dependencies (`lib/build.gradle`)

```groovy
// Spring Security
implementation 'org.springframework.boot:spring-boot-starter-security'
testImplementation 'org.springframework.security:spring-security-test'

// JWT (jjwt library)
implementation 'io.jsonwebtoken:jjwt-api:0.12.5'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.5'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.5'
```

### 2.2 Application properties additions (`lib/src/main/resources/application.properties`)

```properties
# JWT
library.jwt.secret=${JWT_SECRET:default-dev-secret-change-in-production-at-least-32-chars!!}
library.jwt.expiration-ms=86400000
```

---

## 3. Backend – Java (Spring Boot)

All new files live under `lib/src/main/java/library/`.

### 3.1 Entity: `entities/User.java`

```java
package library.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role {
        admin, user
    }
}
```

### 3.2 Repository: `repositories/UserRepository.java`

```java
package library.repositories;

import library.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(User.Role role);
}
```

### 3.3 Security: `security/JwtUtil.java`

```java
package library.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${library.jwt.secret}") String secret,
            @Value("${library.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(Long userId, String email, String role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

### 3.4 Security: `security/JwtAuthenticationFilter.java`

```java
package library.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import library.entities.User;
import library.repositories.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtUtil.parseToken(token);
                Long userId = Long.parseLong(claims.getSubject());
                String role = claims.get("role", String.class);

                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                    var auth = new UsernamePasswordAuthenticationToken(user, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ignored) {
                // Invalid token — continue as unauthenticated
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

### 3.5 Security: `security/SecurityConfig.java`

```java
package library.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers("/auth/bootstrap").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 3.6 Controller: `controllers/AuthController.java`

```java
package library.controllers;

import library.entities.User;
import library.repositories.UserRepository;
import library.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return Map.of(
            "token", token,
            "user", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "displayName", user.getDisplayName(),
                "role", user.getRole().name()
            )
        );
    }

    @PostMapping("/bootstrap")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> bootstrap(@RequestBody Map<String, String> body) {
        // Only allowed when no users exist
        if (userRepository.existsByRole(User.Role.admin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin already exists");
        }

        User admin = new User();
        admin.setEmail(body.get("email"));
        admin.setPasswordHash(passwordEncoder.encode(body.get("password")));
        admin.setDisplayName(body.get("displayName"));
        admin.setRole(User.Role.admin);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);

        String token = jwtUtil.generateToken(admin.getId(), admin.getEmail(), admin.getRole().name());

        return Map.of(
            "token", token,
            "user", Map.of(
                "id", admin.getId(),
                "email", admin.getEmail(),
                "displayName", admin.getDisplayName(),
                "role", admin.getRole().name()
            )
        );
    }
}
```

### 3.7 Controller: `controllers/AdminController.java`

```java
package library.controllers;

import library.entities.User;
import library.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(body.get("password")));
        user.setDisplayName(body.get("displayName"));
        user.setRole(User.Role.user);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() == User.Role.admin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete admin user");
        }

        userRepository.delete(user);
    }
}
```

### 3.8 Helper: `security/CurrentUser.java`

A utility to extract the current user from the security context, used by all controllers:

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
}
```

### 3.9 Entity changes: Add `user_id` to owned entities

Each user-owned entity gets a new field:

```java
// Add to Book.java, Author.java, Series.java, Gauge.java:

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
@JsonIgnore
private User user;
```

`@JsonIgnore` prevents the full user object from appearing in API responses.

### 3.10 Repository changes: Scope queries by user

All repositories for user-owned data add `findBy...AndUserId` methods or use `user_id` parameter:

```java
// BookRepository.java
List<Book> findAllByUserIdOrderBySortTitleAsc(Long userId);

// AuthorRepository.java
List<Author> findAllByUserId(Long userId);
Optional<Author> findByIdAndUserId(Long id, Long userId);

// SeriesRepository.java
List<Series> findAllByUserId(Long userId);
Optional<Series> findByIdAndUserId(Long id, Long userId);

// GaugeRepository.java
@Query("SELECT g FROM Gauge g LEFT JOIN FETCH g.entries WHERE g.user.id = :userId")
List<Gauge> findAllWithEntriesByUserId(@Param("userId") Long userId);
Optional<Gauge> findByIdAndUserId(Long id, Long userId);
```

### 3.11 Controller changes: Use `CurrentUser.id()` to scope data

All existing controllers are updated to:
1. Get the current user via `CurrentUser.id()`
2. Pass `userId` to repository queries
3. Set `user` on newly created entities

Example pattern (BookController):

```java
@GetMapping("/all")
public List<Book> getAll() {
    return bookRepository.findAllByUserIdOrderBySortTitleAsc(CurrentUser.id());
}

@PostMapping("/books")
@ResponseStatus(HttpStatus.CREATED)
public Book create(@RequestBody Map<String, Object> body) {
    Book book = new Book();
    book.setUser(CurrentUser.get());
    // ... rest of existing logic
}
```

The same pattern applies to `AuthorController`, `SeriesController`, and `GaugeController`.

---

## 4. API Contract

### 4.1 Authentication endpoints

| Method | Path | Request Body | Response | Status |
|--------|------|--------------|----------|--------|
| POST | `/api/auth/login` | `{ email, password }` | `{ token, user }` | 200 |
| POST | `/api/auth/bootstrap` | `{ email, password, displayName }` | `{ token, user }` | 201 |

### 4.2 Admin endpoints

| Method | Path | Request Body | Response | Status |
|--------|------|--------------|----------|--------|
| GET | `/api/admin/users` | — | `User[]` | 200 |
| POST | `/api/admin/users` | `{ email, password, displayName }` | `User` | 201 |
| DELETE | `/api/admin/users/:id` | — | — | 204 |

### 4.3 Response shapes

**Login response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "email": "gwen@example.com",
    "displayName": "Gwen",
    "role": "admin"
  }
}
```

**User (admin list):**
```json
{
  "id": 2,
  "email": "friend@example.com",
  "displayName": "Friend",
  "role": "user",
  "createdAt": "2026-05-06T10:00:00"
}
```

### 4.4 All other endpoints (unchanged paths)

Existing endpoints (`/all`, `/books`, `/books/{id}`, `/gauges`, etc.) remain unchanged in path and shape. They now implicitly scope to the authenticated user. A missing or invalid token returns `401 Unauthorized`.

### 4.5 Error responses

| Status | Meaning |
|--------|---------|
| 401 | Missing/invalid/expired JWT |
| 403 | Non-admin accessing `/admin/**`, or bootstrap when admin exists |
| 409 | Email already registered |

---

## 5. Frontend – Vue

All new files live under `library-vue/src/`.

### 5.1 New dependency

```bash
npm install axios
```

Currently the app uses `fetch()` (implicit). Axios provides a cleaner interceptor pattern for attaching the JWT. If the app already uses `fetch` everywhere, an alternative is a thin `api.js` wrapper with `fetch` — see section 5.3.

### 5.2 Routing (`router.js` changes)

```javascript
import Login from "./auth/Login.vue";
import Bootstrap from "./auth/Bootstrap.vue";
import AdminUsers from "./admin/Users.vue";

// Add to routes array:
{ path: '/login', component: Login, meta: { public: true } },
{ path: '/bootstrap', component: Bootstrap, meta: { public: true } },
{ path: '/admin/users', component: AdminUsers, meta: { requiresAdmin: true } }
```

Add a navigation guard:

```javascript
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token');
    if (to.meta.public) {
        next();
    } else if (!token) {
        next('/login');
    } else {
        next();
    }
});
```

### 5.3 API client: `auth/api.js`

A shared module that all pages use for API calls:

```javascript
const API_BASE = '/api';

function getToken() {
    return localStorage.getItem('token');
}

export function getUser() {
    const raw = localStorage.getItem('user');
    return raw ? JSON.parse(raw) : null;
}

export function setAuth(token, user) {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
}

export function clearAuth() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
}

export async function api(path, options = {}) {
    const token = getToken();
    const headers = { 'Content-Type': 'application/json', ...options.headers };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE}${path}`, { ...options, headers });

    if (response.status === 401) {
        clearAuth();
        window.location.hash = '#/login';
        throw new Error('Unauthorized');
    }

    if (!response.ok) {
        throw new Error(`API error: ${response.status}`);
    }

    if (response.status === 204) return null;
    return response.json();
}
```

### 5.4 Page: `auth/Login.vue`

**Responsibilities:**
- Form with email and password inputs.
- On submit: `POST /api/auth/login` → store token + user in localStorage → redirect to `/`.
- Display error message on invalid credentials.
- On mount: check if any admin exists (hit `/api/auth/bootstrap` with GET or try to detect) — if no users exist, redirect to `/bootstrap`.

**Template structure:**
```html
<form @submit.prevent="login">
  <h1>Log In</h1>
  <div class="field">
    <label>Email</label>
    <input v-model="email" type="email" required />
  </div>
  <div class="field">
    <label>Password</label>
    <input v-model="password" type="password" required />
  </div>
  <p v-if="error" class="error">{{ error }}</p>
  <button type="submit">Log In</button>
</form>
```

### 5.5 Page: `auth/Bootstrap.vue`

**Responsibilities:**
- Only shown on first-ever app use (no admin exists).
- Form with email, password, and display name.
- On submit: `POST /api/auth/bootstrap` → store token + user → redirect to `/`.
- If an admin already exists (403 response), redirect to `/login`.

### 5.6 Page: `admin/Users.vue`

**Responsibilities:**
- Fetch `GET /api/admin/users` on mount.
- Display table of users (display name, email, role, created date).
- "Invite User" button opens an inline form: email, display name, temporary password.
- On invite submit: `POST /api/admin/users` → refresh list.
- Delete button per user (not for admin) → `DELETE /api/admin/users/:id` → refresh list.

### 5.7 App.vue changes

Update nav bar to show the current user and logout button:

```html
<nav id="nav">
  <div class="nav-brand">📚 Library</div>
  <ul>
    <li><router-link to="/">Home</router-link></li>
    <li><router-link to="/book/list">Books</router-link></li>
    <li><router-link to="/gauge/list">Gauges</router-link></li>
    <li v-if="isAdmin"><router-link to="/admin/users">Users</router-link></li>
  </ul>
  <div class="nav-user" v-if="user">
    <span>{{ user.displayName }}</span>
    <button @click="logout">Logout</button>
  </div>
</nav>
```

### 5.8 Existing page changes

All existing pages that call the API must switch from raw `fetch('/api/...')` to using the `api()` helper from `auth/api.js`. This ensures the JWT is attached to every request and 401s are handled globally.

---

## 6. File Inventory

### New files to create:

| Path | Type |
|------|------|
| `lib/src/main/java/library/entities/User.java` | Java entity |
| `lib/src/main/java/library/repositories/UserRepository.java` | Java repository |
| `lib/src/main/java/library/security/JwtUtil.java` | JWT utility |
| `lib/src/main/java/library/security/JwtAuthenticationFilter.java` | Security filter |
| `lib/src/main/java/library/security/SecurityConfig.java` | Spring Security config |
| `lib/src/main/java/library/security/CurrentUser.java` | Helper to get current user |
| `lib/src/main/java/library/controllers/AuthController.java` | Login + bootstrap |
| `lib/src/main/java/library/controllers/AdminController.java` | User management |
| `library-vue/src/auth/Login.vue` | Login page |
| `library-vue/src/auth/Bootstrap.vue` | First-run setup page |
| `library-vue/src/auth/api.js` | Shared API client with JWT |
| `library-vue/src/admin/Users.vue` | Admin user management |

### Files to modify:

| Path | Change |
|------|--------|
| `library-docker/mysql/boot.sql` | Add `users` table, add `user_id` column to books/authors/series/gauges, adjust unique constraints |
| `library-docker/mysql/data.sql` | Update seed data to reference a seeded admin user |
| `lib/build.gradle` | Add Spring Security + jjwt dependencies |
| `lib/src/main/resources/application.properties` | Add JWT config properties |
| `lib/src/main/java/library/entities/Book.java` | Add `User user` field |
| `lib/src/main/java/library/entities/Author.java` | Add `User user` field |
| `lib/src/main/java/library/entities/Series.java` | Add `User user` field |
| `lib/src/main/java/library/entities/Gauge.java` | Add `User user` field |
| `lib/src/main/java/library/repositories/BookRepository.java` | Add `findAllByUserId` method |
| `lib/src/main/java/library/repositories/AuthorRepository.java` | Add `findAllByUserId`, `findByIdAndUserId` |
| `lib/src/main/java/library/repositories/SeriesRepository.java` | Add `findAllByUserId`, `findByIdAndUserId` |
| `lib/src/main/java/library/repositories/GaugeRepository.java` | Add `findAllWithEntriesByUserId` |
| `lib/src/main/java/library/controllers/BookController.java` | Scope all queries by `CurrentUser.id()`, set user on create |
| `lib/src/main/java/library/controllers/AuthorController.java` | Scope all queries by user |
| `lib/src/main/java/library/controllers/SeriesController.java` | Scope all queries by user |
| `lib/src/main/java/library/controllers/GaugeController.java` | Scope all queries by user |
| `library-vue/src/router.js` | Add auth routes, navigation guard |
| `library-vue/src/App.vue` | Add user display + logout to nav, admin link |
| `library-vue/src/book/List.vue` | Use `api()` helper |
| `library-vue/src/book/New.vue` | Use `api()` helper |
| `library-vue/src/book/Edit.vue` | Use `api()` helper |
| `library-vue/src/gauge/List.vue` | Use `api()` helper |
| `library-vue/src/gauge/New.vue` | Use `api()` helper |
| `library-vue/src/gauge/Detail.vue` | Use `api()` helper |
| `library-vue/src/Home.vue` | Use `api()` helper |

---

## 7. Implementation Order

1. **Dependencies** — Add Spring Security + jjwt to `build.gradle`. Verify project compiles.
2. **Database** — Update `boot.sql` with `users` table and `user_id` columns. Rebuild Docker container.
3. **Entity + Repository** — Create `User.java` and `UserRepository.java`.
4. **Security infrastructure** — Create `JwtUtil`, `JwtAuthenticationFilter`, `SecurityConfig`, `CurrentUser`. Add properties. Verify app starts (all endpoints now return 401).
5. **Auth controller** — Create `AuthController` with login + bootstrap. Test with curl.
6. **Data isolation** — Add `user` field to Book, Author, Series, Gauge entities. Update repositories. Update all controllers to scope by user and set user on create.
7. **Admin controller** — Create `AdminController`. Test with curl.
8. **Frontend API client** — Create `auth/api.js`. Migrate all existing pages to use it.
9. **Frontend auth pages** — Create `Login.vue`, `Bootstrap.vue`. Update router with guard.
10. **Frontend admin** — Create `admin/Users.vue`. Update `App.vue` nav bar.
11. **Testing** — Write controller integration tests. Manual E2E verification.

---

## 8. Testing Strategy

### Backend (JUnit 5 + Spring Boot Test)

Create `lib/src/test/java/library/controllers/AuthControllerTest.java`:

- **POST /auth/bootstrap** — creates admin when no users exist, returns 201 + token.
- **POST /auth/bootstrap (second call)** — returns 403.
- **POST /auth/login** — valid credentials return token.
- **POST /auth/login** — invalid credentials return 401.
- **Authenticated request** — valid token accesses data.
- **Unauthenticated request** — missing token returns 401.

Create `lib/src/test/java/library/controllers/AdminControllerTest.java`:

- **GET /admin/users** — admin can list users.
- **POST /admin/users** — admin can create user.
- **POST /admin/users (duplicate email)** — returns 409.
- **DELETE /admin/users/:id** — admin can delete non-admin user.
- **Non-admin accessing /admin/** — returns 403.

Data isolation tests:

- **User A creates a book** — User B cannot see it via `GET /all`.
- **User A's book ID** — User B gets 404 trying to access it.

### Frontend (manual for v1)

- Verify unauthenticated user is redirected to `/login`.
- Bootstrap flow creates admin and redirects to home.
- Login with valid credentials stores token and shows library.
- Login with invalid credentials shows error.
- Logout clears token and redirects to login.
- Admin can access `/admin/users`, non-admin cannot.
- Each user sees only their own books/gauges.

---

## 9. Edge Cases & Validation

| Case | Handling |
|------|----------|
| No admin exists (first run) | `/auth/bootstrap` is open; login page detects this and offers setup link |
| Token expired | Frontend receives 401, clears auth, redirects to login |
| Admin deletes themselves | Prevented — cannot delete admin role |
| User deleted while logged in | Next API call returns 401 (user lookup fails in filter) |
| Email format invalid | Return 400 (validate in controller or with `@Email` annotation) |
| Password too short | Return 400 — enforce minimum 8 characters in controller |
| Display name blank | Return 400 |
| Duplicate email on invite | Return 409 |
| Non-admin hits admin endpoint | Spring Security returns 403 |
| CORS (if frontend served separately) | Configure CORS in `SecurityConfig` if needed (not needed with Vite proxy in dev) |

---

## 10. Future Considerations (Out of Scope)

- Friend visibility / social features (designed for — `user_id` scoping makes it possible to selectively show other users' data later).
- Password reset flow (email-based).
- User profile / settings page.
- OAuth / social login.
- Self-registration.
- Rate limiting on login endpoint.
- Refresh tokens (single long-lived JWT is acceptable for a small trusted group).
