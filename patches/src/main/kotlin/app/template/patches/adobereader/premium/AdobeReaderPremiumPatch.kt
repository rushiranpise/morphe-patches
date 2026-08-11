package app.template.patches.adobereader.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.patcher.util.smali.ExternalLabel
import app.template.patches.shared.Constants.ADOBE_READER_COMPATIBILITY
import app.template.patches.shared.returnEarly

/**
 * Unlocks all Acrobat Pro tools in Adobe Acrobat (Reader).
 *
 * ## Architecture (fully traced, 3 layers)
 *
 * ### Layer A — SVServicesAccount.R0/d1/f1() — enum-name gates
 * Static entitlement checks via SERVICE_TYPE / SERVICES_VARIANTS enums.
 * Our injection fires at index 0 before any login or SharedPrefs check.
 * Covers: most ARUserSubscriptionStatusUtil methods (f-u) which call R0().
 *
 * ### Layer B — SVServicesAccount.e1(SERVICE_TYPE)Z — ordinal-based gate
 * Checks b1()Z (login) first — returns false immediately if not signed in.
 * Then reads SharedPrefs via Enum.ordinal() switch table (NOT name()).
 * Called by ARUserSubscriptionStatusUtil.g/j/m/q()Z and j subclass.
 * Our enum-name injection does NOT cover this path.
 * Fix: returnEarly(true) on e1() — bypasses both login and ordinal check.
 *
 * ### Layer C — SVServicesAccount no-arg Z methods (X0, Y0, W0, D0, E0…)
 * Read SharedPrefs directly or delegate to other checks.
 * W0() is already patched via R0() (it calls R0 for each service type).
 * X0/Y0 read SharedPrefs directly — returnEarly(true) needed.
 *
 * ### Layer D — ARUserSubscriptionStatusUtil f-u()Z (15 methods)
 * High-level feature gate called by Activities/Fragments.
 * Most call R0() (covered by Layer A), some call e1() (covered by Layer B).
 * returnEarly(true) on all 15 covers any future method additions.
 *
 * ## Credit
 * Approach adapted from arandomhooman/hoomans-morphe-patches.
 */
@Suppress("unused")
val adobeReaderPremiumPatch = bytecodePatch(
    name = "Adobe Acrobat Premium",
    description = "Unlocks all Acrobat Pro and Studio tools without a subscription.",
) {
    compatibleWith(ADOBE_READER_COMPATIBILITY)

    execute {
        val serviceTypeDesc =
            "Lcom/adobe/libs/services/utils/SVConstants\$SERVICE_TYPE;"
        val variantsDesc =
            "Lcom/adobe/libs/services/utils/SVConstants\$SERVICES_VARIANTS;"

        val account = mutableClassDefByOrNull(
            "Lcom/adobe/libs/services/auth/SVServicesAccount;"
        ) ?: throw PatchException(
            "Adobe Reader: SVServicesAccount not found."
        )

        // ── Layer A: R0(SERVICE_TYPE)Z — enum-name injection ─────────────────
        // Fires before Z0() / b1() / SharedPrefs. Covers all callers that
        // pass a SERVICE_TYPE including ARUserSubscriptionStatusUtil.f-u()Z.
        account.methods.firstOrNull {
            it.name == "R0" && it.returnType == "Z" &&
                it.parameterTypes.map { p -> p.toString() } == listOf(serviceTypeDesc)
        }?.grantAllServiceTypes()
            ?: throw PatchException("Adobe Reader: SVServicesAccount.R0(SERVICE_TYPE)Z not found.")

        // ── Layer A: d1/f1(SERVICES_VARIANTS)Z — enum-name injection ─────────
        listOf("d1", "f1").forEach { name ->
            account.methods.firstOrNull {
                it.name == name && it.returnType == "Z" &&
                    it.parameterTypes.map { p -> p.toString() } == listOf(variantsDesc)
            }?.grantAllVariants()
                ?: throw PatchException("Adobe Reader: SVServicesAccount.$name(SERVICES_VARIANTS)Z not found.")
        }

        // ── Layer B: e1(SERVICE_TYPE)Z — login-gated ordinal check ───────────
        // Checks b1()Z first (login gate) then reads SharedPrefs via ordinal.
        // returnEarly(true) bypasses both. Called by ARUserSubscriptionStatusUtil
        // g/j/m/q()Z for EDITPDF, ORGANIZEPDF, EXPORTPDF, CREATEPDF.
        account.methods.firstOrNull {
            it.name == "e1" && it.returnType == "Z" &&
                it.parameterTypes.map { p -> p.toString() } == listOf(serviceTypeDesc)
        }?.returnEarly(true)
            ?: throw PatchException("Adobe Reader: SVServicesAccount.e1(SERVICE_TYPE)Z not found.")

        // ── Layer C: SharedPrefs-direct no-arg methods ────────────────────────
        // X0()Z, Y0()Z read SharedPrefs directly (no login gate, no enum).
        // D0()Z, E0()Z are additional subscription state checks.
        // O1 and r1 are abstract (no body) — excluded to avoid NPE on null impl.
        listOf("X0", "Y0", "D0", "E0", "O0", "P0", "P1", "Q0", "Q1",
               "T", "V", "a1", "c1", "k0", "n1", "t1").forEach { name ->
            account.methods.firstOrNull { it.name == name && it.returnType == "Z" }
                ?.returnEarly(true)
        }

        // ── Layer D: ARUserSubscriptionStatusUtil f-u()Z (15 methods) ────────
        // High-level feature gates used by Activities/Fragments directly.
        // All call through R0/e1 but returnEarly(true) provides defence-in-depth.
        val aru = mutableClassDefByOrNull(
            "Lcom/adobe/reader/preference/profile/ARUserSubscriptionStatusUtil;"
        ) ?: throw PatchException("Adobe Reader: ARUserSubscriptionStatusUtil not found.")

        listOf("f","g","h","i","j","l","m","n","o","p","q","r","s","t","u").forEach { name ->
            aru.methods.firstOrNull { it.name == name && it.returnType == "Z" }
                ?.returnEarly(true)
        }
    }
}

