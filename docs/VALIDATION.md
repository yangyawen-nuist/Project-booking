# Validation record

## Current repository

Repository: https://github.com/yangyawen-nuist/Project-booking

Environment used locally on 2026-11-27: Windows 11, Microsoft OpenJDK 17.0.20.1, Gradle 8.10.2 and Kotlin 2.0.21. Local builds used the existing Gradle dependency cache and `--offline`; an empty-cache isolated-network environment has not been tested.

| Stage | Command | Result |
|---|---|---|
| Initial configuration | build --offline --no-daemon | Passed; no source or tests yet |
| feature/domain | build --offline --no-daemon | 5 tests passed; 0 Detekt findings |
| feature/persistence | build --offline --no-daemon | 8 tests passed; 0 Detekt findings |
| feature/cli | build installDist --offline --no-daemon | 10 tests passed; 0 Detekt findings |
| feature/quality | clean build installDist --offline --no-daemon | Passed in 47s; 11 tasks executed; 10 tests passed; 0 Detekt findings |

All commands use the committed Wrapper. `gradle/wrapper/gradle-wrapper.properties` fixes Gradle 8.10.2 and SHA-256 `31c55713e40233a8303827ceb42ca48a47267a0ad4bab9177123121e71524c26`. `gradle.lockfile` and `buildscript-gradle.lockfile` fix project and plugin dependencies.

## Tests and coverage

JUnit XML files are generated under `build/test-results/test/`; the HTML summary is `build/reports/tests/test/index.html`. The complete application has 10 passing tests, 0 failures and 0 errors: 5 in `DomainTest`, 3 in `RepositoryTest`, and 2 in `CliTest`.

JaCoCo 0.8.12 generates `build/reports/jacoco/test/html/index.html` and `build/reports/jacoco/test/jacocoTestReport.xml`. The XML root counters for the English application are:

| Metric | Covered | Missed | Coverage |
|---|---:|---:|---:|
| Lines | 137 | 20 | 87.26% |
| Branches | 51 | 3 | 94.44% |
| Instructions | 1109 | 132 | 89.36% |
| Classes | 16 | 0 | 100% |

No source files are excluded from coverage in `build.gradle.kts`. The source report includes the process entry point and unexpected-error handling. Tests do not simulate a full disk, an unsupported atomic replacement or concurrent repository calls inside one JVM.

## Static analysis

Detekt 1.23.7 runs as part of `check`. A successful analysis has an empty `build/reports/detekt/detekt.txt`; the current local application has 0 unresolved findings and no baseline file.

`config/detekt.yml` disables `MagicNumber` for explicit length limits such as 32, 80 and 100 and exit codes in `Main.kt`. `Main.kt` locally suppresses `SpreadOperator` for picocli's Java vararg API. README describes both choices.

## Launcher checks

The feature/cli `installDist` launcher displayed English help with all five subcommands. The earlier English preparation also registered `Microscope`, created a reservation for `Alice`, rejected an overlapping slot with code 1, listed and cancelled the reservation, and rejected an unknown command with code 2. The current source files match that English implementation.

## Earlier preparation record

The separate 2026-11-26 preparation reported 10 passing tests, 130/149 covered lines (87.25%), 51/54 branches (94.44%) and 0 Detekt findings. English message formatting later changed source line counts to 137/157; instruction counts remain 1109 covered and 132 missed.

During that preparation Gradle rejected dependency locking combined with `failOnDynamicVersions` and `failOnChangingVersions`. Removing those modes and retaining two lockfiles fixed configuration. Detekt also reported excessive nesting and a lost exception cause in `Repository.transaction`; extracting `executeLocked` and passing the original exception fixed those findings. UTF-8 JVM options fixed non-ASCII output from the Windows launcher. Those preparation commits remain in a separate backup, outside this repository's history.

## Remote checks

PRs #1, #2 and #3 are merged and used local build results. GitHub CI is added by feature/quality. Its PR and main-branch Actions runs have not yet executed. Before final submission, record their actual URLs and outcomes; the workflow uploads `build/reports/` as the `quality-reports` artifact.
