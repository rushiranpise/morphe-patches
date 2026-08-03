package app.template.patches.pocketprep

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// ── Subscription instance methods ─────────────────────────────────────────────
// All live on the stable, unobfuscated class com.pocketprep.android.api.common.Subscription.
// The single-letter method names (a–g) are consistent across all app variants and
// have not changed since the class was introduced. Fingerprinted by definingClass+name
// so they resolve instantly without scanning the whole DEX.
//
// Verified in 3.28.1 (versionCode 429 / com.pocketprep.android.professional):
//   g()Z              → isActive:       expiresAt.z.isAfter(LocalDateTime.now())
//   a()Luf9;          → getPlan:        days>360→YEARLY(B), days>87→QUARTERLY(A), else→MONTHLY(z)
//   b()Z              → isBundle:       bundlePointer != null && bundlePointer.B == "k9T5L3kC0U"
//   c(String)Z        → supportsExam:   matchesExam(d) && source==SUPPORT
//   d(String)Z        → matchesExam:    examGuid.equalsIgnoreCase(examId)
//   e(String)Z        → activeForExam:  matchesExam && !supportsExam && !teachForExam
//   f(String)Z        → teachForExam:   matchesExam && source==TEACH

internal val SubscriptionIsActiveFingerprint = Fingerprint(
    definingClass = "Lcom/pocketprep/android/api/common/Subscription;",
    name = "g",
    returnType = "Z",
    parameters = listOf()
)

// returnType is the SubscriptionPlan enum — name changes per variant (wd9→cg9→uf9…).
// We omit returnType so this matches any variant's enum class name. The patch reads
// the actual return type at execute time and injects the YEARLY field dynamically.
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
// These static helpers live in a utility class whose obfuscated name changes each
// release (ce9 → ig9 → ag9 in 3.28.1). Fingerprinted purely by access flags,
// return type, parameter types, and body shape (isEmpty → iterator) so they match
// regardless of which obfuscated class they land in next.
//
// 3.28.1: both live in ag9 as methods e(Collection)Z and f(Collection,CompositeKey)Z.

// hasAnyActiveSubscription(Collection<Subscription>): Z
// Returns true if at least one subscription in the collection is active.
// Used as the top-level gate before any exam-specific entitlement check.
internal val HasAnyActiveSubscriptionFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Ljava/util/Collection;"),
    filters = listOf(
        methodCall(definingClass = "Ljava/util/Collection;", name = "isEmpty", returnType = "Z"),
        methodCall(definingClass = "Ljava/util/Iterator;", name = "hasNext", returnType = "Z")
    )
)

// hasActiveSubscriptionForExam(Collection<Subscription>, CompositeKey): Z
// Returns true if any active subscription covers the given exam.
internal val HasActiveSubscriptionForExamFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(
        "Ljava/util/Collection;",
        "Lcom/pocketprep/android/api/common/CompositeKey;"
    ),
    filters = listOf(
        methodCall(definingClass = "Ljava/util/Collection;", name = "isEmpty", returnType = "Z"),
        methodCall(definingClass = "Ljava/util/Iterator;", name = "hasNext", returnType = "Z")
    )
)

// ── Exam-level subscription status resolver ────────────────────────────────────
// This method maps (ExamMetadata, List<Subscription>) → SubscriptionStatusEnum and
// is the single gate that decides whether the full question bank is served.
//
// Version history (obfuscated class and method name change every release):
//   ≤3.27.x : kg9.l0(ExamMetadata, List) → q77   (q77.z=NO_PREMIUM, q77.B=PREMIUM_FROM_CURRENT_BUNDLE)
//   3.28.1  : cg9.I(ExamMetadata, List)  → e87   (e87.z=NO_PREMIUM, e87.B=PREMIUM_FROM_CURRENT_BUNDLE)
//
// We fingerprint by the two stable enum field accesses inside the method body
// (NO_PREMIUM / PREMIUM_FROM_CURRENT_BUNDLE), which always appear in this order,
// so the fingerprint survives the class/method rename on next update.
// The return type e87 is specified explicitly because it is also obfuscated and
// could collide with other (ExamMetadata, List) methods in future versions.
// Update `returnType` here if the status enum is renamed again.
//
// Patch: sget-object v0, Le87;->B:Le87; / return-object v0
// always returns PREMIUM_FROM_CURRENT_BUNDLE, making the question-pool selector
// serve the full bank (ExamQuestions.b) instead of the free subset (ExamQuestions.c).
internal val ExamSubscriptionStatusFingerprint = Fingerprint(
    returnType = "Le87;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(
        "Lcom/pocketprep/android/api/common/ExamMetadata;",
        "Ljava/util/List;"
    ),
    filters = listOf(
        fieldAccess(definingClass = "Le87;", name = "z", type = "Le87;"),
        fieldAccess(definingClass = "Le87;", name = "B", type = "Le87;")
    )
)
