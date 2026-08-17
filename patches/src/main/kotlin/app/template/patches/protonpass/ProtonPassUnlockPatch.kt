package app.template.patches.protonpass

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PROTON_PASS_COMPATIBILITY
import app.template.patches.shared.firebase.spoofFirebaseCertHashPatch
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.signature.spoofSignatureVerificationPatch

// Proton Pass: Password Manager (proton.android.pass) v1.40.3
//
// ROOT CAUSE OF PREVIOUS FAILURE
// The first patch attempt only forced vaultLimit/aliasLimit/totpLimit = Unlimited
// and hideUpgrade = true. This is INSUFFICIENT because:
//
//   1. Multiple UI components (ProfileViewModel, SecurityCenterHomeState, SelectItemViewModel,
//      AuthViewModel, ProfileBottomBarIcon etc.) read Plan.planType directly and do
//      instanceof checks (instance-of planType, PlanType$Paid) — bypassing UpgradeInfo entirely.
//      With planType still = PlanType$Free, all those checks return false → features locked.
//
//   2. CanPerformPaidActionImpl maps Plan → Flow<Boolean> via planType instanceof PlanType$Paid.
//      PaidFeature gates (DarkWebMonitoring, ItemSharing, SecureLinks, etc.) go through this,
//      not through UpgradeInfo.hasReachedLimit.
//
// TWO GATE SYSTEMS — BOTH MUST BE PATCHED:
//
//   System A — quantity/limit gates (UpgradeInfo.hasReachedLimit):
//     UpgradeInfo(isUpgradeAvailable = !hideUpgrade && !isPaidPlan, ...)
//     hasReachedLimit(planLimit, count):
//       if (!isUpgradeAvailable) return false  ← skip limit check
//     → forcing hideUpgrade=true makes isUpgradeAvailable=false → hasReachedLimit always false ✓
//
//   System B — feature access gates (instanceof PlanType$Paid):
//     ProfileViewModel, SelectItemViewModel, SecurityCenterHomeState, AuthViewModel etc.
//     all read plan.planType directly and do: instance-of planType, PlanType$Paid
//     → forcing planType = PlanType$Paid$Plus makes all these checks return true ✓
//
// COMPLETE FIX — Plan.<init>() injection at index 1:
//
//   Plan.<init> parameters:
//     p0 = this
//     p1 = planType:Credential       → REPLACED with PlanType$Paid$Plus("pass2023", "Proton Pass Plus")
//     p2 = hideUpgrade:Z             → FORCED to 0x1 (true)
//     p3 = vaultLimit:PlanLimit      → REPLACED with PlanLimit$Unlimited.INSTANCE
//     p4 = aliasLimit:PlanLimit      → REPLACED with PlanLimit$Unlimited.INSTANCE
//     p5 = totpLimit:PlanLimit       → REPLACED with PlanLimit$Unlimited.INSTANCE
//     p6-p7 = updatedAt:J            → UNTOUCHED
//
//   .registers 8 → 0 free locals (no v0..vN available).
//   Strategy: reuse p3/p4/p5 as scratch for PlanType$Paid$Plus construction, then restore.
//
//   Step 1 — build PlanType$Paid$Plus in p3 (using p4, p5 for string args):
//     new-instance p3, PlanType$Paid$Plus
//     const-string p4, "pass2023"         (internalName — Proton's plan key)
//     const-string p5, "Proton Pass Plus" (displayName)
//     invoke-direct {p3,p4,p5}, PlanType$Paid$Plus-><init>(String,String)V
//     move-object p1, p3                  (replace planType param)
//
//   Step 2 — restore limit params and set hideUpgrade:
//     sget-object p3, PlanLimit$Unlimited->INSTANCE  (restore vaultLimit)
//     sget-object p4, PlanLimit$Unlimited->INSTANCE  (restore aliasLimit)
//     sget-object p5, PlanLimit$Unlimited->INSTANCE  (restore totpLimit)
//     const/4 p2, 0x1                                (force hideUpgrade=true)
//
//   After injection, the constructor proceeds normally:
//     iput-object p1 → planType = PlanType$Paid$Plus ✓
//     iput-boolean p2 → hideUpgrade = true ✓
//     iput-object p3 → vaultLimit = Unlimited ✓
//     iput-object p4 → aliasLimit = Unlimited ✓
//     iput-object p5 → totpLimit  = Unlimited ✓
//     instance-of p2, p1, PlanType$Paid → p2 = 1 ✓ (Paid$Plus extends Paid)
//     iput-boolean p2 → isPaidPlan = true ✓
//     instance-of p2, p1, PlanType$Free → p2 = 0 ✓
//     iput-boolean p2 → isFreePlan = false ✓
//
//   PlanType$Paid$Plus → PlanType$Paid → Credential — assignment to planType:Credential is valid.
//
// SMALI VERIFIED (classes4.dex, v1.40.3):
//   Plan.<init>: .registers 8, super = Object, planType field type = Credential
//   PlanType$Paid$Plus.<init>(String, String): public constructor ✓
//   PlanType$Paid$Plus.super = PlanType$Paid.super = Credential ✓
//   PlanLimit$Unlimited.INSTANCE: public static final singleton ✓

@Suppress("unused")
val protonPassUnlockPatch = bytecodePatch(
    name = "Unlock Unlimited Plan",
    description = "Forces Proton Pass Plus plan with all limits removed and paid features unlocked.",
    default = true,
) {
    compatibleWith(PROTON_PASS_COMPATIBILITY)

    dependsOn(
        spoofSignatureVerificationPatch,
        spoofInstallSourcePatch,
        spoofFirebaseCertHashPatch,
    )

    execute {
        PlanConstructorFingerprint.method.addInstructions(
            1,
            """
                # Step 1: construct PlanType${'$'}Paid${'$'}Plus using p3/p4/p5 as scratch
                new-instance p3, Lproton/android/pass/domain/PlanType${'$'}Paid${'$'}Plus;
                const-string p4, "pass2023"
                const-string p5, "Proton Pass Plus"
                invoke-direct {p3, p4, p5}, Lproton/android/pass/domain/PlanType${'$'}Paid${'$'}Plus;-><init>(Ljava/lang/String;Ljava/lang/String;)V
                move-object p1, p3
                # Step 2: restore limit params and force hideUpgrade
                sget-object p3, Lproton/android/pass/domain/PlanLimit${'$'}Unlimited;->INSTANCE:Lproton/android/pass/domain/PlanLimit${'$'}Unlimited;
                sget-object p4, Lproton/android/pass/domain/PlanLimit${'$'}Unlimited;->INSTANCE:Lproton/android/pass/domain/PlanLimit${'$'}Unlimited;
                sget-object p5, Lproton/android/pass/domain/PlanLimit${'$'}Unlimited;->INSTANCE:Lproton/android/pass/domain/PlanLimit${'$'}Unlimited;
                const/4 p2, 0x1
            """.trimIndent(),
        )
    }
}