private val allServiceTypes = listOf(
    "ADC_SERVICE", "EXPORTPDF_SERVICE", "EDITPDF_SERVICE", "CREATEPDF_SERVICE",
    "CROPPDF_SERVICE", "LIQUIDMODE_SERVICE", "COMPRESSPDF_SERVICE", "PROTECTPDF_SERVICE",
    "ACROBATPRO_SERVICE", "COMBINEPDF_SERVICE", "ORGANIZEPDF_SERVICE",
    "ACROBAT_DC_LITE_SERVICE", "ACROBAT_READER_PLUS_SERVICE", "CREATEPDF_STANDALONE",
    "ACROBAT_PREMIUM_SERVICE", "SCAN_PREMIUM_SERVICE", "OCR_SERVICE",
    "AI_ASSISTANT_ADD_ON", "ACROBAT_PREMIUM_AND_GEN_AI_BUNDLE",
    "ACROBAT_PRO_AND_GEN_AI_BUNDLE", "ACROBAT_LITE_AI_ASSISTANT_BUNDLE",
    "ACROBAT_STUDIO", "ACROBAT_STUDIO_LITE",
)

private val allVariants = listOf(
    "ADC_SUBSCRIPTION", "EXPORT_PDF_SUBSCRIPTION", "CREATE_PDF_SUBSCRIPTION",
    "PDF_PACK_SUBSCRIPTION", "ACROBAT_STANDARD_SUBSCRIPTION", "ACROBAT_PRO_SUBSCRIPTION",
    "ACROBAT_SEND_SUBSCRIPTION", "ACROBAT_PREMIUM_SUBSCRIPTION", "SCAN_PREMIUM_SUBSCRIPTION",
    "ACROBAT_DC_LITE_SUBSCRIPTION", "ACROBAT_READER_PLUS_SUBSCRIPTION", "CROP_PDF_SUBSCRIPTION",
    "AI_ASSISTANT_ADD_ON_PACK", "ACROBAT_PREMIUM_AND_GEN_AI_BUNDLE",
    "ACROBAT_PRO_AND_GEN_AI_BUNDLE", "ACROBAT_LITE_AI_ASSISTANT_BUNDLE",
    "ACROBAT_STUDIO_SUBSCRIPTION", "ACROBAT_STUDIO_LITE_SUBSCRIPTION",
)

private fun MutableMethod.grantAllServiceTypes() = grantForEnumNames(allServiceTypes)
private fun MutableMethod.grantAllVariants() = grantForEnumNames(allVariants)

private fun MutableMethod.grantForEnumNames(names: List<String>) {
    val checks = buildString {
        names.forEach { name ->
            append("const-string v1, \"$name\"\n")
            append("invoke-virtual {v0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z\n")
            append("move-result v1\n")
            append("if-nez v1, :grant\n")
        }
    }
    addInstructionsWithLabels(
        0,
        """
        if-eqz p1, :original
        invoke-virtual {p1}, Ljava/lang/Enum;->name()Ljava/lang/String;
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
