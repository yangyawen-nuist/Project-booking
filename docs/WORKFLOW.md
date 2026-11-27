# Branches and reviews

`main` contains merged code. Develop each feature on a separate branch and open a GitHub PR into `main`.

| Branch | Files | Validation | Status |
|---|---|---|---|
| feature/domain | Domain.kt, DomainTest.kt | 5 domain tests | PR #1 merged |
| feature/persistence | Repository.kt, RepositoryTest.kt | 8 tests including 3 repository tests | PR #2 merged |
| feature/cli | Main.kt, CliTest.kt | 10 tests, standalone launcher help | PR #3 merged |
| feature/quality | CI, README, docs, cache ignore rule | Full build, Detekt, JaCoCo | Fourth PR pending |

Use commit prefixes such as `feat:`, `fix:`, `build:` and `docs:`. The initial `674eeeb` commit contains the Wrapper, pinned dependencies, lockfiles and quality-tool configuration. Subsequent feature commits add only the relevant source and tests; `git log --graph --oneline --all` shows the feature and merge commits.

Start each branch from the updated `main` after the preceding PR merges. Run the relevant tests and `./gradlew build` before merging. Use Create a merge commit to keep a merge node for each PR. Preserve the four feature branches for the assignment.

The fourth PR adds `.github/workflows/build.yml`. Its `verify` job runs the build on Ubuntu with Java 17 and uploads reports even if a check fails. When creating the PR, wait for the `Build / verify` check to pass before merging. Earlier PRs used local checks and show zero GitHub checks because this workflow was not installed yet.

After the check first appears, configure a `main` protection rule to require the successful build check and disallow force pushes. Add a peer approval requirement if the course requires a classmate's review; the owner cannot approve their own PR. Protection rules and peer approval are not yet configured.
