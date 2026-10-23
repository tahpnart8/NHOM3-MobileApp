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

## Decoding a `Uri` opens it twice

`BitmapFactory` needs the bounds before it can choose `inSampleSize`, and an `InputStream` from a
`ContentResolver` cannot be rewound. So `UriSource` calls `openInputStream` once for the bounds pass and again
for the real decode. A single stream reused for both returns null the second time, silently.

## `calculateSampleSize` must reject a zero target

Integer division bottoms out at 0 and `0 >= 0` is true, so a target size of 0 makes the doubling loop run
forever. `chooseTargetSize` never returns 0 today, which is exactly why the guard belongs in the arithmetic
rather than in the caller.
