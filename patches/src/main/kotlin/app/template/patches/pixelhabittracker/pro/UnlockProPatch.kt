package app.template.patches.pixelhabittracker.pro

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PIXEL_HABIT_TRACKER_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.template.patches.shared.killPairIpFull

// Pixel Habit Tracker pro unlock — v2.2.2
//
// Pro state is managed by the PurchaseRepository class (obfuscated name changes
// every update; was hh2 in v2.1.1, lk2 in v2.2.2). It holds:
//   field a : SharedPreferences  — "billing_prefs" prefs file
//   field c : Z                  — in-memory pro boolean flag
//   field d : MutableStateFlow<Boolean> — (obf name changes; was w43, now k83)
//
// The UI observes field d (the StateFlow) to react to pro-state changes.
// SharedPrefs is the persistence layer — read in <init>, written in f(Z)V.
//
// Two patch layers cover the full lifetime:
//
//   Layer 1 — Constructor injection (PurchaseRepositoryConstructorFingerprint):
//     Injects c = true at offset 0, before getBoolean("pro_purchased", false)
//     runs. Ensures the in-memory flag is true from first construction regardless
//     of what's in SharedPrefs.
//
//   Layer 2 — Setter override (ProStateSetterFingerprint):
//     Replaces the body of f(Z)V to always write true to SharedPrefs, set c = true,
//     and emit true to the StateFlow. Handles live billing-client callbacks.
//     clearBody() is mandatory: the original body has no try-catch, but we clear
//     it for safety and to avoid double-patching concerns.
//
//   Layer 3 — Pairip LVL (killPairIpFull):
//     This app ships the LicenseClient-based Pairip variant (no VMRunner /
//     libpairipcore.so). killPairIpFull() uses mutableClassDefByOrNull so it
//     gracefully skips absent VMRunner/SignatureCheck classes while still
//     no-oping LicenseClient.initializeLicenseCheck().

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock PRO",
    description = "Unlocks all PRO features by permanently reporting a purchased state.",
    default = true,
) {
    compatibleWith(PIXEL_HABIT_TRACKER_COMPATIBILITY)

    execute {
        // Layer 1: Pre-set c = true in constructor before SharedPrefs read.
        // Registers: p0 = this, p1 = Context. v0 is the first scratch register.
        PurchaseRepositoryConstructorFingerprint.method.addInstructions(
            0,
            """
            const/4 v0, 0x1
            iput-boolean v0, p0, ${PurchaseRepositoryConstructorFingerprint.classDef.type}->c:Z
            """.trimIndent(),
        )

        // Layer 2: Replace f(Z)V body — always force-write true.
        // The StateFlow emit call uses Lk83;->i(Object;Object;)Z (compareAndSet).
        // We replicate the original flow with p1 hardcoded to 0x1 (true) so the
        // UI observer immediately sees a pro state change.
        val repoType = ProStateSetterFingerprint.classDef.type
        ProStateSetterFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                const/4 p1, 0x1
                iput-boolean p1, p0, $repoType->c:Z
                iget-object v0, p0, $repoType->a:Landroid/content/SharedPreferences;
                invoke-interface {v0}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences${'$'}Editor;
                move-result-object v0
                const-string v1, "pro_purchased"
                invoke-interface {v0, v1, p1}, Landroid/content/SharedPreferences${'$'}Editor;->putBoolean(Ljava/lang/String;Z)Landroid/content/SharedPreferences${'$'}Editor;
                move-result-object v0
                invoke-interface {v0}, Landroid/content/SharedPreferences${'$'}Editor;->apply()V
                invoke-static {p1}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                move-result-object p1
                iget-object v0, p0, $repoType->d:${ProStateSetterFingerprint.classDef.fields.first { it.name == "d" }.type}
                invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
                const/4 v1, 0x0
                invoke-virtual {v0, v1, p1}, ${ProStateSetterFingerprint.classDef.fields.first { it.name == "d" }.type}->i(Ljava/lang/Object;Ljava/lang/Object;)Z
                return-void
                """.trimIndent(),
            )
        }

        // Layer 3: Kill Pairip LVL check.
        killPairIpFull()
    }
}
