package app.template.patches.andropods.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Targets the constructor <init>()V of the PreferencesFragment (a2.l).
 *
 * m0:Z (volatile boolean isPro) defaults to false. On first launch, e0() fires
 * immediately to populate the preference UI — before queryPurchases() returns,
 * so m0 is still false and all premium switches show as disabled.
 *
 * Fix: inject `const/4 v0, 0x1 / iput-boolean v0, p0, La2/l;->m0:Z` at the END of
 * the constructor (before return-void) so m0=true from the moment the fragment exists.
 * e0() then always sees isPro=true on every launch, including the first.
 *
 * Stable anchor: Looper.getMainLooper() — appears exactly once in a2/l, inside <init>()V.
 * Access flags: PUBLIC CONSTRUCTOR, return V, no parameters.
 */
object AndroPodsPremiumInitFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    parameters = emptyList(),
    filters = listOf(
        methodCall(
            definingClass = "Landroid/os/Looper;",
            name = "getMainLooper",
        ),
    ),
)

/**
 * Targets the purchase result handler Y(List<Purchase>)V of the PreferencesFragment (a2.l).
 *
 * Called by X(n0/e, List)V when queryPurchasesAsync() completes. Iterates the purchase
 * list, extracts productIds from the Purchase JSON, and sets m0:Z = true if "pro" is found.
 *
 * Kept as a defence-in-depth patch: even if the constructor patch covers launch 1,
 * this ensures m0 is never reset to false by a billing re-query returning empty results.
 *
 * Stable anchors (in smali instruction order):
 *   1. string "productIds"           — JSONObject key for the productId array
 *   2. JSONObject.optJSONArray()     — reads the array immediately after has()
 *   3. string "pro"                  — the product ID literal
 *   4. ArrayList.contains()          — the membership test that gates iput-boolean m0
 */
object AndroPodsPurchaseResultFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/util/List;"),
    filters = listOf(
        string("productIds"),
        methodCall(
            definingClass = "Lorg/json/JSONObject;",
            name = "optJSONArray",
        ),
        string("pro"),
        methodCall(
            definingClass = "Ljava/util/ArrayList;",
            name = "contains",
        ),
    ),
)
