package app.template.patches.blockblast

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import app.template.patches.shared.Constants.BLOCKBLAST_COMPATIBILITY

// Block Blast drives piece generation, difficulty, revives and ads through server-assigned A/B
// "traits" (Cocos JS modules). These patches rewrite the active trait map before the game
// instantiates any trait, and remap offer types at PuzzleUtil.offerTopic, the single entry point
// every piece generator goes through.

private const val TRAITS = "Lapp/template/extension/extension/BlockBlastTraits;"
private const val APPLICATION = "Lcom/netease/nis/wrapper/MyApplication;"

// OFFER_TYPE ids (AlgorithmType module).
private const val RANDOM_NO_DEATH = 7
private const val SDK_RANDOM_NO_DEATH = 17001

// Unwinnable / death offers: SI_WANG, ZHI_SI_TI, DEATH_IMPRESSION, ALL_CAN_PUT_DIE, MIDDLE_BLOCK_DEATH,
// FULL_BOARD_DEATH, the 19xxx and 24xxx death families, MISLEADING/NEAR_MISS/NEBULA/CLEAR_COMBO/LURED
// deaths, INCREASE_ENTROPY3_DIE, ZHI_SI_SHANG_ZENG, DEATH_FALLACY_HARD, ALGO_LOWER_SCORE_DIE.
private val DEATH_OFFERS = listOf(
    3, 9, 2712, 5001, 5002, 5005, 19002, 19004, 19005, 19008, 19009, 19011, 19012,
    24003, 24004, 24006, 24007, 5300027, 5900007, 5900012, 5900013, 5900022, 3022, 3023, 16056, 291091,
)
private val DEATH_SDK = listOf(24001, 24003, 24004, 24006, 24007)

// Clutter offers (pieces that fit but cannot clear): SHANG_ZENG_1/3/3_NO_BIT/1_NEW/4/3_NO_DOWN,
// iOS variants, SHOUYI/SAOLEI/WEIGHT/QUYU/HABIT/X_8 variants, letter clutter, same-block/other/CX/edge.
private val CLUTTER_OFFERS = listOf(
    1, 2, 10, 11001, 3002, 3007, 3011, 3012, 3016, 3017, 3020, 12026, 12031, 12035, 12039, 12040,
    291054, 291065, 291074, 5400001, 5400003,
)

