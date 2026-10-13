---
trigger: glob
globs: "**/res/**/*.xml, **/AndroidManifest.xml"
description: Layout, string and resource conventions for PUBGApp
---

# Android resources in PUBGApp

Detail: `workflow/docs/conventions/android-java.md`. Who owns which resource file: `workflow/plan/so-huu.md` sections 2 and 6.

- No hard-coded user visible text. Put strings in your module's file `res/values/strings_<module>.xml` (`account`, `listing`, `trade`, `profile`, `admin`, `common`). Never edit another module's strings file. The default language is Vietnamese.
- Layout files are named `fragment_<prefix><screen>.xml` and `item_<prefix><item>.xml`, where the prefix is your module's (`account_`, `notification_`, `listing_`, `trade_`, `profile_`, `trust_`, `admin_`, `common_`), for example `fragment_listing_detail.xml`, `item_listing_card.xml`. Only the three Activities have `activity_` layouts. View ids are `<type>_<name>` in lower snake case, for example `btn_sign_in`, `tv_price`.
- Every screen needs a landscape variant or a layout that adapts (`layout-land`, `ConstraintLayout`, or `sw600dp` qualifiers) so rotation works.
- Colors and text styles come from `themes.xml` and `colors.xml`. These, `dimens.xml`, `strings_common.xml`, the navigation graphs of other modules, `nav_main.xml`, `nav_admin.xml`, the bottom-bar menu and `AndroidManifest.xml` are shared files with one owner (the leader): change one only when your issue lists it, and only by adding lines. Do not reorder or reformat them.
- Navigation: each module has its own graph; going to another module's screen uses only the global actions of `nav_main.xml` (AGENTS.md R22).
