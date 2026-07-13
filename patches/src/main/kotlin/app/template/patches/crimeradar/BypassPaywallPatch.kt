package app.template.patches.crimeradar

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CRIMERADAR_COMPATIBILITY

// Paywall gate methods confirmed from smali analysis:
//
//  RadarMapSubscriptionGateway (smali_classes8):
//    • isActiveNow()Z             — primary runtime gate for premium map UI
//    • isFeatureEnabled()Z        — TweakConfig feature-flag gate
//    • wasEverPremium()Z          — upsell suppression gate
//    • hasPaidEver()Z             — delegates to shouldSuppressPremiumPromotions();
//                                    queried independently by MapFollowedLocationActivity
//                                    and the Me tab to control upsell nag visibility.
//    • isPremiumEntryDismissed()Z — reads a per-session boolean that resets every
//                                    launch; premium entry cards re-appear each session
//                                    without this patch.
//    • autoRenew()Z              — controls upsell/renewal-reminder banner in
//                                    MapFollowedLocationActivity.refreshAdapter().
//                                    When isActiveNow=true but autoRenew=false,
//                                    refreshAdapter resolves guideId to
//                                    "manage_location_portal" and shows a banner.
//                                    Patching to true makes the guide branch resolve
//                                    to "manage_premium_renew", which is deduplicated
//                                    against lastShownGuide and never displayed.
//
//  Note: freeLimit() and premiumLimit() are NOT patched here — they are already
//  handled by RemoveLimitsPatch (forced to Integer.MAX_VALUE, which is a superset
//  of any fix needed for banner suppression).
//
//  SubscriptionAccountHelper (smali_classes8):
//    • shouldSuppressPremiumPromotions()Z — controls premium entry card
//
//  GlobalLocationRepository.updateSavedListFull(GLocationList) (smali_classes7):
//    Receives a GLocationList from the server which contains lockedSaveIds — a Set<String>
//    of location IDs the server marks as paywall-locked. This set is posted to the
//    lockedSaveIds LiveData and consumed by MapFollowedLocationActivity.refreshAdapter(),
//    which passes it to MapFollowedLocationAdapter.updateList3() where it marks rows as locked.
//    Even with isActiveNow=true and limit=MAX_VALUE, a non-empty lockedSaveIds causes
//    followed locations to render as paywalled rows.
//    Fix: replace the entire method body, hardcoding limit=MAX_VALUE and
//    lockedSaveIds=emptySet() so the LiveData streams are never poisoned by
//    server-returned paywall values.
//
//    Original smali body:
//      const-string v0, "result"
//      invoke-static {p1, v0}, kotlin/jvm/internal/m;->h(Object;String;)V    # null check
//      sget-object v0, GlobalLocationRepository;->savedLimit:S
//      invoke-virtual {p1}, GLocationList;->getLimit()I                       # <- replace
//      move-result v1
//      invoke-static {v1}, Integer;->valueOf(I)Integer
//      move-result-object v1
//      invoke-virtual {v0, v1}, LiveData;->l(Object)V                         # postValue(limit)
//      sget-object v0, GlobalLocationRepository;->lockedSaveIds:S
//      invoke-virtual {p1}, GLocationList;->getLockedSaveIds()Set              # <- replace
//      move-result-object v1
//      invoke-virtual {v0, v1}, LiveData;->l(Object)V                         # postValue(ids)
//      invoke-virtual {p1}, GLocationList;->getList()ArrayList
//      move-result-object p1
//      invoke-virtual {p0, p1}, GlobalLocationRepository;->updateSavedList(ArrayList)V
//      return-void
//
//  GlobalLocationRepository.onAccountChanged() (smali_classes7):
//    Called on every account sign-in/switch and on app start. It resets savedLimit
//    LiveData to the hardcoded value 1, overriding whatever updateSavedListFull wrote.
//    THIS IS THE ROOT CAUSE of the "limit 1 survives patch" regression — onAccountChanged()
//    fires after the initial data load and stomps the corrected limit back to 1.
//
//    Original smali body:
//      sget-object v0, GlobalLocationRepository;->savedList:S
//      const/4 v1, 0x0
//      invoke-virtual {v0, v1}, LiveData;->l(Object)V                         # postValue(null)
//      sget-object v0, GlobalLocationRepository;->savedLimit:S
//      const/4 v1, 0x1                                                         # <- THE BUG
//      invoke-static {v1}, Integer;->valueOf(I)Integer
//      move-result-object v1
//      invoke-virtual {v0, v1}, LiveData;->l(Object)V                         # postValue(1)
//      sget-object v0, GlobalLocationRepository;->lockedSaveIds:S
//      sget-object v1, Ae/E;->b:Ae/E                                          # empty set
//      invoke-virtual {v0, v1}, LiveData;->l(Object)V                         # postValue(empty)
//      const-string v0, "mapSaveLocationList"
//      invoke-static {v0}, Dd/z;->i(String)V
//      return-void
//
//    Patched body: identical except `const/4 v1, 0x1` is replaced with
//      `const v1, 0x7fffffff` so savedLimit resets to MAX_VALUE, not 1.

