# Pull request record and next steps

Repository: https://github.com/yangyawen-nuist/Project-booking

| PR | Feature | Merge commit | Status |
|---|---|---|---|
| [#1](https://github.com/yangyawen-nuist/Project-booking/pull/1) | Equipment and reservation rules | b2219a4 | Merged |
| [#2](https://github.com/yangyawen-nuist/Project-booking/pull/2) | JSON persistence | 058ec42 | Merged |
| [#3](https://github.com/yangyawen-nuist/Project-booking/pull/3) | CLI commands and parsing | eacf9e1 | Merged |
| Fourth PR | CI and documentation | Pending | feature/quality prepared locally |

The owner created and merged the first three PRs in GitHub. Each branch was based on the preceding merged `main`. All repository commit messages, source messages and documentation are in English.

## Complete the fourth PR

From the repository directory, push the prepared branch:

```bash
git push -u origin feature/quality
```

If Windows Git reports a TLS backend error, use this one-command override:

```powershell
git -c http.sslBackend=openssl push -u origin feature/quality
```

Open https://github.com/yangyawen-nuist/Project-booking/compare/main...feature/quality?expand=1 and create a PR into `main`. Use the title and description in `docs/pr/04-quality.md`, and include actual local build results.

The PR installs the Build workflow. Open its Checks tab and wait for `verify` to pass. If it fails, read the failed step, repair the branch and push again before merging. After a successful check, select Merge pull request and Confirm merge. Keep the feature branch.

After merging, confirm that the push build on `main` also succeeds. Download the `quality-reports` artifact from the Actions run and save the fourth PR URL and Actions run URL with the assignment evidence. Update this table with the actual fourth PR and merge commit after completion.

## Clone and verify the final repository

```bash
git clone https://github.com/yangyawen-nuist/Project-booking.git
cd Project-booking
./gradlew build
```

Use `.\gradlew.bat build` on Windows. The Wrapper executable bit is recorded in Git. First-time dependency downloads need network access; local preparation used the existing Gradle cache.

The four files in `docs/pr/` are descriptions for their corresponding feature changes. Historical local results are labeled in `VALIDATION.md`; fresh GitHub results must come from the actual Actions run.
