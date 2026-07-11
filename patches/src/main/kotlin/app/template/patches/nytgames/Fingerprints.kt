package app.template.patches.nytgames

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// NYT Games (com.nytimes.crossword) v6.33.0
//
// Subscription architecture:
//   NYT subauth library → SubauthEntitlementClientImpl.r() = isSubscribed()
//   delegates via Subauth.b() → NYTUser.b() (isSubscribed)
//   → GamesSubauth2PurchaseClientImpl.w() → SubauthRxJavaClientImpl.b()
//   → AppEntitlementsImpl.b() → AccountSettingsViewModel.initEntitlements$1
//   → UserEntitlements(isSubscribed, isLoggedIn, email, isStoreSubscription)
//
// "Oops, something went wrong" on the Me page:
//   Patching AppEntitlementsImpl.c() (isLoggedIn) to return true causes the app
//   to attempt loading the user profile via SubauthRxJavaClient.x() (getMe network call)
//   without a real session, which fails and shows the error page.
//   Fix: do NOT patch isLoggedIn — only patch isSubscribed.
//
// Patch strategy (single target):
//   SubauthEntitlementClientImpl.r() → always return true
//   This propagates through the full delegate chain:
//   r() → GamesSubauth2PurchaseClientImpl.w() → SubauthRxJavaClientImpl.b()
//   → AppEntitlementsImpl.b() → UserEntitlements.a (isSubscribed field)
//   All game content, crossword puzzles, and archive access unlock.

/**
 * Matches SubauthEntitlementClientImpl.r()Z — the isSubscribed() method.
 * Identified by two sequential interface delegate invocations:
 *   Subauth.b() → NYTUser chain.
 * Patching to always return true propagates through all delegates.
 */
internal object IsSubscribedFingerprint : Fingerprint(
    definingClass = "Lcom/nytimes/games/integrations/subauth/library/SubauthEntitlementClientImpl;",
    name = "r",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        opcode(Opcode.INVOKE_VIRTUAL),
        opcode(Opcode.INVOKE_INTERFACE),
    ),
)