private const val ENGINE_JS = """
(function (cfgs) {
  if (window.__bbTraits) return;
  var tci = window.traitConfigInfo, T = window.Trait;
  if (!tci || !T || !window.__require || !window.TRAIT) return;
  window.__bbTraits = {};
  function log(m) { console.log('BBTraits ' + m); }
  function merge(dst, src) { for (var k in src) dst[k] = src[k]; return dst; }
  try {
    var off = {}, on = {}, tune = {}, offers = {}, sdk = {};
    cfgs.forEach(function (c) {
      var k;
      (c.off || []).forEach(function (n) { off[n] = true; });
      for (k in c.on || {}) on[k] = merge(on[k] || {}, c.on[k]);
      for (k in c.tune || {}) tune[k] = merge(tune[k] || {}, c.tune[k]);
      merge(offers, (c.remap || {}).offer || {});
      merge(sdk, (c.remap || {}).sdk || {});
    });
    for (var n in on) delete off[n];

    function variant(name, want) {
      var byId = tci.traitsByIdMap, first = null;
      for (var id in byId) {
        if (byId[id].traitClassName !== name) continue;
        if (+id === want) return { id: +id, info: byId[id] };
        if (!first) first = { id: +id, info: byId[id] };
      }
      return first;
    }
    function withOverrides(name, base) {
      var p = merge(merge(merge({}, base || {}), on[name] || {}), tune[name] || {});
      delete p.__variant;
      if (p.firing === 0) p.firing = 1;
      if (p.active === false) delete p.active;
      return p;
    }
    function forced(name) {
      var v = variant(name, (on[name] || {}).__variant);
      if (!v) { log('no variant for ' + name); return null; }
      return withOverrides(name, merge({ id: v.id, feature_key: v.info.traitName }, v.info.param || {}));
    }
    // Runs right before the game instantiates traits and loads their bundles.
    function editMap(map) {
      var n, p;
      for (n in off) delete map[n];
      for (n in map) if (on[n] || tune[n]) map[n].param = withOverrides(n, map[n].param);
      for (n in on) if (!map[n] && (p = forced(n))) map[n] = { id: p.id, param: p };
    }
    // Fallback when the config was already applied: use the game's own runtime trait APIs.
    function applyLive() {
      var map = tci._traitsClassNameMap, add = [], n, p, t;
      T.dynamicDisableTraitsByName(Object.keys(off));
      for (n in off) delete map[n];
      for (n in map) if (on[n] || tune[n]) {
        map[n].param = withOverrides(n, map[n].param);
        if ((t = TRAIT(n))) { t._props = map[n].param; t._cachedStaticActive = undefined; }
      }
      for (n in on) if (!map[n] && (p = forced(n))) add.push(p);
      if (add.length) T.dynamicEnableTraitsAsync(add);
    }

    // The server can push traits at runtime; keep disabled ones out.
    ['dynamicEnableTraits', 'dynamicEnableTraitsAsync'].forEach(function (f) {
      var orig = T[f];
      T[f] = function (list) {
        if (Array.isArray(list)) list = list.filter(function (item) {
          var info = tci.traitsByIdMap[item.id];
          return !(info && off[info.traitClassName]);
        });
        return orig.call(this, list);
      };
    });

    if (Object.keys(offers).length || Object.keys(sdk).length) {
      var PU = window.__require('PuzzleUtil').PuzzleUtil, offerTopic = PU.offerTopic;
      PU.offerTopic = function (args, type, isOffer) {
        var m = isOffer === false ? sdk : offers;
        function swap(x) { return m[x] !== undefined ? m[x] : x; }
        return offerTopic.call(this, args, Array.isArray(type) ? type.map(swap) : swap(type), isOffer);
      };
    }

    if (tci._configReady) {
      applyLive();
      log('applied live');
    } else {
      var ready = false;
      Object.defineProperty(tci, '_configReady', {
        configurable: true,
        enumerable: true,
        get: function () { return ready; },
        set: function (v) {
          if (v && !ready) {
            try { editMap(tci._traitsClassNameMap); log('applied'); } catch (e) { log('edit failed ' + e); }
          }
          ready = v;
        }
      });
      log('hooked');
    }
    window.__bbTraits = { off: Object.keys(off), on: on, tune: tune, offers: offers, sdk: sdk };
  } catch (e) {
    log('install failed ' + e);
  }
  try {
    jsb.reflection.callStaticMethod('app/template/extension/extension/BlockBlastTraits', 'installed', '()V');
  } catch (e) {}
})
"""

@JvmInline
private value class RawJson(val json: String)

private fun json(value: Any?): String = when (value) {
    null -> "null"
    is RawJson -> value.json
    is String -> "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
    is Map<*, *> -> value.entries.joinToString(",", "{", "}") { json(it.key.toString()) + ":" + json(it.value) }
    is List<*> -> value.joinToString(",", "[", "]") { json(it) }
    else -> value.toString()
}

private fun smaliString(value: String) =
    value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")

private fun traitName(name: String) = name.trim().let { if (it.endsWith("Trait")) it else it + "Trait" }

private fun BytecodePatchContext.onCreate() =
    mutableClassDefBy(APPLICATION).methods.first { it.name == "onCreate" }

private fun BytecodePatchContext.addTraitConfig(config: String) = onCreate().addInstructions(
    0,
    """
        const-string v0, "${smaliString(config)}"
        invoke-static {v0}, $TRAITS->add(Ljava/lang/String;)V
    """,
)

