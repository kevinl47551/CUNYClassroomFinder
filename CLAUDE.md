# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Single-module Android app (`:app`, Kotlin + Jetpack Compose) that scrapes class schedules from CUNY Global Search (`https://globalsearch.cuny.edu/CFGlobalSearchTool/`), stores them in a local Room database, and lets the user find rooms that are free on a given day and time range.

## Commands

Run from the repo root (on Windows use `gradlew.bat` or `./gradlew` from Git Bash):

- Build debug APK: `./gradlew assembleDebug`
- Install on a connected device/emulator: `./gradlew installDebug`
- Unit tests: `./gradlew testDebugUnitTest`
- Single unit test: `./gradlew testDebugUnitTest --tests "com.example.cunyclassroomfinder.ExampleUnitTest.addition_isCorrect"`
- Instrumented tests (needs device): `./gradlew connectedDebugAndroidTest`
- Lint: `./gradlew lintDebug`

Dependency versions live in `gradle/libs.versions.toml` (version catalog). Room uses KSP for annotation processing. minSdk 30, compile/target SDK 37, Java 11.

## Architecture

Manual dependency injection, no Hilt/Dagger:
- `CUNYClassroomFinderApplication` creates an `AppContainer`, which builds the OkHttp client, Retrofit service, and both repositories.
- `ui/AppViewModelProvider.factory` builds every ViewModel from the container. A new ViewModel must get an `initializer { }` entry there; screens get it via `viewModel(factory = AppViewModelProvider.factory)`.
- Navigation: `ClassroomFinderNavGraph.kt` has the `ClassroomFinderScreen` enum (Home, Scrape, Find) and the `NavHost`.

Data flow:
1. **Scraping** (`ui/scrape/ScrapeScreenViewModel`): this is where all HTML parsing (Jsoup) happens; the repository only returns the raw HTML as `Response<String>` (Retrofit scalars converter).
   - `GET search.jsp` → parse the school radio inputs and term `<select>` into `NameWithValueOption`s. The school `selectedName` is the label text plus `" | "`, which is the format the server expects.
   - `POST CFSearchToolController` with the school and term → subject list page (`#subject_ld option`).
   - `POST CFSearchToolController` once per subject, all in parallel with `async`/`awaitAll` → course results, parsed from `td[data-label=...]` cells.
   - **The CUNY site is stateful.** The school/term choice is held in the server session, and the session is kept by the `CookieManager`/`JavaNetCookieJar` in `AppContainer`. That is why `scrapeCourses` only sends subject fields: it must run after `scrapeSubjects` on the same client.
   - `CUNYApiService` default field values (in-person only, open classes, etc.) are the search filters the app relies on.
2. **Storage** (`data/`): `Course` (composite PK `days, timeStart, timeEnd, room`, FK to `CunyRoom.room`) and `CunyRoom`. `CourseDao.insertCoursesWRooms` derives the rooms and inserts them first so the foreign key holds. Before inserting, a re-scrape clears only the selected school's courses (`clearCourses(school)`).
3. **Room lookup** (`ui/room/FindRoomViewModel`): `getAvailableRooms(day, timeStart, timeEnd)` returns the rooms that have no course overlapping that day and time range.

Data encodings (the scraper, DAO query, and FindRoom UI must agree):
- `days` is a bitmask: Mo=1, Tu=2, We=4, Th=8, Fr=16, Sa=32, Su=64. The query tests it with `c.days & :day > 0`.
- Times are seconds since midnight (`LocalTime.toSecondOfDay()`), parsed from the site's `h:mma` format.
- Rooms that are "TBA", "Off Campus", or have no days are filtered out before insert.

Room DB is `version = 1` with `exportSchema = false` and no migrations. After changing an entity's schema, uninstall the app or clear its data, or else add a migration or bump the version.
