package app.template.patches.teams.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// =============================================================================
// Microsoft Teams (com.microsoft.teams) — License Fingerprints
// =============================================================================
//
// Teams does NOT use the Office licensing stack (OHubUtil, LicensingState,
// LicenseInfo, NativeProxy). Its licensing model:
//
//   Auth service → SkypeToken JWT
//     → EnterpriseSkypeToken.LicenseDetails (parsed JSON booleans)
//       → SkypeTokenLicenseDetails (adapter: copies each field to mXxx:Z)
//         → AuthenticatedUser.licenseDetails
//           → UserConfiguration / TflUserConfiguration reads via interface
//             → All feature gates in the app
//
// All class names and method names are non-obfuscated interface contracts.
// Teams compiles with an unknown/modified DEX header (not standard R8) but
// the naming is stable — interface method names cannot be renamed without
// breaking the entire ISkypeTokenLicenseDetails / IUserConfiguration hierarchy.
//
// =============================================================================

// ---------------------------------------------------------------------------
// Layer 1 — SkypeTokenLicenseDetailsConstructorFingerprint
// ---------------------------------------------------------------------------
// The DEEPEST patch point: override all premium boolean fields in the
// SkypeTokenLicenseDetails constructor, immediately before return-void.
//
// This is the adapter that maps EnterpriseSkypeToken.LicenseDetails (parsed
// from the SkypeToken JWT) to the internal field set. Overriding here means
// every downstream read — across all 12 DEX files — sees the patched values.
//
// Constructor signature (smali-verified, classes9, .registers 3):
//   <init>(Lcom/microsoft/skype/teams/models/responses/skypetoken/EnterpriseSkypeToken$LicenseDetails;)V
//
// Field map (all iput-boolean in order, instruction indices 0-37):
//   [2]  mIsFreemium:Z              ← force FALSE (free-tier cap removal)
//   [4]  mIsTrial:Z                 ← force FALSE (hide trial UI)
//   [12] mIsTPManagement:Z          ← force TRUE  (Teams Premium management)
//   [14] mIsTeamsPremiumSelfAssigned:Z ← force TRUE
//   [16] mIsTPProtection:Z          ← force TRUE  (Teams Premium protection)
//   [18] mHasM365CopilotLicense:Z   ← force TRUE  (Copilot in meetings/chat)
//   [20] mIsGroupCopilot:Z          ← force TRUE  (Copilot in group chats)
//   [22] mHasM365CopilotBusinessChatLicense:Z ← force TRUE
//   [24] mHasTPCustomizationLicense:Z ← force TRUE (Teams Premium branding)
//   [26] mHasAdvCommsLicense:Z      ← force TRUE  (Advanced Comms add-on)
//   [36] mIsInfoProtectionPremium:Z ← force TRUE  (Info Protection Premium)
//
// Injection at index 37 (before return-void), using v0 as scratch (free at end):
//   const/4 v0, 0x1  → iput-boolean all premium TRUE fields
//   const/4 v0, 0x0  → iput-boolean mIsFreemium, mIsTrial
//
// Fingerprint anchor: non-obfuscated definingClass + name + full parameter list.
// EnterpriseSkypeToken$LicenseDetails is itself non-obfuscated — stable.
internal val licenseDetailsConstructorFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/skype/teams/models/responses/skypetoken/SkypeTokenLicenseDetails;",
    name = "<init>",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    parameters = listOf(
        "Lcom/microsoft/skype/teams/models/responses/skypetoken/EnterpriseSkypeToken\$LicenseDetails;",
    ),
)

// ---------------------------------------------------------------------------
// Layer 2 — AuthenticatedUser license getter fingerprints
// ---------------------------------------------------------------------------
// AuthenticatedUser wraps SkypeTokenLicenseDetails and is the call-site for
// all ViewModels and UserConfiguration readers. Each getter is:
//   iget-object p0, p0, AuthenticatedUser;->licenseDetails:SkypeTokenLicenseDetails;
//   if-eqz p0, :L0
//   invoke-virtual {p0}, SkypeTokenLicenseDetails;->mXxx()Z
//   [return 1 or 0]
//
// With Layer 1 in place these already return true. Layer 2 catches any path
// that constructs AuthenticatedUser WITHOUT going through the constructor
// (e.g. deserialization, mock objects in tests, cached tokens loaded from disk).

// isTeamsPremiumSelfAssigned() — unlocks meeting templates, recaps, branding,
// watermarks, advanced webinars, VirtualApp, intelligent recap.
internal val isTeamsPremiumFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/skype/teams/models/AuthenticatedUser;",
    name = "isTeamsPremiumSelfAssigned",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
)

