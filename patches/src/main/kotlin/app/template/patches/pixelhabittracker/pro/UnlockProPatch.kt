package app.template.patches.pixelhabittracker.pro

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PIXEL_HABIT_TRACKER_COMPATIBILITY
import app.template.patches.shared.killPairIpFull

// Pixel Habit Tracker (com.pixel.al.pixelhabittracker) — Pro unlock v2
//
// hh2 = PurchaseRepository
//   field a:SharedPreferences  — "billing_prefs"
//   field c:Z                  — in-memory pro flag
//   field d:Lw43;              — MutableStateFlow<Boolean>
//
// w43.i(Object, Object)Z = compareAndSet(expect=null, update=newValue)
//
// FINGERPRINT FIX:
//   "habit_tracker_pro" is in method c() (line 857), NOT the constructor (ends at 390).
//   Constructor only has "billing_prefs" + "pro_purchased" → fixed fingerprint.
//
// THREE layers:
//
// Layer 1 — ProStateSetterFingerprint (hh2.f(Z)V):
//   Replace body: write true to SharedPrefs, set c=true, emit true to StateFlow.
//
// Layer 2 — PurchaseRepositoryConstructorFingerprint (hh2.<init>(Context)V):
//   Inject c=true at instruction 0 before getBoolean("pro_purchased", false).
//
// Layer 3 — PairIP LVL (com.pairip.licensecheck.LicenseClient):
//   Lightweight PairIP build (no VMRunner/SignatureCheck native lib).
//   LicenseContentProvider.onCreate() → LicenseClient.initializeLicenseCheck()
//   → Play LVL → NOT_LICENSED → paywall/exit on resigned APK.
//   killPairIpFull() handles the full LicenseClient chain gracefully.

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock PRO",
    description = "Unlocks all PRO features in app",
    default = true,
) {
    compatibleWith(PIXEL_HABIT_TRACKER_COMPATIBILITY)

    execute {
        // Layer 1: hh2.f(Z)V — always emit true
        ProStateSetterFingerprint
            .match(classDefBy(ProStateSetterFingerprint.definingClass!!))
            .method
            .apply {
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    const/4 p1, 0x1
                    iput-boolean p1, p0, Lhh2;->c:Z
                    iget-object v0, p0, Lhh2;->a:Landroid/content/SharedPreferences;
                    invoke-interface {v0}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences${'$'}Editor;
                    move-result-object v0
                    const-string v1, "pro_purchased"
                    invoke-interface {v0, v1, p1}, Landroid/content/SharedPreferences${'$'}Editor;->putBoolean(Ljava/lang/String;Z)Landroid/content/SharedPreferences${'$'}Editor;
                    move-result-object v0
                    invoke-interface {v0}, Landroid/content/SharedPreferences${'$'}Editor;->apply()V
                    iget-object v0, p0, Lhh2;->d:Lw43;
                    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
                    invoke-static {p1}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                    move-result-object p1
                    const/4 v1, 0x0
                    invoke-virtual {v0, v1, p1}, Lw43;->i(Ljava/lang/Object;Ljava/lang/Object;)Z
                    return-void
                    """.trimIndent(),
                )
            }

        // Layer 2: hh2.<init>(Context)V — pre-set c=true before getBoolean
        PurchaseRepositoryConstructorFingerprint
            .match(classDefBy(PurchaseRepositoryConstructorFingerprint.definingClass!!))
            .method
            .addInstructions(
                0,
                """
                const/4 v0, 0x1
                iput-boolean v0, p0, Lhh2;->c:Z
                """.trimIndent(),
            )

        // Layer 3: PairIP LVL kill
        killPairIpFull()
    }
}
