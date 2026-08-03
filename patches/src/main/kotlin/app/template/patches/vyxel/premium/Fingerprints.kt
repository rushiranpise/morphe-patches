package app.template.patches.vyxel.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// Targets com.vythera.vyxelapps.PreferencesManager.loadLiquidGlassUnlocked()
// This is the sole gate that determines whether premium themes (Liquid Glass Light/Dark,
// Neon Punk, Cyberpunk) are unlocked. Called at startup in AppViewModel.init and
// consumed by UiState.liquidGlassUnlocked.
//
// Normal flow:
//   - reads "lg_unlocked" from tokenPrefs (EncryptedSharedPreferences)
//   - falls back to "lg_unlocked_fb" from plain vyxel_prefs SharedPreferences
//   - returns false by default → themes locked behind Gumroad license key API
//
// Patch strategy: returnEarly(true) — always reports unlocked at the read site,
// making the server-side Gumroad verification irrelevant.
//
// Smali verified — DEX: classes (single DEX app)
//   .method public final loadLiquidGlassUnlocked()Z
//   iget-object v0, p0, Lcom/vythera/vyxelapps/PreferencesManager;->tokenPrefs:Landroid/content/SharedPreferences;
//   const/4 v1, 0
//   if-eqz v0, :L0
//   const-string p0, "lg_unlocked"
//   invoke-interface { v0, p0, v1 }, Landroid/content/SharedPreferences;->getBoolean(Ljava/lang/String;Z)Z
//   move-result p0
//   return p0
//   :L0
//   iget-object p0, p0, Lcom/vythera/vyxelapps/PreferencesManager;->prefs:Landroid/content/SharedPreferences;
//   const-string v0, "lg_unlocked_fb"
//   invoke-interface { p0, v0, v1 }, Landroid/content/SharedPreferences;->getBoolean(Ljava/lang/String;Z)Z
//   move-result p0
//   return p0
object LoadLiquidGlassUnlockedFingerprint : Fingerprint(
    definingClass = "Lcom/vythera/vyxelapps/PreferencesManager;",
    name = "loadLiquidGlassUnlocked",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        string("lg_unlocked"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getBoolean"
        ),
    )
)
