package app.template.patches.myradar.premium

// Fingerprints resolved manually in execute {} via classDefByStrings intersections.
//
// ki4  = Entitlements data class; pinned by unique toString string "Entitlements(entitlements="
//   - ki4.c(Entitlement)Z   → hasEntitlement — patched to always return true
//   - ki4.d([Entitlement)Z  → hasEntitlements (varargs) — patched to always return true
//
// LicenseStore = com.acmeaom.android.billing.licenses.LicenseStore (non-obfuscated descriptor)
//   - LicenseStore.l()Z     → hasPremium — patched to always return true