private val blockBlastTraitEnginePatch = bytecodePatch {
    compatibleWith(BLOCKBLAST_COMPATIBILITY)
    extendWith("extensions/extension.mpe")

    execute {
        onCreate().addInstructions(
            0,
            """
                const-string v0, "${smaliString(ENGINE_JS.trimIndent())}"
                invoke-static {p0, v0}, $TRAITS->start(Landroid/content/Context;Ljava/lang/String;)V
            """,
        )
    }
}

private fun traitConfig(
    off: List<String> = emptyList(),
    on: Map<String, Map<String, Any?>> = emptyMap(),
    tune: Map<String, Map<String, Any?>> = emptyMap(),
    remapOffers: Map<Int, Int> = emptyMap(),
    remapSdk: Map<Int, Int> = emptyMap(),
) = json(
    mapOf(
        "off" to off.map(::traitName),
        "on" to on.mapKeys { traitName(it.key) },
        "tune" to tune.mapKeys { traitName(it.key) },
        "remap" to mapOf("offer" to remapOffers, "sdk" to remapSdk),
    ),
)

// Names may omit the "Trait" suffix. on = force enabled (+ params), tune = change params only if active.
private fun traitPatch(
    name: String,
    description: String,
    off: List<String> = emptyList(),
    on: Map<String, Map<String, Any?>> = emptyMap(),
    tune: Map<String, Map<String, Any?>> = emptyMap(),
    remapOffers: Map<Int, Int> = emptyMap(),
    remapSdk: Map<Int, Int> = emptyMap(),
    default: Boolean = true,
) = bytecodePatch(name = name, description = description, default = default) {
    compatibleWith(BLOCKBLAST_COMPATIBILITY)
    dependsOn(blockBlastTraitEnginePatch)

    execute { addTraitConfig(traitConfig(off, on, tune, remapOffers, remapSdk)) }
}

private const val HUGE = 1_000_000_000

// region Difficulty

@Suppress("unused")
val blockBlastDisableTimedHardPuzzlesPatch = traitPatch(
    name = "Disable timed hard puzzles",
    description = "Stops the hard puzzle that is forced roughly every 30-90 seconds after the first 100-180 seconds of a game.",
    off = listOf(
        "IsPuzzleTime", "PuzzleBeforeTiming", "PuzzleAfterTiming", "ProblemIntervalTimeRandomBeta",
        "PuzzleConditionDiff", "AdjustPercentCh", "BPuzzleTime", "MergeBlocksIsPuzzleTime",
        "FirstRoundReduceTime", "HighWeightSessionQuestionReplacer",
    ),
)

@Suppress("unused")
val blockBlastDisableRunEnderPatch = traitPatch(
    name = "Disable run-ender",
    description = "Stops the switch to only hard and unwinnable pieces after 5-6 minutes of a game.",
    off = listOf(
        "Puzzle100", "PuzzleTiming100", "PuzzleSuccess100", "PuzzleFail100", "PuzzleTimingMaxPriority",
        "Puzzle100WeightCondition", "DeliveryPuzzleTiming100", "LWMDualConditionHardPuzzle",
    ),
)

@Suppress("unused")
val blockBlastDisableUnwinnableOffersPatch = traitPatch(
    name = "Disable unwinnable offers",
    description = "Replaces deliberately unwinnable piece sets with random sets that can always be placed.",
    off = listOf(
        "DeadlyToHard", "EndlessDeathBoard", "DifferentStrategyDie", "ClassNewDeathOptimize",
        "IsDelayHardAndRandomNodieProblemTime3", "IsDelayHardAndRandomNodieProblemTime4",
        "IsDelayHardAndRandomNodieProblemTime5", "IsDelayHardAndRandomNodieProblemTime6",
    ),
    remapOffers = DEATH_OFFERS.associateWith { RANDOM_NO_DEATH },
    remapSdk = DEATH_SDK.associateWith { SDK_RANDOM_NO_DEATH },
)

