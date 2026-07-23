package app.template.patches.mobioffice.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

private const val PROXY        = "Lcom/mobisystems/registration2/OsFeaturesCheckProxy;"
private const val SN2          = "Lcom/mobisystems/registration2/SerialNumber2;"
private const val JP_E         = "Ljp/e;"
private const val M1           = "Lcom/mobisystems/monetization/m1;"
private const val WAB          = "Lwa/b;"
private const val L3B          = "Lcom/mobisystems/office/l3\$b;"
private const val AD_LOGIC     = "Lcom/mobisystems/android/ads/AdLogicFactory;"
private const val PRICING_PLAN = "Lcom/mobisystems/registration2/types/PricingPlan;"
private const val LICENSE_LVL  = "Lcom/mobisystems/registration2/types/LicenseLevel;"

// ═════════════════════════════════════════════════════════════════════════════
// ROOT ENTITLEMENT LAYER — PricingPlan + LicenseLevel
//
// All premium state ultimately derives from PricingPlan, which is constructed
// from the MSConnect server FeaturesResult. Patching here propagates to:
//   PricingPlan.d()Z → SerialNumber2.g:Z (isPremium field)
//   PricingPlan.c(String) → all feature lookups ("OSP-A", "OSP-PDF", etc.)
//   LicenseLevel.a(String) → LicenseLevel field + plan name fallback = "premium"
// ═════════════════════════════════════════════════════════════════════════════

/**
 * PricingPlan.d()Z — core isPremium check on the plan object.
 *
 * Checks HashMap.get("OSP-A").equalsIgnoreCase("yes").
 * This return value is written directly to SerialNumber2.g:Z at every
 * entitlement write site (S(), a0(), C(), b0()). Returning true here
 * makes every write to the isPremium field set it to true.
 *
 * Smali (classes10):
 *   const-string v0, "OSP-A"
 *   invoke-virtual {p0, v0}, PricingPlan;->c(String)String
 *   const-string v1, "yes"
 *   invoke-virtual {v1, v0}, String;->equalsIgnoreCase(String)Z
 *   return v0
 */
internal val PricingPlanIsPremiumFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        string("OSP-A"),
        methodCall(definingClass = PRICING_PLAN, name = "c"),
        string("yes"),
        methodCall(definingClass = "Ljava/lang/String;", name = "equalsIgnoreCase"),
    ),
    custom = { _, classDef -> classDef.type == PRICING_PLAN },
)

/**
 * PricingPlan.c(String)String — feature key lookup in the server-provided HashMap.
 *
 * Called for every named feature: "OSP-A" (premium), "OSP-PDF" (PDF export),
 * "OSP-A-FONTS" (fonts), "OSP-A-CLOUD" (cloud), etc.
 * Also called by hasPremiumFeature() on the proxy.
 * Returning "yes" unconditionally makes every feature lookup succeed.
 *
 * Smali (classes10):
 *   iget-object v0, p0, PricingPlan;->c:HashMap
 *   invoke-virtual {v0, p1}, HashMap;->get(Object)Object
 *   check-cast p1, String
 *   return-object p1
 */
internal val PricingPlanFeatureLookupFingerprint = Fingerprint(
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/String;"),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            definingClass = PRICING_PLAN,
            name = "c",
        ),
        methodCall(definingClass = "Ljava/util/HashMap;", name = "get"),
    ),
    custom = { _, classDef -> classDef.type == PRICING_PLAN },
)

/**
 * LicenseLevel.a(String)LicenseLevel — maps server string to LicenseLevel enum.
 *
 * Called from PricingPlan constructor with settings["license"] from server.
 * Server returns "free" for free accounts. Returning premium here forces
 * PricingPlan.a = LicenseLevel.premium, which:
 *   - feeds getLicenseLevel() on the proxy
 *   - feeds the plan name fallback (LicenseLevel.name() = "premium")
 *   - feeds MonetizationUtils.x(LicenseLevel) analytics
 *
 * Smali (classes10):
 *   sget-object v0, LicenseLevel;->premium
 *   invoke-virtual {v0}, Enum;->name()String
 *   invoke-virtual {v1, p0}, String;->equalsIgnoreCase(String)Z
 *   if-eqz → check pro → check free → return null
 */
