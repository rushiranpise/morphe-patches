package app.template.patches.protonpass

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// Proton Pass: Password Manager (proton.android.pass) v1.40.3
//
// PLAN ARCHITECTURE
// Plans are server-side objects fetched via Proton API and cached in Room DB (PlanEntity).
// The plan flow: PlanRepositoryImpl → PlanEntity (Room) → coroutine mapper → Plan (domain object)
//
// Plan domain object (Lproton/android/pass/domain/Plan;) fields:
//   planType:    PlanType$Paid$Plus | PlanType$Paid$Business | PlanType$Free | PlanType$Unknown
//   hideUpgrade: Z — when true, suppresses all upsell/upgrade UI throughout the app
//   vaultLimit:  PlanLimit (Unlimited singleton | Limited(n))
//   aliasLimit:  PlanLimit (Unlimited singleton | Limited(n))
//   totpLimit:   PlanLimit (Unlimited singleton | Limited(n))
//   updatedAt:   J (epoch timestamp)
//   isFreePlan, isPaidPlan, isBusinessPlan: Z — derived from planType in constructor
//
// PlanLimit variants:
//   PlanLimit$Unlimited — singleton, limitOrNull() returns null → "no cap"
//   PlanLimit$Limited   — limitOrNull() returns Integer(n)
//
// Capability checks (e.g. CanCreateVaultImpl) compare vaultLimit to PlanLimit$Unlimited.INSTANCE
// or read PlanLimit$Limited.limit:I directly via instanceof + iget.
// Patching limitOrNull() alone is NOT sufficient — callers use instanceof + direct field read.
//
// PATCH STRATEGY — Plan.<init>()V
//   Inject 4 instructions at index 1 (after super.<init>(), before any iput):
//   sget-object p3, PlanLimit$Unlimited->INSTANCE   → vaultLimit = Unlimited
//   sget-object p4, PlanLimit$Unlimited->INSTANCE   → aliasLimit = Unlimited
//   sget-object p5, PlanLimit$Unlimited->INSTANCE   → totpLimit  = Unlimited
//   const/4 p2, 0x1                                → hideUpgrade = true
//   Uses param registers only — .registers 8 means 0 free locals, so no heap allocs.
//   planType is NOT changed; isFreePlan/isPaidPlan reflect actual account state.
//   All limits become Unlimited regardless of what the server returned.
//
// SMALI VERIFIED (classes4.dex, v1.40.3):
//   .class public final Lproton/android/pass/domain/Plan;
//   .source "SourceFile"  (R8-stripped source name)
//   .method public constructor <init>(Landroidx/credentials/Credential;ZLproton/android/pass/domain/PlanLimit;Lproton/android/pass/domain/PlanLimit;Lproton/android/pass/domain/PlanLimit;J)V
//   .registers 8
//   [0] invoke-direct {p0}, Object-><init>()V       ← super()
//   [1] iput-object p1, p0, Plan->planType          ← inject HERE (index 1)
//   [2] iput-boolean p2, p0, Plan->hideUpgrade
//   [3] iput-object p3, p0, Plan->vaultLimit
//   [4] iput-object p4, p0, Plan->aliasLimit
//   [5] iput-object p5, p0, Plan->totpLimit
//   [6] iput-wide p6, p0, Plan->updatedAt
//   [7] instance-of p2, p1, PlanType$Paid$Business
//   [8] iput-boolean p2, p0, Plan->isBusinessPlan
//   [9] instance-of p2, p1, PlanType$Free
//   [10] iput-boolean p2, p0, Plan->isFreePlan
//   [11] instance-of p2, p1, PlanType$Paid
//   [12] iput-boolean p2, p0, Plan->isPaidPlan
//   [13] iget-object p1, p1, Credential->type
//   [14] check-cast p1, String
//   [15] iput-object p1, p0, Plan->internalName
//   [16] return-void
//
// FINGERPRINT ANCHORS — fully non-obfuscated:
//   definingClass = "Lproton/android/pass/domain/Plan;" (app's own domain model, stable)
//   name = "<init>" (the constructor we need to modify)
//   parameters match the exact constructor signature (unique within the class)
//   No filters needed — only one constructor has this parameter list.
internal val PlanConstructorFingerprint = Fingerprint(
    definingClass = "Lproton/android/pass/domain/Plan;",
    name = "<init>",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    parameters = listOf(
        "Landroidx/credentials/Credential;",
        "Z",
        "Lproton/android/pass/domain/PlanLimit;",
        "Lproton/android/pass/domain/PlanLimit;",
        "Lproton/android/pass/domain/PlanLimit;",
        "J",
    ),
)
