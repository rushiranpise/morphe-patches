package app.template.patches.calimoto.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ── Calimoto membership getter ────────────────────────────────────────────────
//
// Calimoto's entire premium state flows through one static top-level method
// that returns a Membership enum value, called from every feature gate in the
// app. Both the containing class and the Membership enum type have already
// been observed to rotate names between versions carrying real functional
// changes:
//   2026.07.2: com.calimoto.calimoto.parse.user.b#H()Lm7/a;
//   2026.07.5: com.calimoto.calimoto.parse.user.a#H()Lc38;
// (method name "H" itself happens to be stable across both, but that's
// coincidence, not something to rely on — this fingerprint doesn't use it.)
//
// None of those names are hardcoded below. Instead the method is located
// purely by two remote-config flag-name string literals it reads via a
// stable-shaped call — "allMaps" and "subscriptionAndroid" — which are
// genuine app-level config keys with no reason for R8 to ever rename their
// *contents* (only the surrounding class/method identifiers get renamed).
//
// Verified smali (com/calimoto/calimoto/parse/user/a.smali, H(), 2026.07.5):
//   sget-object v0, Lc38;->d:Lc38;               # NONE (default)
//   invoke-static {}, ...;->J()Lc38;
//   ...
//   const-string v2, "allMaps"                    ← filter[0]
//   invoke-static {v2}, Laje;->f(Ljava/lang/String;)Z
//   move-result v2
//   if-eqz v2, :cond_19
//   sget-object v0, Lc38;->q:Lc38;                 ← filter[1] (LIFETIME shortcut)
//   return-object v0
//   :cond_19
//   const-string v2, "subscriptionAndroid"         ← filter[2]
//   ...
//
// filter[1] (the SGET_OBJECT of the LIFETIME shortcut) is captured
// specifically so the patch can read its field reference back out at
// execute-time and reuse it verbatim — meaning the injected replacement in
// CaliMotoPremiumPatch.kt never needs to hardcode the Membership enum's type
// name or its LIFETIME constant's (also obfuscated) field name either.
object MembershipGetterFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf(),
    filters = listOf(
        string("allMaps"),
        opcode(Opcode.SGET_OBJECT),
        string("subscriptionAndroid"),
    ),
)
