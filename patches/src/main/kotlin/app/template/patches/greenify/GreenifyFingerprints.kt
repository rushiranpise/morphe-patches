package app.template.patches.greenify

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * W1.Q(Context, Z)I — master premium status check.
 *
 * Returns integer status: 2 = premium, 1 = Play-purchased (needs donation verify), -6 = none.
 * Fingerprinted by exact name, params, and unique log string.
 */
val PremiumStatusCheckFingerprint = Fingerprint(
    definingClass = "LW1;",
    name = "Q",
    parameters = listOf("Landroid/content/Context;", "Z"),
    returnType = "I",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    filters = listOf(
        string("donation_package_no_signature"),
        string("donation_package_resigned")
    )
)

/**
 * W1.D(Context)Z — donation package signature verifier.
 *
 * Validates com.oasisfeng.greenify.pro signature. Uses name + params only;
 * no filters to avoid ordering ambiguity with other W1 methods.
 */
val DonationVerifierFingerprint = Fingerprint(
    definingClass = "LW1;",
    name = "D",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

/**
 * W1.E(Context)Z — China-region free premium eligibility check.
 *
 * Returns true when device locale/timezone indicates mainland China.
 * Uses name + params only for reliable matching.
 */
val ChinaRegionCheckFingerprint = Fingerprint(
    definingClass = "LW1;",
    name = "E",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    filters = listOf(
        string("cn"),
        string("China")
    )
)
