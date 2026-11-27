# Lab Booking

Kotlin command-line application for laboratory equipment reservations.

## Overview

Register equipment, reserve time slots, list reservations and cancel them by ID. `Repository` saves equipment and bookings in `data/bookings.json`, so separate command invocations share the same data.

## Requirements and quick start

Install JDK 17. `build.gradle.kts` specifies `jvmToolchain(17)`. The committed Wrapper starts Gradle 8.10.2 without a separate Gradle installation. The first build needs access to the Gradle distribution server, Maven Central and the Gradle Plugin Portal.

```bash
git clone https://github.com/yangyawen-nuist/Project-booking.git
cd Project-booking
./gradlew build
./gradlew run --args="--help"
./gradlew run --args='equipment-add --id microscope --name Microscope'
./gradlew run --args='reserve --equipment microscope --user Alice --start 2026-12-01T09:00 --end 2026-12-01T10:00'
./gradlew run --args='bookings --equipment microscope'
./gradlew run --args='cancel --id <reservation-uuid>'
```

On Windows PowerShell, use `.\gradlew.bat` instead of `./gradlew`. The `check` task runs tests and Detekt; `test` is finalized by `jacocoTestReport` in `build.gradle.kts`.

To generate standalone launch scripts:

```bash
./gradlew installDist
./build/install/lab-booking/bin/lab-booking equipment-list
./build/install/lab-booking/bin/lab-booking --data ./data/demo.json equipment-list
```

The Windows launcher is `build\install\lab-booking\bin\lab-booking.bat`. Distribution archives are generated in `build/distributions/`. Java 17 is required to run them. Output uses UTF-8; older Windows terminals may need `chcp 65001`.

## Commands and configuration

| Command | Arguments | Purpose |
|---|---|---|
| equipment-add | --id, --name | Register equipment with a unique ID |
| equipment-list | None | List equipment by ID |
| reserve | --equipment, --user, --start, --end | Create a reservation and print its UUID |
| bookings | Optional --equipment | List reservations by time |
| cancel | --id | Cancel a reservation |

The inherited `--data PATH` option in `LabCommand` selects the JSON file and can appear before or after the subcommand. The default is `data/bookings.json`. Use `--help` for usage. Exit codes are 0 for success, 1 for business or storage errors, 2 for parsing errors, and 3 for unexpected internal errors. `commandLine()` writes errors to standard error.

`Domain.kt` parses local date-times such as `2026-12-01T09:00` with `LocalDateTime.parse`. There is no time-zone conversion or check against the current time, so historical reservations are allowed. Intervals are `[start,end)`: `insert` uses `<=` comparisons, allowing 09:00-10:00 followed by 10:00-11:00. Equipment IDs contain 1-32 ASCII letters, digits, underscores or hyphens. Equipment names contain 1-100 characters and user names 1-80 characters; neither may be blank.

## Architecture and data structures

`Main.kt` parses arguments through picocli command classes such as `Reserve`. Each command calls `Repository.transaction`, which loads data and passes a `BookingService` to the command. `DomainTest` constructs `BookingService(State(...))` directly without starting a CLI or creating files.

1. A hash-based map indexes equipment by ID, giving average O(1) lookup. `BookingService.equipment` uses `associateBy { it.id }.toMutableMap()`; `addEquipment` checks `id !in equipment`.
2. A `TreeMap<LocalDateTime, Reservation>` indexes each device's reservations. `insert` calls `floorEntry(start)` and `ceilingEntry(start)`, each O(log n). Stored intervals have already passed overlap validation, so these two neighbors are sufficient to detect a conflict. A new 10:30-11:30 slot is rejected when 10:00-11:00 already exists.

`Repository.executeLocked` reconstructs `BookingService` from JSON, rechecking duplicate IDs and conflicts. `transaction` acquires `channel.lock()` before reading. The lock file appends `.lck` to the JSON filename, serializing independent CLI processes using the same data file.

