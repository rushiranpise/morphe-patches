package app.template.patches.pocketprep

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.POCKETPREP_BEHAVIORAL_HEALTH_COMPATIBILITY
import app.template.patches.shared.Constants.POCKETPREP_COMPATIBILITY
import app.template.patches.shared.Constants.POCKETPREP_EMS_COMPATIBILITY
import app.template.patches.shared.Constants.POCKETPREP_ESSENTIALS_COMPATIBILITY
import app.template.patches.shared.Constants.POCKETPREP_FITNESS_COMPATIBILITY
import app.template.patches.shared.Constants.POCKETPREP_MAIN_COMPATIBILITY
import app.template.patches.shared.Constants.POCKETPREP_MEDICAL_COMPATIBILITY
import app.template.patches.shared.Constants.POCKETPREP_NURSING_COMPATIBILITY
import app.template.patches.shared.Constants.POCKETPREP_NURSING_SCHOOL_COMPATIBILITY
import app.template.patches.shared.Constants.POCKETPREP_PROFESSIONAL_COMPATIBILITY
import app.template.patches.shared.Constants.POCKETPREP_SKILLED_TRADES_COMPATIBILITY
import org.w3c.dom.Element

private val ALL_VARIANTS = arrayOf(
    POCKETPREP_COMPATIBILITY,
    POCKETPREP_PROFESSIONAL_COMPATIBILITY,
    POCKETPREP_BEHAVIORAL_HEALTH_COMPATIBILITY,
    POCKETPREP_MEDICAL_COMPATIBILITY,
    POCKETPREP_NURSING_SCHOOL_COMPATIBILITY,
    POCKETPREP_NURSING_COMPATIBILITY,
    POCKETPREP_EMS_COMPATIBILITY,
    POCKETPREP_SKILLED_TRADES_COMPATIBILITY,
    POCKETPREP_FITNESS_COMPATIBILITY,
    POCKETPREP_ESSENTIALS_COMPATIBILITY,
    POCKETPREP_MAIN_COMPATIBILITY,
)

private val pocketPrepYearlyPremiumLabelsPatch = resourcePatch(
    name = "UNlock Premium ",
    description = "Unlocks Premium features in app.",
    default = false
) {
    compatibleWith(*ALL_VARIANTS)

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
    description = "Unlocks Pocket Prep premium subscription gates and full question bank.",
    default = true
) {
    compatibleWith(*ALL_VARIANTS)
    dependsOn(pocketPrepYearlyPremiumLabelsPatch)

    execute {
        // ── Boolean return gates (return true) ────────────────────────────────
        // Subscription instance methods: isActive, isBundle, supportsExam,
        // matchesExam, activeForExam, teachForExam. All have definingClass set so
        // these are matched directly without scanning all classes.
        listOf(
            SubscriptionIsActiveFingerprint,
            SubscriptionIsBundleFingerprint,
            SubscriptionSupportsExamFingerprint,
            SubscriptionMatchesExamFingerprint,
            SubscriptionActiveForExamFingerprint,
            SubscriptionTeachForExamFingerprint,
        ).forEach { it.method.addInstructions(0, "const/4 v0, 0x1\nreturn v0") }

        // Static helpers (class name changes per variant: ce9 → ig9 → ?).
        // Fingerprinted by signature + body shape only, no definingClass.
        HasAnyActiveSubscriptionFingerprint.method
            .addInstructions(0, "const/4 v0, 0x1\nreturn v0")

        HasActiveSubscriptionForExamFingerprint.method
            .addInstructions(0, "const/4 v0, 0x1\nreturn v0")

        // ── SubscriptionPlan enum getter ───────────────────────────────────────
        // Subscription.a() returns a SubscriptionPlan enum whose class name
        // changes per variant (wd9, cg9, …). We read the actual return type
        // from the matched method at patch-execute time and inject the B field
        // (always YEARLY in every known variant) dynamically.
        val planEnumClass = SubscriptionPlanFingerprint.method.returnType   // e.g. "Lcg9;"
        SubscriptionPlanFingerprint.method.addInstructions(
            0,
            "sget-object v0, $planEnumClass->B:$planEnumClass\nreturn-object v0"
        )

        // ── Exam-level question pool selector ─────────────────────────────────
        // kg9.l0(ExamMetadata, List<Subscription>) returns q77 (subscription
        // status enum). When it returns q77.z (NO_PREMIUM) — which happens when
        // the subscription list is empty — l03.m0() reads ExamQuestions.c
        // (only the ~80 isFree=true questions) instead of ExamQuestions.b (full
        // question bank). Returning q77.B (PREMIUM_FROM_CURRENT_BUNDLE) always
        // makes l03.m0() serve the complete question pool.
        ExamSubscriptionStatusFingerprint.method.addInstructions(
            0,
            "sget-object v0, Lq77;->B:Lq77;\nreturn-object v0"
        )
    }
}
