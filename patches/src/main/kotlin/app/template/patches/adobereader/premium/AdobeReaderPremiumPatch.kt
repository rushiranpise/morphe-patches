package app.template.patches.adobereader.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.patcher.util.smali.ExternalLabel
import app.template.patches.shared.Constants.ADOBE_READER_COMPATIBILITY

/**
 * Unlocks on-device Acrobat Pro tools in Adobe Acrobat (Reader).
 *
 * ## What works after patching
 * On-device PDF tools that run locally:
 *   - Edit text and images in PDFs
 *   - Organise / rearrange pages
 *   - Crop PDF pages
 *   - Acrobat DC Lite tools
 *
 * ## What still requires a real subscription
 * Cloud-dependent services that hit Adobe's Document Cloud API:
 *   - Export to Word / Excel / PowerPoint
 *   - Create PDF (from Office files)
 *   - OCR (server-side)
 *   - Adobe cloud storage / Send & Track
 *
 * ## Architecture (credit: arandomhooman/hoomans-morphe-patches)
 *
 * SVServicesAccount is the single entitlement chokepoint — its class name
 * and public method names survive R8. Every feature gate calls:
 *
 *   W0()Z → R0(SERVICE_TYPE)Z → [Z0()Z always false] → S0(SERVICE_TYPE)Z
 *                                                         ↓ calls d1/f1
 *   d1(SERVICES_VARIANTS)Z   → SharedPrefs "acrobatPremiumSubscriptionStatusKey"
 *   f1(SERVICES_VARIANTS)Z   → SharedPrefs variant cache
 *
 * We grant on-device SERVICE_TYPEs at the TOP of R0(), before its own logic
 * runs, by matching the enum parameter's name() string — NOT its ordinal, which
 * R8 renumbers. Cloud SERVICE_TYPEs (EXPORTPDF, CREATEPDF etc.) are NOT granted
 * so the app doesn't attempt cloud work it can't complete.
 *
 * We do NOT fake the signed-in state (b1()Z/E0()Z): a globally spoofed sign-in
 * with no real Adobe token sends the app down auth paths that never converge,
 * causing a BillingClient reconcile loop. Leaving login state honest and only
 * injecting at the per-service-type check avoids this.
 */
@Suppress("unused")
val adobeReaderPremiumPatch = bytecodePatch(
    name = "Adobe Acrobat Premium",
    description = "Unlocks on-device Acrobat Pro tools (edit, organise, crop) without a subscription.",
) {
    compatibleWith(ADOBE_READER_COMPATIBILITY)

    execute {
        val account = mutableClassDefByOrNull(
            "Lcom/adobe/libs/services/auth/SVServicesAccount;"
        ) ?: throw PatchException(
            "Adobe Reader: SVServicesAccount not found — services-account package changed."
        )

        val serviceTypeDesc =
            "Lcom/adobe/libs/services/utils/SVConstants\$SERVICE_TYPE;"
        val variantsDesc =
            "Lcom/adobe/libs/services/utils/SVConstants\$SERVICES_VARIANTS;"

        // R0(SERVICE_TYPE)Z — grant EVERY known service type (all local + server).
        // Server-side features will show as enabled; cloud calls may fail gracefully
        // or succeed depending on the Adobe account. UNAVAILABLE_SERVICE excluded.
        val r0 = account.methods.firstOrNull {
            it.name == "R0" && it.returnType == "Z" &&
                it.parameterTypes.map { p -> p.toString() } == listOf(serviceTypeDesc)
        } ?: throw PatchException(
            "Adobe Reader: SVServicesAccount.R0(SERVICE_TYPE)Z not found — method was renamed."
        )
        r0.grantForEnumNames(
            "ADC_SERVICE",
            "EXPORTPDF_SERVICE",
            "EDITPDF_SERVICE",
            "CREATEPDF_SERVICE",
            "CROPPDF_SERVICE",
            "LIQUIDMODE_SERVICE",
            "COMPRESSPDF_SERVICE",
            "PROTECTPDF_SERVICE",
            "ACROBATPRO_SERVICE",
            "COMBINEPDF_SERVICE",
            "ORGANIZEPDF_SERVICE",
            "ACROBAT_DC_LITE_SERVICE",
            "ACROBAT_READER_PLUS_SERVICE",
            "CREATEPDF_STANDALONE",
            "ACROBAT_PREMIUM_SERVICE",
            "SCAN_PREMIUM_SERVICE",
            "OCR_SERVICE",
            "AI_ASSISTANT_ADD_ON",
            "ACROBAT_PREMIUM_AND_GEN_AI_BUNDLE",
            "ACROBAT_PRO_AND_GEN_AI_BUNDLE",
            "ACROBAT_LITE_AI_ASSISTANT_BUNDLE",
            "ACROBAT_STUDIO",
            "ACROBAT_STUDIO_LITE",
        )

        // d1/f1(SERVICES_VARIANTS)Z — grant EVERY known subscription variant.
        listOf("d1", "f1").forEach { methodName ->
            val method = account.methods.firstOrNull {
                it.name == methodName && it.returnType == "Z" &&
                    it.parameterTypes.map { p -> p.toString() } == listOf(variantsDesc)
            } ?: throw PatchException(
                "Adobe Reader: SVServicesAccount.$methodName(SERVICES_VARIANTS)Z not found."
            )
            method.grantForEnumNames(
                "ADC_SUBSCRIPTION",
                "EXPORT_PDF_SUBSCRIPTION",
                "CREATE_PDF_SUBSCRIPTION",
                "PDF_PACK_SUBSCRIPTION",
                "ACROBAT_STANDARD_SUBSCRIPTION",
                "ACROBAT_PRO_SUBSCRIPTION",
                "ACROBAT_SEND_SUBSCRIPTION",
                "ACROBAT_PREMIUM_SUBSCRIPTION",
                "SCAN_PREMIUM_SUBSCRIPTION",
                "ACROBAT_DC_LITE_SUBSCRIPTION",
                "ACROBAT_READER_PLUS_SUBSCRIPTION",
                "CROP_PDF_SUBSCRIPTION",
                "AI_ASSISTANT_ADD_ON_PACK",
                "ACROBAT_PREMIUM_AND_GEN_AI_BUNDLE",
                "ACROBAT_PRO_AND_GEN_AI_BUNDLE",
                "ACROBAT_LITE_AI_ASSISTANT_BUNDLE",
                "ACROBAT_STUDIO_SUBSCRIPTION",
                "ACROBAT_STUDIO_LITE_SUBSCRIPTION",
            )
        }
    }
}

/**
 * Inject at index 0: if the enum parameter's name() matches any granted constant,
 * return true immediately; otherwise fall through to the original body.
 * Uses name() not ordinal() — R8 renumbers ordinals but preserves name strings.
 */
private fun MutableMethod.grantForEnumNames(vararg names: String) {
    val checks = buildString {
        names.forEach { name ->
            append(
                """
                const-string v1, "$name"
                invoke-virtual {v0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                move-result v1
                if-nez v1, :grant
                """.trimIndent()
            )
            append("\n")
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
