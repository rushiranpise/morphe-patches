package app.template.patches.facebook.misc

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

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

