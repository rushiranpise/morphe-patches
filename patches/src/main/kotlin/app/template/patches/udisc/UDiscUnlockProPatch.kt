package app.template.patches.udisc

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.template.patches.shared.Constants.UDISC_COMPATIBILITY
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

// Verified against UDisc 24.2.6 (versionCode 9943) and 24.2.8 (versionCode 20235).
// See Fingerprints.kt for per-fingerprint stability reasoning.
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

        // Force every newly-constructed Subscription to be an active, far-future
        // Google Play paid subscription.
        //
        // The Platform (GooglePlayStore) and Status (Subscribed) concrete types are
        // resolved at patch-execute time from their own fingerprints rather than being
        // hardcoded. Both types are obfuscated by R8 and their short class names drift
        // on every build (e.g. Lyy/x1 in 24.2.8, something else in 24.2.9).
        // Resolving via SubscriptionClassFingerprint / GooglePlayStorePlatformFingerprint /
        // SubscribedStatusFingerprint (which anchor on stable serialisation strings)
        // means the inject smali never needs to be updated regardless of rename.
        patchSubscriptionConstructor()

        patchUserAccountProGates()
        patchWatchAccountProGate()

        // Auto-acknowledge every locally-tracked pending purchase instead of the
        // stock purchase-verification flow.
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

/**
 * Injects into the Subscription synthetic constructor so every new Subscription
 * instance has platform=GooglePlayStore and status=Subscribed, with a far-future
 * expiry date.
 *
 * Platform and Status concrete type descriptors are resolved from their own
 * fingerprints (anchored on stable serialisation strings) so the injected smali
 * never contains hardcoded obfuscated class names.
 */
private fun BytecodePatchContext.patchSubscriptionConstructor() {
    // Resolve the obfuscated concrete type names at patch time.
    val platformType = GooglePlayStorePlatformFingerprint.originalClassDef.type
    val statusType   = SubscribedStatusFingerprint.originalClassDef.type

    // Resolve the single static-final singleton field name in each class.
    val platformField = GooglePlayStorePlatformFingerprint.originalClassDef
        .fields.first { it.accessFlags.and(0x18) == 0x18 } // PUBLIC | STATIC
        .name
    val statusField = SubscribedStatusFingerprint.originalClassDef
        .fields.first { it.accessFlags.and(0x18) == 0x18 }
        .name

    AccountSubscriptionConstructorFingerprint.method.addInstructions(
        0,
        """
            const/4 p1, 0x7
            sget-object p2, $platformType->$platformField:$platformType
            sget-object p3, $statusType->$statusField:$statusType
            const-string p4, "2099-12-31T00:00:00Z"
        """.trimIndent(),
    )
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
 * Resolves [fingerprint] and fails loudly if it matches zero or more than one
 * method, instead of silently patching the wrong target.
 */
private fun BytecodePatchContext.requireSingleMatch(
    fingerprint: app.morphe.patcher.Fingerprint,
    description: String,
): app.morphe.patcher.Match {
    val matches = fingerprint.matchAllOrNull() ?: emptyList()
    return when (matches.size) {
        0    -> throw PatchException("$description not found.")
        1    -> matches.single()
        else -> throw PatchException(
            "$description matched ${matches.size} methods — expected exactly 1. " +
                "The structural heuristic in Fingerprints.kt is no longer unique for this build.",
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
