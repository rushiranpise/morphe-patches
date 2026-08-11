package app.template.patches.oxygenupdater

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.OXYGENUPDATER_COMPATIBILITY

/**
 * Oxygen Updater — Contributor / Ad-Free Unlock
 *
 * App: Oxygen Updater (com.arjanvlek.oxygenupdater) v7.1.0 (versionCode 135)
 * Billing: Google Play Billing, one-time purchase SKU "oxygen_updater_ad_free"
 * No pairip.
 *
 * The "contributor" state is spread across FIVE separate gate sites:
 *
 * ── Gate 1: xu0.a() — SharedPrefs["contribute"] write ──────────────────────
 * Called by billing callback with purchase result boolean.
 * Patch: const/4 p2, 0x1 at index 0 → always writes TRUE.
 *
 * ── Gate 2: t93.d() — DataStore["contribute"] read (ad compositor) ──────────
 * Compose ad slot. contribute=FALSE → ads loaded. TRUE → ads skipped (:L13).
 * XOR applied after branch: FALSE path → v6=1 → XOR(1,1)=0 → hides ads.
 * Patch: const/4 v6, 0x1 at booleanValue.index+2 → always takes :L13 (no-ads).
 *
 * ── Gate 3: d60 constructor — SharedPrefs["34ejrt..."] XOR → dialog StateFlow
 * XOR(FALSE,1)=1 → shows "remove ads" dialog. XOR(TRUE,1)=0 → hides it.
 * Patch: const/4 p1, 0x1 at getBoolean.index+2 → XOR(1,1)=0 → dialog hidden.
 *
 * ── Gate 4: y35.i() — DataStore["34ejrt..."] → ad banner composable ─────────
 * isPurchased=FALSE → if-eqz taken → :L11 → "AdsClick" ad shown.
 * Patch: const/4 v1, 0x1 at booleanValue.index+2 → if-eqz not taken → no ad.
 * (index+2 not +1: move-result must immediately follow its invoke per Dalvik verifier)
 *
 * ── Gate 5: v50.emit() — SharedPrefs["34ejrt..."] read/write + XOR → button StateFlow
 * Central billing-state emitter. Reads current SharedPrefs state, writes new state,
 * XOR's result (inverted): XOR(FALSE,1)=1=TRUE → "Contribute/Remove Ads" button shown.
 * Even after patching gates 1-4, this StateFlow drives the Settings button visibility.
 * Two injections (applied in reverse index order):
 *   A) const/4 v0, 0x1 before putBoolean → always writes TRUE to SharedPrefs cache.
 *   B) const/4 v0, 0x0 after xor-int → overrides XOR result to 0=FALSE → hides button.
 */
@Suppress("unused")
val oxygenUpdaterUnlockPatch = bytecodePatch(
    name = "Oxygen Updater Contributor",
    description = "Removes ads and hides the purchase button by patching all five " +
            "contribute-flag gate sites across SharedPreferences, DataStore, and StateFlow.",
    default = true
) {
    compatibleWith(OXYGENUPDATER_COMPATIBILITY)

    execute {
        // Gate 1: xu0.a() — always write contribute=TRUE to SharedPreferences.
        ContributeWriteFingerprint.method.addInstruction(0, "const/4 p2, 0x1")

        // Gate 2: t93.d() — DataStore contribute read → always take premium (no-ads) path.
        // booleanValue=[57], move-result v6=[58], if-nez v6,:L13=[59]
        // Inject at [57]+2=59: after move-result, before if-nez.
        ContributeDataStoreReadFingerprint.apply {
            val boolIdx = instructionMatches[2].index
            method.addInstruction(boolIdx + 2, "const/4 v6, 0x1")
        }

        // Gate 3: d60 constructor — always hide "remove ads" dialog.
        // getBoolean=[94], move-result p1=[95], xor-int p1,v1=[96]
        // Inject const/4 p1,0x1 at [94]+2=96 (before xor) → XOR(1,1)=0 → hidden.
        IapDialogVisibilityFingerprint.apply {
            val getBoolIdx = instructionMatches[2].index
            method.addInstruction(getBoolIdx + 2, "const/4 p1, 0x1")
        }

        // Gate 4: y35.i() — DataStore IAP key → always skip "AdsClick" ad banner.
        // booleanValue=[54], move-result v1=[55], if-eqz v1,:L11=[56]
        // Inject const/4 v1,0x1 at [54]+2=56: after move-result, before if-eqz.
        // NOTE: must be +2 not +1 — Dalvik verifier requires move-result immediately
        // after invoke; inserting between them causes VerifyError type=Undefined.
        IapDialogDataStoreReadFingerprint.apply {
            val boolIdx = instructionMatches[2].index
            method.addInstruction(boolIdx + 2, "const/4 v1, 0x1")
        }

        // Gate 5: v50.emit() — always write purchased=TRUE and emit FALSE to StateFlow.
        // This drives the "Contribute/Remove Ads" button in Settings.
        // instructionMatches:
        //   [0] = string "34ejrt..."               → index 665
        //   [1] = SharedPreferences.getBoolean()   → index 668
        //   [2] = SharedPreferences$Editor.putBoolean() → index 677
        //
        // Layout after getBoolean: move-result v0[669] → goto :L117[670] →
        //   :L117 lands at iget[674] → editor.edit()[675] → move-result[676] →
        //   putBoolean(v0)[677] → apply()[678] → xor-int v0,v13[679] → valueOf[680]
        //
        // Injection A (at old[677]): const/4 v0,0x1 → putBoolean writes TRUE.
        // Injection B (at old[679]+2=681 after A shifts indices by 1): const/4 v0,0x0
        //   → overrides XOR result to 0=FALSE → valueOf(FALSE) → StateFlow hides button.
        // Apply B first (higher index) then A (lower index) to keep indices valid.
        IapStateFlowEmitFingerprint.apply {
            val putBoolIdx = instructionMatches[2].index   // 677
            val xorIdx     = putBoolIdx + 2               // 679 (apply is at putBool+1)

            // B: inject const/4 v0,0x0 after xor-int (at xorIdx+1+1 because A shifts by 1)
            //    But apply B first (higher index) before A shifts things.
            method.addInstruction(xorIdx + 1, "const/4 v0, 0x0")   // now xor is at 679, this is 680
            // A: inject const/4 v0,0x1 before putBoolean
            method.addInstruction(putBoolIdx, "const/4 v0, 0x1")
        }
    }
}
