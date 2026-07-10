package app.template.patches.pocketcasts

import app.morphe.patcher.Fingerprint

internal val MembershipStatusFingerprint = Fingerprint(
    definingClass = "Lau/com/shiftyjelly/pocketcasts/models/type/Membership;",
    name = "a",
    returnType = "Ld5e;",
    parameters = emptyList(),
)

internal val MembershipHasFeatureFingerprint = Fingerprint(
    definingClass = "Lau/com/shiftyjelly/pocketcasts/models/type/Membership;",
    name = "b",
    returnType = "Z",
    parameters = listOf("Lze7;"),
)

internal val SubscriptionLifetimeFingerprint = Fingerprint(
    definingClass = "Lau/com/shiftyjelly/pocketcasts/models/type/Subscription;",
    name = "a",
    returnType = "Z",
    parameters = emptyList(),
)

internal val SubscriptionStatusMapperFingerprint = Fingerprint(
    definingClass = "Lby5;",
    name = "M",
    returnType = "Lau/com/shiftyjelly/pocketcasts/models/type/Membership;",
    parameters = listOf("Lau/com/shiftyjelly/pocketcasts/servers/sync/SubscriptionStatusResponse;"),
)
