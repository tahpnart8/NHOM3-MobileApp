package com.nhom3.pubgapp.data;

import com.google.firebase.firestore.GeoPoint;
import com.nhom3.pubgapp.model.Category;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.enums.ItemCondition;
import com.nhom3.pubgapp.model.enums.OfferType;
import com.nhom3.pubgapp.model.enums.TradeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Builds the demo categories and listings (goi-y.md, sample data). No Firebase here, so a JVM test can check it.
class DemoDataBuilder {

    static final String CATEGORY_ID_PREFIX = "demo_cat_";
    static final String LISTING_ID_PREFIX = "demo_listing_";
    static final int LISTINGS_PER_CHILD = 6;
    // The first children get one condition for every listing, so the reference price can use the same-condition set.
    static final int SAME_CONDITION_CHILDREN = 4;
    static final int IMAGE_VARIANTS = 6;

    // Centre of district 1 in Ho Chi Minh City; the radius filter chips are tried against it.
    static final double CENTER_LATITUDE = 10.7769;
    static final double CENTER_LONGITUDE = 106.7009;
    private static final double DEGREES_PER_KM = 0.009;

    private static final String PROVINCE_HCM = "TP.HCM";
    private static final String PROVINCE_HANOI = "Hà Nội";
    private static final String PROVINCE_DANANG = "Đà Nẵng";

    private static final String[] ROOT_NAMES = {
        "Điện tử", "Xe cộ", "Thời trang", "Đồ gia dụng", "Sách và học tập", "Thể thao"
    };
    private static final String[][] CHILD_NAMES = {
        {"Điện thoại", "Laptop"}, {"Xe đạp", "Xe máy"}, {"Giày dép", "Quần áo"},
        {"Nội thất", "Đồ điện gia dụng"}, {"Giáo trình", "Truyện tranh"},
        {"Dụng cụ gym", "Cầu lông"}
    };
    // Each keyword repeats in every title of its category, so similar listings and search find each other.
    private static final String[][] CHILD_KEYWORDS = {
        {"iphone", "macbook"}, {"xe dap", "honda"}, {"giay nike", "ao khoac"},
        {"ban go", "noi chien"}, {"giao trinh", "truyen tranh"}, {"ta tay", "vot cau long"}
    };
    private static final String[][] ROOT_FIELDS = {
        {"Dung lượng pin", "Bảo hành"}, {"Năm sản xuất", "Bảo hành"}, {"Kích cỡ", "Màu sắc"},
        {"Chất liệu", "Bảo hành"}, {"Tác giả", "Nhà xuất bản"}, {"Thương hiệu", "Bảo hành"}
    };
    private static final long[] BASE_PRICES = {
        8000000L, 15000000L, 1500000L, 12000000L, 900000L, 400000L,
        3000000L, 1200000L, 150000L, 80000L, 500000L, 300000L
    };
    private static final String[] TITLE_SUFFIXES = {
        "cũ còn mới", "giá rẻ", "chính hãng", "bản đẹp", "hàng tuyển", "cần bán gấp"
    };
    private static final ItemCondition[] MIXED_CONDITIONS = {
        ItemCondition.NEW, ItemCondition.USED, ItemCondition.LIKE_NEW
    };
    private static final String SAMPLE_FIELD_VALUE = "Tốt";
    private static final String DESCRIPTION_PREFIX =
            "Tin mẫu để thử gợi ý, tìm kiếm và giao dịch. Mã ";
    private static final long PRICE_STEP = 10000L;

    private DemoDataBuilder() {
    }

    static String rootId(int rootIndex) {
        return String.format(Locale.ROOT, "%sr%d", CATEGORY_ID_PREFIX, rootIndex + 1);
    }

    static String childId(int rootIndex, int childOfRoot) {
        return String.format(Locale.ROOT, "%sr%d_c%d", CATEGORY_ID_PREFIX, rootIndex + 1, childOfRoot + 1);
    }

    static List<Category> buildRootCategories() {
        List<Category> roots = new ArrayList<Category>();
        for (int root = 0; root < ROOT_NAMES.length; root++) {
            roots.add(newCategory(rootId(root), ROOT_NAMES[root], "", new ArrayList<String>()));
        }
        return roots;
    }

    static List<Category> buildChildCategories() {
        List<Category> children = new ArrayList<Category>();
        for (int root = 0; root < ROOT_NAMES.length; root++) {
            for (int child = 0; child < CHILD_NAMES[root].length; child++) {
                List<String> fields = new ArrayList<String>();
                for (String field : ROOT_FIELDS[root]) {
                    fields.add(field);
                }
                children.add(newCategory(childId(root, child), CHILD_NAMES[root][child], rootId(root), fields));
            }
        }
        return children;
    }

