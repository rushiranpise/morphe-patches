package app.template.patches.inscodeautoclicker

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// Lcom/zipoapps/premiumhelper/d;->x()Z
// ZipoApps PremiumHelper SDK v5.2.1 — Preferences class.
//
// SMALI VERIFIED (classes4.dex, v7.1.4):
//   .class public final Lcom/zipoapps/premiumhelper/d;
//   .method public final x()Z  .registers 4
//   [0] iget-object v0, p0, d->a:Landroid/content/SharedPreferences;
//   [1] const-string v1, "has_active_purchase"
//   [2] const/4 v2, 0x0
//   [3] invoke-interface {v0, v1, v2}, SharedPreferences->getBoolean(String;Z)Z
//   [4] move-result v0
//   [5] return v0
//
// PATCH: returnEarly(true) injects "const/4 v0, 0x1 / return v0" at index 0.
// Cleaner than the old replaceInstruction(lastIndex - 1) approach — no
// fragile lastIndex arithmetic that could break if the method grows.
//
// STABILITY NOTES:
//   - definingClass "Lcom/zipoapps/premiumhelper/d;" is a 3rd-party SDK class
//     inside the ZipoApps package namespace — not obfuscated by app R8.
//   - "has_active_purchase" is an SDK-defined constant string, stable across
//     app updates as long as the SDK version doesn't change.
//   - If the SDK is upgraded and 'd' is renamed, the string filter will still
//     catch it (the filter is kept for defence-in-depth).
//   - apkFileType = APKS — the method lives in classes4.dex of base.apk.
internal val HasActivePurchaseFingerprint = Fingerprint(
    definingClass = "Lcom/zipoapps/premiumhelper/d;",
    name = "x",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        string("has_active_purchase"),
    ),
)
