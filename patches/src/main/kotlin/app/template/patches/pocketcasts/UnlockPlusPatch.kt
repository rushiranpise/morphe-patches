package app.template.patches.pocketcasts

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.POCKET_CASTS_COMPATIBILITY
import app.template.patches.shared.clearBody
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

// Verified against Pocket Casts 8.16 (versionCode 9441). See Fingerprints.kt
// for the reasoning behind each match strategy and which identifiers are
// obfuscated.
@Suppress("unused")
val unlockPlusPatch = bytecodePatch(
    name = "Unlock Patron",
    description = "Unlocks Pocket Casts Patron yearly feature checks.",
    default = true,
) {
    compatibleWith(POCKET_CASTS_COMPATIBILITY)

    execute {
        val paidTierType = resolvePaidMembershipTierReturnSmali()
        val platformType = resolveAndroidPlatformType()

        MembershipStatusFingerprint.method.apply {
            clearBody()
            addInstructions(0, paidTierType)
        }

        MembershipHasFeatureFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """.trimIndent(),
            )
        }

        SubscriptionLifetimeFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """.trimIndent(),
            )
        }

        SubscriptionStatusMapperFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    new-instance v0, Lau/com/shiftyjelly/pocketcasts/models/type/Membership;
                    new-instance v1, Lau/com/shiftyjelly/pocketcasts/models/type/Subscription;
                    sget-object v2, Lau/com/shiftyjelly/pocketcasts/payment/SubscriptionTier;->Patron:Lau/com/shiftyjelly/pocketcasts/payment/SubscriptionTier;
                    sget-object v3, Lau/com/shiftyjelly/pocketcasts/payment/BillingCycle;->Yearly:Lau/com/shiftyjelly/pocketcasts/payment/BillingCycle;
                    sget-object v4, $platformType->Android:$platformType
                    const-wide v5, 0x3bb2cc5c000L
                    invoke-static {v5, v6}, Lj${'$'}/time/Instant;->ofEpochMilli(J)Lj${'$'}/time/Instant;
                    move-result-object v5
                    const/4 v6, 0x1
                    const/16 v7, 0x270f
                    const/4 v8, 0x1
                    invoke-direct/range {v1 .. v8}, Lau/com/shiftyjelly/pocketcasts/models/type/Subscription;-><init>(Lau/com/shiftyjelly/pocketcasts/payment/SubscriptionTier;Lau/com/shiftyjelly/pocketcasts/payment/BillingCycle;${platformType}Lj${'$'}/time/Instant;ZIZ)V
                    new-instance v2, Ljava/util/ArrayList;
                    invoke-direct {v2}, Ljava/util/ArrayList;-><init>()V
                    invoke-direct {v0, v1, v5, v2}, Lau/com/shiftyjelly/pocketcasts/models/type/Membership;-><init>(Lau/com/shiftyjelly/pocketcasts/models/type/Subscription;Lj${'$'}/time/Instant;Ljava/util/List;)V
                    return-object v0
                """.trimIndent(),
            )
        }
    }
}

/**
 * Resolves the enum class that currently holds the "Android" subscription
 * platform constant (obfuscated, renamed every build -- see
 * [SubscriptionPlatformEnumFingerprint]) and returns its type descriptor.
 * Every constant on this enum keeps a stable field name, so no ordinal
 * math is needed here.
 */
private fun app.morphe.patcher.patch.BytecodePatchContext.resolveAndroidPlatformType(): String =
    SubscriptionPlatformEnumFingerprint.originalClassDef.type

/**
 * Resolves the enum constant class that currently represents the "Paid"
 * membership status (obfuscated, renamed every build -- see
 * [PaidMembershipTierConstructorFingerprint]) and builds the smali needed to
 * return its singleton instance. Since not every constant on this enum gets
 * its own dedicated static field (only the ones directly referenced by
 * field elsewhere in the app do), the constant is looked up by ordinal via
 * `values()[n]` instead -- the ordinal is read directly from the matched
 * constructor's own `Enum.<init>(String, int)` call rather than hardcoded,
 * so this keeps working even if a future build reorders the enum.
 */
private fun app.morphe.patcher.patch.BytecodePatchContext.resolvePaidMembershipTierReturnSmali(): String {
    val method = PaidMembershipTierConstructorFingerprint.method
    val classDef = PaidMembershipTierConstructorFingerprint.originalClassDef
    val baseType = classDef.superclass
        ?: throw PatchException("Paid membership tier constant class has no superclass.")

    val instructions = method.implementation?.instructions
        ?: throw PatchException("Paid membership tier constructor has no implementation.")

    val enumInitIndex = instructions.indexOfFirst { instruction ->
        instruction.opcode == Opcode.INVOKE_DIRECT &&
            (instruction as? ReferenceInstruction)?.reference?.let { it as? MethodReference }
                ?.let { it.definingClass == "Ljava/lang/Enum;" && it.name == "<init>" } == true
    }
    if (enumInitIndex <= 0) {
        throw PatchException("Could not find Enum.<init> call in Paid membership tier constructor.")
    }

    val ordinal = (instructions[enumInitIndex - 1] as? NarrowLiteralInstruction)?.narrowLiteral
        ?: throw PatchException("Could not read ordinal for Paid membership tier constant.")

    return """
        invoke-static {}, $baseType->values()[$baseType
        move-result-object v0
        const/4 v1, 0x${ordinal.toString(16)}
        aget-object v0, v0, v1
        return-object v0
    """.trimIndent()
}
