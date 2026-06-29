package app.template.patches.ninjvapn

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * La8/x;->f()Z — isPremium check.
 * Returns true if field f (premium order-id string) is non-null.
 * Called in Dashboard to set l0.r = PREMIUM before UI setup.
 * Unique: only Z-returning method in La8/x with no params.
 */
internal val isPremiumFingerprint = Fingerprint(
    definingClass = "La8/x;",
    name = "f",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC)
)

/**
 * Premium$Mode.get(Purchase) — maps purchaseState to PREMIUM/PENDING/UNKNOWN.
 * Called by SheetUpgrade billing callback (La2/h) and Dashboard init.
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
 * l0.a(l0) — showUpgradeSheet gate.
 * Checks l0.r == PREMIUM; if not PREMIUM and not PENDING → opens SheetUpgrade.
 * Unique: static method returning Z on Lapp/ninjavpn/android/app/l0 referencing SheetUpgrade class.
 */
internal val showUpgradeSheetFingerprint = Fingerprint(
    definingClass = "Lapp/ninjavpn/android/app/l0;",
    name = "a",
    returnType = "Z",
    parameters = listOf("Lapp/ninjavpn/android/app/l0;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)
