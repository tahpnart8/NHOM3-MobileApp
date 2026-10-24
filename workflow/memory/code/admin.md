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
