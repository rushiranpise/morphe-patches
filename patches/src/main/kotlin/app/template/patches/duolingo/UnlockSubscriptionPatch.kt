package app.template.patches.duolingo

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import app.template.patches.duolingo.HasMaxUserInfoConstructorFingerprint
import app.template.patches.duolingo.UserSubscriptionInfoFingerprint
import app.template.patches.shared.Constants.DUOLINGO_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private fun duolingoSubscriptionFeatureFingerprint(feature: String) = Fingerprint(
    filters = listOf(
        fieldAccess(
            name = feature,
            definingClass = "Lcom/duolingo/core/subscription/models/SubscriptionFeatures;",
        ),
        methodCall(
            name = "contains",
            location = InstructionLocation.MatchAfterImmediately(),
        ),
        opcode(Opcode.MOVE_RESULT, InstructionLocation.MatchAfterImmediately()),
    ),
)

@Suppress("unused")
val duolingoUnlockSubscriptionPatch = bytecodePatch(
    name = "Unlock Subscription",
    description = "Unlocks user-selectable Duolingo subscription tiers.",
    default = true,
) {
    compatibleWith(DUOLINGO_COMPATIBILITY)

    val subscriptionTier by stringOption(
        key = "subscriptionTier",
        default = "max",
        values = mapOf(
            "Max" to "max",
            "Max Family" to "max_family",
            "Max Immersive" to "max_immersive",
            "Max Immersive Family" to "max_immersive_family",
            "Super" to "super",
            "Super Family" to "super_family",
            "Super Immersive" to "super_immersive",
            "Super Immersive Family" to "super_immersive_family",
            "Lite" to "lite",
        ),
        title = "Subscription tier",
        description = "Choose Super, Max, Lite, or Immersive metadata.",
    )

    execute {
        val tier = subscriptionTier ?: "max"
        val productId = when (tier) {
            "max_family" -> "gold_subscription_fam_twelve_month"
            "max_immersive" -> "immersive_gold_subscription"
            "max_immersive_family" -> "immersive_gold_family_subscription"
            "super" -> "premium_subscription_twelve_month"
            "super_family" -> "premium_subscription_fam_twelve_month"
            "super_immersive" -> "immersive_subscription"
            "super_immersive_family" -> "immersive_family_subscription"
            "lite" -> "lite_subscription"
            else -> "gold_subscription"
        }
        val vendorPurchaseId = "morphe_$tier"
        val periodLength = 12

        mutableClassDefBy("Lcom/duolingo/data/plus/SubscriptionInfo;")
            .methods
            .first { method ->
                method.name == "<init>" &&
                    method.returnType == "V" &&
                    method.parameterTypes == listOf(
                        "Ljava/lang/String;",
                        "J",
                        "Z",
                        "I",
                        "I",
                        "Ljava/lang/String;",
                        "Ljava/lang/String;",
                        "Z",
                        "Ljava/lang/String;",
                    )
            }.apply {
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
                    const-string p1, "USD"
                    const-wide/32 p2, 0x7fffffff
                    const/4 p4, 0x0
                    const/16 p5, $periodLength
                    const/4 p6, 0x1
                    const-string p7, "$productId"
                    const-string p8, "GOOGLE_PLAY"
                    const/4 p9, 0x1
                    const-string p10, "$vendorPurchaseId"
                    iput-object p1, p0, Lcom/duolingo/data/plus/SubscriptionInfo;->a:Ljava/lang/String;
                    iput-wide p2, p0, Lcom/duolingo/data/plus/SubscriptionInfo;->b:J
                    iput-boolean p4, p0, Lcom/duolingo/data/plus/SubscriptionInfo;->c:Z
                    iput p5, p0, Lcom/duolingo/data/plus/SubscriptionInfo;->d:I
                    iput p6, p0, Lcom/duolingo/data/plus/SubscriptionInfo;->e:I
                    iput-object p7, p0, Lcom/duolingo/data/plus/SubscriptionInfo;->f:Ljava/lang/String;
                    iput-object p8, p0, Lcom/duolingo/data/plus/SubscriptionInfo;->g:Ljava/lang/String;
                    iput-boolean p9, p0, Lcom/duolingo/data/plus/SubscriptionInfo;->h:Z
                    iput-object p10, p0, Lcom/duolingo/data/plus/SubscriptionInfo;->i:Ljava/lang/String;
                    sget-object p1, Ljava/util/concurrent/TimeUnit;->SECONDS:Ljava/util/concurrent/TimeUnit;
                    invoke-virtual {p1, p2, p3}, Ljava/util/concurrent/TimeUnit;->toMillis(J)J
                    move-result-wide p1
                    iput-wide p1, p0, Lcom/duolingo/data/plus/SubscriptionInfo;->j:J
                    return-void
                    """.trimIndent(),
                )
            }

        UserSubscriptionInfoFingerprint
            .match(mutableClassDefBy(UserSubscriptionInfoFingerprint.definingClass!!))
            .method.apply {
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    sget-object v0, Lcom/duolingo/data/plus/SubscriptionInfo;->k:Lcom/duolingo/data/plus/SubscriptionInfo;
                    return-object v0
                    """.trimIndent(),
                )
            }

        if (tier.startsWith("max")) {
            HasMaxUserInfoConstructorFingerprint
                .match(mutableClassDefBy(HasMaxUserInfoConstructorFingerprint.definingClass!!))
                .method.addInstructions(0, "const/4 p1, 0x1")
        }

        val subscriberLevel = when {
            tier.startsWith("max") -> "GOLD"
            tier == "lite" -> "LITE"
            else -> "PREMIUM"
        }
        val userBooleanFields = mapOf(
            "Lcom/duolingo/data/user/User;->Q0:Z" to true,
            "Lcom/duolingo/data/user/User;->y:Z" to (tier != "lite"),
            "Lcom/duolingo/data/user/User;->P0:Z" to tier.startsWith("max"),
        )
        var patchedUserFields = 0
        mutableClassDefBy("Lcom/duolingo/data/user/User;").methods
            .filter { method -> method.name == "<init>" }
            .forEach { method ->
                method.implementation?.instructions
                    ?.mapIndexedNotNull { index, instruction ->
                        val reference = (instruction as? ReferenceInstruction)?.reference?.toString()
                        val registers = instruction as? TwoRegisterInstruction
                        when {
                            instruction.opcode == Opcode.IPUT_BOOLEAN && reference != null && reference in userBooleanFields && registers != null -> {
                                val value = if (userBooleanFields.getValue(reference)) "0x1" else "0x0"
                                index to "const/16 v${registers.registerA}, $value"
                            }
                            instruction.opcode == Opcode.IPUT_OBJECT &&
                                reference == "Lcom/duolingo/data/user/User;->A0:Lcom/duolingo/data/user/SubscriberLevel;" &&
                                registers != null ->
                                index to "sget-object v${registers.registerA}, Lcom/duolingo/data/user/SubscriberLevel;->$subscriberLevel:Lcom/duolingo/data/user/SubscriberLevel;"
                            else -> null
                        }
                    }
                    ?.asReversed()
                    ?.forEach { (index, instruction) ->
                        method.addInstructions(index, instruction)
                        patchedUserFields++
                    }
            }

        if (patchedUserFields == 0) {
            throw PatchException("Could not patch User subscription fields")
        }

        if (tier.startsWith("max")) {
            val features = setOf("VIDEO_CALL_IN_PATH", "VIDEO_CALL_IN_PRACTICE_HUB")
            var patchedFeatures = 0
            features.forEach { feature ->
                duolingoSubscriptionFeatureFingerprint(feature).matchAll().forEach { match ->
                    val moveResultIndex = match.instructionMatches.last().index
                    val register = match.method.instructions
                        .elementAt(moveResultIndex) as OneRegisterInstruction
                    match.method.addInstructions(moveResultIndex + 1, "const/4 v${register.registerA}, 0x1")
                    patchedFeatures++
                }
            }

            if (patchedFeatures == 0) {
                throw PatchException("Could not find Max feature checks")
            }
        }
    }
}