internal val LicenseLevelFromServerFingerprint = Fingerprint(
    returnType = LICENSE_LVL,
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Ljava/lang/String;"),
    filters = listOf(
        fieldAccess(opcode = Opcode.SGET_OBJECT, definingClass = LICENSE_LVL, name = "premium"),
        methodCall(definingClass = "Ljava/lang/Enum;", name = "name"),
        methodCall(definingClass = "Ljava/lang/String;", name = "equalsIgnoreCase"),
    ),
    custom = { _, classDef -> classDef.type == LICENSE_LVL },
)

// ═════════════════════════════════════════════════════════════════════════════
// PROXY LAYER — OsFeaturesCheckProxy
// Belt-and-suspenders: patch the proxy getters so even if PricingPlan
// is read before our constructor injection fires, the results are correct.
// ═════════════════════════════════════════════════════════════════════════════

// isPremium() — reads SerialNumber2.g:Z
internal val IsPremiumFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        methodCall(definingClass = SN2, name = "n"),
        fieldAccess(opcode = Opcode.IGET_BOOLEAN, definingClass = SN2, name = "g"),
    ),
    custom = { _, classDef -> classDef.type == PROXY },
)

// getLicenseLevel() — reads SerialNumber2.x → PricingPlan.a → LicenseLevel
internal val GetLicenseLevelFingerprint = Fingerprint(
    returnType = LICENSE_LVL,
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        methodCall(definingClass = SN2, name = "n"),
        fieldAccess(opcode = Opcode.IGET_OBJECT, definingClass = SN2, name = "x"),
    ),
    custom = { _, classDef -> classDef.type == PROXY },
)

// hasPremiumFeature(String) — PricingPlan feature map vs "yes"
internal val HasPremiumFeatureFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf("Ljava/lang/String;"),
    filters = listOf(
        string("yes"),
        methodCall(definingClass = "Ljava/lang/String;", name = "equalsIgnoreCase"),
    ),
    custom = { _, classDef -> classDef.type == PROXY },
)

// isExpired() — calls SerialNumber2.v()Z
internal val IsExpiredFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        methodCall(definingClass = SN2, name = "n"),
        methodCall(definingClass = SN2, name = "v"),
    ),
    custom = { _, classDef -> classDef.type == PROXY },
)

// isTrial() — calls SerialNumber2.y()Z
internal val IsTrialFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        methodCall(definingClass = SN2, name = "n"),
        methodCall(definingClass = SN2, name = "y"),
    ),
    custom = { _, classDef -> classDef.type == PROXY },
)

// canFreeUsersEditDocs() — m1.a(false) → XOR 1
internal val CanFreeUsersEditDocsFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(methodCall(definingClass = M1, name = "a")),
    custom = { method, classDef ->
        classDef.type == PROXY && method.name == "canFreeUsersEditDocs"
    },
)

// canFreeUsersEditDocsWithQuota() — m1.a(true) → XOR 1
internal val CanFreeUsersEditDocsWithQuotaFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(methodCall(definingClass = M1, name = "a")),
    custom = { method, classDef ->
        classDef.type == PROXY && method.name == "canFreeUsersEditDocsWithQuota"
    },
)

// canFreeUsersCreateDocs() — l3$b.b() + "createNewIsPremium" GTM
internal val CanFreeUsersCreateDocsFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        methodCall(definingClass = L3B, name = "b"),
        string("createNewIsPremium"),
    ),
    custom = { method, classDef ->
        classDef.type == PROXY && method.name == "canFreeUsersCreateDocs"
    },
)

// canFreeUsersCreateDocsWithQuota() — same + quota check
internal val CanFreeUsersCreateDocsWithQuotaFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        methodCall(definingClass = L3B, name = "b"),
        string("createNewIsPremium"),
        string("numFreeEditDocuments"),
    ),
    custom = { method, classDef ->
        classDef.type == PROXY && method.name == "canFreeUsersCreateDocsWithQuota"
    },
)

// canFreeUsersSaveOutsideDrive() — "saveOutsideDriveIsPremium" GTM
internal val CanFreeUsersSaveOutsideDriveFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(string("saveOutsideDriveIsPremium")),
    custom = { _, classDef -> classDef.type == PROXY },
)

