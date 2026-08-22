package app.template.patches.portdroid.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PORTDROID_COMPATIBILITY
import app.template.patches.shared.returnEarly

// PortDroid Premium Patch
//
// Architecture:
//   PortDroid has two pro-state sources that must both be patched:
//
//   A) Settings.getProVersion()Z — reads SharedPreferences "PRO_VERSION".
//      Consulted directly by every feature gate (export, MDNS, UPnP,
//      traceroute, multi-scan, Wi-Fi details, port history).
//      → returnEarly(true)
//
//   B) IAPViewModel$IAPStatus — the billing status object constructed after
//      each Google Play Billing query. Its proVersion field (= sub != null ||
//      lifetime != null) is observed via LiveData by every BaseActivity
//      subclass. The observer calls unlockProFeatures(Z) on the Activity.
//      → Inject iput-boolean 1 into proVersion field after super.<init>()
//        so every IAPStatus constructed (startup, restore, onResume) reports
//        pro=true regardless of actual purchase state.
//
//   C) TamperCheck.validateAppSignature()Z — computes SHA1 of the re-signed
//      APK and compares it to 3 hardcoded hashes. On mismatch App.onCreate
//      writes INVALID_SIGNATURE=true to SharedPrefs (used as a Crashlytics
//      tag, not a hard block). Returning true keeps telemetry clean and avoids
//      any future use of that flag as a gate.
//      → returnEarly(true)
//
// Patch order: TamperCheck first (dependsOn = none, execute block order) so
// that the flag is clean before billing code runs on first launch.

@Suppress("unused")
val portDroidPremiumPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks PortDroid Pro features by bypassing the Google Play Billing check, the SharedPreferences pro gate, and the signature tamper check.",
    default = true,
) {
    compatibleWith(PORTDROID_COMPATIBILITY)

    execute {
        // ── Patch 1: Signature tamper check ──────────────────────────────
        // validateAppSignature() returns true so INVALID_SIGNATURE is never
        // written to SharedPreferences and Crashlytics sees a valid app.
        ValidateAppSignatureFingerprint.method.returnEarly(true)

        // ── Patch 2: SharedPreferences pro gate ──────────────────────────
        // getProVersion() returns true unconditionally, bypassing all
        // inline feature gates throughout the app.
        GetProVersionFingerprint.method.returnEarly(true)

        // ── Patch 3: IAPStatus — billing status object ───────────────────
        // After super.<init>() (index 0 = invoke-direct Object.<init>),
        // force proVersion, activeSubscription, and activeLifetime to true.
        //
        // Smali layout of <init>(Purchase p1, Purchase p2):
        //   .locals 3  → v0, v1, v2 available
        //   index 0: invoke-direct {p0}, Object.<init>()V
        //   index 1: iput-object p1 → subscriptionPurchase
        //   index 2: iput-object p2 → lifetimePurchase
        //   index 3: const/4 v0, 0x0
        //   index 4: const/4 v1, 0x1
        //   ... (null-checks → iput-boolean for activeSubscription, activeLifetime,
        //        proVersion, multiPurchase)
        //
        // We insert after index 0 (after super.<init>) to overwrite the
        // iput-boolean fields before the null-check logic populates them.
        // v0 and v1 are declared locals — safe to use here.
        IAPStatusConstructorFingerprint.method.addInstructions(
            1, // insert after invoke-direct {p0}, Object.<init>()V
            """
                const/4 v0, 0x1
                iput-boolean v0, p0, Lcom/stealthcopter/portdroid/feature/upgrade/IAPViewModel${'$'}IAPStatus;->proVersion:Z
                iput-boolean v0, p0, Lcom/stealthcopter/portdroid/feature/upgrade/IAPViewModel${'$'}IAPStatus;->activeSubscription:Z
                iput-boolean v0, p0, Lcom/stealthcopter/portdroid/feature/upgrade/IAPViewModel${'$'}IAPStatus;->activeLifetime:Z
            """.trimIndent(),
        )
    }
}
