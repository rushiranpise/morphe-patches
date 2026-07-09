package app.template.patches.ninjvapn

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * core/c.f()Z — isPremium check.
 * Returns true if field z (premium order-id String) is non-null.
 * Called in Dashboard to set k0.r = PREMIUM before UI setup.
 * v1.4.6: La8/x;->f()Z → v1.4.7: Lapp/ninjavpn/android/app/core/c;->f()Z
 * Unique: only Z-returning no-param method in this class.
 */
internal val isPremiumFingerprint = Fingerprint(
    definingClass = "Lapp/ninjavpn/android/app/core/c;",
    name = "f",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC)
)

/**
 * Premium$Mode.get(Purchase) — maps purchaseState to PREMIUM/PENDING/UNKNOWN.
 * Called by billing callback and Dashboard init. Unchanged across versions.
 * Unique string: "purchaseState" inside the only static method of this enum class.
 */
internal val getPremiumModeFingerprint = Fingerprint(
    definingClass = "Lapp/ninjavpn/android/app/revenue/subscription/Premium\$Mode;",
    name = "get",
    returnType = "Lapp/ninjavpn/android/app/revenue/subscription/Premium\$Mode;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    filters = listOf(string("purchaseState"))
)

/**
 * k0.a(k0) — showUpgradeSheet gate.
 * Checks k0.r == PREMIUM; if not PREMIUM and not PENDING → opens SheetUpgrade.
 * v1.4.6: Lapp/ninjavpn/android/app/l0;->a(l0)Z → v1.4.7: Lapp/ninjavpn/android/app/k0;->a(k0)Z
 * Unique: static method returning Z on k0 referencing SheetUpgrade class.
 */
internal val showUpgradeSheetFingerprint = Fingerprint(
    definingClass = "Lapp/ninjavpn/android/app/k0;",
    name = "a",
    returnType = "Z",
    parameters = listOf("Lapp/ninjavpn/android/app/k0;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)