private const val GATEWAY_CLASS =
    "Lcom/particlemedia/feature/subscription/RadarMapSubscriptionGateway;"

private const val ACCOUNT_HELPER_CLASS =
    "Lcom/particlemedia/feature/subscription/SubscriptionAccountHelper;"

private const val GLOBAL_LOCATION_REPO_CLASS =
    "Lcom/particlemedia/feature/map/GlobalLocationRepository;"

private const val SAVE_LOCATION_WORKER_CLASS =
    "Lcom/particlemedia/feature/map/SafetyMapViewModel\$saveLocation\$1;"

object IsActiveNowFingerprint : Fingerprint(
    definingClass = GATEWAY_CLASS,
    name = "isActiveNow"
)

object IsFeatureEnabledFingerprint : Fingerprint(
    definingClass = GATEWAY_CLASS,
    name = "isFeatureEnabled"
)

object WasEverPremiumFingerprint : Fingerprint(
    definingClass = GATEWAY_CLASS,
    name = "wasEverPremium"
)

object HasPaidEverFingerprint : Fingerprint(
    definingClass = GATEWAY_CLASS,
    name = "hasPaidEver"
)

object IsPremiumEntryDismissedFingerprint : Fingerprint(
    definingClass = GATEWAY_CLASS,
    name = "isPremiumEntryDismissed"
)

object AutoRenewFingerprint : Fingerprint(
    definingClass = GATEWAY_CLASS,
    name = "autoRenew"
)

object ShouldSuppressPremiumPromotionsFingerprint : Fingerprint(
    definingClass = ACCOUNT_HELPER_CLASS,
    name = "shouldSuppressPremiumPromotions"
)

// GlobalLocationRepository.updateSavedListFull — smali_classes7
// Full-method rewrite: hardcode limit=MAX_VALUE and lockedSaveIds=emptySet() so
// the LiveData streams are never poisoned by server-returned paywall values.
object UpdateSavedListFullFingerprint : Fingerprint(
    definingClass = GLOBAL_LOCATION_REPO_CLASS,
    name = "updateSavedListFull"
)

// GlobalLocationRepository.onAccountChanged — smali_classes7
// Full-method rewrite: replace the hardcoded `const/4 v1, 0x1` with MAX_VALUE
// so savedLimit is not reset to 1 on every account change / app start.
object OnAccountChangedFingerprint : Fingerprint(
    definingClass = GLOBAL_LOCATION_REPO_CLASS,
    name = "onAccountChanged"
)

// SafetyMapViewModel$saveLocation$1.invokeSuspend — smali_classes7
// Intercept the API save call and mock successful SaveResp local insertion.
object SaveLocationWorkerFingerprint : Fingerprint(
    definingClass = SAVE_LOCATION_WORKER_CLASS,
    name = "invokeSuspend"
)

