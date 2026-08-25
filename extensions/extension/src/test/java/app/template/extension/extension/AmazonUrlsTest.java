package app.template.extension.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Adversarial coverage for Amazon origin trust decisions. */
public class AmazonUrlsTest {

    @Test
    public void trustsSupportedMarketplacesAndSubdomains() {
        assertTrue(AmazonUrls.isTrustedAmazonUrl("https://www.amazon.com/dp/B0CX23V2ZK"));
        assertTrue(AmazonUrls.isTrustedAmazonUrl("https://amazon.com/"));
        assertTrue(AmazonUrls.isTrustedAmazonUrl("https://www.amazon.in/dp/B0CX23V2ZK?ref=foo"));
        assertTrue(AmazonUrls.isTrustedAmazonUrl("https://smile.amazon.co.uk/gp/product/B0CX23V2ZK"));
        assertTrue(AmazonUrls.isTrustedAmazonUrl("http://www.amazon.co.jp/"));
        assertTrue(AmazonUrls.isTrustedAmazonUrl("https://m.media-amazon.com/images/I/x.jpg"));
        assertTrue(AmazonUrls.isTrustedAmazonUrl("https://WWW.AMAZON.COM/dp/B0CX23V2ZK"));
        assertTrue(AmazonUrls.isTrustedAmazonUrl("https://www.amazon.com./dp/B0CX23V2ZK"));
    }

    @Test
    public void rejectsLookalikeHosts() {
        assertFalse(AmazonUrls.isTrustedAmazonUrl("https://evilamazon.com/"));
        assertFalse(AmazonUrls.isTrustedAmazonUrl("https://amazon.com.evil.example/"));
        assertFalse(AmazonUrls.isTrustedAmazonUrl("https://notamazon.co.uk/"));
        assertFalse(AmazonUrls.isTrustedAmazonUrl("https://amazon.com.br.evil.example/dp/B0CX23V2ZK"));
        assertFalse(AmazonUrls.isTrustedAmazonUrl("https://xn--amazon-jua.com/"));
    }

    @Test
    public void rejectsAmazonTextOutsideHost() {
        assertFalse(AmazonUrls.isTrustedAmazonUrl("https://evil.example/amazon.com"));
        assertFalse(AmazonUrls.isTrustedAmazonUrl("https://evil.example/?next=amazon.com"));
        assertFalse(AmazonUrls.isTrustedAmazonUrl("https://evil.example/#www.amazon.com/dp/B0CX23V2ZK"));
        assertFalse(AmazonUrls.isTrustedAmazonUrl("https://evil.example/a?x=amazon."));
    }

    @Test
    public void rejectsUserInfoTricks() {
        assertFalse(AmazonUrls.isTrustedAmazonUrl("https://amazon.com@evil.example/"));
        assertFalse(AmazonUrls.isTrustedAmazonUrl("https://www.amazon.com:pass@evil.example/dp/B0CX23V2ZK"));
        assertNull(AmazonUrls.webHost("https://amazon.com@evil.example/"));
    }

    @Test
    public void rejectsNonWebSchemesAndMalformedInput() {
        assertFalse(AmazonUrls.isTrustedAmazonUrl("javascript:alert(document.cookie)//amazon.com"));
        assertFalse(AmazonUrls.isTrustedAmazonUrl("data:text/html;base64,PHNjcmlwdD4x"));
        assertFalse(AmazonUrls.isTrustedAmazonUrl("file:///sdcard/amazon.com/index.html"));
        assertFalse(AmazonUrls.isTrustedAmazonUrl("content://com.amazon.provider/item"));
        assertFalse(AmazonUrls.isTrustedAmazonUrl("intent://www.amazon.com/#Intent;scheme=https;end"));
        assertFalse(AmazonUrls.isTrustedAmazonUrl("//www.amazon.com/dp/B0CX23V2ZK"));
        assertFalse(AmazonUrls.isTrustedAmazonUrl("www.amazon.com"));
        assertFalse(AmazonUrls.isTrustedAmazonUrl("https://"));
        assertFalse(AmazonUrls.isTrustedAmazonUrl("ht tp://www.amazon.com"));
        assertFalse(AmazonUrls.isTrustedAmazonUrl(""));
        assertFalse(AmazonUrls.isTrustedAmazonUrl(null));
    }

    @Test
    public void resolvesMarketplaceCodes() {
        assertEquals("us", AmazonUrls.marketplaceCode("www.amazon.com"));
        assertEquals("in", AmazonUrls.marketplaceCode("www.amazon.in"));
        assertEquals("uk", AmazonUrls.marketplaceCode("amazon.co.uk"));
        assertEquals("au", AmazonUrls.marketplaceCode("www.amazon.com.au"));
        assertNull(AmazonUrls.marketplaceCode("m.media-amazon.com"));
        assertNull(AmazonUrls.marketplaceCode("amazon.com.evil.example"));
    }

    @Test
    public void extractsAsinFromPathOnly() {
        assertEquals("B0CX23V2ZK", AmazonUrls.productAsin("https://www.amazon.com/dp/B0CX23V2ZK?th=1"));
        assertEquals("B0CX23V2ZK", AmazonUrls.productAsin("https://www.amazon.in/gp/aw/d/b0cx23v2zk/"));
        assertEquals("B0CX23V2ZK",
            AmazonUrls.productAsin("https://www.amazon.co.uk/Some-Product-Name/dp/B0CX23V2ZK/ref=sr_1_1"));
        assertNull(AmazonUrls.productAsin("https://www.amazon.com/s?k=/dp/B0CX23V2ZK"));
        assertNull(AmazonUrls.productAsin("https://evil.example/dp/B0CX23V2ZK"));
        assertNull(AmazonUrls.productAsin("https://www.amazon.com/dp/B0CX23V2ZKEXTRA"));
        assertNull(AmazonUrls.productAsin("https://www.amazon.com/"));
    }

    @Test
    public void canonicalizesProductUrls() {
        assertEquals("https://www.amazon.com/dp/B0CX23V2ZK",
            AmazonUrls.canonicalProductUrl("https://www.amazon.com/gp/product/B0CX23V2ZK?tag=aff-20&psc=1"));
        assertEquals("https://www.amazon.in/dp/B0CX23V2ZK",
            AmazonUrls.canonicalProductUrl("https://www.amazon.in/dp/b0cx23v2zk/ref=x"));
        assertNull(AmazonUrls.canonicalProductUrl("https://evilamazon.com/dp/B0CX23V2ZK"));
    }
}
