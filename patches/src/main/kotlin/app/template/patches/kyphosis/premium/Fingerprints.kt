package app.template.patches.kyphosis.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// App.isPremium():Z — reads SharedPreferences key "sp_premium".
// Central user subscription gate called by all model classes.
object AppIsPremiumFingerprint : Fingerprint(
    definingClass = "Lair/com/musclemotion/App;",
    name = "isPremium",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)

// LoginResponse.isPaid():Z — user-level paid flag from login API.
object LoginResponseIsPaidFingerprint : Fingerprint(
    definingClass = "Lair/com/musclemotion/entities/response/LoginResponse;",
    name = "isPaid",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)

// AssetCJ.isPaid():Z — content-level gate for exercises/anatomy assets (Realm entity).
object AssetCJIsPaidFingerprint : Fingerprint(
    definingClass = "Lair/com/musclemotion/entities/AssetCJ;",
    name = "isPaid",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)

// NewsItem.isPaid():Z — content-level gate for news/popular items (Realm entity).
object NewsItemIsPaidFingerprint : Fingerprint(
    definingClass = "Lair/com/musclemotion/entities/NewsItem;",
    name = "isPaid",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)
