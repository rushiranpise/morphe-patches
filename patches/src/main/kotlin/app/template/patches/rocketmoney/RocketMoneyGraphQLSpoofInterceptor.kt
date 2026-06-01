package app.template.patches.rocketmoney

/**
 * Companion class injected into the Rocket Money APK at runtime.
 *
 * This class is referenced by name in the patched OkHttp interceptor chain.
 * It intercepts GraphQL API responses and rewrites the user subscription
 * payload to always report premium status as active.
 *
 * Placement: injected as a new class into the app's DEX via the Morphe
 * patcher class injection mechanism — NOT compiled into patches.dex directly.
 * The patcher copies the compiled .class bytes from the patch bundle into the
 * target APK's DEX before signing.
 *
 * ## Why GraphQL spoofing is needed
 *
 * Rocket Money (com.rocketmoney.app) is a React Native / Expo app backed by
 * a GraphQL API. Premium status is determined server-side and returned in the
 * `currentUser { isPremium, premiumTier, subscriptionStatus }` fragment on
 * every authenticated query. The JS bundle reads this field directly from the
 * Apollo in-memory cache — so simply patching a JVM class is insufficient.
 *
 * Strategy:
 *   1. Intercept every OkHttp response whose URL contains "/graphql".
 *   2. Parse the response body as a JSON string.
 *   3. Walk `data.currentUser` (if present) and force:
 *        isPremium          = true
 *        premiumTier        = "PREMIUM"
 *        subscriptionStatus = "ACTIVE"
 *   4. Return a new Response with the rewritten body.
 *
 * This is a pure Java/Kotlin implementation with no external dependencies
 * beyond the OkHttp classes already present in the APK.
 */
class RocketMoneyGraphQLSpoofInterceptor : okhttp3.Interceptor {

    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Only intercept GraphQL endpoints
        val url = request.url.toString()
        if (!"/graphql" in url && !"graphql" in url.substringAfterLast("/")) {
            return response
        }

        val contentType = response.body?.contentType()
        val bodyString = response.body?.string() ?: return response

        val rewritten = rewriteUserPayload(bodyString)

        val newBody = okhttp3.ResponseBody.create(contentType, rewritten)
        return response.newBuilder().body(newBody).build()
    }

    /**
     * Rewrites `data.currentUser` fields in the JSON payload.
     *
     * Uses simple string replacement to avoid pulling in a JSON library;
     * the app already has Gson/Moshi on the classpath but referencing them
     * from injected code is fragile across APK versions.
     *
     * Targets (case-sensitive, as returned by the Rocket Money GraphQL API):
     *   "isPremium":false   -> "isPremium":true
     *   "premiumTier":null  -> "premiumTier":"PREMIUM"
     *   "premiumTier":".*" (non-PREMIUM values)
     *   "subscriptionStatus":"INACTIVE" -> "subscriptionStatus":"ACTIVE"
     *   "subscriptionStatus":"CANCELED" -> "subscriptionStatus":"ACTIVE"
     *   "subscriptionStatus":"TRIALING" -> "subscriptionStatus":"ACTIVE"
     *   "subscriptionStatus":null       -> "subscriptionStatus":"ACTIVE"
     */
    private fun rewriteUserPayload(json: String): String {
        // Only touch responses that carry currentUser data
        if ("currentUser" !in json) return json

        return json
            .replace("\"isPremium\":false", "\"isPremium\":true")
            .replace("\"isPremium\": false", "\"isPremium\": true")
            .replace("\"premiumTier\":null", "\"premiumTier\":\"PREMIUM\"")
            .replace("\"premiumTier\": null", "\"premiumTier\": \"PREMIUM\"")
            .replace("\"subscriptionStatus\":\"INACTIVE\"", "\"subscriptionStatus\":\"ACTIVE\"")
            .replace("\"subscriptionStatus\": \"INACTIVE\"", "\"subscriptionStatus\": \"ACTIVE\"")
            .replace("\"subscriptionStatus\":\"CANCELED\"", "\"subscriptionStatus\":\"ACTIVE\"")
            .replace("\"subscriptionStatus\": \"CANCELED\"", "\"subscriptionStatus\": \"ACTIVE\"")
            .replace("\"subscriptionStatus\":\"TRIALING\"", "\"subscriptionStatus\":\"ACTIVE\"")
            .replace("\"subscriptionStatus\": \"TRIALING\"", "\"subscriptionStatus\": \"ACTIVE\"")
            .replace("\"subscriptionStatus\":null", "\"subscriptionStatus\":\"ACTIVE\"")
            .replace("\"subscriptionStatus\": null", "\"subscriptionStatus\": \"ACTIVE\"")
    }

    companion object {
        const val CLASS_DESCRIPTOR =
            "Lapp/template/patches/rocketmoney/RocketMoneyGraphQLSpoofInterceptor;"
    }
}
