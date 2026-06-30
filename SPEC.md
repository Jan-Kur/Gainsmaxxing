# Gainsmaxxing — Implementation Spec

Living document for **what is built**, **how it behaves**, and **what to build next**. Product vision and design rules stay in [AGENTS.md](AGENTS.md). Update this file at the end of each implementation session.

---

## Milestone status

| Area | Status |
|------|--------|
| Workout tab (data + UX) | **Done** (Milestone 1) |
| User preferences (partial) | **Done** |
| Home tab (data) | Not started (Milestone 2) |
| Calendar tab (data) | Not started (Milestone 3) |
| Strava integration | Deferred |
| Export data | Deferred |

---

## Architecture (established)

```
ui/          Stateless Composables + ViewModels (per feature)
domain/      Plain models + pure logic (no Android imports)
data/
  db/        Room entities, DAOs, AppDatabase (v1)
  mapper/    Entity ↔ domain
  repository/ Sole data access from ViewModels
di/          Hilt modules
```

**Rules**

- Weights stored in **kg** in Room; UI converts for lbs via `WeightFormat`.
- Exercise identity: **free-text names**, global list — same name = shared history.
- Bodyweight exercises: log **reps + added weight** (`isBodyweight` on exercise).
- In-workout set PRs (trophy) ≠ Home strength PRs (manual 1RM, not built yet).

---

## Milestone 1 — Workout (DONE)

### Room schema (v1)

| Table | Purpose |
|-------|---------|
| `exercises` | Global exercise catalog (`name` unique, `isBodyweight`) |
| `split_days` | `dayOfWeek` 0–6 (Mon–Sun), optional `workoutName` |
| `template_exercises` | Exercises per day: `targetSets`, `targetReps`, `sortOrder` |
| `workout_sessions` | `IN_PROGRESS` or `COMPLETED`, `startedAtEpochMs`, `dayOfWeek`, `workoutName` |
| `session_sets` | `weightKg`, `reps`, `isWarmup`, `sortOrder` per session |
| `user_preferences` | Singleton: `profileName`, `weightUnit` (`KG` / `LBS`) |

Schema export: `app/schemas/`. Bump version + migration when adding tables.

### Repositories

- **`WorkoutRepository`** — split, sessions, sets, exercise history, last-session reference per exercise.
- **`UserPreferencesRepository`** — profile name, weight unit, defaults on first launch (`Athlete`, kg).

### Domain logic

- `SetComparison` — top working weight, PR flags, chart tick steps.
- `WeightFormat` — kg ↔ display unit, formatting; **`STEP_KG = 2.5f`** for log sheet increments.
- `WeekMath` — day names, today's index.

### Workout tab behavior

**Split overview**

- Mon–Sun pills; empty install = all rest days until split is configured.
- Exercise cards show reference line from **last completed session**: working set count, top-set reps, top-set weight. Before history: template sets/reps and `—` / `BW` for weight.
- **Start Workout** when no in-progress session.
- **Resume Workout** when session is `IN_PROGRESS` but active screen is minimized.

**Active workout**

- Full-screen overlay (no swipe-to-dismiss).
- Header: workout name, **elapsed timer** (from `startedAt`, keeps running when minimized or app backgrounded), **X** = discard (delete session).
- **Finish Workout** = mark `COMPLETED`.
- **Android back** = minimize only — session stays `IN_PROGRESS`.
- Log set bottom sheet: weight +/- in **2.5 kg** steps; tap number for manual entry; warmup toggle.
- Set pills; trophy on in-workout PR (vs historical best excluding current session).
- Progress uses template **`targetSets`** (`+ Set 2/4`, Done threshold).

**Exercise history**

- Info icon → read-only list + top-set weight chart.
- Full-screen overlay; back closes (no swipe dismiss).

**Settings → Edit Workout Split**

- Per weekday: rest toggle, workout name, add/remove/reorder exercises.
- Per exercise: name, sets, reps, bodyweight toggle.
- Save per day. Global exercises created on save.

### Home (partial wiring)

- `HomeViewModel` + persisted profile name and weight unit.
- Settings: profile name edit, weight unit toggle, link to split editor.
- Notifications row **removed** (out of scope).
- PRs, bodyweight, sleep charts still **mock data**.

### Calendar

- UI only; no Room.

### Tests

- `SetComparisonTest`, `WeightFormatTest` under `app/src/test/`.

---

## Split editor: what Sets and Reps mean

| Field | Role |
|-------|------|
| **Sets** | Template target for active workout — drives `+ Set n/N` and Done. **Still used after first workout.** |
| **Reps** | Default reps in log sheet **before** any history. After first workout, card + log defaults come from last session. |

---

## Milestone 2 — Home (NEXT)

Wire body metrics and strength PRs. Reuse `UserPreferencesRepository`.

### Scope

1. **Room** — new entities (suggested names):
   - `bodyweight_entries` — `date`, `weightKg`
   - `sleep_entries` — `date`, `hours`, `energyTag` (`SLEEPY` / `NEUTRAL` / `ENERGISED`)
   - `strength_prs` — `exerciseName` or `exerciseId`, `oneRmKg`, `updatedAt`
   - `strength_pr_selection` — which exercises show on Home (user-configurable list)
2. **Repositories** — `BodyMetricsRepository`, `StrengthPrRepository` (or one `HomeRepository`).
3. **ViewModel** — extend `HomeViewModel`; remove mock generators from `HomeScreen`.
4. **Logging UX** — quick-log on Home (+ or tap chart) → bottom sheet for weigh-in and sleep/energy.
5. **Strength PRs** — Settings: pick exercises to display; Home: tap card to edit manual 1RM.
6. **Running PRs** — keep placeholders until Strava milestone.

### Out of scope for M2

- Strava, calendar, export.

---

## Milestone 3 — Calendar

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

---

## Files map (workout slice)

| Layer | Key files |
|-------|-----------|
| UI | `WorkoutScreen.kt`, `WorkoutViewModel.kt`, `SplitEditorScreen.kt`, `SplitEditorViewModel.kt` |
| Home prefs | `HomeScreen.kt`, `HomeViewModel.kt`, `SettingsSheet.kt` |
| Data | `WorkoutRepository.kt`, `UserPreferencesRepository.kt`, `AppDatabase.kt`, `dao/*`, `entities/*` |
| Domain | `domain/model/*`, `SetComparison.kt`, `WeightFormat.kt`, `WeekMath.kt` |
| DI | `DatabaseModule.kt`, `CoroutineModule.kt`, `AppInitializer.kt` |

`data/remote/` does not exist yet.

---

## Session checklist (for agents)

When starting work:

1. Read this file for status and next milestone.
2. Read [AGENTS.md](AGENTS.md) for product/design constraints.
3. Work on a `feat/` or `fix/` branch — never `main`.

When finishing work:

1. Update **Milestone status** and relevant sections here.
2. Ensure [AGENTS.md](AGENTS.md) still points here (no duplicate gap list).
