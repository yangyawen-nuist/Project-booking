# feat: Add equipment and reservation rules

`Domain.kt` provides equipment registration, reservations, listing and cancellation through `BookingService`. A hash-based map indexes devices by ID; `addEquipment` rejects duplicates. Each device has a `TreeMap`, and `insert` checks neighboring slots using `floorEntry` and `ceilingEntry`.

Adjacent reservations are allowed: 10:00-11:00 can be followed by 11:00-12:00. `DomainTest` checks adjacent slots, four overlap cases, cancellation, snapshot reconstruction and simultaneous slots on different devices.

Run `./gradlew test --tests lab.DomainTest`, followed by `./gradlew build` before merging. The historical class report in `quality-reports/tests/test/` records 5 passing domain tests.

Local integration result: passed; see ../VALIDATION.md.
Merged PR: https://github.com/yangyawen-nuist/Project-booking/pull/1
