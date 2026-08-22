package app.template.patches.word.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22c
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

// ── Stable non-obfuscated fingerprints ───────────────────────────────────────

internal val isPremiumPlanUpsellEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/plat/PlatFeatureGateHelper;",
    name = "isPremiumPlanUpsellEnabled",
    returnType = "Z",
    parameters = emptyList(),
)

internal val isEnterpriseViewOLSCheckEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/plat/PlatFeatureGateHelper;",
    name = "IsEnterpriseViewOLSCheckEnabled",
    returnType = "Z",
    parameters = emptyList(),
)

internal val hasFamilyPlanFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/licensing/LicenseInfo;",
    name = "HasFamilyPlan",
    returnType = "Z",
    parameters = emptyList(),
)

internal val hasPersonalPlanFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/licensing/LicenseInfo;",
    name = "HasPersonalPlan",
    returnType = "Z",
    parameters = emptyList(),
)

internal val hasPremiumPlanFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/licensing/LicenseInfo;",
    name = "HasPremiumPlan",
    returnType = "Z",
    parameters = emptyList(),
)

/**
 * OHubUtil.GetLicensingState() — returns the LicensingState enum used throughout the
 * UI to determine subscription display. ConsumerPremium hides all upsell/buy UI.
 * Stable: non-obfuscated public static API.
 */
internal val getLicensingStateFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/officehub/util/OHubUtil;",
    name = "GetLicensingState",
    returnType = "Lcom/microsoft/office/licensing/LicensingState;",
    parameters = emptyList(),
)

/**
 * SubscriptionData.isTrial() — paywallsdk trial flag.
 * Returning false removes the trial badge from paywall UI.
 * Stable: non-obfuscated public API in paywallsdk.
 */
internal val subscriptionDataIsTrialFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/mobile/paywallsdk/publics/SubscriptionData;",
    name = "isTrial",
    returnType = "Z",
    parameters = emptyList(),
)

// ── Obfuscated fingerprints — pinned by return type + params (stable across renames) ──

/**
 * licensing.?.?() — returns LicensingState from the native OLS session via NativeProxy.Gs.
 * Called after server licensing check; overwrites local GetLicensingState with the server
 * result. Returning ConsumerPremium prevents OLS_E_ENTITLEMENT_NOT_FOUND downgrading state.
 *
 * Stable anchor: methodCall LicensingState.FromInt(I) is non-obfuscated and verified
 * globally unique to exactly one ()LicensingState method in the app — survives class rename.
 *
 * Filter matches smali instruction order in d():
 *   invoke-static {p0}, LicensingState;->FromInt(I)LicensingState;
 */
internal val licenseSessionStateFingerprint = Fingerprint(
    returnType = "Lcom/microsoft/office/licensing/LicensingState;",
    parameters = emptyList(),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/microsoft/office/licensing/LicensingState;",
            name = "FromInt",
        ),
    ),
)

/**
 * licensing.?.?(String, UserAccountType, String, Z) → LicenseInfo
 * Native licensing lookup via NativeProxy.Glifu. Returns null when no server entitlement,
 * causing paywall despite HasFamilyPlan patches. Returning empty LicenseInfo keeps it
 * non-null so Has*Plan patches apply. Class renamed over time (g→h in 16.0.20228).
 *
 * Stable anchor: methodCall NativeProxy.Glifu is non-obfuscated (JNI bridge) and verified
 * globally unique to exactly one (String,UserAccountType,String,Z)LicenseInfo method.
 * Survives any future class or method name rename.
 *
 * Filter matches smali instruction order in h():
 *   invoke-static {p1,p0,p3,p4}, NativeProxy;->Glifu(String;IString;Z)LicenseInfo;
 */
internal val licensingFGFingerprint = Fingerprint(
    returnType = "Lcom/microsoft/office/licensing/LicenseInfo;",
    parameters = listOf(
        "Ljava/lang/String;",
        "Lcom/microsoft/office/licensing/UserAccountType;",
        "Ljava/lang/String;",
        "Z",
    ),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/microsoft/office/jni/NativeProxy;",
            name = "Glifu",
        ),
    ),
)

/**
 * upsellplugin/models/?.isPremium()Z — enum method, returns true only for
 * MANAGED_PREMIUM/UNMANAGED_PREMIUM instances. Class was j, renamed k in 16.0.20326.
 *
 * Stable anchor: sget-object MANAGED_PREMIUM is a non-obfuscated enum field
 * reference inside isPremium(). Verified globally unique to exactly one isPremium()Z
 * method across the entire app — survives any future enum class rename.
 *
 * Filter matches smali instruction order in isPremium():
 *   sget-object  ?->MANAGED_PREMIUM:?
 */
internal val licenseStatusIsPremiumFingerprint = Fingerprint(
    name = "isPremium",
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.SGET_OBJECT,
            name = "MANAGED_PREMIUM",
        ),
    ),
)

