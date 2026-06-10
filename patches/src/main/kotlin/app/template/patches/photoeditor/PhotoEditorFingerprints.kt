package app.template.patches.photoeditor

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// c81.a(Context) — checks license cache for "License.no.advertisement"
// Return true = licensed, premium unlocked, no ads
val PhotoEditorLicenseCacheCheckFingerprint = Fingerprint(
    definingClass = "Lc81;",
    name = "a",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)
