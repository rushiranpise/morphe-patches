package app.template.patches.toxly

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TOXLY_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode

@Suppress("unused")
val toxlyUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Toxly Premium",
    default = true,
) {
    compatibleWith(TOXLY_COMPATIBILITY)

    execute {
        PairIpCheckLicenseFingerprint.method.addInstructions(0, "return-void")

        BillingRepositoryConstructorFingerprint.method.apply {
            val returnIndex = instructions.indexOfLast { it.opcode == Opcode.RETURN_VOID }
            if (returnIndex < 0) throw PatchException("Toxly billing constructor return not found.")

            addInstructions(
                returnIndex,
                """
                    iget-object v0, p0, Lg60;->e:Lc16;
                    const/4 v1, 0x0
                    sget-object v2, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                    invoke-virtual {v0, v1, v2}, Lc16;->j(Ljava/lang/Object;Ljava/lang/Object;)Z
                    iget-object v0, p0, Lg60;->a:Landroid/content/Context;
                    const/4 v1, 0x1
                    invoke-static {v0, v1}, Lp4;->b(Landroid/content/Context;Z)V
                """.trimIndent(),
            )
        }

        BillingRepositoryRefreshPurchasesFingerprint.method.addInstructions(
            0,
            """
                iget-object v0, p0, Lg60;->e:Lc16;
                const/4 v1, 0x0
                sget-object v2, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                invoke-virtual {v0, v1, v2}, Lc16;->j(Ljava/lang/Object;Ljava/lang/Object;)Z
                iget-object v0, p0, Lg60;->a:Landroid/content/Context;
                const/4 v1, 0x1
                invoke-static {v0, v1}, Lp4;->b(Landroid/content/Context;Z)V
                return-void
            """.trimIndent(),
        )
    }
}
