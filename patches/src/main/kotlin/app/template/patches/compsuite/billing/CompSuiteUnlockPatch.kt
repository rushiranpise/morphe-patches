package app.template.patches.compsuite.billing

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPSUITE_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.template.patches.compsuite.protection.compSuitePairIpPatch

// ── App name / branding ───────────────────────────────────────────────────────
// Package: com.gs.complications.suite
// Play Store name: WearOS Toolset (previously "Complications Suite")
// v6.1.2 (versionCode 11619), APKS, 5 DEX files (classes–classes5)

// ── Unlock model ──────────────────────────────────────────────────────────────
//
// Premium state is driven by a single enum:
//
//   UnlockMode { UNKNOWN, FREE, OLD_SUB, MONTHLY, YEARLY, LIFETIME }
//
// Stored in BillingLibrary.unlockMode: StateFlow<UnlockMode> (in-memory).
// Collected by Compose UI via collectAsState(); feature screens check:
//   billingState.unlockMode.isFull()
//     → true for everything except UNKNOWN and FREE
//
// The StateFlow is fed by TWO independent sources:
//
//   SOURCE A — Google Play Billing (BillingClient):
//     processAndAcknowledgePurchases(List<Purchase>)
//       └── evaluatePurchasesToUnlockMode(List<Purchase>): UnlockMode
//             Priority: LIFETIME > YEARLY > MONTHLY > OLD_SUB > FREE
//     Called when BillingClient connects and whenever purchases change.
//
//   SOURCE B — DataStore cache (observeDataStoreCache):
//     Reads a serialised UnlockData (JSON, Preferences DataStore) persisted
//     from the last successful billing query.
//       └── fallbackToTimeBoundedCache(UnlockData, UnlockMode): UnlockMode
//             If cache age < 24h → returns cached UnlockMode
//             If cache age ≥ 24h → returns FREE (the fallback parameter)
//     Runs at app startup before BillingClient connects, giving an immediate
//     premium state without waiting for the network billing query.
//
// ── Why both sources must be patched ─────────────────────────────────────────
//
//   Patching only SOURCE A (evaluatePurchasesToUnlockMode):
//     On cold start, observeDataStoreCache() runs first. If the DataStore has
//     a previously-cached FREE state (written when the user had no purchase),
//     fallbackToTimeBoundedCache() returns FREE → UI shows free mode briefly.
//     Even after BillingClient connects and SOURCE A fires, there is a race
//     condition window. Worse: if BillingClient fails to connect (no network,
//     Play Store unavailable), SOURCE A never fires at all → always FREE.
//
//   Patching both SOURCE A + SOURCE B:
//     fallbackToTimeBoundedCache() always returns LIFETIME → DataStore cache
//     is immediately overridden → unlockMode StateFlow emits LIFETIME at startup.
//     Then SOURCE A also fires → returns LIFETIME → no change. App is fully
//     unlocked in all network conditions including offline.
//
// ── Patch strategy ────────────────────────────────────────────────────────────
//
//   PATCH 1 — EvaluatePurchasesFingerprint → return UnlockMode.LIFETIME
//     clearBody() removes the List iteration + try/catch table.
//     Returns UnlockMode.LIFETIME via sget-object.
//
//   PATCH 2 — FallbackCacheFingerprint → return UnlockMode.LIFETIME
//     clearBody() removes the 24h timestamp comparison.
//     Returns UnlockMode.LIFETIME regardless of cache age or cached value.
//
//   LIFETIME chosen: isFull() = true, getShouldShowUpgrade() = false (no upgrade
//     CTA shown), isSubscription() = false. The highest tier — no upsell nags.
//
@Suppress("unused")
val compSuiteUnlockPatch = bytecodePatch(
    name = "Unlock Lifetime",
    description = "Unlocks WearOS Toolset by forcing both the Google Play Billing " +
        "purchase evaluator and the DataStore cache fallback to return " +
        "UnlockMode.LIFETIME, making isFull() return true and enabling all " +
        "premium complications and features in all network conditions.",
    default = true,
) {
    compatibleWith(COMPSUITE_COMPATIBILITY)

    dependsOn(compSuitePairIpPatch)

    execute {
        // PATCH 1 — evaluatePurchasesToUnlockMode() → LIFETIME
        // Original: iterates purchase list, returns LIFETIME/YEARLY/MONTHLY/OLD_SUB/FREE
        // Replacement: immediately returns UnlockMode.LIFETIME
        EvaluatePurchasesFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                sget-object v0, Lcom/gs/complications/suite/utils/UnlockMode;->LIFETIME:Lcom/gs/complications/suite/utils/UnlockMode;
                return-object v0
                """.trimIndent(),
            )
        }

        // PATCH 2 — fallbackToTimeBoundedCache() → LIFETIME
        // Original: returns cached UnlockMode if < 24h old, else the fallback (FREE)
        // Replacement: always returns UnlockMode.LIFETIME
        // This ensures the DataStore cache path never returns FREE at startup.
        FallbackCacheFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                sget-object v0, Lcom/gs/complications/suite/utils/UnlockMode;->LIFETIME:Lcom/gs/complications/suite/utils/UnlockMode;
                return-object v0
                """.trimIndent(),
            )
        }
    }
}