    private static Category newCategory(String id, String name, String parentId, List<String> fields) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setParentId(parentId);
        category.setConditionFields(fields);
        category.setActive(true);
        return category;
    }

    static List<String> allListingIds() {
        List<String> ids = new ArrayList<String>();
        int total = countChildren() * LISTINGS_PER_CHILD;
        for (int number = 1; number <= total; number++) {
            ids.add(listingId(number));
        }
        return ids;
    }

    static String listingId(int number) {
        return String.format(Locale.ROOT, "%s%02d", LISTING_ID_PREFIX, number);
    }

    static int countChildren() {
        int count = 0;
        for (String[] names : CHILD_NAMES) {
            count += names.length;
        }
        return count;
    }

    // imageIds and thumbs are parallel lists of saved sample images; the seller id is set by ListingRepository.create.
    static List<Listing> buildListings(List<String> imageIds, List<String> thumbs) {
        List<Listing> listings = new ArrayList<Listing>();
        int childNumber = 0;
        for (int root = 0; root < ROOT_NAMES.length; root++) {
            for (int child = 0; child < CHILD_NAMES[root].length; child++) {
                for (int variant = 0; variant < LISTINGS_PER_CHILD; variant++) {
                    int number = childNumber * LISTINGS_PER_CHILD + variant + 1;
                    int imageIndex = number % imageIds.size();
                    Listing listing = buildListing(root, child, childNumber, variant, number);
                    List<String> ids = new ArrayList<String>();
                    ids.add(imageIds.get(imageIndex));
                    listing.setImageIds(ids);
                    listing.setCoverThumb(thumbs.get(imageIndex));
                    listings.add(listing);
                }
                childNumber++;
            }
        }
        return listings;
    }

    private static Listing buildListing(int root, int child, int childNumber, int variant, int number) {
        Listing listing = new Listing();
        listing.setId(listingId(number));
        listing.setCategoryId(childId(root, child));
        listing.setTitle(CHILD_KEYWORDS[root][child] + " " + TITLE_SUFFIXES[variant]);
        listing.setDescription(DESCRIPTION_PREFIX + listingId(number) + ".");
        listing.setPrice(priceOf(BASE_PRICES[childNumber], variant));
        listing.setItemCondition(conditionOf(childNumber, variant));
        List<TradeType> tradeTypes = new ArrayList<TradeType>();
        tradeTypes.add(TradeType.SELL);
        listing.setTradeTypes(tradeTypes);
        List<OfferType> offerTypes = new ArrayList<OfferType>();
        offerTypes.add(OfferType.MONEY);
        listing.setAcceptedOfferTypes(offerTypes);
        listing.setProvince(provinceOf(variant));
        listing.setLocation(locationOf(variant));
        listing.setConditionValues(conditionValuesOf(root));
        return listing;
    }

    // From 40 percent to 165 percent of the base price, so the 25th and 75th percentile differ.
    static long priceOf(long basePrice, int variant) {
        long percent = 40L + 25L * variant;
        long raw = basePrice * percent / 100L;
        return (raw / PRICE_STEP) * PRICE_STEP;
    }

    static ItemCondition conditionOf(int childNumber, int variant) {
        if (childNumber < SAME_CONDITION_CHILDREN) {
            return ItemCondition.USED;
        }
        return MIXED_CONDITIONS[variant % MIXED_CONDITIONS.length];
    }

    // Variants 0 to 3 sit about 0.5, 2, 4 and 8 km from the centre of district 1, so the 1, 3 and 5 km chips all differ.
    static String provinceOf(int variant) {
        if (variant < 4) {
            return PROVINCE_HCM;
        }
        return variant == 4 ? PROVINCE_HANOI : PROVINCE_DANANG;
    }

    static GeoPoint locationOf(int variant) {
        double[] offsetsKm = {0.5, 2.0, 4.0, 8.0};
        if (variant < offsetsKm.length) {
            return new GeoPoint(CENTER_LATITUDE + offsetsKm[variant] * DEGREES_PER_KM, CENTER_LONGITUDE);
        }
        if (variant == 4) {
            return new GeoPoint(21.0285, 105.8542);
        }
        return new GeoPoint(16.0544, 108.2022);
    }

    private static Map<String, String> conditionValuesOf(int root) {
        Map<String, String> values = new HashMap<String, String>();
        for (String field : ROOT_FIELDS[root]) {
            values.put(field, SAMPLE_FIELD_VALUE);
        }
        return values;
    }
}