@Suppress("unused")
val blockBlastDisableAdDrivenGameOversPatch = traitPatch(
    name = "Disable ad-driven game overs",
    description = "Stops ending runs on purpose when ad prices are high (eCPM-triggered death puzzles and boost-then-kill).",
    off = listOf(
        "ADPushBackModel", "Ecpm_player_high_death", "MultiClearBoostThenKill",
        "IsOpenInAdvanceDeathByLowEfficiencyGiveRevive",
    ),
)

@Suppress("unused")
val blockBlastDisableFastPlayPunishmentPatch = traitPatch(
    name = "Disable fast-play punishment",
    description = "Stops serving harder pieces to players who place quickly near their high score.",
    off = listOf("FastHandPunish"),
)

@Suppress("unused")
val blockBlastDisableComboLimitsPatch = traitPatch(
    name = "Disable combo limits",
    description = "Stops the game from breaking or capping long combo streaks.",
    off = listOf(
        "LimitHighCombo", "MoreClearStopCombo", "IsOpenComboInterruptionStrategyReplaceBlock",
        "IsOpenComboInterruptionStrategyBlockLimit", "ChangeOfferWithCombo", "HighComboScoringRuleAdjust",
        "LimitComboScoreToTen", "IsOpencomboDifficultFirstBlockid",
    ),
)

@Suppress("unused")
val blockBlastDisableScoreCapsPatch = traitPatch(
    name = "Disable score caps",
    description = "Removes score regulation that holds games to a fraction of the high score or scales combos down.",
    off = listOf(
        "HighScoreLimitByUserAbility", "NewbieScoreRegulation", "NewbieScoreRegulation2", "TravelScoreCutoffByProgress",
        "ScoreController", "RecordBreakScoreRegulation", "RecordBreakScoreControl", "ScoreGrowthRecordControl",
        "RandomSeedHighScoreLimit", "DynamicComboMultiplier", "ClassFirstNoAddScore", "ReviveBlockScore",
        // Hook-only traits the server can still push through adjust configs.
        "ScoreReduction", "ScoreRatioComboCoeff", "ComboDDAStrategy", "ColdStartComboSuppress", "CancelPlacementScore",
    ),
)

@Suppress("unused")
val blockBlastDisableHighScoreSpikePatch = traitPatch(
    name = "Disable high score difficulty spike",
    description = "Keeps the easy difficulty lane when approaching or beating the high score.",
    off = listOf("HighNearDanger", "TwoHighScoreExperiences"),
    tune = mapOf(
        "BaseplateLane" to mapOf("sections" to listOf(listOf(listOf(HUGE, 1, false)))),
        "BaseplateConditionDiff" to mapOf(
            "winalgos" to listOf(RANDOM_NO_DEATH, RANDOM_NO_DEATH, RANDOM_NO_DEATH),
            "failalgos" to listOf(RANDOM_NO_DEATH, RANDOM_NO_DEATH, RANDOM_NO_DEATH),
        ),
    ),
)

@Suppress("unused")
val blockBlastDisableClutterPiecesPatch = traitPatch(
    name = "Disable clutter pieces",
    description = "Replaces piece sets built to fit but never clear lines with random placeable sets.",
    off = listOf(
        "ClassicsRandomReplace", "IsOpenShang1ToShang3", "IsOpenZiMuShangZeng", "Shangzeng3ReplaceZimushangzeng",
        "AlgoSameBlockShangZeng", "AlgoSymmetryBlock", "ShangZengAddRandom", "EntropyIncreaseBeforeFill",
        "EntropyIncreaseBeforeFillReduceTime", "EntropyIncreaseBeforeFillWithRound", "AddSpaceAdd4",
    ),
    remapOffers = CLUTTER_OFFERS.associateWith { RANDOM_NO_DEATH },
)