// hasM365CopilotLicense() — no-param overload used by UserConfiguration.
internal val hasCopilotLicenseNoParamFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/skype/teams/models/AuthenticatedUser;",
    name = "hasM365CopilotLicense",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
)

// hasM365CopilotLicense(Z) — param = fallback default value.
internal val hasCopilotLicenseFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/skype/teams/models/AuthenticatedUser;",
    name = "hasM365CopilotLicense",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf("Z"),
)

// hasGroupCopilotLicense(Z) — Copilot in group chat threads.
internal val hasGroupCopilotFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/skype/teams/models/AuthenticatedUser;",
    name = "hasGroupCopilotLicense",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf("Z"),
)

// hasM365CopilotBusinessChatLicense(Z) — Microsoft 365 Business Chat (BizChat).
internal val hasBizChatCopilotFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/skype/teams/models/AuthenticatedUser;",
    name = "hasM365CopilotBusinessChatLicense",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf("Z"),
)

// isFreemiumUser() — returns true on free consumer tier (60-min meeting cap,
// no recordings, no scheduling). Force false to claim paid-tier status.
// The abstract declaration in TeamsUser (ABSTRACT flag) and ITeamsUser are
// excluded by accessFlags = PUBLIC (non-abstract).
internal val isFreemiumUserFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/skype/teams/models/AuthenticatedUser;",
    name = "isFreemiumUser",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
)

// ---------------------------------------------------------------------------
// Layer 3 — TflUserConfiguration Copilot gate fingerprints
// ---------------------------------------------------------------------------
// TflUserConfiguration.isCopilotLicenseRequired() reads "openai/copilotLicenseRequired"
// from ECS (server-side experiment flag, default true). Force false → Copilot
// unlocked regardless of server configuration.
//
// isCopilotLicenseAvailable() reads "copilotLicenseType" pref (int, compared to
// CopilotLicenseType.NONE). Force true → Copilot treated as available.
//
// Note: UserConfiguration.isCopilotLicenseRequired() (the base class) already
// returns const/4 p0, 0 (false) — only TflUserConfiguration needs patching.

internal val copilotLicenseRequiredFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/skype/teams/services/configuration/TflUserConfiguration;",
    name = "isCopilotLicenseRequired",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)

internal val copilotLicenseAvailableFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/skype/teams/services/configuration/TflUserConfiguration;",
    name = "isCopilotLicenseAvailable",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)

// ---------------------------------------------------------------------------
// Layer 4 — validateLicenseDetailsFingerprint (consumer license upsell screen)
// ---------------------------------------------------------------------------
// The upsell screen ("Benefits with Microsoft 365", 60 min / 100 participants)
// is driven by a SEPARATE licensing model from the SkypeToken enterprise stack:
//
//   ConsumerSkypeToken$ConsumerLicenseDetailsJson (from server, field hasTeamsLicense=false)
//     → TeamsLicenseParsingLogic.validateLicenseDetails()
//       → TeamsLicenseInfo$FreeLicenseInfo(restrictions: 60min, 100 participants)
//         → TeamsLicenseRepository.getLicenseDetailsFlow() StateFlow
//           → UpsellBenefitsFragment reads limits and shows upgrade screen
//
// Our SkypeTokenLicenseDetails constructor patch targets the ENTERPRISE token path.
// This is the CONSUMER token path — completely separate class hierarchy.
//
// validateLicenseDetails(Z, ConsumerLicenseDetailsJson, ScenarioContext):
//   reads ConsumerLicenseDetailsJson.hasTeamsLicense (server sends false for free accounts)
//   → if false → creates FreeLicenseInfo with 60 min/100 participant limits
//   → if true  → creates PaidLicenseInfo with unlimited restrictions
//
// Fix: inject at index 0, construct PaidLicenseInfo with max integer limits for
// meeting duration and participants, copilotEnabled=true, callRecordingEnabled=true,
// wrap in ConsumerLicenseDetails, and return immediately.
// This bypasses the server response entirely.
//
// Fingerprint: definingClass + name + parameters (all non-obfuscated stable names).
// Verified unique: only one definition across all 12 DEX files (classes11).
//
// Smali verified (.registers 10, static method):
//   p0=Z (isPaidRestrictionsEnforced), p1=ConsumerLicenseDetailsJson, p2=ScenarioContext
//   v0-v6 available as scratch registers
internal val validateLicenseDetailsFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/teams/license/TeamsLicenseParsingLogic;",
    name = "validateLicenseDetails",
    returnType = "Lcom/microsoft/teams/license/model/ConsumerLicenseDetails;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf(
        "Z",
        "Lcom/microsoft/skype/teams/models/responses/skypetoken/ConsumerSkypeToken\$ConsumerLicenseDetailsJson;",
        "Lcom/microsoft/skype/teams/services/diagnostics/ScenarioContext;",
    ),
)
