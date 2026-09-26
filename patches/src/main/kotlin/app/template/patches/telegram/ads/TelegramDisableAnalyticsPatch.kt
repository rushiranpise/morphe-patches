package app.template.patches.telegram.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY

/**
 * Plus Messenger 12.10.3.0-specific analytics fix.
 *
 * The previous implementation depended on three fingerprints that no longer
 * match this Plus build. DEX inspection of Plus 12.10.3.0 identified the
 * corresponding analytics controller as Lyj/c:
 *
 *   c(Application)             -> FirebaseAnalytics.getInstance(...).b(Z)
 *   a(String)                  -> FirebaseAnalytics.a(String, Bundle)
 *   b(HashMap, String)         -> FirebaseAnalytics.a(String, Bundle)
 *
 * We patch those exact methods directly by descriptor instead of relying on
 * the stale fingerprints. FirebaseApp.initializeApp() is not modified, so
 * Firebase initialization/FCM remains intact.
 *
 * Target APK verified:
 *   Package: org.telegram.plus
 *   Version: 12.10.3.0
 *   APK SHA-256: 53d07bee34c0419cd01c026e444f2104ba6d8e739254f8a6f0268d92401d4a21
 */
@Suppress("unused")
val telegramPlusDisableAnalyticsPatch = bytecodePatch(
    name = "Disable analytics",
    description = "Blocks Firebase analytics and event tracking in Telegram Plus while preserving Firebase initialization for push notifications.",
) {
    compatibleWith(TELEGRAM_PLUS_COMPATIBILITY)

    execute {
        val analyticsClass = mutableClassDefBy(
            classDefBy("Lyj/c;")
        )

        fun findMethod(
            name: String,
            parameters: List<String>,
        ) = analyticsClass.methods.firstOrNull {
            it.name == name &&
                it.returnType == "V" &&
                it.parameterTypes == parameters
        } ?: throw IllegalStateException(
            "Telegram Plus analytics method not found: Lyj/c->$name(${parameters.joinToString("")})V"
        )

        // AnalyticsEnableFingerprint equivalent:
        // Lyj/c->c(Application) -> FirebaseAnalytics.getInstance(...).b(Z)
        findMethod(
            "c",
            listOf("Landroid/app/Application;"),
        ).addInstructions(0, "return-void")

        // AnalyticsTrackEventFingerprint equivalent:
        // Lyj/c->a(String) -> FirebaseAnalytics.a(String, Bundle)
        findMethod(
            "a",
            listOf("Ljava/lang/String;"),
        ).addInstructions(0, "return-void")

        // AnalyticsTrackEventMapFingerprint equivalent:
        // Lyj/c->b(HashMap, String) -> FirebaseAnalytics.a(String, Bundle)
        findMethod(
            "b",
            listOf(
                "Ljava/util/HashMap;",
                "Ljava/lang/String;",
            ),
        ).addInstructions(0, "return-void")
    }
}