@Suppress("unused")
val blockBlastDisableExtraHardVariantsPatch = traitPatch(
    name = "Disable extra-hard puzzle variants",
    description = "Stops upgrades of hard puzzles to harder variants and anti-pattern difficulty.",
    off = listOf(
        "IsOpenZhiJueToKunNan", "CrossClearHard", "ParallelClearHard", "AlgoRegretHard", "ExtremeHard",
        "FirstDifAfterBecomeDif", "Find_link_border_difficulty_logic", "Find_link_border_difficulty_time",
        "AdvantureScore", "AvoidClearBoard", "BeforeThreeRoundRemoveHunHeTianKong", "TerminateRefreshScore",
    ),
)

@Suppress("unused")
val blockBlastDisableSmallPieceRestrictionsPatch = traitPatch(
    name = "Disable small-piece restrictions",
    description = "Keeps small pieces in the pool after 2000-3000 points.",
    off = listOf("BlockLimitClassics", "BlockLimitLevel", "FillExclude", "LimitNoviceBlockSize"),
)

@Suppress("unused")
val blockBlastDisableAdaptiveDifficultyPatch = traitPatch(
    name = "Disable adaptive difficulty models",
    description = "Turns off models that adapt piece difficulty to play habits and predicted moves.",
    off = listOf(
        "SequencePredictDynamicDifficulty", "PlaceHabitAdaptPath", "MABXiBai", "LoginTimeFirstDifficultyRatio",
        "ReplaceBoard",
    ),
)

// endregion

// region Score

@Suppress("unused")
val blockBlastDisableCheatScoreFlaggingPatch = traitPatch(
    name = "Disable cheat-score flagging",
    description = "Stops the downloaded model that marks game-end scores as cheated.",
    off = listOf("JudgeCheatScoreCtrl"),
)

@Suppress("unused")
val blockBlastLoyaltyScoreMultiplierPatch = traitPatch(
    name = "Loyalty score multiplier",
    description = "Multiplies line-clear points by up to 1 + ln(days played): about x2 after 3 days, x3 after a week, x4 after a month.",
    on = mapOf("Param230012" to mapOf("type" to 7)),
    default = false,
)

private fun BytecodePatchContext.addScoreTraitConfig(on: Map<String, Map<String, Any?>>, off: List<String> = emptyList()) =
    addTraitConfig(traitConfig(off = off, on = on))

@Suppress("unused")
val blockBlastStrongerComboMultiplierPatch = bytecodePatch(
    name = "Stronger combo multiplier",
    description = "Raises the bonus multiplier applied to line-clear points during long combos (game default: >5 x1.5, >10 x2).",
    default = false,
) {
    compatibleWith(BLOCKBLAST_COMPATIBILITY)
    dependsOn(blockBlastTraitEnginePatch)

    val strength by stringOption(
        key = "blockBlastComboMultiplier",
        default = "3:2:6:3",
        values = mapOf(
            "Mild (combo >3 x2, >6 x3)" to "3:2:6:3",
            "Strong (combo >2 x3, >5 x5)" to "2:3:5:5",
            "Extreme (combo >1 x5, >3 x10)" to "1:5:3:10",
        ),
        title = "Strength",
        description = "Combo thresholds and multipliers (threshold1:multiplier1:threshold2:multiplier2, multipliers 1-99).",
    )

    execute {
        val (t1, m1, t2, m2) = strength!!.split(':').map(String::toDouble)
        addScoreTraitConfig(
            mapOf(
                "BComboScoreChange" to mapOf(
                    "comboThreshold1" to t1.toInt(), "comboMultiplier1" to m1,
                    "comboThreshold2" to t2.toInt(), "comboMultiplier2" to m2,
                    // Unset the score above which some variants stop the boost.
                    "triggerScore" to null,
                ),
            ),
        )
    }
}

