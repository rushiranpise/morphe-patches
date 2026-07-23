package app.template.patches.calimoto.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// com.calimoto.calimoto.parse.user.b.H() — the single top-level membership getter.
// Returns m7/a (Membership enum): NONE, WEEKLY, SEASON_PASS, YEARLY, LIFETIME, TRIAL, NAVIGATION_PACKAGE.
// Called 13+ times across classes3 and classes4 for every premium gate in the app.
//
// The class name "b" is obfuscated but the package path com.calimoto.calimoto.parse.user
// is non-obfuscated and stable. The method name "H" is obfuscated — we cannot use
// definingClass+name directly. Instead we fingerprint by stable SDK call sites inside
// the method body.
//
// Verified smali (classes3, com/calimoto/calimoto/parse/user/b.smali line 232):
//   .method public static H()Lm7/a;
//     .locals 4
//     sget-object v0, Lm7/a;->d:Lm7/a;        # NONE default
//     invoke-static {}, ...b;->J()Lm7/a;
//     move-result-object v1
//     if-eqz v1, :cond_0
//     invoke-static {}, ...b;->J()Lm7/a;
//     move-result-object v1
//     goto :goto_0
//     :cond_0
//     move-object v1, v0
//     :goto_0
//     const-string v2, "allMaps"              ← filter[0]
//     invoke-static {v2}, Lm7/c;->f(Ljava/lang/String;)Z  ← filter[1]
//     move-result v2
//     if-eqz v2, :cond_1
//     sget-object v0, Lm7/a;->q:Lm7/a;       # LIFETIME shortcut if allMaps flag
//     return-object v0
//     :cond_1
//     const-string v2, "subscriptionAndroid"   ← filter[2]
//     invoke-static {v2}, Lm7/c;->f(Ljava/lang/String;)Z  ← filter[3]
//
// Patch: return LIFETIME (m7/a.q) immediately from H().
object MembershipGetterFingerprint : Fingerprint(
    returnType = "Lm7/a;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf(),
    filters = listOf(
        string("allMaps"),
        methodCall(
            definingClass = "Lm7/c;",
            name = "f",
        ),
        string("subscriptionAndroid"),
    ),
)
