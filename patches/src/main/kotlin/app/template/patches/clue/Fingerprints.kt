package app.template.patches.clue

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * com/helloclue/userappmode/ProductTier – stable unobfuscated data class.
 * Fields: a:Z = hasCluePlus, b:String = analyticsValue
 *
 * hasCluePlus is the ROOT subscription gate. It flows via two independent paths:
 *
 * Path 1 (launch):
 *   InitializeResponseDto.productTier.hasCluePlus
 *   → gc.smali writes it to UserPreference.isSubscribed_ via UserPreference.l(pref, Z)
 *   → mp9.b() reads getIsSubscribed() → constructs User(y6h) with isSubscribed = y6h.j:Z
 *   → 12 gate sites across the app read y6h.j:Z
 *
 * Path 2 (paywall/subscription refresh):
 *   PaywallResponse.productTier.hasCluePlus
 *   → ts1 reads it → calls ys1.t(Z) → emits to subscription StateFlow → UI gates
 *
 * Patching ProductTier.a()Z (getHasCluePlus) to always return true covers BOTH paths.
 */
internal val ProductTierHasCluePlusFingerprint = Fingerprint(
    definingClass = "Lcom/helloclue/userappmode/ProductTier;",
    name = "a",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

/**
 * com/helloclue/user/UserPreference.getIsSubscribed()Z – proto-generated accessor.
 *
 * Secondary gate: read directly by mp9 (UserPreference→User mapper).
 * Patching this ensures even if ProductTier is somehow null/bypassed,
 * the User object is always constructed with isSubscribed=true.
 */
internal val UserPreferenceIsSubscribedFingerprint = Fingerprint(
    definingClass = "Lcom/helloclue/user/UserPreference;",
    name = "getIsSubscribed",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)
