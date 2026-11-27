# feat: Persist reservations in JSON

`Repository.kt` reads and writes JSON using Jackson. `transaction` acquires a `.lck` file lock before reading, serializing independent CLI processes that use the same data file. After a successful operation, `save` writes a temporary file and replaces the original using `Files.move` with `ATOMIC_MOVE`.

`RepositoryTest` creates a duplicate-ID error during a transaction and compares the file before and after the failure. Another test writes `broken` to the file and checks that failed loading leaves it unchanged. A read-only test checks that querying does not create a data file.

Run `./gradlew test --tests lab.RepositoryTest`, followed by `./gradlew build` before merging. The historical class report in `quality-reports/tests/test/` records 3 passing repository tests.

Local integration result: passed; see ../VALIDATION.md.
Merged PR: https://github.com/yangyawen-nuist/Project-booking/pull/2
