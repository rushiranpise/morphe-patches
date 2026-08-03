package app.template.patches.stickerly

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.STICKERLY_COMPATIBILITY
import app.template.patches.shared.clearBody

// ── Patch strategy ───────────────────────────────────────────────────────────
//
// Root fix: patch the SharedPreferences cache reader (rx/l.a()) so it always
// returns a synthetic premium SubscriptionModel. Every consumer reads through
// this single method:
//
//   rx/l.a() ← ty/n.a()Z   ← boolean feature gates (40+ callers)
//           ← ty/n.b()    ← full-object consumers (mg/lb ViewModel, etc.)
//
// Patching at this root covers both paths. The previous attempt targeted only
// ty/n.a()Z (the boolean wrapper), which missed ty/n.b() callers like the main
// paywall ViewModel (mg/lb) that reads SubscriptionModel.a directly.
//
// Second hook: patch the server-response mapper (rx/k.h()) so that when the
// billing server responds, the model written to SharedPreferences is also premium.
// This is belt-and-suspenders — the cache-read patch already returns premium
// on every read regardless of what's stored, but patching the mapper ensures
// the persisted value is premium too.
//
// Register layout for rx/l.a() (.registers 5, non-static):
//   Registers: v0, v1, v2, v3 (locals) + p0 (this = v4 slot)
//   After clearBody(), p0 is overwritten as scratch for the second Date object.
//   This is safe: ART verifier allows overwriting parameter registers; we
//   return-object v0 before p0 is ever read as 'this'.
//   v0 = new SubscriptionModel
//   v1 = new Date  →  init with v2+v3 = 0L  →  purchaseDate
//   p0 = new Date  →  init with v2+v3 = 0x3bb2cc3d800L  →  expiryDate (year ~2100)
//   v2 = const 1   →  subscribed = true
//   v3 = "plus"    →  productId
//   invoke-direct {v0, v2, v3, v1, p0}  →  SubscriptionModel(true, "plus", epoch, farFuture)
//   return-object v0
//
// Note: Facebook Login requires the Facebook app to be uninstalled first
// (signature mismatch after re-signing prevents FB auth).

private const val SUBSCRIPTION_MODEL =
    "Lcom/snowcorp/stickerly/android/base/domain/payment/SubscriptionModel;"

@Suppress("unused")
val stickerlyUnlockPlusPatch = bytecodePatch(
    name = "Unlock Plus",
    description = "Unlocks Sticker.ly Plus subscription. " +
        "Note: For Facebook Login, uninstall the Facebook app first.",
    default = true,
) {
    compatibleWith(STICKERLY_COMPATIBILITY)

    execute {
        // Shared smali block — both target methods have enough registers for this body.
        // See class-level kdoc for register layout details.
        val premiumModelSmali = """
            new-instance v0, $SUBSCRIPTION_MODEL
            new-instance v1, Ljava/util/Date;
            const-wide/16 v2, 0x0
            invoke-direct {v1, v2, v3}, Ljava/util/Date;-><init>(J)V
            new-instance p0, Ljava/util/Date;
            const-wide v2, 0x3bb2cc3d800L
            invoke-direct {p0, v2, v3}, Ljava/util/Date;-><init>(J)V
            const/4 v2, 0x1
            const-string v3, "plus"
            invoke-direct {v0, v2, v3, v1, p0}, $SUBSCRIPTION_MODEL-><init>(ZLjava/lang/String;Ljava/util/Date;Ljava/util/Date;)V
            return-object v0
        """.trimIndent()

        // Hook 1 — SharedPreferences cache reader: the single root all subscription checks
        // delegate to. Fixing here covers the boolean gate (ty/n.a()Z) AND the full-object
        // accessor (ty/n.b()) that drives the main paywall ViewModel.
        SubscriptionCacheReadFingerprint.method.apply {
            clearBody()
            addInstructions(0, premiumModelSmali)
        }

        // Hook 2 — server response mapper: ensures the value persisted to SharedPreferences
        // after a billing server sync is also premium. Uses v0..v4 from its 10-register budget.
        SubscriptionResponseMapperFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    new-instance v0, $SUBSCRIPTION_MODEL
                    new-instance v1, Ljava/util/Date;
                    const-wide/16 v2, 0x0
                    invoke-direct {v1, v2, v3}, Ljava/util/Date;-><init>(J)V
                    new-instance v4, Ljava/util/Date;
                    const-wide v2, 0x3bb2cc3d800L
                    invoke-direct {v4, v2, v3}, Ljava/util/Date;-><init>(J)V
                    const/4 v2, 0x1
                    const-string v3, "plus"
                    invoke-direct {v0, v2, v3, v1, v4}, $SUBSCRIPTION_MODEL-><init>(ZLjava/lang/String;Ljava/util/Date;Ljava/util/Date;)V
                    return-object v0
                """.trimIndent(),
            )
        }
    }
}
