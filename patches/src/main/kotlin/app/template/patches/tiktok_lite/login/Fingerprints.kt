package app.template.patches.tiktok_lite.login

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// MandatoryLoginService.L(String)Z -- "enableForcedLogin(flow)".
// Checks if the given flow string is in an enabled-flows list, returning true to SHOW
// the login gate. Patched to always return false.
// Fingerprinted by: stable List->contains call on the first param (flow string check).
// DEX: classes4. PUBLIC FINAL, 1 String param, returns Z.
internal object MandatoryLoginEnableFlowFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/mini_account_impl/MandatoryLoginService;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    filters = listOf(
        methodCall(
            definingClass = "Ljava/util/List;",
            name = "contains",
        ),
    ),
)

// MandatoryLoginService.LB(String)Z -- "shouldShowForcedLogin(flow)".
// Reads the Keva SharedPrefs-like store to check if login was deferred for this flow.
// Patched to always return false (never show login).
// DEX: classes4. Fingerprinted by definingClass suffix and same signature.
internal object MandatoryLoginShouldShowFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/mini_account_impl/MandatoryLoginService;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/bytedance/keva/Keva;",
            name = "getBoolean",
        ),
    ),
)

// MandatoryLoginService.LCC(String, String)Z -- calls LBL() then does further checks.
// Patched to return false to suppress the secondary login trigger paths.
// DEX: classes4. PUBLIC FINAL, two String params, returns Z.
internal object MandatoryLoginSecondaryFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/mini_account_impl/MandatoryLoginService;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;"),
)

// X/5v2.L()Z -- feature-flag gate consulted by LBL() to determine whether forced login
// is active at all ("lark_inhouse" experiment). Patched to always return false so LBL()
// itself short-circuits and returns false without reaching canSkipForcedLoginPanel().
// DEX: classes3. PUBLIC STATIC, no params, returns Z.
// Fingerprinted by the stable string literal "lark_inhouse" it checks.
internal object ForcedLoginFeatureFlagFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        string("lark_inhouse"),
    ),
)
