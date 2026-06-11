package app.template.patches.netmonster

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * SubscriptionRepository$4$1 (er/o$a$a) — Kotlin Flow transformer that maps
 * List<AdaptyProfile.Subscription> → Boolean (isPremium).
 * Emits true/false to er/o.f MutableStateFlow consumed by MonitorVM (er/o.k).
 * Replacing the body always emits true → premium features unlocked in MonitorVM.
 */
val SubscriptionActiveFlowFingerprint = Fingerprint(
    definingClass = "Ler/o\$a\$a;",
    name = "a",
    parameters = listOf("Ljava/util/List;", "Lxt/f;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * SubscriptionRepository.n() (er/o) — recomputes SIM permission state.
 * Prepending return-void skips the early FALSE emission to er/o.g.
 */
val SubscriptionRepositoryPermissionGateFingerprint = Fingerprint(
    definingClass = "Ler/o;",
    name = "n",
    returnType = "V",
    parameters = emptyList(),
    strings = listOf(
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.READ_PHONE_STATE"
    )
)

/**
 * MoreVM$1$1 (uq/l$a$a) — Flow collector that receives the max subscription
 * expiry OffsetDateTime from er/o$g$a and writes it into uq/l.Z
 * (MutableStateFlow<uq/k>).  MoreFragment reads this and shows
 * "Inactive" when uq/k.a == null.
 *
 * Injecting at the top: if p1 (OffsetDateTime) is null, replace it with
 * OffsetDateTime.now(ZoneOffset.UTC).plusYears(50) before the original
 * CAS write, so the UI always shows an active expiry date.
 *
 * Fingerprinted by:
 *  - definingClass "Luq/l$a$a;" + name "a"
 *  - parameters (OffsetDateTime, Continuation)
 */
val SubscriptionExpiryEmitFingerprint = Fingerprint(
    definingClass = "Luq/l\$a\$a;",
    name = "a",
    parameters = listOf("Ljava/time/OffsetDateTime;", "Lxt/f;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * App.onCreate() — NetMonster's Application class.
 * We inject NetMonsterHelper.init() here to install the PackageManager
 * proxy before Google Maps SDK reads the cert/API key.
 */
val NetMonsterAppOnCreateFingerprint = Fingerprint(
    definingClass = "Lcz/mroczis/netmonster/application/App;",
    name = "onCreate",
    returnType = "V"
)
