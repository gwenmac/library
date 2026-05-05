# Gauge System – Shaping Doc

## Problem

There is currently no way to track the ratio of books read versus books bought over time. The user wants a visual "gauge" that shows whether they are reading faster than they are acquiring new books. They also want to support multiple independent gauges so the concept can be reused for other metrics.

---

## Appetite

Small batch – estimated 1–2 days of work across backend and frontend.

---

## Solution

### Concept

A **Gauge** is a named counter with a current value that changes over time. Each change is recorded as an event (a **GaugeEntry**) so the full history is preserved. One gauge might track "Books Read vs Bought" where reading increments the value and buying decrements it, but the system is generic enough to support any counter the user wants.

### Core Objects

| Object | Purpose |
|--------|---------|
| `Gauge` | Named gauge with a current computed value |
| `GaugeEntry` | A single +/− delta recorded at a point in time |

---

## Database Design

### `gauges` table

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT, PK, AUTO_INCREMENT | |
| `name` | VARCHAR(255), NOT NULL, UNIQUE | e.g. "Books Read vs Bought" |
| `description` | TEXT | Optional explanation of what the gauge tracks |
| `created_at` | DATETIME | |

### `gauge_entries` table

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT, PK, AUTO_INCREMENT | |
| `gauge_id` | BIGINT, FK → gauges(id), NOT NULL | |
| `delta` | INT, NOT NULL | Positive = read, Negative = bought (or whatever meaning the user assigns) |
| `note` | VARCHAR(255) | Optional label, e.g. "Bought 3 books" |
| `created_at` | DATETIME | When the event occurred |

The gauge's **current value** is derived from `SUM(delta)` across its entries. No denormalized column is needed at this scale.

---

## Backend (Spring Boot)

### Entity: `Gauge`

- Fields: `id`, `name`, `description`, `createdAt`
- `@OneToMany` relationship to `GaugeEntry`

### Entity: `GaugeEntry`

- Fields: `id`, `gauge` (ManyToOne), `delta`, `note`, `createdAt`

### Repositories

- `GaugeRepository extends JpaRepository<Gauge, Long>`
- `GaugeEntryRepository extends JpaRepository<GaugeEntry, Long>`
  - Custom query: `List<GaugeEntry> findByGaugeIdOrderByCreatedAtAsc(Long gaugeId)`

### Controller: `GaugeController`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/gauges` | List all gauges (with computed current value) |
| POST | `/gauges` | Create a new gauge |
| GET | `/gauges/{id}` | Get gauge detail + current value |
| PUT | `/gauges/{id}` | Update gauge name/description |
| DELETE | `/gauges/{id}` | Delete gauge and all its entries |
| GET | `/gauges/{id}/entries` | Get full history of entries for a gauge |
| POST | `/gauges/{id}/entries` | Add a new entry (delta + optional note) |
| DELETE | `/gauges/{id}/entries/{entryId}` | Remove an entry |

### Response shape for a gauge

```json
{
  "id": 1,
  "name": "Books Read vs Bought",
  "description": "Positive means ahead on reading",
  "value": 3,
  "createdAt": "2026-01-15T10:00:00"
}
```

The `value` field is computed at query time via a `@Query` annotation or in-service summation of entry deltas.

---

## Frontend (Vue)

### New route: `/gauges`

### Pages

| Page | Path | Purpose |
|------|------|---------|
| Gauge List | `/gauges` | Shows all gauges as cards with current value displayed as a visual meter |
| Gauge Detail | `/gauges/:id` | Shows history chart (value over time) and a form to add entries |
| New Gauge | `/gauges/new` | Form to create a gauge |

### Components

- **GaugeDisplay.vue** – Visual meter/bar showing the current value (centered at zero, positive to the right, negative to the left)
- **GaugeHistoryChart.vue** – Line chart showing cumulative value over time (built from entries)
- **EntryForm.vue** – Quick form: delta (number input), note (text), submit button

### Home page integration

Add a summary widget to `Home.vue` showing the primary gauge at a glance.

---

## Rabbit Holes

- **Don't** tie gauges to specific book records via FK. Keep it simple – the user manually logs deltas. Automatic tracking (e.g. auto-incrementing when a book status changes) is a future enhancement.
- **Don't** build complex charting. A simple line or bar chart from entry data is sufficient for v1.
- **Don't** add user accounts or multi-tenancy. This is a single-user system like the rest of the app.

---

## No-Gos

- No automatic syncing with book status changes (future scope).
- No gauge "goals" or target values (future scope).
- No gauge sharing or export.

---

## Cut of Work

1. **Backend**: Migration SQL in `boot.sql`, JPA entities, repositories, controller, tests.
2. **Frontend**: Gauge list page, detail page with history, new gauge form, route registration in `router.js`.
3. **Integration**: Wire Vue pages to backend API, add Home page widget.

