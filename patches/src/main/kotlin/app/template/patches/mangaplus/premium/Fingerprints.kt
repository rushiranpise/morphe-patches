package app.template.patches.mangaplus.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Targets: xa6.e(String)Lrb7; in classes3
 *
 * This static method converts the server-provided planType string
 * ("deluxe", "standard", or anything else = basic) into the
 * SubscriptionPlan enum (Lrb7;). It is called everywhere the app
 * needs to compare or display the user's subscription tier.
 *
 * Stable identifiers: PUBLIC STATIC, single String param,
 * returns the enum type, uses string literals "deluxe" and "standard"
 * (SDK-independent app constants — not from any third-party library).
 *
 * NOTE: The return type is an obfuscated class (Lrb7;) whose name
 * will change across versions. The fingerprint relies on the string
 * literals and the method signature shape, not on the class name.
 */
object SubscriptionPlanDeserializerFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Ljava/lang/String;"),
    filters = listOf(
        string("deluxe"),
        string("standard"),
    )
)