@Suppress("unused")
val bypassPaywallPatch = bytecodePatch(
    name = "Bypass Subscription Paywall",
    description = "Bypasses the subscription paywall in-app."
) {
    compatibleWith(CRIMERADAR_COMPATIBILITY)

    execute {
        val trueInstructions = """
            const/4 v0, 0x1
            return v0
        """.trimIndent()

        val falseInstructions = """
            const/4 v0, 0x0
            return v0
        """.trimIndent()

        // Patch boolean gate methods in RadarMapSubscriptionGateway to return true
        for (fingerprint in listOf(
            IsActiveNowFingerprint,
            IsFeatureEnabledFingerprint,
            WasEverPremiumFingerprint,
            HasPaidEverFingerprint,
            IsPremiumEntryDismissedFingerprint
        )) {
            val method = fingerprint.match(classDefBy(GATEWAY_CLASS)).method
            method.removeInstructions(0, method.instructions.count())
            method.addInstructions(0, trueInstructions)
        }

        // Patch autoRenew in RadarMapSubscriptionGateway to return false
        val autoRenewMethod = AutoRenewFingerprint
            .match(classDefBy(GATEWAY_CLASS))
            .method
        autoRenewMethod.removeInstructions(0, autoRenewMethod.instructions.count())
        autoRenewMethod.addInstructions(0, falseInstructions)

        // Patch shouldSuppressPremiumPromotions in SubscriptionAccountHelper
        val helperMethod = ShouldSuppressPremiumPromotionsFingerprint
            .match(classDefBy(ACCOUNT_HELPER_CLASS))
            .method
        helperMethod.removeInstructions(0, helperMethod.instructions.count())
        helperMethod.addInstructions(0, trueInstructions)

        // Patch GlobalLocationRepository.updateSavedListFull:
        // Full-method rewrite — hardcode limit=MAX_VALUE and lockedSaveIds=emptySet()
        // and only update from server list if local list is null or empty to prevent stomping.
        val updateMethod = UpdateSavedListFullFingerprint
            .match(classDefBy(GLOBAL_LOCATION_REPO_CLASS))
            .method
        updateMethod.removeInstructions(0, updateMethod.instructions.count())
        updateMethod.addInstructions(
            0,
            """
            const-string v0, "result"
            invoke-static {p1, v0}, Lkotlin/jvm/internal/n;->h(Ljava/lang/Object;Ljava/lang/String;)V
            sget-object v0, Lcom/particlemedia/feature/map/GlobalLocationRepository;->savedLimit:Landroidx/lifecycle/T;
            const v1, 0x7fffffff
            invoke-static {v1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
            move-result-object v1
            invoke-virtual {v0, v1}, Landroidx/lifecycle/T;->l(Ljava/lang/Object;)V
            sget-object v0, Lcom/particlemedia/feature/map/GlobalLocationRepository;->lockedSaveIds:Landroidx/lifecycle/T;
            invoke-static {}, Ljava/util/Collections;->emptySet()Ljava/util/Set;
            move-result-object v1
            invoke-virtual {v0, v1}, Landroidx/lifecycle/T;->l(Ljava/lang/Object;)V
            sget-object v0, Lcom/particlemedia/feature/map/GlobalLocationRepository;->savedList:Landroidx/lifecycle/T;
            invoke-virtual {v0}, Landroidx/lifecycle/N;->d()Ljava/lang/Object;
            move-result-object v0
            check-cast v0, Ljava/util/ArrayList;
            if-eqz v0, :cond_update_server
            invoke-virtual {v0}, Ljava/util/ArrayList;->isEmpty()Z
            move-result v0
            if-eqz v0, :cond_skip_server
            :cond_update_server
            invoke-virtual {p1}, Lcom/particlemedia/feature/map/data/GLocationList;->getList()Ljava/util/ArrayList;
            move-result-object p1
            invoke-virtual {p0, p1}, Lcom/particlemedia/feature/map/GlobalLocationRepository;->updateSavedList(Ljava/util/ArrayList;)V
            :cond_skip_server
            return-void
            """.trimIndent()
        )

        // Patch GlobalLocationRepository.onAccountChanged:
        // Full-method rewrite — replace `const/4 v1, 0x1` with MAX_VALUE so the
        // savedLimit LiveData is not reset to 1 on every account change / app start.
        // lockedSaveIds reset to emptySet (Ae/E.b) is preserved — it is already correct.
        val accountChangedMethod = OnAccountChangedFingerprint
            .match(classDefBy(GLOBAL_LOCATION_REPO_CLASS))
            .method
        accountChangedMethod.removeInstructions(0, accountChangedMethod.instructions.count())
        accountChangedMethod.addInstructions(
            0,
            """
            sget-object v0, Lcom/particlemedia/feature/map/GlobalLocationRepository;->savedList:Landroidx/lifecycle/T;
            const/4 v1, 0x0
            invoke-virtual {v0, v1}, Landroidx/lifecycle/T;->l(Ljava/lang/Object;)V
            sget-object v0, Lcom/particlemedia/feature/map/GlobalLocationRepository;->savedLimit:Landroidx/lifecycle/T;
            const v1, 0x7fffffff
            invoke-static {v1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
            move-result-object v1
            invoke-virtual {v0, v1}, Landroidx/lifecycle/T;->l(Ljava/lang/Object;)V
            sget-object v0, Lcom/particlemedia/feature/map/GlobalLocationRepository;->lockedSaveIds:Landroidx/lifecycle/T;
            sget-object v1, LPf/E;->b:LPf/E;
            invoke-virtual {v0, v1}, Landroidx/lifecycle/T;->l(Ljava/lang/Object;)V
            const-string v0, "mapSaveLocationList"
            invoke-static {v0}, Lje/w;->i(Ljava/lang/String;)V
            return-void
            """.trimIndent()
        )

        // Patch SafetyMapViewModel${'$'}saveLocation${'$'}1.invokeSuspend
        // Full-method rewrite — mock network save API response and add location locally.
        val saveLocationMethod = SaveLocationWorkerFingerprint
            .match(classDefBy(SAVE_LOCATION_WORKER_CLASS))
            .method
        saveLocationMethod.removeInstructions(0, saveLocationMethod.instructions.count())
        saveLocationMethod.addInstructions(
            0,
            """
            iget-object v0, p0, Lcom/particlemedia/feature/map/SafetyMapViewModel${'$'}saveLocation${'$'}1;->${'$'}gLocation:Ljc/a;
            invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
            move-result-wide v1
            invoke-static {v1, v2}, Ljava/lang/Long;->toString(J)Ljava/lang/String;
            move-result-object v1
            invoke-virtual {v0, v1}, Ljc/a;->C(Ljava/lang/String;)V
            invoke-virtual {v0}, Ljc/a;->o()Ljava/lang/String;
            move-result-object v1
            if-nez v1, :cond_zip
            const-string v1, "00000"
            :cond_zip
            invoke-virtual {v0, v1}, Ljc/a;->F(Ljava/lang/String;)V
            new-instance p1, Ljava/util/ArrayList;
            sget-object v0, Lcom/particlemedia/feature/map/GlobalLocationRepository;->INSTANCE:Lcom/particlemedia/feature/map/GlobalLocationRepository;
            invoke-virtual {v0}, Lcom/particlemedia/feature/map/GlobalLocationRepository;->getSavedList()Landroidx/lifecycle/T;
            move-result-object v1
            invoke-virtual {v1}, Landroidx/lifecycle/N;->d()Ljava/lang/Object;
            move-result-object v1
            check-cast v1, Ljava/util/ArrayList;
            if-eqz v1, :cond_empty
            goto :goto_init
            :cond_empty
            new-instance v1, Ljava/util/ArrayList;
            invoke-direct {v1}, Ljava/util/ArrayList;-><init>()V
            :goto_init
            invoke-direct {p1, v1}, Ljava/util/ArrayList;-><init>(Ljava/util/Collection;)V
            iget-object v1, p0, Lcom/particlemedia/feature/map/SafetyMapViewModel${'$'}saveLocation${'$'}1;->${'$'}gLocation:Ljc/a;
            invoke-virtual {p1, v1}, Ljava/util/ArrayList;->remove(Ljava/lang/Object;)Z
            iget-object v1, p0, Lcom/particlemedia/feature/map/SafetyMapViewModel${'$'}saveLocation${'$'}1;->${'$'}gLocation:Ljc/a;
            const/4 v2, 0x1
            invoke-virtual {v1, v2}, Ljc/a;->D(Z)V
            iget-object v1, p0, Lcom/particlemedia/feature/map/SafetyMapViewModel${'$'}saveLocation${'$'}1;->${'$'}gLocation:Ljc/a;
            const/4 v2, 0x0
            invoke-virtual {p1, v2, v1}, Ljava/util/ArrayList;->add(ILjava/lang/Object;)V
            sget-object v0, Lcom/particlemedia/feature/map/GlobalLocationRepository;->INSTANCE:Lcom/particlemedia/feature/map/GlobalLocationRepository;
            invoke-virtual {v0, p1}, Lcom/particlemedia/feature/map/GlobalLocationRepository;->updateSavedList(Ljava/util/ArrayList;)V
            iget-object p1, p0, Lcom/particlemedia/feature/map/SafetyMapViewModel${'$'}saveLocation${'$'}1;->${'$'}handleExceptionUnit:Lag/l;
            if-eqz p1, :cond_callback
            const/4 v0, 0x0
            invoke-interface {p1, v0}, Lag/l;->invoke(Ljava/lang/Object;)Ljava/lang/Object;
            :cond_callback
            sget-object p1, LOf/G;->a:LOf/G;
            return-object p1
            """.trimIndent()
        )
    }
}
