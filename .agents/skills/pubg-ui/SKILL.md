---
name: pubg-ui
description: How to build a PUBGApp screen so all 33 screens look like one app, by one team: design tokens instead of hard-coded values, the four states every screen owes the user, file and id naming, rotation, and the shared-file fence. Use before you write or change anything under res/layout, res/values, res/drawable, or a Fragment, Adapter or ViewHolder, and again before you open the pull request.
---

Five people build 33 screens. Without one rule set the app looks like five apps. This skill is that rule set. The values it points at live in [`workflow/docs/conventions/giao-dien.md`](../../../workflow/docs/conventions/giao-dien.md); this file says how to use them.

Read [`pubg-code`](../pubg-code/SKILL.md) too: everything it says about plain Java applies to Fragments, Adapters and ViewHolders.

## 1. Never hard-code a value

Four things are always a resource, never a literal in a layout or in Java.

| Thing | Comes from | Wrong | Right |
| --- | --- | --- | --- |
| Colour | `colors.xml` through a theme attribute | `android:textColor="#333333"` | `?attr/colorOnSurface` |
| Spacing, size, radius | `dimens.xml` | `android:padding="16dp"` | `@dimen/space_md` |
| Text style | `themes.xml` text appearances | `android:textSize="14sp"` | `?attr/textAppearanceBodyMedium` |
| Any user-visible text | `strings_<module>.xml` | `android:text="Đăng tin"` | `@string/listing_post_action` |

Prefer the theme attribute (`?attr/...`) over the raw colour (`@color/...`), so a screen follows the theme instead of pinning one colour.

If you need a value the tokens do not have, that is a signal: either an existing token fits, or the design needs a new token. You do not add it yourself, see section 6.

## 2. Every screen owes the user four states

A screen that only handles the happy path is not finished. Each screen that loads data handles all four, and the layout contains all four from the start:

1. **Loading.** A progress indicator, no empty list flashing first.
2. **Content.** The data.
3. **Empty.** Not a blank screen: an icon, a line saying what is missing, and where relevant a button that fixes it ("Đăng tin đầu tiên").
4. **Error.** A short Vietnamese sentence saying what went wrong and a "Thử lại" button. Never a silent failure and never a raw exception message (AGENTS.md R12).

On top of that, any screen that reads from the Room cache shows the **offline banner** when the network is gone, worded as `workflow/docs/ui/README.md` describes.

## 3. Naming

| What | Pattern | Example |
| --- | --- | --- |
| Screen layout | `fragment_<module prefix><screen>.xml` | `fragment_listing_detail.xml` |
| List row layout | `item_<module prefix><item>.xml` | `item_listing_card.xml` |
| Reused piece | `view_<name>.xml` | `view_empty_state.xml` |
| Dialog | `dialog_<name>.xml` | `dialog_confirm_cancel.xml` |
| View id | `<type><Thing>` in lowerCamelCase | `textTitle`, `buttonSubmit`, `recyclerListings` |
| String | `<module>_<screen>_<what>` | `listing_detail_price_label` |
| Drawable | `ic_<name>` for icons, `bg_<name>` for backgrounds | `ic_bell`, `bg_chip_selected` |

Your module prefix is the one in [`workflow/plan/so-huu.md`](../../../workflow/plan/so-huu.md). Strings go in your module's own `strings_<module>.xml`, never in `strings.xml`.

## 4. Build it with ViewBinding, keep state in the ViewModel

- `ViewBinding` only. No `findViewById`, no Kotlin synthetics, no Data Binding expressions in XML.
- In a Fragment, null the binding in `onDestroyView`. Do not touch the binding after that.
- **Rotation must not lose anything.** Typed text, the loaded list, the scroll position, the selected tab and the open filter all survive. State lives in the `ViewModel`, never in a field of the Fragment or Activity. This is a course requirement, not a preference.
- Use `ConstraintLayout` so one layout adapts, and add `layout-land` only when the portrait layout genuinely cannot stretch.
- No work on the main thread. No network or disk call from a click handler.

## 5. Images

Images are Base64 strings inside Firestore documents, because the project runs on the free Firebase plan (AGENTS.md R21). There is no Cloud Storage and no video anywhere.

- Compress on the device before writing: full image 1024 px long edge and at most 300 KB; listing thumbnail 320 px; avatar 256 px; thumbnails at most 20 KB.
- At most 5 images per listing.
- Always draw a placeholder while an image decodes, and a fallback when it fails. A letter avatar is the fallback for a missing profile picture.
- Decode off the main thread. A list must stay smooth while scrolling.

## 6. The shared-file fence

These belong to the leader (`so-huu.md` section 6). You read them, you do not edit them:

`colors.xml`, `themes.xml`, `dimens.xml`, `strings_common.xml`, `AndroidManifest.xml`, `nav_main.xml`, `nav_admin.xml`, the bottom navigation menu.

Need a new colour, dimension, icon or shared string? Open an issue labelled `area:ui`, say which screen needs it and why an existing token does not fit, and wait. Do not add it locally "for now": that is how five palettes appear.

You also do not touch another module's layouts, drawables or strings. Going to another module's screen uses the global actions listed in `so-huu.md` section 4 (AGENTS.md R22).

## 7. Accessibility and polish, the small set that is not optional

- Anything tappable is at least 48dp by 48dp.
- Every icon-only button has a `contentDescription`; a decorative image has `android:importantForAccessibility="no"`.
- Text contrast follows the tokens; do not put grey text on a coloured background by hand.
- A list uses a stable `RecyclerView.Adapter` with a ViewHolder, never a `LinearLayout` filled in a loop.
- A long form scrolls, and the keyboard does not cover the field being typed in.

## 8. Before you open the pull request

Tick every line, and paste the list into the pull request:

- [ ] No hard-coded colour, size or string in the layouts or Java I touched.
- [ ] All four states exist and I have seen each one on the emulator.
- [ ] Rotated the screen: nothing typed, loaded, selected or scrolled was lost.
- [ ] Screenshots attached, portrait and landscape.
- [ ] Only files my issue lists, and no shared file from section 6.
- [ ] File names, ids and string keys follow section 3.
- [ ] `./gradlew assembleDebug lintDebug` passes, and `lintDebug` reports no new hard-coded text or missing `contentDescription`.
- [ ] Strings are Vietnamese, in my module's string file, with no emoji.

## 9. What goes in the design document, not here

When you learn something that the next person needs (a component that reappears, a spacing that the mockups use everywhere, a state the design forgot), add it to `workflow/docs/conventions/giao-dien.md` in the same pull request. Do not leave it in a code comment, and do not leave it only in your head (AGENTS.md R15).