@Suppress("unused")
val blockBlastFasterComboBuildUpPatch = bytecodePatch(
    name = "Faster combo build-up",
    description = "Each cleared line adds more combo steps (game default: a clear adds one step per line).",
    default = false,
) {
    compatibleWith(BLOCKBLAST_COMPATIBILITY)
    dependsOn(blockBlastTraitEnginePatch)

    val stepsPerLine by stringOption(
        key = "blockBlastComboStepsPerLine",
        default = "2",
        values = mapOf("2 per line" to "2", "3 per line" to "3", "5 per line" to "5"),
        title = "Combo steps per line",
        description = "Combo steps added for each cleared line (1-5).",
    )

    execute {
        addScoreTraitConfig(
            mapOf(
                "ComboAddRemoveNum" to mapOf(
                    "comboMultiple" to stepsPerLine!!.toInt(),
                    "comboOffsetValue" to 0,
                    "isAccumulateCombo" to true,
                ),
            ),
        )
    }
}

@Suppress("unused")
val blockBlastEasierComboRescuePatch = bytecodePatch(
    name = "Easier combo rescue",
    description = "A broken combo of 3+ is restored by a multi-line clear within more placements, and resumes one higher.",
    default = false,
) {
    compatibleWith(BLOCKBLAST_COMPATIBILITY)
    dependsOn(blockBlastTraitEnginePatch)

    val window by stringOption(
        key = "blockBlastComboRescueWindow",
        default = "3",
        values = mapOf("2 placements" to "2", "3 placements" to "3", "5 placements" to "5"),
        title = "Rescue window",
        description = "Placements after a combo breaks during which a multi-line clear restores it (game default: 1).",
    )

    execute {
        addScoreTraitConfig(
            on = mapOf("IsOpenComboContinuous" to mapOf("limitCount" to window!!.toInt())),
            off = listOf("ComboScoreSplit"),
        )
    }
}

// endregion

// region Assists

@Suppress("unused")
val blockBlastAlwaysPlaceablePiecesPatch = traitPatch(
    name = "Always placeable pieces",
    description = "Swaps pieces that cannot be placed anywhere for ones that fit.",
    on = mapOf(
        "OneRoundNoCleanBlock" to emptyMap(),
        "FixthreeblocksRandomReplace" to emptyMap(),
        "ClassPerfectNextNoDie" to emptyMap(),
        "FeatPerfectTranslate" to emptyMap(),
        "IsOpenRandomToRandomNoDie" to emptyMap(),
        "RepateCPlusPlusRandomNoDeath" to emptyMap(),
    ),
)

@Suppress("unused")
val blockBlastEasierGameStartPatch = traitPatch(
    name = "Easier game start",
    description = "Gap-filling pieces in the first rounds, no repeat of the pieces that ended the last game.",
    on = mapOf(
        "EveryGamePreRoundForceToFill" to mapOf("round" to 3),
        "FirstRoundSpiceOffer" to emptyMap(),
        "NoFailBlock" to emptyMap(),
        "InitBoardTolerance" to emptyMap(),
        "SlowPacedUserInitBoard" to emptyMap(),
    ),
)

@Suppress("unused")
val blockBlastClearBoardHelpPatch = traitPatch(
    name = "Clear-board help",
    description = "Offers whole-board clears near the high score and keeps combos alive after a clear.",
    on = mapOf(
        "TryCleanAllBlocks" to emptyMap(),
        "\$39311_f_newPlayerClearHelp" to emptyMap(),
        "ClassicComboProtectAfterAllClean" to emptyMap(),
        "NoLimitClearScreen" to emptyMap(),
    ),
)

@Suppress("unused")
val blockBlastComebackAssistsPatch = traitPatch(
    name = "Stronger comeback assists",
    description = "Enables and strengthens the game's own help after losses, low scores and broken combos.",
    on = mapOf(
        "ConsecutiveLossRecovery" to mapOf("protectRounds" to 10, "sessionTriggerLimit" to 99),
        "HighScoreBreakthroughBoost" to mapOf("highScoreProgressPercent" to 50),
        "FastNearTkxcClass" to mapOf("rate" to 1),
        "LowFeedbackPleasurableOffer" to mapOf("lowFeedbackRounds" to 2),
        "ComboHelpAlgo" to emptyMap(),
        "OptimalFillClear" to emptyMap(),
        "MoreAreaTKXC" to emptyMap(),
        "LowHardOfferTimes" to emptyMap(),
        "LowHardOfferTimesToEverayone" to emptyMap(),
        "DifficultSimplePuzzles" to emptyMap(),
    ),
)

