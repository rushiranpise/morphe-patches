package app.template.patches.reddit.layout.trending

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ── LocaleLanguageManager — provides content-language list ────────────────────

/**
 * Modern constructor (2026.16.0+). Anchored on stable android.content.Context
 * and Locale SDK calls that are never obfuscated.
 */
internal object LocaleLanguageManagerConstructorFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Landroid/content/res/Configuration;"),
    filters = listOf(
        methodCall(smali = "Landroid/content/Context;->getApplicationContext()Landroid/content/Context;"),
        methodCall(smali = "Ljava/util/Locale;->toLanguageTag()Ljava/lang/String;"),
        methodCall(smali = "Landroid/os/BaseBundle;->putString(Ljava/lang/String;Ljava/lang/String;)V"),
        fieldAccess(smali = "Ljava/util/Locale;->ENGLISH:Ljava/util/Locale;"),
        string("UI_LANGUAGE_TAG"),
    ),
)

/** Legacy constructor (< 2026.16.0). Anchored on the stable string "localeLanguageManager". */
internal object LocaleLanguageManagerConstructorLegacyFingerprint : Fingerprint(
    name = "<init>",
    returnType = "V",
    filters = listOf(
        string("localeLanguageManager"),
        opcode(Opcode.RETURN_VOID),
    ),
)

/** Accessor method that returns the list of content languages. */
internal object LocaleLanguageManagerContentLanguagesFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/util/", // matches ArrayList or List
    parameters = listOf(),
    filters = listOf(
        opcode(Opcode.IF_EQZ),
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            definingClass = "this",
            type = "Ljava/util/ArrayList;",
            location = MatchAfterImmediately(),
        ),
        opcode(
            opcode = Opcode.RETURN_OBJECT,
            location = MatchAfterImmediately(),
        ),
    ),
)

// ── Search section / trending item composable anchors ─────────────────────────

/**
 * The composable method that renders a search section header.
 * Anchored on the stable layout tag strings.
 */
internal object SearchSectionHeaderFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        string("search_section_header"),
        string("search_section_title"),
    ),
)

/**
 * The composable that renders a single trending search row (modern).
 */
internal object TrendingItemFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        string("search_trending_item"),
    ),
)

/**
 * Legacy trending item composable for older Reddit versions.
 */
internal object TrendingItemLegacyFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/typeahead/ui/zerostate/composables",
    returnType = "V",
    filters = listOf(
        string("search_trending_item"),
    ),
)

/**
 * Typeahead suggestion (community) row composable.
 */
internal object TypeaheadSuggestionItemFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        string("typeahead_suggestion_item"),
    ),
)

/**
 * Trending feed-unit section composable (2026.21.0+).
 */
internal object TrendingFeedUnitSectionFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        string("trending_feed_unit_section"),
    ),
)

/**
 * Dismissed trending feed-unit section composable (2026.21.0+).
 */
internal object TrendingFeedUnitDismissedSectionFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        string("trending_feed_unit_dismissed_section"),
    ),
)
