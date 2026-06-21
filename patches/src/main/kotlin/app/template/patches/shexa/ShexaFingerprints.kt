package app.template.patches.shexa

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * ZipoApps PremiumHelper — d.v()Z
 * Reads "has_active_purchase" from SharedPreferences.
 * This is the primary gate used across all billing flow checks.
 * Fingerprinted by: definingClass + returnType + unique string "has_active_purchase".
 */
val PremiumHelperHasActivePurchaseFingerprint = Fingerprint(
    definingClass = "Lcom/zipoapps/premiumhelper/d;",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("has_active_purchase")
)

/**
 * ZipoApps PremiumHelper — a.u()Z
 * Returns field g:Z, the runtime isPremium flag set after billing validation.
 * Fingerprinted by: definingClass + name + returnType + accessFlags.
 */
val PremiumHelperIsPremiumFingerprint = Fingerprint(
    definingClass = "Lcom/zipoapps/premiumhelper/a;",
    name = "u",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
