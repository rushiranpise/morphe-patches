package app.template.patches.photoeditor

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// j81.a(Context) — checks license cache for "License.no.advertisement"
// Return true = licensed, premium unlocked, no ads
// v13.3: Lc81;->a(Context)Z → v13.4: Lj81;->a(Context)Z (class renamed)
val PhotoEditorLicenseCacheCheckFingerprint = Fingerprint(
    definingClass = "Lj81;",
    name = "a",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)