// endregion

// region Revives

@Suppress("unused")
val blockBlastAlwaysOfferRevivePatch = traitPatch(
    name = "Always offer revive",
    description = "Offers a revive on every game over, even below the high score or without a loaded ad.",
    off = listOf(
        "IsOpenNoVideoNoRevivie", "TestReadyByAdTypeInterface", "DecreaseReviveUIShowByLimitHighScore",
        "ReviveStrategyAdjust", "ChangeReviveStrategyByLTV", "ReviveActiveByEcpmValue",
    ),
    tune = mapOf(
        "ReviveLimit" to mapOf("per" to 0, "score" to 0),
        "LevelReviveCondi" to mapOf("per" to 0),
    ),
)

@Suppress("unused")
val blockBlastUnlimitedRevivesPatch = traitPatch(
    name = "Unlimited revives",
    description = "Raises the revive limit per game to 99.",
    on = mapOf(
        "ReviveCount" to mapOf("count" to 99),
        "LevelReviveCount" to mapOf("count" to 99),
    ),
)

@Suppress("unused")
val blockBlastBetterRevivesPatch = traitPatch(
    name = "Better revives",
    description = "Revives give line-clearing pieces and free revives are not withheld from high ad-value players.",
    on = mapOf(
        "IsOpenReviveSpecial" to emptyMap(),
        "NewLifeBlockCtrl" to emptyMap(),
    ),
    tune = mapOf(
        "ReviveCleanBoard" to mapOf("repeatBlockProbability" to 0),
        "NewUserFreeRevive" to mapOf("ecpmSkipThreshold" to HUGE),
        "NewUserFreeReviveModifyEcpm" to mapOf("otherRegionThreshold" to HUGE, "targetRegionThreshold" to HUGE),
    ),
)

// endregion

// region Ads and monetization

@Suppress("unused")
val blockBlastReduceAdTraitsPatch = traitPatch(
    name = "Reduce ads (traits)",
    description = "Disables ad-increasing traits and enables the game's own ad-protection traits.",
    off = listOf(
        "FullScreenAdAddByEcpm", "IsOpenNextGameIsAddInterByEcpm", "AppShowPlayInterstitialAd",
        "ReplaceRewardADToInterstitial", "IsOpenShowBanner", "IsOpenBannerTimeCtr", "AdPlusMinusStrategy",
        "GLShowAdsByGameTime", "LaunchBrandAdDisplay", "LaunchBrandAd2Display", "LaunchBrandAdJump",
        "LaunchBrandAdFrequency", "LaunchBrandAdRetention", "LaunchBrandAdNative",
    ),
    on = mapOf(
        "AdProtect" to mapOf("first" to HUGE, "noFirst" to HUGE),
        "ScoreNoAd" to mapOf("score" to HUGE),
        "AdInterval" to mapOf("time" to 3600),
        "RewardPlayFailNoFull" to emptyMap(),
        "ReviveSkipEndAdFlag" to emptyMap(),
        "LaunchBrandAdSkip" to emptyMap(),
    ),
)

@Suppress("unused")
val blockBlastDisableAdProfilingPatch = traitPatch(
    name = "Disable ad profiling and upsells",
    description = "Stops ad-value profiling of the player and subscription / revive-ad nudges.",
    off = listOf(
        "MaturityDevelopmentV2", "RewardHighValueAdHarvest", "AdSubEntry", "AdSubScribePopModel",
        "AdSubScribePopModelSupplement", "ClassReviveAdGuide",
    ),
)

// endregion

// region Quality of life

@Suppress("unused")
val blockBlastKeepSavedGamePatch = traitPatch(
    name = "Keep saved game",
    description = "Stops discarding the saved board after a few hours away.",
    off = listOf("IsWarmhands"),
)

