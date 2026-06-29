package app.template.patches.theweatherchannel.premium

// No Fingerprint objects — the patch target is AppState.getPremiumSubscriptions(), which is
// resolved directly by class descriptor in execute {} using mutableClassDefBy.
// AppState is a Kotlin data class with a stable, non-obfuscated descriptor:
// Lcom/weather/corgikit/context/AppState;
// The getter is pinned by name ("getPremiumSubscriptions"), return type, and empty params.
