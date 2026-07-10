package app.template.patches.subwaynow

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.SUBWAYNOW_COMPATIBILITY
import app.template.patches.shared.clearBody
import org.w3c.dom.Element

private const val PLUS_SKU = "subwaynowapp.plus"

private fun forcePlusStateInstructions(ownerRegister: String = "p0") = """
    iget-object v0, $ownerRegister, Lp5/f;->a:Lp5/i;
    iget-object v0, v0, Lp5/i;->b:La6/Z;
    const-string v1, "$PLUS_SKU"
    invoke-static { v1 }, Ljava/util/Collections;->singletonList(Ljava/lang/Object;)Ljava/util/List;
    move-result-object v1
    :force_plus_state
    invoke-virtual { v0 }, La6/Z;->getValue()Ljava/lang/Object;
    move-result-object v2
    invoke-virtual { v0, v2, v1 }, La6/Z;->k(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :force_plus_state
    return-void
""".trimIndent()

private val stripLicenseManifestPatch = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.documentElement
            val permissions = document.getElementsByTagName("uses-permission")
            for (index in permissions.length - 1 downTo 0) {
                val node = permissions.item(index) as Element
                if (node.getAttribute("android:name") in setOf(
                        "com.android.vending.CHECK_LICENSE",
                        "android.permission.ACCESS_ADSERVICES_AD_ID",
                        "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
                    )
                ) {
                    manifest.removeChild(node)
                }
            }
        }
    }
}

@Suppress("unused")
val unlockPlusPatch = bytecodePatch(
    name = "Unlock Plus",
    description = "Unlocks Subway Now Plus.",
    default = true,
) {
    compatibleWith(SUBWAYNOW_COMPATIBILITY)
    dependsOn(stripLicenseManifestPatch)

    execute {
        PurchaseManagerConstructorFingerprint.method.addInstructions(
            PurchaseManagerConstructorFingerprint.method.implementation!!.instructions.size - 1,
            """
                const-string v0, "$PLUS_SKU"
                invoke-static { v0 }, Ljava/util/Collections;->singletonList(Ljava/lang/Object;)Ljava/util/List;
                move-result-object v0
                iget-object v1, p0, Lp5/i;->b:La6/Z;
                invoke-virtual { v1 }, La6/Z;->getValue()Ljava/lang/Object;
                move-result-object v2
                invoke-virtual { v1, v2, v0 }, La6/Z;->k(Ljava/lang/Object;Ljava/lang/Object;)Z
            """.trimIndent(),
        )

        PurchaseQueryCallbackFingerprint.method.apply {
            clearBody()
            addInstructions(0, forcePlusStateInstructions())
        }

        PurchaseUpdateCallbackFingerprint.method.apply {
            clearBody()
            addInstructions(0, forcePlusStateInstructions().replace("Lp5/f;->a", "Lp5/g;->a"))
        }

        LicenseProviderOnCreateFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return v0
                """.trimIndent(),
            )
        }

        InitializeLicenseCheckFingerprint.method.apply {
            clearBody()
            addInstructions(0, "return-void")
        }

        CheckLicenseFingerprint.methodOrNull?.addInstructions(0, "return-void")
    }
}
