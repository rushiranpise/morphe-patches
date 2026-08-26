package app.template.extension.extension;

/**
 * Parsed-origin trust checks for Amazon URLs.
 *
 * Deliberately free of Android APIs so the logic is covered by plain JVM unit
 * tests (see AmazonUrlsTest). Trust is decided from a parsed URI and a
 * validated hostname, never from substring matching on the whole URL.
 */

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AmazonUrls {

    private AmazonUrls() {
    }

    /**
     * Registrable domains of the Amazon marketplaces these patches target,
     * mapped to the marketplace country code used by the price-history
     * providers. A small maintained allowlist rather than a broad pattern:
     * an unknown "amazon-ish" host must fail closed.
     */
    private static final Map<String, String> MARKETPLACES;

    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("amazon.com", "us");
        m.put("amazon.co.uk", "uk");
        m.put("amazon.de", "de");
        m.put("amazon.fr", "fr");
        m.put("amazon.co.jp", "jp");
        m.put("amazon.ca", "ca");
        m.put("amazon.it", "it");
        m.put("amazon.es", "es");
        m.put("amazon.in", "in");
        m.put("amazon.nl", "nl");
        m.put("amazon.se", "se");
        m.put("amazon.pl", "pl");
        m.put("amazon.ie", "ie");
        m.put("amazon.ae", "ae");
        m.put("amazon.sa", "sa");
        m.put("amazon.eg", "eg");
        m.put("amazon.sg", "sg");
        m.put("amazon.com.au", "au");
        m.put("amazon.com.br", "br");
        m.put("amazon.com.mx", "mx");
        m.put("amazon.com.be", "be");
        m.put("amazon.com.tr", "tr");
        MARKETPLACES = Collections.unmodifiableMap(m);
    }

    /**
     * Amazon-operated asset domains ordinary in-app navigation touches.
     * Amazon URL shorteners (a.co, amzn.to, ...) are intentionally absent: an
     * unfamiliar shortener goes to the external browser rather than widening
     * the internal trust boundary.
     */
    private static final String[] ASSET_DOMAINS = {
        "media-amazon.com",
        "ssl-images-amazon.com",
    };

    private static final Pattern ASIN_PATH = Pattern.compile(
        "/(?:dp|gp/product|gp/aw/d)/([A-Za-z0-9]{10})(?![A-Za-z0-9])");

    /**
     * Returns the normalized hostname of a well-formed http(s) URL, or null.
     *
     * Null for: malformed URIs, relative/hostless URLs, opaque URIs
     * (javascript:, data:, mailto:), non-web schemes (file:, content:, intent:)
     * and URLs carrying userinfo (https://amazon.com@evil.example/).
     */
    public static String webHost(String url) {
        if (url == null) return null;
        String trimmed = url.trim();
        if (trimmed.isEmpty()) return null;

        URI uri;
        try {
            uri = new URI(trimmed);
        } catch (URISyntaxException e) {
            return null;
        }

        if (!uri.isAbsolute() || uri.isOpaque()) return null;

        String scheme = uri.getScheme();
        if (scheme == null) return null;
        scheme = scheme.toLowerCase(Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https")) return null;

        // Userinfo is never legitimate for Amazon page navigation and is the
        // classic way to make a hostile host look like amazon.com.
        if (uri.getRawUserInfo() != null) return null;

        // getHost() is null when the authority is not a valid registry host,
        // which is exactly the fail-closed outcome we want.
        String host = uri.getHost();
        if (host == null) return null;

        host = host.toLowerCase(Locale.ROOT);
        while (host.endsWith(".")) host = host.substring(0, host.length() - 1);
        return host.isEmpty() ? null : host;
    }

    /** True when the host is an Amazon marketplace/asset host, matched on dot boundaries. */
    public static boolean isAmazonHost(String host) {
        if (host == null) return false;
        if (marketplaceDomain(host) != null) return true;
        for (String domain : ASSET_DOMAINS) {
            if (matches(host, domain)) return true;
        }
        return false;
    }

    /** True only for a well-formed http(s) URL whose validated host belongs to Amazon. */
    public static boolean isTrustedAmazonUrl(String url) {
        return isAmazonHost(webHost(url));
    }

    /** The registrable Amazon marketplace domain for a host, or null. */
    public static String marketplaceDomain(String host) {
        if (host == null) return null;
        for (String domain : MARKETPLACES.keySet()) {
            if (matches(host, domain)) return domain;
        }
        return null;
    }

    /** Marketplace country code (us, uk, in, ...) for a host, or null. */
    public static String marketplaceCode(String host) {
        String domain = marketplaceDomain(host);
        return domain == null ? null : MARKETPLACES.get(domain);
    }

    /** The ASIN from a trusted Amazon product URL path, uppercased, or null. */
    public static String productAsin(String url) {
        String host = webHost(url);
        if (!isAmazonHost(host)) return null;

        URI uri;
        try {
            uri = new URI(url.trim());
        } catch (URISyntaxException e) {
            return null;
        }
        String path = uri.getPath();
        if (path == null) return null;

        Matcher m = ASIN_PATH.matcher(path);
        return m.find() ? m.group(1).toUpperCase(Locale.ROOT) : null;
    }

    /** Canonical product URL without tracking parameters, or null when not a product page. */
    public static String canonicalProductUrl(String url) {
        String asin = productAsin(url);
        if (asin == null) return null;
        String domain = marketplaceDomain(webHost(url));
        if (domain == null) return null;
        return "https://www." + domain + "/dp/" + asin;
    }

    private static boolean matches(String host, String domain) {
        return host.equals(domain) || host.endsWith("." + domain);
    }
}
