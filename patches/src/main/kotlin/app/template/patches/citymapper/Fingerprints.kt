package app.template.patches.citymapper

import app.morphe.patcher.Fingerprint

val CitymapperApplicationOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/citymapper/app/ActualCitymapperApplication;",
    name = "onCreate",
    returnType = "V",
    parameters = emptyList(),
)

val ClubFeaturesFingerprint = Fingerprint(
    definingClass = "Lof7;",
    name = "b",
    returnType = "Ljava/util/List;",
    parameters = listOf("Ljava/util/List;"),
    strings = listOf("default"),
)

val SettingsStateMapperFingerprint = Fingerprint(
    definingClass = "Lf7;",
    name = "invoke",
    returnType = "Ljava/lang/Object;",
    parameters = listOf(
        "Ljava/lang/Object;",
        "Ljava/lang/Object;",
    ),
)

val CurrentClubSubscriptionFingerprint = Fingerprint(
    definingClass = "Lkbk;",
    name = "b",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Lx74;"),
)

val FeatureFlagIsEnabledFingerprint = Fingerprint(
    definingClass = "Lst3;",
    name = "isEnabled",
    returnType = "Z",
    parameters = emptyList(),
)

val SubscriptionsActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/citymapper/app/subscription/impl/SubscriptionsActivity;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

val SubscriptionSignupPosterFingerprint = Fingerprint(
    definingClass = "Lcom/citymapper/app/subscription/impl/signup/SubscriptionSignupPosterFragment;",
    name = "b0",
    returnType = "V",
    parameters = listOf("Lsom;", "Landroid/os/Bundle;"),
)

