package app.template.patches.mobioffice.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

private const val PROXY        = "Lcom/mobisystems/registration2/OsFeaturesCheckProxy;"
private const val SN2          = "Lcom/mobisystems/registration2/SerialNumber2;"
// Q1 replaces M1 (com/mobisystems/monetization/m1 → q1) in 16.5.60515.
// N3B replaces L3B (com/mobisystems/office/l3$b → n3$b) in 16.5.60515.
// HB_B replaces WAB (wa/b → hb/b) in 16.5.60515.
// VP_E replaces L3B as the GTM flag provider (com/mobisystems/office/l3$b → vp/e).
private const val Q1           = "Lcom/mobisystems/monetization/q1;"
private const val HB_B         = "Lhb/b;"
private const val N3B          = "Lcom/mobisystems/office/n3\$b;"
private const val VP_E         = "Lvp/e;"
private const val PRICING_PLAN = "Lcom/mobisystems/registration2/types/PricingPlan;"
private const val LICENSE_LVL  = "Lcom/mobisystems/registration2/types/LicenseLevel;"

// ═════════════════════════════════════════════════════════════════════════════
// ROOT ENTITLEMENT LAYER — PricingPlan + LicenseLevel
//
// All premium state ultimately derives from PricingPlan, constructed from the
// MSConnect server FeaturesResult. Patching here propagates to:
//   PricingPlan.d()Z → SerialNumber2.g:Z (isPremium field, written at every
//                       entitlement commit: S(), a0(), C(), b0())
//   PricingPlan.c(String) → all named feature lookups ("OSP-A", "OSP-PDF", …)
//   LicenseLevel.a(String) → LicenseLevel field + plan name fallback = "premium"
// These class/method paths have been stable across every observed MobiOffice
// release — they are part of the non-obfuscated registration2 public API.
// ═════════════════════════════════════════════════════════════════════════════

