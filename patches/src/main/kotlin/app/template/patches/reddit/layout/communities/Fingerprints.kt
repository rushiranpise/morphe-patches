@file:Suppress("ClassName")

package app.template.patches.reddit.layout.communities

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.literal
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// Scoping parent — finds the class that renders community recommendation sections
// using the stable layout tag "community_recomendation_section_" (note the typo
// in Reddit's own code, preserved here for matching accuracy).
private object CommunityRecommendationSectionParentFingerprint : Fingerprint(
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    filters = listOf(
        string("community_recomendation_section_"),
    ),
)

/** 2026.18.0+ composable — uses stable layout tag strings. */
internal object CommunityRecommendationSection_2026_18_Fingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("L", "L", "I"),
    strings = listOf(
        "section_title",
        "recommendation_chaining",
    ),
)

/** 2026.16.0–2026.17.x composable — anchored on stable resource ID literals. */
internal object CommunityRecommendationSection_2026_16_Fingerprint : Fingerprint(
    classFingerprint = CommunityRecommendationSectionParentFingerprint,
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        literal(2140398383),
        literal(2043119215),
    ),
)

/** Legacy composable (< 2026.16.0) — anchored on stable string "feedContext". */
internal object CommunityRecommendationSectionLegacyFingerprint : Fingerprint(
    classFingerprint = CommunityRecommendationSectionParentFingerprint,
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        string("feedContext"),
    ),
)

/** Shared Compose wrapper method — present across all versions. */
internal object CommunityRecommendationsComposeMethodFingerprint : Fingerprint(
    classFingerprint = CommunityRecommendationSectionParentFingerprint,
    returnType = "V",
    parameters = listOf("L", "L", "I"),
)
