package app.template.patches.sai

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SAI_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.Opcode

// SAI premium system overview (v2.3.3, versionCode 45):
//
// ── Pairip (LicenseClient-only variant) ──────────────────────────────────────
// Application.attachBaseContext() → LicenseClient.checkLicense(Context) [static]
// No VMRunner / SignatureCheck / StartupLauncher in this build.
//
// ── Subscription product IDs ─────────────────────────────────────────────────
// "pro_version_weekly", "pro_version_monthly"  (jj9.a)
//
// ── State flows ──────────────────────────────────────────────────────────────
// sr0.d (v89 StateFlow, initial value = NULL):
//   → billing result arrives async → emits Boolean
//   → yr0.e → zn5 → yn5.emit()       MainActivity paywall overlay gate
//
// DataStore (dj9): SharedPrefs "has_active_subscription"
//   → written by dj9.c() after billing result
//   → read synchronously by dj9.b()
//
// ── Onboarding paywall (THIS screenshot) ─────────────────────────────────────
// OnboardingActivity shows pages built by ov6 constructor.
// ov6 always adds vu6.d (paywall slide) as the last page — unconditionally,
// no subscription check guards it. The slide stays on screen because:
//   - sr0.d starts as NULL → ou6.emit receives null → no-op (null cast to Boolean
//     succeeds in Dalvik but tq4.j(null, TRUE) returns false → nothing happens)
//   - Billing connects asynchronously; TRUE arrives only after Play Store responds
//   - User sees the paywall slide for the full billing latency window (1-5 seconds)
//     and it never auto-completes on a fresh patched install (no purchases exist)
//
// Root fix: remove the vu6.d add() call from ov6 constructor entirely.
// The paywall slide never enters the page list → OnboardingActivity.m5745C()
// is called after the last real page → onboarding completes without paywall.
//
// ── MainActivity paywall overlay ─────────────────────────────────────────────
// yn5.emit() on sr0.d → if FALSE → checks gf8."subscription_prompt_shown"
// → shows overlay. Fixed by clearBody + always-TRUE branch.
//
// ── Patch strategy (5 steps) ─────────────────────────────────────────────────
// 1. LicenseClient.checkLicense — returnEarly()
// 2. dj9.b(Context):Z — returnEarly(true)       [sync gate]
// 3. dj9.c(p2=Z) — prepend const/4 p2, 0x1      [write path]
// 4. ov6.<init> — nop the xc5.add(vu6.d) call   [onboarding paywall page]
// 5. yn5.emit — clearBody + TRUE branch          [MainActivity paywall overlay]

@Suppress("unused")
val saiUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks SAI Pro by bypassing Pairip, removing the onboarding paywall slide from the page list, and suppressing the MainActivity subscription overlay.",
) {
    compatibleWith(SAI_COMPATIBILITY)

    execute {
        // 1. Kill Pairip license check entry point.
        mutableClassDefBy("Lcom/pairip/licensecheck/LicenseClient;")
            .methods.first { it.name == "checkLicense" }
            .returnEarly()

        // 2. Synchronous subscription getter — always true.
        HasActiveSubscriptionFingerprint.method.returnEarly(true)

        // 3. Subscription coroutine setter — force boolean arg (p2) to true.
        //    p0=Ldj9; p1=Context p2=Z p3=Lcu1;
        SetActiveSubscriptionFingerprint.method.addInstructions(0, "const/4 p2, 0x1")

        // 4. Remove the paywall page from the onboarding page list.
        //    OnboardingPaywallPageFingerprint matches fv6.<init> via:
        //      filter[0] = sget-object Lmu6;->d (paywall page enum)  [was vu6 in v2.3.2]
        //      filter[1] = invoke-virtual Lqc5;->add() (the list add immediately after)  [was xc5]
        //    instructionMatches[1] is the add() call — replace it with a const/4 no-op.
        //    The add() return value (Z) is never used, so discarding it is safe.
        //    After replacement the uu6(vu6.d,...) object is constructed but never added;
        //    it becomes unreachable and is collected immediately.
        OnboardingPaywallPageFingerprint.apply {
            val addIndex = instructionMatches[1].index
            method.replaceInstruction(addIndex, "const/4 v0, 0x0")
        }

        // 5. MainActivity paywall FlowCollector — always execute TRUE branch.
        //    .registers 5: v0=sentinel, v1=null; p0→MainActivity, p1→MainActivity.I
        //    v2.3.3 renames: Lpca;→Lfca;, Lyn5;→Lsn5;, Lr87;→Li87;, Lb49;→Ls39;
        PaywallEmitFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v0, Lfca;->a:Lfca;
                    const/4 v1, 0x0
                    iget-object p0, p0, Lsn5;->b:Lcom/mtv/sai/ui/activities/main/MainActivity;
                    iget-object p1, p0, Lcom/mtv/sai/ui/activities/main/MainActivity;->I:Li87;
                    invoke-virtual {p1, v1}, Ls39;->setValue(Ljava/lang/Object;)V
                    invoke-virtual {p0, v1}, Lcom/mtv/sai/ui/activities/main/MainActivity;->H(Ljava/lang/Integer;)V
                    iput-object v1, p0, Lcom/mtv/sai/ui/activities/main/MainActivity;->K:Ljava/lang/Integer;
                    return-object v0
                """.trimIndent(),
            )
        }
    }
}
