package app.template.patches.autocursor.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Targets gp1->y()Z — the single root unlock check.
 *
 * Full call chain:
 *   MainPref.unlocked()
 *     → gp1.y()Z
 *       → gp1.m()String   (Base64+XOR key decoder → "IS_PURCHASED_PREF")
 *       → ym0.T(String, Z)Z  (SharedPreferences.getBoolean wrapper)
 *
 * unlocked() is called from AllPref constructor, 6+ UI classes, the cursor
 * service, and menu activity — patching y() at the root covers everything.
 *
 * Smali (classes/gp1.smali):
 *   .method public static y()Z
 *     .registers 2
 *       invoke-static { }, Lgp1;->m()Ljava/lang/String;   ← FILTER 1
 *       move-result-object v0
 *       const/4 v1, 0
 *       invoke-static { v0, v1 }, Lym0;->T(Ljava/lang/String;Z)Z  ← FILTER 2
 *       move-result v0
 *       return v0
 *
 * Uniqueness: two other public static ()Z methods call Lym0;->T but none
 * also call Lgp1;->m() — the combination is unique to y().
 */
object IsUnlockedFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf(),
    filters = listOf(
        methodCall(definingClass = "Lgp1;", name = "m"),
        methodCall(definingClass = "Lym0;", name = "T"),
    )
)
