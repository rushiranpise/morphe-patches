package app.template.patches.calm.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CALM_COMPATIBILITY

@Suppress("unused")
val calmPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Calm Premium: removes all subscription gates, " +
        "upsell banners, video session locks, and content locks.",
    default = true,
) {
    compatibleWith(CALM_COMPATIBILITY)

    execute {
        val returnTrue  = "const/4 v0, 0x1\nreturn v0"
        val returnFalse = "const/4 v0, 0x0\nreturn v0"

        // Layer 1: Subscription.getValid() → true
        SubscriptionGetValidFingerprint.method.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, returnTrue)
        }

        // Layer 2: SubscriptionRefreshResponse.getValid() → true
        SubscriptionRefreshResponseGetValidFingerprint.method.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, returnTrue)
        }

        // Layer 3: UserRepository.isSubscribed() → true
        UserRepositoryIsSubscribedFingerprint.method.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, returnTrue)
        }

        // Layer 4: UserRepository.getHasValidSubscription() → true
        UserRepositoryGetHasValidSubscriptionFingerprint.method.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, returnTrue)
        }

        // Layer 5: isGuideInFreeAccessLimit(Guide) → false
        IsGuideInFreeAccessLimitFingerprint.method.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, returnFalse)
        }

        // Layer 6: isFreeAccessSessionLocked(String,Z) → Single.just(false)
        IsFreeAccessSessionLockedFingerprint.method.apply {
            removeInstructions(0, instructions.count())
            addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    invoke-static {v0}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                    move-result-object v0
                    invoke-static {v0}, Lio/reactivex/Single;->just(Ljava/lang/Object;)Lio/reactivex/Single;
                    move-result-object v0
                    return-object v0
                """.trimIndent(),
            )
        }

        // Layer 7: PackViewHolderFactory.getViewHolder() — skip BannerPromo for subscribers
        //
        // Inject at the index of new-instance BannerPromoViewHolder (instructionMatches[0]).
        // When isSubscribed=true: create BlankCellViewHolder and return-object early.
        // When isSubscribed=false: fall through to original BannerPromoViewHolder creation.
        //
        // Register layout at :pswitch_e2 injection point (.registers 15 → v0..v8 locals):
        //   p0 = PackViewHolderFactory (this) — intact
        //   p1 = LayoutInflater — intact (needed for inflate)
        //   p2 = "inflate(...)" string (overwritten from original DisplayStyle param — safe to clobber)
        //   p3 = DisplayStyle ordinal result (consumed by packed-switch — safe to clobber)
        //   v0..v5 = used by earlier branches sharing the register frame
        //   v6, v7 = scratch (unused at this point in :pswitch_e2)
        //
        // Register assignments:
        //   v6  = isSubscribed() bool, then reused as new BlankCellViewHolder instance
        //   p3  = PackCell$DisplayStyle.Blank (sget-object — clobbers consumed param)
        //   v7  = PackCellBlankBinding (inflate result)
        //   p2  = packCellDependenciesBundle (iget-object — clobbers consumed inflate string)
        //   invoke-direct {v6, p3, v7, p2}  ← 4 distinct registers, no aliasing
        //
        // BlankCellViewHolder renders an empty 0-height row — safe for RecyclerView.
        // Cannot return null: RecyclerView.onCreateViewHolder would NPE immediately.
        PackViewHolderFactoryGetViewHolderFingerprint.apply {
            val insertIdx = instructionMatches[0].index
            method.addInstructionsWithLabels(
                insertIdx,
                """
                    iget-object v6, p0, Lcom/calm/android/packs/PackViewHolderFactory;->userRepository:Lcom/calm/android/core/data/repositories/UserRepository;
                    invoke-virtual {v6}, Lcom/calm/android/core/data/repositories/UserRepository;->isSubscribed()Z
                    move-result v6
                    if-eqz v6, :not_subscribed
                    new-instance v6, Lcom/calm/android/packs/viewholders/BlankCellViewHolder;
                    sget-object p3, Lcom/calm/android/data/packs/PackCell${'$'}DisplayStyle;->Blank:Lcom/calm/android/data/packs/PackCell${'$'}DisplayStyle;
                    invoke-static {p1}, Lcom/calm/android/packs/databinding/PackCellBlankBinding;->inflate(Landroid/view/LayoutInflater;)Lcom/calm/android/packs/databinding/PackCellBlankBinding;
                    move-result-object v7
                    iget-object p2, p0, Lcom/calm/android/packs/PackViewHolderFactory;->packCellDependenciesBundle:Lcom/calm/android/data/packs/PackCellDependenciesBundle;
                    invoke-direct {v6, p3, v7, p2}, Lcom/calm/android/packs/viewholders/BlankCellViewHolder;-><init>(Lcom/calm/android/data/packs/PackCell${'$'}DisplayStyle;Lcom/calm/android/packs/databinding/PackCellBlankBinding;Lcom/calm/android/data/packs/PackCellDependenciesBundle;)V
                    check-cast v6, Lcom/calm/android/packs/utils/PackCellViewHolder;
                    return-object v6
                    :not_subscribed
                    nop
                """.trimIndent(),
            )
        }

        // Layer 8: PackItem.isUnlocked() → true
        PackItemIsUnlockedFingerprint.method.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, returnTrue)
        }

        // Layer 9: ActionData.isLocked() → false
        ActionDataIsLockedFingerprint.method.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, returnFalse)
        }
    }
}
