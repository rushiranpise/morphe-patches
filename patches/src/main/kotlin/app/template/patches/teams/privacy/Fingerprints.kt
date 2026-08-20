package app.template.patches.teams.privacy

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// =============================================================================
// Microsoft Teams — Privacy & Presence Fingerprints
// =============================================================================

// ---------------------------------------------------------------------------
// shouldShowReadReceipts (privacy)
// ---------------------------------------------------------------------------
// Controls whether the app sends read receipts to message senders.
// Reads "Read_Receipts_Enabled" boolean from IPreferences (SharedPrefs).
//
// Returning false means YOUR read receipts are never transmitted to others —
// senders cannot see that you read their messages. This does NOT affect
// incoming read receipts (you still see who read yours if they send them).
//
// Two implementations exist — only UserConfiguration is the authoritative one:
//   UserConfiguration.shouldShowReadReceipts()  ← called by ReadReceiptsHandler
//   ChatFragmentViewModel.shouldShowReadReceipts() ← mirrors same pref
//
// Fingerprint: definingClass + name + string anchor uniquely identifies the
// UserConfiguration implementation vs the ViewModel mirror.
//
// Smali verified (classes9, UserConfiguration, .registers 4):
//   iget-object v0, p0, UserConfiguration;->mAuthenticatedUser:AuthenticatedUser;
//   [null check]
//   invoke-virtual {v0}, AuthenticatedUser;->getUserObjectId()String;
//   move-result-object v0
//   iget-object p0, p0, UserConfiguration;->mPreferences:IPreferences;
//   const-string v2, "Read_Receipts_Enabled"
//   invoke-interface {p0, v2, v0, v1}, IPreferences;->getBooleanUserPref(String;String;Z)Z
//   move-result p0
//   return p0
internal val readReceiptsFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/skype/teams/services/configuration/UserConfiguration;",
    name = "shouldShowReadReceipts",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        string("Read_Receipts_Enabled"),
    ),
)

// ---------------------------------------------------------------------------
// getIsUserInBlockingMode (quiet hours / shift-end policy gate)
// ---------------------------------------------------------------------------
// Returns true when the user's "quiet hours" or shift-end policy is active,
// which blocks presence from being reported and forces status to "away".
//
// Returning false prevents the shift-end policy from ever activating, so Teams
// continues to report your actual presence regardless of admin-configured
// quiet-hours schedules.
//
// Note on "always online": Teams presence is server-managed via heartbeats.
// The client only sends status updates; the server expires presence if no
// heartbeat arrives. There is no client-side "force Available" flag that
// survives network gaps. This patch is the best achievable: it ensures the
// app never voluntarily degrades your presence via policy restrictions.
//
// Smali: definingClass + name, classes9, .registers 5, accessFlags PUBLIC FINAL.
internal val blockingModeFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/skype/teams/utilities/PresenceOffShiftHelper;",
    name = "getIsUserInBlockingMode",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)
