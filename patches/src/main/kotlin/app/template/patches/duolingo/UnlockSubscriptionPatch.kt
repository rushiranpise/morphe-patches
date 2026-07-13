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
import app.template.patches.duolingo.LoggedInStateFingerprint
import app.template.patches.duolingo.UserFingerprint
import app.template.patches.duolingo.UserHasGoldFieldUsageFingerprint
import app.template.patches.duolingo.UserIsPaidFieldUsageFingerprint
import app.template.patches.duolingo.UserSubscriptionInfoFingerprint
import app.template.patches.duolingo.VideoCallTabCtaButtonStateToStringFingerprint
import app.template.patches.shared.Constants.DUOLINGO_COMPATIBILITY
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

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
        fun resolvedField(fingerprint: Fingerprint, label: String): FieldReference =
            fingerprint.method.instructions
                .elementAt(fingerprint.instructionMatches.first().index)
                .let { instruction -> (instruction as? ReferenceInstruction)?.reference as? FieldReference }
                ?: throw PatchException("Could not resolve Duolingo $label field")

        fun fieldFromToString(fingerprint: Fingerprint, label: String): FieldReference {
            val stringIndex = fingerprint.method.instructions.indexOfFirst { instruction ->
                instruction.opcode == Opcode.CONST_STRING &&
                    ((instruction as? ReferenceInstruction)?.reference as? StringReference)
                        ?.string
                        ?.contains(label) == true
            }
            if (stringIndex < 0) throw PatchException("Could not find Duolingo $label string")

            return fingerprint.method.instructions
                .drop(stringIndex + 1)
                .firstNotNullOfOrNull { instruction ->
                    (instruction as? ReferenceInstruction)?.reference as? FieldReference
                }
                ?: throw PatchException("Could not resolve Duolingo $label field")
        }

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
                val fields = instructions
                    .mapNotNull { instruction ->
                        (instruction as? ReferenceInstruction)?.reference as? FieldReference
                    }
                    .filter { field -> field.definingClass == "Lcom/duolingo/data/plus/SubscriptionInfo;" }
                    .distinctBy { field -> "${field.name}:${field.type}" }
                    .take(10)

                if (fields.size < 10) {
                    throw PatchException("Could not resolve Duolingo SubscriptionInfo fields")
                }

                val currencyField = fields[0]
                val expiryField = fields[1]
                val trialField = fields[2]
                val periodLengthField = fields[3]
                val periodUnitField = fields[4]
                val productIdField = fields[5]
                val providerField = fields[6]
                val activeField = fields[7]
                val vendorPurchaseIdField = fields[8]
                val expiryMillisField = fields[9]

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
                    iput-object p1, p0, ${currencyField.definingClass}->${currencyField.name}:${currencyField.type}
                    iput-wide p2, p0, ${expiryField.definingClass}->${expiryField.name}:${expiryField.type}
                    iput-boolean p4, p0, ${trialField.definingClass}->${trialField.name}:${trialField.type}
                    iput p5, p0, ${periodLengthField.definingClass}->${periodLengthField.name}:${periodLengthField.type}
                    iput p6, p0, ${periodUnitField.definingClass}->${periodUnitField.name}:${periodUnitField.type}
                    iput-object p7, p0, ${productIdField.definingClass}->${productIdField.name}:${productIdField.type}
                    iput-object p8, p0, ${providerField.definingClass}->${providerField.name}:${providerField.type}
                    iput-boolean p9, p0, ${activeField.definingClass}->${activeField.name}:${activeField.type}
                    iput-object p10, p0, ${vendorPurchaseIdField.definingClass}->${vendorPurchaseIdField.name}:${vendorPurchaseIdField.type}
                    sget-object p1, Ljava/util/concurrent/TimeUnit;->SECONDS:Ljava/util/concurrent/TimeUnit;
                    invoke-virtual {p1, p2, p3}, Ljava/util/concurrent/TimeUnit;->toMillis(J)J
                    move-result-wide p1
                    iput-wide p1, p0, ${expiryMillisField.definingClass}->${expiryMillisField.name}:${expiryMillisField.type}
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
        val userClass = mutableClassDefBy("Lcom/duolingo/data/user/User;")
        val isPaidField = resolvedField(UserIsPaidFieldUsageFingerprint, "isPaid")
        val hasGoldField = resolvedField(UserHasGoldFieldUsageFingerprint, "hasGold")
        val hasPlusField = fieldFromToString(UserFingerprint, "hasPlus")
        val subscriberLevelField = fieldFromToString(UserFingerprint, "subscriberLevel")

        setOf(isPaidField, hasPlusField, hasGoldField, subscriberLevelField).forEach { field ->
            userClass.fields
                .firstOrNull { it.name == field.name }
                ?.apply { accessFlags = accessFlags and AccessFlags.FINAL.value.inv() }
        }

        LoggedInStateFingerprint.classDef.methods
            .first { method -> method.name == "<init>" }
            .apply {
                val returnIndex = instructions.indexOfLast { instruction -> instruction.opcode == Opcode.RETURN_VOID }
                if (returnIndex < 0) throw PatchException("Could not find LoggedIn constructor return")

                addInstructions(
                    returnIndex,
                    """
                    const/4 v0, 0x1
                    iput-boolean v0, p1, ${isPaidField.definingClass}->${isPaidField.name}:${isPaidField.type}
                    iput-boolean v0, p1, ${hasPlusField.definingClass}->${hasPlusField.name}:${hasPlusField.type}
                    const/4 v0, ${if (tier.startsWith("max")) "0x1" else "0x0"}
                    iput-boolean v0, p1, ${hasGoldField.definingClass}->${hasGoldField.name}:${hasGoldField.type}
                    sget-object v0, ${subscriberLevelField.type}->$subscriberLevel:${subscriberLevelField.type}
                    iput-object v0, p1, ${subscriberLevelField.definingClass}->${subscriberLevelField.name}:${subscriberLevelField.type}
                    """.trimIndent(),
                )
        }

        if (tier.startsWith("max")) {
            val features = setOf(
                "VIDEO_CALL_IN_PATH",
                "VIDEO_CALL_IN_PRACTICE_HUB",
                "EXPLAIN_MY_ANSWER",
                "ROLEPLAY_FOR_INTERMEDIATE_LEARNERS",
            )
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

            VideoCallTabCtaButtonStateToStringFingerprint.method.apply {
                val upsellField = VideoCallTabCtaButtonStateToStringFingerprint
                    .instructionMatches
                    .last()
                    .index
                    .let { index -> instructions.elementAt(index) as? ReferenceInstruction }
                    ?.reference as? FieldReference
                    ?: throw PatchException("Could not resolve Duolingo Max upsell field")

                val constructor = mutableClassDefBy(upsellField.definingClass)
                    .methods
                    .firstOrNull { method -> method.name == "<init>" }
                    ?: throw PatchException("Could not find Duolingo Max upsell constructor")
                val setFieldIndex = constructor.instructions.indexOfFirst { instruction ->
                    (instruction as? ReferenceInstruction)?.reference?.toString() == upsellField.toString()
                }
                if (setFieldIndex < 0) {
                    throw PatchException("Could not find Duolingo Max upsell field assignment")
                }
                val register = constructor.instructions
                    .elementAt(setFieldIndex)
                    .let { instruction -> (instruction as? TwoRegisterInstruction)?.registerA }
                    ?: throw PatchException("Could not resolve Duolingo Max upsell register")

                constructor.addInstructions(setFieldIndex, "const/4 v$register, 0x0")
            }
        }
    }
}
