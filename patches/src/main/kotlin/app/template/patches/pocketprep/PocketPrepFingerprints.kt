package app.template.patches.pocketprep

import app.morphe.patcher.Fingerprint

internal val SubscriptionIsActiveFingerprint = Fingerprint(
    definingClass = "Lcom/pocketprep/android/api/common/Subscription;",
    name = "g",
    returnType = "Z",
    parameters = emptyList()
)

internal val SubscriptionPlanFingerprint = Fingerprint(
    definingClass = "Lcom/pocketprep/android/api/common/Subscription;",
    name = "a",
    returnType = "Lwd9;",
    parameters = emptyList()
)

internal val SubscriptionIsBundleFingerprint = Fingerprint(
    definingClass = "Lcom/pocketprep/android/api/common/Subscription;",
    name = "b",
    returnType = "Z",
    parameters = emptyList()
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

internal val HasAnyActiveSubscriptionFingerprint = Fingerprint(
    definingClass = "Lce9;",
    name = "e",
    returnType = "Z",
    parameters = listOf("Ljava/util/Collection;")
)

internal val HasActiveSubscriptionForExamFingerprint = Fingerprint(
    definingClass = "Lce9;",
    name = "f",
    returnType = "Z",
    parameters = listOf(
        "Ljava/util/Collection;",
        "Lcom/pocketprep/android/api/common/CompositeKey;"
    )
)

internal val ChoosePlanActionFingerprint = Fingerprint(
    definingClass = "Lw21;",
    name = "a",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;")
)

internal val ConfigureSubjectStateConstructorFingerprint = Fingerprint(
    definingClass = "Ln09;",
    name = "<init>",
    returnType = "V",
    parameters = listOf(
        "Lbma;",
        "Lcom/pocketprep/android/api/common/ExamMetadata;",
        "Z",
        "I"
    )
)

internal val ConfigureQuizStateConstructorFingerprint = Fingerprint(
    definingClass = "Lrg1;",
    name = "<init>",
    returnType = "V",
    parameters = listOf(
        "Ljava/util/List;",
        "Lri7;",
        "Ljava/util/List;",
        "Lgp8;",
        "Lqc9;",
        "Z",
        "Ljava/util/Set;",
        "Z",
        "Z",
        "Z",
        "Z",
        "I",
        "Z"
    )
)

internal val FreeQuestionCounterConstructorFingerprint = Fingerprint(
    definingClass = "Lm09;",
    name = "<init>",
    returnType = "V",
    parameters = listOf("I", "I", "Z")
)

internal val PremiumAccessEventConstructorFingerprints = listOf(
    "Lak7;",
    "Lc85;",
    "Lhg1;",
    "Lil8;",
    "Lr39;",
    "Lsa9;",
    "Lwl9;",
    "Lyv2;",
    "Lzv2;"
).map { definingClass ->
    Fingerprint(
        definingClass = definingClass,
        name = "<init>",
        returnType = "V",
        parameters = listOf("Z")
    )
}
