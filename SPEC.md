# Gainsmaxxing — Implementation Spec

Living document for **what is built**, **how it behaves**, and **what to build next**. Product vision and design rules stay in [AGENTS.md](AGENTS.md). Update this file at the end of each implementation session.

---

## Milestone status

| Area | Status |
|------|--------|
| Workout tab (data + UX) | **Done** (Milestone 1) |
| User preferences (partial) | **Done** |
| Home tab (data) | **Done** (Milestone 2) |
| Calendar tab (data) | Not started (Milestone 3) |
| Strava integration | Deferred |
| Export data | Deferred |

---

## Architecture (established)

```
ui/          Stateless Composables + ViewModels (per feature)
domain/      Plain models + pure logic (no Android imports)
data/
  db/        Room entities, DAOs, AppDatabase (v2)
  mapper/    Entity ↔ domain
  repository/ Sole data access from ViewModels
di/          Hilt modules
```

**Rules**

- Weights stored in **kg** in Room; UI converts for lbs via `WeightFormat`.
- Exercise identity: **free-text names**, global list — same name = shared history.
- Bodyweight exercises: log **reps + added weight** (`isBodyweight` on exercise).
- In-workout set PRs (trophy) ≠ Home strength PRs (manual 1RM history, free-text names).

---

## Milestone 1 — Workout (DONE)

(See git history / prior sections — unchanged.)

---

## Milestone 2 — Home (DONE)

### Room schema (v2 additions)

| Table | Purpose |
|-------|---------|
| `bodyweight_entries` | `date` (PK), `weightKg` — one per calendar date |
| `sleep_entries` | `date` (PK), `hours`, `energyTag` (`SLEEPY` / `NEUTRAL` / `ENERGISED`) |
| `strength_pr_entries` | `exerciseName`, `oneRmKg`, `loggedAtEpochMs` — history per exercise |
| `strength_pr_selection` | `exerciseName` (PK), `sortOrder` — up to 4 exercises on Home |

Migration: `MIGRATION_1_2` in `data/db/Migrations.kt`. Schema export: `app/schemas/` (v2).

### Repositories

- **`BodyMetricsRepository`** — bodyweight (26-week window); sleep chart via `observeSleepChart()` + `SleepChartSlots`; upsert per date.
- **`StrengthPrRepository`** — selection list, 1RM entry history, summaries for Home cards.
- **`UserPreferencesRepository`** — profile name, weight unit (unchanged).

### Domain logic

- **`StrengthPrComparison`** — latest/previous entry, delta formatting vs prior 1RM.
- **`SleepChartSlots`** — 30-bar sleep chart layout (left-anchored until 30 days of history, then rolling latest 30 calendar days).
- **`SleepFormat`** — hours + minutes ↔ stored `hours` float; display as `7h 30m`.

### Home tab behavior

**Body metrics**

- Log via **outline-circle +** on Bodyweight / Sleep section headers (bottom sheets).
- One entry per date; re-log overwrites. Sleep date = wake-up morning.
- Charts read-only; bodyweight shows ~26 weeks; sleep always renders **30 bar slots**.
- **Sleep chart layout:** first logs anchor **left** and grow right; once span ≥ 30 days, show rolling latest 30 calendar days (oldest left, today right). Empty slots = no bar. Y-axis ticks include **9h**.
- **Sleep log sheet:** separate hours (0–14) and minutes (0–59) with ±1 and tap-to-edit (same pattern as weight/reps).
- **Bodyweight chart:** chronological left → right.

**Strength PRs**

- **Settings → Strength Records**: configure up to 4 free-text exercise names.
- Home grid: latest 1RM + delta vs previous (`—` on first entry); grid height fits row count (1 row for 1–2 exercises).
- Tap card → detail overlay: 1RM chart + dated entry list (read-only).
- **+** on detail → log new 1RM bottom sheet.
- **1RM / top-set charts:** chronological left → right (tie-break same-day entries by `loggedAt` / `startedAt`).

**Running PRs**

- Empty state until Strava milestone (“Running records will sync from Strava”).

### Tests

- `StrengthPrComparisonTest`, `SleepChartSlotsTest` under `app/src/test/`.

---

## Milestone 3 — Calendar (NEXT)

1. **Room** — activity types (color, icon key), weekly template slots (day × morning/evening), per-date skip overrides.
2. **Repository** — `CalendarRepository`.
3. **ViewModel** — `CalendarViewModel`; remove hardcoded schedule in `CalendarScreen`.
4. **Settings → Edit Calendar** — slot editor (toggle activity / empty); activity type management.

Skip flags are **per calendar date**, not on the template.

---

## Milestone 4 — Strava

1. OAuth flow (no manual token paste unless revisited).
2. `data/remote/` — Retrofit, DTOs, mappers.
3. Sync runs; derive running PRs for Home distances user selects in settings.
4. Running PR section replaces placeholders.

---

## Milestone 5 — Export data

- Format TBD (JSON vs CSV) when implementing.
- Settings → Export Data.

---

## Decisions log (don't re-litigate without user)

| Topic | Decision |
|-------|----------|
| First install | Empty — no seeded mock data |
| Weight storage | kg internal; display unit from prefs |
| Strava timing | After local features |
| Notifications | Removed from UI; not planned |
| Active workout persistence | Auto-save every set; resume on reopen |
| Minimize vs discard vs finish | Back = minimize; X = discard; Finish = save |
| History / active overlay dismiss | No swipe; back or explicit buttons only |
| Strength PR identity | Free-text `exerciseName`, not linked to workout catalog |
| Strength PR UX | Settings picks exercises; detail screen for history + log |
| Body/sleep edit | Log only via +; past entries read-only on charts |
| Running tab until Strava | Empty state, no mock data |

---

## Files map (home slice)

| Layer | Key files |
|-------|-----------|
| UI | `HomeScreen.kt`, `HomeViewModel.kt`, `HomeLogSheets.kt`, `StrengthPrDetailScreen.kt`, `StrengthPrSettingsScreen.kt`, `SettingsSheet.kt` |
| Data | `BodyMetricsRepository.kt`, `StrengthPrRepository.kt`, `BodyMetricsDao.kt`, `StrengthPrDao.kt`, `HomeMappers.kt`, `Migrations.kt` |
| Domain | `BodyweightEntry.kt`, `SleepEntry.kt`, `EnergyTag.kt`, `StrengthPrEntry.kt`, `StrengthPrComparison.kt`, `SleepChartSlots.kt`, `SleepFormat.kt` |

---

## Session checklist (for agents)

When starting work:

1. Read this file for status and next milestone.
2. Read [AGENTS.md](AGENTS.md) for product/design constraints.
3. Work on a `feat/` or `fix/` branch — never `main`.

When finishing work:

1. Update **Milestone status** and relevant sections here.
2. Ensure [AGENTS.md](AGENTS.md) still points here (no duplicate gap list).
