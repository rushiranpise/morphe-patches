package app.template.patches.googlephotos

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.GOOGLE_PHOTOS_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Based on RookieEnough/De-Vanced Google Photos DCIM backup control patch.
@Suppress("unused")
val googlePhotosDcimBackupControlPatch = bytecodePatch(
    name = "Enable DCIM folders backup control",
    description = "Allows controlling Camera and other DCIM folder backup individually.",
    default = false,
) {
    compatibleWith(GOOGLE_PHOTOS_COMPATIBILITY)

    execute {
        IsDcimFolderBackupControlDisabledFingerprint.method.returnEarly(false)
    }
}
