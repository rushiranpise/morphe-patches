package app.template.patches.dooflix.protection

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// Li5/e; static (L)Z — checks private DNS servers
// v9.7: i5/e.smali ✓  v9.8: i5/e.smali ✓ (unchanged)
object DnsBlockerGateFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("L"),
    filters = listOf(
        methodCall(definingClass = "Landroid/net/ConnectivityManager;", name = "getAllNetworks"),
        methodCall(definingClass = "Landroid/net/ConnectivityManager;", name = "getLinkProperties"),
        methodCall(definingClass = "Landroid/net/LinkProperties;", name = "isPrivateDnsActive"),
    )
)

// static ()V — tamper class checker (Frida/Xposed detection via Class.forName)
// v9.7: t8/r0  v9.8: u8/q0  (class name changed, logic identical)
// Fingerprint is class-name-agnostic: static ()V + forName + SecurityException.<init>
object TamperClassCheckerFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf(),
    filters = listOf(
        methodCall(definingClass = "Ljava/lang/Class;", name = "forName"),
        methodCall(definingClass = "Ljava/lang/SecurityException;", name = "<init>"),
    )
)
