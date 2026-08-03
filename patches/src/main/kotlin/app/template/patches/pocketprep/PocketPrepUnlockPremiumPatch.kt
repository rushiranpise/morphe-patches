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

// Replaces UI strings that reference the free/upsell state with subscribed equivalents.
// All string keys are stable resource names (not obfuscated) and have been verified
// present in 3.28.1. If a key disappears in a future release, the resource patch
// will silently skip it rather than crashing.
private val pocketPrepPremiumLabelsPatch = resourcePatch(
    name = "Pocket Prep Premium Labels",
    description = "Replaces free-tier and upgrade UI strings with subscribed equivalents.",
    default = false
) {
    compatibleWith(*ALL_VARIANTS)

    execute {
        document("res/values/strings.xml").use { document ->
            val replacements = mapOf(
                "settings_study_plan_title_free_prep"    to "Premium Pocket Prep",
                "settings_study_plan_subtitle_free"      to "Subscribed - 12 Months",
                "settings_study_plan_upgrade_to_premium" to "Subscribed",
                "choose_plan_purchase_premium"            to "Subscribed",
                "configure_quiz_subscribe_to_use"         to "Subscribed",
                "out_of_free_questions_dialog_upgrade"    to "Subscribed",
                "take_quiz_premium_question_upgrade"      to "Subscribed",
                "purchase_complete_header"                to "You're Subscribed",
                "purchase_complete_body"                  to "Premium Pocket Prep is active for 12 months."
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
    description = "Unlocks Pocket Prep premium subscription gates and the full question bank.",
    default = true
) {
    compatibleWith(*ALL_VARIANTS)
    dependsOn(pocketPrepPremiumLabelsPatch)

    execute {
        // ── Subscription instance boolean gates → always true ────────────────
        //
        // All seven methods live on the stable unobfuscated class
        // com.pocketprep.android.api.common.Subscription and are fingerprinted
        // by definingClass + name, so they survive code shuffles inside the class.
        //
        //   g()Z              isActive       — expiry date check
        //   b()Z              isBundle       — bundle-pointer check
        //   c(String)Z        supportsExam   — matchesExam && source==SUPPORT
        //   d(String)Z        matchesExam    — examGuid.equalsIgnoreCase
        //   e(String)Z        activeForExam  — matches && !support && !teach
        //   f(String)Z        teachForExam   — matchesExam && source==TEACH
        listOf(
            SubscriptionIsActiveFingerprint,
            SubscriptionIsBundleFingerprint,
            SubscriptionSupportsExamFingerprint,
            SubscriptionMatchesExamFingerprint,
            SubscriptionActiveForExamFingerprint,
            SubscriptionTeachForExamFingerprint,
        ).forEach { it.method.addInstructions(0, "const/4 v0, 0x1\nreturn v0") }

        // ── SubscriptionPlan enum getter → always YEARLY ─────────────────────
        //
        // Subscription.a() returns the SubscriptionPlan enum (obfuscated class name
        // changes per release: wd9 → cg9 → uf9 in 3.28.1). The YEARLY variant is
        // always stored in field B of that enum. We read the actual return type from
        // the matched method at patch-execute time so this survives the next rename.
        val planEnumType = SubscriptionPlanFingerprint.method.returnType
        SubscriptionPlanFingerprint.method.addInstructions(
            0,
            "sget-object v0, $planEnumType->B:$planEnumType\nreturn-object v0"
        )

        // ── Subscription utility statics → always true ────────────────────────
        //
        // hasAnyActiveSubscription(Collection) and hasActiveSubscriptionForExam(Collection, CompositeKey)
        // live in an obfuscated utility class whose name changes every release
        // (ce9 → ig9 → ag9 in 3.28.1). Fingerprinted by signature + isEmpty/iterator body
        // shape so they match regardless of class rename.
        HasAnyActiveSubscriptionFingerprint.method
            .addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        HasActiveSubscriptionForExamFingerprint.method
            .addInstructions(0, "const/4 v0, 0x1\nreturn v0")

        // ── Exam-level question pool selector → always PREMIUM_FROM_CURRENT_BUNDLE
        //
        // cg9.I(ExamMetadata, List<Subscription>): e87  (was kg9.l0 → q77 in ≤3.27.x)
        //
        // This is the single gate that controls which question set is served.
        // When the return value is e87.z (NO_PREMIUM), the caller (l03.m0 or equivalent)
        // reads only ExamQuestions.c (≈80 isFree=true questions). Returning
        // e87.B (PREMIUM_FROM_CURRENT_BUNDLE) makes it read ExamQuestions.b
        // (the full bank). The fingerprint targets the enum field accesses inside
        // the method body so it survives the class/method rename on next update.
        // If the status enum class is renamed again, update `returnType` in
        // ExamSubscriptionStatusFingerprint to match.
        ExamSubscriptionStatusFingerprint.method.addInstructions(
            0,
            "sget-object v0, Le87;->B:Le87;\nreturn-object v0"
        )
    }
}
