package app.template.patches.andropods.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.ANDROPODS_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode

// AndroPods uses Google Play Billing (product ID "pro") with a volatile boolean m0:Z
// on the PreferencesFragment (a2.l) as the runtime premium gate.
//
// TWO-POINT PATCH:
//
// Point 1 — Constructor <init>()V (.registers 3 → locals v0, v1):
//   m0 defaults to false. e0() fires synchronously during fragment creation before
//   queryPurchasesAsync() returns → launch 1 always shows free UI without this patch.
//   We inject m0=true before return-void. v0 last held a LK1/a reference but const/4
//   safely overwrites its type slot to Integer — ART verifier allows this.
//
// Point 2 — Purchase result handler Y(List<Purchase>)V (.registers 7 → locals v0-v4):
//   Called when billing responds. Sets m0=true when "pro" is found in the purchase list.
//   IMPORTANT: must use v0 (local), NOT p1 (the List<> reference parameter).
//   Previous crash: "tried to get class from non-reference register v6 (type=BooleanConstant)"
//   was caused by using p1 (reference type) as a boolean scratch — ART's verifier rejected
//   the method because later instructions still expect p1 to be a List reference.
//   Fix: use v0 (uninitialized local at offset 0, safe for const/4 → iput-boolean).
@Suppress("unused")
val androPodsPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks AndroPods Pro: voice call integration, assistant control, " +
        "ear detection auto-pause/resume, and all premium preferences.",
    default = true,
) {
    compatibleWith(ANDROPODS_COMPATIBILITY)

    execute {
        // Point 1: Set m0=true in constructor before return-void.
        // v0 is safe here: const/4 overwrites the prior LK1/a reference type in v0's slot.
        AndroPodsPremiumInitFingerprint.method.apply {
            val returnIndex = instructions.indexOfLast { it.opcode == Opcode.RETURN_VOID }
            if (returnIndex < 0) throw PatchException("AndroPods: constructor return-void not found.")
            addInstructions(
                returnIndex,
                """
                    const/4 v0, 0x1
                    iput-boolean v0, p0, La2/l;->m0:Z
                """.trimIndent(),
            )
        }

        // Point 2: Set m0=true at start of billing result handler.
        // Use v0 (local) — safe because const/4 initializes it to Integer type.
        // DO NOT use p1: it is declared as Ljava/util/List; and ART will reject a
        // BooleanConstant being stored in a reference-typed register.
        AndroPodsPurchaseResultFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                iput-boolean v0, p0, La2/l;->m0:Z
            """.trimIndent(),
        )
    }
}
