package app.template.patches.accubattery.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Targets Lab/s7;->isValid()Z — the server Offer validity check.
 *
 * s7 = the "Offer" / subscription record returned by Digibites' server check-in API.
 * isValid() returns true only if: productType != null AND expiryDate != null
 * AND expiryDate > currentTimeMillis() (or featureIds non-empty, with 24h grace).
 *
 * All string constants are DexGuard-encrypted — no string filters available.
 *
 * Stable identifiers (in instruction order within isValid()):
 *   1. opcode(CONST_WIDE_32) — the 86400000L grace-period literal.
 *      UNIQUE: only isValid() in Lab/s7; contains a CONST_WIDE_32 instruction.
 *      isExpired() and isInGracePeriod() have none.
 *   2. methodCall(System, currentTimeMillis) — immediately after the literal.
 *
 * Smali (classes/ab/s7.smali, line 647):
 *   .method public isValid()Z   ← PUBLIC, non-static, no params, returns Z
 *     invoke-virtual {p0}, getProductType()
 *     invoke-virtual {p0}, getExpiryEpochMilli()
 *     invoke-virtual {p0}, getFeatures()
 *     invoke-static  {v3}, r8lambda878h.read(Collection)Z
 *     invoke-virtual {v1}, Number.longValue()J
 *     const-wide/32 v6, 86400000    ← FINGERPRINT ANCHOR (opcode CONST_WIDE_32)
 *     invoke-static {}, System.currentTimeMillis()J
 *     cmp-long ...
 *     return true/false
 */
object OfferIsValidFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf(),
    filters = listOf(
        opcode(Opcode.CONST_WIDE_32),                      // 86400000L grace period — unique to isValid()
        methodCall(
            definingClass = "Ljava/lang/System;",
            name = "currentTimeMillis"
        ),
    )
)

/**
 * Targets Lab/s7;->isExpired()Z — the offer expiry check.
 *
 * isExpired() returns true if getExpiryEpochMilli() < currentTimeMillis().
 * Called by isInGracePeriod() and indirectly by isValid().
 *
 * Stable identifiers (instruction order in isExpired()):
 *   1. methodCall(Number, longValue) — unpacks the Long expiry value
 *   2. methodCall(System, currentTimeMillis) — compares against now
 *
 * Distinguished from isValid() by the ABSENCE of CONST_WIDE_32.
 * Distinguished from isInGracePeriod() which does NOT call currentTimeMillis directly.
 *
 * Uses classFingerprint to scope search to Lab/s7; class only, which is found
 * via OfferIsValidFingerprint.
 *
 * Smali (classes/ab/s7.smali, line 570):
 *   .method public isExpired()Z
 *     invoke-virtual {p0}, getExpiryEpochMilli()Long
 *     invoke-virtual {v1}, Number.longValue()J   ← FILTER 1
 *     invoke-static {}, System.currentTimeMillis()J  ← FILTER 2
 *     cmp-long ...
 *     return true/false
 */
object OfferIsExpiredFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf(),
    classFingerprint = OfferIsValidFingerprint,
    filters = listOf(
        methodCall(
            definingClass = "Ljava/lang/Number;",
            name = "longValue"
        ),
        methodCall(
            definingClass = "Ljava/lang/System;",
            name = "currentTimeMillis"
        ),
    )
)
