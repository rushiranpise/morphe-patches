package app.template.patches.udisc

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.template.patches.shared.Constants.UDISC_COMPATIBILITY
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

// Verified against UDisc 24.2.6 (versionCode 9943). See Fingerprints.kt for the
// reasoning behind each match strategy and which identifiers are obfuscated.
@Suppress("unused")
val uDiscUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks UDisc Pro subscription.",
    default = true,
) {
    compatibleWith(UDISC_COMPATIBILITY)

    extendWith("extensions/extension.mpe")

    execute {
        UDiscApplicationOnCreateFingerprint.method.addInstructions(
            0,
            "invoke-static {}, Lapp/template/extension/extension/UDiscHelper;->init()V",
        )

        // Force every newly-constructed Account.Subscription to report an active,
        // far-future paid subscription (platform ordinal 1, status SUBSCRIBED).
        AccountSubscriptionConstructorFingerprint.method.addInstructions(
            0,
            """
                const/4 p1, 0x7
                invoke-static {}, Lcom/udisc/kmp/account/Account${'$'}Subscription${'$'}Platform;->values()[Lcom/udisc/kmp/account/Account${'$'}Subscription${'$'}Platform;
                move-result-object p2
                const/4 v0, 0x1
                aget-object p2, p2, v0
                invoke-static {}, Lcom/udisc/kmp/account/Account${'$'}Subscription${'$'}Status;->values()[Lcom/udisc/kmp/account/Account${'$'}Subscription${'$'}Status;
                move-result-object p3
                aget-object p3, p3, v0
                const-string p4, "2099-12-31T00:00:00Z"
            """.trimIndent(),
        )

        patchUserAccountProGates()
        patchWatchAccountProGate()

        // Auto-acknowledge every locally-tracked pending purchase instead of the
        // stock purchase-verification flow. See Fingerprints.kt for why this
        // can't match on the listener's parameter type.
        PlayBillingPurchaseListenerFingerprint.method.addInstructions(
            0,
            """
                iget-object v0, p0, Lcom/udisc/android/billing/b;->a:Loo/c;
                iget-object v0, v0, Loo/c;->d:Ljava/util/LinkedHashSet;
                invoke-interface {v0}, Ljava/lang/Iterable;->iterator()Ljava/util/Iterator;
                move-result-object v0
                :udisc_loop
                invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z
                move-result v1
                if-eqz v1, :udisc_done
                invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;
                move-result-object v1
                check-cast v1, Loo/a;
                const/4 v2, 0x1
                invoke-interface {v1, v2}, Loo/a;->f(Z)V
                goto :udisc_loop
                :udisc_done
                return-void
            """.trimIndent(),
        )
    }
}

private fun BytecodePatchContext.patchUserAccountProGates() {
    requireSingleMatch(AccountHasEntitlementFingerprint, "UDisc account entitlement gate")
        .method
        .returnBooleanEarly(true)

    requireSingleMatch(AccountIsTrialingFingerprint, "UDisc account trialing gate")
        .method
        .returnBooleanEarly(false)
}

private fun BytecodePatchContext.patchWatchAccountProGate() {
    val watchIsProIndex = WatchAccountProFingerprint.instructionMatches.first().index
    val watchIsProRegister =
        (WatchAccountProFingerprint.method.instructions[watchIsProIndex] as TwoRegisterInstruction).registerA
    WatchAccountProFingerprint.method.addInstructions(
        watchIsProIndex,
        "const/4 v$watchIsProRegister, 0x1",
    )
}

/**
 * Resolves [fingerprint] against the current build and fails loudly if it matches
 * zero or more than one method, instead of silently patching the wrong target.
 * Both [AccountHasEntitlementFingerprint] and [AccountIsTrialingFingerprint] are
 * structural (opcode-shape / referenced-type) matches rather than name-based ones,
 * since neither underlying method keeps a stable name across app updates -- see
 * Fingerprints.kt for the full reasoning.
 */
private fun BytecodePatchContext.requireSingleMatch(
    fingerprint: app.morphe.patcher.Fingerprint,
    description: String,
): app.morphe.patcher.Match {
    val matches = fingerprint.matchAllOrNull() ?: emptyList()
    return when (matches.size) {
        0 -> throw PatchException("$description not found.")
        1 -> matches.single()
        else -> throw PatchException(
            "$description matched ${matches.size} methods -- expected exactly 1. " +
                "The structural heuristic in Fingerprints.kt is no longer unique for this app version.",
        )
    }
}

private fun MutableMethod.returnBooleanEarly(value: Boolean) = addInstructions(
    0,
    """
        const/4 v0, ${if (value) "0x1" else "0x0"}
        return v0
    """.trimIndent(),
)
