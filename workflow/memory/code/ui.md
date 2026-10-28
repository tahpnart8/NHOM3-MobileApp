# UI layer

What the screens and the shared view code have taught us. Reference implementations live in
`PUBGApp/app/src/main/java/com/nhom3/pubgapp/ui/common/`.

## `ImageLoader` has one overload per image source

Pick by where the image is in your hand, not by taste:

| You hold | Call | The recycle guard is |
| --- | --- | --- |
| A Base64 string from a Firestore document (`Listing.coverThumb`, `User.avatarBase64`) | `load(String, ImageView, placeholder, fallback)` | the string itself |
| The `byte[]` from `MediaRepository.getImage` | `load(byte[], String imageId, ImageView, placeholder, fallback)` | the image id you pass |
| A `Uri` the user just picked, before it reaches Firestore | `load(Uri, ImageView, placeholder, fallback)` | the Uri |

The guard is the point of the class, not a detail. `RecyclerView` reuses a `ViewHolder` while a decode is in
flight, so the loader tags the `ImageView` with what it wants now and a late result that no longer matches is
dropped. Decoding on a bare `new Thread` without that tag puts one row's image on another row, and it has
happened six times in this project. If a new source appears, add a fourth overload here instead of decoding in
an adapter.

The `byte[]` overload needs the id because the bytes carry no identity. Passing a wrong or shared id is worse
than passing none: two images would answer to one tag and race. If you have no id, you have the wrong overload.

## Avatars go through `AvatarBinder`, which layers the photo over the initial

`view_avatar.xml` stacks `imageAvatar` above `textInitial` in a clipped `FrameLayout`. `AvatarBinder.bind`
always sets and shows the initial, then loads the photo with no placeholder and no fallback (resource 0). While
the photo decodes, and if it fails, the image view is transparent and the initial shows through; once it
decodes it covers the letter. So no callback from `ImageLoader` is needed to swap the two views. The earlier
pattern (hide the initial as soon as a photo string exists) left an empty green circle whenever the decode
failed, and passing `bg_avatar` as placeholder drew a circle over the letter. Pass the name and photo of the
user the row is about; for a partner or reviewer whose `User` is not loaded yet, pass the fallback name and a
null photo, then bind again when the profile arrives. Binding the same photo twice is free: `ImageLoader` tags
the view with the Base64 string, and `bind` skips the reload when the tag already matches, so a rebind on every
navigation or `notifyItemChanged` does not blink the photo back to the letter.

The home top bar avatar is in `MainActivity`, which observes the activity-scoped `ProfileViewModel` (the same
instance `MyProfileFragment` uses) so the Google photo written after sign-in arrives through the profile
listener without a restart.

## Decoding a `Uri` opens it twice

`BitmapFactory` needs the bounds before it can choose `inSampleSize`, and an `InputStream` from a
`ContentResolver` cannot be rewound. So `UriSource` calls `openInputStream` once for the bounds pass and again
for the real decode. A single stream reused for both returns null the second time, silently.

## `calculateSampleSize` must reject a zero target

Integer division bottoms out at 0 and `0 >= 0` is true, so a target size of 0 makes the doubling loop run
forever. `chooseTargetSize` never returns 0 today, which is exactly why the guard belongs in the arithmetic
rather than in the caller.

## Session restore and tab blocking order

On cold start, Firebase session restore runs asynchronously (`restoreViewModel.restoreSession()`).
During this in-flight window, `SessionManager.isLoggedIn()` is temporarily false while the user
identity is being re-established.
To prevent premature guest-blocking:
1. `MainActivity.decideTabAction` returns `TabAction.WAIT_RESTORE` when a protected tab is tapped
   during session restore. It stores `pendingTabId` and displays `R.string.common_loading`. Once
   restore finishes, it dispatches to `pendingTabId` if logged in, or `action_global_sign_in` if not.
2. `HomeFragment.shouldShowGuestSignIn` hides the guest header while `isRestoring` is true,
   preventing the sign-in button from flashing or staying visible when the user is logged in.

## Account and notification UI pitfalls

1. **Always null binding in `onDestroyView()`**: Long multi-step fragments like `RegisterFragment`
   and settings screens retain view references in the binding field. If not cleared in `onDestroyView`,
   the entire view hierarchy leaks across orientation changes and fragment transactions.
2. **Standard theme tokens over hardcoded dp or color literals**: Always use `?attr/colorOnSurface`,
   `?attr/colorOnSurfaceVariant` instead of `?android:attr/textColor*`, and use `@dimen/touch_min` (48dp)
   for interactive buttons and touch targets.
3. **Link and punctuation delimiters**: Separators like `|` between privacy and terms links must be
   extracted to string resources (`@string/account_register_link_separator`) to comply with zero hardcoded
   string policy.

## Trade and deal UI pitfalls

1. **Rotation preservation on interactive inputs and filters**:
   - `ChatFragment` draft messages and `ConversationListFragment` search queries must be saved in `onSaveInstanceState` and restored on view recreation to avoid wiping user text on rotation.
   - `DealHistoryFragment` must persist `currentFilter` alongside `currentTab` in `outState` and re-apply chip selection and adapter filter upon rotation.
2. **Standard image loading across all deal cards and receipts**:
   - Always route thumbnail and media loading through `ImageLoader.load` instead of manual `BitmapFactory.decodeByteArray`. Manual decoding lacks recycling guards and causes image flickering and memory overhead on rotation.
3. **Punctuation and bullet separators**:
   - Metadata bullet separators (`·`) between role, payment method, and timestamp must use `@string/trade_bullet_separator` instead of hardcoded characters.
4. **Touch targets on RadioGroup and action buttons**:
   - In multi-step or bottom sheet forms like `MakeOfferFragment` and `DealPaymentFragment`, each `RadioButton` and action `MaterialButton` must have `android:minHeight="@dimen/touch_min"` (48dp).

