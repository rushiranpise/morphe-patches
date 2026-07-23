package app.template.patches.networkguru.license

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// Targets P4/L.b(P4/L, Z) — the subscription state setter that posts to a MutableLiveData.
//
// P4/L is the "PremiumManager" controller (obfuscated). Its field `k` is a
// MutableLiveData<Boolean> (androidx.lifecycle.U) observed by MainActivity, FragmentMain,
// FragmentAppUsageDetailed, and FragmentSupport to show/hide ads and premium UI.
//
// P4/L.b() is called from:
//   Q4/l (billing purchase query result — product IDs: one_year, one_month, one_week subscription)
//   P4/J (ad reward callback — also grants premium via this setter)
//   A3/c (another billing path)
//
// When true is posted: P4/H observer hides AdMob banner, destroys ad, hides ad containers.
// When false is posted: ads are shown.
//
// Uniqueness: string "SUBSCRIBED" + methodCall(lifecycle/U;->i) + public static final (L...;Z)V → 1 match.
// Verified in classes.dex: P4/L.smali .method public static final b(LP4/L;Z)V
// Filter order matches smali instruction sequence exactly.
val SubscriptionLiveDataSetterFingerprint = Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    strings = listOf("SUBSCRIBED"),
    filters = listOf(
        methodCall(
            definingClass = "Ljava/lang/String;",
            name = "valueOf",
        ),
        methodCall(
            definingClass = "Landroid/util/Log;",
            name = "e",
        ),
        methodCall(
            definingClass = "Ljava/lang/Boolean;",
            name = "valueOf",
        ),
        methodCall(
            definingClass = "Landroidx/lifecycle/U;",
            name = "i",
        ),
    ),
)
