package app.template.patches.torrdroid

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TORRDROID_COMPATIBILITY

@Suppress("unused")
val torrDroidRemoveAdsPatch = bytecodePatch(
    name = "Remove Ads",
    description = "Removes ads from TorrDroid.",
    default = true
) {
    compatibleWith(TORRDROID_COMPATIBILITY)

    execute {
        // Patch 1: L4/b.<init>(MainActivity)V → always set c:Z = true
        // Instead of reading SharedPrefs("adPreferencesFile").getBoolean("ad_free"),
        // always write true. Chain: c:Z → V9/m.d:Z → MainActivity.j:Z
        runCatching {
            adFreeInitFingerprint
                .match(classDefBy(adFreeInitFingerprint.definingClass!!))
                .method
        }.getOrNull()?.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.size)
            addInstructions(
                0,
                """
                const/4 v0, 0x3
                iput v0, p0, LL4/b;->a:I
                invoke-direct {p0}, Ljava/lang/Object;-><init>()V
                iput-object p1, p0, LL4/b;->b:Ljava/lang/Object;
                const/4 v0, 0x1
                iput-boolean v0, p0, LL4/b;->c:Z
                return-void
                """.trimIndent()
            )
        }

        // Patch 2: V9/m.b(Purchase)V → NOP the purchase processor
        // This prevents any server-side purchase verification that could set d:Z=false
        // if the purchase is detected as invalid. The init patch (Patch 1) already sets
        // the flag to true on startup; this just prevents it being reset.
        runCatching {
            purchaseProcessorFingerprint
                .match(classDefBy(purchaseProcessorFingerprint.definingClass!!))
                .method
        }.getOrNull()?.apply {
            if (implementation == null) return@apply
            // Find where d:Z is set to v3 (0=false on refund) and skip that path
            // by setting d:Z=true at the two iput-boolean lines (776 and 65-equivalent)
            val instrList = instructions
            instrList.forEachIndexed { index, instr ->
                val str = instr.toString()
                // Replace the iput-boolean that writes v3 (could be 0/false) to d:Z
                if (str.contains("iput-boolean") && str.contains("LV9/m;->d:Z")) {
                    removeInstructions(index, 1)
                    addInstructions(
                        index,
                        """
                        const/4 v3, 0x1
                        iput-boolean v3, p0, LV9/m;->d:Z
                        """.trimIndent()
                    )
                }
            }
        }
    }
}
