package app.template.patches.myanimelist.supporter

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * User.isSupporter() — non-obfuscated MAL-owned getter for the supporter boolean.
 *
 * Smali (classes4, Lnet/myanimelist/data/entity/User;):
 *   .method public final isSupporter()Z
 *     iget-boolean v0, p0, Lnet/myanimelist/data/entity/User;->isSupporter:Z
 *     return v0
 *   .end method
 *
 * Called per-item in SeasonalPagingAdapter, SearchPagingAdapter, WomPagingAdapter
 * and SearchTopAdAsset — all after the user object is loaded. Returning true here
 * suppresses AdView show and AdRequest.load at all adapter call sites.
 *
 * Anchored on definingClass + name alone (both non-obfuscated); no filters needed.
 * DEX: classes4
 */
object UserIsSupporterFingerprint : Fingerprint(
    definingClass = "Lnet/myanimelist/data/entity/User;",
    name = "isSupporter",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
)

/**
 * HomeActivity.onCreate() — the one place MobileAds.initialize() is called.
 *
 * This call is gated by User.isSupporter() BUT the User object is null at onCreate
 * time (loaded async from API after activity creation). The null-check branch
 * bypasses isSupporter() and initialises MobileAds unconditionally.
 *
 * Fix: fingerprint by the MobileAds.initialize() methodCall (unique across the APK —
 * only one call site) and skip it with returnEarly inside the enclosing scope.
 * Since we can't returnEarly on onCreate itself, we replace the instruction at the
 * matched index with a nop to suppress initialization entirely.
 *
 * Smali evidence (classes4, HomeActivity.smali:1245-1246):
 *   sget-object p1, Lnet/myanimelist/presentation/activity/n1;->a:...
 *   invoke-static {p0, p1}, Lcom/google/android/gms/ads/MobileAds;->initialize(...)V
 *
 * DEX: classes4
 */
object MobileAdsInitFingerprint : Fingerprint(
    definingClass = "Lnet/myanimelist/presentation/activity/HomeActivity;",
    name = "onCreate",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PROTECTED),
    parameters = listOf("Landroid/os/Bundle;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/google/android/gms/ads/MobileAds;",
            name = "initialize",
        ),
    ),
)
