# Admin screens

### One request guard per kind of work in `AdminContentViewModel`
- **Fact:** the E04 list, the listing detail and every write share one ViewModel class, so a plain "latest request wins" token would let loading a listing cancel loading its reports. Each kind of work (reports, listing, write) has its own token. A request that is replaced, or that has not answered in 15 seconds, has its late answer dropped. A write that times out may still reach the server later, so the message tells the admin to refresh.
- **Why it matters here:** Firestore queues a write while offline and never fails it, so without the timeout `loading` would stay true and lock the buttons for good. `viewmodel/AdminContentViewModel.java`.
- **Source:** project code. **Checked:** 2026-10-24.

### Reports are published after the listings they show
- **Fact:** `loadReports` reads every reported listing before it posts `reports`, because the screen redraws on `reports` and a row needs its listing for the title and the thumbnail. A listing that cannot be read is left out and the row says "Tin không còn".
- **Why it matters here:** posting the reports first made every row flash that text. The cost is one `getById` per reported listing, also on the detail screen.
- **Source:** project code. **Checked:** 2026-10-24.

### Going to E03 uses the destination id, not an action
- **Fact:** `nav_admin.xml` has no action from E04 or the listing detail to `adminUserDetailFragment`. `NavController.navigate(R.id.adminUserDetailFragment, args)` takes a destination id and works without one. The argument name is `userId`.
- **Why it matters here:** `nav_admin.xml` belongs to the leader (AGENTS.md R19), so E04 does not add an action.
- **Source:** https://developer.android.com/reference/androidx/navigation/NavController (`navigate(int, Bundle)`). **Unverified on a device:** nobody has run the navigation yet.

### A repository callback can fire twice, so count a read once
- **Fact:** `DealRepository.getMyDeals` calls its callback with the Room cache first and again with the Firestore result. Other reads call once.
- **Why it matters here:** `AdminUserViewModel` keeps one counter of reads in flight to drive `loading`. A callback that is counted on every answer would push the counter below zero and turn `loading` off while other reads still run. `ReadCallback` there counts a read as finished on its first answer only and still posts every value.
- **Source:** `PUBGApp/app/src/main/java/com/nhom3/pubgapp/data/DealRepository.java` (`getMyDeals`). **Checked:** 2026-10-24.

### E03 asks for what it holds, and a failed read clears that memory
- **Fact:** `AdminUserViewModel.loadProfile`, `loadActivityLog` and `loadVerificationRequests` return at once when they already asked for the same user, so a rotation reads nothing. A failed read clears that memory, so the screen's Retry reads again.
- **Why it matters here:** the signatures in `viewmodel.md` have no `force` flag, so the guard has to live inside the method. After a review the ViewModel reloads through its private starters, which skip the guard.
- **Source:** `PUBGApp/app/src/main/java/com/nhom3/pubgapp/viewmodel/AdminUserViewModel.java`. **Checked:** 2026-10-24.

### `NetworkMonitor.getInstance()` works now, two screens still carry the old fallback
- **Fact:** `NetworkMonitor` is filled: `getInstance().isOnline()` returns the real state. `AdminUserDetailFragment.isOnline` and `AdminModerationFragment.isOnline` still catch `UnsupportedOperationException` from the time it was a skeleton, so that branch can no longer run.
- **Why it matters here:** remove the `catch` when someone next opens those two methods; it is not worth a change of its own.
- **Source:** `PUBGApp/app/src/main/java/com/nhom3/pubgapp/util/NetworkMonitor.java`. **Checked:** 2026-10-24.

### E03 actions: lock, unlock and warn reload the profile, remove-all does not
- **Fact:** `AdminUserViewModel.lock`, `unlock` and `warn` run one write with a 15 second timeout and, on success, start the private profile load so the status, the warning count and the violation history redraw. `AdminContentViewModel.removeAllOf` belongs to the other ViewModel, which cannot reach that private load, and `loadProfile` returns at once for a user it already holds. So the Listings tab of E03 shows the old statuses until the screen is opened again.
- **Why it matters here:** a force refresh needs a method that is not in `viewmodel.md`, which AGENTS.md R20 forbids adding. Ask the leader before adding one.
- **Source:** `viewmodel/AdminUserViewModel.java` (`ActionCallback`), `viewmodel/AdminContentViewModel.java` (`removeAllOf`). **Checked:** 2026-10-24.

### E04 lists for flagged listings and keywords have their own request guards
- **Fact:** `flaggedGuard` and `keywordsGuard` sit beside `reportsGuard`, `listingGuard` and `writeGuard`, for the same reason: one tab opening must not cancel another tab's read. A write reloads the flagged and keyword lists only if their tab was opened (`flaggedAsked`, `keywordsAsked`), so hiding a flagged listing drops it from the tab.
- **Why it matters here:** `refreshLoading` ORs all five guards. A sixth kind of work needs its own guard and an entry in that line.
- **Source:** `viewmodel/AdminContentViewModel.java`. **Checked:** 2026-10-24.

### A banned keyword must be ASCII after accent stripping, the box accepts more
- **Fact:** the `bannedKeywords` create rule demands `^[ -~]+$` and a lower case value. `AdminKeywordRules.isValidKeyword` accepts any letter or digit, because Vietnamese letters lose their marks in `AdminRepository.addKeyword`. A letter with no ASCII form (for example a Chinese character) passes the box and is refused by the Rules with a permission error.
- **Why it matters here:** the admin sees a generic denied message for such a word. Tightening the box would need the leader to confirm UC38 (open question 3 of P2-E3c).
- **Source:** `firebase/firestore.rules` (`match /bannedKeywords/{id}`). **Checked:** 2026-10-24.