`Repository.save` writes a temporary file in the same directory, then calls `Files.move` with `ATOMIC_MOVE` and `REPLACE_EXISTING`. A failed business action never reaches `save`; `RepositoryTest` compares the file before and after a failed transaction. Invalid JSON or an unsupported atomic replacement returns a data-access error and leaves the original file in place.

This implementation targets a small, single-machine laboratory. `Cancel` only requires an ID; anyone who knows it can cancel that reservation. There is no authentication, administrator role or network interface. `LocalDateTime` does not resolve daylight-saving changes. Concurrent calls from multiple threads inside one JVM have not been tested.

## Tests and code quality

```bash
./gradlew test
./gradlew jacocoTestReport
./gradlew detekt
./gradlew clean build
```

| Report | Generated path |
|---|---|
| JUnit | build/reports/tests/test/index.html |
| JaCoCo HTML | build/reports/jacoco/test/html/index.html |
| JaCoCo XML | build/reports/jacoco/test/jacocoTestReport.xml |
| Detekt | build/reports/detekt/detekt.html |

The current local build passes all 10 tests: 5 in `DomainTest`, 3 in `RepositoryTest`, and 2 in `CliTest`. JaCoCo reports 137/157 covered lines (87.26%) and 51/54 covered branches (94.44%). Detekt reports 0 unresolved findings. See [VALIDATION.md](docs/VALIDATION.md) for commands and earlier measurements. GitHub Actions runs the same build and uploads `build/reports/` as the `quality-reports` artifact.

Tests exercise adjacent and overlapping slots, cancellation, snapshot rebuilding, invalid input, transaction rollback, corrupt JSON and command parsing. No files are excluded from JaCoCo in `build.gradle.kts`.

Direct versions are explicit in `build.gradle.kts`, including picocli 4.7.6 and JUnit 5.11.3. `gradle.lockfile` and `buildscript-gradle.lockfile` lock transitive application and plugin dependencies. The Wrapper properties fix Gradle 8.10.2 and its distribution SHA-256.

`config/detekt.yml` disables `MagicNumber` for inline limits such as 32, 80 and 100 in `Domain.kt`, and exit codes 1 and 3 in `Main.kt`. Only the process entry point suppresses `SpreadOperator`: picocli's Java vararg interface requires `execute(*args)`, copying a small argument array once. The project has no Detekt baseline.

## Development workflow

This repository uses `main` and four feature branches. [PR #1](https://github.com/yangyawen-nuist/Project-booking/pull/1) added the domain layer, [PR #2](https://github.com/yangyawen-nuist/Project-booking/pull/2) added persistence, and [PR #3](https://github.com/yangyawen-nuist/Project-booking/pull/3) added the CLI. [PR #4](https://github.com/yangyawen-nuist/Project-booking/pull/4) added CI and documentation. All four are merged. The [PR check](https://github.com/yangyawen-nuist/Project-booking/actions/runs/45942560381) and [main build](https://github.com/yangyawen-nuist/Project-booking/actions/runs/45942936968) both passed. See [WORKFLOW.md](docs/WORKFLOW.md) and [PR_GUIDE.md](docs/PR_GUIDE.md).

## Reflection

Looking back at the build record, the first failure was the dependency-lock configuration in `build.gradle.kts`. The initial idea was to enable locking alongside checks for dynamic and changing versions. Gradle rejected that combination; removing the dynamic-version check exposed the same conflict with the changing-version check. Keeping locking and generating two lockfiles allowed the build to continue.

The next issue was `Repository.transaction`. Reading, applying business changes and saving were all nested inside the file lock because keeping the sequence together seemed easier to follow. Detekt reported excessive nesting and a lost exception cause. The repair moved the locked operations into `executeLocked` and passed the original exception into the new exception. The lock still covers the entire operation.

The standalone launcher exposed the third problem. UTF-8 source and JSON had seemed sufficient, but `equipment-list` displayed a non-ASCII equipment name incorrectly in Windows. The build configuration then added UTF-8 JVM options to both the application launcher and run tasks. After regenerating the launcher, the name displayed correctly. All 10 tests had already passed before that separate-process check found the problem.
