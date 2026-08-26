package app.template.patches.inscodeautoclicker

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ZipoApps PremiumHelper SDK — has_active_purchase getter
//
// SMALI VERIFIED (classes4.dex, v7.1.5):
//   .class public final Lcom/zipoapps/premiumhelper/b;   ← was 'd' in v7.1.4
//   .source "Preferences.kt"
//   .method public final x()Z  .registers 4
//   [0] iget-object v0, p0, b->a:Landroid/content/SharedPreferences;
//   [1] const-string v1, "has_active_purchase"
//   [2] const/4 v2, 0x0
//   [3] invoke-interface {v0,v1,v2}, SharedPreferences->getBoolean(String;Z)Z
//   [4] move-result v0
//   [5] return v0
//
// WHAT CHANGED IN 7.1.5:
//   The SDK class was renamed from 'd' → 'b' by R8 (short obfuscated name recycled).
//   Method name 'x' and field name 'a' are also obfuscated and may change again.
//   The old fingerprint anchored on definingClass="Lcom/zipoapps/premiumhelper/d;"
//   and name="x" — both are R8-recycled and broke on this update.
//
// STABLE ANCHOR STRATEGY — no obfuscated names at all:
//   - returnType="Z" + accessFlags PUBLIC FINAL + parameters=[] — narrows to getter
//   - string("has_active_purchase") — SDK-defined SharedPreferences key; stable
//     across SDK versions as long as billing architecture stays the same.
//     Only occurs in the ZipoApps SDK (confirmed across all 5 DEX files).
//   - methodCall(SharedPreferences, getBoolean) — confirms this reads from prefs.
//   Together these three uniquely identify x()Z regardless of class name or method name.
//   "has_active_purchase" appears twice in b.smali but only once in a ()Z method —
//   the other occurrence is in U(Z)V (setter, returnType=V), which the returnType
//   filter excludes.
//
// DEX: classes4
internal val HasActivePurchaseFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        string("has_active_purchase"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getBoolean",
        ),
    ),
)
