package app.template.patches.casttotv.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Matches H6/a.e() — the master "has any subscription / remove-ads" gate.
 *
 * Smali (classes3/H6/a.smali, method e()Z):
 *   const-string v0, "VCLJLJL"            ← SharedPrefs key: remove-ads IAP
 *   invoke-static {v0, v1}, LH6/f1;->b(Ljava/lang/String;Z)Z
 *   ...
 *   const-string v0, "VCLioJLJL"          ← SharedPrefs key: discount IAP
 *   invoke-static {v0, v1}, LH6/f1;->b(Ljava/lang/String;Z)Z
 *   ...
 *   invoke-static {}, LH6/a;->g()Z        ← yearly_sub check
 *   invoke-static {}, LH6/a;->c()Z        ← monthly_sub check
 *
 * All compound gate methods (j, k, l) delegate to e() first.
 * Returning true here cascades to all subscription and feature gates.
 *
 * Stable signals: app-specific obfuscated SharedPrefs keys "VCLJLJL"
 * and "VCLioJLJL" combined with the SharedPrefs getter call signature.
 */
internal object IsSubscribedFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        string("VCLJLJL"),
        methodCall(
            definingClass = "LH6/f1;",
            name = "b",
        ),
        string("VCLioJLJL"),
        methodCall(
            definingClass = "LH6/f1;",
            name = "b",
        ),
    ),
)


