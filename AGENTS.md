# Gainsmaxxing

This is a personal mobile app for tracking fitness. A daily tool to save workouts or check on user's PRs and body metrics. The app must be easy to use, distraction-free and snappy.

## App Structure

- Tab 1: Calendar
- Tab 2: Home
- Tab 3: Workout

### Tab 1 - Calendar

The purpose of the calendar is to give the user a good visualisation of their physical activities across the week, remind the user of an activity they have to perform and track skipped activities.

The calendar is not meant to be edited often, the schedule is mostly fixed, but with an option to edit everything about the activities (what they are, reschedule them, change colors and icons). No settings/edit options on the calendar page.

**Time slots**
There are two time slots for an activity on each day: Morning and Evening.

**Activity types**
Current activity types include: long runs, short runs, tempo runs, sprinting, gym, swimming and basketball. User must be able to change these activity types if his intentions change. Each activity is labeled with a color and an icon. Activities from the same family, such as long and short runs, should share the same color, but use different icons. Both should be different for unrelated activities (eg. swimming, running and basketball).

**Layout**
A weekly view. The full week (Mon–Sun) is visible at once. The user can swipe left/right to navigate between weeks.

The days in the calendar are rows, the time slots should be columns. This way the whole week can fit on the mobile screen.

**Skipping an activity**
In the calendar you can tap on an activity to flag it as skipped. A skipped activity should be grey instead of colorful.
Over time this creates a visual compliance record — the user can see patterns like "I always skip Friday gym."

### Tab 2 — Home

The landing screen. Answers "how am I progressing?" at a glance.

**Personal Records section**

- Two categories: Strength (manual-entry 1RM records only) and Running (pulled from Strava automatically)
- User can select which and how many exercises or distances to show records for.

**Body Metrics section**

- Bodyweight graph: a smooth and well animated line chart showing all logged weigh-ins over time. No target lines, no zones — just the raw trend.
- Sleep overview: a bar chart showing sleep duration per night for the last 30 days. Each bar is colour-coded by the energy tag logged that morning (Sleepy / Neutral / Energised — use three distinct, readable colours that make sense in this case). Tapping any bar shows the date, hours slept, and energy tag.

**Profile**

- The home screen greets the user with their name.
- In the top right corner is a Profile icon. Clicking on it expands profile information and app settings

**Settings**
The settings include:

- Exercises to show in the Strength Records
- Distances to show in the Running Records
- Editing calendar (shows the calendar view where you tap on an activity slot to toggle between different activities and no activity)
- Editing workout split
- and a couple more minor useful settings such as exporting data.

### Tab 3 - Workout

**Split overview (default view)**
The user will be able to add a workout split (eg. Upper Lower) to the app and edit it down the line (rarely). No settings/edit options in the workout tab.

At the top of the tab are days in a week (Mon-Sun). A workout template is shown for the selected (current by default) day of the week. Below the template is a button to start tracking a workout. Clicking it opens the active workout screen.

**Active workout screen**

- Displays the exercises and number of sets in the template workout in order, so the user can follow the workout plan.
- Each exercise is marked with the number of sets the user has to perform, and a reference weight and rep count taken from the last workout. Each exercise also has a button to log a set, which opens a bottom sheet with a weight and number of reps input, both additionally controlled with buttons to increment or decrement quickly. 
- A logged set appears as a small pill in the exercise card. If a logged set uses more weight than any previous set for that exercise, show a small, non-intrusive PR indicator on that set tag (e.g. a trophy icon). This set PR stays within the workout/exercise-history UI and does not update the Home tab Personal Records.
- A "Finish workout" button at the bottom saves the session and returns to the split overview.

**Exercise history (sub-screen)**

- Accessed by long-pressing or tapping an info icon on any exercise name.
- Shows a chronological list of every logged session for that exercise: date, and all sets performed (weight × reps).
- Also shows a small line chart of the top-set weight over time.
- No editing past sessions — read only.

## Design

### Colors

- The app is only available in dark mode.

### Animations

- Animations are welcome. They make using an app more enjoyable, which is what we want with this one.
- Use micro interactions for visual feedback where the outcome is unknown. Don't overuse, it can get annoying if overused.
- Remember - every interaction needs a response.

