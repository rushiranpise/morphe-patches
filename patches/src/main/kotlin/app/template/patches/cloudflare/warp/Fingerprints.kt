package app.template.patches.cloudflare.warp

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Targets AccountData.<init>(String, WarpPlusState, Long, Long, LocalDateTime,
 *                             Integer, String, Managed, AccountRole)V
 *
 * WHY THIS CLASS IS STABLE:
 * AccountData is a Moshi-serialised JSON data class for the /accounts API response.
 * Moshi requires all field names to remain intact at runtime (via @Json annotations
 * on the constructor parameters), so R8 cannot rename AccountData or its constructor
 * parameters. The class and its full 9-parameter primary constructor signature are
 * permanently stable across app updates.
 *
 * WHAT IT DOES:
 * The constructor assigns p2 (WarpPlusState — the "account_type" JSON field) into
 * the final field `b`. By injecting a sget-object before index 0, we overwrite p2
 * with WarpPlusState.UNLIMITED before the iput-object fires, so the final field is
 * always written as UNLIMITED regardless of what the server returned.
 *
 * WHY NOT THE GETTER (WarpDataStore.getWarpAccount):
 * AccountData.b is a `final` Kotlin val. The Dalvik/ART verifier enforces that final
 * instance fields may ONLY be written inside the declaring class's own <init> method.
 * Writing it from any other method (e.g. a getter override) causes a hard VerifyError
 * on class load, crashing the app at launch before any activity is displayed.
 *
 * VERIFIED v6.38.8 (versionCode 5431):
 *   AccountData.smali line 154:
 *     .method public constructor <init>(String;WarpPlusState;Long;Long;LocalDateTime;
 *                                       Integer;String;Managed;AccountRole;)V
 *     .registers 11
 *     ...
 *     iput-object p2, p0, AccountData;->b:WarpPlusState;   ← injection point
 *
 *   WarpPlusState enum values: FREE, LIMITED, TEAM, UNLIMITED (stable, Moshi-kept).
 */
object AccountDataConstructorFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    returnType = "V",
    parameters = listOf(
        "Ljava/lang/String;",
        "Lcom/cloudflare/app/data/warpapi/WarpPlusState;",
        "Ljava/lang/Long;",
        "Ljava/lang/Long;",
        "Lorg/threeten/bp/LocalDateTime;",
        "Ljava/lang/Integer;",
        "Ljava/lang/String;",
        "Lcom/cloudflare/app/data/warpapi/Managed;",
        "Lcom/cloudflare/app/data/warpapi/AccountRole;",
    ),
    custom = { _, classDef ->
        classDef.type == "Lcom/cloudflare/app/data/warpapi/AccountData;"
    },
)

/**
 * Targets AnalyticsService.c(AnalyticsService, Bundle)V
 * — the Kotlin-generated static dispatcher for the logAnalyticsEvent() extension function.
 *
 * WHY THIS TARGET:
 * Every Cloudflare-specific telemetry call site (tunnel_mode, encryption, feature,
 * connection_type, ip_version events) routes through this static companion method.
 * The method enriches the event Bundle with network metadata (MNc, MCC, connectivity
 * type) before dispatching it downstream. No-oping it at position 0:
 *   - Prevents bundle enrichment (no-op is safe — returns void with no side effects)
 *   - Prevents the downstream Firebase dispatch from receiving meaningful data
 *
 * WHY THIS CLASS IS STABLE:
 * AnalyticsService is a Hilt @Singleton injected via constructor injection with 8
 * domain-layer parameters, all of which are Hilt-kept (cannot be renamed). R8 cannot
 * rename the class or its static dispatcher because Hilt generates a factory that
 * references it by name. The (AnalyticsService, Bundle) → V signature is unique
 * within this class.
 *
 * ARCHITECTURE NOTE (v6.38.8 change):
 * In v6.38.7, a second analytics layer existed via FirebaseAnalytics.a(Bundle, String)V.
 * In v6.38.8, this method was removed. The Firebase dispatch now happens via two
 * anonymous Kotlin lambda classes (AnalyticsService$3 and $7) which implement
 * Function1<Bundle, Unit> and call zzdq->n() directly. These lambdas have no stable
 * names and are not fingerprinted here. The DisableAnalyticsPatch handles them via
 * a classDefForEach scan of the AnalyticsService enclosing class.
 *
 * VERIFIED v6.38.8 (versionCode 5431):
 *   AnalyticsService.smali line 1107:
 *     .method public static final c(AnalyticsService;Bundle;)V
 *     .registers 4
 *     iget-object p0, p0, AnalyticsService;->a:Context;
 *     invoke-static {p0}, NetworkInfoHelper;->a(Context;)NetworkDetails;
 *     ... [~300 lines building bundle data] ...
 *     return-void
 */
object AnalyticsServiceDispatchFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(
        "Lcom/cloudflare/app/domain/analytics/AnalyticsService;",
        "Landroid/os/Bundle;",
    ),
    custom = { _, classDef ->
        classDef.type == "Lcom/cloudflare/app/domain/analytics/AnalyticsService;"
    },
)
