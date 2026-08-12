package app.template.patches.facebook.misc

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// ─── Meta Verified after-post upsell suppressor ──────────────────────────────
// Verified: classes13/com/facebook/nme/fbafterpostupsell/impl/
//            MetaVerifiedFbAfterPostUpsellBottomSheetHandlerImpl.smali → method A00
//
// WHY definingClass+parameters FAILS:
//   The strings field is checked PER METHOD (must be present in the target method body).
//   definingClass uses classDefByOrNull which does an exact classMap lookup — this works,
//   but the parameters list with "L" partial matches combined with accessFlags exact match
//   is fragile. Previous attempts using this approach consistently failed to apply.
//
// FIX: Use filters with stable non-obfuscated SDK method calls that ARE in A00's body.
//   A00 calls Activity.isFinishing() and Activity.isDestroyed() — both non-obfuscated,
//   stable Android SDK methods that appear in this exact order in A00.
//   Combined with returnType=Object and no accessFlags constraint, this uniquely
//   identifies A00 across all 21 DEX files.
//
// A00 method body (verified stable anchors):
//   invoke-virtual {v1}, Landroid/app/Activity;->isFinishing()Z
//   invoke-virtual {v1}, Landroid/app/Activity;->isDestroyed()Z
//   invoke-virtual {p0, p2, p3, p4, v5}, ...MetaVerifiedFbAfterPostUpsellBottomSheetHandlerImpl;->A01(...)
//
// Verified against com.facebook.katana 569.0.0.42.72 (classes13).
internal val MetaVerifiedUpsellFingerprint = Fingerprint(
    returnType = "Ljava/lang/Object;",
    filters = listOf(
        methodCall(
            definingClass = "Landroid/app/Activity;",
            name = "isFinishing",
        ),
        methodCall(
            definingClass = "Landroid/app/Activity;",
            name = "isDestroyed",
        ),
    ),
    custom = { _, classDef ->
        // Restrict to the MetaVerified upsell class — non-obfuscated, stable
        classDef.type == "Lcom/facebook/nme/fbafterpostupsell/impl/MetaVerifiedFbAfterPostUpsellBottomSheetHandlerImpl;"
    },
)

// ─── hWL.<init> — E2EE backup package name lookup ────────────────────────────
// Verified: classes10/X/hWL.smali → constructor <init>()V
//
// hWL calls Context.getPackageName() then matches against a packed-switch table.
// When installed under a renamed package, the lookup fails → NoSuchElementException.
// Fix: inject const-string v6, "com.facebook.katana" after move-result-object v6.
//
// Fingerprint: PUBLIC CONSTRUCTOR, no params +
// filter on Context.getPackageName() (stable Android SDK method) +
// custom: classDef has SharedPreferences field AND Context field.
internal val hWLInitFingerprint = Fingerprint(
    accessFlags = listOf(
        AccessFlags.PUBLIC,
        AccessFlags.CONSTRUCTOR,
    ),
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        methodCall(
            definingClass = "Landroid/content/Context;",
            name = "getPackageName",
        ),
    ),
    custom = { _, classDef ->
        classDef.superclass == "Ljava/lang/Object;" &&
            classDef.fields.any { it.type == "Landroid/content/SharedPreferences;" } &&
            classDef.fields.any { it.type == "Landroid/content/Context;" }
    },
)
