package app.template.patches.onetapcleaner.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants

// Internal: applied automatically as a dependency of Unlock Pro.
@Suppress("unused")
val disableAdsPatch = bytecodePatch(
    description = "Forces the isPro ad-suppressor flag (ipi.f25332) to true on every call " +
        "to the purchase-verification method, bypassing the APK signature hash check that " +
        "prevents the flag from being set on re-signed builds. " +
        "Without this, bxt.m4114() returns true (show ads) even when BillingManager.m5236() " +
        "returns true, because f25332 is never written by cyq.m5937() on a re-signed APK.",
) {
    compatibleWith(Constants.ONETAPCLEANER_COMPATIBILITY)

    execute {
        // cyq.m5937(Context, List)V — the sole writer of ipi.f25332.
        //
        // Original flow:
        //   1. Signature hash check → bail if mismatch (always bails on re-signed builds)
        //   2. Set ipi.f25330.f25332 = (validPurchase != null)
        //
        // Patched: inject at index 0 to set f25332 = true unconditionally, then let the
        // original method continue (it returns void, so no register conflict).
        // The signature check still runs but its result no longer matters — f25332 is
        // already true before any check executes.
        //
        // Smali injected:
        //   sget-object v0, Ldefpackage/ipi;->f25330:Ldefpackage/ipi;
        //   iget-object v0, v0, Ldefpackage/ipi;->f25332:Ljava/util/concurrent/atomic/AtomicBoolean;
        //   const/4 v1, 0x1
        //   invoke-virtual {v0, v1}, Ljava/util/concurrent/atomic/AtomicBoolean;->set(Z)V
        //
        // ipi and its fields ARE obfuscated — we locate them at patch time via
        // the fingerprint's resolved method, which is inside cyq whose static initializer
        // references ipi directly. We inject raw smali using the class/field descriptors
        // found statically from the decompiled source.
        //
        // ipi descriptor: Ldefpackage/ipi;  (defpackage — stable between minor versions
        //   since it is the catch-all obfuscation package for this app)
        // f25330: static final ipi  (the singleton)
        // f25332: AtomicBoolean     (the isPro ad flag)
        //
        AdStateFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Ldefpackage/ipi;->f25330:Ldefpackage/ipi;
                iget-object v0, v0, Ldefpackage/ipi;->f25332:Ljava/util/concurrent/atomic/AtomicBoolean;
                const/4 v1, 0x1
                invoke-virtual {v0, v1}, Ljava/util/concurrent/atomic/AtomicBoolean;->set(Z)V
            """.trimIndent(),
        )
    }
}
