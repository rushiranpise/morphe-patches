package app.template.patches.fitia.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// ── Local premium flag ────────────────────────────────────────────────────────
//
// UserModel.isPremium()Z is the single field-backed getter read by every UI
// gate in the app (DatabaseFragment, PlanFragment, ProfileFragment, MenuActivity,
// BarcodeFragment, CompareFoodFragment, ProgressTrackerWidgetUi, etc.).
// The class is in the app's non-obfuscated package — stable across updates.
//
// Smali verified (v25.0.6, classes4.dex):
//   .method public final isPremium()Z
//   iget-boolean p0, p0, Lcom/.../UserModel;->isPremium:Z
//   return p0
internal val UserModelIsPremiumFingerprint = Fingerprint(
    definingClass = "Lcom/nutrition/technologies/Fitia/refactor/data/local/models/UserModel;",
    name = "isPremium",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── Server-side subscription response ────────────────────────────────────────
//
// SubscriptionFitiaDataResponse is the JSON-deserialized billing response from
// Fitia's backend. Its getPremium()Z field is true only when the server
// confirms an active subscription. Returning true here makes every mapping that
// converts this response into local state report "subscribed" immediately.
//
// Smali verified (v25.0.6, classes4.dex):
//   .method public final getPremium()Z
//   iget-boolean p0, p0, L.../SubscriptionFitiaDataResponse;->premium:Z
//   return p0
internal val SubscriptionResponsePremiumFingerprint = Fingerprint(
    definingClass = "Lcom/nutrition/technologies/Fitia/refactor/data/remote/models/familyPlan/SubscriptionFitiaDataResponse;",
    name = "getPremium",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── Entitlement active flag ───────────────────────────────────────────────────
//
// Entitlement is the per-product entitlement record inside the subscription
// response. getActive() returns a boxed Boolean (nullable). Returning
// Boolean.TRUE ensures every entitlement check sees an active entitlement.
//
// Smali verified (v25.0.6, classes4.dex):
//   .method public final getActive()Ljava/lang/Boolean;
//   iget-object p0, p0, L.../Entitlement;->active:Ljava/lang/Boolean;
//   return-object p0
internal val EntitlementActiveFingerprint = Fingerprint(
    definingClass = "Lcom/nutrition/technologies/Fitia/refactor/data/remote/models/familyPlan/Entitlement;",
    name = "getActive",
    returnType = "Ljava/lang/Boolean;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)
