package app.template.patches.cloudflare.warp

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.WARP_COMPATIBILITY

/**
 * Spoof WARP+ Unlimited — UI unlock via AccountData constructor intercept.
 *
 * ── What this patches ────────────────────────────────────────────────────────
 * Cloudflare stores the user's account tier in AccountData.b (WarpPlusState),
 * deserialised from the /accounts API response field "account_type".
 *
 * WarpPlusState enum: FREE | LIMITED | TEAM | UNLIMITED
 *
 * By injecting `sget-object p2, WarpPlusState;->UNLIMITED` at index 0 of the
 * primary constructor, p2 (the accountType parameter) is overwritten with
 * UNLIMITED before the constructor stores it into the final field `b`.
 * Every AccountData instance created — including those deserialised from disk
 * cache — will have accountType == UNLIMITED for the lifetime of the process.
 *
 * ── Why the constructor (not the getter) ─────────────────────────────────────
 * AccountData.b is a `final` Kotlin val. ART enforces that final instance fields
 * may only be written inside the declaring class's own <init> method. Writing it
 * from any other method (e.g. a WarpDataStore getter override) causes a hard
 * VerifyError on class load, crashing the app before any activity is displayed.
 *
 * ── Stability ────────────────────────────────────────────────────────────────
 * AccountData is Moshi-serialised: all field names are preserved at runtime via
 * @Json annotations. R8 cannot rename the class, constructor, or enum values.
 * WarpPlusState is a sealed enum with @Json names; UNLIMITED is Moshi-kept.
 * The 9-parameter constructor signature is unique and stable across updates.
 *
 * ── Verified v6.38.8 (versionCode 5431) ─────────────────────────────────────
 * Constructor signature, .registers 11, iput-object p2 → field b: all confirmed
 * identical to v6.38.7. No structural changes in AccountData or WarpPlusState.
 */
@Suppress("unused")
val unlockWarpPlusPatch = bytecodePatch(
    name = "Spoof WARP+ Unlimited UI",
    description = "Forces WarpPlusState to UNLIMITED on every AccountData instance by intercepting the primary constructor before the account type field is written.",
    default = true,
) {
    compatibleWith(WARP_COMPATIBILITY)

    execute {
        AccountDataConstructorFingerprint.method.addInstructions(
            0,
            "sget-object p2, Lcom/cloudflare/app/data/warpapi/WarpPlusState;->UNLIMITED:Lcom/cloudflare/app/data/warpapi/WarpPlusState;",
        )
    }
}
