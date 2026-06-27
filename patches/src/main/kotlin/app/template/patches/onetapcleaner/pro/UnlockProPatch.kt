package app.template.patches.onetapcleaner.pro

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.onetapcleaner.license.disablePairIPLicenseCheckPatch
import app.template.patches.onetapcleaner.gma.disableGmaPcamPatch
import app.template.patches.onetapcleaner.antitamper.disableAntiTamperPatch
import app.template.patches.shared.Constants
import app.template.patches.shared.clearBody

// 1Tap Cleaner's pro state is held in izo singleton (izo.灕):
//   izo.顩 = isPurchased (primary gate read by all feature checks)
//   izo.讟 = isPurchased copy (secondary gate)
//   izo.讌 = billing initialized
//   izo.蘣 = unused/credits
// All 4 are AtomicBoolean, initialized to false(0x0) in izo.<clinit>.
//
// The billing result handler afu.顩(Context, List) calls LicWnd.鬗(list) to find
// the "level1" purchase, then bjj.蘣() to check purchaseState==1.
// On a re-signed build Play Billing returns cert-mismatch → empty list →
// LicWnd.鬗 returns null → bjj.蘣() is NEVER CALLED → izo.顩 stays false.
//
// Fix: patch izo.<clinit> to initialize izo.顩 and izo.讟 to true(0x1) before
// any billing check runs. Feature gates read izo.顩.get() which returns true
// immediately on first access, regardless of billing outcome.
// Also no-op afu.顩 (billing handler) so it can't write false back over our true.
@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks 1Tap Cleaner PRO: history export, app-group filters, " +
        "unlimited cache targets, ad removal.",
    default = true,
) {
    dependsOn(disablePairIPLicenseCheckPatch, disableGmaPcamPatch, disableAntiTamperPatch)
    compatibleWith(Constants.ONETAPCLEANER_COMPATIBILITY)

    execute {
        // Patch izo.<clinit> — initialize izo.顩 and izo.讟 to true instead of false.
        // mutableClassDefBy("Lizo;") looks up by exact type in classMap — no string needed.
        val izoClass = mutableClassDefBy("Lizo;")

        val clinit = izoClass.methods.firstOrNull {
            it.name == "<clinit>" && it.returnType == "V" && it.parameterTypes.isEmpty()
        } ?: throw PatchException("1Tap Cleaner: izo.<clinit> not found.")

        // Rewrite <clinit>: same structure as original but all 4 AtomicBooleans init to true.
        // Registers: v0=izo instance, v1=AtomicBoolean, v2=true(1). Original used v2=0x0.
        clinit.clearBody()
        clinit.addInstructions(
            0,
            """
            new-instance v0, Lizo;
            invoke-direct {v0}, Ljava/lang/Object;-><init>()V
            const/4 v2, 0x1
            new-instance v1, Ljava/util/concurrent/atomic/AtomicBoolean;
            invoke-direct {v1, v2}, Ljava/util/concurrent/atomic/AtomicBoolean;-><init>(Z)V
            iput-object v1, v0, Lizo;->讌:Ljava/util/concurrent/atomic/AtomicBoolean;
            new-instance v1, Ljava/util/concurrent/atomic/AtomicBoolean;
            invoke-direct {v1, v2}, Ljava/util/concurrent/atomic/AtomicBoolean;-><init>(Z)V
            iput-object v1, v0, Lizo;->顩:Ljava/util/concurrent/atomic/AtomicBoolean;
            new-instance v1, Ljava/util/concurrent/atomic/AtomicBoolean;
            invoke-direct {v1, v2}, Ljava/util/concurrent/atomic/AtomicBoolean;-><init>(Z)V
            iput-object v1, v0, Lizo;->蘣:Ljava/util/concurrent/atomic/AtomicBoolean;
            new-instance v1, Ljava/util/concurrent/atomic/AtomicBoolean;
            invoke-direct {v1, v2}, Ljava/util/concurrent/atomic/AtomicBoolean;-><init>(Z)V
            iput-object v1, v0, Lizo;->讟:Ljava/util/concurrent/atomic/AtomicBoolean;
            sput-object v0, Lizo;->灕:Lizo;
            return-void
            """.trimIndent(),
        )

        // No-op afu.顩(Context, List)V — the billing result handler.
        // Without this it runs after <clinit>, gets empty purchase list, and writes
        // izo.顩 = false, overwriting our true. Pin by "my_apps" unique to afu.
        val afuClass = classDefByStrings("my_apps")
            .firstOrNull()
            ?: throw PatchException("1Tap Cleaner: billing handler class (afu) not found.")
        val mutableAfu = mutableClassDefBy(afuClass)

        val billingHandler = mutableAfu.methods.firstOrNull {
            it.returnType == "V" &&
                it.parameterTypes == listOf("Landroid/content/Context;", "Ljava/util/List;")
        } ?: throw PatchException("1Tap Cleaner: afu.顩(Context, List)V not found.")

        billingHandler.clearBody()
        billingHandler.addInstructions(0, "return-void")
    }
}
