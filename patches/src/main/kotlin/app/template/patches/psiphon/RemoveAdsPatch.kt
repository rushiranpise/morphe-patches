package app.template.patches.psiphon

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PSIPHON_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.template.patches.shared.ensureRegisters

// ─────────────────────────────────────────────────────────────────────────────
// Purchase JSON for getPurchase() replacement.
//
// Purchase.<init>(String purchaseJson, String signature):
//   • Parses purchaseJson into a JSONObject stored in field c.
//   • getProducts() reads JSONArray "productIds" from c.
//   • The subscription-info Fragment calls getPurchase().getProducts().get(0)
//     to build a Play Store manage-subscription URL — requires ≥1 element.
//
// Smali const-string cannot embed literal double-quote characters; they must
// be escaped as \" in smali source.  The FAKE_PURCHASE_JSON_SMALI constant
// below pre-escapes every " so the string is ready for direct interpolation
// into a smali instruction without further processing.
// ─────────────────────────────────────────────────────────────────────────────
private const val FAKE_PRODUCT_ID = "basic_ad_free_subscription"

// Pre-escaped for smali: every " replaced with \"
// Expands in the smali block to:
//   {"orderId":"0","purchaseToken":"morphe","productIds":["basic_ad_free_subscription"],...}
private const val FAKE_PURCHASE_JSON_SMALI =
    """{\"orderId\":\"0\",\"purchaseToken\":\"morphe\",\"productIds\":[\"$FAKE_PRODUCT_ID\"],\"purchaseState\":1,\"purchaseTime\":0}"""

private const val FAKE_PURCHASE_SIG = "morphe"

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Forces the subscription gate to report an unlimited subscription, " +
        "removing ads and the upgrade prompt. Constructs a minimal fake Purchase " +
        "to prevent NPE/IOOB in call sites that dereference getProducts().get(0).",
    default = true,
) {
    compatibleWith(PSIPHON_COMPATIBILITY)

    execute {

        // ── Layer 1: getStatus() → HAS_UNLIMITED_SUBSCRIPTION ────────────────
        //
        // Original body (.registers 2, non-static, 0 params):
        //   iget-object v0, p0, LB2/c;->b:LB2/j0$a;
        //   return-object v0
        //
        // STABLE replacement body — no const-class, no obfuscated field ref:
        //
        //   Every Java/Kotlin enum has a compiler-generated static method:
        //     valueOf(String name): EnumType
        //   We call the enum's own valueOf() using its descriptor captured at
        //   patch time from the method's returnType (e.g. "LB2/j0$a;").
        //   This survives R8 class renames — the type descriptor is read from
        //   the method signature, not hard-coded.
        //
        //   Register layout after ensureRegisters(3):
        //     v0 = scratch (const-string target + move-result + return)
        //     v1 = (unused scratch)
        //     v2 = p0 (this) — not used, body is self-contained
        //
        //   Instructions used: const-string, invoke-static, move-result-object,
        //   return-object — all confirmed working in inline smali compiler.
        GetSubscriptionStatusFingerprint.method.apply {
            val enumType = returnType   // e.g. "LB2/j0$a;" — captured at patch time
            // valueOf(String) descriptor for this enum type:
            val valueOfDescriptor = "$enumType->valueOf(Ljava/lang/String;)$enumType"
            ensureRegisters(3)          // original has 2; need v0 + p0→v2
            clearBody()
            addInstructions(
                0,
                """
                const-string v0, "HAS_UNLIMITED_SUBSCRIPTION"
                invoke-static {v0}, $valueOfDescriptor
                move-result-object v0
                return-object v0
                """.trimIndent(),
            )
        }

        // ── Layer 2: getPurchase() → well-formed fake Purchase ────────────────
        //
        // Original body (.registers 2, non-static, 0 params):
        //   iget-object v0, p0, LB2/c;->c:Lcom/android/billingclient/api/Purchase;
        //   return-object v0
        //
        // Constructs a minimal Purchase via the stable SDK constructor:
        //   Purchase(String purchaseJson, String signature)
        // JSON pre-escaped for smali (\" instead of ") at definition time.
        //
        // Register layout after ensureRegisters(4):
        //   v0 = new Purchase instance
        //   v1 = purchaseJson string
        //   v2 = signature string
        //   v3 = p0 (this) — not used
        GetPurchaseFingerprint.method.apply {
            ensureRegisters(4)
            clearBody()
            addInstructions(
                0,
                """
                new-instance v0, Lcom/android/billingclient/api/Purchase;
                const-string v1, "$FAKE_PURCHASE_JSON_SMALI"
                const-string v2, "$FAKE_PURCHASE_SIG"
                invoke-direct {v0, v1, v2}, Lcom/android/billingclient/api/Purchase;-><init>(Ljava/lang/String;Ljava/lang/String;)V
                return-object v0
                """.trimIndent(),
            )
        }

        // ── Layer 3: hasValidPurchase() → true ───────────────────────────────
        //
        // Original body (.registers 3, non-static, 0 params):
        //   Three getStatus() calls + enum comparisons → returns true if any match.
        //
        // With Layer 1 in place this is redundant on the happy path.
        // Kept as defence-in-depth: shortcircuits the predicate before it reaches
        // any getStatus() dispatch, guarding against Reactive pipeline races where
        // a lambda captured pre-patch could still call hasValidPurchase() once.
        //
        // Existing .registers 3 is sufficient: only needs v0.
        HasValidPurchaseFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                const/4 v0, 0x1
                return v0
                """.trimIndent(),
            )
        }
    }
}
