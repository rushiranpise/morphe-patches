package app.template.extension.extension;

/**
 * AmazonHelper — runtime injection helper for Amazon Shopping patches.
 *
 * Ad-blocking selectors and dark-mode CSS/JS are inspired by and partially
 * derived from amznkiller by hxreborn:
 *   https://github.com/hxreborn/amznkiller
 *   License: GPL-3.0
 *
 * Selector list fetched at runtime from:
 *   https://raw.githubusercontent.com/hxreborn/amznkiller/main/lists/generated/merged.txt
 *
 * dark_mode.js fetched at runtime from:
 *   https://raw.githubusercontent.com/hxreborn/amznkiller/main/app/src/main/resources/payload/js/dark_mode.js
 */

import android.webkit.WebView;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AmazonHelper {

    // ── Ad selectors ─────────────────────────────────────────────────────────

    private static final String SELECTOR_URL =
        "https://raw.githubusercontent.com/hxreborn/amznkiller/main/lists/generated/merged.txt";

    /** Embedded fallback — mirrors amznkiller lists/static.txt (GPL-3.0, hxreborn) */
    private static final String[] EMBEDDED_SELECTORS = {
        ".a-cardui[class*=\"_new-detail-faceout-box_\"]:has(.dynamicSponsoredLabelClass)",
        ".a-carousel-card:has(.p13n-sc-sponsored-label)",
        ".amzn-safe-frame-container", ".ape-wrapper",
        ".dp-widget-card-deck:has([data-ad-placement-metadata])",
        ".s-result-item.AdHolder",
        ".s-result-item:has([data-ad-feedback])",
        ".s-result-item:has([data-ad-feedback-label-id])",
        ".s-result-item:has(.puis-sponsored-label-text)",
        ".s-result-item:has(div[cel_widget_id^=\"MAIN-FEATURED_ASINS_LIST-\"])",
        ".s-result-list > .a-section:has(.sbv-ad-content-container)",
        ".p13n-sc-unified-ad-label", ".sbv-video-single-product",
        ".sponsored-products-detail-mobile",
        "#detailILM_feature_div",
        "#faceoutContainer:has(> [class$=\"sponsored-label\"])",
        "#mobile-mshop-ad", "#nav-swmslot", "#sc-rec-bottom", "#sc-rec-right",
        "#similarities_feature_div:has(span.sponsored_label_tap_space)",
        "#sponsoredProducts2_feature_div", "#sponsoredProducts_feature_div",
        "#sponsored-proofPoint-section",
        "#typ-recommendations-stripe-1", "#typ-recommendations-stripe-2",
        "#widget-rightUpsellSlot", ".AdsCard_container__K6vmo",
        "[cel_widget_id*=\"-creative-desktop_loom-desktop-\"]",
        "[class*=\"_adFeedbackWrapper_\"]",
        "[data-ad-id]", "[data-component-type^=\"aspa-asin-ajax\"]",
        "[id^=\"ape_\"]", "[id^=\"mobile-dp-ilm\"]",
        "div.s-inner-result-item > div.sg-col-inner:has(a.puis-sponsored-label-text)",
        "div[cel_widget_id*=\"_ad-placements-\"]", "div[cel_widget_id*=\"Deals3Ads\"]",
        "div[cel_widget_id*=\"desktop-dp-\"]",
        "div[cel_widget_id=\"sp-orderdetails-desktop-carousel_desktop-yo-orderdetails_0\"]",
        "div[cel_widget_id=\"sp-orderdetails-mobile-list_mobile-yo-orderdetails_0\"]",
        "div[cel_widget_id=\"sp-pop-mobile-carousel_mobile-yo-postdelivery_0\"]",
        "div[cel_widget_id=\"sp-rhf-desktop-carousel_desktop-rhf_0\"]",
        "div[cel_widget_id=\"sp-shiptrack-desktop-carousel_desktop-yo-shiptrack_0\"]",
        "div[cel_widget_id=\"sp-shiptrack-mobile-list_mobile-yo-shiptrack_0\"]",
        "div[cel_widget_id=\"sp-typ-mobile-carousel_mobile-typ-carousels_2\"]",
        "div[cel_widget_id=\"sp_phone_detail_thematic\"]", "div[cel_widget_id=\"typ-ads\"]",
        "div[cel_widget_id^=\"LEFT-SAFE_FRAME-\"]",
        "div[cel_widget_id^=\"MAIN-FEATURED_ASINS_LIST-\"]",
        "div[cel_widget_id^=\"MAIN-VIDEO_SINGLE_PRODUCT-\"]",
        "div[cel_widget_id^=\"adplacements:\"]",
        "div[cel_widget_id^=\"multi-brand-\"]", "div[cel_widget_id^=\"sb-\"]",
        "div[cel_widget_id^=\"sb-themed-collection\"]",
        "div[cel_widget_id^=\"sp-desktop-carousel_handsfree-browse\"]",
        "div[cel_widget_id^=\"sp-mobile-thematic-bundle\"]",
        "div[class*=\"SponsoredProducts\"]", "div[class*=\"_dpNoOverflow_\"][data-idt]",
        "div[data-a-carousel-options*='\"isSponsoredProduct\":\"true\"']",
        "div[data-cel-widget*=\"-mobile_loom-mobile-inline-slot\"]",
        "div[data-cel-widget=\"sp-rhf-desktop-carousel_desktop-rhf_1\"]",
        "div[data-cel-widget=\"sp-shiptrack-desktop-carousel_desktop-yo-shiptrack_0\"]",
        "div[data-cel-widget^=\"mobile-ads-\"]",
        "div[data-cel-widget^=\"multi-brand-video-mobile_DPSims_\"]",
        "div[data-cel-widget^=\"multi-brand-video-mobile_DetailPage_\"]",
        "div[data-cel-widget^=\"multi-card-creative-desktop_loom-desktop-top-slot_\"]",
        "div[data-cel-widget^=\"sb-\"]", "div[data-cel-widget^=\"sp-mobile-thematic-bundle\"]",
        "div[data-csa-c-painter=\"single-creative-card\"]:has([data-ad-feedback-label-id])",
        "div[data-csa-c-painter=\"single-video-card\"]:has([data-ad-feedback-label-id])",
        "div[data-csa-c-painter=\"sp-cart-mobile-carousel-cards\"]",
        "div[data-csa-c-slot-id^=\"loom-mobile-brand-footer-slot_hsa-id-\"]",
        "div[data-csa-c-slot-id^=\"loom-mobile-top-slot_hsa-id-\"]",
        "li.gwm-window-tile:has(div[data-csa-c-painter=\"single-creative-card\"] [data-ad-feedback-label-id])",
        "li.gwm-window-tile:has(div[data-csa-c-painter=\"single-video-card\"] [data-ad-feedback-label-id])",
        "div[id^=\"sims-simsContainer_feature_div_\"]:has(.sp_info_link)",
        "div[id^=\"sp_detail\"]",
        "span[cel_widget_id^=\"MAIN-FEATURED_ASINS_LIST-\"]",
        "span[cel_widget_id^=\"MAIN-loom-desktop-brand-footer-slot_hsa-id-CARDS-\"]",
        "span[cel_widget_id^=\"MAIN-loom-desktop-top-slot_hsa-id-CARDS-\"]",
        // Frequently bought together
        "#sims-fbt-content",
        "#sims-fbt",
        "div[cel_widget_id*=\"fbt\"]",
        "div[data-cel-widget*=\"fbt\"]",
        "[cel_widget_id^=\"MAIN-sims-fbt\"]",
    };

    private static volatile String cachedAdCss = null;
    private static volatile long lastAdFetch = 0;
    private static final long CACHE_TTL_MS = 6 * 60 * 60 * 1000L;

    private static String buildCss(String[] selectors) {
        StringBuilder sb = new StringBuilder();
        for (String s : selectors) sb.append(s.trim()).append("{display:none!important}");
        return sb.toString();
    }

    private static String fetchText(String urlStr) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(urlStr).openConnection();
            c.setConnectTimeout(5000); c.setReadTimeout(8000);
            c.setRequestProperty("User-Agent", "AmazonHelper/1.0");
            if (c.getResponseCode() != 200) return null;
            BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append("\n");
            r.close(); c.disconnect();
            return sb.toString();
        } catch (Exception e) { return null; }
    }

    private static String getAdCss() {
        long now = System.currentTimeMillis();
        if (cachedAdCss != null && (now - lastAdFetch) < CACHE_TTL_MS) return cachedAdCss;
        String raw = fetchText(SELECTOR_URL);
        if (raw != null) {
            List<String> lines = new ArrayList<>();
            for (String l : raw.split("\n")) {
                l = l.trim();
                if (!l.isEmpty() && !l.startsWith("!") && !l.startsWith("//")) lines.add(l);
            }
            if (!lines.isEmpty()) { cachedAdCss = buildCss(lines.toArray(new String[0])); lastAdFetch = now; }
        }
        if (cachedAdCss == null) { cachedAdCss = buildCss(EMBEDDED_SELECTORS); lastAdFetch = now; }
        return cachedAdCss;
    }

    public static void injectAdBlock(final WebView webView) {
        if (webView == null) return;
        new Thread(new Runnable() {
            @Override public void run() {
                final String css = getAdCss();
                webView.post(new Runnable() {
                    @Override public void run() {
                        String escaped = css.replace("\\", "\\\\").replace("'", "\\'");
                        webView.evaluateJavascript(
                            "(function(){var id='amznkiller-ads';var s=document.getElementById(id);"
                            + "if(!s){s=document.createElement('style');s.id=id;"
                            + "(document.head||document.documentElement).appendChild(s);}"
                            + "s.textContent='" + escaped + "';})();", null);
                    }
                });
            }
        }).start();
    }

    // ── Price charts ─────────────────────────────────────────────────────────

    // Marketplace country codes each provider actually tracks. The code itself
    // is derived from the Amazon domain at runtime (see countryCode), so no
    // per-store lookup tables need to be maintained.
    // graph.keepa.com accepts these codes directly as the `domain` parameter
    // (verified: us, uk, de, fr, jp, ca, it, es, in, mx, br, nl) but serves no
    // data for Amazon Australia — every com.au ASIN returns the same static
    // "no price history available" image — so `au` is excluded here.
    private static final Set<String> KEEPA_CODES = new HashSet<>(Arrays.asList(
        "us", "uk", "de", "fr", "jp", "ca", "it", "es", "in", "mx", "br", "nl"));
    // keepa.com product pages need Keepa's numeric marketplace id in the URL
    // (#!product/{id}-{asin}) — only the chart endpoint accepts country codes.
    // Verified against graph.keepa.com: domain=it == domain=8, nl == 14, etc.
    private static final Map<String, Integer> KEEPA_IDS = new HashMap<>();
    static {
        KEEPA_IDS.put("us", 1);  KEEPA_IDS.put("uk", 2);  KEEPA_IDS.put("de", 3);
        KEEPA_IDS.put("fr", 4);  KEEPA_IDS.put("jp", 5);  KEEPA_IDS.put("ca", 6);
        KEEPA_IDS.put("it", 8);  KEEPA_IDS.put("es", 9);  KEEPA_IDS.put("in", 10);
        KEEPA_IDS.put("mx", 11); KEEPA_IDS.put("br", 12); KEEPA_IDS.put("nl", 14);
    }
    // CCC tracks fewer marketplaces; its link host is {code}.camelcamelcamel.com
    // except the US which has no subdomain.
    private static final Set<String> CCC_CODES = new HashSet<>(Arrays.asList(
        "us", "uk", "de", "fr", "jp", "ca", "it", "es", "au"));

    /** Derives the marketplace country code from an Amazon domain:
     *  amazon.com → us, amazon.co.uk → uk, amazon.co.jp → jp, amazon.com.au → au,
     *  everything else → last TLD label (amazon.in → in, amazon.de → de, …). */
    private static String countryCode(String domain) {
        if (domain.equals("amazon.com")) return "us";
        if (domain.endsWith(".co.uk"))  return "uk";
        if (domain.endsWith(".co.jp"))  return "jp";
        if (domain.endsWith(".com.au")) return "au";
        int dot = domain.lastIndexOf('.');
        return dot >= 0 ? domain.substring(dot + 1) : null;
    }

    /** Returns the CamelCamelCamel base URL for a given locale code.
     *  US has no subdomain; all others use {locale}.camelcamelcamel.com. */
    private static String camelHost(String locale) {
        if ("us".equals(locale)) return "camelcamelcamel.com";
        return locale + ".camelcamelcamel.com";
    }

    public static void injectPriceCharts(WebView webView, String url, String period, boolean showToggle) {
        if (webView == null || url == null) return;
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("/(?:dp|gp/product|gp/aw/d)/([A-Z0-9]{10})", java.util.regex.Pattern.CASE_INSENSITIVE)
            .matcher(url);
        if (!m.find()) return;
        final String asin = m.group(1);
        // [a-z.]* (not +) — with + the regex backtracks and "www." ends up in the
        // captured domain, so the Keepa/CCC map lookups miss and no charts show.
        java.util.regex.Matcher dm = java.util.regex.Pattern
            .compile("https?://(?:www\\.)?([a-z.]*amazon[a-z.]+)").matcher(url);
        String domain = dm.find() ? dm.group(1) : "amazon.com";
        // The marketplace country code is derived from the domain — no tables.
        final String cc = countryCode(domain);
        // Chart URLs are built in JS so the same script can re-inject when Amazon
        // navigates between products without a full page reload (SPA navigation).
        // graph.keepa.com sends Access-Control-Allow-Origin so the placeholder gate
        // can inspect the image size; charts.camelcamelcamel.com does not, so CCC
        // relies on the img onerror fallback instead of fetch().
        final String keepaJs = (cc != null && KEEPA_CODES.contains(cc))
            ? "var keepaUrl='https://graph.keepa.com/pricehistory.png?used=1&amazon=1&new=1&domain=" + cc + "&asin=';"
              + "var keepaLink='https://keepa.com/#!product/" + KEEPA_IDS.get(cc) + "-';"
            : "var keepaUrl=null;var keepaLink=null;";
        final String camel = cc != null && CCC_CODES.contains(cc) ? cc : null;
        final String camelJs = (camel != null)
            ? "var camelBase='https://charts.camelcamelcamel.com/" + camel + "/';"
              + "var camelLink='https://" + camelHost(camel) + "/product/';"
            : "var camelBase=null;var camelLink=null;";
        // CCC honors `tp` for the time window and `w` for the image width
        // (a smaller width zooms the chart in). The default period comes from
        // the patch option; anything unexpected falls back to 1y.
        final String defPeriod = "3y".equals(period) || "all".equals(period) ? period : "1y";
        String js = "(function(){var curAsin='" + asin + "';"
            + "var defPeriod='" + defPeriod + "';"
            + "var showToggle=" + showToggle + ";"
            + "var periods=[{l:'1Y',v:'1y'},{l:'3Y',v:'3y'},{l:'ALL',v:'all'}];"
            + keepaJs
            + camelJs
            + "function inject(asin){"
            + "var e=document.getElementById('amznkiller-charts');"
            + "if(e&&e.getAttribute('data-asin')===asin)return;if(e)e.remove();"
            + "var c=document.createElement('div');c.id='amznkiller-charts';"
            + "c.setAttribute('data-asin',asin);"
            + "c.style.cssText='margin:16px 0;padding:12px;border:1px solid #ddd;border-radius:8px;background:#fafafa';"
            + "var t=document.createElement('div');"
            + "t.style.cssText='font-weight:bold;font-size:14px;margin-bottom:8px;color:#333';"
            + "t.textContent='Price History';c.appendChild(t);"
            + "function toLink(a,lbl){if(!a.querySelector('img'))return;"
            + "a.textContent='Open price history on '+lbl;"
            + "a.style.cssText='font-size:13px;color:#0066c0;text-decoration:underline';}"
            + "function add(src,lbl,href,check,pBase){var w=document.createElement('div');"
            + "w.style.marginBottom='8px';"
            + "var l=document.createElement('div');"
            + "l.style.cssText='font-size:12px;color:#666;margin-bottom:4px';l.textContent=lbl;w.appendChild(l);"
            + "var a=document.createElement('a');a.href=href;a.target='_blank';a.rel='noopener';"
            + "var i=document.createElement('img');i.src=src;"
            + "i.style.cssText='width:100%;height:auto;border-radius:4px';"
            + "a.appendChild(i);w.appendChild(a);c.appendChild(w);"
            + "i.onerror=function(){toLink(a,lbl);};"
            // Placeholder gate: the chart CDNs return a small stub for unknown
            // ASINs or when the service is degraded; real charts are much larger.
            // Only used where CORS lets fetch() read the response (Keepa).
            + "if(check)fetch(src).then(function(r){return r.ok?r.blob():null;})"
            + ".then(function(b){if(!b||b.size<15000)toLink(a,lbl);})"
            + ".catch(function(){});"
            // Period toggle: swaps the chart image between views without reload.
            // `&t=` cache-buster forces a fresh render so the new period actually shows.
            + "if(pBase&&showToggle){"
            + "var row=document.createElement('div');"
            + "row.style.cssText='margin-top:4px;';"
            + "periods.forEach(function(p){"
            + "var b=document.createElement('button');"
            + "b.type='button';b.textContent=p.l;"
            + "b.style.cssText='margin-right:6px;padding:2px 8px;border:1px solid #ccc;border-radius:3px;background:#fff;font-size:11px;color:#333;cursor:pointer';"
            + "b.onclick=function(){var img=a.querySelector('img');"
            + "if(!img)return;"
            + "img.src=pBase+p.v+'&t='+Date.now();"
            + "img.onerror=function(){toLink(a,lbl);};};"
            + "row.appendChild(b);});"
            + "w.appendChild(row);}"
            + "}"
            + "var camelChart=camelBase?camelBase+asin+'/amazon-new-used.png?force=1&legend=1&w=625&h=400&tp=':null;"
            + "if(keepaUrl)add(keepaUrl+asin,'Keepa',keepaLink+asin,true,null);"
            + "if(camelChart)add(camelChart+defPeriod+'&t='+Date.now(),'CamelCamelCamel',camelLink+asin,false,camelChart);"
            // Place the charts right under the price block. Amazon uses different
            // ids per page type, so try the known containers, retry briefly while
            // the page lazy-loads, and fall back to the end of the page.
            + "var targets=['#buyBoxAccordion','#corePriceDisplay_desktop_feature_div',"
            + "'#corePrice_feature_div','#unifiedPrice_feature_div',"
            + "'#mobileapp_buybox_feature_div','#desktop_buybox','#buybox',"
            + "'#price_feature_div','#newAccordionRow','#productOverview_feature_div',"
            + "'#centerCol','#mobileapp_accordion_feature_div','#apex_mobile',"
            + "'#apex_desktop','[id^=corePrice]'];"
            + "function place(){for(var i=0;i<targets.length;i++){"
            + "var el=document.querySelector(targets[i]);"
            + "if(el&&el.parentNode){el.parentNode.insertBefore(c,el.nextSibling);return true;}}"
            + "return false;}"
            + "if(!place()){"
            + "var fb=document.getElementById('dp')||document.getElementById('ppd');"
            + "if(fb)fb.appendChild(c);"
            + "else{var obs=new MutationObserver(function(_,o){if(place())o.disconnect();});"
            + "obs.observe(document.body||document.documentElement,{childList:true,subtree:true});"
            + "setTimeout(function(){obs.disconnect();if(!c.parentNode)document.body.appendChild(c);},10000);}"
            + "}"
            + "}"
            // Inject for the current product page, then keep charts in sync when
            // Amazon's in-app WebView navigates between products (SPA navigation).
            + "var m0=window.location.href.match(/\\/(?:dp|gp\\/product|gp\\/aw\\/d)\\/([A-Z0-9]{10})/i);"
            + "if(m0)inject(m0[1]);else if(curAsin)inject(curAsin);"
            + "if(!window.__amznkillerChartsNav){"
            + "window.__amznkillerChartsNav=true;"
            + "var re=/\\/(?:dp|gp\\/product|gp\\/aw\\/d)\\/([A-Z0-9]{10})/i;"
            + "function onNav(){var m=window.location.href.match(re);if(m)inject(m[1]);}"
            + "var ps=history.pushState,rs=history.replaceState;"
            + "history.pushState=function(){ps.apply(this,arguments);setTimeout(onNav,0);};"
            + "history.replaceState=function(){rs.apply(this,arguments);setTimeout(onNav,0);};"
            + "window.addEventListener('popstate',onNav);"
            + "}"
            + "})();";
        webView.evaluateJavascript(js, null);
    }

    // ── Dark mode (inspired by amznkiller dark_mode.js, GPL-3.0, hxreborn) ──

    private static final String DARK_MODE_JS_URL =
        "https://raw.githubusercontent.com/hxreborn/amznkiller/main/app/src/main/resources/payload/js/dark_mode.js";

    private static volatile String cachedDarkJs = null;
    private static volatile long lastDarkFetch = 0;

    /** Embedded fallback — mirrors amznkiller dark_mode.js (GPL-3.0, hxreborn) */
    private static final String EMBEDDED_DARK_FIX_CSS =
        "[class*=image-container] img{mix-blend-mode:normal!important}"
        + "img[style*=\"mix-blend-mode\"]{mix-blend-mode:normal!important}"
        + "[class*=asin-metadata]{mix-blend-mode:normal!important}"
        + "html{background-color:#1a1a1a!important}"
        + "body{background-color:#1a1a1a!important}"
        + "[class*=asin-container],[class*=asin-image-wrapper]{background-color:white!important}"
        + "[class*=badgeMessage]{background-color:transparent!important}"
        + ".a-button-primary,.a-button-oneclick{color-scheme:only light!important}";

    private static String getDarkModeJs() {
        long now = System.currentTimeMillis();
        if (cachedDarkJs != null && (now - lastDarkFetch) < CACHE_TTL_MS) return cachedDarkJs;
        String remote = fetchText(DARK_MODE_JS_URL);
        if (remote != null && remote.length() > 100) {
            cachedDarkJs = remote; lastDarkFetch = now;
        } else if (cachedDarkJs == null) {
            cachedDarkJs = "(function(){if(!window.AmznKiller)window.AmznKiller={};"
                + "if(window.AmznKiller.setDarkMode)return;"
                + "var CSS='" + EMBEDDED_DARK_FIX_CSS.replace("'", "\\'") + "';"
                + "window.AmznKiller.setDarkMode=function(a){"
                + "var e=!!a&&a.enabled;var s=document.getElementById('amznkiller-dark');"
                + "if(e){if(!s){s=document.createElement('style');s.id='amznkiller-dark';"
                + "(document.head||document.documentElement).appendChild(s);}s.textContent=CSS;}"
                + "else{if(s)s.remove();}return null;};})();";
            lastDarkFetch = now;
        }
        return cachedDarkJs;
    }

    private static boolean isDarkEnabled(String mode) {
        if ("on".equals(mode)) return true;
        if ("follow_system".equals(mode)) {
            try {
                android.app.Application app = currentApp();
                if (app == null) return false;
                int uiMode = app.getResources().getConfiguration().uiMode;
                return (uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                    == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            } catch (Exception e) { return false; }
        }
        return false;
    }

    private static android.app.Application currentApp() {
        try {
            return (android.app.Application) Class.forName("android.app.ActivityThread")
                .getMethod("currentApplication").invoke(null);
        } catch (Exception e) { return null; }
    }

    public static void injectDarkMode(final WebView webView, final String mode) {
        if (webView == null || !isDarkEnabled(mode)) return;
        new Thread(new Runnable() {
            @Override public void run() {
                final String js = getDarkModeJs();
                webView.post(new Runnable() {
                    @Override public void run() {
                        webView.evaluateJavascript(js
                            + "\nwindow.AmznKiller.setDarkMode({enabled:true});", null);
                    }
                });
            }
        }).start();
    }

    // ── Video autoplay ────────────────────────────────────────────────────────

    public static void injectDisableVideoAutoplay(WebView webView) {
        if (webView == null) return;
        webView.evaluateJavascript(
            "(function(){var id='amznkiller-novideo';"
            + "if(document.getElementById(id))return;"
            + "var s=document.createElement('style');s.id=id;"
            + "(document.head||document.documentElement).appendChild(s);"
            + "s.textContent='video{pointer-events:auto!important}';"
            + "document.querySelectorAll('video').forEach(function(v){"
            + "v.autoplay=false;v.pause();v.removeAttribute('autoplay');});"
            + "new MutationObserver(function(ms){ms.forEach(function(){"
            + "document.querySelectorAll('video[autoplay]').forEach(function(v){"
            + "v.autoplay=false;v.pause();v.removeAttribute('autoplay');});});})"
            + ".observe(document.body||document.documentElement,"
            + "{childList:true,subtree:true});})();", null);
    }

    // ── Sanitize share URL ────────────────────────────────────────────────────

    /** Strips Amazon tracking params and returns a clean https://amazon.com/dp/ASIN URL */
    public static String sanitizeAmazonUrl(String url) {
        if (url == null || url.isEmpty()) return url;
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(?:https?://[a-z.]*amazon[a-z.]+)?/(?:dp|gp/product|gp/aw/d)/([A-Z0-9]{10})",
                    java.util.regex.Pattern.CASE_INSENSITIVE).matcher(url);
            if (!m.find()) return url;
            String asin = m.group(1);
            java.util.regex.Matcher dm = java.util.regex.Pattern
                .compile("https?://(?:www\\.)?([a-z.]*amazon[a-z.]+)").matcher(url);
            String domain = dm.find() ? dm.group(1) : "amazon.com";
            return "https://www." + domain + "/dp/" + asin;
        } catch (Exception e) { return url; }
    }

    // ── Tab bar icon tint ─────────────────────────────────────────────────────

    /**
     * Called at the top of BaseTabController.getTabIcon()→ImageView.
     * At that point the ImageView hasn't been returned yet — we receive it via
     * the injection site which passes the result register back here after the
     * original method body runs. We apply a white SRC_IN ColorFilter so the
     * icon is visible against a dark background.
     *
     * Injection pattern in DarkModePatch (returnLate style):
     *   invoke-static {v0, vMode}, AmazonHelper->tintTabIconIfDark(ImageView,String)V
     * where v0 = the ImageView returned by the original method.
     */
    public static void tintTabIconIfDark(android.widget.ImageView icon, String mode) {
        if (icon == null || !isDarkEnabled(mode)) return;
        icon.setColorFilter(
            android.graphics.Color.WHITE,
            android.graphics.PorterDuff.Mode.SRC_IN
        );
    }
}
