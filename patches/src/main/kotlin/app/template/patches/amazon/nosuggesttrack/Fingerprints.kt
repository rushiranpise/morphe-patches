package app.template.patches.amazon.nosuggesttrack

import app.morphe.patcher.Fingerprint

internal val SearchSuggestionsSetEventFingerprint = Fingerprint(
    definingClass = "Lcom/amazon/search/resources/dependency/client/suggestions/SearchSuggestionsV2Request\$Builder;",
    name = "setEvent",
    returnType = "Lcom/amazon/search/resources/dependency/client/suggestions/SearchSuggestionsV2Request\$Builder;",
    parameters = listOf("Lcom/amazon/search/resources/dependency/client/suggestions/Event;"),
)
