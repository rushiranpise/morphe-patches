package app.template.patches.sai.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_SAI
import app.template.patches.shared.returnEarly
import app.template.patches.shared.disablePairIPLicenseCheckPatch

/**
 * Unlocks SAI Split APKs Installer premium (com.mtv.sai).
 *
 * ## Billing architecture
 *
 * SAI uses RevenueCat with entitlement ID "SAI Premium".
 * Configured in App.onCreate() with API key goog_dBqYIdndYvcMygUHRDdMpxICTZH.
 *
 * Two obfuscated R8 wrapper classes handle billing state (names change every update):
 *
 *   PurchaseCallbackFingerprint target (invoke(Object,Object)Object):
 *     Called after a successful purchase. Checks CustomerInfo entitlement,
 *     then calls g(CustomerInfo) to persist the result.
 *
 *   CustomerInfoStateWriterFingerprint target (g(CustomerInfo)V):
 *     Called on app start, purchase, and restore to persist premium state.
 *     Reads CustomerInfo.getEntitlements().get("SAI Premium").isActive()
 *     and writes the result to local storage.
 *
 * ## Fingerprint stability
 *
 * Both fingerprints are anchored ONLY on RevenueCat SDK method calls:
 *   CustomerInfo.getEntitlements() → EntitlementInfos.get() → EntitlementInfo.isActive()
 *
 * RevenueCat is a published SDK — these method names are part of its stable
 * binary API and are NEVER renamed by R8 or obfuscated by any tool.
 * The obfuscated wrapper class names (wr0, xr0) are intentionally excluded
 * from both fingerprints so updates to the app don't break them.
 *
 * ## Strategy
 *
 * returnEarly() on both targets:
 *   - PurchaseCallbackFingerprint: returns null-Object before writing purchase result.
 *   - CustomerInfoStateWriterFingerprint: nops the state persistence write.
 *
 * Together these ensure no "not premium" state is ever written, regardless of
 * what RevenueCat returns at runtime.
 */
@Suppress("unused")
val saiPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks SAI Split APKs Installer premium by bypassing " +
        "RevenueCat entitlement checks using stable SDK-anchored fingerprints.",
) {
    compatibleWith(COMPATIBILITY_SAI)
    dependsOn(disablePairIPLicenseCheckPatch)

    execute {
        // Step 1: Nop the purchase callback entitlement check.
        // Returns null-Object before CustomerInfo.getEntitlements() is called.
        // Safe: caller handles null return via Kotlin nullability.
        PurchaseCallbackFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return-object v0
            """.trimIndent(),
        )

        // Step 2: Nop the CustomerInfo state writer.
        // Prevents "SAI Premium" entitlement check result from being persisted.
        // Called on every app start and RevenueCat CustomerInfo update —
        // blocking it ensures no "not premium" state is ever stored.
        CustomerInfoStateWriterFingerprint.method.returnEarly()
    }
}
