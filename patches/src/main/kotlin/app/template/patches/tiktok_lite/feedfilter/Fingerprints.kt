package app.template.patches.tiktok_lite.feedfilter

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ── Primary ad gates (Aweme model methods) ────────────────────────────────────

// Aweme.isAd()Z -- reads isAd:Z field then checks awemeRawAd!=null.
// PUBLIC, no params. Smali verified. Patching to false kills the primary ad signal.
internal object AwemeIsAdFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;",
    name = "isAd",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Z",
    parameters = emptyList(),
)

// Aweme.isAdTraffic()Z -- calls isAd() then isSoftAd(). PUBLIC, no params.
// isAd() is already patched; patch this too to also kill isSoftAd() path.
internal object AwemeIsAdTrafficFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;",
    name = "isAdTraffic",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Z",
    parameters = emptyList(),
)

// Aweme.isPseudoAd()Z -- reads commerceInfo. Caught by tiktokkk.extBool("isPseudoAd").
// Patching to false clears commerce-disguised ads from the feed.
internal object AwemeIsPseudoAdFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;",
    name = "isPseudoAd",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Z",
    parameters = emptyList(),
)

// Aweme.isMonetizationTraffic()Z -- delegates to isAdTraffic(). Stable name.
// Source: tiktokkk.extBool("isMonetizationTraffic"). Return false.
internal object AwemeIsMonetizationTrafficFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;",
    name = "isMonetizationTraffic",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Z",
    parameters = emptyList(),
)

// ── Live content gate ─────────────────────────────────────────────────────────

// Aweme.isLive()Z -- checks awemeType==0x65 (101). PUBLIC, no params.
// Source: Toki installFeedFilters / tiktokkk FeedFilter.isLiveCard.
// Optional patch -- allows hiding live cards from the feed.
internal object AwemeIsLiveFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;",
    name = "isLive",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Z",
    parameters = emptyList(),
)

// ── FeedItemList getter -- upstream filter point ──────────────────────────────

// FeedItemList.getItems()List -- every consumer reads the feed through here.
// tiktokkk FeedFilter hooks this getter and filters items in-place.
// Stable class + method name, DEX classes4.
internal object FeedItemListGetItemsFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/main/homepage/fragment/data/model/FeedItemList;",
    name = "getItems",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Ljava/util/List;",
    parameters = emptyList(),
)
