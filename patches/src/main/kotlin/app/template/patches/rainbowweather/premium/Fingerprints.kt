package app.template.patches.rainbowweather.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Matches eq.e.invokeSuspend() — the Adapty profile success callback that writes PaymentInfoDataStore.
 *
 * Full trace (classes3/eq/e.smali = p112eq.C3092e):
 *   1. Adapty.getProfile() returns AdaptyResult.Success → this coroutine resumes
 *   2. Reads accessLevels.get("premium") → AccessLevel (null if no subscription)
 *   3. Logic at :cond_226:
 *        if-eqz v18 (AccessLevel), jump → v3=0 (isPremium=false)
 *        invoke isActive() → if false, jump → v3=0
 *        else v3=1 (isPremium=true)
 *   4. invoke-direct {v5, v2, v3}, Ldn/e0;-><init>(String;Z)V  ← writes PaymentInfoDataStore
 *   5. const-string "savePaymentInfo -> "  ← stable anchor (immediately after #4)
 *
 * Fix: insert `const/4 v3, 0x1` before the Ldn/e0;-><init> call so isPremium is always true
 * regardless of whether the "premium" access level key exists or isActive() returns false.
 *
 * Filters (in smali order):
 *   methodCall(Ldn/e0;-><init>(String;Z)V)  — the DataStore constructor write
 *   string("savePaymentInfo -> ")           — stable log string immediately after
 */
object SavePaymentInfoFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/adapty/utils/AdaptyResult\$Success;",
            name = "getValue",
        ),
        methodCall(
            definingClass = "Lcom/adapty/models/AdaptyProfile;",
            name = "getAccessLevels",
        ),
        methodCall(
            definingClass = "Lcom/adapty/utils/ImmutableMap;",
            name = "get",
        ),
        methodCall(
            definingClass = "Lcom/adapty/models/AdaptyProfile\$AccessLevel;",
            name = "isActive",
        ),
        methodCall(
            definingClass = "Ldn/e0;",
            name = "<init>",
        ),
        string("savePaymentInfo -> "),
    ),
)
