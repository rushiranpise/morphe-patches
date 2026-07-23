package app.template.patches.batteryguru.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BATTERYGURU_COMPATIBILITY
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
                method.parameterTypes == listOf("Ljava/lang/Boolean;")
        }?.addInstructions(0, "sget-object p1, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;")
            ?: throw PatchException("Battery Guru: premium state writer not found.")

        val planClass = classDefByStrings("SubscriptionPlan(productId=").singleOrNull()
            ?: throw PatchException("Battery Guru: subscription plan model not found or ambiguous.")
        val mutablePlanClass = mutableClassDefBy(planClass)
        mutablePlanClass.methods.singleOrNull { method ->
            method.name == "<init>" &&
                method.parameterTypes == listOf(
                    "Ljava/lang/String;",
                    "I",
                    "Ljava/lang/Integer;",
                    "F",
                    "Ljw1;",
                    "Z",
                    "Z",
                    "Z",
                )
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
                    method.parameterTypes == listOf("Ljava/lang/Object;") &&
                    "last_known_subscribed" in method.stringLiterals()
                ) {
                    cacheWriters += classDef.type to method
                }
            }
        }
        if (cacheWriters.size != 1) {
            throw PatchException("Battery Guru: expected 1 subscribed cache writer, found ${cacheWriters.size}.")
        }
        mutableClassDefBy(cacheWriters.single().first).methods.single { method ->
            method.returnType == "V" &&
                method.parameterTypes == listOf("Ljava/lang/Object;") &&
                "last_known_subscribed" in method.stringLiterals()
        }.addInstructions(0, "sget-object p1, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;")

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
        val forceYearlyUiState = """
            const-string p2, "one_year_subscription"
            const-string p3, "one_year_subscription"
            const/4 p4, 0x1
            const/4 p5, 0x1
            const-wide p14, 0x1d9dbbb8a00L
        """
        premiumUiState.methods.single { method ->
            method.name == "<init>" &&
                method.parameterTypes == listOf(
                    "Ljava/util/List;",
                    "Ljava/lang/String;",
                    "Ljava/lang/String;",
                    "Z",
                    "Z",
                    "Z",
                    "Z",
                    "Z",
                    "Z",
                    "Z",
                    "Z",
                    "I",
                    "I",
                    "J",
                )
        }.addInstructions(0, forceYearlyUiState)

        val uiStateCopy = premiumUiState.methods.single { method ->
            method.returnType == premiumUiState.type &&
                method.parameterTypes.lastOrNull() == "I" &&
                method.instructionIndexOf("L${premiumUiState.type.removePrefix("L")}-><init>") >= 0
        }
        val uiStateCopyInsertIndex = uiStateCopy.instructionIndexOf("L${premiumUiState.type.removePrefix("L")}-><init>")
        if (uiStateCopyInsertIndex < 0) {
            throw PatchException("Battery Guru: premium UI state copy insert point not found.")
        }
        uiStateCopy.addInstructions(uiStateCopyInsertIndex, forceYearlyUiState)
    }
}
