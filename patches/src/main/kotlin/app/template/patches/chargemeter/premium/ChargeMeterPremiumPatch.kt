package app.template.patches.chargemeter.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CHARGEMETER_COMPATIBILITY
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

// Charge Meter v2.9.7 — SharedPreferences premium bypass + Pairip license bypass.
//
// PREMIUM MODEL
// No ViewModel/cache — every gate independently calls:
//   getSharedPreferences("PremiumPreference", 0).getBoolean("premium_user", false)
//
// VERIFYERROR HISTORY
// Injecting into MainActivity.onCreate() caused VerifyError because:
// - v13/v14/v15: verifier sees v14+v15 as wide pair used later → v15=Undefined
// - v0/v1/v2:   const-string at [0x0] then move-result-object at [0x3]; verifier
//   rejects because byte offset [0x3] is still inside the const-string encoding.
//   Root cause: addInstructions(0,...) inserts BEFORE the first instruction, and
//   the verifier's byte-offset tracking conflicts with the large method's type state.
//
// SOLUTION: inject into App.onCreate() (the Application subclass) instead.
// - Runs before any Activity onCreate — persists premium_user=true to disk.
// - Small method (.registers 5), simple body, no wide types, no type conflicts.
// - Inject at index 1 (after invoke-super) using v0/v1/v2 (immediately overwritten).
// - All subsequent getSharedPreferences reads in x7/j, x7/o, widgets etc return true.
//
// PATCH TARGETS
// 1. App.onCreate()      — write premium_user=true at startup (covers all read sites)
// 2. SettingsActivity.onResume() — override getBoolean result for GET PREMIUM card
// 3. ProfileActivity.u(Z)V      — force write-path to always persist true
// 4. LicenseClient.processResponse() — skip Pairip license check

@Suppress("unused")
val chargeMeterPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Charge Meter premium features.",
) {
    compatibleWith(CHARGEMETER_COMPATIBILITY)

    execute {
        // 1. App.onCreate() — inject SharedPrefs write at index 1 (after invoke-super).
        //    .registers 5: p0=this, v0..v3 non-wide locals, all free after invoke-super.
        //    Persists premium_user=true to disk before any Activity starts.
        AppOnCreateFingerprint.method.addInstructions(
            1,
            """
                const-string v0, "PremiumPreference"
                const/4 v1, 0x0
                invoke-virtual { p0, v0, v1 }, Landroid/content/Context;->getSharedPreferences(Ljava/lang/String;I)Landroid/content/SharedPreferences;
                move-result-object v0
                invoke-interface { v0 }, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences${'$'}Editor;
                move-result-object v0
                const-string v1, "premium_user"
                const/4 v2, 0x1
                invoke-interface { v0, v1, v2 }, Landroid/content/SharedPreferences${'$'}Editor;->putBoolean(Ljava/lang/String;Z)Landroid/content/SharedPreferences${'$'}Editor;
                invoke-interface { v0 }, Landroid/content/SharedPreferences${'$'}Editor;->apply()V
            """,
        )

        // 2. SettingsActivity.onResume() — override getBoolean result immediately.
        //    Controls GET PREMIUM card visibility + premium badge icons.
        SettingsActivityOnResumePremiumFingerprint.let { fp ->
            val getBooleanIdx = fp.instructionMatches[2].index
            val moveResultIdx = getBooleanIdx + 1
            val reg = fp.method.getInstruction<OneRegisterInstruction>(moveResultIdx).registerA
            fp.method.replaceInstruction(moveResultIdx, "const/4 v$reg, 0x1")
        }

        // 3. ProfileActivity.u(Z)V — always write true to SharedPrefs.
        SetPremiumFingerprint.method.addInstruction(0, "const/4 p1, 0x1")

        // 4. Pairip — skip license check.
        ProcessLicenseResponseFingerprint.method.returnEarly()
    }
}
