# Gauge System – Design Doc

## Overview

This document details the implementation plan for the gauge system described in `shaping.md`. It specifies exact file paths, class structures, SQL, API contracts, and Vue component designs, following the patterns already established in this project.

---

## 1. Database

### 1.1 Schema additions (append to `library-docker/mysql/boot.sql`)

```sql
-- Gauges

CREATE TABLE gauges (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at  DATETIME NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE gauge_entries (
    id         BIGINT NOT NULL AUTO_INCREMENT,
    gauge_id   BIGINT NOT NULL,
    delta      INT NOT NULL,
    note       VARCHAR(255),
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (gauge_id) REFERENCES gauges(id) ON DELETE CASCADE
);
```

`ON DELETE CASCADE` ensures that deleting a gauge removes its entries without requiring application-level cleanup.

---

## 2. Backend – Java (Spring Boot)

All new files live under `lib/src/main/java/library/`.

### 2.1 Entity: `entities/Gauge.java`

```java
package library.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "gauges")
public class Gauge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @JsonIgnore
    @OneToMany(mappedBy = "gauge", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<GaugeEntry> entries = new ArrayList<>();

    @Transient
    private Integer value;

    public Integer getValue() {
        if (value != null) return value;
        return entries.stream().mapToInt(GaugeEntry::getDelta).sum();
    }
}
```

### 2.2 Entity: `entities/GaugeEntry.java`

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
@Table(name = "gauge_entries")
public class GaugeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gauge_id", nullable = false)
    private Gauge gauge;

    @Column(nullable = false)
    private Integer delta;

    @Column(length = 255)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
```

### 2.3 Repository: `repositories/GaugeRepository.java`

```java
package library.repositories;

import library.entities.Gauge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GaugeRepository extends JpaRepository<Gauge, Long> {

    @Query("SELECT g FROM Gauge g LEFT JOIN FETCH g.entries")
    List<Gauge> findAllWithEntries();
}
```

### 2.4 Repository: `repositories/GaugeEntryRepository.java`

```java
package library.repositories;

import library.entities.GaugeEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GaugeEntryRepository extends JpaRepository<GaugeEntry, Long> {

    List<GaugeEntry> findByGaugeIdOrderByCreatedAtAsc(Long gaugeId);
}
```

### 2.5 Controller: `controllers/GaugeController.java`

```java
package library.controllers;

import library.entities.Gauge;
import library.entities.GaugeEntry;
import library.repositories.GaugeEntryRepository;
import library.repositories.GaugeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gauges")
public class GaugeController {

    private final GaugeRepository gaugeRepository;
    private final GaugeEntryRepository gaugeEntryRepository;

    public GaugeController(GaugeRepository gaugeRepository,
                           GaugeEntryRepository gaugeEntryRepository) {
        this.gaugeRepository = gaugeRepository;
        this.gaugeEntryRepository = gaugeEntryRepository;
    }

    // ── Gauge CRUD ──────────────────────────────────────────

    @GetMapping
    public List<Gauge> getAll() {
        return gaugeRepository.findAllWithEntries();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Gauge create(@RequestBody Map<String, Object> body) {
        Gauge gauge = new Gauge();
        gauge.setName((String) body.get("name"));
        gauge.setDescription((String) body.get("description"));
        gauge.setCreatedAt(LocalDateTime.now());
        return gaugeRepository.save(gauge);
    }

    @GetMapping("/{id}")
    public Gauge getById(@PathVariable Long id) {
        return gaugeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gauge not found"));
    }

    @PutMapping("/{id}")
    public Gauge update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Gauge gauge = gaugeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gauge not found"));
        if (body.containsKey("name")) {
            gauge.setName((String) body.get("name"));
        }
        if (body.containsKey("description")) {
            gauge.setDescription((String) body.get("description"));
        }
        return gaugeRepository.save(gauge);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Gauge gauge = gaugeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gauge not found"));
        gaugeRepository.delete(gauge);
    }

    // ── Entries ─────────────────────────────────────────────

    @GetMapping("/{id}/entries")
    public List<GaugeEntry> getEntries(@PathVariable Long id) {
        if (!gaugeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Gauge not found");
        }
        return gaugeEntryRepository.findByGaugeIdOrderByCreatedAtAsc(id);
    }

    @Transactional
    @PostMapping("/{id}/entries")
    @ResponseStatus(HttpStatus.CREATED)
    public GaugeEntry addEntry(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Gauge gauge = gaugeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gauge not found"));

        GaugeEntry entry = new GaugeEntry();
        entry.setGauge(gauge);
        entry.setDelta(((Number) body.get("delta")).intValue());
        entry.setNote((String) body.get("note"));
        entry.setCreatedAt(LocalDateTime.now());
        return gaugeEntryRepository.save(entry);
    }

