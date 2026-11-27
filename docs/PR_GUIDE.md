# Pull request record and submission evidence

Repository: https://github.com/yangyawen-nuist/Project-booking

| PR | Feature | Merge commit | Status |
|---|---|---|---|
| [#1](https://github.com/yangyawen-nuist/Project-booking/pull/1) | Equipment and reservation rules | b2219a4 | Merged |
| [#2](https://github.com/yangyawen-nuist/Project-booking/pull/2) | JSON persistence | 058ec42 | Merged |
| [#3](https://github.com/yangyawen-nuist/Project-booking/pull/3) | CLI commands and parsing | eacf9e1 | Merged |
| [#4](https://github.com/yangyawen-nuist/Project-booking/pull/4) | CI and documentation | 68f9829 | Merged |

The owner created and merged all four PRs in GitHub. Each feature branch was created from the preceding merged `main`. Preserve these branches and the merge history for the assignment.

## Successful remote builds

- [PR #4 validation](https://github.com/yangyawen-nuist/Project-booking/actions/runs/45942560381): completed, success.
- [Main build after PR #4](https://github.com/yangyawen-nuist/Project-booking/actions/runs/45942936968): completed, success; commit `68f9829432a5d6731c8598cd4c8f421227c91fef`.

These outcomes were checked through the GitHub Actions API. The workflow uses Ubuntu and Java 17. Download `quality-reports` from the Artifacts section of the main run to retain its test, coverage and Detekt reports.

## Verify a fresh clone

```bash
git clone https://github.com/yangyawen-nuist/Project-booking.git
cd Project-booking
./gradlew build
```

Use `.\gradlew.bat build` on Windows. The Wrapper executable bit is recorded in Git. First-time dependency downloads need network access; local preparation used the existing Gradle cache.

## Final submission checklist

- Repository URL and the four merged PR URLs above.
- Successful main Actions run URL and its `quality-reports` artifact.
- README and `VALIDATION.md`.
- Optional screenshots showing PR merges and successful checks.

Branch protection and peer review remain optional setup tasks unless the course requires them. To require CI on future changes, protect `main`, disallow force pushes and require the `verify` check. A peer-review requirement needs another reviewer; the owner cannot approve their own PR.

The files in `docs/pr/` preserve descriptions of the four feature changes. Original preparation failures and historical coverage figures are documented separately in `VALIDATION.md`.
