package app.template.patches.batteryguru.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BATTERYGURU_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Suppress("unused")
val batteryGuruUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium and marks the yearly plan as purchased.",
    default = true,
) {
    compatibleWith(BATTERYGURU_COMPATIBILITY)

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

        fun Method.instructionIndexOf(pattern: String): Int =
            implementation?.instructions?.indexOfFirst { instruction ->
                instruction.toString().contains(pattern)
            } ?: -1

        val billingRepo = classDefByStrings("last_product_id").singleOrNull()
            ?: throw PatchException("Battery Guru: billing repository not found or ambiguous.")
        val billingStrings = billingRepo.methods.flatMap { it.stringLiterals() }.toSet()
        if ("video_time" !in billingStrings || "rewarded_ad_count" !in billingStrings) {
            throw PatchException("Battery Guru: billing repository key check failed.")
        }

        val mutableBillingRepo = mutableClassDefBy(billingRepo)

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

        mutableBillingRepo.methods.singleOrNull { method ->
            method.returnType == "Ljava/lang/Object;" &&
                method.parameterTypes.size == 1 &&
                "last_product_id" in method.stringLiterals()
        }?.addInstructions(0, "const-string v0, \"one_year_subscription\"\nreturn-object v0")
            ?: throw PatchException("Battery Guru: selected product reader not found.")

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

        mutableBillingRepo.methods.singleOrNull { method ->
            method.returnType == "V" &&
                method.parameterTypes.map { it.toString() } == listOf("Ljava/lang/Boolean;")
        }?.addInstructions(0, "sget-object p1, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;")
            ?: throw PatchException("Battery Guru: premium state writer not found.")

        val planClass = classDefByStrings("SubscriptionPlan(productId=").singleOrNull()
            ?: throw PatchException("Battery Guru: subscription plan model not found or ambiguous.")
        val mutablePlanClass = mutableClassDefBy(planClass)
        // Constructor: (String, I, Integer, F, <obfuscated-details-class>, Z, Z, Z)
        // Avoid matching the obfuscated Ljw1; type directly — it changes each release.
        // Instead match by shape: <init> with exactly 3 boolean (Z) params and 1 float (F).
        // The synthetic copy constructor has 0 Z params so this uniquely selects the real one.
        mutablePlanClass.methods.singleOrNull { method ->
            method.name == "<init>" &&
                method.parameterTypes.map { it.toString() }.let { params ->
                    params.count { it == "Z" } == 3 &&
                    "F" in params &&
                    "Ljava/lang/String;" in params
                }
        }?.addInstructions(
            0,
            """
                const/4 p8, 0x1
            """,
        ) ?: throw PatchException("Battery Guru: subscription plan constructor not found.")

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
            throw PatchException("Battery Guru: expected 1 subscribed cache writer, found ${cacheWriters.size}.")
        }
        mutableClassDefBy(cacheWriters.single().first).methods.singleOrNull { method ->
            method.returnType == "V" &&
                method.parameterTypes.map { it.toString() } == listOf("Ljava/lang/Object;") &&
                "last_known_subscribed" in method.stringLiterals()
        }?.addInstructions(0, "sget-object p1, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;")
            ?: throw PatchException("Battery Guru: subscribed cache writer method not found.")

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
            throw PatchException("Battery Guru: expected 1 premium UI state, found ${premiumUiStateTypes.size}.")
        }

        val premiumUiState = mutableClassDefBy(premiumUiStateTypes.single())
        // Force selectedProductId and rewardedProductId to the yearly plan.
        // Note: in v2.5.0.5 the isSubscribed boolean was removed from PremiumUiState.
        // p4 is now isBillingConnected — do NOT force it true (triggers billing flows).
        // The rewarded-ad expiry timestamp (was p14) is still present at p14/p15 (J).
        // We do not force it because the rewarded ad path is unreachable when the
        // subscription gate (el5.e()Z) and active premium reader (el5.o()) are patched.
        val forceYearlyUiState = """
            const-string p2, "one_year_subscription"
            const-string p3, "one_year_subscription"
        """
        // Constructor: (List, String, String, Z*8, I, I, J)
        // Match by shape to avoid brittle full param list — the number of booleans
        // may shift if new fields are added but the anchors (List, String, String, J) are stable.
        premiumUiState.methods.singleOrNull { method ->
            method.name == "<init>" &&
                method.parameterTypes.map { it.toString() }.let { params ->
                    params.firstOrNull() == "Ljava/util/List;" &&
                    params.count { it == "Z" } >= 6 &&
                    params.lastOrNull() == "J"
                }
        }?.addInstructions(0, forceYearlyUiState)
            ?: throw PatchException("Battery Guru: PremiumUiState constructor not found.")

        // The copy method is the only method in PremiumUiState that returns its own type.
        // It calls the <init> constructor via invoke-direct/range to construct the copy.
        // We inject forceYearlyUiState just before that invoke-direct/range call.
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
