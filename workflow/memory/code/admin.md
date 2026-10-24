# Admin screens

### A repository callback can fire twice, so count a read once
- **Fact:** `DealRepository.getMyDeals` calls its callback with the Room cache first and again with the Firestore result. Other reads call once.
- **Why it matters here:** `AdminUserViewModel` keeps one counter of reads in flight to drive `loading`. A callback that is counted on every answer would push the counter below zero and turn `loading` off while other reads still run. `ReadCallback` there counts a read as finished on its first answer only and still posts every value.
- **Source:** `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/DealRepository.java` (`getMyDeals`). **Checked:** 2026-10-24.

### E03 asks for what it holds, and a failed read clears that memory
- **Fact:** `AdminUserViewModel.loadProfile`, `loadActivityLog` and `loadVerificationRequests` return at once when they already asked for the same user, so a rotation reads nothing. A failed read clears that memory, so the screen's Retry reads again.
- **Why it matters here:** the signatures in `viewmodel.md` have no `force` flag, so the guard has to live inside the method. After a review the ViewModel reloads through its private starters, which skip the guard.
- **Source:** `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/AdminUserViewModel.java`. **Checked:** 2026-10-24.

### `NetworkMonitor.getInstance()` still throws
- **Fact:** `NetworkMonitor` is a skeleton: `getInstance()` throws `UnsupportedOperationException("NOT_READY")`.
- **Why it matters here:** a screen that calls it directly crashes. `AdminUserDetailFragment` catches the exception and asks `ConnectivityManager` instead, as `LoginFragment` does. Remove the fallback when the class is filled.
- **Source:** `PUBGApp/app/src/main/java/com/nhom3/pubgapp/util/NetworkMonitor.java`. **Checked:** 2026-10-24.
