package app.template.patches.toxly

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TOXLY_COMPATIBILITY
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.Opcode

// Toxly premium system overview (v1.18.16, versionCode 99):
//
// Protection stack:
//   Pairip (LicenseClient-only variant — no VMRunner / SignatureCheck / StartupLauncher):
//     Application.attachBaseContext() → LicenseClient.checkLicense(Context) [static]
//     Same structure as SAI — kill via checkLicense.returnEarly().
//
// Premium state:
//   Managed by BillingRepository (kz):
//     kz.e: Lac4 (MutableStateFlow<Boolean>) — initialised to FALSE in constructor
//     kz.g: Lac4 (MutableStateFlow<Boolean>) — second state flow (UI-bound)
//     r5.b: Lac4 (MutableStateFlow<Boolean>) — global SharedPrefs-backed state
//     r5.c(Context, Z): writes "is_ad_free" to "ad_prefs" SharedPrefs + sets r5.b
//     r5.d():Z:         reads r5.b.getValue() — synchronous isPremium getter
//
//   kz.d(List<Purchase>) = onQueryPurchasesResponse:
//     Iterates purchases for "toxly_premium_sub", checks purchaseState + acknowledged.
//     If found and valid:
//       kz.e.k(null, TRUE) + r5.c(ctx, true)   ← premium ON
//     Else (empty list or product not found):
//       kz.e.k(null, FALSE) + r5.c(ctx, false)  ← premium OFF
//
// Patch strategy:
//   1. LicenseClient.checkLicense — returnEarly(). Null-ops the entire Pairip
//      license check before it constructs a LicenseClient instance.
//
//   2. kz constructor — inject before first return-void.
//      Sets kz.e to TRUE (unconditional via k(null, TRUE)) and calls r5.c(kz.a, true)
//      to write SharedPreferences. Repository is born with premium=true regardless
//      of purchase state. Registers used: v0, v1, v2 (safe — all dead at injection site).
//
//   3. kz.d(List) — inject at index 0 then return-void.
//      Sets premium state and returns immediately, preventing the purchase list from
//      ever setting premium=false (covers subscription lapse, empty query result, etc.).

@Suppress("unused")
val toxlyUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Toxly Premium by bypassing the Pairip license check and activating the premium billing state at repository level.",
) {
    compatibleWith(TOXLY_COMPATIBILITY)

    execute {
        // 1. Null-op Pairip license check.
        mutableClassDefBy("Lcom/pairip/licensecheck/LicenseClient;")
            .methods.first { it.name == "checkLicense" }
            .returnEarly()

        // 2. Inject premium activation at the end of the BillingRepository constructor.
        //    Confirmed register layout at injection point (kz.<init> .registers 10):
        //      p0 = this (Lkz;)   — used to iget kz.e and kz.a
        //      v0, v1, v2         — dead locals, safe to reuse
        //    Lac4.k(p1=null, p2=TRUE): null p1 bypasses CAS and sets unconditionally.
        BillingRepositoryConstructorFingerprint.method.apply {
            val returnIdx = instructions.indexOfFirst { it.opcode == Opcode.RETURN_VOID }
            if (returnIdx < 0) throw PatchException(
                "Toxly: BillingRepository constructor return-void not found.",
            )
            addInstructions(
                returnIdx,
                """
                    iget-object v0, p0, Lkz;->e:Lac4;
                    const/4 v1, 0x0
                    sget-object v2, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                    invoke-virtual {v0, v1, v2}, Lac4;->k(Ljava/lang/Object;Ljava/lang/Object;)Z
                    iget-object v0, p0, Lkz;->a:Landroid/content/Context;
                    const/4 v1, 0x1
                    invoke-static {v0, v1}, Lr5;->c(Landroid/content/Context;Z)V
                """.trimIndent(),
            )
        }

        // 3. Intercept the purchase-list callback: activate premium then return immediately.
        //    Confirmed register layout at method entry (kz.d .registers 12):
        //      p0 = this (Lkz;)   — used to iget kz.e and kz.a
        //      p1 = List<Purchase> (ignored after return-void)
        //      v0, v1, v2         — free at entry
        BillingRepositoryOnPurchasesFingerprint.method.addInstructions(
            0,
            """
                iget-object v0, p0, Lkz;->e:Lac4;
                const/4 v1, 0x0
                sget-object v2, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                invoke-virtual {v0, v1, v2}, Lac4;->k(Ljava/lang/Object;Ljava/lang/Object;)Z
                iget-object v0, p0, Lkz;->a:Landroid/content/Context;
                const/4 v1, 0x1
                invoke-static {v0, v1}, Lr5;->c(Landroid/content/Context;Z)V
                return-void
            """.trimIndent(),
        )
    }
}
