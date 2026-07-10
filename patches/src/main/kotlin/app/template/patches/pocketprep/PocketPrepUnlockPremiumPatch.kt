package app.template.patches.pocketprep

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.POCKETPREP_COMPATIBILITY
import org.w3c.dom.Element

private val pocketPrepYearlyPremiumLabelsPatch = resourcePatch(
    name = "UNlock Premium ",
    description = "Unlocks Premium features in app.",
    default = false
) {
    compatibleWith(POCKETPREP_COMPATIBILITY)

    execute {
        document("res/values/strings.xml").use { document ->
            val replacements = mapOf(
                "settings_study_plan_title_free_prep" to "Premium Pocket Prep",
                "settings_study_plan_subtitle_free" to "Subscribed - 12 Months",
                "settings_study_plan_upgrade_to_premium" to "Subscribed",
                "choose_plan_purchase_premium" to "Subscribed",
                "configure_quiz_subscribe_to_use" to "Subscribed",
                "out_of_free_questions_dialog_upgrade" to "Subscribed",
                "take_quiz_premium_question_upgrade" to "Subscribed",
                "purchase_complete_header" to "You're Subscribed",
                "purchase_complete_body" to "Premium Pocket Prep is active for 12 months."
            )

            val strings = document.getElementsByTagName("string")
            for (i in 0 until strings.length) {
                val node = strings.item(i) as Element
                replacements[node.getAttribute("name")]?.let { node.textContent = it }
            }
        }
    }
}

@Suppress("unused")
val pocketPrepUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Pocket Prep premium subscription gates.",
    default = true
) {
    compatibleWith(POCKETPREP_COMPATIBILITY)
    dependsOn(pocketPrepYearlyPremiumLabelsPatch)

    execute {
        listOf(
            SubscriptionIsActiveFingerprint,
            SubscriptionIsBundleFingerprint,
            SubscriptionSupportsExamFingerprint,
            SubscriptionMatchesExamFingerprint,
            SubscriptionActiveForExamFingerprint,
            SubscriptionTeachForExamFingerprint,
            HasAnyActiveSubscriptionFingerprint,
            HasActiveSubscriptionForExamFingerprint
        ).forEach { fingerprint ->
            fingerprint.match(classDefBy(fingerprint.definingClass!!))
                .method
                .addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        SubscriptionPlanFingerprint
            .match(classDefBy(SubscriptionPlanFingerprint.definingClass!!))
            .method
            .addInstructions(0, "sget-object v0, Lwd9;->B:Lwd9;\nreturn-object v0")

        ChoosePlanActionFingerprint
            .match(classDefBy(ChoosePlanActionFingerprint.definingClass!!))
            .method
            .addInstructions(
                0,
                """
                    new-instance v0, Lz11;
                    const/4 v1, 0x0
                    invoke-direct {v0, v1}, Lz11;-><init>(Z)V
                    return-object v0
                """
            )

        ConfigureSubjectStateConstructorFingerprint
            .match(classDefBy(ConfigureSubjectStateConstructorFingerprint.definingClass!!))
            .method
            .addInstructions(0, "const/4 p3, 0x1")

        ConfigureQuizStateConstructorFingerprint
            .match(classDefBy(ConfigureQuizStateConstructorFingerprint.definingClass!!))
            .method
            .addInstructions(0, "const/4 p13, 0x1")

        FreeQuestionCounterConstructorFingerprint
            .match(classDefBy(FreeQuestionCounterConstructorFingerprint.definingClass!!))
            .method
            .addInstructions(0, "const/4 p3, 0x1")

        PremiumAccessEventConstructorFingerprints.forEach { fingerprint ->
            fingerprint.match(classDefBy(fingerprint.definingClass!!))
                .method
                .addInstructions(0, "const/4 p1, 0x1")
        }
    }
}