    @DeleteMapping("/{id}/entries/{entryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEntry(@PathVariable Long id, @PathVariable Long entryId) {
        GaugeEntry entry = gaugeEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));
        if (!entry.getGauge().getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entry does not belong to this gauge");
        }
        gaugeEntryRepository.delete(entry);
    }
}
```

---

## 3. API Contract

Base URL: `/api/gauges` (Vite proxy rewrites `/api` → backend root)

### 3.1 Gauges

| Method | Path | Request Body | Response | Status |
|--------|------|--------------|----------|--------|
| GET | `/api/gauges` | — | `Gauge[]` (each with `value`) | 200 |
| POST | `/api/gauges` | `{ name, description? }` | `Gauge` | 201 |
| GET | `/api/gauges/:id` | — | `Gauge` (with `value`) | 200 |
| PUT | `/api/gauges/:id` | `{ name?, description? }` | `Gauge` | 200 |
| DELETE | `/api/gauges/:id` | — | — | 204 |

### 3.2 Entries

| Method | Path | Request Body | Response | Status |
|--------|------|--------------|----------|--------|
| GET | `/api/gauges/:id/entries` | — | `GaugeEntry[]` | 200 |
| POST | `/api/gauges/:id/entries` | `{ delta, note? }` | `GaugeEntry` | 201 |
| DELETE | `/api/gauges/:id/entries/:entryId` | — | — | 204 |

### 3.3 Response shapes

**Gauge:**
```json
{
  "id": 1,
  "name": "Books Read vs Bought",
  "description": "Positive = ahead on reading",
  "value": 3,
  "createdAt": "2026-01-15T10:00:00"
}
```

**GaugeEntry:**
```json
{
  "id": 42,
  "delta": -2,
  "note": "Bought 2 books at the store",
  "createdAt": "2026-05-01T14:30:00"
}
```

---

## 4. Frontend – Vue

All new files live under `library-vue/src/`.

### 4.1 Routing (`router.js` additions)

```javascript
import GaugeList from "./gauge/List.vue";
import GaugeDetail from "./gauge/Detail.vue";
import GaugeNew from "./gauge/New.vue";

// Add to routes array:
{ path: '/gauge/list', component: GaugeList },
{ path: '/gauge/new', component: GaugeNew },
{ path: '/gauge/:id', component: GaugeDetail }
```

### 4.2 Page: `gauge/List.vue`

**Responsibilities:**
- Fetch `GET /api/gauges` on mount.
- Display each gauge as a card showing name, description, and current value.
- Each card contains a `<GaugeDisplay>` component for visual representation.
- "Add Gauge" button links to `/gauge/new`.
- Clicking a card navigates to `/gauge/:id`.

**Data flow:**
```
mounted() → fetch /api/gauges → gauges[]
```

### 4.3 Page: `gauge/Detail.vue`

**Responsibilities:**
- Fetch gauge and entries on mount (`GET /api/gauges/:id` + `GET /api/gauges/:id/entries`).
- Display the gauge header (name, description, current value with `<GaugeDisplay>`).
- Show a `<GaugeHistoryChart>` plotting cumulative value over time.
- Show an entry table listing all entries (date, delta, note, delete button).
- Include an `<EntryForm>` to post new entries.
- On new entry submission: `POST /api/gauges/:id/entries` → prepend to list → recalculate chart.

**Data flow:**
```
mounted() → Promise.all([fetchGauge, fetchEntries])
addEntry() → POST → refresh entries + value
deleteEntry() → DELETE → refresh entries + value
```

### 4.4 Page: `gauge/New.vue`

**Responsibilities:**
- Simple form with fields: name (required), description (optional).
- On submit: `POST /api/gauges` → navigate to `/gauge/list`.

**Template structure:**
```html
<form @submit.prevent="save">
  <div class="field">
    <label>Name</label>
    <input v-model="form.name" required />
  </div>
  <div class="field">
    <label>Description</label>
    <textarea v-model="form.description" rows="3"></textarea>
  </div>
  <div class="actions">
    <button type="submit">Create Gauge</button>
    <button type="button" @click="$router.push('/gauge/list')">Cancel</button>
  </div>
</form>
```

### 4.5 Component: `components/GaugeDisplay.vue`

**Props:**
- `value` (Number) — the current gauge value
- `min` (Number, default: -20) — left bound of the visual range
- `max` (Number, default: 20) — right bound of the visual range

**Visual design:**
- Horizontal bar with a center line at zero.
- A filled region extends from center to the right (positive) or left (negative).
- Color: green for positive values, red for negative.
- Numeric value displayed above/below the bar.

**Implementation approach:**
- Pure CSS with a `width` percentage calculated from value relative to range.
- No external charting library needed.

### 4.6 Component: `components/GaugeHistoryChart.vue`

**Props:**
- `entries` (Array) — the full list of `GaugeEntry` objects

**Behavior:**
- Computes a running cumulative sum from entries (sorted by `createdAt`).
- Renders a simple SVG line chart (no external library for v1).
- X-axis: time, Y-axis: cumulative value.
- Points are plotted and connected with a polyline.
- Zero line drawn as a dashed horizontal reference.

**Alternative (if SVG proves too cumbersome):**
- Use a minimal chart library like `chart.js` with `vue-chartjs`. Only add this dep if SVG approach is rejected during implementation.

### 4.7 Component: `components/EntryForm.vue`

**Props:**
- None (emits events)

**Emits:**
- `submit` with payload `{ delta: Number, note: String }`

**Template:**
```html
<form @submit.prevent="onSubmit" class="entry-form">
  <input v-model.number="delta" type="number" placeholder="+1 or -1" required />
  <input v-model="note" type="text" placeholder="Note (optional)" />
  <button type="submit">Add</button>
