package app.template.patches.ampere.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ── WHY NO OBFUSCATED NAMES ───────────────────────────────────────────────────
//
// R8 re-assigns all short class names (bz0, kf, g01, af...) every build.
// Previous fingerprints used these as anchors and broke every update.
//
// All anchors below use ONLY:
//   • String constants baked into bytecode that R8 cannot strip
//   • Return type + access flags + parameter signatures
//   • Java/Kotlin stdlib method calls (never obfuscated)
// ─────────────────────────────────────────────────────────────────────────────

// ── IsProVersionFingerprint ───────────────────────────────────────────────────
//
// Targets the isProVersion() getter in the SettingsRepository class.
//
// The property name "isProVersion" is passed verbatim to Kotlin's reflection
// API (MutablePropertyReference1Impl.<init>) in the class <clinit>. R8 cannot
// remove or rename this string — it is required at runtime for Kotlin property
// delegation. This makes it a permanent stable anchor for the CLASS.
//
// "isProVersion" lives in <clinit>, NOT in the getter method body itself.
// The `strings=` Fingerprint field searches method bodies — it would NOT find
// the string inside <clinit>. The correct approach is `classFingerprint` to
// pin the class via <clinit> strings, then match the getter by its signature.
//
// Within the class, the getter is the ONLY PUBLIC FINAL ()Z method (verified
// in 4.37.0). No method body filter is needed — the signature is unique.
//
// Smali (4.37.0): classes3/g01.smali
//   <clinit>: const-string v2, "isProVersion"    ← classFingerprint anchor
//             const-string v3, "isProVersion()Z"
//   .method public final f()Z                    ← matched by returnType+flags
//     invoke-virtual { v1, p0, v0 }, Lb11;->getValue(...)Ljava/lang/Object;
//     check-cast p0, Ljava/lang/Boolean;
//     invoke-virtual { p0 }, Ljava/lang/Boolean;->booleanValue()Z
//     return p0
//
object IsProVersionFingerprint : Fingerprint(
    // classFingerprint: scan all classes for one whose body contains "isProVersion".
    // Only the SettingsRepository class has this string (it's the Kotlin property
    // name registered with MutablePropertyReference1Impl). Unique across all DEXes.
    classFingerprint = Fingerprint(
        strings = listOf("isProVersion"),
    ),
    // Within that class, match the single PUBLIC FINAL ()Z method.
    // No name= needed — it would be obfuscated anyway.
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    // Extra guard: the getter calls Boolean.booleanValue() — Java stdlib, stable.
    // This eliminates any synthetic/bridge methods that might also return Z.
    filters = listOf(
        methodCall(
            definingClass = "Ljava/lang/Boolean;",
            name = "booleanValue",
        ),
    ),
)

// ── VerifyPurchaseFingerprint ─────────────────────────────────────────────────
//
// Targets the purchase receipt verifier — validates that the purchase contains
// the product ID "ampere_no_ads" and verifies RSA/Base64 signature via the
// com.braintrapp.billing.iab library.
//
// Two stable method-body anchors (both inside the method, so `filters=` works):
//   1. string("ampere_no_ads") — the Play Store SKU, developer-controlled,
//      never touched by R8.
//   2. methodCall(ArrayList::contains) — Java stdlib, never obfuscated.
//      Immediately follows the product ID const-string load.
//
// Additionally, the method has a catch block for Base64DecoderException from
// the braintrapp IAB library — a third-party stable class path. Used as the
// custom predicate to eliminate any other method that might coincidentally
// contain the same string + contains() pair.
//
// Smali (4.37.0): classes3/af.smali → method a(Lzr0;)Z
//   .catch Lcom/braintrapp/billing/iab/Base64DecoderException; ...
//   const-string v3, "ampere_no_ads"
//   invoke-virtual {v2, v3}, Ljava/util/ArrayList;->contains(Ljava/lang/Object;)Z
//
object VerifyPurchaseFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        string("ampere_no_ads"),
        methodCall(
            definingClass = "Ljava/util/ArrayList;",
            name = "contains",
        ),
    ),
    custom = { method, _ ->
        method.implementation?.tryBlocks?.any { tryBlock ->
            tryBlock.exceptionHandlers.any { handler ->
                handler.exceptionType == "Lcom/braintrapp/billing/iab/Base64DecoderException;"
            }
        } == true
    },
)
