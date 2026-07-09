package app.template.patches.scrl

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object Fingerprints {
    // Obfuscated static helper isPremium(CustomerInfo) -> boolean.
    // Looks up RC entitlement keyed "premium" and checks isActive().
    // Root cause of free-state: if the server returns no "premium" entitlement,
    // EntitlementInfos.get() returns null and the method returns false immediately.
    // Fingerprinted by: static, (CustomerInfo)Z, strings ["premium"],
    // calls getEntitlements + isActive — unique in the codebase.
    val IsPremiumHelperFingerprint = Fingerprint(
        accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
        returnType = "Z",
        parameters = listOf("Lcom/revenuecat/purchases/CustomerInfo;"),
        strings = listOf("premium"),
    )
}
