package app.template.patches.awake.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.AWAKE_COMPATIBILITY
import app.template.patches.shared.gutPairIpVm
import app.template.patches.shared.killPairIpFull
import com.android.tools.smali.dexlib2.Opcode

// ─────────────────────────────────────────────────────────────────────────────
// Awake v1.10.3 — premium unlock + pairip bypass
//
// TWO independent gating systems must be patched:
//
// ① RC premium chain  (all suspend funs — replaceInstruction only, no returnEarly)
//
//   Layer A — gk/f.b (isUserPremium):
//     Purchases.isAnonymous() result at [41] → if true → loginUser → returns false
//     Fix: const/4 v2, 0x0 at [41] — always "not anonymous" → calls gk/f.a()
//     Anchor: filter match on Purchases.isAnonymous() → instructionMatches[0].index = 40
//             → move-result is at 40+1 = 41
//
//   Layer B — gk/f.a (checkPremiumEntitlement):
//     isActive() at [44], move-result p0 at [45] (active path)
//     null-entitlement fallback move at [47]
//     Fix: const/4 p0, 0x1 at both [45] and [47]
//     Already in current patch — kept unchanged.
//
//   Layer C — fk/f1.<init> DataStore write lambda constructor:
//     Force field b:Z = true always via addInstructions(0, "const/4 p1, 0x1")
//     Belt+suspenders for DataStore cache warmth.
//
// ② Pairip LicenseClient  (non-suspend, DEX-only — no libpairipcore.so present)
//
//   LicenseContentProvider.onCreate() → LicenseClient.initializeLicenseCheck()
//   → async connect to Play licensing service → processResponse(code, bundle)
//   responseCode 2 = paywall → LicenseActivity shown, app blocked
//
//   Fix: killPairIpFull() from shared/PairIp.kt:
//     - initializeLicenseCheck() → return-void (never connects)
//     - processResponse()        → return-void (never handles response)
//     - startPaywallActivity()   → return-void (paywall never shown)
//     - connectToLicensingService() → return-void
//   No VMRunner/SignatureCheck/StartupLauncher in this app (DEX-only pairip).
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium features in Awake by bypassing the " +
        "RC entitlement check chain and the Pairip Play Licensing verification.",
    default = true,
) {
    compatibleWith(AWAKE_COMPATIBILITY)

    execute {

        // ── Layer A: IsUserPremium — force "not anonymous" path ───────────────
        //
        // gk/f.b at instruction sequence:
        //   [40] invoke-virtual {v2}, Purchases.isAnonymous()Z
        //   [41] move-result v2                 ← REPLACE with const/4 v2, 0x0
        //   [42] if-eqz v2, :cond_76            (eqz = 0 = not-anon → logged-in branch)
        //   [43] iput v4, v0, gk/b->c:I
        //   [44] invoke-virtual {p0, v0}, gk/f.c(Continuation)  ← skipped (anon path)
        //   ...
        //   :cond_76 → "User is logged in" → calls gk/f.a() (our patched method)
        //
        // With v2=0: if-eqz 0 → true → jumps to :cond_76 → calls gk/f.a() always.
        // Register: v2 is the move-result register — we read it from the instruction.
        IsUserPremiumFingerprint.method.apply {
            val isAnonIdx = IsUserPremiumFingerprint.instructionMatches[0].index // isAnonymous()
            val moveResultIdx = isAnonIdx + 1

            val moveResultInsn = implementation!!.instructions.toList()[moveResultIdx]
            if (moveResultInsn.opcode != Opcode.MOVE_RESULT) throw PatchException(
                "Awake IsUserPremium: expected MOVE_RESULT at ${moveResultIdx}, got ${moveResultInsn.opcode}"
            )

            // Force v2 = 0 (not anonymous) so if-eqz takes the logged-in branch
            replaceInstruction(moveResultIdx, "const/4 v2, 0x0")
        }

        // ── Layer B: CheckPremiumEntitlement — force isActive() = true ────────
        //
        // gk/f.a instruction sequence:
        //   [44] invoke-virtual EntitlementInfo.isActive()Z
        //   [45] move-result p0                 ← REPLACE with const/4 p0, 0x1
        //   [46] goto :goto_57
        //   [47] move p0, v5  (null entitlement, v5=0)  ← REPLACE with const/4 p0, 0x1
        //
        // Work reverse order (B before A) to keep indices stable.
        CheckPremiumEntitlementFingerprint.method.apply {
            val isActiveIdx = CheckPremiumEntitlementFingerprint.instructionMatches.last().index

            val instructions = implementation!!.instructions.toList()
            val moveResultInsn   = instructions[isActiveIdx + 1]
            val nullFallbackInsn = instructions[isActiveIdx + 3]

            if (moveResultInsn.opcode != Opcode.MOVE_RESULT) throw PatchException(
                "Awake CheckPremiumEntitlement: expected MOVE_RESULT at ${isActiveIdx + 1}, " +
                    "got ${moveResultInsn.opcode}"
            )
            if (nullFallbackInsn.opcode != Opcode.MOVE) throw PatchException(
                "Awake CheckPremiumEntitlement: expected MOVE at ${isActiveIdx + 3}, " +
                    "got ${nullFallbackInsn.opcode}"
            )

            replaceInstruction(isActiveIdx + 3, "const/4 p0, 0x1")  // null path
            replaceInstruction(isActiveIdx + 1, "const/4 p0, 0x1")  // active path
        }

        // ── Layer C: DataStore write lambda constructor — always store true ────
        //
        // fk/f1.<init>(Z, Continuation): stores isPremium in field b:Z.
        // Prepend const/4 p1, 0x1 — forces b = true in all constructions.
        val lambdaClass = SetPremiumInvokeSuspendFingerprint.classDef
        val constructor = lambdaClass.methods.firstOrNull { method ->
            method.name == "<init>" &&
            method.returnType == "V" &&
            method.parameters.size == 2 &&
            method.parameters[0].type == "Z"
        } ?: throw PatchException(
            "Awake: could not find fk/f1.<init>(Z, Continuation)V in ${lambdaClass.type}"
        )
        constructor.addInstructions(0, "const/4 p1, 0x1")

        // ── Pairip: kill LicenseClient ────────────────────────────────────────
        //
        // Play LVL licence check triggered by LicenseContentProvider.onCreate().
        // No VMRunner/SignatureCheck/StartupLauncher (DEX-only pairip variant).
        // killPairIpFull() no-ops: initializeLicenseCheck, connectToLicensingService,
        // processResponse, startPaywallActivity, lambda$retryOrThrow$0.
        // mutableClassDefByOrNull used throughout — safe no-op if class absent.
        killPairIpFull()
    }
}
