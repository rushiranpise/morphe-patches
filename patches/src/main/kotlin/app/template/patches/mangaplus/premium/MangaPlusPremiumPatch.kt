package app.template.patches.mangaplus.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MANGA_PLUS_COMPATIBILITY
import app.template.patches.shared.clearBody
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

/**
 * MANGA Plus Premium Patch
 *
 * Patches the planType deserializer (xa6.e in classes3) to always return the
 * DELUXE enum value, regardless of what the server sends.
 *
 * Architecture:
 * - Subscription is SERVER-SIDE validated; Shueisha's backend controls what
 *   planType string it sends in TitleDetailData and ViewerData API responses.
 * - xa6.e(String)Lrb7; converts that string to the SubscriptionPlan enum
 *   (BASIC / STANDARD / DELUXE) used throughout the UI for lock icons and
 *   chapter navigation gates.
 * - Patching it to always return DELUXE removes all lock UI client-side.
 * - Whether the server returns actual chapter page image URLs for locked
 *   chapters on a non-subscriber account is determined server-side and
 *   CANNOT be bypassed client-side.
 *
 * Smali (classes3/xa6.smali):
 *   .method public static e(Ljava/lang/String;)Lrb7;
 *     const-string v0, "deluxe"
 *     invoke-static { p0, v0 }, Lwv3;->h(...)Z
 *     move-result v0
 *     if-eqz v0, :L0
 *     sget-object p0, Lrb7;->e:Lrb7;   ← DELUXE (first sget-object)
 *     return-object p0
 *   :L0
 *     const-string v0, "standard"
 *     ...
 *     sget-object p0, Lrb7;->d:Lrb7;   ← STANDARD
 *     return-object p0
 *   :L1
 *     sget-object p0, Lrb7;->c:Lrb7;   ← BASIC
 *     return-object p0
 */
@Suppress("unused")
val mangaPlusPremiumPatch = bytecodePatch(
    name = "Manga Plus Premium",
    description = "Unlocks the Deluxe subscription.",
    default = true
) {
    compatibleWith(MANGA_PLUS_COMPATIBILITY)

    execute {
        val method = SubscriptionPlanDeserializerFingerprint.method
        val returnType = method.returnType  // Lrb7; — obfuscated SubscriptionPlan enum

        // Find the first SGET_OBJECT whose FieldReference type == returnType.
        // In xa6.e(), the DELUXE branch comes first (matched against "deluxe" string),
        // so the first SGET_OBJECT returning Lrb7; is the DELUXE constant field.
        val deluxeFieldRef = method.implementation!!.instructions
            .firstOrNull { insn ->
                insn.opcode == Opcode.SGET_OBJECT &&
                    ((insn as? ReferenceInstruction)?.reference as? FieldReference)
                        ?.type == returnType
            }
            ?.let { insn ->
                (insn as ReferenceInstruction).reference as FieldReference
            }
            ?: throw Exception(
                "MangaPlus: Could not locate DELUXE SGET_OBJECT (type=$returnType) " +
                    "in planType deserializer. Re-verify smali after version update."
            )

        // Build the field descriptor: Lclass;->field:Ltype;
        val fieldDescriptor = "${deluxeFieldRef.definingClass}->${deluxeFieldRef.name}:${deluxeFieldRef.type}"

        method.clearBody()
        method.addInstructions(
            0,
            """
                sget-object v0, $fieldDescriptor
                return-object v0
            """.trimIndent()
        )
    }
}