</form>
```

**UX considerations:**
- Quick-action buttons: "+1 Read" (delta=+1) and "−1 Bought" (delta=−1) for the common case.
- Custom delta input for bulk entries (e.g. "bought 5 books" → delta = -5).

### 4.8 Home page widget (`Home.vue` update)

Add a section that fetches all gauges and displays a compact `<GaugeDisplay>` for each:

```html
<div class="gauge-summary" v-if="gauges.length">
  <h2>Gauges</h2>
  <div v-for="g in gauges" :key="g.id" class="gauge-card">
    <router-link :to="'/gauge/' + g.id">
      <strong>{{ g.name }}</strong>
      <GaugeDisplay :value="g.value" />
    </router-link>
  </div>
</div>
```

---

## 5. File Inventory

### New files to create:

| Path | Type |
|------|------|
| `lib/src/main/java/library/entities/Gauge.java` | Java entity |
| `lib/src/main/java/library/entities/GaugeEntry.java` | Java entity |
| `lib/src/main/java/library/repositories/GaugeRepository.java` | Java repository |
| `lib/src/main/java/library/repositories/GaugeEntryRepository.java` | Java repository |
| `lib/src/main/java/library/controllers/GaugeController.java` | Java controller |
| `library-vue/src/gauge/List.vue` | Vue page |
| `library-vue/src/gauge/Detail.vue` | Vue page |
| `library-vue/src/gauge/New.vue` | Vue page |
| `library-vue/src/components/GaugeDisplay.vue` | Vue component |
| `library-vue/src/components/GaugeHistoryChart.vue` | Vue component |
| `library-vue/src/components/EntryForm.vue` | Vue component |

### Files to modify:

| Path | Change |
|------|--------|
| `library-docker/mysql/boot.sql` | Append `gauges` and `gauge_entries` CREATE TABLE statements |
| `library-vue/src/router.js` | Add gauge routes and imports |
| `library-vue/src/Home.vue` | Add gauge summary widget |

---

## 6. Implementation Order

1. **Database** — Add tables to `boot.sql`. Tear down and recreate the Docker MySQL container to apply.
2. **Entities + Repositories** — Create `Gauge.java`, `GaugeEntry.java`, `GaugeRepository.java`, `GaugeEntryRepository.java`.
3. **Controller** — Create `GaugeController.java`. Verify with manual curl/Postman tests.
4. **Vue components** — Create `GaugeDisplay.vue`, `GaugeHistoryChart.vue`, `EntryForm.vue`.
5. **Vue pages** — Create `gauge/List.vue`, `gauge/Detail.vue`, `gauge/New.vue`.
6. **Routing + Home** — Update `router.js` and `Home.vue`.
7. **Testing** — Write controller integration test `GaugeControllerTest.java`.

---

## 7. Testing Strategy

### Backend (JUnit 5 + Spring Boot Test)

Create `lib/src/test/java/library/controllers/GaugeControllerTest.java`:

- **POST /gauges** — creates gauge, returns 201 with body.
- **GET /gauges** — returns list with computed values.
- **POST /gauges/{id}/entries** — adds entry, verify value changes.
- **DELETE /gauges/{id}** — cascade deletes entries.
- **DELETE /gauges/{id}/entries/{entryId}** — verify entry removed, value recalculated.
- **404 cases** — non-existent gauge/entry IDs.

Use `@SpringBootTest` with `webEnvironment = RANDOM_PORT` and `TestRestTemplate`, matching existing test patterns.

### Frontend (manual for v1)

- Verify gauge list loads and displays values.
- Create a gauge, add entries, confirm value updates.
- Delete entries, confirm value recalculates.
- Navigate between pages, confirm routing works.

---

## 8. Edge Cases & Validation

| Case | Handling |
|------|----------|
| Gauge name is blank/null | Return 400 — Spring validation or controller check |
| Gauge name already exists | Return 409 (or let DB unique constraint produce 500 → future: handle gracefully) |
| Delta is zero | Allow it (no-op entry, useful as a note/marker) |
| Delta is null/missing | Return 400 |
| Gauge has no entries | `value` = 0 (sum of empty list) |
| Delete gauge with many entries | CASCADE handles it at DB level |
| Entry does not belong to gauge in URL | Return 400 with message |

---

## 9. Future Considerations (Out of Scope)

- Automatic gauge updates when book status changes to "Completed" or when a new book is created.
- Gauge goals/targets with progress indicators.
- Date-range filtering on entry history.
- Multiple gauge types (percentage, absolute, rate-per-month).

