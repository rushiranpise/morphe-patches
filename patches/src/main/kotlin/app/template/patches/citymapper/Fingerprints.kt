package app.template.patches.citymapper

import app.morphe.patcher.Fingerprint

// ── Stable non-obfuscated classes ────────────────────────────────────────────

// Entry point — always stable; 'g' field (fake-subscription boolean) unchanged.
internal val CitymapperApplicationOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/citymapper/app/ActualCitymapperApplication;",
    name = "onCreate",
    returnType = "V",
    parameters = emptyList(),
)

internal val SubscriptionsActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/citymapper/app/subscription/impl/SubscriptionsActivity;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

// ── FeatureFlag enum ─────────────────────────────────────────────────────────
//
// The FeatureFlag enum holds USE_FAKE_SUBSCRIPTION and is the sole gate checked
// by isEnabled() to unlock club features. The obfuscated class name shifts every
// update (v11.55: Lst3; → v11.56: Lry3;).
//
// "USE_FAKE_SUBSCRIPTION" appears only in <clinit> (the enum initialiser), not
// inside isEnabled() itself. The string() filter in filters= searches method
// instruction bodies, so it cannot match isEnabled(). Instead we use:
//   classFingerprint: finds the FeatureFlag class by its <clinit> string.
//   name + returnType + parameters: then pins to isEnabled() within that class.
//
// Smali verified (v11.56.1, classes.dex, Lry3;):
//   .class public enum Lry3;
//   <clinit>: const-string v13, "USE_FAKE_SUBSCRIPTION"   ← classFingerprint
//   .method public isEnabled()Z                            ← name/sig match
internal val FeatureFlagIsEnabledFingerprint = Fingerprint(
    classFingerprint = Fingerprint(
        strings = listOf("USE_FAKE_SUBSCRIPTION"),
    ),
    name = "isEnabled",
    returnType = "Z",
    parameters = emptyList(),
)

// ── ClubFeatures list filter ──────────────────────────────────────────────────
//
// Static method: takes the full list of club feature entries, returns filtered
// list for the current plan tier. The "default" string is the fallback tier key
// set in the enum's <clinit> — not inside the b(List)List method itself.
// Class name shifts every update (v11.55: Lof7; → v11.56: Lsr7;).
//
// Same pattern as FeatureFlagIsEnabledFingerprint: classFingerprint locates the
// class by its <clinit> string; name + returnType + parameters pin the method.
//
// Smali verified (v11.56.1, classes4.dex, Lsr7;):
//   <clinit>: const-string v6, "default"   ← classFingerprint
//   .method public static b(Ljava/util/List;)Ljava/util/List;  ← method match
internal val ClubFeaturesFingerprint = Fingerprint(
    classFingerprint = Fingerprint(
        strings = listOf("default"),
    ),
    name = "b",
    returnType = "Ljava/util/List;",
    parameters = listOf("Ljava/util/List;"),
)
