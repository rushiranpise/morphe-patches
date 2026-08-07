package app.template.patches.oxygenupdater

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// The "contribute" boolean (free purchase) is stored in TWO separate locations:
//   1. SharedPreferences key: "contribute"
//   2. Jetpack DataStore key: "contribute" (separate store)
// Both gate ad display independently and must both be patched.
//
// ADDITIONALLY the "remove ads" IAP dialog visibility is controlled by a SEPARATE key:
//   SharedPreferences key: "34ejrtgalsJKDf;awljker;2k3jrpwosKjdfpio24uj3tp3oiwfjdscPOKj"
// Read by d60 (SharedPrefs → XOR → StateFlow) and y35 (DataStore → i62 → branch).
// Both paths must also be patched to hide the purchase dialog.

// ── 1. Contribute SharedPrefs write ─────────────────────────────────────────
// xu0.a(Context, boolean): V — writes SharedPrefs["contribute"] from billing callback.
// Patched: const/4 p2, 0x1 at index 0 → always writes TRUE.
internal val ContributeWriteFingerprint = Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/content/Context;", "Z"),
    filters = listOf(
        string("contribute"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences\$Editor;",
            name = "putBoolean"
        )
    )
)

// ── 2. Contribute DataStore read (ad gate) ───────────────────────────────────
// t93.d(...): V — Compose ad composable reads DataStore["contribute"] via Li62;->invoke.
// contribute=FALSE → ads shown. contribute=TRUE → ads skipped (:L13).
// Patched: const/4 v6, 0x1 at instructionMatches[2].index+2 (after booleanValue move-result)
// → branch always goes to :L13 (no-ads path).
//
// Smali: classes/t93.smali, method d at line 5801
//   instr[57]: invoke-virtual {v6}, Boolean->booleanValue()Z
//   instr[58]: move-result v6
//   instr[59]: if-nez v6, :L13   ← inject const/4 v6,0x1 HERE (at index 59)
internal val ContributeDataStoreReadFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.STATIC),
    returnType = "V",
    filters = listOf(
        string("contribute"),
        methodCall(definingClass = "Li62;", name = "invoke"),
        methodCall(definingClass = "Ljava/lang/Boolean;", name = "booleanValue")
    )
)

// ── 3. IAP dialog SharedPrefs read (d60 constructor) ────────────────────────
// d60 constructor reads SharedPrefs["34ejrt..."] (IAP purchased flag) and XORs with 1.
// XOR result feeds a StateFlow: 1=show IAP dialog, 0=hide it.
// XOR(FALSE, 1) = 1 → shows dialog (not purchased).
// XOR(TRUE,  1) = 0 → hides dialog (purchased).
// Patched: const/4 p1, 0x1 after getBoolean (before XOR) → XOR(1,1)=0 → hides dialog.
//
// Smali: classes/d60.smali, constructor at line 37
//   string "oxygen_updater_ad_free" (product SKU) appears first (line 59)
//   then string "34ejrt..." (purchase flag key, line 335)
//   then getBoolean call (line 338) → move-result p1 → xor-int p1,v1
// Filter order: string(SKU) → string(key) → methodCall(getBoolean) → methodCall(valueOf)
// No accessFlags specified — CONSTRUCTOR flag coexists with PUBLIC and varies by patcher version.
internal val IapDialogVisibilityFingerprint = Fingerprint(
    name = "<init>",
    parameters = listOf(
        "Landroid/content/SharedPreferences;",
        "Lx50;",
        "Lf15;"
    ),
    filters = listOf(
        string("oxygen_updater_ad_free"),
        string("34ejrtgalsJKDf;awljker;2k3jrpwosKjdfpio24uj3tp3oiwfjdscPOKj"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getBoolean"
        ),
        methodCall(
            definingClass = "Ljava/lang/Boolean;",
            name = "valueOf"
        )
    )
)

