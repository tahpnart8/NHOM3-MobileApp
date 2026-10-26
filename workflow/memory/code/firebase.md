# Firebase

### Security rules are not filters
- **Fact:** Firestore evaluates a query against its whole potential result set. If the rules could deny even one document the query might return, the entire query fails, even when the database holds no such document.
- **Why it matters here:** every query by a user who is not the owner must carry a condition that the rules accept, for example `status` in the public statuses. A plain `where sellerId == X` on a collection whose rules hide some documents is rejected.
- **Source:** https://firebase.google.com/docs/firestore/security/rules-query. **Checked:** 2026-09-25.

### Google sign-in uses Credential Manager and the web client ID
- **Fact:** Sign in with Google on Android goes through AndroidX Credential Manager with `GetGoogleIdOption`. `setServerClientId` takes the **web** application client ID (`default_web_client_id`), not the Android client ID. The Google ID token is then exchanged with `GoogleAuthProvider.getCredential(idToken, null)` and `FirebaseAuth.signInWithCredential`.
- **Why it matters here:** the older `GoogleSignInClient` API is not to be used. A missing SHA-1 or an old `google-services.json` shows up as `DEVELOPER_ERROR` (`workflow/docs/workflow/firebase-setup.md`, steps 7 and 8).
- **Source:** https://firebase.google.com/docs/auth/android/google-signin. **Checked:** 2026-09-25.

### Firebase KTX modules are gone; Java uses the main modules
- **Fact:** From Firebase BoM 34.0.0 (July 2025) the `-ktx` modules are no longer released. Kotlin extensions are in the main modules, and Java code uses the same main modules, for example `firebase-auth` and `firebase-firestore`, with no version when the BoM is imported.
- **Why it matters here:** an example or an AI answer that names a `-ktx` artifact is out of date. Look the BoM version up when adding it (AGENTS.md R5).
- **Source:** https://firebase.google.com/docs/android/learn-more. **Checked:** 2026-09-25.

### Cloud Storage is not used: the project stays on the Spark plan
- **Fact:** Since 2026-02-03 Cloud Storage for Firebase works only on the Blaze plan. On the Spark plan every call returns 402 or 403. Cloud Functions also needs Blaze.
- **Why it matters here:** the project runs on Spark with no billing card (decision of 2026-10-12, `workflow/docs/workflow/firebase-setup.md`, step 1). Do not add `firebase-storage` or `firebase-functions`. Images are compressed and stored as Base64 in the `images` collection of Firestore, one document under 1 MiB each (`workflow/docs/data/firestore-schema.md`). A Storage error in a copied example is expected; change the approach instead of the plan.
- **Source:** https://firebase.google.com/docs/storage/faqs-storage-changes-announced-sept-2024. **Checked:** 2026-09-25.

### Firestore POJO mapping: four rules for model classes
- **Fact:** `@DocumentId` on a `String` property makes the SDK fill it with the document id; the id is then not stored as a field. `@Exclude` keeps a getter or field out of serialization. Firestore reads and writes a property for every public getter and setter, so a computed method named `isX()` or `getX()` would be written as a field unless it is excluded. A boolean field whose name starts with `is` (for example `isDefault`) is mapped through its getter `isDefault()` to the property name `default`, so it needs `@PropertyName("isDefault")` on the getter and setter to keep the stored name. `createdAt` and `updatedAt` are written with `FieldValue.serverTimestamp()` so the Security Rules and every device agree on time.
- **Why it matters here:** `BaseDocument.id` carries `@DocumentId`; computed methods such as `Complaint.isOpen()` carry `@Exclude`; `UserAddress.isDefault`, `Category.isActive` and `AppNotification.isRead` carry `@PropertyName`. The rules are listed in `workflow/docs/conventions/android-java.md`.
- **Source:** `@DocumentId` and `@Exclude` exist in the Firebase Android SDK (https://github.com/firebase/firebase-android-sdk, firestore CHANGELOG 20.2.0 for `@DocumentId`). **Unverified:** the exact `isX` property-name behaviour and the `@PropertyName` fix; a unit test that writes and reads one `UserAddress` against the emulator or a mapper call would verify it.

### Account deactivation order of operations and partial failure handling
- **Fact:** Firestore security rules allow updating a user document to `DELETED` only while the client is still authenticated as that user (`request.auth.uid == userId`). Deleting the Firebase Auth login (`FirebaseUser.delete()`) before updating Firestore causes `PERMISSION_DENIED`. If Firestore deactivation succeeds but `deleteLogin()` subsequently fails (e.g. `requires-recent-login`), the user is already marked `DELETED` in Firestore. In this scenario, the app must sign out, clear local session credentials, wipe local Room caches, and direct the user to guest mode with an explanatory message.
- **Why it matters here:** `ProfileViewModel.deactivateFirestoreAndAuth` updates Firestore status to `DELETED` first, then calls `AuthRepository.deleteLogin`. If `deleteLogin` fails, it calls `authRepository.signOut()`, clears `SessionManager`, purges Room tables, and shows a clear message explaining that login cleanup requires re-login while the account was already deactivated.
- **Source:** `workflow/docs/data/security-rules.md` (users collection update rule), Firebase Auth `delete()` documentation. **Checked:** 2026-10-26.