@Suppress("unused")
val blockBlastDisableTouchThrottlingPatch = traitPatch(
    name = "Disable touch throttling",
    description = "Stops ignoring piece touches that follow the previous one too quickly.",
    off = listOf("IsOpenChangeTouchSpace"),
)

@Suppress("unused")
val blockBlastDisablePopupsPatch = traitPatch(
    name = "Disable popups and push nudges",
    description = "Disables rating prompts, more-games popups and re-engagement pushes.",
    off = listOf("BCommentGuarantee", "GLResultView6", "IsOpenGLHallMoreGamesPopup", "TopDailyActiveCountryOnlinePeakPush"),
)

// endregion

private fun splitList(value: String?, separator: Char) =
    value.orEmpty().split(separator).map(String::trim).filter(String::isNotEmpty)

private fun rawJson(value: String) = RawJson(
    if (value.toDoubleOrNull() != null || value in setOf("true", "false", "null") || value.firstOrNull() in "[{\"".toList()) {
        value
    } else {
        json(value)
    },
)

@Suppress("unused")
val blockBlastCustomTraitOverridesPatch = bytecodePatch(
    name = "Custom trait overrides",
    description = "Force any trait off or on, change trait parameters and remap piece generators.",
    default = true,
) {
    compatibleWith(BLOCKBLAST_COMPATIBILITY)
    dependsOn(blockBlastTraitEnginePatch)

    val disable by stringOption(
        key = "blockBlastTraitsDisable",
        default = "",
        title = "Disable traits",
        description = "Comma-separated trait names to force off, e.g. FastHandPunish, IsWarmhands.",
    )
    val enable by stringOption(
        key = "blockBlastTraitsEnable",
        default = "",
        title = "Enable traits",
        description = "Comma-separated trait names to force on, optionally with a variant id: NoFailBlock, AdInterval:328245004.",
    )
    val params by stringOption(
        key = "blockBlastTraitsParams",
        default = "",
        title = "Trait parameters",
        description = "Semicolon-separated Trait.param=value pairs (value is JSON), e.g. ReviveCount.count=99; AdInterval.time=600.",
    )
    val remap by stringOption(
        key = "blockBlastTraitsRemap",
        default = "",
        title = "Remap generators",
        description = "Comma-separated OFFER_TYPE from=to pairs, e.g. 6=7 turns hard puzzles into random no-death sets.",
    )
    val dump by booleanOption(
        key = "blockBlastTraitsDump",
        default = false,
        title = "Dump active traits",
        description = "Write the active trait config and score counters to Android/data/com.block.juggle/files/traits.json every minute.",
    )

    execute {
        val on = splitList(enable, ',').associate { entry ->
            val (name, variant) = (entry.split(':', limit = 2) + "").let { it[0] to it[1].trim().toIntOrNull() }
            traitName(name) to (if (variant != null) mapOf<String, Any>("__variant" to variant) else emptyMap())
        }
        val tune = splitList(params, ';').groupBy(
            { traitName(it.substringBefore('.')) },
            { it.substringAfter('.').substringBefore('=').trim() to rawJson(it.substringAfter('=').trim()) },
        ).mapValues { (_, pairs) -> pairs.toMap<String, Any>() }
        val remapOffers = splitList(remap, ',').associate {
            it.substringBefore('=').trim().toInt() to it.substringAfter('=').trim().toInt()
        }
        // Params for force-enabled traits travel with "on" so they also apply to the inserted variant.
        val merged = on.mapValues { (name, value) -> value + tune[name].orEmpty() }

        if (merged.isNotEmpty() || tune.isNotEmpty() || remapOffers.isNotEmpty() || splitList(disable, ',').isNotEmpty()) {
            addTraitConfig(traitConfig(splitList(disable, ','), merged, tune - merged.keys, remapOffers))
        }
        if (dump == true) {
            onCreate().addInstructions(0, "invoke-static {}, $TRAITS->dump()V")
        }
    }
}
