package app.template.patches.tasker

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TASKER_COMPATIBILITY
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation

/**
 * Unlock Tasker (6.6.20+)
 *
 * ## Layer 0 — rf.w.z() forced to always call S() (Licensed)
 *
 * `rf.w.z(ZZZ)Single` is the main license check. It normally calls
 * `Kid.b(Context)Z` and if true, invokes `S()` which returns Licensed.
 * We replace z() with a direct call to S() so licensing always resolves
 * to Licensed — without touching Kid.b() globally.
 *
 * **Why not patch Kid.b()?**
 * Kid.b() returning true globally activates Tasker's "Kid mode" code paths
 * which try to load `kid/data.xml` from assets. That file is only present
 * in official Play Store builds. In modded APKs it's absent, causing cascading
 * NPEs and IOOBE crashes across MonitorService, TaskerApp, Kid.j, ExecuteService,
 * etc. Patching z() directly avoids this entirely.
 *
 * ## Layer 1 — Licence.w0() forced to true
 *
 * Prevents the "no stored key" branch from showing an invalid-state dialog.
 *
 * ## Layer 2 — MonitorService.P3() returns "cust_notification"
 *
 * P3 reads notification icon name. Its Kid.b()=true branch tries tn.v(0)
 * on an empty list — crash. The false-branch hardcodes "cust_notification"
 * which is safe. We short-circuit to always return that string.
 *
 * @package net.dinglisch.android.taskerm
 */
@Suppress("unused")
val taskerUnlockPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks Tasker Pro features.",
    default = true
) {
    compatibleWith(TASKER_COMPATIBILITY)

    execute {

        fun clearTryBlocks(implementation: MutableMethodImplementation) {
            val field = MutableMethodImplementation::class.java.getDeclaredField("tryBlocks")
            field.isAccessible = true
            (field.get(implementation) as MutableList<*>).clear()
        }

        fun replaceMethod(fp: app.morphe.patcher.Fingerprint, body: String) {
            val method = fp.method
            method.removeInstructions(0, method.instructions.count())
            clearTryBlocks(method.implementation!!)
            method.addInstructions(0, body)
        }

        // Layer 0: rf.w.z() → always call S() → always Licensed
        // S() is a private instance method so we invoke-direct on `this` (p0)
        replaceMethod(LicenseCheckZFingerprint, """
            invoke-direct {p0}, Lrf/w;->S()Lio/reactivex/Single;
            move-result-object p0
            return-object p0
        """.trimIndent())

        // Layer 1: Licence.w0() → true (skip "no key" dialog)
        replaceMethod(LicenceHasKeyFingerprint, "const/4 v0, 0x1\nreturn v0")

        // Layer 2: MonitorService.P3() → "cust_notification" (skip tn.v(0) crash)
        replaceMethod(MonitorServiceP3Fingerprint, """
            const-string p0, "cust_notification"
            return-object p0
        """.trimIndent())
    }
}