### Accessibility

- Sufficient contrast even on less important, dimmed text (it can't be so dark it's hard to see).
- Responsive, snappy design.

## Not Included In The App

- Onboarding, or logging in. This is a personal app used by only one user.
- Counting macros, or calories burned.
- Workout suggestions, presets

## Tech Stack

Native Android app, single user. Kotlin + Jetpack Compose (Material 3, dark theme only).

Decided so far:

- **Local storage** (no remote backend).
- **Hilt** for dependency injection.
- **Strava API** for run data.
- **Lucide** for icons.

UI-specific tooling (charts, etc.) is not pinned here — I'll give you the exact UI to recreate in Kotlin and you choose the best tool for it. Anything else undecided, pick per the rule in [Important Rules](#important-rules) when the need comes up.

Architecture is MVVM: stateless Composables ← ViewModel (exposes UI state) ← Repository ← Room / Strava. ViewModels and Composables never touch a DAO or the network directly — always go through a repository. Pure logic (1RM estimate, PR comparison, week math) lives in plain Kotlin with no Android imports so it's unit-testable.

## Conventions

### Running the app

Tested on a physical Android device (or emulator) via Gradle. There's no hot reload — every change is a rebuild + reinstall.

- **Build & install:** `./gradlew installDebug`
- **Compile / quick check:** `./gradlew compileDebugKotlin`
- **Unit tests:** `./gradlew testDebugUnitTest`

Use the Gradle wrapper (`./gradlew`).

### Folder layout

Single `app` module, package `com.gainsmaxxing` under `app/src/main/java/com/gainsmaxxing/`:

```
ui/
  calendar/        # CalendarScreen + CalendarViewModel
  home/            # HomeScreen + HomeViewModel
  workout/         # SplitOverview, ActiveWorkout, ExerciseHistory + ViewModels
  components/      # reusable Composables (pills, bottom sheets, chart wrappers…)
  theme/           # Color.kt, Type.kt, Theme.kt — single source of truth
  navigation/      # NavHost, routes, tab scaffold
data/
  db/              # Room: entities/, dao/, AppDatabase, Converters
  repository/      # *Repository — the only way UI reaches data
  remote/          # Strava: Retrofit service, DTOs, OAuth, mappers
domain/
  model/           # plain Kotlin domain models (Compose/Room-free)
  ...              # pure logic: OneRepMax, PrComparison, WeekMath…
di/                # Hilt modules
```

Unit tests mirror this under `app/src/test/java/com/gainsmaxxing/`.

### Code style

- Colours, spacing and typography come from `ui/theme/` only — no hardcoded values in Composables.
- Data access goes through a repository only — never a DAO or network call from UI/ViewModel.
- Keep domain models separate from Room entities / Strava DTOs; map between them in the repository.
- Hoist state: Composables stay stateless, state down / events up.

## Keeping This File Current

As libraries get chosen we do some work, update the [Tech Stack](#tech-stack) section — keep it lean, only what a fresh session would otherwise get wrong.

**Implementation status and next milestones** live in [SPEC.md](SPEC.md). Update that file at the end of each session; do not duplicate a gap list here.

## Important Rules

- Before adding a library for new functionality, show me the options. If there's a clear best-fit winner, just name it and use it.
- Keep the code clean and **easily testable**. Deep modules — hide complex logic behind simple interfaces. Pure logic stays in `domain/` with unit tests.
- When working on the UI always use the theme, colors and text styles we set up.

## Git Rules

### Branch

- Don't touch any files on the main branch. Always create a new branch or work on an existing one
- Naming: feat/, fix/; use a more general name if the work might involve more than just the one single change
- Never commit directly to main or develop

### Commits

- Use Conventional Commits: feat:, fix:, refactor:, chore:, docs:
- Keep commit messages under 72 characters, imperative mood
- One logical change per commit — don't bundle unrelated changes

### PR hygiene

- Break huge tasks into smaller PRs, but a PR can be pretty large if focused on one thing
- Never force push
- Never amend a commit that has already been pushed
- Never make draft PRs, unless told otherwise

### What requires my confirmation

- git push (ask me before pushing; I'll determine if we are done or need more changes)

