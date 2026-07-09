package app.template.patches.myradar.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import com.android.tools.smali.dexlib2.AccessFlags

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks MyRadar's yearly premium features",
) {
    compatibleWith(Constants.MYRADAR_COMPATIBILITY)

    execute {
        // Architecture:
        //
        // Layer 1 — ki4 (Entitlements data class): wraps Set<Entitlement>
        //   c(Entitlement)Z       → hasEntitlement (single)   — patched → always true
        //   d([Entitlement)Z      → hasEntitlements (varargs)  — patched → always true
        //   Pinned by unique string: "Entitlements(entitlements="
        //
        // Layer 2 — LicenseStore.l()Z → hasPremium
        //   Reads MutableStateFlow<Boolean> (field k).
        //   Patched → always true.
        //   LicenseStore pinned by non-obfuscated class descriptor; l() by name+sig.
        //
        // Layer 3 (UI label fix) — LicenseStore.w(List)Set → active License enum Set
        //   Called by the DB-change coroutine; result drives the activeLicenses StateFlow (field h).
        //   ps9 (subscription UI) iterates this Set and calls License.getDisplayNameRes() on each.
        //   Without a purchase, w() returns empty Set → UI shows "Free".
        //   Patched → always returns Collections.singleton(License.Premium1), so UI shows
        //   "Premium Features" and hasPremium() returns true naturally via License.isPremium().
        //   w(List)Set: non-static, (Ljava/util/List;)Ljava/util/Set; in LicenseStore.

        // --- Patch 1 & 2: ki4.c(Entitlement)Z and ki4.d([Entitlement)Z → return true ---
        val entitlementsClass = classDefByStrings("Entitlements(entitlements=").firstOrNull()
            ?: throw PatchException(
                "MyRadar: Entitlements class (ki4) not found. " +
                    "Re-derive from class containing \"Entitlements(entitlements=\".",
            )

        val mutableEntitlements = mutableClassDefBy(entitlementsClass)

        // ki4.c(Entitlement)Z — single entitlement check
        val hasEntitlementSingle = mutableEntitlements.methods.firstOrNull { method ->
            !AccessFlags.STATIC.isSet(method.accessFlags) &&
                method.returnType == "Z" &&
                method.parameterTypes.size == 1 &&
                method.parameterTypes[0] == "Lcom/acmeaom/android/billing/model/Entitlement;"
        } ?: throw PatchException(
            "MyRadar: ki4.c(Entitlement)Z not found in ${entitlementsClass.type}.",
        )

        hasEntitlementSingle.clearBody()
        hasEntitlementSingle.ensureRegisters(1)
        hasEntitlementSingle.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )

        // ki4.d([Entitlement)Z — varargs entitlement check
        val hasEntitlementVarargs = mutableEntitlements.methods.firstOrNull { method ->
            !AccessFlags.STATIC.isSet(method.accessFlags) &&
                method.returnType == "Z" &&
                method.parameterTypes.size == 1 &&
                method.parameterTypes[0] == "[Lcom/acmeaom/android/billing/model/Entitlement;"
        } ?: throw PatchException(
            "MyRadar: ki4.d([Entitlement)Z not found in ${entitlementsClass.type}.",
        )

        hasEntitlementVarargs.clearBody()
        hasEntitlementVarargs.ensureRegisters(1)
        hasEntitlementVarargs.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )

        // --- Patches 3 & 4: LicenseStore methods ---
        val licenseStoreClass = mutableClassDefByOrNull(
            "Lcom/acmeaom/android/billing/licenses/LicenseStore;",
        ) ?: throw PatchException(
            "MyRadar: LicenseStore class not found. Package layout changed.",
        )

        // --- Patch 3: LicenseStore.l()Z → hasPremium — always return true ---
        val hasPremium = licenseStoreClass.methods.firstOrNull { method ->
            method.name == "l" &&
                !AccessFlags.STATIC.isSet(method.accessFlags) &&
                method.returnType == "Z" &&
                method.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "MyRadar: LicenseStore.l()Z (hasPremium) not found.",
        )

        hasPremium.clearBody()
        hasPremium.ensureRegisters(1)
        hasPremium.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )

        // --- Patch 4: LicenseStore.w(List)Set → always return {License.Premium1} ---
        // This drives the activeLicenses StateFlow.  Without it the Set is empty and
        // the subscription settings screen (ps9) shows "Free" instead of "Premium Features".
        // Premium1 (id="prem1") grants all 9 entitlements via License.getEntitlements().
        val buildActiveLicenses = licenseStoreClass.methods.firstOrNull { method ->
            method.name == "w" &&
                !AccessFlags.STATIC.isSet(method.accessFlags) &&
                method.returnType == "Ljava/util/Set;" &&
                method.parameterTypes.size == 1 &&
                method.parameterTypes[0] == "Ljava/util/List;"
        } ?: throw PatchException(
            "MyRadar: LicenseStore.w(List)Set (buildActiveLicenses) not found.",
        )

        buildActiveLicenses.clearBody()
        buildActiveLicenses.ensureRegisters(2)
        buildActiveLicenses.addInstructions(
            0,
            """
                sget-object v0, Lcom/acmeaom/android/billing/model/License;->Premium1:Lcom/acmeaom/android/billing/model/License;
                invoke-static {v0}, Ljava/util/Collections;->singleton(Ljava/lang/Object;)Ljava/util/Set;
                move-result-object v0
                return-object v0
            """,
        )
    }
}