// offerPremium() — wa/b.t()Z → upgrade-prompt visibility
internal val OfferPremiumProxyFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(methodCall(definingClass = WAB, name = "t")),
    custom = { _, classDef -> classDef.type == PROXY },
)

// canUseAddOnFonts() — "offerOfficeSuiteFontPack" GTM
internal val CanUseAddOnFontsFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(string("offerOfficeSuiteFontPack")),
    custom = { _, classDef -> classDef.type == PROXY },
)

// ═════════════════════════════════════════════════════════════════════════════
// DELEGATE LAYER — m1 + AdLogicFactory
// ═════════════════════════════════════════════════════════════════════════════

// m1.a(Z)Z — edit-mode gate; return false → callers XOR → canEdit=true
internal val M1EditGateFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Z"),
    filters = listOf(
        methodCall(definingClass = L3B, name = "b"),
        string("editModeIsPremium"),
    ),
    custom = { _, classDef -> classDef.type == M1 },
)

// AdLogicFactory.p(Z)Z — reads SerialNumber2.g:Z directly; return false = no ads
internal val AdEligibilityFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Z"),
    filters = listOf(
        string("tv"),
        fieldAccess(opcode = Opcode.IGET_BOOLEAN, definingClass = SN2, name = "g"),
        methodCall(definingClass = SN2, name = "y"),
    ),
    custom = { _, classDef -> classDef.type == AD_LOGIC },
)

// ═════════════════════════════════════════════════════════════════════════════
// MISSING GATES — discovered in full proxy audit
// ═════════════════════════════════════════════════════════════════════════════

/**
 * canUseJapaneseFonts() — FontsManager.f() + "offerOfficeSuiteJapaneseFontPack" GTM flag.
 * Distinct from canUseAddOnFonts (which uses FontsManager.d + "offerOfficeSuiteFontPack").
 */
internal val CanUseJapaneseFontsFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(string("offerOfficeSuiteJapaneseFontPack")),
    custom = { _, classDef -> classDef.type == PROXY },
)

/**
 * showOxfordDictForPremium() — reads SerialNumber2.h:Z (isPremiumWithACE, separate from g:Z).
 * h:Z is written from the encrypted license file on disk and from S()/a0() entitlement commits.
 * Our PricingPlan.d() patch covers the S()/a0() path, but the cached disk read at startup
 * will still produce h:Z=false for a free account. Patch the proxy method directly.
 *
 * Smali: if isKDDI → :cond_20; elif h:Z=0 → check wa/b.h() → return 1; :cond_20 return 0
 * Logic: returns true when user has a premium-tier inApp item OR valid inAppItem string.
 * Patch: return true unconditionally.
 *
 * Filter: unique combination of h:Z field read + wa/b.h() call in OsFeaturesCheckProxy.
 */
internal val ShowOxfordDictFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_BOOLEAN,
            definingClass = "Lcom/mobisystems/registration2/SerialNumber2;",
            name = "h",
        ),
        methodCall(definingClass = WAB, name = "h"),
    ),
    custom = { _, classDef -> classDef.type == PROXY },
)

/**
 * showQuickPdf() — delegates to MonetizationUtils.C()Z.
 * MonetizationUtils.C() reads SerialNumber2.h:Z (not g:Z) plus wa/b.u() inAppItem.
 * Same disk-cache problem as above. Patch MonetizationUtils.C() directly.
 *
 * Smali (classes9/MonetizationUtils):
 *   VersionCompatibilityUtils.C()Z  → if isKDDI → return false
 *   SerialNumber2.n() → iget h:Z    → if h:Z → return true
 *   wa/b.u()String                  → if non-null → return true
 *   ...
 * Patch: return true.
 */
internal val MonetizationUtilsShowQuickPdfFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_BOOLEAN,
            definingClass = "Lcom/mobisystems/registration2/SerialNumber2;",
            name = "h",
        ),
        methodCall(definingClass = WAB, name = "u"),
    ),
    custom = { _, classDef ->
        classDef.type == "Lcom/mobisystems/monetization/MonetizationUtils;"
    },
)
