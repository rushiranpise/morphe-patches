package app.template.patches.udisc

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.template.patches.shared.Constants.UDISC_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

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

private fun app.morphe.patcher.patch.BytecodePatchContext.patchUserAccountProGates() {
    val userAccountClass = UserAccountClassFingerprint.classDef

    val isProMethod = userAccountClass.methods.firstOrNull { method ->
        method.returnType == "Z" &&
            method.parameterTypes.isEmpty() &&
            method.implementation?.instructions?.any { instruction ->
                instruction.opcode == Opcode.IF_LEZ || instruction.opcode == Opcode.IF_GTZ
            } == true
    } ?: throw PatchException("UDisc account pro gate not found.")

    val isTrialMethod = userAccountClass.methods.firstOrNull { method ->
        method.returnType == "Z" &&
            method.parameterTypes.isEmpty() &&
            method.implementation?.instructions?.any { instruction ->
                instruction.opcode == Opcode.SGET_OBJECT
            } == true
    } ?: throw PatchException("UDisc account trial gate not found.")

    isProMethod.returnBooleanEarly(true)
    isTrialMethod.returnBooleanEarly(false)
}

private fun app.morphe.patcher.patch.BytecodePatchContext.patchWatchAccountProGate() {
    val watchIsProIndex = WatchAccountProFingerprint.instructionMatches.first().index
    val watchIsProRegister =
        (WatchAccountProFingerprint.method.instructions[watchIsProIndex] as TwoRegisterInstruction).registerA
    WatchAccountProFingerprint.method.addInstructions(
        watchIsProIndex,
        "const/4 v$watchIsProRegister, 0x1",
    )
}

private fun MutableMethod.returnBooleanEarly(value: Boolean) = addInstructions(
    0,
    """
        const/4 v0, ${if (value) "0x1" else "0x0"}
        return v0
    """.trimIndent(),
)
