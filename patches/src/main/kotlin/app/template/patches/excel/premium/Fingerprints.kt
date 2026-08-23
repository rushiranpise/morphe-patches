package app.template.patches.excel.premium

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
 * OHubUtil.GetLicensingState() — returns the LicensingState enum controlling all
 * subscription UI. ConsumerPremium suppresses all upsell/buy surfaces.
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
 * Stable: non-obfuscated public API in paywallsdk.
 */
internal val subscriptionDataIsTrialFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/mobile/paywallsdk/publics/SubscriptionData;",
    name = "isTrial",
    returnType = "Z",
    parameters = emptyList(),
)

/**
 * LicenseStatus.isPremium() — non-obfuscated enum method in growth/upsellplugin.
 * Excel retains the non-obfuscated class name (unlike Word which uses 'j').
 */
internal val licenseStatusIsPremiumFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/growth/upsellplugin/models/LicenseStatus;",
    name = "isPremium",
    returnType = "Z",
    parameters = emptyList(),
)

// ── Obfuscated fingerprints — pinned by return type + params ──────────────────

/**
 * licensing.?.?() — returns LicensingState from native OLS session via NativeProxy.Gs.
 * Returning ConsumerPremium prevents OLS_E_ENTITLEMENT_NOT_FOUND downgrading state.
 *
 * Stable anchor: methodCall LicensingState.FromInt(I) is non-obfuscated and verified
 * globally unique to exactly one ()LicensingState method. Drops definingClass "e" and
 * name "d" (both obfuscated, shift every update).
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
 * Native licensing lookup via NativeProxy.Glifu. Returning empty non-null LicenseInfo
 * ensures Has*Plan patches are reached. Class/method name are obfuscated and shift.
 *
 * Stable anchor: methodCall NativeProxy.Glifu (JNI bridge name, never obfuscated)
 * verified globally unique to exactly one (String,UserAccountType,String,Z)LicenseInfo.
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
 * ?$a.m(Context) — subscription-check dispatcher. Calls y/o/r/v in sequence;
 * if any returns true it skips paywall. return-void no-ops the entire dispatch.
 *
 * Stable anchor: sget-object SubscriptionPurchaseController$EntryPoint->SaveFlowUpsell
 * is a non-obfuscated enum field inside m(). Verified globally unique to exactly one
 * (Context)V static final method — survives any inner-class rename (a1$a → w0$a etc.).
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
 * AccountProfileInfo.?() — boolean hasProfile field getter. Method name is R8-renamed
 * every update (was B()). Found structurally via custom predicate:
 *   1. public non-final non-static ()Z with no invoke-* (pure iget-boolean getter)
 *   2. reads the field written by the second (AccountProfileInfo,Z)V bridge synthetic
 *      (bridge ordering follows Kotlin property declaration order: isSignedIn first,
 *      hasProfile second — stable across R8 renames)
 * definingClass AccountProfileInfo is non-obfuscated and stable.
 */
internal val accountProfileInfoHasProfileFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/docsui/common/AccountProfileInfo;",
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        val flags = method.accessFlags
        val isCandidate = method.returnType == "Z" &&
            method.parameters.isEmpty() &&
            flags and AccessFlags.FINAL.value == 0 &&
            flags and AccessFlags.STATIC.value == 0 &&
            method.implementation?.instructions?.none {
                it.opcode.name.startsWith("INVOKE")
            } == true
        if (!isCandidate) return@Fingerprint false

        val readField = method.implementation?.instructions
            ?.filterIsInstance<Instruction22c>()
            ?.firstOrNull { it.opcode == Opcode.IGET_BOOLEAN }
            ?.reference as? FieldReference
            ?: return@Fingerprint false

        val bridges = classDef.methods.filter { m ->
            m.accessFlags and AccessFlags.BRIDGE.value != 0 &&
            m.accessFlags and AccessFlags.STATIC.value != 0 &&
            m.parameters.size == 2 &&
            m.parameters[0].type == "Lcom/microsoft/office/docsui/common/AccountProfileInfo;" &&
            m.parameters[1].type == "Z" &&
            m.returnType == "V"
        }
        val hasProfileWriter = bridges.getOrNull(1) ?: return@Fingerprint false

        val writtenField = hasProfileWriter.implementation?.instructions
            ?.filterIsInstance<Instruction22c>()
            ?.firstOrNull { it.opcode == Opcode.IPUT_BOOLEAN }
            ?.reference as? FieldReference
            ?: return@Fingerprint false

        readField.name == writtenField.name
    },
)

/**
 * unifiedStorageQuota.?.?(Identity) — storage quota UI check.
 * NPE guard when identity is null. Returning false skips quota display.
 *
 * Stable anchor: (Identity)Z + PUBLIC STATIC FINAL is globally unique to exactly
 * one method. Drops definingClass "f" and name "b" (both obfuscated).
 */
/**
 * ?$n.run() — account-switcher dialog builder. Calls GetActiveIdentity() which returns
 * null when no account is present, causing NPE on getMetaData() at offset 128.
 *
 * Stable anchor: AccountActionsController.setAccountInfoDialog(DrillInDialog) is a
 * non-obfuscated call inside run(). Globally unique to exactly one run()V method.
 * We insert a null guard after GetActiveIdentity() — if null, return-void (no dialog).
 *
 * Filter matches instruction order:
 *   invoke-virtual  IdentityLiblet;->GetActiveIdentity()Identity  [index 52]
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