/**
 * PricingPlan.d()Z — core isPremium check on the plan object.
 *
 * Reads HashMap entry for "OSP-A" and calls equalsIgnoreCase("yes").
 * Return value written directly to SerialNumber2.g:Z at every entitlement
 * write site. Returning true makes every write to g:Z produce true.
 *
 * Smali (classes10, stable):
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
 * Returning "yes" unconditionally makes every feature lookup succeed.
 *
 * Smali (classes10, stable):
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
 * PricingPlan.a = LicenseLevel.premium, which feeds getLicenseLevel() and
 * sets the plan-name fallback to "premium".
 *
 * Smali (classes10, stable):
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
// Belt-and-suspenders: patch the proxy getters so even if PricingPlan is read
// before our constructor injection fires, the results are correct.
// All proxy method names are stable public API — never obfuscated.
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

// hasPremiumFeature(String) — PricingPlan.c(String) result vs "yes"
// 16.5: no longer uses jp/e; reads PricingPlan directly.
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

// canFreeUsersEditDocs() — q1.a(false) → XOR 1 = canEdit
// 16.5: q1 replaces m1; filter updated accordingly.
internal val CanFreeUsersEditDocsFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(methodCall(definingClass = Q1, name = "a")),
    custom = { method, classDef ->
        classDef.type == PROXY && method.name == "canFreeUsersEditDocs"
    },
)

// canFreeUsersEditDocsWithQuota() — q1.a(true) → XOR 1
// 16.5: q1 replaces m1.
internal val CanFreeUsersEditDocsWithQuotaFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(methodCall(definingClass = Q1, name = "a")),
    custom = { method, classDef ->
        classDef.type == PROXY && method.name == "canFreeUsersEditDocsWithQuota"
    },
)

// canFreeUsersCreateDocs() — n3$b.b() + "createNewIsPremium" GTM via vp/e.a()
// 16.5: n3$b replaces l3$b; vp/e replaces l3$b as GTM provider.
internal val CanFreeUsersCreateDocsFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        methodCall(definingClass = N3B, name = "b"),
        string("createNewIsPremium"),
    ),
    custom = { method, classDef ->
        classDef.type == PROXY && method.name == "canFreeUsersCreateDocs"
    },
)

// canFreeUsersCreateDocsWithQuota() — same + "numFreeEditDocuments" quota check
// 16.5: n3$b + "createNewIsPremium" + "numFreeEditDocuments" (via vp/e).
internal val CanFreeUsersCreateDocsWithQuotaFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        methodCall(definingClass = N3B, name = "b"),
        string("createNewIsPremium"),
        string("numFreeEditDocuments"),
    ),
    custom = { method, classDef ->
        classDef.type == PROXY && method.name == "canFreeUsersCreateDocsWithQuota"
    },
)

// canFreeUsersSaveOutsideDrive() — "saveOutsideDriveIsPremium" GTM string (stable)
internal val CanFreeUsersSaveOutsideDriveFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(string("saveOutsideDriveIsPremium")),
    custom = { _, classDef -> classDef.type == PROXY },
)

// offerPremium() — hb/b.t()Z → upgrade-prompt visibility
// 16.5: hb/b replaces wa/b.
internal val OfferPremiumProxyFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(methodCall(definingClass = HB_B, name = "t")),
    custom = { _, classDef -> classDef.type == PROXY },
)

// canUseAddOnFonts() — "offerOfficeSuiteFontPack" GTM string (stable)
internal val CanUseAddOnFontsFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(string("offerOfficeSuiteFontPack")),
    custom = { _, classDef -> classDef.type == PROXY },
)

// canUseJapaneseFonts() — "offerOfficeSuiteJapaneseFontPack" GTM string (stable)
internal val CanUseJapaneseFontsFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(string("offerOfficeSuiteJapaneseFontPack")),
    custom = { _, classDef -> classDef.type == PROXY },
)

// ═════════════════════════════════════════════════════════════════════════════
// DELEGATE LAYER — q1 (edit gate)
// 16.5: AdLogicFactory was removed entirely. Ad eligibility is now read directly
// from SerialNumber2.g:Z across ad-integration code. Since PricingPlan.d()=true
// propagates g:Z=true at every entitlement commit, no dedicated fingerprint is
// needed. q1 replaces m1 as the edit-mode gate.
// ═════════════════════════════════════════════════════════════════════════════

// q1.a(Z)Z — edit-mode gate; return false → callers XOR → canEdit=true
// 16.5: replaces M1EditGateFingerprint (was Lcom/mobisystems/monetization/m1;).
// New filter: n3$b.b() + "editModeIsPremium" + vp/e.a().
internal val Q1EditGateFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Z"),
    filters = listOf(
        methodCall(definingClass = N3B, name = "b"),
        string("editModeIsPremium"),
        methodCall(definingClass = VP_E, name = "a"),
    ),
    custom = { _, classDef -> classDef.type == Q1 },
)

// ═════════════════════════════════════════════════════════════════════════════
// H:Z LAYER — showOxfordDictForPremium + MonetizationUtils.C() (showQuickPdf)
//
// SerialNumber2.h:Z (isPremiumWithACE) is loaded from an encrypted license
// file on disk at startup, unaffected by the PricingPlan entitlement patch.
// Methods reading h:Z must be patched directly on the proxy / MonetizationUtils.
// 16.5: hb/b replaces wa/b in both filters below.
// ═════════════════════════════════════════════════════════════════════════════

/**
 * showOxfordDictForPremium() — reads SN2.h:Z + hb/b.h() (was wa/b.h()).
 *
 * Smali (16.5):
 *   sget-boolean MonetizationUtils.a:Z       ← overwritten immediately
 *   invoke-static VersionCompatibilityUtils.C()Z
 *   invoke-static SN2.n()
 *   iget-boolean SN2.h:Z
 *   invoke-static hb/b.h()String             ← was wa/b.h()
 *   sget-object com/mobisystems/k.b:[String
 */
internal val ShowOxfordDictFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(opcode = Opcode.IGET_BOOLEAN, definingClass = SN2, name = "h"),
        methodCall(definingClass = HB_B, name = "h"),
    ),
    custom = { _, classDef -> classDef.type == PROXY },
)

/**
 * MonetizationUtils.C()Z — the showQuickPdf implementation.
 *
 * Smali (classes9, 16.5):
 *   invoke-static VersionCompatibilityUtils.C()Z → if isKDDI → return false
 *   invoke-static SN2.n() → iget h:Z → if h:Z → return true
 *   invoke-static hb/b.u()String → if non-null → return true   ← was wa/b.u()
 */
internal val MonetizationUtilsShowQuickPdfFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(opcode = Opcode.IGET_BOOLEAN, definingClass = SN2, name = "h"),
        methodCall(definingClass = HB_B, name = "u"),
    ),
    custom = { _, classDef ->
        classDef.type == "Lcom/mobisystems/monetization/MonetizationUtils;"
    },
)
