package app.template.patches.batteryguru.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BATTERYGURU_COMMUNITY_COMPATIBILITY
import app.template.patches.shared.Constants.BATTERYGURU_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

// Supports:
//   Battery Guru (Play Store APKS) — v2.5.0.6 (versionCode 721)
//   Battery Guru Community  (APK)  — v2.5.0.5 (versionCode 714)
//
// Both variants share the same package (com.paget96.batteryguru) and identical
// billing/premium class structure — same string anchors, same method shapes.
// The execute block runs unchanged for both; only the Compatibility differs.

@Suppress("unused")
val batteryGuruUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium and marks the yearly plan as purchased.",
    default = true,
) {
    compatibleWith(BATTERYGURU_COMPATIBILITY)
    compatibleWith(BATTERYGURU_COMMUNITY_COMPATIBILITY)

    execute {
        fun Method.stringLiterals(): Set<String> =
            implementation?.instructions?.mapNotNull { instruction ->
                (instruction as? ReferenceInstruction)?.reference
                    ?.let { it as? StringReference }?.string
            }?.toSet().orEmpty()

        fun Method.references(pattern: String): Boolean =
            implementation?.instructions?.any { instruction ->
                (instruction as? ReferenceInstruction)?.reference?.toString()?.contains(pattern) == true
            } == true

        // ── Billing repository ──────────────────────────────────────────────
        // Anchored by "last_product_id" — a stable SharedPreferences key.
        // Play:      hm5  (v2.5.0.6)
        // Community: ub1  (v2.5.0.5)
        val billingRepo = classDefByStrings("last_product_id").singleOrNull()
            ?: throw PatchException("Battery Guru: billing repository not found or ambiguous.")
        val billingStrings = billingRepo.methods.flatMap { it.stringLiterals() }.toSet()
        if ("video_time" !in billingStrings || "rewarded_ad_count" !in billingStrings) {
            throw PatchException("Battery Guru: billing repository key check failed.")
        }

        val mutableBillingRepo = mutableClassDefBy(billingRepo)

        // 1. Subscription gate: only method in the billing repo that returns Z,
        //    takes no parameters, and references all three product-ID strings.
        mutableBillingRepo.methods.singleOrNull { method ->
            method.returnType == "Z" &&
                method.parameterTypes.isEmpty() &&
                method.stringLiterals().let { literals ->
                    "one_week_subscription" in literals &&
                        "one_month_subscription" in literals &&
                        "one_year_subscription" in literals
                }
        }?.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            ?: throw PatchException("Battery Guru: subscription gate not found.")

        // 2. Selected-product reader: returns Object, takes 1 parameter,
        //    reads "last_product_id" from SharedPreferences.
        mutableBillingRepo.methods.singleOrNull { method ->
            method.returnType == "Ljava/lang/Object;" &&
                method.parameterTypes.size == 1 &&
                "last_product_id" in method.stringLiterals()
        }?.addInstructions(0, "const-string v0, \"one_year_subscription\"\nreturn-object v0")
            ?: throw PatchException("Battery Guru: selected product reader not found.")

        // 3. Active-premium reader: returns Object, takes 1 parameter,
        //    reads "video_time" (rewarded-ad expiry) and calls currentTimeMillis.
        //    Forcing Boolean.TRUE means the rewarded-ad grant is always treated as active,
        //    which satisfies the premium check upstream.
        mutableBillingRepo.methods.singleOrNull { method ->
            method.returnType == "Ljava/lang/Object;" &&
                method.parameterTypes.size == 1 &&
                "video_time" in method.stringLiterals() &&
                "rewarded_ad_count" !in method.stringLiterals() &&
                method.references("currentTimeMillis")
        }?.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """,
        ) ?: throw PatchException("Battery Guru: active premium reader not found.")

        // 4. Premium-state writer: void, single Boolean parameter.
        //    Override the incoming value to TRUE so any downstream persistence
        //    writes always record the subscribed state.
        mutableBillingRepo.methods.singleOrNull { method ->
            method.returnType == "V" &&
                method.parameterTypes.map { it.toString() } == listOf("Ljava/lang/Boolean;")
        }?.addInstructions(0, "sget-object p1, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;")
            ?: throw PatchException("Battery Guru: premium state writer not found.")

        // ── SubscriptionPlan model ──────────────────────────────────────────
        // Anchored by "SubscriptionPlan(productId=" in toString output.
        // Play:      j17  (v2.5.0.6)
        // Community: a67  (v2.5.0.5)
        // Constructor shape (stable across versions):
        //   <init>(String, I, Integer, F, <obfuscated-details>, Z, Z, Z)
        //   — exactly 3 boolean (Z) params, 1 float (F), 1 String.
        //   p8 is the third boolean = isPurchased/isSubscribed flag.
        val planClass = classDefByStrings("SubscriptionPlan(productId=").singleOrNull()
            ?: throw PatchException("Battery Guru: subscription plan model not found or ambiguous.")
        mutableClassDefBy(planClass).methods.singleOrNull { method ->
            method.name == "<init>" &&
                method.parameterTypes.map { it.toString() }.let { params ->
                    params.count { it == "Z" } == 3 &&
                    "F" in params &&
                    "Ljava/lang/String;" in params
                }
        }?.addInstructions(0, "const/4 p8, 0x1")
            ?: throw PatchException("Battery Guru: subscription plan constructor not found.")

        // ── Subscribed cache writer ─────────────────────────────────────────
        // Anchored by "last_known_subscribed" — a SharedPreferences boolean flag
        // written in a background observer whenever billing state changes.
        // Play:      d90  (v2.5.0.6)
        // Community: v80  (v2.5.0.5)
        val cacheWriters = mutableListOf<Pair<String, Method>>()
        classDefForEach { classDef ->
            classDef.methods.forEach { method ->
                if (
                    method.returnType == "V" &&
                    method.parameterTypes.map { it.toString() } == listOf("Ljava/lang/Object;") &&
                    "last_known_subscribed" in method.stringLiterals()
                ) {
                    cacheWriters += classDef.type to method
                }
            }
        }
        if (cacheWriters.size != 1) {
            throw PatchException(
                "Battery Guru: expected 1 subscribed cache writer, found ${cacheWriters.size}.",
            )
        }
        mutableClassDefBy(cacheWriters.single().first).methods.singleOrNull { method ->
            method.returnType == "V" &&
                method.parameterTypes.map { it.toString() } == listOf("Ljava/lang/Object;") &&
                "last_known_subscribed" in method.stringLiterals()
        }?.addInstructions(0, "sget-object p1, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;")
            ?: throw PatchException("Battery Guru: subscribed cache writer method not found.")

        // ── PremiumUiState ──────────────────────────────────────────────────
        // Anchored by all four toString fragments — highly stable across versions.
        // Play:      tq5  (v2.5.0.6, 7 booleans in constructor)
        // Community: jt5  (v2.5.0.5, 8 booleans in constructor)
        // Patch: force selectedProductId (p2) and rewardedProductId (p3) to the
        // yearly plan string in both the constructor and the copy method.
        val premiumUiStateTypes = mutableListOf<String>()
        classDefForEach { classDef ->
            val literals = classDef.methods.flatMap { it.stringLiterals() }.toSet()
            if (
                "PremiumUiState(plans=" in literals &&
                ", selectedProductId=" in literals &&
                ", rewardedProductId=" in literals &&
                ", isRewardedTimeOver=" in literals
            ) {
                premiumUiStateTypes += classDef.type
            }
        }
        if (premiumUiStateTypes.size != 1) {
            throw PatchException(
                "Battery Guru: expected 1 premium UI state, found ${premiumUiStateTypes.size}.",
            )
        }

        val premiumUiState = mutableClassDefBy(premiumUiStateTypes.single())

        // p2 = selectedProductId (String), p3 = rewardedProductId (String).
        // These parameter positions are stable: List is always p1, the two
        // product-ID strings are always p2 and p3, regardless of how many
        // booleans follow (7 in v2.5.0.6, 8 in community v2.5.0.5).
        val forceYearlyUiState = """
            const-string p2, "one_year_subscription"
            const-string p3, "one_year_subscription"
        """

        // Constructor: (List, String, String, Z×N, I, I, J) — N differs by version.
        // Match by shape: first param List, at least 6 booleans, last param J.
        premiumUiState.methods.singleOrNull { method ->
            method.name == "<init>" &&
                method.parameterTypes.map { it.toString() }.let { params ->
                    params.firstOrNull() == "Ljava/util/List;" &&
                    params.count { it == "Z" } >= 6 &&
                    params.lastOrNull() == "J"
                }
        }?.addInstructions(0, forceYearlyUiState)
            ?: throw PatchException("Battery Guru: PremiumUiState constructor not found.")

        // Copy method: the only method in PremiumUiState that returns its own type.
        // Inject just before the invoke-direct/range that calls the <init> constructor.
        val uiStateCopy = premiumUiState.methods.singleOrNull { method ->
            method.returnType == premiumUiState.type
        } ?: throw PatchException("Battery Guru: PremiumUiState copy method not found.")

        val uiStateCopyInsertIndex = uiStateCopy.implementation?.instructions
            ?.indexOfFirst { it.opcode == Opcode.INVOKE_DIRECT_RANGE }
            ?.takeIf { it >= 0 }
            ?: throw PatchException("Battery Guru: PremiumUiState copy invoke-direct/range not found.")

        uiStateCopy.addInstructions(uiStateCopyInsertIndex, forceYearlyUiState)
    }
}
