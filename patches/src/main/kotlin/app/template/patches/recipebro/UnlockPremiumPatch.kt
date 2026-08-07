package app.template.patches.recipebro

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.RECIPEBRO_COMPATIBILITY
import app.template.patches.shared.killPairIpFull
import app.template.patches.shared.returnEarly

// RecipeBro — Unlock Premium — v1.7.21
//
// Premium check architecture (fully traced):
//
//   Two parallel coroutine flows in pl0.invokeSuspend feed isPro state:
//
//   FLOW A (h16 → pl0 q=0 → state 0):
//     ne.a() → fetches kmp CustomerInfo from RC
//     → ne.d(CustomerInfo) → builds gt0
//     → gt0.a:Set<String> (activeSubscriptions)
//     → Collection.isEmpty() → xor → isPro = !isEmpty
//     → d83.setValue(isPro)
//
//   FLOW B (lr3 → pl0 q=1 → state 1):
//     y84.d() → fetches nh1 (FeatureFlagResponse) from server
//     → iget-boolean nh1.g:Z (showCookbookSharing field)
//     → d83.setValue(nh1.g)
//
//   Both flows write into the same d83 MutableStateFlow<Boolean> that drives
//   the entire UI premium state.
//
// Patch strategy — three layers covering both flows:
//
//   1. CustomerInfoConverterFingerprint (ne.d) — FLOW A fix:
//      Inject a non-empty singleton set into v1 at index 0 before
//      getActiveSubscriptions() runs. v1 is passed verbatim to gt0.<init>
//      as the activeSubscriptions parameter. gt0.a.isEmpty() is always false.
//
//   2. FeatureFlagResponseConstructorFingerprint (nh1.<init>) — FLOW B fix:
//      After the normal constructor assignments, overwrite all boolean fields
//      to true. nh1.g (showCookbookSharing) becomes true → pl0 state 1
//      emits true into d83. Also unlocks showCategories, showNewRecipes,
//      showMealPlan, and showPremiumAfterOnboarding as a side-effect.
//
//   3. EntitlementInfo.isActive() × 2 — belt-and-suspenders:
//      Both JVM and KMP EntitlementInfo.isActive() return true, covering any
//      code path that checks isActive directly rather than through the flows.
//
//   4. killPairIpFull() — Pairip LVL no-op.

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks RecipeBro premium by patching both CustomerInfo and FeatureFlagResponse premium gates.",
) {
    compatibleWith(RECIPEBRO_COMPATIBILITY)

    execute {

        // 1. FLOW A: inject non-empty activeSubscriptions set into ne.d() at index 2.
        //    ne.d() instruction layout:
        //      index 0: invoke-virtual {p0}, kmp CustomerInfo.getActiveSubscriptions()
        //      index 1: move-result-object v1   ← RC result (emptySet) lands in v1
        //      index 2: invoke-virtual {p0}, getAllPurchasedProductIdentifiers()  ← HERE
        //    Inserting at index 0 was wrong — move-result-object v1 at index 1
        //    immediately overwrote our injected singleton with the RC emptySet.
        //    Inserting at index 2 runs AFTER move-result-object v1, overwriting
        //    v1 with our singleton before it reaches gt0.<init>.
        //    v1 is then passed verbatim to gt0.<init> as the activeSubscriptions Set.
        CustomerInfoConverterFingerprint.method.addInstructions(
            2,
            """
                const-string v1, "pro"
                invoke-static { v1 }, Ljava/util/Collections;->singleton(Ljava/lang/Object;)Ljava/util/Set;
                move-result-object v1
            """.trimIndent(),
        )

        // 2. FLOW B: overwrite all nh1 boolean fields to true after construction.
        //    The constructor has .registers 11 and 11 params (p0..p10), leaving
        //    zero free local registers — v0 aliases p0 (this). Using v0 for
        //    const/4 would corrupt the this reference and cause VerifyError:
        //    "instance field access on object that has non-reference type".
        //    Safe fix: use p1 (the Z showPremiumAfterOnboarding param) as scratch
        //    — it has already been consumed by iput-boolean at instruction 0,
        //    and its boolean type is accepted by iput-boolean without issue.
        //    p0 (this=Lnh1;) is never touched.
        val lastIdx = FeatureFlagResponseConstructorFingerprint
            .method.implementation!!.instructions.lastIndex
        FeatureFlagResponseConstructorFingerprint.method.addInstructions(
            lastIdx,
            """
                const/4 p1, 0x1
                iput-boolean p1, p0, Lnh1;->a:Z
                iput-boolean p1, p0, Lnh1;->e:Z
                iput-boolean p1, p0, Lnh1;->f:Z
                iput-boolean p1, p0, Lnh1;->g:Z
                iput-boolean p1, p0, Lnh1;->h:Z
            """.trimIndent(),
        )

        // 3. Belt-and-suspenders: EntitlementInfo.isActive() both layers.
        EntitlementInfoIsActiveFingerprint.method.returnEarly(true)
        KmpEntitlementInfoIsActiveFingerprint.method.returnEarly(true)

        // 4. Pairip LVL.
        killPairIpFull()
    }
}
