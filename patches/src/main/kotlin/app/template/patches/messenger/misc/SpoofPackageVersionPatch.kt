package app.template.patches.messenger.misc

import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import app.template.patches.shared.Constants.MESSENGER_COMPATIBILITY
import org.w3c.dom.Element
import app.template.patches.messenger.misc.messengerSignaturePatch

// Sets the versionCode to Int.MAX_VALUE so the Play Store considers the app
// already up to date and never offers an update prompt.
//
// Current versionCode for v573.0.0.44.88 is 344611864.
// Defaults to 2147483647 (Int.MAX_VALUE).
//
// Verified against com.facebook.orca 573.0.0.44.88.
@Suppress("unused")
val messengerSpoofPackageVersionPatch = resourcePatch(
    name = "Spoof package version",
    description = "Sets a very high version code so the Play Store treats the app as already up to date and never offers an update.",
    default = false,
) {
    compatibleWith(MESSENGER_COMPATIBILITY)

    dependsOn(messengerSignaturePatch)

    val versionCodeOption by stringOption(
        key = "messengerVersionCode",
        default = "2147483647",
        title = "Version code",
        description = "The version code to set. Must be higher than the Play Store version to suppress updates. Defaults to the maximum value.",
        required = true,
    ) { it != null && it.matches(Regex("^\\d{1,10}$")) && it.toLong() <= Int.MAX_VALUE.toLong() }

    finalize {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.getElementsByTagName("manifest").item(0) as Element
            manifest.setAttribute("android:versionCode", versionCodeOption ?: "2147483647")
        }
    }
}
