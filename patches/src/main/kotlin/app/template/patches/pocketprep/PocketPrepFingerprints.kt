package app.template.patches.pocketprep

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// ── Subscription instance methods ──────────────────────────────────────────────
// All methods live on com.pocketprep.android.api.common.Subscription and are
// stable across APK variants (unobfuscated defining class, obfuscated method names
// a–g). Fingerprinted by definingClass+name so they match without filters.

internal val SubscriptionIsActiveFingerprint = Fingerprint(
    definingClass = "Lcom/pocketprep/android/api/common/Subscription;",
    name = "g",
    returnType = "Z",
    parameters = listOf()
)

// returnType changes per variant: wd9 (itcybersecurity) → cg9 (professional).
// We don't specify returnType here so it matches any SubscriptionPlan enum name.
internal val SubscriptionPlanFingerprint = Fingerprint(
    definingClass = "Lcom/pocketprep/android/api/common/Subscription;",
    name = "a",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf()
)

internal val SubscriptionIsBundleFingerprint = Fingerprint(
    definingClass = "Lcom/pocketprep/android/api/common/Subscription;",
    name = "b",
    returnType = "Z",
    parameters = listOf()
)

internal val SubscriptionSupportsExamFingerprint = Fingerprint(
    definingClass = "Lcom/pocketprep/android/api/common/Subscription;",
    name = "c",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;")
)

internal val SubscriptionMatchesExamFingerprint = Fingerprint(
    definingClass = "Lcom/pocketprep/android/api/common/Subscription;",
    name = "d",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;")
)

internal val SubscriptionActiveForExamFingerprint = Fingerprint(
    definingClass = "Lcom/pocketprep/android/api/common/Subscription;",
    name = "e",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;")
)

internal val SubscriptionTeachForExamFingerprint = Fingerprint(
    definingClass = "Lcom/pocketprep/android/api/common/Subscription;",
    name = "f",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;")
)

// ── Subscription utility statics ───────────────────────────────────────────────
// These moved from ce9 → ig9 between itcybersecurity and professional.
// Fingerprinted by method signature + body shape (isEmpty + iterator calls)
// rather than definingClass so they match regardless of which obfuscated
// class they land in next time.

// hasAnyActiveSubscription(Collection<Subscription>): Z
// Iterates subscriptions, returns true if any is active. Used as the top-level
// gate before checking exam-specific entitlement.
internal val HasAnyActiveSubscriptionFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Ljava/util/Collection;"),
    filters = listOf(
        methodCall(
            definingClass = "Ljava/util/Collection;",
            name = "isEmpty",
            returnType = "Z"
        ),
        methodCall(
            definingClass = "Ljava/util/Iterator;",
            name = "hasNext",
            returnType = "Z"
        )
    )
)

// hasActiveSubscriptionForExam(Collection<Subscription>, CompositeKey): Z
// Checks whether any active subscription covers the specified exam.
internal val HasActiveSubscriptionForExamFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(
        "Ljava/util/Collection;",
        "Lcom/pocketprep/android/api/common/CompositeKey;"
    ),
    filters = listOf(
        methodCall(
            definingClass = "Ljava/util/Collection;",
            name = "isEmpty",
            returnType = "Z"
        ),
        methodCall(
            definingClass = "Ljava/util/Iterator;",
            name = "hasNext",
            returnType = "Z"
        )
    )
)

// ── Exam-level subscription status resolver ────────────────────────────────────
// kg9.l0(ExamMetadata, List<Subscription>): q77
// The canonical question-pool selector: returns NO_PREMIUM (q77.z) when the
// subscription list is empty, causing l03.m0() to serve only the free 80-question
// pool instead of ExamQuestions.b (full bank). Returning q77.B always fixes this.
// Stable: class kg9 and method l0 have not changed across tested variants.
internal val ExamSubscriptionStatusFingerprint = Fingerprint(
    definingClass = "Lkg9;",
    name = "l0",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Lq77;",
    parameters = listOf(
        "Lcom/pocketprep/android/api/common/ExamMetadata;",
        "Ljava/util/List;"
    ),
    filters = listOf(
        fieldAccess(definingClass = "Lq77;", name = "z", type = "Lq77;"),
        fieldAccess(definingClass = "Lq77;", name = "B", type = "Lq77;")
    )
)
