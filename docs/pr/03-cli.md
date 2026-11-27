# feat: Add booking subcommands

`Main.kt` uses picocli 4.7.6 for argument parsing. `LabCommand` registers `equipment-add`, `equipment-list`, `reserve`, `bookings` and `cancel`. The `--data` option selects the JSON file and `--help` displays usage.

`commandLine()` maps business errors to exit code 1; picocli returns 2 for unknown commands and missing arguments. `CliTest` checks these codes with `execute` and runs registration, reservation, listing and cancellation against the same data path.

Run `./gradlew test --tests lab.CliTest`, followed by `./gradlew build` before merging. The historical class report in `quality-reports/tests/test/` records 2 passing CLI tests.

Local integration result: passed; see ../VALIDATION.md.
Merged PR: https://github.com/yangyawen-nuist/Project-booking/pull/3
