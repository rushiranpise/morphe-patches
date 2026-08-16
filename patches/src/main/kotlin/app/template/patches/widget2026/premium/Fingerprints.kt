package app.template.patches.widget2026.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// Widget 2026 / Aesthetic Widgets v85.1.4 (versionCode 116)
// Package: com.remi.widget — Native Android, 3 DEX + audience_network.dex
// Compiler: R8. Billing: com.zipoapps.premiumhelper SDK + Google Play Billing (217 classes).
// Ads: AppLovin (2776 classes), Meta Audience Network, AdMob.
// No Pairip, no Play Integrity.
//
// Premium check cascade:
//   d.b()Z  [public static, PremiumHelper public API]
//     → e$a.a() → PremiumHelper singleton (e)
//     → e.h (yd/e)
//     → yd/e.j()Z
//         → SharedPreferences.getBoolean("has_active_purchase", false)
//
// d.b() is called from 34 sites: every widget Activity, MainActivity, ActivitySetting,
// BaseProvider (controls all widget rendering), and internal SDK screens.
//
// Two-layer patch: yd/e.j() at the SharedPrefs root + d.b() at the public API level.
// Smali verified against versionCode 116.

// Targets yd/e.j()Z — reads SharedPrefs key "has_active_purchase".
// This is the root of the premium check cascade.
//
// string("has_active_purchase") uniquely identifies this method: the string only appears
// in a ()Z method in yd/e.smali. Combined with returnType + PUBLIC FINAL, it is unique
// across all 33k smali files.
//
// Smali verified (smali_classes3/yd/e.smali):
//   .method public final j()Z
//       const-string v0, "has_active_purchase"
//       const/4 v1, 0x0
//       iget-object v2, p0, Lyd/e;->a:Landroid/content/SharedPreferences;
//       invoke-interface {v2, v0, v1}, Landroid/content/SharedPreferences;->getBoolean(Ljava/lang/String;Z)Z
//       move-result v0
//       return v0
//   .end method
object HasActivePurchaseFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    strings = listOf("has_active_purchase"),
)

// Targets d.b()Z — PremiumHelper's public static isPremium() API.
// Called from 34 sites across all widget Activities and BaseProvider.
//
// d.smali has no const-strings. Fingerprinted via the unique instruction sequence:
//   invoke-static {}, Lcom/zipoapps/premiumhelper/e$a;->a()Lcom/zipoapps/premiumhelper/e;
//   ...
//   invoke-virtual {v0}, Lyd/e;->j()Z
// The combination of calling e$a.a() then yd/e.j() in a static ()Z method is unique.
//
// Smali verified (smali_classes3/com/zipoapps/premiumhelper/d.smali):
//   .method public static final b()Z
//       sget-object v0, Lcom/zipoapps/premiumhelper/e;->C:Lcom/zipoapps/premiumhelper/e$a;
//       invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
//       invoke-static {}, Lcom/zipoapps/premiumhelper/e$a;->a()Lcom/zipoapps/premiumhelper/e;
//       move-result-object v0
//       iget-object v0, v0, Lcom/zipoapps/premiumhelper/e;->h:Lyd/e;
//       invoke-virtual {v0}, Lyd/e;->j()Z
//       move-result v0
//       return v0
//   .end method
object IsPremiumStaticFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/zipoapps/premiumhelper/e\$a;",
            name = "a",
        ),
        methodCall(
            definingClass = "Lyd/e;",
            name = "j",
        ),
    ),
)
