package app.template.patches.parallelspace

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// Lcom/lbe/parallel/ad;->k()Z — global "is Pro / lifetime purchased" flag.
// Reads SharedPreferences "bpcs" int and returns (value == 2).
val IsProActiveFingerprint = Fingerprint(
    definingClass = "Lcom/lbe/parallel/ad;",
    name = "k",
    parameters = emptyList(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(string("bpcs"))
)
