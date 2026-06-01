package app.template.patches.rocketmoney

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * OkHttpClient.Builder.addInterceptor(Interceptor) or addNetworkInterceptor(Interceptor)
 *
 * Rocket Money builds its OkHttpClient in a Dagger/Hilt module that calls
 * addNetworkInterceptor(...) to register each interceptor. We hook the
 * builder's build() finalizer instead so we can inject our spoofing
 * interceptor as the LAST network interceptor (ensuring it fires after auth
 * headers are attached but before the response is cached).
 *
 * Defining class: the Hilt/Dagger-generated NetworkModule that constructs
 * the OkHttpClient used by Apollo GraphQL.
 *
 * Strategy:
 *   - Before the terminal `invoke-virtual {v0}, Lokhttp3/OkHttpClient$Builder;->build()` call,
 *     inject:
 *       new-instance vN, Lapp/template/patches/rocketmoney/RocketMoneyGraphQLSpoofInterceptor;
 *       invoke-direct {vN}, <init>()V
 *       invoke-virtual {v0, vN}, Lokhttp3/OkHttpClient$Builder;->addNetworkInterceptor(Lokhttp3/Interceptor;)Lokhttp3/OkHttpClient$Builder;
 */
val RocketMoneyOkHttpBuilderFingerprint = Fingerprint(
    definingClass = "Lcom/rocketmoney/app/network/NetworkModule;",
    name = "provideOkHttpClient",
    returnType = "Lokhttp3/OkHttpClient;",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

/**
 * Fallback: Apollo GraphQL NetworkTransport builder
 *
 * If the OkHttpClient builder fingerprint misses (obfuscated class name),
 * we target the Apollo HttpNetworkTransport.Builder.build() call site instead.
 * This is less precise but more resilient to renaming.
 */
val RocketMoneyApolloBuilderFingerprint = Fingerprint(
    definingClass = "Lcom/rocketmoney/app/network/ApolloModule;",
    name = "provideApolloClient",
    returnType = "Lcom/apollographql/apollo3/ApolloClient;",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

/**
 * isPremium getter on the local UserSession / CurrentUser domain model.
 *
 * This is the JVM-side gate used by the React Native bridge to pass initial
 * user state before the JS bundle runs. Patching this ensures that the
 * native Android UI (splash, onboarding gating) also sees a premium user.
 *
 * Strategy: replace body with const/4 v0, 0x1 / return v0
 */
val RocketMoneyIsPremiumFingerprint = Fingerprint(
    definingClass = "Lcom/rocketmoney/app/domain/model/UserSession;",
    name = "isPremium",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * subscriptionStatus getter on the UserSession domain model.
 *
 * Returns an enum/string checked by native gating code.
 * Strategy: replace return value with the ACTIVE constant.
 */
val RocketMoneySubscriptionStatusFingerprint = Fingerprint(
    definingClass = "Lcom/rocketmoney/app/domain/model/UserSession;",
    name = "getSubscriptionStatus",
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * Premium feature gate — used by native Android paywall dialogs.
 *
 * Some screens (budgeting, subscription tracker) show a native Android
 * paywall dialog rather than a React Native one. This gate method is called
 * before navigating to those screens.
 *
 * Strategy: replace body with const/4 v0, 0x1 / return v0
 */
val RocketMoneyPremiumGateFingerprint = Fingerprint(
    definingClass = "Lcom/rocketmoney/app/ui/premium/PremiumGateHelper;",
    name = "isPremiumUser",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
