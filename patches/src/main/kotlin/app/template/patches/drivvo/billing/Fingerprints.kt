package app.template.patches.drivvo.billing

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ── IsPremiumFingerprint ──────────────────────────────────────────────────────
//
// Targets: y9.a(Context)Z  [classes.dex]
//
// App-wide isPremium() gate. Checks fleet account (e20.t), then login flag,
// then reads expiry date via internal wrapper w62.k(), compares to now via ej.k().
// Does NOT call SharedPreferences.getString directly — uses w62.k() wrapper.
//
// Unique: y9 has exactly ONE public static (Context)Z method.
// String filter anchors to "PlanoDrivvoServerDataExpiracao" for version stability.
//
object IsPremiumFingerprint : Fingerprint(
    definingClass = "Ly9;",
    name = "a",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        string("PlanoDrivvoServerDataExpiracao"),
    )
)

// ── PlanTypeFingerprint ───────────────────────────────────────────────────────
//
// Targets: y9.d(Context)Ljava/lang/String;  [classes.dex]
//
// Returns stored plan tier: "pro", "individual", "frota*", null.
// "pro" → PlanosNovoActivity returns immediately (fully unlocked).
//
// Smali: e20.t fleet check → if fleet: return "frota"
//        else: w62.h(ctx) → SharedPreferences → getString("PlanoDrivvoServerPlano")
//
// String filter: "PlanoDrivvoServerPlano" appears in y9 (method body) + gq.
// definingClass+name uniquely identifies this method.
//
object PlanTypeFingerprint : Fingerprint(
    definingClass = "Ly9;",
    name = "d",
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        string("PlanoDrivvoServerPlano"),
    )
)

// ── VehicleLimitFingerprint ───────────────────────────────────────────────────
//
// Targets: y9.j(Activity)Z  [classes.dex]
//
// Checks whether the user has exceeded their plan's vehicle limit and gates
// feature screens. Called from vehicle-related screens before allowing access.
// Logic:
//   if y9.a()=true (premium):
//     reads SP("PlanoDrivvoServerVeiculos") → the plan's max vehicle count
//     if activeVehicles > limit → PlanosNovoActivity.t0(activity, 6 or 8) + return false
//     else → return true
//   if y9.a()=false (free):
//     if activeVehicles >= 2 → PlanosNovoActivity.t0(activity, 5) + return false
//     else → return true
//
// Even with y9.a() patched to true, SP("PlanoDrivvoServerVeiculos") is 0/null
// (never written by the server for a non-paying user), so 0 vehicles ≥ any count
// → this method always returns false for users with ≥1 vehicle, blocking the app.
//
// FIX: return true unconditionally — no vehicle limit applies.
//
// String filter: "TbVeiculo" (SQLite table name queried for vehicle count)
// uniquely identifies this method in y9 (appears only at line 1371).
//
object VehicleLimitFingerprint : Fingerprint(
    definingClass = "Ly9;",
    name = "j",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/app/Activity;"),
    filters = listOf(
        string("TbVeiculo"),
    )
)

// ── PremiumScreenGateFingerprint ──────────────────────────────────────────────
//
// Targets: y9.k(Activity)Z  [classes.dex]
//
// Simplified gate used on screens that only need a basic premium check.
// Logic:
//   if y9.a()=true → return true  (but may also launch PlanosNovoActivity)
//   if y9.a()=false → PlanosNovoActivity.t0(activity, tab) + return false
//
// Smali (y9.smali line 1490):
//   .method public static k(Landroid/app/Activity;)Z
//   invoke-static { p0 }, Ly9;->a(Context)Z
//   if-eqz v0, :L0   → if false: show plans screen + return false
//   return p0         → return true
//
// No string filters needed — method is uniquely identified by definingClass + name
// + return type + parameter. y9 has exactly one method named "k".
//
object PremiumScreenGateFingerprint : Fingerprint(
    definingClass = "Ly9;",
    name = "k",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/app/Activity;"),
)
