package app.template.patches.tomtomgo

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import app.template.patches.shared.Constants.TOMTOMGO_COMPATIBILITY

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks TomTom GO premium features for the selected vehicle type.",
) {
    compatibleWith(TOMTOMGO_COMPATIBILITY)

    val vehicleType by stringOption(
        key = "vehicleType",
        default = "car",
        values = mapOf(
            "Car" to "car",
            "Truck" to "truck",
        ),
        title = "Vehicle type",
        description = "Choose which premium path to unlock.",
    )

    execute {
        val unlockTruck = vehicleType == "truck"
        val primaryType = if (unlockTruck) "c" else "a"
        val fallbackType = if (unlockTruck) "a" else "c"

        CurrentSubscriptionFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, """
                iget-object v0, p0, Le9/o2;->G1:LX9/p;
                sget-object v1, Ltb/a${'$'}b;->$primaryType:Ltb/a${'$'}b;
                invoke-virtual {v0, v1}, LX9/p;->a(Ltb/a${'$'}b;)Ljava/util/ArrayList;
                move-result-object v0
                invoke-virtual {v0}, Ljava/util/ArrayList;->isEmpty()Z
                move-result v1
                if-eqz v1, :cond_found
                iget-object v0, p0, Le9/o2;->G1:LX9/p;
                sget-object v1, Ltb/a${'$'}b;->b:Ltb/a${'$'}b;
                invoke-virtual {v0, v1}, LX9/p;->a(Ltb/a${'$'}b;)Ljava/util/ArrayList;
                move-result-object v0
                invoke-virtual {v0}, Ljava/util/ArrayList;->isEmpty()Z
                move-result v1
                if-eqz v1, :cond_found
                iget-object v0, p0, Le9/o2;->G1:LX9/p;
                sget-object v1, Ltb/a${'$'}b;->$fallbackType:Ltb/a${'$'}b;
                invoke-virtual {v0, v1}, LX9/p;->a(Ltb/a${'$'}b;)Ljava/util/ArrayList;
                move-result-object v0
                invoke-virtual {v0}, Ljava/util/ArrayList;->isEmpty()Z
                move-result v1
                if-eqz v1, :cond_found
                const/4 v0, 0x0
                return-object v0
                :cond_found
                const/4 v1, 0x0
                invoke-virtual {v0, v1}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;
                move-result-object v0
                check-cast v0, Ltb/a;
                return-object v0
            """)
        }

        // Car: combiner always true
        HasActiveSubscriptionsCombinerFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """)
        }

        // Car: mapper always true
        HasActiveSubscriptionsMapperFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """)
        }

        BillingPurchaseStarterFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                invoke-static {v0}, LCj/u;->g(Ljava/lang/Object;)LRj/m;
                move-result-object v0
                return-object v0
            """)
        }

        if (!unlockTruck) {
            SubscriptionTypeCarFingerprint.method.apply {
                removeInstructions(0, instructions.size)
                addInstructions(0, """
                    const/4 v0, 0x1
                    return v0
                """)
            }

            SubscriptionTypeTruckFingerprint.method.apply {
                removeInstructions(0, instructions.size)
                addInstructions(0, """
                    const/4 v0, 0x0
                    return v0
                """)
            }

            SubscriptionDetailsIsTruckFingerprint.method.apply {
                removeInstructions(0, instructions.size)
                addInstructions(0, """
                    const/4 v0, 0x0
                    return v0
                """)
            }

            return@execute
        }

        SubscriptionTypeCarFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, """
                const/4 v0, 0x0
                return v0
            """)
        }

        SubscriptionTypeTruckFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, """
                const/4 v0, 0x1
                return v0
            """)
        }

        SubscriptionDetailsIsTruckFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, """
                const/4 v0, 0x1
                return v0
            """)
        }

        // Truck path 1: Db/d default branch (a>=4) → TRUE
        TruckGateDefaultBranchFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """)
        }

        // Truck path 2: showstopper gate → false
        TruckShowstopperGateFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, """
                const/4 v0, 0x0
                return v0
            """)
        }

        TruckPurchasedToastGateFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, """
                const/4 v0, 0x0
                return v0
            """)
        }

        TruckCreateProfileDialogFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, """
                const/4 v0, 0x0
                return-object v0
            """)
        }

        // Truck path 3: NavBanner dismiss button → no-op
        TruckNavBannerSubscribeFingerprint.method.addInstructions(0, """
            iget v0, p0, Le9/P0;->a:I
            const/4 v1, 0x1
            if-ne v0, v1, :cond_original
            return-void
            :cond_original
        """)

        // Truck path 4: subscription screen always on car tab
        SubscriptionScreenTruckTabFingerprint.method.apply {
            replaceInstruction(50, "const/4 v0, 0x0")
            replaceInstruction(57, "const/4 v0, 0x0")
            replaceInstruction(58, "const/4 v0, 0x0")
        }

        // Truck path 5: show truck NavBanner
        ShowLargeVehiclesBannerFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """)
        }

        // Truck path 6: NavBanner body tap → no-op
        TruckBannerMessageClickFingerprint.method.addInstructions(0, """
            iget v0, p0, LPc/v;->a:I
            const/4 v1, 0x4
            if-ne v0, v1, :cond_original
            return-void
            :cond_original
        """)

        // Truck path 7: block Urban Airship ModalActivity launch (server-triggered truck IAM).
        // ai/i.invoke() is a singleton Function2 called when Airship delivers the truck
        // subscription in-app message on startup. It creates ui/a and calls a() which starts
        // ModalActivity. Return Unit immediately to suppress all Airship IAMs for this app.
        AirshipIAMLauncherFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, """
                sget-object v0, Lmk/u;->a:Lmk/u;
                return-object v0
            """)
        }
    }
}
