package app.template.extension.extension;

/**
 * AmazonHelper — runtime injection helper for Amazon Shopping patches.
 *
 * Ad-blocking selectors and dark-mode CSS are inspired by and partially
 * derived from amznkiller by hxreborn:
 *   https://github.com/hxreborn/amznkiller
 *   License: GPL-3.0
 *
 * Everything this helper executes or injects is vendored in this repository and
 * shipped inside the patch bundle. Nothing is downloaded at runtime, so the
 * JavaScript and CSS applied to an authenticated Amazon page are fully
 * determined by reviewed source at build time.
 */

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AmazonHelper {

    // ── Ad selectors (vendored, static) ──────────────────────────────────────

    /**
     * Vendored selector list — mirrors amznkiller lists/static.txt (GPL-3.0,
     * hxreborn). Updating these rules is a reviewed commit plus a new patch
     * bundle, never a runtime download, so no remote party can restyle or
     * reshape the authenticated Amazon UI.
     */
    private static final String[] AD_SELECTORS = {
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

    private static volatile String adCss;

    /**
     * Builds the hide-only stylesheet from the vendored selectors. A selector is
     * skipped if it could terminate its own rule and open another, so the
     * generated CSS can only ever hide elements.
     */
    private static String adCss() {
        String css = adCss;
        if (css != null) return css;
        StringBuilder sb = new StringBuilder();
        for (String selector : AD_SELECTORS) {
            String s = selector.trim();
            if (s.isEmpty()
                || s.indexOf('{') >= 0 || s.indexOf('}') >= 0
                || s.indexOf(';') >= 0 || s.indexOf('@') >= 0
                || s.contains("/*") || s.contains("*/")) continue;
            sb.append(s).append("{display:none!important}");
        }
        css = sb.toString();
        adCss = css;
        return css;
    }

    /** Applies the vendored ad-hiding stylesheet. Performs no network access. */
    public static void injectAdBlock(WebView webView) {
        if (webView == null) return;
        webView.evaluateJavascript(
            "(function(css){var id='morphe-amazon-ads';"
            + "var s=document.getElementById(id);"
            + "if(!s){s=document.createElement('style');s.id=id;"
            + "(document.head||document.documentElement).appendChild(s);}"
            + "s.textContent=css;})(" + jsonString(adCss()) + ");", null);
    }

    // ── Price history (explicitly user initiated) ───────────────────────────

    // graph.keepa.com takes a marketplace country code as its `domain`
    // parameter, while keepa.com product links need Keepa's numeric marketplace
    // id. Keepa serves no data for Amazon Australia, so `au` is absent here.
    private static final Map<String, Integer> KEEPA_IDS = new HashMap<>();
    static {
        KEEPA_IDS.put("us", 1);  KEEPA_IDS.put("uk", 2);  KEEPA_IDS.put("de", 3);
        KEEPA_IDS.put("fr", 4);  KEEPA_IDS.put("jp", 5);  KEEPA_IDS.put("ca", 6);
        KEEPA_IDS.put("it", 8);  KEEPA_IDS.put("es", 9);  KEEPA_IDS.put("in", 10);
        KEEPA_IDS.put("mx", 11); KEEPA_IDS.put("br", 12); KEEPA_IDS.put("nl", 14);
    }

    // CamelCamelCamel tracks fewer marketplaces; its link host is
    // {code}.camelcamelcamel.com except the US, which has no subdomain.
    private static final Set<String> CCC_CODES = new LinkedHashSet<>(Arrays.asList(
        "us", "uk", "de", "fr", "jp", "ca", "it", "es", "au"));

    private static final List<String> PERIODS =
        Arrays.asList("1m", "6m", "1y", "3y", "5y", "all");
    private static final Map<String, String> PERIOD_LABELS = new HashMap<>();
    private static final Map<String, Integer> PERIOD_RANGES = new HashMap<>();
    static {
        PERIOD_LABELS.put("1m", "1M");  PERIOD_LABELS.put("6m", "6M");
        PERIOD_LABELS.put("1y", "1Y");  PERIOD_LABELS.put("3y", "3Y");
        PERIOD_LABELS.put("5y", "5Y");  PERIOD_LABELS.put("all", "ALL");
        PERIOD_RANGES.put("1m", 30);    PERIOD_RANGES.put("6m", 180);
        PERIOD_RANGES.put("1y", 365);   PERIOD_RANGES.put("3y", 1095);
        PERIOD_RANGES.put("5y", 1826);
    }

    private static final Map<String, String> CCC_PNG = new HashMap<>();
    static {
        CCC_PNG.put("new_used", "amazon-new-used");
        CCC_PNG.put("new", "amazon-new");
        CCC_PNG.put("used", "amazon-used");
    }

    /** Containers the price-history block is inserted after, most specific first. */
    private static final String[] PLACEMENT_TARGETS = {
        "#buyBoxAccordion", "#corePriceDisplay_desktop_feature_div",
        "#corePrice_feature_div", "#unifiedPrice_feature_div",
        "#mobileapp_buybox_feature_div", "#desktop_buybox", "#buybox",
        "#price_feature_div", "#newAccordionRow", "#productOverview_feature_div",
        "#centerCol", "#mobileapp_accordion_feature_div", "#apex_mobile",
        "#apex_desktop", "[id^=corePrice]",
    };

    /**
     * Static price-history UI.
     *
     * Rendering it contacts nobody: the script only builds a disclosure and a
     * "Load price history" button, and the provider <img> elements are created
     * solely inside that button's click handler. Charts are then ordinary
     * browser resource loads sent with no referrer, so no Amazon cookies,
     * headers, account identifiers or page data are supplied to the providers.
     *
     * The script itself is a constant; every runtime value arrives as a
     * JSON-encoded argument, never as concatenated JavaScript source.
     */
    private static final String PRICE_HISTORY_JS =
        "function(cfg){"
        + "var ID='morphe-price-history';"
        + "var ASIN_RE=new RegExp('/(?:dp|gp/product|gp/aw/d)/([A-Za-z0-9]{10})(?![A-Za-z0-9])');"
        + "function toLink(a,label){if(!a.querySelector('img'))return;"
        + "a.textContent='Open price history on '+label;"
        + "a.style.cssText='font-size:13px;color:#0066c0;text-decoration:underline';}"
        + "function keepaChart(asin,period){if(!cfg.keepa)return null;"
        + "var range=cfg.keepa.ranges[period];"
        + "return cfg.keepa.chart+asin+(range?'&range='+range:'');}"
        + "function camelChart(asin,period){if(!cfg.camel)return null;"
        + "return cfg.camel.chart+asin+'/'+cfg.camel.png+'.png?force=1&legend=1&w='"
        + "+cfg.camel.width+'&h=400'+cfg.camel.zero+'&tp='+period;}"
        + "function addChart(parent,label,href,chartUrl,asin){"
        + "var wrap=document.createElement('div');wrap.style.marginBottom='8px';"
        + "var cap=document.createElement('div');"
        + "cap.style.cssText='font-size:12px;color:#666;margin-bottom:4px';"
        + "cap.textContent=label;wrap.appendChild(cap);"
        + "var a=document.createElement('a');a.href=href;a.target='_blank';"
        + "a.rel='noopener noreferrer';"
        + "var img=document.createElement('img');img.referrerPolicy='no-referrer';"
        + "img.style.cssText='width:100%;height:auto;border-radius:4px';"
        + "img.onerror=function(){toLink(a,label);};"
        + "img.src=chartUrl(asin,cfg.period)+'&t='+Date.now();"
        + "a.appendChild(img);wrap.appendChild(a);parent.appendChild(wrap);"
        + "if(cfg.showToggle&&cfg.periods.length){"
        + "var row=document.createElement('div');row.style.marginTop='4px';"
        + "cfg.periods.forEach(function(p){"
        + "var b=document.createElement('button');b.type='button';b.textContent=p.label;"
        + "b.style.cssText='margin-right:6px;padding:2px 8px;border:1px solid #ccc;"
        + "border-radius:3px;background:#fff;font-size:11px;color:#333;cursor:pointer';"
        + "b.onclick=function(){img.onerror=function(){toLink(a,label);};"
        + "img.src=chartUrl(asin,p.value)+'&t='+Date.now();};"
        + "row.appendChild(b);});"
        + "wrap.appendChild(row);}}"
        + "function loadCharts(parent,asin){"
        + "if(cfg.keepa)addChart(parent,'Keepa',cfg.keepa.link+asin,keepaChart,asin);"
        + "if(cfg.camel)addChart(parent,'CamelCamelCamel',cfg.camel.link+asin,camelChart,asin);}"
        + "function place(box){for(var i=0;i<cfg.targets.length;i++){"
        + "var el=document.querySelector(cfg.targets[i]);"
        + "if(el&&el.parentNode){el.parentNode.insertBefore(box,el.nextSibling);return true;}}"
        + "return false;}"
        + "function build(asin){"
        + "var old=document.getElementById(ID);"
        + "if(old&&old.getAttribute('data-asin')===asin)return;"
        + "if(old)old.remove();"
        + "var box=document.createElement(cfg.collapsed?'details':'div');"
        + "box.id=ID;box.setAttribute('data-asin',asin);"
        + "box.style.cssText='margin:16px 0;padding:12px;border:1px solid #ddd;"
        + "border-radius:8px;background:#fafafa';"
        + "var head=document.createElement(cfg.collapsed?'summary':'div');"
        + "head.style.cssText='font-weight:bold;font-size:14px;color:#333'"
        + "+(cfg.collapsed?';cursor:pointer':';margin-bottom:8px');"
        + "head.textContent='Price History';box.appendChild(head);"
        + "var note=document.createElement('div');"
        + "note.style.cssText='font-size:12px;color:#666;margin:6px 0';"
        + "note.textContent=cfg.notice;box.appendChild(note);"
        + "var charts=document.createElement('div');"
        + "var btn=document.createElement('button');btn.type='button';"
        + "btn.textContent=cfg.loadLabel;"
        + "btn.style.cssText='padding:6px 12px;border:1px solid #ccc;border-radius:4px;"
        + "background:#fff;font-size:13px;color:#333;cursor:pointer';"
        + "btn.onclick=function(){btn.remove();loadCharts(charts,asin);};"
        + "box.appendChild(btn);box.appendChild(charts);"
        + "if(!place(box)){"
        + "var fb=document.getElementById('dp')||document.getElementById('ppd');"
        + "if(fb)fb.appendChild(box);"
        + "else{var obs=new MutationObserver(function(_,o){if(place(box))o.disconnect();});"
        + "obs.observe(document.body||document.documentElement,{childList:true,subtree:true});"
        + "setTimeout(function(){obs.disconnect();"
        + "if(!box.parentNode)document.body.appendChild(box);},10000);}}}"
        + "function currentAsin(){var m=window.location.pathname.match(ASIN_RE);"
        + "return m?m[1].toUpperCase():null;}"
        + "build(currentAsin()||cfg.asin);"
        + "if(!window.__morphePriceHistoryNav){window.__morphePriceHistoryNav=true;"
        + "var onNav=function(){var a=currentAsin();if(a)build(a);};"
        + "var ps=history.pushState,rs=history.replaceState;"
        + "history.pushState=function(){ps.apply(this,arguments);setTimeout(onNav,0);};"
        + "history.replaceState=function(){rs.apply(this,arguments);setTimeout(onNav,0);};"
        + "window.addEventListener('popstate',onNav);}"
        + "}";

    public static void injectPriceCharts(WebView webView, String url, String config) {
        if (webView == null) return;

        // Both trust and product identity come from the parsed URL.
        String asin = AmazonUrls.productAsin(url);
        if (asin == null) return;
        String code = AmazonUrls.marketplaceCode(AmazonUrls.webHost(url));
        if (code == null) return;

        // The patch packs its settings into one pipe-separated string:
        // period|showToggle|togglePeriods|keepaSeries|cccType|hideZero|width|collapsed
        String[] parts = config == null ? new String[0] : config.split("\\|", -1);
        String period = oneOf(part(parts, 0), PERIODS, "1y");
        boolean showToggle = !"0".equals(part(parts, 1));
        String series = oneOf(part(parts, 3), Arrays.asList("new", "new_used", "all"), "all");
        String cccType = oneOf(part(parts, 4), Arrays.asList("new_used", "new", "used"), "new_used");
        boolean hideZero = "1".equals(part(parts, 5));
        String width = oneOf(part(parts, 6), Arrays.asList("500", "625", "750"), "625");
        boolean collapsed = !"0".equals(part(parts, 7));

        Integer keepaId = KEEPA_IDS.get(code);
        boolean camel = CCC_CODES.contains(code);
        if (keepaId == null && !camel) return;

        List<String> providers = new ArrayList<>();
        if (keepaId != null) providers.add("Keepa");
        if (camel) providers.add("CamelCamelCamel");

        StringBuilder cfg = new StringBuilder("{");
        cfg.append("\"asin\":").append(jsonString(asin));
        cfg.append(",\"period\":").append(jsonString(period));
        cfg.append(",\"showToggle\":").append(showToggle);
        cfg.append(",\"collapsed\":").append(collapsed);
        cfg.append(",\"periods\":").append(periodsJson(part(parts, 2)));
        cfg.append(",\"targets\":").append(jsonArray(PLACEMENT_TARGETS));
        cfg.append(",\"loadLabel\":").append(jsonString("Load price history"));
        cfg.append(",\"notice\":").append(jsonString(
            "Price history is not loaded. Loading it requests charts directly from "
            + join(providers, " and ")
            + ", which shares this product's ASIN with "
            + (providers.size() > 1 ? "those providers." : "that provider.")));

        if (keepaId != null) {
            cfg.append(",\"keepa\":{\"chart\":").append(jsonString(
                "https://graph.keepa.com/pricehistory.png?new=1"
                + "&used=" + ("new".equals(series) ? "0" : "1")
                + "&amazon=" + ("all".equals(series) ? "1" : "0")
                + "&domain=" + code + "&asin="));
            cfg.append(",\"link\":").append(jsonString(
                "https://keepa.com/#!product/" + keepaId + "-"));
            cfg.append(",\"ranges\":").append(rangesJson()).append("}");
        } else {
            cfg.append(",\"keepa\":null");
        }

        if (camel) {
            cfg.append(",\"camel\":{\"chart\":").append(jsonString(
                "https://charts.camelcamelcamel.com/" + code + "/"));
            cfg.append(",\"link\":").append(jsonString(
                "https://" + ("us".equals(code) ? "" : code + ".")
                + "camelcamelcamel.com/product/"));
            cfg.append(",\"png\":").append(jsonString(CCC_PNG.get(cccType)));
            cfg.append(",\"width\":").append(jsonString(width));
            cfg.append(",\"zero\":").append(jsonString(hideZero ? "&zero=0" : "")).append("}");
        } else {
            cfg.append(",\"camel\":null");
        }
        cfg.append("}");

        webView.evaluateJavascript("(" + PRICE_HISTORY_JS + ")(" + cfg + ");", null);
    }

    // ── Dark mode (inspired by amznkiller dark_mode.js, GPL-3.0, hxreborn) ──

    /** Vendored dark-mode fixups — mirrors amznkiller dark_mode.js (GPL-3.0, hxreborn) */
    private static final String DARK_FIX_CSS =
        "[class*=image-container] img{mix-blend-mode:normal!important}"
        + "img[style*=\"mix-blend-mode\"]{mix-blend-mode:normal!important}"
        + "[class*=asin-metadata]{mix-blend-mode:normal!important}"
        + "html{background-color:#1a1a1a!important}"
        + "body{background-color:#1a1a1a!important}"
        + "[class*=asin-container],[class*=asin-image-wrapper]{background-color:white!important}"
        + "[class*=badgeMessage]{background-color:transparent!important}"
        + ".a-button-primary,.a-button-oneclick{color-scheme:only light!important}";

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

    /**
     * Applies the vendored dark-mode stylesheet. Performs no network access, so
     * dark mode works offline and cannot be altered by any external repository.
     */
    public static void injectDarkMode(WebView webView, String mode) {
        if (webView == null || !isDarkEnabled(mode)) return;
        webView.evaluateJavascript(
            "(function(css){var id='morphe-amazon-dark';"
            + "var s=document.getElementById(id);"
            + "if(!s){s=document.createElement('style');s.id=id;"
            + "(document.head||document.documentElement).appendChild(s);}"
            + "s.textContent=css;})(" + jsonString(DARK_FIX_CSS) + ");", null);
    }

    // ── External navigation ──────────────────────────────────────────

    /**
     * Decides whether a WebView navigation must leave the Amazon app, returning
     * true when the destination was handed to the external browser and the
     * navigation should be reported as handled.
     *
     * Trust comes from the parsed hostname, so destinations that merely contain
     * the text "amazon." outside the host (evilamazon.com,
     * amazon.com.evil.example, https://amazon.com@evil.example/,
     * https://evil.example/?next=amazon.com) are treated as external. Only
     * http(s) URLs are ever handed to another app, so this can never launch an
     * intent:, file: or content: target.
     *
     * Malformed, hostless and non-web URLs are consumed without launching or
     * rendering them. This is intentionally fail-closed: javascript:, data:,
     * file:, content: and intent: never gain authenticated WebView treatment.
     */
    public static boolean openExternally(WebView webView, String url) {
        if (webView == null) return true;
        String host = AmazonUrls.webHost(url);
        if (host == null) return true;
        if (AmazonUrls.isAmazonHost(host)) return false;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.trim()));
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            webView.getContext().startActivity(intent);
        } catch (Exception e) {
            // No external handler: still refuse to render untrusted content
            // inside the authenticated Amazon WebView.
        }
        return true;
    }

    // ── Sanitize share URL ───────────────────────────────────────────

    /** Strips Amazon tracking parameters, returning a canonical /dp/ASIN URL. */
    public static String sanitizeAmazonUrl(String url) {
        String canonical = AmazonUrls.canonicalProductUrl(url);
        return canonical != null ? canonical : url;
    }

    // ── Tab bar icon tint ────────────────────────────────────────────

    /**
     * Called with the ImageView returned by BaseTabController.getTabIcon() so
     * the icon stays visible against a dark background.
     */
    public static void tintTabIconIfDark(android.widget.ImageView icon, String mode) {
        if (icon == null || !isDarkEnabled(mode)) return;
        icon.setColorFilter(
            android.graphics.Color.WHITE,
            android.graphics.PorterDuff.Mode.SRC_IN
        );
    }

    // ── Encoding helpers ─────────────────────────────────────────────

    private static String part(String[] parts, int index) {
        return index < parts.length ? parts[index] : null;
    }

    private static String oneOf(String value, List<String> allowed, String fallback) {
        return value != null && allowed.contains(value) ? value : fallback;
    }

    private static String join(List<String> values, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(separator);
            sb.append(values.get(i));
        }
        return sb.toString();
    }

    private static String periodsJson(String raw) {
        Set<String> selected = new LinkedHashSet<>();
        if (raw != null) {
            for (String value : raw.split(",")) {
                String v = value.trim();
                if (PERIODS.contains(v)) selected.add(v);
            }
        }
        if (selected.isEmpty()) selected.addAll(PERIODS);
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String value : selected) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{\"label\":").append(jsonString(PERIOD_LABELS.get(value)))
                .append(",\"value\":").append(jsonString(value)).append("}");
        }
        return sb.append("]").toString();
    }

    private static String rangesJson() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : PERIOD_RANGES.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append(jsonString(entry.getKey())).append(":").append(entry.getValue());
        }
        return sb.append("}").toString();
    }

    private static String jsonArray(String[] values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(jsonString(values[i]));
        }
        return sb.append("]").toString();
    }

    /**
     * JSON-encodes a value for embedding in generated JavaScript. Quotes,
     * backslashes, control characters, angle brackets, ampersands, apostrophes
     * and the JS line terminators are escaped, so no runtime value — ASIN,
     * marketplace, option string, label, CSS or selector — can be parsed as
     * JavaScript syntax.
     */
    private static String jsonString(String value) {
        if (value == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20 || c > 0x7e || c == '<' || c == '>' || c == '&'
                        || c == '\'' || c == 0x2028 || c == 0x2029) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append("\"").toString();
    }
}
