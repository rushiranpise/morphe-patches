package app.template.patches.waze

import app.morphe.patcher.Fingerprint

// AdvilRequest.getPageUrl() — returns the ad request page URL
// MOD: stub to return "" so no ad page URL is sent → ad server returns nothing
// ORIG: iget-object v0, v0, Lcom/waze/jni/protos/AdvilRequest;->pageUrl_ Ljava/lang/String;
//       return-object v0
object AdvilRequestGetPageUrlFingerprint : Fingerprint(
    definingClass = "Lcom/waze/jni/protos/AdvilRequest;",
    name = "getPageUrl"
)

// SpeedometerView update method — contains const/16 20 and const/16 13 text size values
// [131] const/16 v7, 20  [132] goto  [133] const/16 v7, 13
object SpeedometerUpdateFingerprint : Fingerprint(
    definingClass = "Lcom/waze/main_screen/h/b/d;",
    strings = listOf("SpeedometerView: Not shown. (speed == ")
)

// ConfigManager.onConfigSyncedFromServer() — called on every server config sync
// Inject setConfigValueBoolNTV(632, true) here to force
// CONFIG_VALUE_ALERTS_PLAY_SPEED_CAMERA_SOUND_BELOW_SPEED_LIMIT = true
object ConfigManagerOnConfigSyncedFingerprint : Fingerprint(
    definingClass = "Lcom/waze/ConfigManager;",
    name = "onConfigSyncedFromServer"
)
