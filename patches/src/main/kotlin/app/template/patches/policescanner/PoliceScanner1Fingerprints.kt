package app.template.patches.policescanner

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * H9/p.b(Purchase)Z — purchase validity / expiry check.
 *
 * Returns true when the subscription has not expired.
 * Fingerprinted by "Subscription expired: " which is unique to this method.
 */
internal val PurchaseValidityFingerprint = Fingerprint(
    definingClass = "LH9/p;",
    name = "b",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Lcom/android/billingclient/api/Purchase;"),
    filters = listOf(string("Subscription expired: ")),
)

/**
 * I9/l.<init>(Z)V — PremiumStatus data-class constructor.
 *
 * All in-memory PremiumStatus creation (iap/a, iap/b) goes through here.
 * Patch: swap iput-boolean p1 → iput-boolean v0 (v0=0x1 from id assignment above).
 */
internal val PremiumStatusConstructorFingerprint = Fingerprint(
    definingClass = "LI9/l;",
    name = "<init>",
    parameters = listOf("Z"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
)

/**
 * I9/k$a.call()Object — Room Callable: reads entitled from premium_status DB table.
 *
 * This is what the LiveData observer actually emits to the UI.
 * The try-catch block means we cannot removeInstructions (causes VerifyError).
 *
 * Safe patch: replace instruction 11 "move-result v1" (1 code unit) with
 * "const/4 v1, 0x1". This forces the cursor.getInt() result to 1 (true),
 * so "if-eqz v1, :cond_0" never branches and entitled is always set to true.
 * The replacement is inside the try block and does not touch the exception table.
 *
 * Fingerprinted by definingClass + name "call" + return type.
 */
internal val DbReaderCallFingerprint = Fingerprint(
    definingClass = "LI9/k\$a;",
    name = "call",
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)