/**
 * ?$a.m(Context) — the subscription-check dispatcher. Calls y/o/r/v in sequence;
 * if any returns true it jumps past the paywall. Patching m() to return-void silently
 * no-ops the entire dispatch without touching the individual check methods.
 *
 * Stable anchor: SubscriptionPurchaseController$EntryPoint->SaveFlowUpsell is a
 * non-obfuscated enum field sget-object'd inside m(). Verified globally unique to
 * exactly this one (Context)V static final method — survives any inner-class rename.
 *
 * Filter matches smali instruction order in m():
 *   sget-object  EntryPoint->SaveFlowUpsell   [cond_3a]
 */
internal val subscriptionStatusYFingerprint = Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.SGET_OBJECT,
            definingClass = "Lcom/microsoft/office/inapppurchase/SubscriptionPurchaseController\$EntryPoint;",
            name = "SaveFlowUpsell",
        ),
    ),
)

/**
 * AccountProfileInfo.?() — returns the boolean hasProfile field.
 * False = doughboy/sign-in avatar in MeControl; true = real account avatar shown.
 *
 * Stable anchor: definingClass is non-obfuscated. Method name is R8-renamed every
 * update (was B()). Identified structurally via custom predicate:
 *   - public, non-final, non-static ()Z
 *   - body is exactly 2 instructions: iget-boolean + return (pure field getter, no invoke)
 *   - reads a Z field on AccountProfileInfo (not on another class)
 * There are two such getters (isSignedIn and hasProfile). hasProfile is the one whose
 * backing field is ONLY written by bridge synthetic f(AccountProfileInfo,Z)V — i.e. the
 * second (AccountProfileInfo,Z)V bridge in declaration order (index 1, after isSignedIn).
 * We identify it by matching the field name read by that bridge.
 */
internal val accountProfileInfoHasProfileFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/docsui/common/AccountProfileInfo;",
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        // Must be public non-final non-static ()Z with no invoke instructions
        val flags = method.accessFlags
        val isCandidate = method.returnType == "Z" &&
            method.parameters.isEmpty() &&
            flags and com.android.tools.smali.dexlib2.AccessFlags.FINAL.value == 0 &&
            flags and com.android.tools.smali.dexlib2.AccessFlags.STATIC.value == 0 &&
            method.implementation?.instructions?.none {
                it.opcode.name.startsWith("INVOKE")
            } == true

        if (!isCandidate) return@Fingerprint false

        // Find the field this getter reads
        val readField = method.implementation?.instructions
            ?.filterIsInstance<com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22c>()
            ?.firstOrNull { it.opcode == com.android.tools.smali.dexlib2.Opcode.IGET_BOOLEAN }
            ?.reference as? com.android.tools.smali.dexlib2.iface.reference.FieldReference
            ?: return@Fingerprint false

        // Find the second bridge (AccountProfileInfo,Z)V — that one writes hasProfile
        val bridges = classDef.methods
            .filter { m ->
                m.accessFlags and com.android.tools.smali.dexlib2.AccessFlags.BRIDGE.value != 0 &&
                m.accessFlags and com.android.tools.smali.dexlib2.AccessFlags.STATIC.value != 0 &&
                m.parameters.size == 2 &&
                m.parameters[0].type == "Lcom/microsoft/office/docsui/common/AccountProfileInfo;" &&
                m.parameters[1].type == "Z" &&
                m.returnType == "V"
            }
        val hasProfileWriter = bridges.getOrNull(1) ?: return@Fingerprint false

        val writtenField = hasProfileWriter.implementation?.instructions
            ?.filterIsInstance<com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22c>()
            ?.firstOrNull { it.opcode == com.android.tools.smali.dexlib2.Opcode.IPUT_BOOLEAN }
            ?.reference as? com.android.tools.smali.dexlib2.iface.reference.FieldReference
            ?: return@Fingerprint false

        readField.name == writtenField.name
    },
)

/**
 * unifiedStorageQuota.?.?(Identity) — checks if the storage quota UI should show.
 * Crashes with NPE when identity is null (no real account but hasProfile=true).
 * Returning false safely skips quota display.
 *
 * Stable anchor: (Identity)Z with PUBLIC STATIC FINAL access flags is verified globally
 * unique to exactly one method in the entire app — no definingClass or name needed.
 */
/**
 * ?$n.run() — account-switcher dialog builder. GetActiveIdentity() returns null
 * when no account is present → NPE on getMetaData() on main thread.
 * Null guard inserted after move-result-object v12 — if null, return-void.
 *
 * Stable anchors: setAccountInfoDialog (unique run()V) + GetActiveIdentity (index locator).
 */
internal val accountSwitcherRunnableFingerprint = Fingerprint(
    name = "run",
    returnType = "V",
    parameters = emptyList(),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/microsoft/office/docsui/common/AccountActionsController;",
            name = "setAccountInfoDialog",
        ),
        methodCall(
            definingClass = "Lcom/microsoft/office/identity/IdentityLiblet;",
            name = "GetActiveIdentity",
        ),
    ),
)

internal val storageQuotaCheckFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf("Lcom/microsoft/office/identity/Identity;"),
)


