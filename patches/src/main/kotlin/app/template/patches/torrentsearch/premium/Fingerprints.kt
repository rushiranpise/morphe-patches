package app.template.patches.torrentsearch.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Matches w1/c0.c(String, Z)V — the final pro verification result dispatcher.
 *
 * This method receives the (reason, isPro) result from ProKey verification,
 * logs "Pro verification result=", posts a w1/b0 EventBus event with the isPro
 * boolean, and updates the MutableLiveData. Patching p2 to 0x1 here forces
 * all downstream paths (ads removal, pro search providers) to see isPro=true.
 *
 * Smali: classes4/w1/c0.smali — method c(Ljava/lang/String;Z)V
 * Access: public final
 * Stable anchors: "Pro verification result=" string + MutableLiveData.setValue call
 */
object ProVerificationResultFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/String;", "Z"),
    returnType = "V",
    filters = listOf(
        string("Pro verification result="),
        methodCall(
            definingClass = "Landroidx/lifecycle/MutableLiveData;",
            name = "setValue"
        ),
    )
)

/**
 * Matches w1/c0.f()Z — checks if ProKey companion app is installed.
 *
 * Called on every verification attempt. Returns false when ProKey is not
 * installed, which routes to c("prokey_not_visible", false). Returning true
 * lets the flow proceed to the actual entitlement launch.
 *
 * Smali: classes4/w1/c0.smali — method f()Z
 * Access: public final
 * Stable anchors: "ProKey package name is empty in remote config (kpg field)" string
 */
object IsProKeyVisibleFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
    filters = listOf(
        string("ProKey package name is empty in remote config (kpg field)"),
    )
)

/**
 * Matches w1/c0.b()Z — legacy installer source check.
 *
 * Returns true only if the app was installed from com.android.vending or
 * com.google.android.feedback. Controlled by Firebase Remote Config
 * "enable_installer_check". When false → c("legacy_installer_rejected", false).
 *
 * Smali: classes4/w1/c0.smali — method b()Z
 * Access: public final
 * Stable anchors: "Installer check failed:" string + installer package name list
 */
object InstallerCheckFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
    filters = listOf(
        string("Installer check failed: "),
    )
)
