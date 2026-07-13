package app.template.patches.migraconnect

import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.Constants.MIGRACONNECT_COMPATIBILITY
import java.security.MessageDigest

/**
 * Unlocks MigraConnect Enterprise tier (com.tecso.MigraConnect v2.8.1).
 *
 * ## Plan tiers (AlertsByPlanTier)
 * basic=1, standard=5, premium=10, business=50, enterprise=250 alerts/category
 *
 * ## Patches (Hermes v96 bundle)
 *
 * ### Patch 1 — getSubscriptionPlanFromCustomerInfo (#15096 @ 0x333d40)
 * Makes RevenueCat entitlement check always return "enterprise".
 * ```
 * Before: 6c 02 01  LoadParam r2 / 76 00  LoadConstUndefined r0
 * After:  73 00 D9 37  LoadConstString r0, "enterprise" / 5c 00  Ret r0
 * ```
 *
 * ### Patch 2 — getSubscriptionPlan generator body (#15101 @ 0x333e4c)
 * Skips async init()+getCustomerInfo() flow → fixes startup race where
 * null subscriptionPlan triggered the paywall/Google Play purchase dialog.
 * ```
 * Before: 6c 02 01  LoadParam r2  (then awaits init + getCustomerInfo)
 * After:  73 04 D9 37  LoadConstString r4, "enterprise"
 *         88           CompleteGenerator
 *         5c 04        Ret r4
 * ```
 */
@Suppress("unused")
val migraConnectUnlockEnterprisePatch = rawResourcePatch(
    name = "Unlock Enterprise",
    description = "Unlocks Enterprise features in app.",
    default = true,
) {
    compatibleWith(MIGRACONNECT_COMPATIBILITY)

    execute {
        val bundleFile = get("assets/index.android.bundle")
            ?: error("Could not find index.android.bundle")

        val raw = bundleFile.readBytes().toMutableList()

        // ── Patch 1: getSubscriptionPlanFromCustomerInfo (#15096 @ 0x333d40) ──
        // LoadConstString r0, "enterprise"(14297=0x37D9) + Ret r0
        val patch1 = 0x333d40
        raw[patch1 + 0] = 0x73.toByte()
        raw[patch1 + 1] = 0x00.toByte()
        raw[patch1 + 2] = 0xD9.toByte()
        raw[patch1 + 3] = 0x37.toByte()
        raw[patch1 + 4] = 0x5c.toByte()
        raw[patch1 + 5] = 0x00.toByte()

        // ── Patch 2: getSubscriptionPlan generator body (#15101 @ 0x333e4c) ──
        // After StartGenerator+LoadThisNS+ResumeGenerator+JmpTrueLong (rel=0x0c):
        // LoadConstString r4, "enterprise" + CompleteGenerator + Ret r4
        val patch2 = 0x333e40 + 0x0c
        raw[patch2 + 0] = 0x73.toByte()
        raw[patch2 + 1] = 0x04.toByte()
        raw[patch2 + 2] = 0xD9.toByte()
        raw[patch2 + 3] = 0x37.toByte()
        raw[patch2 + 4] = 0x88.toByte() // CompleteGenerator
        raw[patch2 + 5] = 0x5c.toByte()
        raw[patch2 + 6] = 0x04.toByte()

        // ── Update trailing SHA-1 ─────────────────────────────────────────────
        val newHash = MessageDigest.getInstance("SHA-1").digest(raw.dropLast(20).toByteArray())
        for (i in 0 until 20) raw[raw.size - 20 + i] = newHash[i]

        bundleFile.writeBytes(raw.toByteArray())
    }
}
