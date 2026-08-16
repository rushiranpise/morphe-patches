package app.template.patches.bubbleupnp.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Matches AndroidUpnpService.n1(int, String) — the sole setter for the
 * master licence boolean Ljn;->q0:Z (AbstractApplicationC1142jn.f16941q0).
 *
 * Called by the Play Billing purchase callback (oc0 / mc0) with
 *   n1(0, "true")  on successful purchase verification
 *   n1(0, "false") otherwise.
 *
 * By injecting `const-string p2, "true"` at index 0 every downstream
 * Boolean.parseBoolean(p2) returns true, writing q0 = true into the
 * Application singleton and Lhs6;->q0:Z (renderer licence flag).
 *
 * Stable signals used (both live inside n1 body):
 *   - string("music_mode")                         — pref key written in n1
 *   - string("reset_licensed_prefs_on_startup")    — pref key written in n1
 *
 * definingClass is non-obfuscated (com.bubblesoft.android.bubbleupnp).
 */
internal object LicenseSetterFingerprint : Fingerprint(
    definingClass = "Lcom/bubblesoft/android/bubbleupnp/AndroidUpnpService;",
    name = "n1",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("I", "Ljava/lang/String;"),
    filters = listOf(
        string("music_mode"),
        string("reset_licensed_prefs_on_startup"),
    ),
)
