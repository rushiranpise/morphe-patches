package app.template.patches.carousell

import app.morphe.patcher.Fingerprint

// ContentRequestOptions.getExcludePromotedListing() — forces promoted listings excluded from search
internal object ExcludePromotedListingFingerprint : Fingerprint(
    definingClass = "Lcom/thecarousell/core/entity/fieldset/ContentRequestOptions;",
    name = "getExcludePromotedListing",
    returnType = "Z",
    parameters = emptyList()
)

// ContentRequestOptions.getExcludePromotedProfile() — forces promoted profiles excluded from search
internal object ExcludePromotedProfileFingerprint : Fingerprint(
    definingClass = "Lcom/thecarousell/core/entity/fieldset/ContentRequestOptions;",
    name = "getExcludePromotedProfile",
    returnType = "Z",
    parameters = emptyList()
)

// AdLoadManagerImpl.g() — interstitial ad loader; return Observable.empty()
internal object AdLoadManagerLoadInterstitialFingerprint : Fingerprint(
    definingClass = "Lij1/h;",
    name = "g",
    returnType = "Lio/reactivex/r;",
    parameters = listOf(
        "Lcom/thecarousell/data/external_ads/model/AdLoadConfigNew;",
        "Ljava/lang/Class;",
        "Lcom/thecarousell/data/external_ads/model/AdEventTrackingData;",
        "Landroid/app/Activity;"
    )
)

// AdLoadManagerImpl.d() — banner/native ad loader; return Observable.empty()
internal object AdLoadManagerLoadBannerFingerprint : Fingerprint(
    definingClass = "Lij1/h;",
    name = "d",
    returnType = "Lio/reactivex/r;",
    parameters = listOf(
        "Lcom/thecarousell/data/external_ads/model/AdLoadConfigNew;",
        "Ljava/lang/Class;",
        "Lcom/thecarousell/data/external_ads/model/AdEventTrackingData;"
    )
)

// GetAdResponse.getPromotedListingCards() — returns inline promoted listing cards; return empty list
internal object GetAdResponsePromotedCardsFingerprint : Fingerprint(
    definingClass = "Lcom/thecarousell/core/entity/ads/GetAdResponse;",
    name = "getPromotedListingCards",
    returnType = "Ljava/util/List;",
    parameters = emptyList()
)
