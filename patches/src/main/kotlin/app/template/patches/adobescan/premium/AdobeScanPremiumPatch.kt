package app.template.patches.adobescan.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.patcher.util.smali.ExternalLabel
import app.template.patches.shared.Constants.ADOBE_SCAN_COMPATIBILITY
import app.template.patches.shared.returnEarly

/**
 * Unlocks all premium features in Adobe Scan.
 *
 * ## Architecture (fully traced)
 *
 * Adobe Scan has THREE independent premium gate layers:
 *
 * ### Layer A — hm.n.w/y/z() enum-name gates
 * Static methods called by some feature gates directly.
 * Patched via enum-name injection at index 0 (before x() login check).
 *
 * ### Layer B — nq.t0 — authenticated StateFlow gates (THE MAIN GATE)
 * nq.t0 implements nq.e0 interface. Each method reads an m80.p Lazy<Boolean>
 * StateFlow whose value is populated by coroutine collectors (nq.h0, nq.i0 etc.)
 * calling hm.n.y(SUBSCRIPTION_TYPE)Z. The lazy is uninitialized until the
 * coroutine runs AND the user is logged in. So nq.t0.l()Z etc. return false
 * for unauthenticated users even though hm.n.y() would return true via Layer A.
 *
 * Used by: com.adobe.scan.android.util.l.d()Z, p1()Z, Y()I
 * → dispatched via nq.e0 interface → nq.e0$a.a() singleton getter
 *
 * ### Layer C — nq.e0$a$a — unauthenticated stub (ALL METHODS HARDCODED FALSE)
 * When user is not logged into Adobe IMS, nq.e0$a.a() returns this stub.
 * Every one of its 16 boolean methods is: const/4 p0, 0x0; return p0.
 * CombineActivity and CompressActivity are gated here.
 *
 * ## Patches
 *
 * Patch 1: hm.n.w/y/z() — enum-name injection (covers direct callers)
 * Patch 2: nq.t0 b-s()Z — returnEarly(true) on all 16 methods
 * Patch 3: nq.e0$a$a b-s()Z — returnEarly(true) on all 16 stub methods
 */
@Suppress("unused")
val adobeScanPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium scanning and PDF tools in Adobe Scan.",
) {
    compatibleWith(ADOBE_SCAN_COMPATIBILITY)

    execute {
        val serviceTypeDesc = "Lwm/c\$g;"
        val subscriptionDesc = "Lwm/c\$e;"
        val booleanMethods = listOf("b","c","d","e","f","g","h","i","j","k","l","m","p","q","r","s")

        // ── Patch 1: hm.n.w/y/z() enum-name injection ───────────────────────
        // Fires before the x()Z login check — covers direct callers.
        val hmn = mutableClassDefByOrNull("Lhm/n;")
            ?: throw PatchException("Adobe Scan: hm.n not found.")

        hmn.methods.firstOrNull {
            it.name == "w" && it.returnType == "Z" &&
                it.parameterTypes.map { p -> p.toString() } == listOf(serviceTypeDesc)
        }?.grantAll()
            ?: throw PatchException("Adobe Scan: hm.n.w(wm.c\$g)Z not found.")

        listOf("y", "z").forEach { name ->
            hmn.methods.firstOrNull {
                it.name == name && it.returnType == "Z" &&
                    it.parameterTypes.map { p -> p.toString() } == listOf(subscriptionDesc)
            }?.grantAll()
                ?: throw PatchException("Adobe Scan: hm.n.$name(wm.c\$e)Z not found.")
        }

        // ── Patch 2: nq.t0 — authenticated StateFlow gate ───────────────────
        // All 16 boolean methods read Lazy<Boolean> StateFlows that may be
        // uninitialized. returnEarly(true) bypasses the lazy entirely.
        val t0 = mutableClassDefByOrNull("Lnq/t0;")
            ?: throw PatchException("Adobe Scan: nq.t0 not found.")
        booleanMethods.forEach { name ->
            t0.methods.firstOrNull { it.name == name && it.returnType == "Z" }
                ?.returnEarly(true)
        }

        // ── Patch 3: nq.e0$a$a — unauthenticated stub ───────────────────────
        // Returned by nq.e0$a.a() when not logged in. All methods are
        // hardcoded false. returnEarly(true) converts all to true.
        val stub = mutableClassDefByOrNull("Lnq/e0\$a\$a;")
            ?: throw PatchException("Adobe Scan: nq.e0\$a\$a not found.")
        booleanMethods.forEach { name ->
            stub.methods.firstOrNull { it.name == name && it.returnType == "Z" }
                ?.returnEarly(true)
        }
    }
}

/** Grant all known service/subscription enum values via name() comparison at index 0. */
private fun MutableMethod.grantAll() {
    val allTypes = listOf(
        "ADC_SERVICE", "EXPORTPDF_SERVICE", "EDITPDF_SERVICE", "CREATEPDF_SERVICE",
        "CROPPDF_SERVICE", "LIQUIDMODE_SERVICE", "COMPRESSPDF_SERVICE", "PROTECTPDF_SERVICE",
        "ACROBATPRO_SERVICE", "COMBINEPDF_SERVICE", "ORGANIZEPDF_SERVICE",
        "ACROBAT_DC_LITE_SERVICE", "ACROBAT_READER_PLUS_SERVICE", "CREATEPDF_STANDALONE",
        "ACROBAT_PREMIUM_SERVICE", "SCAN_PREMIUM_SERVICE", "OCR_SERVICE",
        "AI_ASSISTANT_ADD_ON", "ACROBAT_PREMIUM_AND_GEN_AI_BUNDLE",
        "ACROBAT_PRO_AND_GEN_AI_BUNDLE", "ACROBAT_LITE_AI_ASSISTANT_BUNDLE",
        "ACROBAT_STUDIO", "ACROBAT_STUDIO_LITE",
        // subscription variants (wm.c$e)
        "ADC_SUBSCRIPTION", "EXPORT_PDF_SUBSCRIPTION", "CREATE_PDF_SUBSCRIPTION",
        "PDF_PACK_SUBSCRIPTION", "ACROBAT_STANDARD_SUBSCRIPTION", "ACROBAT_PRO_SUBSCRIPTION",
        "ACROBAT_SEND_SUBSCRIPTION", "ACROBAT_PREMIUM_SUBSCRIPTION", "SCAN_PREMIUM_SUBSCRIPTION",
        "ACROBAT_DC_LITE_SUBSCRIPTION", "ACROBAT_READER_PLUS_SUBSCRIPTION", "CROP_PDF_SUBSCRIPTION",
        "AI_ASSISTANT_ADD_ON_PACK", "ACROBAT_PREMIUM_AND_GEN_AI_BUNDLE",
        "ACROBAT_PRO_AND_GEN_AI_BUNDLE", "ACROBAT_LITE_AI_ASSISTANT_BUNDLE",
        "ACROBAT_STUDIO_SUBSCRIPTION", "ACROBAT_STUDIO_LITE_SUBSCRIPTION",
    )
    val checks = buildString {
        allTypes.forEach { name ->
            append("const-string v1, \"$name\"\n")
            append("invoke-virtual {v0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z\n")
            append("move-result v1\n")
            append("if-nez v1, :grant\n")
        }
    }
    addInstructionsWithLabels(
        0,
        """
        if-eqz p0, :original
        invoke-virtual {p0}, Ljava/lang/Enum;->name()Ljava/lang/String;
        move-result-object v0
        $checks
        goto :original
        :grant
        const/4 v0, 0x1
        return v0
        """.trimIndent(),
        ExternalLabel("original", getInstruction(0)),
    )
}
