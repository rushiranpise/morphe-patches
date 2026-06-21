package app.template.patches.inscodeautoclicker

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// Lcom/zipoapps/premiumhelper/d;->x()Z — reads "has_active_purchase" from SharedPreferences.
val HasActivePurchaseFingerprint = Fingerprint(
    definingClass = "Lcom/zipoapps/premiumhelper/d;",
    name = "x",
    parameters = emptyList(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(string("has_active_purchase"))
)