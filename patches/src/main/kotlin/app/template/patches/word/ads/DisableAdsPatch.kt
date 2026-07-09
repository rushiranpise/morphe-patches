package app.template.patches.word.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.clearBody

private val getAIFAFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/adsmobile_admeasurementpartner/admeasurement/AdMeasurementPlatformData;",
    name = "getAIFA",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
)

private val getAppSetIdFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/adsmobile_admeasurementpartner/admeasurement/AdMeasurementPlatformData;",
    name = "getAppSetId",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
)

private val wordDisableAdsPatch = bytecodePatch(
) {
    execute {
        getAIFAFingerprint.method.apply {
            clearBody()
            addInstructions(0, """
                    const-string v0, ""
                    return-object v0
                """)
        }
        getAppSetIdFingerprint.method.apply {
            clearBody()
            addInstructions(0, """
                    const-string v0, ""
                    return-object v0
                """)
        }
    }
}

@JvmSynthetic
internal fun wordDisableAdsDependency() = wordDisableAdsPatch