// ── 4. IAP dialog DataStore read (y35.i) ────────────────────────────────────
// y35.i(Z, s52, i62, ap0, int): V — Compose composable reads DataStore["34ejrt..."]
// via Li62;->invoke. If result=FALSE (v1=0) → if-eqz taken → :L11 → renders "AdsClick" ad.
// If TRUE (v1=1) → if-eqz NOT taken → falls through → ad not rendered.
// Patch: inject const/4 v1, 0x1 at booleanValue.index+2 (AFTER move-result v1 at +1,
// BEFORE if-eqz v1,:L11 at +2). Must be +2 not +1: move-result must immediately
// follow its invoke or the verifier rejects the class with VerifyError type=Undefined.
//
// Smali: classes/y35.smali, method i at line 11578
//   instr[54]: invoke-virtual {v1}, Boolean->booleanValue()Z   ← instructionMatches[2]
//   instr[55]: move-result v1                                   ← +1: DO NOT inject here
//   instr[56]: if-eqz v1, :L11                                 ← +2: inject here ✓
internal val IapDialogDataStoreReadFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.STATIC),
    returnType = "V",
    filters = listOf(
        string("34ejrtgalsJKDf;awljker;2k3jrpwosKjdfpio24uj3tp3oiwfjdscPOKj"),
        methodCall(definingClass = "Li62;", name = "invoke"),
        methodCall(definingClass = "Ljava/lang/Boolean;", name = "booleanValue")
    )
)

// ── 5. IAP button StateFlow emitter (v50.emit) ──────────────────────────────
// v50.emit(Object, Continuation): Object is the central billing-state Flow emitter.
// It reads SharedPrefs["34ejrt..."] (current purchase state) and:
//   1. Writes it back with the new purchase result via putBoolean.
//   2. XOR's the result with 1 (inverts it) → feeds the UI StateFlow.
// StateFlow semantics (inverted): XOR(FALSE,1)=1=TRUE → shows "buy now" button.
//                                 XOR(TRUE, 1)=0=FALSE → hides "buy now" button.
//
// Even after patching d60 (dialog) and y35.i (ad banner), the "Contribute/Remove Ads"
// BUTTON in Settings is driven by THIS StateFlow. Without patching v50.emit(), the
// button always shows because the initial SharedPrefs["34ejrt..."]=FALSE.
//
// Patch strategy (two injections, applied reverse-index order):
//   A) const/4 v0, 0x1 at putBoolean.index (instructionMatches[2].index)
//      → always writes TRUE to SharedPrefs → cached state becomes "purchased".
//   B) const/4 v0, 0x0 at xor.index+1 = putBoolean.index+3 (after xor-int/2addr)
//      → overrides XOR result to 0=FALSE → StateFlow emits FALSE → hides button.
//
// Smali: classes/v50.smali, method emit at line 350
//   instr[665]: const-string v1, "34ejrt..."             ← filter 0
//   instr[668]: invoke-interface getBoolean(key, false)Z  ← filter 1 → [1].index=668
//   instr[669]: move-result v0
//   instr[670]: goto :L117
//   instr[674]: iget-object v2, x50->a (SharedPrefs)
//   instr[677]: putBoolean(v1, v0)                       ← filter 2 → [2].index=677
//   instr[678]: apply()
//   instr[679]: xor-int/2addr v0, v13                   ← [2].index+2=679
//   instr[680]: valueOf(v0)
// Inject A at [677] → new layout: [677]=const, [678]=putBoolean, [679]=apply, [680]=xor
// Inject B at [679]+1+1=[681] (high-to-low, so B first at old[679]+2=681, then A at 677)
// Actually reverse order: inject B at old[679]+2=681, shift, then A at old[677]=677.
// Cleaner: both injections together after computing indices from matches.
internal val IapStateFlowEmitFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;", "Lsu0;"),
    filters = listOf(
        string("34ejrtgalsJKDf;awljker;2k3jrpwosKjdfpio24uj3tp3oiwfjdscPOKj"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getBoolean"
        ),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences\$Editor;",
            name = "putBoolean"
        )
    )
)
