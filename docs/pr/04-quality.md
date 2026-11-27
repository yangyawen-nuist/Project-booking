# build: add CI and project documentation

Add a GitHub Actions workflow that runs `./gradlew build --no-daemon` on pushes and PRs with Java 17, then uploads `build/reports/` as `quality-reports`.

Add English README, architecture and data-structure explanations, build and run examples, validation evidence, contribution disclosure, branch rules and PR records. Ignore the Kotlin compiler cache directory.

The initial commit already contains Gradle Wrapper 8.10.2, two dependency lockfiles, JUnit 5.11.3, Detekt 1.23.7 and JaCoCo 0.8.12. This PR documents those settings and adds remote build automation.

Local validation uses `./gradlew clean build installDist --offline --no-daemon` with Java 17 and the existing dependency cache. The complete application has 10 tests, 87.26% line coverage (137/157), 94.44% branch coverage (51/54) and 0 unresolved Detekt findings. The earlier preparation recorded 87.25% line coverage before English source line wrapping; see `docs/VALIDATION.md`.

All four PRs are merged. PR #4 merged as `68f9829`. Its [PR check](https://github.com/yangyawen-nuist/Project-booking/actions/runs/45942560381) and the [main build](https://github.com/yangyawen-nuist/Project-booking/actions/runs/45942936968) both completed successfully.

The code, tests, configuration and documentation were prepared with AI assistance. The owner created and merged all four platform PRs; `docs/CONTRIBUTIONS.md` records the division of work.
