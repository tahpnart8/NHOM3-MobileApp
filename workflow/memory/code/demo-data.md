# Demo data

What was learned while writing `DemoDataSeeder` and `DemoDataBuilder` (issue #269), so the next person does not rediscover it.

- **A listing cannot be deleted unless it is a draft** (`firestore.rules`, `listings`, delete). "Clear demo data" therefore hides the listings with `ListingRepository.setHidden` (the owner hides: no activity log, and notices only go to buyers with a pending offer, of which demo listings have none) and seeding again shows them with `setHidden(id, false)`. Ids are fixed (`demo_listing_01` to `demo_listing_72`), so a second seed never doubles the data. Hiding with `AdminRepository.hideListing` was rejected: it writes a notification and a log row for every listing.
- **Categories go in two commits.** The category rule reads the parent with `get()`, which sees the database before the batch, so a child cannot be created in the same batch as its parent. Roots first, children after.
- **The categories are read with one `get()` of the whole collection**, not a range query on the document id; there are only a few dozen documents.
- **The seller of every demo listing is the signed-in admin.** `sellerId` must equal the caller and a client cannot create another user's profile, so the sample sellers with different ratings that `goi-y.md` asks for cannot be seeded. Sorting by "Đánh giá tốt" needs several test accounts made by hand.
- **The admin must be an approved seller** (`verificationStatus` APPROVED and a verified email) or `ListingRepository.create` refuses with "Tài khoản chưa đủ điều kiện bán hàng".
- **Sample images are drawn on the device** (a coloured bitmap with the text DEMO), saved with `MediaRepository.saveImage` and `makeThumb`, six images shared by the 72 listings. The listing rule only checks that `imageIds` and `coverThumb` are filled, not that the image document exists.
- **The debug check uses `FLAG_DEBUGGABLE`**, because `BuildConfig` is not enabled in `build.gradle.kts` (adding it is a Gradle change, rule R5).
- **TP.HCM listings sit about 0.5, 2, 4 and 8 km from the centre of district 1** (10.7769, 106.7009), so the 1, 3 and 5 km chips of the radius filter give different results. When `Geocoder` finds nothing, a TP.HCM address saved in A06 falls back to the same centre in `ProfileViewModel.findCoordinatesByProvince`, so the demo still works offline.
