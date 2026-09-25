# Android resources in PUBGApp (apply when editing `**/res/**/*.xml`)

Detail: `docs/conventions/android-java.md`.

- No hard-coded user visible text. Put strings in `res/values/strings_<feature>.xml` (one file per feature, to avoid merge conflicts). The default language is Vietnamese.
- Layout files are named `<kind>_<feature>_<name>.xml`, for example `activity_auth_login.xml`, `fragment_listing_detail.xml`, `item_listing_card.xml`. View ids are `<type>_<name>` in lower snake case, for example `btn_sign_in`, `tv_price`.
- Every screen needs a landscape variant or a layout that adapts (`layout-land`, `ConstraintLayout`, or `sw600dp` qualifiers) so rotation works.
- Colors and text styles come from `themes.xml` and `colors.xml`. Change shared theme files only under an `area:ui` issue.
- Do not reorder or reformat `AndroidManifest.xml`; only add the lines your issue needs.
