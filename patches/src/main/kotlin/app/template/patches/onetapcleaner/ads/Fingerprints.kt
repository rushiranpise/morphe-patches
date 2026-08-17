package app.template.patches.onetapcleaner.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// ── AdStateFingerprint ─────────────────────────────────────────────────────────
//
// Targets: cyq.m5937(Context, List)V  — the method that sets ipi.f25332 (the
// isPro ad-suppressor flag) based on purchase verification.
//
// Ad display gate: bxt.m4114() returns true (show ads) when:
//   ipi.f25330.f25332.get() == false   (not Pro → show ads)
//   ipi.f25330.f25334.get() == true    (GMA initialized)
//   ipi.f25330.f25333.get() == false   (no ad-free reward active)
//
// cyq.m5937() is the ONLY writer of f25332. It guards itself with a signature
// hash check before setting f25332 = true:
//   l20.m13765(context).hashCode() != -1105923880 → return early (no set)
// On a re-signed APK the hash never matches → f25332 stays false → ads show,
// even though BillingManager.m5236() returns true.
//
// Fingerprint: public static void, params (Context, List) — uniquely identified
// by the stable non-obfuscated LicWnd.m5182() call inside the method body.
//
object AdStateFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;", "Ljava/util/List;"),
    filters = listOf(
        // LicWnd is a stable non-obfuscated class — safe to pin across versions
        methodCall(
            definingClass = "Lcom/a0soft/gphone/acc/wnd/LicWnd;",
            name = "m5182",
        ),
    ),
)
