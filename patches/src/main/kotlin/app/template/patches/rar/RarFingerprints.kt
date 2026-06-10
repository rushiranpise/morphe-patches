package app.template.patches.rar

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * RarPurchase.isSubsPurchased()Z
 *
 * Returns true only when purchaseState == PRESENT and purchaseOneTime == false.
 * Fingerprinted by the PRESENT enum reference and the purchaseOneTime field read
 * unique to this method.
 */
val IsSubsPurchasedFingerprint = Fingerprint(
    definingClass = "Lcom/rarlab/rar/RarPurchase;",
    name = "isSubsPurchased",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC)
)

/**
 * AdsNotify.show(AppCompatActivity)V
 *
 * Static entry point that checks MIN_TIME_BETWEEN_NOTIFY (0xea60 ms) and
 * delegates to showMessage() to inflate the subscription-reminder dialog.
 * Prepending return-void prevents any ad/reminder dialog from ever showing.
 *
 * v7.20+: "adsnotify_lastshown" is a field value only, not used inline in
 * show() — fingerprinted by class/name/accessFlags alone.
 */
val AdsNotifyShowFingerprint = Fingerprint(
    definingClass = "Lcom/rarlab/rar/AdsNotify;",
    name = "show",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)