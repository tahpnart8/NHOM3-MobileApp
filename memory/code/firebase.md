# Firebase

### Security rules are not filters
- **Fact:** Firestore evaluates a query against its whole potential result set. If the rules could deny even one document the query might return, the entire query fails, even when the database holds no such document.
- **Why it matters here:** every query by a user who is not the owner must carry a condition that the rules accept, for example `status` in the public statuses. A plain `where sellerId == X` on a collection whose rules hide some documents is rejected.
- **Source:** https://firebase.google.com/docs/firestore/security/rules-query. **Checked:** 2026-09-25.

### Google sign-in uses Credential Manager and the web client ID
- **Fact:** Sign in with Google on Android goes through AndroidX Credential Manager with `GetGoogleIdOption`. `setServerClientId` takes the **web** application client ID (`default_web_client_id`), not the Android client ID. The Google ID token is then exchanged with `GoogleAuthProvider.getCredential(idToken, null)` and `FirebaseAuth.signInWithCredential`.
- **Why it matters here:** the older `GoogleSignInClient` API is not to be used. A missing SHA-1 or an old `google-services.json` shows up as `DEVELOPER_ERROR` (`docs/workflow/firebase-setup.md`, steps 7 and 8).
- **Source:** https://firebase.google.com/docs/auth/android/google-signin. **Checked:** 2026-09-25.

### Firebase KTX modules are gone; Java uses the main modules
- **Fact:** From Firebase BoM 34.0.0 (July 2025) the `-ktx` modules are no longer released. Kotlin extensions are in the main modules, and Java code uses the same main modules, for example `firebase-auth` and `firebase-firestore`, with no version when the BoM is imported.
- **Why it matters here:** an example or an AI answer that names a `-ktx` artifact is out of date. Look the BoM version up when adding it (AGENTS.md R5).
- **Source:** https://firebase.google.com/docs/android/learn-more. **Checked:** 2026-09-25.

### Cloud Storage needs the Blaze plan
- **Fact:** Since 2026-02-03 Cloud Storage for Firebase works only on the Blaze plan. On the Spark plan every call returns 402 or 403. No-cost usage remains on Blaze within the Always Free limits, for buckets in `us-central1`, `us-east1` or `us-west1`.
- **Why it matters here:** a Storage error in a member's build with a fresh project is usually billing, not code. The project owner set this up (`docs/workflow/firebase-setup.md`, step 1).
- **Source:** https://firebase.google.com/docs/storage/faqs-storage-changes-announced-sept-2024. **Checked:** 2026-09-25.
