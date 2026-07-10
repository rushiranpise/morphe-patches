package app.template.patches.scrl

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.scrl.Fingerprints.IsPremiumHelperFingerprint
import app.template.patches.shared.Constants.SCRL_COMPATIBILITY

@Suppress("unused")
val scrlUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium feature in app.",
    default = true
) {
    compatibleWith(SCRL_COMPATIBILITY)

    execute {
        // Root cause (from log): RC's BillingWrapper fires DEVELOPER_ERROR (patched APK signature),
        // so no purchase completes. The RC server returns no "premium" entitlement for the
        // anonymous user ($RCAnonymousID). zi2.a(CustomerInfo) calls
        // EntitlementInfos.get("premium") → null → short-circuits to false before isActive()
        // is ever called. The previous EntitlementInfo.isActive() patch was unreachable.
        //
        // Fix: patch zi2.a() (the isPremium helper) directly to always return true.
        // Fingerprinted by: static, (CustomerInfo)Z, string literal "premium",
        // calls getEntitlements() + isActive() — uniquely identifies this method.
        IsPremiumHelperFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
