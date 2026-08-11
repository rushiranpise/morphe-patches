package app.template.patches.vradio

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Targets the obfuscated static method o31.d(Context): Z — the single premium gate.
 *
 * Called from MainActivity.onResume to decide whether to hide the "Go Premium" menu item:
 *   ((NavigationView) …).getMenu().findItem(R.id.action_premium)
 *       .setVisible(!o31.d(this))  ← hidden when d() returns true (= premium)
 *
 * o31.d() reads four SharedPreferences boolean flags (all with false defaults):
 *   1. "sleepTimerSecond" — toggled by purchase of the sleep-timer feature
 *   2. "appearanceP"      — appearance/theme premium flag
 *   3. "atvP"             — Android TV premium flag
 *   4. "pfaC"             — (all-in-one unlock flag)
 * Returns TRUE when any flag is set (= at least one premium feature purchased).
 *
 * returnEarly(true) makes the method permanently return true → full premium.
 *
 * Smali (classes/o31.smali, line 464):
 *   .method public static d(Landroid/content/Context;)Z
 *     476: const-string v0, "sleepTimerSecond"   ← filter 0
 *     490: const-string v0, "appearanceP"         ← filter 1
 *     504: const-string v0, "atvP"                ← filter 2
 *     516: const-string v0, "pfaC"                ← filter 3
 *
 * Fingerprint is update-stable: the four SharedPreferences keys are human-readable
 * product feature names and are unlikely to be renamed. The class name o31 is
 * obfuscated and will change; the strings + access flags uniquely identify the method.
 *
 * Verified unique: only o31.smali contains all four keys in a public-static-Z method.
 * (m2.smali reads "pfaC" as well but from an instance method that does UI layout.)
 */
internal val IsPremiumFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("sleepTimerSecond", "appearanceP", "atvP", "pfaC"),
    filters = listOf(
        string("sleepTimerSecond"),
        string("appearanceP"),
        string("atvP"),
        string("pfaC"),
    )
)
