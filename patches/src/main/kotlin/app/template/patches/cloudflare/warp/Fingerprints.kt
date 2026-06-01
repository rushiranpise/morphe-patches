package app.template.patches.cloudflare.warp

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Targets: AccountData.<init>(String, WarpPlusState, Long, Long, LocalDateTime,
 *                              Integer, String, Managed, AccountRole)V
 *
 * AccountData is kept by Moshi JSON serialisation — R8 cannot rename it.
 * The 9-parameter constructor is the sole non-synthetic primary constructor;
 * its full parameter list uniquely identifies it across the entire APK.
 *
 * p2 (the second constructor parameter) is WarpPlusState — the `accountType`
 * JSON field. It is written into final field `b` at line 208 of AccountData.smali:
 *   iput-object p2, p0, Lcom/cloudflare/app/data/warpapi/AccountData;->b:Lcom/cloudflare/app/data/warpapi/WarpPlusState;
 *
 * By injecting at offset 0 inside this constructor (before the iput-object),
 * we overwrite p2 with WarpPlusState.UNLIMITED so the final field is always
 * written as UNLIMITED — no Dex VerifyError, no crash.
 *
 * WarpPlusState ordinals (from WhenMappings.smali + AccountData.smali clInit):
 *   FREE=0, LIMITED=1, UNLIMITED=2, TEAM=3
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
        "Lcom/cloudflare/app/data/warpapi/AccountRole;"
    ),
    custom = { _, classDef ->
        classDef.type == "Lcom/cloudflare/app/data/warpapi/AccountData;"
    }
)

/**
 * Targets: AnalyticsService.<init> — the 8-parameter constructor.
 *
 * NOTE: This fingerprint is intentionally NOT used in DisableAnalyticsPatch.
 * Injecting return-void into a constructor before super.<init>() causes a
 * Dex VerifyError at runtime. Retained here for reference / future use.
 *
 * The constructor is identified by:
 *   - accessFlags PUBLIC + CONSTRUCTOR (both flags required — Dex sets
 *     ACC_CONSTRUCTOR 0x10000 on every <init>, the patcher matches all flags)
 *   - returnType V
 *   - full 8-parameter signature of Hilt-kept domain classes
 *
 * AnalyticsService is a Hilt @Singleton — R8 cannot rename it.
 */
object AnalyticsServiceConstructorFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Lcom/cloudflare/app/vpnservice/VpnServiceMediator;",
        "Lcom/cloudflare/app/vpnservice/tunnel/provider/TunnelTypeStore;",
        "Lcom/cloudflare/app/domain/resolver/ResolverOptionStore;",
        "Lcom/cloudflare/app/vpnservice/detectors/Dns64NetworkDetector;",
        "Lcom/cloudflare/app/vpnservice/detectors/CaptivePortalDetector;",
        "Lcom/cloudflare/app/vpnservice/servicepause/ServicePauseDataStore;",
        "Lcom/cloudflare/app/vpnservice/detectors/IpVersionDetector;"
    ),
    custom = { _, classDef ->
        classDef.type == "Lcom/cloudflare/app/domain/analytics/AnalyticsService;"
    }
)

/**
 * Targets: AnalyticsService.c(AnalyticsService, Bundle)V
 *
 * This is the Kotlin-compiler-generated static dispatcher for the
 * logAnalyticsEvent() extension function on AnalyticsService.
 * Every Cloudflare telemetry call site invokes this static method;
 * no-oping it at position 0 silences all Cloudflare-specific analytics
 * without touching the constructor or the Firebase SDK internals.
 *
 * Signature is stable: AnalyticsService is a kept class (Hilt), and
 * Bundle is an Android framework class — neither can be renamed by R8.
 * The (AnalyticsService, Bundle) → V combination is unique in this class.
 */
object AnalyticsServiceDispatchFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(
        "Lcom/cloudflare/app/domain/analytics/AnalyticsService;",
        "Landroid/os/Bundle;"
    ),
    custom = { _, classDef ->
        classDef.type == "Lcom/cloudflare/app/domain/analytics/AnalyticsService;"
    }
)

/**
 * Targets: FirebaseAnalytics.a(Bundle, String)V — the obfuscated logEvent() method.
 *
 * In the decompiled Smali this method is named `a` and delegates to:
 *   zzdn->n(String, String, Bundle, Z)V
 * which is the actual Scion/Measurement SDK dispatch.
 *
 * Why we can use the class name: FirebaseAnalytics is always kept by the
 * Firebase SDK's own consumer ProGuard rules — it is never obfuscated.
 *
 * Why we target zzdn->n: it is the sole call in this method body and its
 * definingClass + method name pair is unique inside FirebaseAnalytics,
 * making the filter globally precise.
 *
 * Two-parameter signature (Bundle, String) matches the public logEvent API
 * surface and rules out unrelated single-parameter helpers in the same class.
 */
object FirebaseAnalyticsLogEventFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;", "Ljava/lang/String;"),
    filters = listOf(
        methodCall(
            opcode = Opcode.INVOKE_VIRTUAL,
            definingClass = "Lcom/google/android/gms/internal/measurement/zzdn;",
            name = "n"
        )
    ),
    custom = { _, classDef ->
        classDef.type == "Lcom/google/firebase/analytics/FirebaseAnalytics;"
    }
)