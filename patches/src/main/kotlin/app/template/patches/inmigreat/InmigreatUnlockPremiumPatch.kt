package app.template.patches.inmigreat

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patcher.string
import app.template.patches.shared.Constants.INMIGREAT_COMPATIBILITY
import com.android.tools.smali.dexlib2.AccessFlags
import java.security.MessageDigest

/**
 * Unlocks Inmigreat — Immigration Guide & Case Tracker (com.changayaf.inmigreat v2.2.83).
 *
 * ## Platform
 *
 * React Native / Expo app. All subscription logic lives in the **Hermes v96 JS bundle**
 * (`assets/index.android.bundle`, 5 MB). There is no Java/Kotlin smali to patch.
 *
 * ## Entitlement model
 *
 * Subscriptions are server-side (api-pro.inmigreat.com). The GraphQL `me` query
 * returns `entitlements { entitlementKey expiresAt }`. The mapping function `toUser`
 * (Function #16589) transforms the server response into a local user object:
 *
 *   `user.isPro = resolveProEntitlement(entitlements) !== null`
 *
 * `resolveProEntitlement` (Function #16587) returns the first matching entitlement
 * or **null** when the server returns an empty array (free user).
 *
 * Feature gates in `MainShell` (Function #20291) and throughout the app read
 * `user.isPro` to decide what to render (Lexi AI, Pro alerts, Judge Analytics…).
 *
 * ## Binary patches (5 total, all in `assets/index.android.bundle`)
 *
 * ### Patch 1 — `toUser` isPro computation (Function #16589)
 *   File offset: **`0x3862fb`** (rel `0x8b` within the function)
 *   `StrictNeq r4, r5, r2` (4 B: `11 04 05 02`)
 *   → `LoadConstTrue r4` + `Nop` × 2 (`78 04 00 00`)
 *   Forces `user.isPro = true` for all users after the API responds.
 *
 * ### Patch 2a — `hasEntitlement` expiry check (Function #16588)
 *   File offset: **`0x386266`** (rel `0x54`)
 *   `Greater r0, r1, r0` (4 B: `14 00 01 00`)
 *   → `LoadConstTrue r0` + `Ret r0` (`78 00 5c 00`)
 *   Expired entitlements always return true.
 *
 * ### Patch 2b — `hasEntitlement` key-mismatch (Function #16588)
 *   File offset: **`0x38626c`** (rel `0x5a`)
 *   `LoadConstFalse r0` opcode (`0x79`) → `LoadConstTrue` (`0x78`)
 *   Missing-key case returns true.
 * *
 * After all patches the bundle's trailing **SHA-1 checksum** (last 20 bytes) is
 * recomputed so Hermes accepts the modified file at runtime.
 */
private val CheckLicenseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(string("Skipping license check in isolated process.")),
)

private val inmigreatBypassLicensePatch = bytecodePatch(
    name = "Bypass PairIP License Check",
    description = "Makes LicenseClient.checkLicense() a no-op so the app never shows the license-denied blocking screen.",
    default = false,
) {
    compatibleWith(INMIGREAT_COMPATIBILITY)

    execute {
        CheckLicenseFingerprint
            .match(classDefBy(CheckLicenseFingerprint.definingClass!!))
            .method
            .apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "return-void")
            }
    }
}

@Suppress("unused")
val inmigreatUnlockPremiumPatch = rawResourcePatch(
    name = "Unlock Pro",
    description = "Unlock Pro Features in app.",
    default = true,
) {
    compatibleWith(INMIGREAT_COMPATIBILITY)
    dependsOn(inmigreatBypassLicensePatch)

    execute {
        val bundleFile = get("assets/index.android.bundle")
            ?: error("Could not find index.android.bundle")

        val raw = bundleFile.readBytes().toMutableList()

        // ── Patch 1: toUser isPro (Function #16589, rel 0x8b) ────────────────
        // StrictNeq r4,r5,r2 (4B) → LoadConstTrue r4, Nop, Nop
        val p1 = 0x3862fb
        raw[p1 + 0] = 0x78.toByte(); raw[p1 + 1] = 0x04.toByte() // LoadConstTrue r4
        raw[p1 + 2] = 0x8e.toByte(); raw[p1 + 3] = 0x00.toByte() // Jmp +0 (jump to next = no-op; 0x00=Unreachable in v96!)

        // ── Patch 2a: hasEntitlement expiry (Function #16588, rel 0x54) ──────
        // Greater r0,r1,r0 (4B) → LoadConstTrue r0 + Ret r0
        val p2a = 0x386266
        raw[p2a + 0] = 0x78.toByte(); raw[p2a + 1] = 0x00.toByte()
        raw[p2a + 2] = 0x5c.toByte(); raw[p2a + 3] = 0x00.toByte()

        // ── Patch 2b: hasEntitlement key-miss (Function #16588, rel 0x5a) ────
        // LoadConstFalse opcode 0x79 → LoadConstTrue 0x78
        raw[0x38626c] = 0x78.toByte()

        // ── Update trailing SHA-1 ─────────────────────────────────────────────
        val newHash = MessageDigest.getInstance("SHA-1").digest(raw.dropLast(20).toByteArray())
        for (i in 0 until 20) raw[raw.size - 20 + i] = newHash[i]

        bundleFile.writeBytes(raw.toByteArray())
    }
}
